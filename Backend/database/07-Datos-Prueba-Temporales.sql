-- Autor: Josue Guevara Lostalo
-- Fecha: 04/10/2026
-- Descripción: Datos de prueba de las importaciones temporales para el ambiente local.
--   NO debe ejecutarse en producción. Los expedientes son ficticios: siguen la forma de los
--   registros de la Unidad de Importaciones, pero no copian datos reales.
-- Caso de uso asociado: HU-025 Registrar importaciones temporales y HU-027 Alertas de vencimiento
-- Motor: MySQL 8+
--
-- Historial de cambios:
--   Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
--   Descripción: se agregan exportaciones temporales de prueba (HU-026 Registrar exportaciones
--     temporales), con valores en colones como los registra la Unidad.
--
-- Las fechas se calculan a partir del día en que se carga el script, para que la cuenta regresiva
-- muestre, en cada tipo, un expediente sin plazo, uno próximo a vencer (genera alerta), uno en
-- plazo y uno cerrado. En la exportación el vencimiento es el plazo estándar: la fecha del DUA de
-- exportación más 6 meses.
--
-- Requisito previo: ejecutar 04-Datos-Prueba.sql (usuario 1) y 06-Tablas-Tramites-Temporales.sql.
--
-- Cómo cargarlo (PowerShell):
--   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "source database/07-Datos-Prueba-Temporales.sql"

-- Declara la codificacion de la conexion: sin esto el cliente mysql de Windows
-- interpreta el archivo con la codificacion de la consola y corrompe las tildes.
SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- Limpieza de una carga anterior, para que el script se pueda volver a ejecutar.
-- ---------------------------------------------------------------------------

DELETE a FROM SIMT_ALERTA_TEMPORAL a
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = a.ID_TRAMITE
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

DELETE g FROM SIMT_TEMPORAL_AGENTE g
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = g.ID_TRAMITE
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

DELETE x FROM SIMT_TRAMITE_TEMPORAL x
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = x.ID_TRAMITE
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

DELETE h FROM SIMH_ESTADO_TRAMITE h
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = h.ID_TRAMITE
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

DELETE b FROM SIML_BITACORA b
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = b.ID_TRAMITE
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

UPDATE SIMF_DOCUMENTO d
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = d.ID_TRAMITE
SET d.ID_DOCUMENTO_ANTERIOR = NULL
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

DELETE d FROM SIMF_DOCUMENTO d
JOIN SIMT_TRAMITE t ON t.ID_TRAMITE = d.ID_TRAMITE
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

DELETE FROM SIMT_TRAMITE
WHERE REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');

-- ---------------------------------------------------------------------------
-- Expedientes comunes.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    ID_MONEDA, VALOR_ESTIMADO, DESCRIPCION, REGIMEN_ADUANERO, ADUANA_CONTROL, FECHA_CREACION, FECHA_CIERRE)
SELECT d.REFERENCIA, tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, m.ID_MONEDA, d.VALOR, d.DESCRIPCION,
    'Importación temporal', 'Aduana Santamaría', d.FECHA_CREACION, d.FECHA_CIERRE
FROM (
    SELECT 'IT-901-26' AS REFERENCIA, 'Por llegar' AS ESTADO, 'USD' AS MONEDA, 2500.00 AS VALOR,
           'Espectrómetro portátil en préstamo' AS DESCRIPCION,
           NOW() - INTERVAL 5 DAY AS FECHA_CREACION, NULL AS FECHA_CIERRE
    UNION ALL SELECT 'IT-902-26', 'Activo en el país', 'GBP', 85000.00,
           'Cámara térmica para exhibición', NOW() - INTERVAL 150 DAY, NULL
    UNION ALL SELECT 'IT-903-26', 'Retenido en aduana', 'EUR', 12000.00,
           'Sensor de flujo para investigación compartida', NOW() - INTERVAL 40 DAY, NULL
    UNION ALL SELECT 'IT-904-26', 'Finalizado', 'USD', 1800.00,
           'Fluorómetro de calibración', NOW() - INTERVAL 300 DAY, CURDATE() - INTERVAL 20 DAY
) d
JOIN SIMM_TIPO_TRAMITE tt ON tt.CODIGO = 'IMPORT_TEMPORAL'
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.NOMBRE = d.ESTADO
JOIN SIMM_MONEDA m ON m.CODIGO_ISO = d.MONEDA;

