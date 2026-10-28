-- Autor: Camila Pastor Castro
-- Fecha: 03/10/2026
-- Descripción: Tablas de los trámites temporales y de sus alertas de vencimiento.
-- Caso de uso asociado: HU-027 Alertas de vencimiento de trámites temporales
-- Motor: MySQL 8+
--
-- Historial de cambios:
--   Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
--   Descripción: el trámite temporal pasa a ser un expediente de SIMT_TRAMITE para la HU-025
--     Registrar importaciones temporales: SIMT_TRAMITE_TEMPORAL comparte la llave primaria con
--     SIMT_TRAMITE, y la referencia, el tipo, la descripción y el usuario responsable se toman del
--     expediente común en lugar de repetirse aquí. Se agregan los datos de la solicitud, del
--     ingreso, de la prórroga y de la salida que registra la Unidad de Importaciones. La fecha
--     de vencimiento pasa a ser opcional porque la fija la aduana después del registro. Se
--     conservan el plazo, la extensión y la regla del contrato de la HU-027.
--     SIMR_TRAMITE_TEMPORAL_AGENTE pasa a llamarse SIMT_TEMPORAL_AGENTE (estándar B02) y la alerta
--     se dirige a un usuario destinatario: el responsable del expediente o un agente asignado.
--     Se catalogan el tipo de trámite IMPORT_TEMPORAL (sigla IT-), sus estados, sus tipos de
--     documento, la acción de bitácora PRORROGA y la moneda GBP.
--   Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
--   Descripción: se agrega MOTIVO_PRORROGA, el motivo por el que se solicita la prórroga.
--   Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
--   Descripción: la tabla atiende también la HU-026 Registrar exportaciones temporales. Se
--     agregan los datos que solo registra la exportación: antecedente, garantía, peso, país de
--     destino, oficio de notificación al usuario, notificación del DUA de salida a la aduana y
--     los datos de la reimportación (solicitud, fecha de reingreso, documento de embarque y
--     desalmacenaje). Se catalogan el tipo EXPORT_TEMPORAL (sigla EXP-T-), sus estados, sus
--     tipos de documento y los países de destino que faltaban. La descripción de SIMT_TRAMITE
--     se amplía a 255 caracteres en las bases ya creadas.
--
-- Requisito previo: ejecutar 02-Tablas-Tramites.sql, 03-Datos-Iniciales.sql y
--   04-Tabla-Sesion-HU004.sql.
--
-- Cómo cargarlo (PowerShell):
--   & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "source database/06-Tablas-Tramites-Temporales.sql"
--
-- El script se puede volver a ejecutar: elimina y vuelve a crear sus tablas, y los catálogos solo
-- se insertan si no existen. Al volver a ejecutarlo se pierden los datos propios de los trámites
-- temporales ya registrados.

-- Declara la codificacion de la conexion: sin esto el cliente mysql de Windows
-- interpreta el archivo con la codificacion de la consola y corrompe las tildes.
SET NAMES utf8mb4;

USE sim;

DROP TABLE IF EXISTS SIMT_ALERTA_TEMPORAL;
DROP TABLE IF EXISTS SIMT_TEMPORAL_AGENTE;
DROP TABLE IF EXISTS SIMR_TRAMITE_TEMPORAL_AGENTE; -- nombre anterior de SIMT_TEMPORAL_AGENTE
DROP TABLE IF EXISTS SIMT_TRAMITE_TEMPORAL;

-- Bases creadas con una versión anterior de 02-Tablas-Tramites.sql: la Unidad describe el equipo
-- con su marca, modelo, serie y placa, y esa descripción supera los 100 caracteres.
ALTER TABLE SIMT_TRAMITE MODIFY DESCRIPCION VARCHAR(255) NULL;

