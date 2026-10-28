-- Autor: Katherine Susana Valverde Fallas
-- Fecha: 10/09/2026
-- Descripción: Datos de prueba para el ambiente local. NO debe ejecutarse en producción.
-- Caso de uso asociado: HU-031 Registrar trámite de desalmacenamiento especial
-- Motor: MySQL 8+
--
-- Mientras el inicio de sesión no exista, el formulario de la HU-031 envía un usuario
-- responsable fijo con identificador 1. Este script crea esa cuenta para poder probar el
-- registro de expedientes. La contraseña no es utilizable: la cuenta no permite autenticarse.
--
-- Historial de cambios:
--   Responsable: Josue Guevara Lostalo. Fecha: 16/09/2026.
--   Descripción: se agregan trámites de distintos tipos, estados, monedas, regímenes aduaneros y
--     aduanas de control para probar la HU-034 Buscar y filtrar datos.
--
-- Cómo cargarlo (PowerShell o MySQL Workbench):
--   mysql -u root -p < database/04-Datos-Prueba.sql

-- Declara la codificacion de la conexion: sin esto el cliente mysql de Windows
-- interpreta el archivo con la codificacion de la consola y corrompe las tildes.
SET NAMES utf8mb4;

USE sim;

INSERT INTO SIMF_USUARIO (
    ID_USUARIO,
    NOMBRE_COMPLETO,
    NUMERO_CEDULA,
    FECHA_NACIMIENTO,
    CORREO_INSTITUCIONAL,
    CONTRASENA_HASH,
    ESTADO_VALIDACION,
    ESTADO_ROL
) VALUES (
    1,
    'Analista de Importaciones',
    '100000001',
    '1990-01-01',
    'analista.importaciones@ucr.ac.cr',
    'CUENTA_DE_PRUEBA_SIN_CONTRASENA',
    'VAL',
    'ASI'
);

-- ---------------------------------------------------------------------------
-- HU-034 Buscar y filtrar datos.
-- Trámites con distintos tipos, categorías de estado, monedas, regímenes aduaneros y aduanas
-- de control, para probar la combinación de filtros. Cada trámite incluye sus datos propios,
-- de modo que también aparezca en las pantallas de seguimiento de su tipo.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    ID_PAIS_ORIGEN, ID_MONEDA, VALOR_ESTIMADO, DESCRIPCION, REGIMEN_ADUANERO, ADUANA_CONTROL, FECHA_CREACION)
SELECT 'DE-2026-9001', tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, p.ID_PAIS, m.ID_MONEDA, 1850.00,
    'Equipo de laboratorio sin documentación de origen', 'Importación definitiva', 'Aduana Santamaría',
    '2026-07-08 09:15:00'
FROM SIMM_TIPO_TRAMITE tt
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.NOMBRE = 'Pendiente'
JOIN SIMM_MONEDA m ON m.CODIGO_ISO = 'USD'
JOIN SIMM_PAIS p ON p.CODIGO_ISO = 'USA'
WHERE tt.CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    ID_PAIS_ORIGEN, ID_MONEDA, VALOR_ESTIMADO, DESCRIPCION, REGIMEN_ADUANERO, ADUANA_CONTROL, FECHA_CREACION)
SELECT 'DE-2026-9002', tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, NULL, m.ID_MONEDA, 425000.00,
    'Mobiliario de oficina retenido en bodega', 'Importación definitiva', 'Aduana Central',
    '2026-08-14 14:30:00'
FROM SIMM_TIPO_TRAMITE tt
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.NOMBRE = 'Documentación en revisión'
JOIN SIMM_MONEDA m ON m.CODIGO_ISO = 'CRC'
WHERE tt.CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    ID_PAIS_ORIGEN, ID_MONEDA, VALOR_ESTIMADO, DESCRIPCION, REGIMEN_ADUANERO, ADUANA_CONTROL, FECHA_CREACION,
    FECHA_CIERRE)
SELECT 'DE-2026-9003', tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, p.ID_PAIS, m.ID_MONEDA, 3200.00,
    'Repuestos de microscopio electrónico', 'Importación temporal', 'Aduana Santamaría',
    '2026-09-02 08:00:00', '2026-09-10'
FROM SIMM_TIPO_TRAMITE tt
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.NOMBRE = 'Finalizada'
JOIN SIMM_MONEDA m ON m.CODIGO_ISO = 'EUR'
JOIN SIMM_PAIS p ON p.CODIGO_ISO = 'DEU'
WHERE tt.CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMT_DESALMACENAJE_ESPEC (ID_TRAMITE, ORIGEN_DEPENDENCIA, JUSTIFICACION)
SELECT ID_TRAMITE, 'Escuela de Química', 'Mercancía institucional recibida sin documentación de origen.'
FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = 'DE-2026-9001';

INSERT INTO SIMT_DESALMACENAJE_ESPEC (ID_TRAMITE, ORIGEN_DEPENDENCIA, JUSTIFICACION)
SELECT ID_TRAMITE, 'Oficina de Suministros', 'Mobiliario retenido que pertenece a la institución.'
FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = 'DE-2026-9002';

INSERT INTO SIMT_DESALMACENAJE_ESPEC (ID_TRAMITE, ORIGEN_DEPENDENCIA, JUSTIFICACION)
SELECT ID_TRAMITE, 'Centro de Investigación en Estructuras Microscópicas',
    'Repuestos enviados por el fabricante para reparar equipo institucional.'
FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = 'DE-2026-9003';

-- La solicitud de exoneración no declara moneda ni valor estimado (HU-018).
INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    REGIMEN_ADUANERO, ADUANA_CONTROL, FECHA_CREACION)
SELECT 'EXO-2026-9001', tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, 'Importación definitiva', 'Aduana Santamaría',
    '2026-08-20 10:45:00'
FROM SIMM_TIPO_TRAMITE tt
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.ES_INICIAL = 1
WHERE tt.CODIGO = 'EXONERACION';

INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    FECHA_CREACION)
SELECT 'EXO-2026-9002', tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, '2026-09-05 16:20:00'
FROM SIMM_TIPO_TRAMITE tt
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.ES_INICIAL = 1
WHERE tt.CODIGO = 'EXONERACION';

INSERT INTO SIMT_EXONERACION (ID_TRAMITE, TIPO_EXONERACION, NUMERO_CONTRATACION, ORDEN_COMPRA_GECO, CONTRATO_SICOP)
SELECT ID_TRAMITE, 'COMPRA_INSTITUCIONAL', '2026LD-000045-0000900001', 'GECO-2026-1180', 'SICOP-2026-0457'
FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = 'EXO-2026-9001';

INSERT INTO SIMT_EXONERACION (ID_TRAMITE, TIPO_EXONERACION, NUMERO_CONTRATACION, ORDEN_COMPRA_GECO, CONTRATO_SICOP)
SELECT ID_TRAMITE, 'DONACION', '2026CD-000112-0000900001', 'GECO-2026-1215', 'SICOP-2026-0502'
FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = 'EXO-2026-9002';
