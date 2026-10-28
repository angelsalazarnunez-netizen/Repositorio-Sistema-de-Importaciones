-- Autor: Equipo de Desarrollo SIM
-- Fecha: 05/10/2026
-- Descripción: Script incremental de la HU-023 Registrar resolución / autorización EXONET.
--   Crea el historial de solicitudes y autorizaciones EXONET de un expediente de exoneración,
--   cataloga el tipo de documento de la autorización y las acciones de bitácora de la HU.
-- Caso de uso asociado: HU-023 Registrar resolución / autorización EXONET
-- Motor: MySQL 8.0.16+ (requiere restricciones CHECK y columnas generadas)
-- Estándares aplicados: B02 nombre de tablas, B03 nombre de columnas, B04 llaves primarias,
--                       B05 llaves foráneas, B06 índices, B07 restricciones, B08 tipos de datos.
-- Dependencias: 02-Tablas-Tramites.sql (SIMT_EXONERACION, SIMF_DOCUMENTO, SIMF_USUARIO,
--   SIMM_TIPO_DOCUMENTO, SIMC_ACCION_BITACORA).
--
-- Decisiones de diseño:
--   * SIMT_AUTORIZACION_EXONET es la fuente de verdad de la HU-023. Las columnas NUMERO_EXONET,
--     NUMERO_AUTORIZACION_EXONET, RESOLUCION y FECHA_RESOLUCION de SIMT_EXONERACION (HU-018) no
--     se modifican ni se sincronizan desde aquí (deuda técnica documentada).
--   * Los registros no se borran. Una autorización vigente (VIG) solo puede pasar a anulada (ANU)
--     o sustituida (SUS); ambos estados son terminales y conservan quién, cuándo y por qué.
--   * La sustituta apunta a la autorización que reemplazó mediante ID_AUTORIZACION_ANTERIOR.
--   * ID_TRAMITE_VIGENTE vale ID_TRAMITE solo cuando la fila está VIG y NULL en otro caso; su
--     índice único garantiza como máximo una autorización vigente por expediente (MySQL no
--     admite índices únicos parciales). Por esa misma restricción, al sustituir se debe marcar
--     primero la anterior como SUS y confirmar ese cambio (flush) antes de insertar la nueva VIG.
--   * No se agregan columnas de DUA: una integración futura podrá referenciar
--     ID_AUTORIZACION_EXONET desde su propia tabla sin modificar esta.

SET NAMES utf8mb4;

USE sim;

