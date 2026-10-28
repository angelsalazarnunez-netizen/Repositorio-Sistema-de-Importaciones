-- Autor: Equipo de Desarrollo SIM
-- Fecha: 05/10/2026
-- Descripción: Crea un usuario administrador para probar todas las funcionalidades en el
--   ambiente local: correo validado, rol ADMINISTRADOR asignado y todos los permisos activos
--   del catálogo SIMM_PERMISO asociados a ese rol. NO debe ejecutarse en producción.
-- Motor: MySQL 8+
--
-- Credenciales de prueba (solo ambiente local):
--   Correo:     admin.pruebas@ucr.ac.cr
--   Contraseña: Sim2026*Pruebas
--   CONTRASENA_HASH es el BCrypt de esa contraseña, el mismo algoritmo de CfgSeguridad.
--
-- Requisito previo: ejecutar 02-Tablas-Tramites.sql y 04-Tabla-Sesion-HU004.sql.
-- El script puede ejecutarse más de una vez: no duplica registros. Si después se agregan
-- permisos al catálogo, basta con volver a ejecutarlo para asignárselos al administrador.

SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- Usuario. ESTADO_VALIDACION = 'VAL' (correo validado) y ESTADO_ROL = 'ASI' (rol asignado)
-- son las dos condiciones que exige el inicio de sesión (HU-004).
-- ---------------------------------------------------------------------------

INSERT INTO SIMF_USUARIO (
    NOMBRE_COMPLETO,
    NUMERO_CEDULA,
    FECHA_NACIMIENTO,
    CORREO_INSTITUCIONAL,
    CONTRASENA_HASH,
    ESTADO_VALIDACION,
    ESTADO_ROL
) VALUES (
    'Administrador de Pruebas',
    '100000099',
    '1990-01-01',
    'admin.pruebas@ucr.ac.cr',
    '$2a$10$nUd0NYIs4gwOUAOjQXVlS.ExV6SVdBu5h1D.18nwcEWwlLKPily2y',
    'VAL',
    'ASI'
)
ON DUPLICATE KEY UPDATE
    CONTRASENA_HASH   = VALUES(CONTRASENA_HASH),
    ESTADO_VALIDACION = 'VAL',
    ESTADO_ROL        = 'ASI';

SET @vln_IdUsuario = (
    SELECT ID_USUARIO FROM SIMF_USUARIO WHERE CORREO_INSTITUCIONAL = 'admin.pruebas@ucr.ac.cr'
);

-- ---------------------------------------------------------------------------
-- Rol con todos los permisos existentes.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_ROL (NOMBRE, DESCRIPCION, ESTADO)
VALUES ('ADMINISTRADOR', 'Acceso a todas las funcionalidades del sistema.', 'ACT')
ON DUPLICATE KEY UPDATE ESTADO = 'ACT';

SET @vln_IdRol = (SELECT ID_ROL FROM SIMM_ROL WHERE NOMBRE = 'ADMINISTRADOR');

INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO)
SELECT @vln_IdRol, P.ID_PERMISO, 'ACT'
FROM SIMM_PERMISO P
WHERE P.ESTADO = 'ACT'
  AND NOT EXISTS (
      SELECT 1
      FROM SIMT_ROL_PERMISO RP
      WHERE RP.ID_ROL = @vln_IdRol
        AND RP.ID_PERMISO = P.ID_PERMISO
        AND RP.ESTADO = 'ACT'
  );

-- ---------------------------------------------------------------------------
-- Asignación del rol al usuario. El propio administrador queda como asignador.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_USUARIO_ROL (ID_USUARIO, ID_ROL, ID_USUARIO_ASIGNADOR, MOTIVO, ESTADO)
SELECT @vln_IdUsuario, @vln_IdRol, @vln_IdUsuario, 'Usuario de pruebas del ambiente local', 'ACT'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM SIMT_USUARIO_ROL UR
    WHERE UR.ID_USUARIO = @vln_IdUsuario
      AND UR.ID_ROL = @vln_IdRol
      AND UR.ESTADO = 'ACT'
      AND UR.FECHA_FINALIZACION IS NULL
);

-- Resultado: usuario, roles y cantidad de permisos que recibirá al iniciar sesión.
SELECT U.ID_USUARIO,
       U.CORREO_INSTITUCIONAL,
       U.ESTADO_VALIDACION,
       U.ESTADO_ROL,
       R.NOMBRE AS ROL,
       (SELECT COUNT(*) FROM SIMT_ROL_PERMISO RP
         WHERE RP.ID_ROL = R.ID_ROL AND RP.ESTADO = 'ACT') AS PERMISOS
FROM SIMF_USUARIO U
JOIN SIMT_USUARIO_ROL UR ON UR.ID_USUARIO = U.ID_USUARIO AND UR.ESTADO = 'ACT'
JOIN SIMM_ROL R ON R.ID_ROL = UR.ID_ROL
WHERE U.ID_USUARIO = @vln_IdUsuario;
