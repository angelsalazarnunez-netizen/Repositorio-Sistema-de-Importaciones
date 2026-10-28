-- Autor: Equipo de Desarrollo SIM
-- Fecha: 04/10/2026
-- Descripción: Script incremental de la HU-020 Registrar observaciones o
-- aclaraciones del expediente.
-- Caso de uso asociado: HU-020 Registrar observaciones o aclaraciones del expediente
-- Motor: MySQL 8.0.16+ (requiere restricciones CHECK)

SET NAMES utf8mb4;

USE sim;

CREATE TABLE IF NOT EXISTS SIMT_OBSERVACION_EXPED (
                                                      ID_OBSERVACION          BIGINT        NOT NULL AUTO_INCREMENT,
                                                      ID_TRAMITE              INT           NOT NULL,
                                                      ID_USUARIO_AUTOR        INT           NOT NULL,
                                                      MOTIVO                  VARCHAR(100)  NULL,
    OBSERVACION             VARCHAR(1000) NOT NULL,
    ESTADO_OBSERVACION      CHAR(3)       NOT NULL DEFAULT 'BOR',
    FECHA_CREACION          DATETIME      NOT NULL,
    FECHA_MODIFICACION      DATETIME      NULL,
    FECHA_CONFIRMACION      DATETIME      NULL,
    ESTADO                  CHAR(3)       NOT NULL DEFAULT 'ACT',

    CONSTRAINT PK_SIMT_OBSERVACION_EXPED
    PRIMARY KEY (ID_OBSERVACION),

    CONSTRAINT FK_SIMT_TRAMITE_SIMT_OBSERVACION_EXPED
    FOREIGN KEY (ID_TRAMITE)
    REFERENCES SIMT_TRAMITE (ID_TRAMITE),

    CONSTRAINT FK_SIMF_USUARIO_SIMT_OBSERVACION_EXPED
    FOREIGN KEY (ID_USUARIO_AUTOR)
    REFERENCES SIMF_USUARIO (ID_USUARIO),

    CONSTRAINT CK_ESTADO_OBSERVACION_VALOR
    CHECK (
              ESTADO_OBSERVACION IN ('BOR', 'CON')
    ),

    CONSTRAINT CK_ESTADO_OBSERVACION_REGISTRO
    CHECK (
              ESTADO IN ('ACT', 'INA')
    ),

    CONSTRAINT CK_OBSERVACION_FECHA_CONFIRMACION
    CHECK (
(
              ESTADO_OBSERVACION = 'CON'
              AND FECHA_CONFIRMACION IS NOT NULL
)
    OR
(
    ESTADO_OBSERVACION = 'BOR'
    AND FECHA_CONFIRMACION IS NULL
)
    ),

    CONSTRAINT CK_OBSERVACION_CONFIRMADA_ACTIVA
    CHECK (
              NOT (
              ESTADO_OBSERVACION = 'CON'
              AND ESTADO = 'INA'
                  )
    ),

    INDEX IDX_SIMT_OBSERV_EXPED_TRAMITE
(ID_TRAMITE, ESTADO_OBSERVACION),

    INDEX IDX_SIMT_OBSERV_EXPED_AUTOR
(ID_USUARIO_AUTOR)

    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='Observaciones y aclaraciones de un expediente. Los borradores son editables y las confirmadas forman parte del historial.';

INSERT IGNORE INTO SIMC_ACCION_BITACORA
    (ACCION, DESCRIPCION)
VALUES
    (
        'OBSERVACION_CONFIRMADA',
        'Confirmación de una observación del expediente'
    );