CREATE TABLE IF NOT EXISTS SIMT_AUTORIZACION_EXONET (
    ID_AUTORIZACION_EXONET      BIGINT          NOT NULL AUTO_INCREMENT,
    ID_TRAMITE                  INT             NOT NULL,
    NUMERO_SOLICITUD_EXONET     VARCHAR(45)     NULL, -- número de solicitud que devuelve EXONET
    FECHA_ENVIO_EXONET          DATE            NULL, -- fecha de envío o presentación en EXONET
    NUMERO_AUTORIZACION         VARCHAR(45)     NULL, -- número de la autorización externa
    FECHA_AUTORIZACION          DATE            NULL, -- fecha de la autorización externa
    FECHA_COMUNICACION          DATE            NULL, -- fecha de comunicación al interesado
    ID_DOCUMENTO                INT             NULL, -- documento de respaldo, opcional
    OBSERVACIONES               VARCHAR(1000)   NULL,
    ESTADO_AUTORIZACION         CHAR(3)         NOT NULL DEFAULT 'VIG', -- VIG, ANU, SUS
    ID_AUTORIZACION_ANTERIOR    BIGINT          NULL, -- autorización que esta fila sustituyó
    ID_USUARIO_REGISTRO         INT NOT NULL,
    ID_USUARIO_CAMBIO_ESTADO    INT NULL,
    FECHA_REGISTRO              DATETIME        NOT NULL,
    FECHA_CAMBIO_ESTADO         DATETIME        NULL,
    MOTIVO_CAMBIO_ESTADO        VARCHAR(500)    NULL,
    -- Columna técnica: solo tiene valor cuando la fila es la vigente del expediente.
    ID_TRAMITE_VIGENTE          INT             GENERATED ALWAYS AS (
                                    CASE WHEN ESTADO_AUTORIZACION = 'VIG' THEN ID_TRAMITE END
                                ) STORED,

    CONSTRAINT PK_SIMT_AUTORIZACION_EXONET
        PRIMARY KEY (ID_AUTORIZACION_EXONET),

    CONSTRAINT AK_SIMT_AUTORIZ_EXONET_VIGENTE
        UNIQUE (ID_TRAMITE_VIGENTE),

    CONSTRAINT AK_SIMT_AUTORIZ_EXONET_ANTERIOR
        UNIQUE (ID_AUTORIZACION_ANTERIOR),

    CONSTRAINT FK_SIMT_EXONERACION_SIMT_AUTORIZACION_EXONET
        FOREIGN KEY (ID_TRAMITE)
        REFERENCES SIMT_EXONERACION (ID_TRAMITE),

    CONSTRAINT FK_SIMF_DOCUMENTO_SIMT_AUTORIZACION_EXONET
        FOREIGN KEY (ID_DOCUMENTO)
        REFERENCES SIMF_DOCUMENTO (ID_DOCUMENTO),

    CONSTRAINT FK_SIMT_AUTORIZ_EXONET_ANTERIOR
        FOREIGN KEY (ID_AUTORIZACION_ANTERIOR)
        REFERENCES SIMT_AUTORIZACION_EXONET (ID_AUTORIZACION_EXONET),

    CONSTRAINT FK_SIMF_USUARIO_SIMT_AUTORIZ_EXONET_REGISTRO
        FOREIGN KEY (ID_USUARIO_REGISTRO)
        REFERENCES SIMF_USUARIO (ID_USUARIO),

    CONSTRAINT FK_SIMF_USUARIO_SIMT_AUTORIZ_EXONET_CAMBIO
        FOREIGN KEY (ID_USUARIO_CAMBIO_ESTADO)
        REFERENCES SIMF_USUARIO (ID_USUARIO),

    CONSTRAINT CK_AUTORIZ_EXONET_ESTADO_VALOR
        CHECK (ESTADO_AUTORIZACION IN ('VIG', 'ANU', 'SUS')),

    -- Debe existir al menos uno de los dos identificadores EXONET.
    CONSTRAINT CK_AUTORIZ_EXONET_IDENTIFICADOR
        CHECK (NUMERO_SOLICITUD_EXONET IS NOT NULL OR NUMERO_AUTORIZACION IS NOT NULL),

    -- La ausencia de dato se guarda como NULL, nunca como texto vacío.
    CONSTRAINT CK_AUTORIZ_EXONET_SOLICITUD_NO_VACIA
        CHECK (TRIM(NUMERO_SOLICITUD_EXONET) <> ''),

    CONSTRAINT CK_AUTORIZ_EXONET_AUTORIZ_NO_VACIA
        CHECK (TRIM(NUMERO_AUTORIZACION) <> ''),

    CONSTRAINT CK_AUTORIZ_EXONET_OBSERV_NO_VACIA
        CHECK (TRIM(OBSERVACIONES) <> ''),

    -- Orden de los hitos: envío <= autorización <= comunicación, cuando existen.
    CONSTRAINT CK_AUTORIZ_EXONET_ORDEN_FECHAS
        CHECK (
            (FECHA_ENVIO_EXONET IS NULL OR FECHA_AUTORIZACION IS NULL
                OR FECHA_ENVIO_EXONET <= FECHA_AUTORIZACION)
            AND (FECHA_AUTORIZACION IS NULL OR FECHA_COMUNICACION IS NULL
                OR FECHA_AUTORIZACION <= FECHA_COMUNICACION)
            AND (FECHA_ENVIO_EXONET IS NULL OR FECHA_COMUNICACION IS NULL
                OR FECHA_ENVIO_EXONET <= FECHA_COMUNICACION)
        ),

    -- Una vigente no tiene datos de cambio de estado; una anulada o sustituida los tiene todos.
    CONSTRAINT CK_AUTORIZ_EXONET_CAMBIO_ESTADO
        CHECK (
            (
                ESTADO_AUTORIZACION = 'VIG'
                AND ID_USUARIO_CAMBIO_ESTADO IS NULL
                AND FECHA_CAMBIO_ESTADO IS NULL
                AND MOTIVO_CAMBIO_ESTADO IS NULL
            )
            OR
            (
                ESTADO_AUTORIZACION IN ('ANU', 'SUS')
                AND ID_USUARIO_CAMBIO_ESTADO IS NOT NULL
                AND FECHA_CAMBIO_ESTADO IS NOT NULL
                AND MOTIVO_CAMBIO_ESTADO IS NOT NULL
                AND TRIM(MOTIVO_CAMBIO_ESTADO) <> ''
            )
        ),

    CONSTRAINT CK_AUTORIZ_EXONET_FECHA_CAMBIO
        CHECK (FECHA_CAMBIO_ESTADO IS NULL OR FECHA_CAMBIO_ESTADO >= FECHA_REGISTRO),

    INDEX IDX_SIMT_AUTORIZ_EXONET_TRAMITE
        (ID_TRAMITE, ESTADO_AUTORIZACION),

    INDEX IDX_SIMT_AUTORIZ_EXONET_DOCUMENTO
        (ID_DOCUMENTO),

    INDEX IDX_SIMT_AUTORIZ_EXONET_USR_REGISTRO
        (ID_USUARIO_REGISTRO),

    INDEX IDX_SIMT_AUTORIZ_EXONET_USR_CAMBIO
        (ID_USUARIO_CAMBIO_ESTADO)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Historial de solicitudes y autorizaciones EXONET de un expediente de exoneración. VIG = vigente, ANU = anulada, SUS = sustituida. No se borran registros.';

-- ---------------------------------------------------------------------------
-- Tipo de documento de la autorización. No se reutiliza EXONET porque ese código
-- representa el formulario de la solicitud (HU-018).
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_TIPO_DOCUMENTO (CODIGO, NOMBRE)
SELECT 'AUTORIZACION_EXONET', 'Autorización EXONET'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM SIMM_TIPO_DOCUMENTO x WHERE x.CODIGO = 'AUTORIZACION_EXONET'
);

-- ---------------------------------------------------------------------------
-- Acciones de bitácora de la HU-023.
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO SIMC_ACCION_BITACORA
    (ACCION, DESCRIPCION)
VALUES
    ('AUTORIZACION_REGISTRADA', 'Registro de una solicitud o autorización EXONET'),
    ('AUTORIZACION_COMPLETADA', 'Datos pendientes completados en la autorización EXONET vigente'),
    ('AUTORIZACION_ANULADA', 'Anulación de una autorización EXONET'),
    ('AUTORIZACION_SUSTITUIDA', 'Sustitución de una autorización EXONET por una nueva');
