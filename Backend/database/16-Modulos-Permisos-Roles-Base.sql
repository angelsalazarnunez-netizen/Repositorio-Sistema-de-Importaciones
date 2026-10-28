-- Autor: Equipo de Desarrollo SIM
-- Fecha: 19/10/2026
-- Descripción: Fase 1 de la integración de las restricciones por rol (HU-001 Registrar roles o
--   cargos y HU-003 Asignar rol de usuario del sistema).
--     1. Agrega a SIMM_PERMISO la columna MODULO, que agrupa los permisos en los ocho módulos de
--        acceso que enumera la HU-003. Los permisos siguen siendo granulares (CONSULTAR, CREAR,
--        MODIFICAR...); el módulo solo los agrupa para el menú, la pantalla de roles y los roles
--        base.
--     2. Unifica el permiso de aprobar exoneraciones: queda EXONERACION_APROBAR (el que usan el
--        backend y el frontend desde la HU-022) y se inactiva APROBACION_EXONERACION, que era un
--        duplicado del catálogo de la rama de roles.
--     3. Registra los ocho roles base de la HU-003, cada uno con los permisos de su módulo.
--     4. Deja al rol ADMINISTRADOR y al rol JEFATURA_IMPORTACIONES con todos los permisos activos,
--        para que nadie pierda acceso cuando el sistema empiece a aplicar las restricciones.
-- Caso de uso asociado: HU-001 y HU-003
-- Motor: MySQL 8+
--
-- Módulos (columna MODULO). Deben coincidir con Enumerados.ModuloPermiso en el backend:
--   IMPORTACIONES   = Gestión de importaciones.
--   EXONERACIONES   = Gestión de exoneraciones.
--   ADUANERA        = Gestión aduanera (trámites temporales y reclamos).
--   DONACIONES      = Gestión de donaciones.
--   DESALMACENAJES  = Gestión de desalmacenajes especiales.
--   REPORTES        = Reportes y consultas.
--   APROBACIONES    = Aprobaciones.
--   ADMIN_USUARIOS  = Administración de usuarios.
--
-- Cómo agregar un permiso nuevo: insertarlo en SIMM_PERMISO con su MODULO y volver a ejecutar
-- este script; el rol base de su módulo, ADMINISTRADOR y JEFATURA_IMPORTACIONES lo reciben.
-- Nota: al volver a ejecutarlo, los roles base recuperan los permisos de su módulo que se les
-- hayan quitado manualmente. Los roles creados desde la pantalla no se modifican.
--
-- Requisito previo: ejecutar los scripts 02 a 15 (en especial 04, 10 y 14).
-- El script puede ejecutarse más de una vez: no duplica registros.

SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- 1. Columna MODULO en SIMM_PERMISO.
-- Se deja NULL para no romper los scripts anteriores que insertan permisos sin módulo; un
-- permiso sin módulo no aparece en ningún rol base (la consulta final lo señala).
-- MySQL 8 no admite ADD COLUMN IF NOT EXISTS: la columna se agrega solo si no existe.
-- ---------------------------------------------------------------------------

SET @vlc_Sentencia = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE SIMM_PERMISO ADD COLUMN MODULO VARCHAR(30) NULL AFTER DESCRIPCION',
              'SELECT 1')
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMM_PERMISO'
      AND COLUMN_NAME = 'MODULO'
);
PREPARE vlo_Sentencia FROM @vlc_Sentencia;
EXECUTE vlo_Sentencia;
DEALLOCATE PREPARE vlo_Sentencia;

SET @vlc_Sentencia = (
    SELECT IF(COUNT(*) = 0,
              'CREATE INDEX IDX_SIMM_PERMISO_MODULO ON SIMM_PERMISO (MODULO)',
              'SELECT 1')
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMM_PERMISO'
      AND INDEX_NAME = 'IDX_SIMM_PERMISO_MODULO'
);
PREPARE vlo_Sentencia FROM @vlc_Sentencia;
EXECUTE vlo_Sentencia;
DEALLOCATE PREPARE vlo_Sentencia;

-- ---------------------------------------------------------------------------
-- 2. Clasificación de los permisos existentes por módulo.
-- ---------------------------------------------------------------------------

