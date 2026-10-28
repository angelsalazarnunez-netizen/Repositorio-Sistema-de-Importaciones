-- Autor: Equipo de Desarrollo SIM
-- Fecha: 12/10/2026
-- Descripción: Catálogo de permisos del módulo de seguridad (HU-001 Registrar cuenta de usuario y
--   HU-003 Administrar y asignar roles). Proviene de sim_seed_permisos.sql de la rama de roles,
--   adaptado para que pueda ejecutarse más de una vez sin duplicar registros. Las tablas
--   SIMM_ROL, SIMM_PERMISO, SIMT_ROL_PERMISO y SIMT_USUARIO_ROL ya las crea
--   04-Tabla-Sesion-HU004.sql, y SIMF_USUARIO ya tiene las columnas que usa el registro
--   (02-Tablas-Tramites.sql), por lo que este script no modifica la estructura de la base.
-- Motor: MySQL 8+
--
-- Requisito previo: ejecutar 02-Tablas-Tramites.sql y 04-Tabla-Sesion-HU004.sql. Si existe el
-- rol ADMINISTRADOR de pruebas (09-Usuario-Pruebas-Administrador.sql), recibe estos permisos.
--
-- Historial de cambios:
--   Responsable: Equipo de Desarrollo SIM
--   Fecha: 19/10/2026
--   Descripción: se retira APROBACION_EXONERACION, duplicado de EXONERACION_APROBAR (script 10).
--     Si se dejaba aquí, cada ejecución lo reactivaba. El script 16 lo inactiva en las bases que
--     ya lo tenían y asigna el módulo de cada permiso.

SET NAMES utf8mb4;

USE sim;

INSERT INTO SIMM_PERMISO (CODIGO, NOMBRE, DESCRIPCION, ESTADO)
VALUES
    ('ROL_CONSULTAR', 'Consultar roles',
        'Permite consultar los roles registrados en el sistema.', 'ACT'),
    ('ROL_CREAR', 'Crear roles',
        'Permite registrar nuevos roles en el sistema.', 'ACT'),
    ('ROL_MODIFICAR', 'Modificar roles',
        'Permite modificar la informacion de los roles existentes.', 'ACT'),
    ('ROL_ASIGNAR', 'Asignar roles',
        'Permite asignar y retirar roles a los usuarios.', 'ACT'),
    ('USUARIO_CONSULTAR', 'Consultar usuarios',
        'Permite consultar la informacion de los usuarios del sistema.', 'ACT'),
    ('USUARIO_MODIFICAR', 'Modificar usuarios',
        'Permite modificar la informacion de los usuarios del sistema.', 'ACT'),
    ('IMPORTACION_CONSULTAR', 'Consultar importaciones',
        'Permite consultar tramites de importacion.', 'ACT'),
    ('IMPORTACION_CREAR', 'Crear importaciones',
        'Permite registrar nuevos tramites de importacion.', 'ACT'),
    ('IMPORTACION_MODIFICAR', 'Modificar importaciones',
        'Permite modificar tramites de importacion.', 'ACT'),
    ('IMPORTACION_PAGO_GESTIONAR', 'Gestionar pagos de importaciones',
        'Permite gestionar pagos asociados a tramites de importacion.', 'ACT'),
    ('IMPORTACION_DOCUMENTO_GENERAR', 'Generar documentos de importacion',
        'Permite generar documentos relacionados con tramites de importacion.', 'ACT'),
    ('EXONERACION_CONSULTAR', 'Consultar exoneraciones',
        'Permite consultar tramites de exoneracion.', 'ACT'),
    ('EXONERACION_CREAR', 'Crear exoneraciones',
        'Permite registrar tramites de exoneracion.', 'ACT'),
    ('EXONERACION_MODIFICAR', 'Modificar exoneraciones',
        'Permite modificar tramites de exoneracion.', 'ACT'),
    ('DONACION_CONSULTAR', 'Consultar donaciones',
        'Permite consultar tramites de donacion.', 'ACT'),
    ('DONACION_CREAR', 'Crear donaciones',
        'Permite registrar tramites de donacion.', 'ACT'),
    ('DONACION_MODIFICAR', 'Modificar donaciones',
        'Permite modificar tramites de donacion.', 'ACT'),
    ('ADUANERO_TEMPORAL_CONSULTAR', 'Consultar tramites aduaneros temporales',
        'Permite consultar tramites aduaneros temporales.', 'ACT'),
    ('ADUANERO_TEMPORAL_CREAR', 'Crear tramites aduaneros temporales',
        'Permite registrar tramites aduaneros temporales.', 'ACT'),
    ('ADUANERO_TEMPORAL_MODIFICAR', 'Modificar tramites aduaneros temporales',
        'Permite modificar tramites aduaneros temporales.', 'ACT'),
    ('ADUANERO_RECLAMO_GESTIONAR', 'Gestionar reclamos aduaneros',
        'Permite gestionar reclamos relacionados con procesos aduaneros.', 'ACT'),
    ('DESALMACENAJE_CONSULTAR', 'Consultar desalmacenajes especiales',
        'Permite consultar tramites de desalmacenaje especial.', 'ACT'),
    ('DESALMACENAJE_CREAR', 'Crear desalmacenajes especiales',
        'Permite registrar tramites de desalmacenaje especial.', 'ACT'),
    ('DESALMACENAJE_MODIFICAR', 'Modificar desalmacenajes especiales',
        'Permite modificar tramites de desalmacenaje especial.', 'ACT'),
    ('REPORTE_CONSULTAR', 'Consultar reportes',
        'Permite consultar reportes del sistema.', 'ACT'),
    ('REPORTE_FILTRAR', 'Filtrar reportes',
        'Permite aplicar filtros a reportes y consultas.', 'ACT'),
    ('REPORTE_GENERAR', 'Generar reportes',
        'Permite generar reportes del sistema.', 'ACT'),
    ('REPORTE_EXPORTAR', 'Exportar reportes',
        'Permite exportar reportes generados por el sistema.', 'ACT'),
    ('GRAFICO_GENERAR', 'Generar graficos',
        'Permite generar representaciones graficas de la informacion.', 'ACT'),
    ('APROBACION_FONDOS', 'Aprobar fondos',
        'Permite realizar aprobaciones relacionadas con fondos.', 'ACT'),
    ('BITACORA_CONSULTAR', 'Consultar bitacora',
        'Permite consultar la bitacora de operaciones del sistema.', 'ACT')
ON DUPLICATE KEY UPDATE ESTADO = 'ACT';

-- ---------------------------------------------------------------------------
-- El administrador de pruebas recibe todos los permisos activos, igual que en el script 09.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO)
SELECT R.ID_ROL, P.ID_PERMISO, 'ACT'
FROM SIMM_ROL R
JOIN SIMM_PERMISO P ON P.ESTADO = 'ACT'
WHERE R.NOMBRE = 'ADMINISTRADOR'
  AND NOT EXISTS (
      SELECT 1
      FROM SIMT_ROL_PERMISO RP
      WHERE RP.ID_ROL = R.ID_ROL
        AND RP.ID_PERMISO = P.ID_PERMISO
        AND RP.ESTADO = 'ACT'
  );