-- ---------------------------------------------------------------------------
-- Datos propios del trámite temporal: la importación temporal (HU-025, sigla IT-) y la
-- exportación temporal (HU-026, sigla EXP-T-) comparten la tabla. El DUA de inicio es el de
-- importación o de exportación temporal, y el DUA de cierre es el de reexportación o el de
-- reingreso. Las columnas marcadas como de exportación quedan en NULL en las importaciones.
-- Las referencias de la aduana (oficios, número de gestión, resolución, cancelación) se guardan
-- como texto porque la Unidad registra a veces un oficio y a veces un correo con su fecha.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMT_TRAMITE_TEMPORAL (
    ID_TRAMITE                  INT             NOT NULL,
    -- Solicitud de la unidad
    UNIDAD_SOLICITANTE          VARCHAR(120)    NOT NULL,
    OFICIO_SOLICITUD            VARCHAR(80)     NOT NULL,
    FECHA_RECEPCION             DATE            NOT NULL,
    ESTADO_MERCANCIA            VARCHAR(15)     NOT NULL,
    NUMERO_FACTURA              VARCHAR(80)     NULL, -- SR = la mercancía no trae factura
    ANTECEDENTE                 VARCHAR(120)    NULL, -- exportación: placa, pedido o donación del equipo
    EN_GARANTIA                 TINYINT(1)      NULL, -- exportación: el equipo está en periodo de garantía
    PESO_KG                     DECIMAL(10,2)   NULL, -- exportación
    ID_PAIS_DESTINO             INT             NULL, -- exportación
    -- Gestión ante la aduana e inicio del régimen
    OFICIO_ADUANA               VARCHAR(120)    NULL,
    NUMERO_GESTION              VARCHAR(80)     NULL,
    RESOLUCION_AUTORIZACION     VARCHAR(120)    NULL,
    OFICIO_NOTIFICACION_USUARIO VARCHAR(120)    NULL, -- exportación: notifica al usuario la autorización
    NUMERO_DUA_INICIO           VARCHAR(80)     NULL,
    FECHA_DUA_INICIO            DATE            NULL,
    NOTIFICACION_DUA_ADUANA     VARCHAR(120)    NULL, -- exportación: correo que notifica el DUA de salida
    FECHA_VENCIMIENTO           DATE            NULL, -- NULL = la aduana aún no fija el plazo
    -- Plazo y extensión de la HU-027
    UNIDAD_PLAZO                VARCHAR(10)     NULL,
    CANTIDAD_PLAZO              SMALLINT        NULL,
    UNIDAD_EXTENSION            VARCHAR(10)     NULL,
    CANTIDAD_EXTENSION          SMALLINT        NULL,
    NUMERO_CONTRATO             VARCHAR(45)     NULL,
    -- Prórroga
    MOTIVO_PRORROGA             VARCHAR(500)    NULL, -- justificación de la unidad para pedir más plazo
    OFICIO_PRORROGA             VARCHAR(120)    NULL,
    NUMERO_GESTION_PRORROGA     VARCHAR(80)     NULL,
    AUTORIZACION_PRORROGA       VARCHAR(120)    NULL,
    -- Cierre del régimen: reexportación (importación) o reimportación (exportación)
    SOLICITUD_INGRESO           VARCHAR(120)    NULL, -- exportación: solicitud de reingreso del usuario
    FECHA_REINGRESO             DATE            NULL, -- exportación: fecha de reingreso al país
    DOCUMENTO_EMBARQUE          VARCHAR(80)     NULL, -- exportación: guía o documento de embarque
    NUMERO_DUA_CIERRE           VARCHAR(80)     NULL,
    FECHA_DUA_CIERRE            DATE            NULL,
    FECHA_DESALMACENAJE         DATE            NULL, -- exportación
    CANCELACION_REGIMEN         VARCHAR(120)    NULL,
    FECHA_CANCELACION           DATE            NULL,
    TIME_STAMP                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMT_TRAMITE_TEMPORAL PRIMARY KEY (ID_TRAMITE),
    CONSTRAINT FK_SIMT_TRAMITE_SIMT_TRAMITE_TEMPORAL
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMM_PAIS_SIMT_TRAMITE_TEMPORAL
        FOREIGN KEY (ID_PAIS_DESTINO) REFERENCES SIMM_PAIS (ID_PAIS),
    CONSTRAINT CK_ESTADO_MERCANCIA_VALOR_ADMITIDO
        CHECK (ESTADO_MERCANCIA IN ('NUEVA', 'USADA', 'REACONDICIONADA')),
    CONSTRAINT CK_PESO_KG_MAYOR_CERO CHECK (PESO_KG IS NULL OR PESO_KG > 0),
    CONSTRAINT CK_SIMT_TEMPORAL_UNIDAD
        CHECK (UNIDAD_PLAZO IS NULL OR UNIDAD_PLAZO IN ('DIAS', 'MESES', 'ANOS')),
    CONSTRAINT CK_SIMT_TEMPORAL_PLAZO
        CHECK ((UNIDAD_PLAZO IS NULL AND CANTIDAD_PLAZO IS NULL)
            OR (UNIDAD_PLAZO IS NOT NULL AND CANTIDAD_PLAZO > 0)),
    CONSTRAINT CK_SIMT_TEMPORAL_EXTENSION
        CHECK ((UNIDAD_EXTENSION IS NULL AND CANTIDAD_EXTENSION IS NULL)
            OR (UNIDAD_EXTENSION IN ('DIAS', 'MESES', 'ANOS') AND CANTIDAD_EXTENSION > 0)),
    CONSTRAINT CK_SIMT_TEMPORAL_CONTRATO
        CHECK (UNIDAD_PLAZO IS NULL OR UNIDAD_PLAZO <> 'ANOS'
            OR (NUMERO_CONTRATO IS NOT NULL AND TRIM(NUMERO_CONTRATO) <> ''))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Datos propios del trámite temporal. Comparte la llave primaria con SIMT_TRAMITE.';

CREATE INDEX IDX_SIMT_TRAMITE_TEMPORAL_FECHA_VENCIM ON SIMT_TRAMITE_TEMPORAL (FECHA_VENCIMIENTO);

-- Agentes de aduanas que, además del responsable del expediente, reciben las alertas.
CREATE TABLE SIMT_TEMPORAL_AGENTE (
    ID_TRAMITE          INT             NOT NULL,
    ID_USUARIO_AGENTE   INT             NOT NULL,
    TIME_STAMP          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMT_TEMPORAL_AGENTE PRIMARY KEY (ID_TRAMITE, ID_USUARIO_AGENTE),
    CONSTRAINT FK_SIMT_TRAMITE_TEMPORAL_SIMT_TEMPORAL_AGENTE
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE_TEMPORAL (ID_TRAMITE),
    CONSTRAINT FK_SIMF_USUARIO_SIMT_TEMPORAL_AGENTE
        FOREIGN KEY (ID_USUARIO_AGENTE) REFERENCES SIMF_USUARIO (ID_USUARIO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Agentes de aduanas asignados a un trámite temporal (HU-027).';

CREATE INDEX IDX_SIMT_TEMPORAL_AGENTE_USUARIO ON SIMT_TEMPORAL_AGENTE (ID_USUARIO_AGENTE);

-- Una alerta por destinatario y por fecha de vencimiento: si una prórroga cambia la fecha, la
-- nueva fecha genera su propia alerta.
CREATE TABLE SIMT_ALERTA_TEMPORAL (
    ID_ALERTA_TEMPORAL      INT             NOT NULL AUTO_INCREMENT,
    ID_TRAMITE              INT             NOT NULL,
    ID_USUARIO_DESTINO      INT             NOT NULL,
    FECHA_VENCIMIENTO       DATE            NOT NULL,
    FECHA_GENERACION        DATE            NOT NULL,
    ESTADO                  VARCHAR(10)     NOT NULL DEFAULT 'PENDIENTE',
    ESTADO_CORREO           VARCHAR(10)     NOT NULL DEFAULT 'PENDIENTE',
    FECHA_ENVIO_CORREO      DATETIME        NULL,
    TIME_STAMP              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMT_ALERTA_TEMPORAL PRIMARY KEY (ID_ALERTA_TEMPORAL),
    CONSTRAINT AK_SIMT_ALERTA_TEMPORAL_DESTINO
        UNIQUE (ID_TRAMITE, ID_USUARIO_DESTINO, FECHA_VENCIMIENTO),
    CONSTRAINT FK_SIMT_TRAMITE_TEMPORAL_SIMT_ALERTA_TEMPORAL
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE_TEMPORAL (ID_TRAMITE),
    CONSTRAINT FK_SIMF_USUARIO_SIMT_ALERTA_TEMPORAL
        FOREIGN KEY (ID_USUARIO_DESTINO) REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT CK_SIMT_ALERTA_ESTADO CHECK (ESTADO IN ('PENDIENTE', 'ATENDIDA')),
    CONSTRAINT CK_SIMT_ALERTA_CORREO CHECK (ESTADO_CORREO IN ('PENDIENTE', 'ENVIADO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Notificaciones en el sistema y cola de correos de vencimiento (HU-027).';

CREATE INDEX IDX_SIMT_ALERTA_USUARIO_ESTADO ON SIMT_ALERTA_TEMPORAL (ID_USUARIO_DESTINO, ESTADO);
CREATE INDEX IDX_SIMT_ALERTA_CORREO_PENDIENTE ON SIMT_ALERTA_TEMPORAL (ESTADO_CORREO, FECHA_GENERACION);

-- ---------------------------------------------------------------------------
-- Catálogos de la importación temporal (HU-025).
-- Se insertan solo si no existen, porque 03-Datos-Iniciales.sql no se vuelve a ejecutar cada
-- vez que se carga este script.
-- ---------------------------------------------------------------------------

-- La Unidad identifica las importaciones temporales con la sigla IT- (por ejemplo IT-001-25).
INSERT INTO SIMM_TIPO_TRAMITE (CODIGO, NOMBRE, PREFIJO_REFERENCIA, REQUIERE_SICOP, REQUIERE_ORDEN_COMPRA)
SELECT 'IMPORT_TEMPORAL', 'Importación temporal', 'IT-', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'IMPORT_TEMPORAL');

-- El expediente nace Por llegar (PEN). Retenido en aduana pausa los pendientes operativos pero
-- no la cuenta regresiva del vencimiento. Vencido sigue abierto porque el expediente aún requiere
-- gestión (reexportar o atender la multa). Finalizado y Cancelado cierran el expediente.
INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT t.ID_TIPO_TRAMITE, e.NOMBRE, e.ES_INICIAL, e.ES_FINAL, e.CATEGORIA_ESTADO, e.ORDEN
FROM SIMM_TIPO_TRAMITE t
JOIN (
    SELECT 'Por llegar' AS NOMBRE, 1 AS ES_INICIAL, 0 AS ES_FINAL, 'PEN' AS CATEGORIA_ESTADO, 1 AS ORDEN
    UNION ALL SELECT 'En gestión aduanera', 0, 0, 'ACT', 2
    UNION ALL SELECT 'Retenido en aduana', 0, 0, 'ACT', 3
    UNION ALL SELECT 'Activo en el país', 0, 0, 'ACT', 4
    UNION ALL SELECT 'Vencido', 0, 0, 'ACT', 5
    UNION ALL SELECT 'En reexportación', 0, 0, 'ACT', 6
    UNION ALL SELECT 'Finalizado', 0, 1, 'FIN', 7
    UNION ALL SELECT 'Cancelado', 0, 1, 'FIN', 8
) e
WHERE t.CODIGO = 'IMPORT_TEMPORAL'
  AND NOT EXISTS (
      SELECT 1 FROM SIMM_ESTADO_TRAMITE x
      WHERE x.ID_TIPO_TRAMITE = t.ID_TIPO_TRAMITE AND x.NOMBRE = e.NOMBRE
  );

-- Documentos que respaldan cada dato del expediente. La factura del exportador reutiliza el tipo
-- FACTURA ya catalogado.
INSERT INTO SIMM_TIPO_DOCUMENTO (CODIGO, NOMBRE)
SELECT d.CODIGO, d.NOMBRE
FROM (
    SELECT 'OFICIO_SOLICITUD' AS CODIGO, 'Oficio del usuario solicitante' AS NOMBRE
    UNION ALL SELECT 'OFICIO_ADUANA', 'Oficio enviado a la aduana'
    UNION ALL SELECT 'RESOL_AUTORIZACION', 'Resolución de autorización de la aduana'
    UNION ALL SELECT 'DUA_IMPORT_TEMPORAL', 'DUA de importación temporal'
    UNION ALL SELECT 'OFICIO_PRORROGA', 'Oficio de solicitud de prórroga'
    UNION ALL SELECT 'AUTORIZ_PRORROGA', 'Autorización de prórroga'
    UNION ALL SELECT 'DUA_REEXPORTACION', 'DUA de reexportación'
    UNION ALL SELECT 'OFICIO_CANCELACION', 'Oficio de cancelación del régimen'
) d
WHERE NOT EXISTS (SELECT 1 FROM SIMM_TIPO_DOCUMENTO x WHERE x.CODIGO = d.CODIGO);

-- La exportación temporal tiene su propio DUA de salida (HU-026): este tipo queda solo para la
-- reexportación de la importación temporal.
UPDATE SIMM_TIPO_DOCUMENTO SET NOMBRE = 'DUA de reexportación' WHERE CODIGO = 'DUA_REEXPORTACION';

INSERT INTO SIMC_ACCION_BITACORA (ACCION, DESCRIPCION)
SELECT 'PRORROGA', 'Prórroga del plazo autorizado'
WHERE NOT EXISTS (SELECT 1 FROM SIMC_ACCION_BITACORA WHERE ACCION = 'PRORROGA');

-- Equipos en préstamo declarados en libras esterlinas.
INSERT INTO SIMM_MONEDA (CODIGO_ISO, NOMBRE, SIMBOLO)
SELECT 'GBP', 'Libra esterlina', '£'
WHERE NOT EXISTS (SELECT 1 FROM SIMM_MONEDA WHERE CODIGO_ISO = 'GBP');

-- ---------------------------------------------------------------------------
-- Catálogos de la exportación temporal (HU-026).
-- ---------------------------------------------------------------------------

-- La Unidad identifica las exportaciones temporales con la sigla EXP-T (por ejemplo EXP-T-001-25).
-- Al reimportar el equipo antepone RI- a la misma referencia: no es otro expediente.
INSERT INTO SIMM_TIPO_TRAMITE (CODIGO, NOMBRE, PREFIJO_REFERENCIA, REQUIERE_SICOP, REQUIERE_ORDEN_COMPRA)
SELECT 'EXPORT_TEMPORAL', 'Exportación temporal', 'EXP-T-', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'EXPORT_TEMPORAL');

-- El expediente nace Por salir (PEN). Fuera del país exige el vencimiento del régimen y Retenido
-- en aduana pausa los pendientes operativos sin detener la cuenta regresiva. Hay tres cierres:
-- Finalizado (el equipo se reimportó), Salida definitiva (el equipo no regresa y se cancela el
-- régimen) y Anulado (la exportación no se realizó).
INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT t.ID_TIPO_TRAMITE, e.NOMBRE, e.ES_INICIAL, e.ES_FINAL, e.CATEGORIA_ESTADO, e.ORDEN
FROM SIMM_TIPO_TRAMITE t
JOIN (
    SELECT 'Por salir' AS NOMBRE, 1 AS ES_INICIAL, 0 AS ES_FINAL, 'PEN' AS CATEGORIA_ESTADO, 1 AS ORDEN
    UNION ALL SELECT 'En gestión aduanera', 0, 0, 'ACT', 2
    UNION ALL SELECT 'Retenido en aduana', 0, 0, 'ACT', 3
    UNION ALL SELECT 'Fuera del país', 0, 0, 'ACT', 4
    UNION ALL SELECT 'Vencido', 0, 0, 'ACT', 5
    UNION ALL SELECT 'En reimportación', 0, 0, 'ACT', 6
    UNION ALL SELECT 'Finalizado', 0, 1, 'FIN', 7
    UNION ALL SELECT 'Salida definitiva', 0, 1, 'FIN', 8
    UNION ALL SELECT 'Anulado', 0, 1, 'FIN', 9
) e
WHERE t.CODIGO = 'EXPORT_TEMPORAL'
  AND NOT EXISTS (
      SELECT 1 FROM SIMM_ESTADO_TRAMITE x
      WHERE x.ID_TIPO_TRAMITE = t.ID_TIPO_TRAMITE AND x.NOMBRE = e.NOMBRE
  );

-- Documentos propios de la exportación. Reutiliza los de la solicitud, la aduana, la prórroga y
-- la cancelación ya catalogados arriba, y el documento de embarque (EMBARQUE) de la HU-031.
INSERT INTO SIMM_TIPO_DOCUMENTO (CODIGO, NOMBRE)
SELECT d.CODIGO, d.NOMBRE
FROM (
    SELECT 'OFICIO_NOTIF_USUARIO' AS CODIGO, 'Oficio de notificación al usuario de la autorización' AS NOMBRE
    UNION ALL SELECT 'DUA_EXPORT_TEMPORAL', 'DUA de exportación temporal'
    UNION ALL SELECT 'SOLICITUD_REINGRESO', 'Solicitud de reingreso del usuario'
    UNION ALL SELECT 'DUA_REIMPORTACION', 'DUA de reingreso'
) d
WHERE NOT EXISTS (SELECT 1 FROM SIMM_TIPO_DOCUMENTO x WHERE x.CODIGO = d.CODIGO);

-- Países de destino de los equipos que el catálogo inicial no incluía.
INSERT INTO SIMM_PAIS (CODIGO_ISO, NOMBRE)
SELECT p.CODIGO_ISO, p.NOMBRE
FROM (
    SELECT 'ARG' AS CODIGO_ISO, 'Argentina' AS NOMBRE
    UNION ALL SELECT 'AUS', 'Australia'
    UNION ALL SELECT 'AUT', 'Austria'
    UNION ALL SELECT 'BEL', 'Bélgica'
    UNION ALL SELECT 'KOR', 'Corea del Sur'
    UNION ALL SELECT 'DNK', 'Dinamarca'
    UNION ALL SELECT 'SLV', 'El Salvador'
    UNION ALL SELECT 'FIN', 'Finlandia'
    UNION ALL SELECT 'GTM', 'Guatemala'
    UNION ALL SELECT 'HND', 'Honduras'
    UNION ALL SELECT 'IND', 'India'
    UNION ALL SELECT 'IRL', 'Irlanda'
    UNION ALL SELECT 'ISR', 'Israel'
    UNION ALL SELECT 'NIC', 'Nicaragua'
    UNION ALL SELECT 'NOR', 'Noruega'
    UNION ALL SELECT 'PER', 'Perú'
    UNION ALL SELECT 'PRT', 'Portugal'
    UNION ALL SELECT 'SWE', 'Suecia'
    UNION ALL SELECT 'CHE', 'Suiza'
) p
WHERE NOT EXISTS (SELECT 1 FROM SIMM_PAIS x WHERE x.CODIGO_ISO = p.CODIGO_ISO);