UPDATE SIMM_PERMISO
SET MODULO = CASE
        WHEN CODIGO IN ('ROL_CONSULTAR', 'ROL_CREAR', 'ROL_MODIFICAR', 'ROL_ASIGNAR',
                        'USUARIO_CONSULTAR', 'USUARIO_MODIFICAR')
            THEN 'ADMIN_USUARIOS'
        WHEN CODIGO IN ('EXONERACION_APROBAR', 'APROBACION_EXONERACION', 'APROBACION_FONDOS')
            THEN 'APROBACIONES'
        WHEN CODIGO LIKE 'IMPORTACION\_%'   THEN 'IMPORTACIONES'
        WHEN CODIGO LIKE 'EXONERACION\_%'   THEN 'EXONERACIONES'
        WHEN CODIGO LIKE 'ADUANERO\_%'      THEN 'ADUANERA'
        WHEN CODIGO LIKE 'DONACION\_%'      THEN 'DONACIONES'
        WHEN CODIGO LIKE 'DESALMACENAJE\_%' THEN 'DESALMACENAJES'
        WHEN CODIGO LIKE 'REPORTE\_%'
          OR CODIGO IN ('GRAFICO_GENERAR', 'BITACORA_CONSULTAR')
            THEN 'REPORTES'
        ELSE MODULO
    END
WHERE MODULO IS NULL
   OR CODIGO IN ('EXONERACION_APROBAR', 'APROBACION_EXONERACION', 'APROBACION_FONDOS');

-- ---------------------------------------------------------------------------
-- 3. Permiso duplicado: APROBACION_EXONERACION se inactiva (no se borra, para conservar la
-- historia) y se retira de los roles que lo tengan. El permiso vigente es EXONERACION_APROBAR.
-- ---------------------------------------------------------------------------

UPDATE SIMT_ROL_PERMISO RP
JOIN SIMM_PERMISO P ON P.ID_PERMISO = RP.ID_PERMISO
SET RP.ESTADO = 'INA'
WHERE P.CODIGO = 'APROBACION_EXONERACION'
  AND RP.ESTADO = 'ACT';

UPDATE SIMM_PERMISO
SET ESTADO = 'INA',
    DESCRIPCION = 'Reemplazado por EXONERACION_APROBAR. Se conserva inactivo por historial.'
WHERE CODIGO = 'APROBACION_EXONERACION';

-- ---------------------------------------------------------------------------
-- 4. Roles base de la HU-003. Si el rol ya existe no se modifica (ni su estado ni su
-- descripción), para respetar lo que la jefatura haya cambiado desde la pantalla.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_ROL (NOMBRE, DESCRIPCION, ESTADO)
VALUES
    ('Gestión de importaciones',
        'Registrar, consultar y dar seguimiento a los pedidos de importación y sus pagos.', 'ACT'),
    ('Gestión de exoneraciones',
        'Registrar, consultar y tramitar solicitudes de exoneración y su documentación.', 'ACT'),
    ('Gestión aduanera',
        'Gestionar trámites aduaneros temporales, sus alertas de vencimiento y reclamos.', 'ACT'),
    ('Gestión de donaciones',
        'Registrar, consultar y dar seguimiento a los trámites de donación.', 'ACT'),
    ('Gestión de desalmacenajes especiales',
        'Registrar, consultar y dar seguimiento a los desalmacenajes especiales.', 'ACT'),
    ('Reportes y consultas',
        'Consultar, filtrar, generar y exportar reportes y búsquedas de trámites.', 'ACT'),
    ('Aprobaciones',
        'Aprobar, rechazar o pedir modificaciones de solicitudes que requieren aprobación.', 'ACT'),
    ('Administración de usuarios',
        'Registrar y modificar roles, y asignar o retirar roles a los usuarios del sistema.', 'ACT')
ON DUPLICATE KEY UPDATE NOMBRE = NOMBRE;