-- ---------------------------------------------------------------------------
-- Datos propios de cada importación temporal.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_TRAMITE_TEMPORAL (ID_TRAMITE, UNIDAD_SOLICITANTE, OFICIO_SOLICITUD, FECHA_RECEPCION,
    ESTADO_MERCANCIA, NUMERO_FACTURA, OFICIO_ADUANA, NUMERO_GESTION, RESOLUCION_AUTORIZACION,
    NUMERO_DUA_INICIO, FECHA_DUA_INICIO, FECHA_VENCIMIENTO, NUMERO_DUA_CIERRE, FECHA_DUA_CIERRE,
    CANCELACION_REGIMEN, FECHA_CANCELACION)
SELECT t.ID_TRAMITE, d.UNIDAD, d.OFICIO, d.RECEPCION, d.MERCANCIA, d.FACTURA, d.OFICIO_ADUANA,
    d.GESTION, d.RESOLUCION, d.DUA_INICIO, d.FECHA_DUA_INICIO, d.VENCIMIENTO, d.DUA_CIERRE,
    d.FECHA_DUA_CIERRE, d.CANCELACION, d.FECHA_CANCELACION
FROM (
    SELECT 'IT-901-26' AS REFERENCIA, 'Escuela de Física' AS UNIDAD, 'EF-15-2026' AS OFICIO,
           CURDATE() - INTERVAL 5 DAY AS RECEPCION, 'USADA' AS MERCANCIA, 'SR' AS FACTURA,
           NULL AS OFICIO_ADUANA, NULL AS GESTION, NULL AS RESOLUCION, NULL AS DUA_INICIO,
           NULL AS FECHA_DUA_INICIO, NULL AS VENCIMIENTO, NULL AS DUA_CIERRE,
           NULL AS FECHA_DUA_CIERRE, NULL AS CANCELACION, NULL AS FECHA_CANCELACION
    UNION ALL SELECT 'IT-902-26', 'Escuela de Artes Plásticas', 'EAP-40-2026',
           CURDATE() - INTERVAL 150 DAY, 'NUEVA', 'F-000123', 'OS-900-2026', '9001-2026', '9001-2026',
           '005-2026-900001', CURDATE() - INTERVAL 140 DAY, CURDATE() + INTERVAL 30 DAY,
           NULL, NULL, NULL, NULL
    UNION ALL SELECT 'IT-903-26', 'Centro de Investigación de Prueba', 'CIP-08-2026',
           CURDATE() - INTERVAL 40 DAY, 'USADA', 'SR', 'Correo enviado a la aduana', '9002-2026', NULL,
           NULL, NULL, CURDATE() + INTERVAL 90 DAY, NULL, NULL, NULL, NULL
    UNION ALL SELECT 'IT-904-26', 'Laboratorio de Pruebas', 'LP-77-2025',
           CURDATE() - INTERVAL 300 DAY, 'USADA', '7325', 'OS-901-2025', '9003-2025', '9003-2025',
           '005-2025-900002', CURDATE() - INTERVAL 290 DAY, CURDATE() - INTERVAL 110 DAY,
           '005-2026-900003', CURDATE() - INTERVAL 30 DAY, 'Correo de cancelación del régimen',
           CURDATE() - INTERVAL 20 DAY
) d
JOIN SIMT_TRAMITE t ON t.REFERENCIA_INTERNA = d.REFERENCIA;

-- ---------------------------------------------------------------------------
-- Exportaciones temporales (HU-026).
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_TRAMITE (REFERENCIA_INTERNA, ID_TIPO_TRAMITE, ID_ESTADO_TRAMITE, ID_USUARIO_RESPONSABLE,
    ID_MONEDA, VALOR_ESTIMADO, DESCRIPCION, REGIMEN_ADUANERO, ADUANA_CONTROL, FECHA_CREACION, FECHA_CIERRE)
