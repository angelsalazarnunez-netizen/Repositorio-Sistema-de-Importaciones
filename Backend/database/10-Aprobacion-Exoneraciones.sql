-- Autor: Equipo de Desarrollo SIM
-- Fecha: 05/10/2026
-- Descripción: Script incremental de la HU-022 Aprobar solicitud de exoneración. Cataloga los
--   estados que resultan de la decisión de la jefatura, el permiso que autoriza a decidir, el
--   rol de jefatura que lo tiene, y crea las tablas que conservan cada decisión y la
--   notificación enviada a la técnica responsable.
-- Caso de uso asociado: HU-022 Aprobar solicitud de exoneración
-- Motor: MySQL 8.0.16+ (requiere restricciones CHECK)
--
-- Requisito previo: ejecutar 02-Tablas-Tramites.sql, 03-Datos-Iniciales.sql,
--   04-Tabla-Sesion-HU004.sql y 08-Control-Documental-Exoneraciones.sql.
-- El script puede ejecutarse más de una vez: no duplica registros.

SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- Estados que resultan de la decisión.
-- Exoneración aprobada: el trámite continúa con el registro EXONET / Hacienda (HU-023).
-- Exoneración rechazada: cierra el expediente (ES_FINAL = 1).
-- Pendiente de modificación: el expediente vuelve a la técnica, que lo corrige y lo reenvía
--   con "Continuar a aprobación" (HU-021).
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO SIMM_ESTADO_TRAMITE
    (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente de modificación', 0, 0, 'ACT', 3
FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'EXONERACION';

INSERT IGNORE INTO SIMM_ESTADO_TRAMITE
    (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Exoneración aprobada', 0, 0, 'ACT', 4
FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'EXONERACION';

INSERT IGNORE INTO SIMM_ESTADO_TRAMITE
    (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Exoneración rechazada', 0, 1, 'FIN', 9
FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'EXONERACION';

-- ---------------------------------------------------------------------------
-- Decisiones de la jefatura sobre la solicitud de exoneración.
-- El historial de decisiones nunca se modifica ni se elimina: cada decisión es un registro
-- nuevo con su usuario, su fecha y su hora, y el estado del expediente antes y después.
-- DECISION: APR = aprobar, REC = rechazar, MOD = solicitar modificación.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SIMT_DECISION_EXONERACION (
    ID_DECISION             INT             NOT NULL AUTO_INCREMENT,
    ID_TRAMITE              INT             NOT NULL,
    ID_USUARIO_DECISOR      INT             NOT NULL,
    DECISION                CHAR(3)         NOT NULL,
    JUSTIFICACION           VARCHAR(1000)   NULL,
    ID_ESTADO_ANTERIOR      INT             NOT NULL,
    ID_ESTADO_NUEVO         INT             NOT NULL,
    FECHA_HORA              DATETIME        NOT NULL,
    ESTADO                  CHAR(3)         NOT NULL DEFAULT 'ACT',
    CONSTRAINT PK_SIMT_DECISION_EXONERACION
        PRIMARY KEY (ID_DECISION),
    CONSTRAINT FK_SIMT_TRAMITE_SIMT_DECISION_EXONERACION
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMF_USUARIO_SIMT_DECISION_EXONERACION
        FOREIGN KEY (ID_USUARIO_DECISOR) REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT FK_SIMM_ESTADO_ANTERIOR_SIMT_DECISION_EXONERACION
        FOREIGN KEY (ID_ESTADO_ANTERIOR) REFERENCES SIMM_ESTADO_TRAMITE (ID_ESTADO_TRAMITE),
    CONSTRAINT FK_SIMM_ESTADO_NUEVO_SIMT_DECISION_EXONERACION
        FOREIGN KEY (ID_ESTADO_NUEVO) REFERENCES SIMM_ESTADO_TRAMITE (ID_ESTADO_TRAMITE),
    CONSTRAINT CK_DECISION_EXONERACION_VALOR
        CHECK (DECISION IN ('APR', 'REC', 'MOD')),
    CONSTRAINT CK_DECISION_EXONERACION_JUSTIFICACION
        CHECK (DECISION = 'APR' OR JUSTIFICACION IS NOT NULL),
    CONSTRAINT CK_ESTADO_DECISION_EXONERACION
        CHECK (ESTADO IN ('ACT', 'INA')),
    INDEX IDX_SIMT_DECISION_EXON_TRAMITE (ID_TRAMITE, FECHA_HORA),
    INDEX IDX_SIMT_DECISION_EXON_DECISOR (ID_USUARIO_DECISOR)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Decisiones de aprobación, rechazo o modificación de las solicitudes de exoneración (HU-022).';

-- ---------------------------------------------------------------------------
-- Notificación de la decisión a la técnica responsable.
-- Se registra junto con la decisión; ESTADO_ENVIO indica si el correo salió:
-- PEN = pendiente, ENV = enviado, ERR = no se pudo enviar.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SIMT_NOTIFICACION_EXONERACION (
    ID_NOTIFICACION         INT             NOT NULL AUTO_INCREMENT,
    ID_DECISION             INT             NOT NULL,
    ID_USUARIO_DESTINO      INT             NOT NULL,
    CORREO_DESTINO          VARCHAR(120)    NULL,
    ASUNTO                  VARCHAR(150)    NOT NULL,
    MENSAJE                 VARCHAR(2000)   NOT NULL,
    ESTADO_ENVIO            CHAR(3)         NOT NULL DEFAULT 'PEN',
    FECHA_CREACION          DATETIME        NOT NULL,
    FECHA_ENVIO             DATETIME        NULL,
    ESTADO                  CHAR(3)         NOT NULL DEFAULT 'ACT',
    CONSTRAINT PK_SIMT_NOTIFICACION_EXONERACION
        PRIMARY KEY (ID_NOTIFICACION),
    CONSTRAINT FK_SIMT_DECISION_EXONERACION_SIMT_NOTIFICACION_EXONERACION
        FOREIGN KEY (ID_DECISION) REFERENCES SIMT_DECISION_EXONERACION (ID_DECISION),
    CONSTRAINT FK_SIMF_USUARIO_SIMT_NOTIFICACION_EXONERACION
        FOREIGN KEY (ID_USUARIO_DESTINO) REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT CK_ESTADO_ENVIO_NOTIFICACION_EXON
        CHECK (ESTADO_ENVIO IN ('PEN', 'ENV', 'ERR')),
    CONSTRAINT CK_ESTADO_NOTIFICACION_EXON
        CHECK (ESTADO IN ('ACT', 'INA')),
    INDEX IDX_SIMT_NOTIF_EXON_DECISION (ID_DECISION),
    INDEX IDX_SIMT_NOTIF_EXON_DESTINO (ID_USUARIO_DESTINO)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Notificaciones enviadas a la técnica responsable por cada decisión de exoneración (HU-022).';

INSERT IGNORE INTO SIMC_ACCION_BITACORA (ACCION, DESCRIPCION)
VALUES ('DECISION_EXONERACION', 'Decisión de la jefatura sobre la solicitud de exoneración');

-- ---------------------------------------------------------------------------
-- Autorización: solo quien tenga el permiso EXONERACION_APROBAR puede decidir.
-- El rol JEFATURA_IMPORTACIONES lo tiene. Si existe el rol ADMINISTRADOR de pruebas
-- (09-Usuario-Pruebas-Administrador.sql), también lo recibe.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_PERMISO (CODIGO, NOMBRE, DESCRIPCION, ESTADO)
VALUES ('EXONERACION_APROBAR', 'Aprobar solicitudes de exoneración',
        'Registrar la decisión de aprobar, rechazar o solicitar modificación de una exoneración.', 'ACT')
ON DUPLICATE KEY UPDATE ESTADO = 'ACT';

INSERT INTO SIMM_ROL (NOMBRE, DESCRIPCION, ESTADO)
VALUES ('JEFATURA_IMPORTACIONES', 'Jefatura de la Unidad de Importaciones.', 'ACT')
ON DUPLICATE KEY UPDATE ESTADO = 'ACT';

INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO)
SELECT R.ID_ROL, P.ID_PERMISO, 'ACT'
FROM SIMM_ROL R
JOIN SIMM_PERMISO P ON P.CODIGO = 'EXONERACION_APROBAR'
WHERE R.NOMBRE IN ('JEFATURA_IMPORTACIONES', 'ADMINISTRADOR')
  AND NOT EXISTS (
      SELECT 1
      FROM SIMT_ROL_PERMISO RP
      WHERE RP.ID_ROL = R.ID_ROL
        AND RP.ID_PERMISO = P.ID_PERMISO
        AND RP.ESTADO = 'ACT'
  );