-- Relación rol base -> módulo. Es temporal: solo vive durante la ejecución del script.
DROP TEMPORARY TABLE IF EXISTS TMP_ROL_MODULO;
CREATE TEMPORARY TABLE TMP_ROL_MODULO (
    NOMBRE_ROL VARCHAR(50) NOT NULL,
    MODULO     VARCHAR(30) NOT NULL
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO TMP_ROL_MODULO (NOMBRE_ROL, MODULO)
VALUES
    ('Gestión de importaciones',             'IMPORTACIONES'),
    ('Gestión de exoneraciones',             'EXONERACIONES'),
    ('Gestión aduanera',                     'ADUANERA'),
    ('Gestión de donaciones',                'DONACIONES'),
    ('Gestión de desalmacenajes especiales', 'DESALMACENAJES'),
    ('Reportes y consultas',                 'REPORTES'),
    ('Aprobaciones',                         'APROBACIONES'),
    ('Administración de usuarios',           'ADMIN_USUARIOS');

-- Cada rol base recibe los permisos activos de su módulo.
INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO)
SELECT R.ID_ROL, P.ID_PERMISO, 'ACT'
FROM TMP_ROL_MODULO RM
JOIN SIMM_ROL R ON R.NOMBRE = RM.NOMBRE_ROL
JOIN SIMM_PERMISO P ON P.MODULO = RM.MODULO AND P.ESTADO = 'ACT'
WHERE NOT EXISTS (
    SELECT 1
    FROM SIMT_ROL_PERMISO RP
    WHERE RP.ID_ROL = R.ID_ROL
      AND RP.ID_PERMISO = P.ID_PERMISO
      AND RP.ESTADO = 'ACT'
);

-- Quien aprueba necesita ver el expediente que decide: el rol Aprobaciones también puede
-- consultar exoneraciones (solo lectura).
INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO)
SELECT R.ID_ROL, P.ID_PERMISO, 'ACT'
FROM SIMM_ROL R
JOIN SIMM_PERMISO P ON P.CODIGO = 'EXONERACION_CONSULTAR' AND P.ESTADO = 'ACT'
WHERE R.NOMBRE = 'Aprobaciones'
  AND NOT EXISTS (
      SELECT 1
      FROM SIMT_ROL_PERMISO RP
      WHERE RP.ID_ROL = R.ID_ROL
        AND RP.ID_PERMISO = P.ID_PERMISO
        AND RP.ESTADO = 'ACT'
  );

DROP TEMPORARY TABLE IF EXISTS TMP_ROL_MODULO;

-- ---------------------------------------------------------------------------
-- 5. ADMINISTRADOR (pruebas, script 09) y JEFATURA_IMPORTACIONES (script 10) reciben todos los
-- permisos activos. La jefatura es quien registra roles, los asigna y aprueba (HU-001, HU-003 y
-- HU-022); si se le quieren limitar permisos, se hace desde la pantalla de Gestionar roles.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO)
SELECT R.ID_ROL, P.ID_PERMISO, 'ACT'
FROM SIMM_ROL R
JOIN SIMM_PERMISO P ON P.ESTADO = 'ACT'
WHERE R.NOMBRE IN ('ADMINISTRADOR', 'JEFATURA_IMPORTACIONES')
  AND NOT EXISTS (
      SELECT 1
      FROM SIMT_ROL_PERMISO RP
      WHERE RP.ID_ROL = R.ID_ROL
        AND RP.ID_PERMISO = P.ID_PERMISO
        AND RP.ESTADO = 'ACT'
  );

-- ---------------------------------------------------------------------------
-- Verificación.
-- ---------------------------------------------------------------------------

-- Permisos activos sin módulo: debe devolver cero filas.
SELECT CODIGO AS PERMISO_SIN_MODULO
FROM SIMM_PERMISO
WHERE ESTADO = 'ACT'
  AND MODULO IS NULL;

-- Permisos por módulo.
SELECT MODULO, COUNT(*) AS PERMISOS_ACTIVOS
FROM SIMM_PERMISO
WHERE ESTADO = 'ACT'
GROUP BY MODULO
ORDER BY MODULO;

-- Roles y cantidad de permisos activos.
SELECT R.NOMBRE AS ROL,
       R.ESTADO,
       (SELECT COUNT(*)
          FROM SIMT_ROL_PERMISO RP
          JOIN SIMM_PERMISO P ON P.ID_PERMISO = RP.ID_PERMISO
         WHERE RP.ID_ROL = R.ID_ROL
           AND RP.ESTADO = 'ACT'
           AND P.ESTADO = 'ACT') AS PERMISOS
FROM SIMM_ROL R
ORDER BY R.NOMBRE;