SELECT d.REFERENCIA, tt.ID_TIPO_TRAMITE, e.ID_ESTADO_TRAMITE, 1, m.ID_MONEDA, d.VALOR, d.DESCRIPCION,
    'Exportación temporal', 'Santamaría', d.FECHA_CREACION, d.FECHA_CIERRE
FROM (
    SELECT 'EXP-T-901-26' AS REFERENCIA, 'Por salir' AS ESTADO, 3624740.18 AS VALOR,
           'Fluorómetro de laboratorio para calibración' AS DESCRIPCION,
           NOW() - INTERVAL 3 DAY AS FECHA_CREACION, NULL AS FECHA_CIERRE
    UNION ALL SELECT 'EXP-T-902-26', 'Fuera del país', 22930956.30,
           'Cuadrupolo para espectroscopía de masas, marca de prueba, modelo QX-900, serie QX90000123, placa UCR 900001',
           NOW() - INTERVAL 160 DAY, NULL
    UNION ALL SELECT 'EXP-T-903-26', 'Retenido en aduana', 5482080.00,
           'Gravímetro de campo para reparación', NOW() - INTERVAL 45 DAY, NULL
    UNION ALL SELECT 'EXP-T-904-26', 'Finalizado', 4307884.85,
           'Cámara de gases para mantenimiento en garantía', NOW() - INTERVAL 250 DAY, CURDATE() - INTERVAL 15 DAY
) d
JOIN SIMM_TIPO_TRAMITE tt ON tt.CODIGO = 'EXPORT_TEMPORAL'
JOIN SIMM_ESTADO_TRAMITE e ON e.ID_TIPO_TRAMITE = tt.ID_TIPO_TRAMITE AND e.NOMBRE = d.ESTADO
JOIN SIMM_MONEDA m ON m.CODIGO_ISO = 'CRC';

INSERT INTO SIMT_TRAMITE_TEMPORAL (ID_TRAMITE, UNIDAD_SOLICITANTE, OFICIO_SOLICITUD, FECHA_RECEPCION,
    ESTADO_MERCANCIA, NUMERO_FACTURA, ANTECEDENTE, EN_GARANTIA, PESO_KG, ID_PAIS_DESTINO, OFICIO_ADUANA,
    NUMERO_GESTION, RESOLUCION_AUTORIZACION, OFICIO_NOTIFICACION_USUARIO, NUMERO_DUA_INICIO, FECHA_DUA_INICIO,
    NOTIFICACION_DUA_ADUANA, FECHA_VENCIMIENTO, UNIDAD_PLAZO, CANTIDAD_PLAZO, SOLICITUD_INGRESO,
    FECHA_REINGRESO, DOCUMENTO_EMBARQUE, NUMERO_DUA_CIERRE, FECHA_DUA_CIERRE, FECHA_DESALMACENAJE,
    CANCELACION_REGIMEN, FECHA_CANCELACION)
SELECT t.ID_TRAMITE, d.UNIDAD, d.OFICIO, d.RECEPCION, d.MERCANCIA, d.FACTURA, d.ANTECEDENTE, d.GARANTIA,
    d.PESO, p.ID_PAIS, d.OFICIO_ADUANA, d.GESTION, d.RESOLUCION, d.NOTIFICACION_USUARIO, d.DUA_INICIO,
    d.FECHA_DUA_INICIO, d.NOTIFICACION_DUA, d.FECHA_DUA_INICIO + INTERVAL 6 MONTH,
    CASE WHEN d.FECHA_DUA_INICIO IS NULL THEN NULL ELSE 'MESES' END,
    CASE WHEN d.FECHA_DUA_INICIO IS NULL THEN NULL ELSE 6 END,
    d.SOLICITUD_INGRESO, d.FECHA_REINGRESO, d.EMBARQUE, d.DUA_CIERRE, d.FECHA_DUA_CIERRE,
    d.FECHA_DESALMACENAJE, d.CANCELACION, d.FECHA_CANCELACION
FROM (
    SELECT 'EXP-T-901-26' AS REFERENCIA, 'Centro de Investigación de Prueba' AS UNIDAD,
           'CIP-249-2026' AS OFICIO, CURDATE() - INTERVAL 3 DAY AS RECEPCION, 'NUEVA' AS MERCANCIA,
           NULL AS FACTURA, 'P-901-2026' AS ANTECEDENTE, 1 AS GARANTIA, 7.00 AS PESO,
           'Estados Unidos' AS PAIS, NULL AS OFICIO_ADUANA, NULL AS GESTION, NULL AS RESOLUCION,
           NULL AS NOTIFICACION_USUARIO, NULL AS DUA_INICIO, NULL AS FECHA_DUA_INICIO,
           NULL AS NOTIFICACION_DUA, NULL AS SOLICITUD_INGRESO, NULL AS FECHA_REINGRESO, NULL AS EMBARQUE,
           NULL AS DUA_CIERRE, NULL AS FECHA_DUA_CIERRE, NULL AS FECHA_DESALMACENAJE, NULL AS CANCELACION,
           NULL AS FECHA_CANCELACION
    UNION ALL SELECT 'EXP-T-902-26', 'Laboratorio de Pruebas', 'LP-246-2026', CURDATE() - INTERVAL 160 DAY,
           'USADA', '904-2026', 'Placa UCR 900001', 0, 52.00, 'Alemania', 'OS-948-2026', '9408-2026',
           'MH-DGA-AS-DT-STO-OF-9625-2026', 'OS-916-2026', '005-2026-900575',
           CURDATE() + INTERVAL 30 DAY - INTERVAL 6 MONTH, 'correo del día de salida',
           NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
    UNION ALL SELECT 'EXP-T-903-26', 'Escuela de Prueba', 'EP-414-2026', CURDATE() - INTERVAL 45 DAY,
           'USADA', '903-2026', NULL, 0, 17.50, 'Dinamarca', 'OS-921-2026', '9822-2026',
           'MH-DGA-AS-DT-STO-OF-9680-2026', NULL, '005-2026-900286', CURDATE() - INTERVAL 30 DAY,
           NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
    UNION ALL SELECT 'EXP-T-904-26', 'Escuela de Prueba de Medicina', 'EPM-41-2025',
           CURDATE() - INTERVAL 250 DAY, 'USADA', 'garantía de funcionamiento', 'P-905-2025', 1, 5.00,
           'Estados Unidos', 'OS-965-2025', '9472-2025', 'MH-DGA-AS-DT-STO-OF-9075-2025', 'OS-915-2025',
           '005-2025-949368', CURDATE() - INTERVAL 240 DAY, 'correo del día de salida',
           'correo del usuario', CURDATE() - INTERVAL 40 DAY, '1Z0000000000000000',
           '005-2026-922578', CURDATE() - INTERVAL 35 DAY, CURDATE() - INTERVAL 30 DAY,
           'correo de cancelación del régimen', CURDATE() - INTERVAL 20 DAY
) d
JOIN SIMT_TRAMITE t ON t.REFERENCIA_INTERNA = d.REFERENCIA
JOIN SIMM_PAIS p ON p.NOMBRE = d.PAIS;

-- ---------------------------------------------------------------------------
-- Historial: cada expediente registra el movimiento que lo dejó en su estado actual.
-- ---------------------------------------------------------------------------

INSERT INTO SIMH_ESTADO_TRAMITE (ID_TRAMITE, ID_ESTADO_ANTERIOR, ID_ESTADO_NUEVO, ID_USUARIO,
    JUSTIFICACION, FECHA_HORA)
SELECT t.ID_TRAMITE, NULL, t.ID_ESTADO_TRAMITE, 1, 'Registro inicial del expediente (datos de prueba).',
    t.FECHA_CREACION
FROM SIMT_TRAMITE t
WHERE t.REFERENCIA_INTERNA IN ('IT-901-26', 'IT-902-26', 'IT-903-26', 'IT-904-26',
    'EXP-T-901-26', 'EXP-T-902-26', 'EXP-T-903-26', 'EXP-T-904-26');
