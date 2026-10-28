-- Autor: Katherine Susana Valverde Fallas
-- Fecha: 10/09/2026
-- Descripción: Tablas del módulo de trámites y de los expedientes de desalmacenaje especial
--   y de donación.
-- Caso de uso asociado: HU-031 Registrar trámite de desalmacenamiento especial
-- Motor: MySQL 8+
-- Estándares aplicados: B02 nombre de tablas, B03 nombre de columnas, B04 llaves primarias,
--                       B05 llaves foráneas, B06 índices, B07 restricciones, B08 tipos de datos.
--
-- Historial de cambios:
--   Responsable: Josue Guevara Lostalo. Fecha: 10/09/2026.
--   Descripción: se agrega SIMT_DONACION para la HU-024 Registrar donaciones.
--   Responsable: Katherine Susana Valverde Fallas. Fecha: 15/09/2026.
--   Descripción: se agregan SIMT_EXONERACION, SIMC_ACCION_BITACORA y SIML_BITACORA para
--     la HU-018 Registrar solicitud de exoneración. La moneda y el valor estimado de
--     SIMT_TRAMITE pasan a ser opcionales porque la solicitud de exoneración no los declara.
--   Responsable: Josue Guevara Lostalo. Fecha: 16/09/2026.
--   Descripción: se agrega CATEGORIA_ESTADO a SIMM_ESTADO_TRAMITE y los índices por fecha de
--     creación, régimen aduanero y aduana de control de SIMT_TRAMITE para la HU-034 Buscar y
--     filtrar datos.
--   Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
--   Descripción: se eliminan primero las tablas de los trámites temporales (HU-025 y HU-027),
--     que referencian a SIMT_TRAMITE, para que el script se pueda volver a ejecutar. Después de
--     cargarlo hay que volver a ejecutar 06-Tablas-Tramites-Temporales.sql.
--   Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
--   Descripción: la descripción de SIMT_TRAMITE pasa de 100 a 255 caracteres para la HU-026
--     Registrar exportaciones temporales: la Unidad describe el equipo con su marca, modelo,
--     serie y placa, y sus registros superan los 100 caracteres.
--   Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
--   Descripción: SIMF_USUARIO vuelve a la definición que usan EntUsuario y los demás scripts
--     (ID_USUARIO INT, NOMBRE_COMPLETO y ESTADO_ROL), y las columnas que la referencian vuelven
--     a INT: con BIGINT fallaban las llaves foráneas de 04-Tabla-Sesion-HU004.sql y de
--     06-Tablas-Tramites-Temporales.sql. Se corrige la coma faltante de SIMT_REQUISITO_DOCUMENTAL.
--
-- Cómo cargarlo (PowerShell o MySQL Workbench):
--   mysql -u root -p < database/02-Tablas-Tramites.sql

-- Declara la codificacion de la conexion: sin esto el cliente mysql de Windows
-- interpreta el archivo con la codificacion de la consola y corrompe las tildes.
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS sim
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sim;

-- SIMF_USUARIO pertenece a la HU-002 (registrar usuario). Aquí se crea solo con las
-- columnas mínimas que necesita la llave foránea del expediente, para que esta historia
-- se pueda cargar y probar por separado. Cuando la HU-002 llegue a main, su script define
-- la tabla completa y este CREATE queda sin efecto por el IF NOT EXISTS.
CREATE TABLE IF NOT EXISTS SIMF_USUARIO (
    ID_USUARIO              INT             NOT NULL AUTO_INCREMENT,
    NOMBRE_COMPLETO         VARCHAR(150)    NOT NULL,
    NUMERO_CEDULA           VARCHAR(20)     NOT NULL,
    FECHA_NACIMIENTO        DATE            NOT NULL,
    CORREO_INSTITUCIONAL    VARCHAR(120)    NOT NULL,
    CONTRASENA_HASH         VARCHAR(255)    NOT NULL,
    ESTADO_VALIDACION       CHAR(3)         NOT NULL DEFAULT 'PEN',
    ESTADO_ROL              CHAR(3)         NOT NULL DEFAULT 'PEN',
    TOKEN_VERIFICACION      VARCHAR(64)     NULL,
    TIME_STAMP              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMF_USUARIO PRIMARY KEY (ID_USUARIO),
    CONSTRAINT AK_SIMF_USUARIO_CORREO UNIQUE (CORREO_INSTITUCIONAL),
    CONSTRAINT AK_SIMF_USUARIO_CEDULA UNIQUE (NUMERO_CEDULA)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Usuarios del sistema. La HU-002 es la dueña de esta tabla.';

-- ---------------------------------------------------------------------------
-- Catálogos del módulo de trámites.
-- Las tablas de tipo M incorporan ESTADO y TIME_STAMP según el estándar B07.
-- ---------------------------------------------------------------------------

-- Las tablas que referencian a SIMT_TRAMITE y a SIMF_DOCUMENTO se eliminan de primero.
-- Las de los trámites temporales se crean en 06-Tablas-Tramites-Temporales.sql.
DROP TABLE IF EXISTS SIMT_ALERTA_TEMPORAL;
DROP TABLE IF EXISTS SIMT_TEMPORAL_AGENTE;
DROP TABLE IF EXISTS SIMR_TRAMITE_TEMPORAL_AGENTE;
DROP TABLE IF EXISTS SIMT_TRAMITE_TEMPORAL;
DROP TABLE IF EXISTS SIML_BITACORA;
DROP TABLE IF EXISTS SIMT_REVISION_DOCUMENTO;
DROP TABLE IF EXISTS SIMT_REQUISITO_DOCUMENTAL;
DROP TABLE IF EXISTS SIMC_ACCION_BITACORA;
DROP TABLE IF EXISTS SIMT_EXONERACION;
DROP TABLE IF EXISTS SIMT_DONACION;
DROP TABLE IF EXISTS SIMH_ESTADO_TRAMITE;
DROP TABLE IF EXISTS SIMF_DOCUMENTO;
DROP TABLE IF EXISTS SIMT_DESALMACENAJE_ESPEC;
DROP TABLE IF EXISTS SIMT_TRAMITE;
DROP TABLE IF EXISTS SIMM_TIPO_DOCUMENTO;
DROP TABLE IF EXISTS SIMM_ESTADO_TRAMITE;
DROP TABLE IF EXISTS SIMM_TIPO_TRAMITE;
DROP TABLE IF EXISTS SIMM_MONEDA;
DROP TABLE IF EXISTS SIMM_PAIS;

CREATE TABLE SIMM_PAIS (
    ID_PAIS         INT             NOT NULL AUTO_INCREMENT,
    CODIGO_ISO      CHAR(3)         NOT NULL,
    NOMBRE          VARCHAR(50)     NOT NULL,
    ESTADO          CHAR(3)         NOT NULL DEFAULT 'ACT',
    TIME_STAMP      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMM_PAIS PRIMARY KEY (ID_PAIS),
    CONSTRAINT AK_SIMM_PAIS UNIQUE (CODIGO_ISO),
    CONSTRAINT CK_ESTADO_PAIS_VALOR_DEFECTO CHECK (ESTADO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo de países utilizados como origen de la mercancía de un trámite.';

CREATE INDEX IDX_SIMM_PAIS_NOMBRE ON SIMM_PAIS (NOMBRE);

CREATE TABLE SIMM_MONEDA (
    ID_MONEDA       INT             NOT NULL AUTO_INCREMENT,
    CODIGO_ISO      CHAR(3)         NOT NULL,
    NOMBRE          VARCHAR(50)     NOT NULL,
    SIMBOLO         VARCHAR(5)      NOT NULL,
    ESTADO          CHAR(3)         NOT NULL DEFAULT 'ACT',
    TIME_STAMP      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMM_MONEDA PRIMARY KEY (ID_MONEDA),
    CONSTRAINT AK_SIMM_MONEDA UNIQUE (CODIGO_ISO),
    CONSTRAINT CK_ESTADO_MONEDA_VALOR_DEFECTO CHECK (ESTADO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo de monedas en las que se declara el valor estimado de la mercancía.';

-- REQUIERE_SICOP y REQUIERE_ORDEN_COMPRA gobiernan por datos la regla de la HU-031:
-- el desalmacenaje especial se registra con ambos indicadores en 0.
CREATE TABLE SIMM_TIPO_TRAMITE (
    ID_TIPO_TRAMITE         INT                 NOT NULL AUTO_INCREMENT,
    CODIGO                  VARCHAR(15)         NOT NULL,
    NOMBRE                  VARCHAR(60)         NOT NULL,
    PREFIJO_REFERENCIA      VARCHAR(10)         NOT NULL,
    REQUIERE_SICOP          TINYINT UNSIGNED    NOT NULL DEFAULT 0, -- 0 = no exigido, 1 = exigido
    REQUIERE_ORDEN_COMPRA   TINYINT UNSIGNED    NOT NULL DEFAULT 0, -- 0 = no exigido, 1 = exigido
    ESTADO                  CHAR(3)             NOT NULL DEFAULT 'ACT',
    TIME_STAMP              TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMM_TIPO_TRAMITE PRIMARY KEY (ID_TIPO_TRAMITE),
    CONSTRAINT AK_SIMM_TIPO_TRAMITE UNIQUE (CODIGO),
    CONSTRAINT AK_SIMM_TIPO_TRAMITE_PREFIJO UNIQUE (PREFIJO_REFERENCIA),
    CONSTRAINT CK_REQUIERE_SICOP_VALOR_LOGICO CHECK (REQUIERE_SICOP IN (0, 1)),
    CONSTRAINT CK_REQUIERE_ORDEN_COMPRA_LOGICO CHECK (REQUIERE_ORDEN_COMPRA IN (0, 1)),
    CONSTRAINT CK_ESTADO_TIPO_TRAMITE_DEFECTO CHECK (ESTADO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo de tipos de trámite. El prefijo de referencia es único y diferencia las referencias internas de cada tipo.';

-- ES_INICIAL identifica el estado con el que nace el expediente. Para la HU-031 es Pendiente.
-- CATEGORIA_ESTADO agrupa los estados propios de cada tipo de trámite en los tres estados
-- funcionales que consulta la HU-034: pendiente de ingreso, trámite activo y trámite finalizado.
-- Se declara en cada estado del catálogo; ES_INICIAL y ES_FINAL conservan su significado.
CREATE TABLE SIMM_ESTADO_TRAMITE (
    ID_ESTADO_TRAMITE   INT                 NOT NULL AUTO_INCREMENT,
    ID_TIPO_TRAMITE     INT                 NOT NULL,
    NOMBRE              VARCHAR(50)         NOT NULL,
    ES_INICIAL          TINYINT UNSIGNED    NOT NULL DEFAULT 0, -- 1 = estado con el que se crea el expediente
    ES_FINAL            TINYINT UNSIGNED    NOT NULL DEFAULT 0, -- 1 = estado que cierra el expediente
    CATEGORIA_ESTADO    CHAR(3)             NOT NULL DEFAULT 'ACT', -- PEN = pendiente de ingreso, ACT = trámite activo, FIN = trámite finalizado
    ORDEN               SMALLINT            NOT NULL DEFAULT 1,
    ESTADO              CHAR(3)             NOT NULL DEFAULT 'ACT',
    TIME_STAMP          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMM_ESTADO_TRAMITE PRIMARY KEY (ID_ESTADO_TRAMITE),
    CONSTRAINT AK_SIMM_ESTADO_TRAMITE UNIQUE (ID_TIPO_TRAMITE, NOMBRE),
    CONSTRAINT FK_SIMM_TIPO_TRAMITE_SIMM_ESTADO_TRAMITE
        FOREIGN KEY (ID_TIPO_TRAMITE) REFERENCES SIMM_TIPO_TRAMITE (ID_TIPO_TRAMITE),
    CONSTRAINT CK_ES_INICIAL_VALOR_LOGICO CHECK (ES_INICIAL IN (0, 1)),
    CONSTRAINT CK_ES_FINAL_VALOR_LOGICO CHECK (ES_FINAL IN (0, 1)),
    CONSTRAINT CK_CATEGORIA_ESTADO_VALOR_ADMITIDO CHECK (CATEGORIA_ESTADO IN ('PEN', 'ACT', 'FIN')),
    CONSTRAINT CK_ESTADO_ESTADO_TRAMITE_DEFECTO CHECK (ESTADO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo de estados por tipo de trámite. Define el estado inicial y el estado final de cada expediente.';

CREATE TABLE SIMM_TIPO_DOCUMENTO (
    ID_TIPO_DOCUMENTO   INT             NOT NULL AUTO_INCREMENT,
    CODIGO              VARCHAR(20)     NOT NULL,
    NOMBRE              VARCHAR(80)     NOT NULL,
    ESTADO              CHAR(3)         NOT NULL DEFAULT 'ACT',
    TIME_STAMP          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT PK_SIMM_TIPO_DOCUMENTO PRIMARY KEY (ID_TIPO_DOCUMENTO),
    CONSTRAINT AK_SIMM_TIPO_DOCUMENTO UNIQUE (CODIGO),
    CONSTRAINT CK_ESTADO_TIPO_DOCUMENTO_DEFECTO CHECK (ESTADO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catálogo de tipos de documento que se adjuntan a un expediente de trámite.';

-- ---------------------------------------------------------------------------
-- Expediente del trámite.
-- SIMT_TRAMITE concentra los datos comunes a todos los tipos de trámite;
-- SIMT_DESALMACENAJE_ESPEC y SIMT_DONACION agregan los datos propios de cada
-- tipo y comparten con él la llave primaria.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMT_TRAMITE (
    ID_TRAMITE              INT             NOT NULL AUTO_INCREMENT,
    REFERENCIA_INTERNA      VARCHAR(45)     NOT NULL,
    ID_TIPO_TRAMITE         INT             NOT NULL,
    ID_ESTADO_TRAMITE       INT             NOT NULL,
    ID_USUARIO_RESPONSABLE  INT             NOT NULL,
    ID_PAIS_ORIGEN          INT             NULL,
    ID_MONEDA               INT             NULL, -- NULL = el tipo de trámite no declara valor
    VALOR_ESTIMADO          DECIMAL(13,2)   NULL, -- NULL = el tipo de trámite no declara valor
    DESCRIPCION             VARCHAR(255)    NULL,
    REGIMEN_ADUANERO        VARCHAR(60)     NULL,
    ADUANA_CONTROL          VARCHAR(60)     NULL,
    -- HU-036: datos de costo que alimentan el reporte de consultas internas. El valor
    -- estimado hace de monto CIF; los impuestos y el bodegaje se registran al cerrar el
    -- tramite, por eso admiten nulos mientras el tramite esta en proceso.
    -- HU-036: a quien se le carga el costo. El tipo distingue si es la propia Unidad de
    -- Importaciones, el partido solicitante o una orden de compra, y el detalle guarda su
    -- nombre o su numero, que es lo que agrupa el reporte.
    TIPO_PARTIDO_RESPONSABLE VARCHAR(25)    NULL,
    PARTIDO_RESPONSABLE     VARCHAR(120)    NULL,
    IMPUESTOS_PAGADOS       DECIMAL(13,2)   NULL,
    IMPUESTOS_EXONERADOS    DECIMAL(13,2)   NULL,
    MONTO_BODEGAJE          DECIMAL(13,2)   NULL,
    OBSERVACIONES           VARCHAR(4000)   NULL,
    FECHA_CREACION          DATETIME        NOT NULL,
    FECHA_CIERRE            DATE            NULL,
    CONSTRAINT PK_SIMT_TRAMITE PRIMARY KEY (ID_TRAMITE),
    CONSTRAINT AK_SIMT_TRAMITE UNIQUE (REFERENCIA_INTERNA),
    CONSTRAINT FK_SIMM_TIPO_TRAMITE_SIMT_TRAMITE
        FOREIGN KEY (ID_TIPO_TRAMITE) REFERENCES SIMM_TIPO_TRAMITE (ID_TIPO_TRAMITE),
    CONSTRAINT FK_SIMM_ESTADO_TRAMITE_SIMT_TRAMITE
        FOREIGN KEY (ID_ESTADO_TRAMITE) REFERENCES SIMM_ESTADO_TRAMITE (ID_ESTADO_TRAMITE),
    CONSTRAINT FK_SIMF_USUARIO_SIMT_TRAMITE
        FOREIGN KEY (ID_USUARIO_RESPONSABLE) REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT FK_SIMM_PAIS_SIMT_TRAMITE
        FOREIGN KEY (ID_PAIS_ORIGEN) REFERENCES SIMM_PAIS (ID_PAIS),
    CONSTRAINT FK_SIMM_MONEDA_SIMT_TRAMITE
        FOREIGN KEY (ID_MONEDA) REFERENCES SIMM_MONEDA (ID_MONEDA),
    CONSTRAINT CK_VALOR_ESTIMADO_MAYOR_CERO CHECK (VALOR_ESTIMADO > 0),
    CONSTRAINT CK_TIPO_PARTIDO_RESPONSABLE_ADMITIDO
        CHECK (TIPO_PARTIDO_RESPONSABLE IS NULL OR TIPO_PARTIDO_RESPONSABLE IN
               ('UNIDAD_IMPORTACIONES', 'PARTIDO_SOLICITANTE', 'ORDEN_COMPRA')),
    CONSTRAINT CK_IMPUESTOS_PAGADOS_NO_NEGATIVO
        CHECK (IMPUESTOS_PAGADOS IS NULL OR IMPUESTOS_PAGADOS >= 0),
    CONSTRAINT CK_IMPUESTOS_EXONERADOS_NO_NEGATIVO
        CHECK (IMPUESTOS_EXONERADOS IS NULL OR IMPUESTOS_EXONERADOS >= 0),
    CONSTRAINT CK_MONTO_BODEGAJE_NO_NEGATIVO
        CHECK (MONTO_BODEGAJE IS NULL OR MONTO_BODEGAJE >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Expediente común a todos los tipos de trámite de importación. La referencia interna es única en todo el sistema.';

CREATE INDEX IDX_SIMT_TRAMITE_ID_TIPO_TRAMITE ON SIMT_TRAMITE (ID_TIPO_TRAMITE);
CREATE INDEX IDX_SIMT_TRAMITE_ID_ESTADO ON SIMT_TRAMITE (ID_ESTADO_TRAMITE);
CREATE INDEX IDX_SIMT_TRAMITE_ID_USUARIO_RESP ON SIMT_TRAMITE (ID_USUARIO_RESPONSABLE);
-- Criterios de la búsqueda de trámites (HU-034).
CREATE INDEX IDX_SIMT_TRAMITE_FECHA_CREACION ON SIMT_TRAMITE (FECHA_CREACION);
CREATE INDEX IDX_SIMT_TRAMITE_REGIMEN_ADUANERO ON SIMT_TRAMITE (REGIMEN_ADUANERO);
CREATE INDEX IDX_SIMT_TRAMITE_ADUANA_CONTROL ON SIMT_TRAMITE (ADUANA_CONTROL);

-- El nombre SIMT_DESALMACENAJE_ESPEC aplica la regla de recorte del estándar B05:
-- SIMT_DESALMACENAJE_ESPECIAL ocupa 27 caracteres y supera el máximo de 25, por lo que
-- se recorta la palabra de la derecha hasta terminar en consonante: ESPECIAL -> ESPEC.
CREATE TABLE SIMT_DESALMACENAJE_ESPEC (
    ID_TRAMITE                  INT             NOT NULL,
    ORIGEN_DEPENDENCIA          VARCHAR(120)    NOT NULL,
    JUSTIFICACION               VARCHAR(1000)   NOT NULL,
    ESTADO_INVESTIGACION        VARCHAR(50)     NULL,
    ID_TRAMITE_RECLASIFICADO    INT             NULL,
    -- HU-045: constancia de si el expediente reune la documentacion que exige el tramite.
    ESTADO_DOCUMENTAL           VARCHAR(10)     NULL,
    OBSERVACIONES_DOCUMENTALES  VARCHAR(1000)   NULL,
    CONSTRAINT PK_SIMT_DESALMACENAJE_ESPEC PRIMARY KEY (ID_TRAMITE),
    CONSTRAINT FK_SIMT_TRAMITE_SIMT_DESALMACENAJE_ESPEC
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMT_TRAMITE_SIMT_DESALM_ESPEC_RECLASIF
        FOREIGN KEY (ID_TRAMITE_RECLASIFICADO) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT CK_ESTADO_DOCUMENTAL_VALOR_ADMITIDO
        CHECK (ESTADO_DOCUMENTAL IS NULL OR ESTADO_DOCUMENTAL IN ('COMPLETO', 'INCOMPLETO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Datos propios del trámite de desalmacenaje especial. Comparte la llave primaria con SIMT_TRAMITE.';

-- ---------------------------------------------------------------------------
-- Documentación del expediente.
-- La HU-031 exige documentación flexible: actas antiguas, justificaciones y
-- documentos atípicos, por lo que no se restringe el formato del archivo.
-- ---------------------------------------------------------------------------

-- ID_DOCUMENTO_ANTERIOR encadena las versiones de un mismo documento: al sustituir un
-- archivo, la versión previa queda en estado INA y la nueva apunta hacia ella.
CREATE TABLE SIMF_DOCUMENTO (
    ID_DOCUMENTO            INT             NOT NULL AUTO_INCREMENT,
    ID_TRAMITE              INT             NOT NULL,
    ID_TIPO_DOCUMENTO       INT             NOT NULL,
    ID_USUARIO_CARGA        INT             NOT NULL,
    ID_DOCUMENTO_ANTERIOR   INT             NULL,
    NOMBRE_ARCHIVO          VARCHAR(120)    NOT NULL,
    RUTA_ARCHIVO            VARCHAR(255)    NOT NULL,
    VERSION                 SMALLINT        NOT NULL DEFAULT 1,
    ESTADO_DOCUMENTO        CHAR(3)         NOT NULL DEFAULT 'ACT',
    FECHA_CARGA             DATETIME        NOT NULL,
    CONSTRAINT PK_SIMF_DOCUMENTO PRIMARY KEY (ID_DOCUMENTO),
    CONSTRAINT FK_SIMT_TRAMITE_SIMF_DOCUMENTO
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMM_TIPO_DOCUMENTO_SIMF_DOCUMENTO
        FOREIGN KEY (ID_TIPO_DOCUMENTO) REFERENCES SIMM_TIPO_DOCUMENTO (ID_TIPO_DOCUMENTO),
    CONSTRAINT FK_SIMF_USUARIO_SIMF_DOCUMENTO
        FOREIGN KEY (ID_USUARIO_CARGA) REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT FK_SIMF_DOCUMENTO_SIMF_DOCUMENTO_ANTERIOR
        FOREIGN KEY (ID_DOCUMENTO_ANTERIOR) REFERENCES SIMF_DOCUMENTO (ID_DOCUMENTO),
    CONSTRAINT CK_ESTADO_DOCUMENTO_VALOR_DEFECTO CHECK (ESTADO_DOCUMENTO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Archivos de respaldo asociados a un expediente de trámite.';

CREATE INDEX IDX_SIMF_DOCUMENTO_ID_TRAMITE ON SIMF_DOCUMENTO (ID_TRAMITE);

-- ---------------------------------------------------------------------------
-- Requisitos documentales de los trámites.
-- Permite definir por datos cuáles documentos son obligatorios u opcionales
-- para una modalidad de exoneración, sin codificar la regla en Java.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMT_REQUISITO_DOCUMENTAL (
    ID_REQUISITO_DOCUMENTAL INT             NOT NULL AUTO_INCREMENT,
    ID_TIPO_TRAMITE         INT             NOT NULL,
    ID_TIPO_DOCUMENTO       INT             NOT NULL,
    TIPO_EXONERACION        VARCHAR(20)     NOT NULL DEFAULT 'GENERAL',
    OBLIGATORIO             TINYINT UNSIGNED NOT NULL DEFAULT 1,
    ESTADO                  CHAR(3)         NOT NULL DEFAULT 'ACT',
    TIME_STAMP              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT PK_SIMT_REQUISITO_DOCUMENTAL
        PRIMARY KEY (ID_REQUISITO_DOCUMENTAL),
    CONSTRAINT FK_SIMM_TIPO_TRAMITE_SIMT_REQUISITO_DOCUMENTAL
        FOREIGN KEY (ID_TIPO_TRAMITE) REFERENCES SIMM_TIPO_TRAMITE (ID_TIPO_TRAMITE),
    CONSTRAINT FK_SIMM_TIPO_DOCUMENTO_SIMT_REQUISITO_DOCUMENTAL
        FOREIGN KEY (ID_TIPO_DOCUMENTO) REFERENCES SIMM_TIPO_DOCUMENTO (ID_TIPO_DOCUMENTO),
    CONSTRAINT AK_SIMT_REQUISITO_DOCUMENTAL
        UNIQUE (ID_TIPO_TRAMITE, ID_TIPO_DOCUMENTO, TIPO_EXONERACION),
    CONSTRAINT CK_OBLIGATORIO_REQUISITO_VALOR CHECK (OBLIGATORIO IN (0, 1)),
    CONSTRAINT CK_ESTADO_REQUISITO_VALOR CHECK (ESTADO IN ('ACT', 'INA'))) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Configuración de documentos requeridos u opcionales según el tipo y modalidad del trámite.';

-- ---------------------------------------------------------------------------
-- Revisión documental.
-- Cada registro conserva el resultado de una revisión humana realizada
-- sobre una versión concreta de un documento del expediente.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMT_REVISION_DOCUMENTO (
    ID_REVISION_DOCUMENTO   INT             NOT NULL AUTO_INCREMENT,
    ID_DOCUMENTO            INT             NOT NULL,
    ID_USUARIO_REVISOR      INT             NOT NULL,
    ESTADO_REVISION         VARCHAR(10)     NOT NULL,
    OBSERVACION             VARCHAR(1000)   NULL,
    FECHA_HORA              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ESTADO                  CHAR(3)         NOT NULL DEFAULT 'ACT',

    CONSTRAINT PK_SIMT_REVISION_DOCUMENTO
        PRIMARY KEY (ID_REVISION_DOCUMENTO),
    CONSTRAINT FK_SIMF_DOCUMENTO_SIMT_REVISION_DOCUMENTO
        FOREIGN KEY (ID_DOCUMENTO)
        REFERENCES SIMF_DOCUMENTO (ID_DOCUMENTO),
    CONSTRAINT FK_SIMF_USUARIO_SIMT_REVISION_DOCUMENTO
        FOREIGN KEY (ID_USUARIO_REVISOR)
        REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT CK_ESTADO_REVISION_VALOR
        CHECK (ESTADO_REVISION IN ('RECIBIDO','PENDIENTE','RECHAZADO')),
    CONSTRAINT CK_ESTADO_REVISION_REGISTRO
        CHECK (ESTADO IN ('ACT', 'INA'))) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Historial de revisiones realizadas sobre documentos asociados a un expediente.';
CREATE INDEX IDX_SIMT_REQUISITO_ID_TIPO
    ON SIMT_REQUISITO_DOCUMENTAL (ID_TIPO_TRAMITE);
CREATE INDEX IDX_SIMT_REQUISITO_ID_DOCUMENTO
    ON SIMT_REQUISITO_DOCUMENTAL (ID_TIPO_DOCUMENTO);
CREATE INDEX IDX_SIMT_REVISION_ID_DOCUMENTO
    ON SIMT_REVISION_DOCUMENTO (ID_DOCUMENTO);
CREATE INDEX IDX_SIMT_REVISION_ID_REVISOR
    ON SIMT_REVISION_DOCUMENTO (ID_USUARIO_REVISOR);

-- ---------------------------------------------------------------------------
-- Expediente de la donación (HU-024).
-- Se crea después de SIMF_DOCUMENTO porque ID_DOCUMENTO_ACEPTACION referencia
-- esa tabla: la resolución o acta de aceptación es obligatoria, de manera que la
-- donación no puede existir sin el documento que la respalda.
-- El país de origen, el valor estimado, la moneda, el usuario responsable y las
-- observaciones viven en SIMT_TRAMITE y no se duplican aquí.
-- FUNDACION_UCR es la donación por medio de la Fundación UCR (antes FUNDEVI), cuya
-- referencia interna usa la sigla DF-; las demás donaciones usan la sigla D-.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMT_DONACION (
    ID_TRAMITE                  INT             NOT NULL,
    ID_DOCUMENTO_ACEPTACION     INT             NOT NULL,
    TIPO_DONACION               VARCHAR(20)     NOT NULL,
    NOMBRE_DONANTE              VARCHAR(150)    NOT NULL,
    REFERENCIA_FUNDACION_UCR    VARCHAR(80)     NULL,
    CONSTRAINT PK_SIMT_DONACION PRIMARY KEY (ID_TRAMITE),
    CONSTRAINT FK_SIMT_TRAMITE_SIMT_DONACION
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMF_DOCUMENTO_SIMT_DONACION
        FOREIGN KEY (ID_DOCUMENTO_ACEPTACION) REFERENCES SIMF_DOCUMENTO (ID_DOCUMENTO),
    CONSTRAINT CK_TIPO_DONACION_VALOR_ADMITIDO
        CHECK (TIPO_DONACION IN ('NACIONAL', 'INTERNACIONAL', 'FUNDACION_UCR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Datos propios del trámite de donación. Comparte la llave primaria con SIMT_TRAMITE.';

-- ---------------------------------------------------------------------------
-- Solicitud de exoneración (HU-018).
-- El contrato SICOP y la orden de compra GECO son opcionales: hay exoneraciones que no
-- se tramitan por SICOP y la OC de GECO no siempre existe al registrar (comentarios de la
-- Unidad; ver 15-Ajustes-Comentarios-Unidad.sql para bases ya creadas).
-- NUMERO_EXONET, RESOLUCION y FECHA_RESOLUCION se completan en etapas posteriores
-- del trámite, por eso admiten nulo.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMT_EXONERACION (
    ID_TRAMITE                  INT             NOT NULL,
    TIPO_EXONERACION            VARCHAR(20)     NOT NULL,
    NUMERO_CONTRATACION         VARCHAR(45)     NOT NULL,
    ORDEN_COMPRA_GECO           VARCHAR(45)     NULL, -- la OC de GECO no siempre existe al registrar
    CONTRATO_SICOP              VARCHAR(45)     NULL, -- opcional: no todas las exoneraciones van por SICOP
    NUMERO_EXONET               VARCHAR(45)     NULL, -- numero de solicitud que devuelve EXONET
    NUMERO_AUTORIZACION_EXONET  VARCHAR(45)     NULL, -- numero de autorizacion que devuelve EXONET
    RESOLUCION                  VARCHAR(45)     NULL,
    FECHA_RESOLUCION            DATE            NULL,
    CONSTRAINT PK_SIMT_EXONERACION PRIMARY KEY (ID_TRAMITE),
    CONSTRAINT FK_SIMT_TRAMITE_SIMT_EXONERACION
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT CK_TIPO_EXONERACION_VALOR_ADMITIDO
        CHECK (TIPO_EXONERACION IN ('COMPRA_INSTITUCIONAL', 'DONACION', 'CLIENTE_EXTERNO',
                                    'VEHICULOS', 'MOVIM_TRASPASO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Datos propios de la solicitud de exoneración. Comparte la llave primaria con SIMT_TRAMITE.';

-- ---------------------------------------------------------------------------
-- Historial de estados del expediente.
-- El primer movimiento se registra al crear el trámite, con estado anterior nulo,
-- de manera que el seguimiento muestre desde cuándo el expediente está Pendiente.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMH_ESTADO_TRAMITE (
    ID_HISTORIAL        INT             NOT NULL AUTO_INCREMENT,
    ID_TRAMITE          INT             NOT NULL,
    ID_ESTADO_ANTERIOR  INT             NULL,
    ID_ESTADO_NUEVO     INT             NOT NULL,
    ID_USUARIO          INT             NOT NULL,
    JUSTIFICACION       VARCHAR(255)    NULL,
    FECHA_HORA          DATETIME        NOT NULL,
    CONSTRAINT PK_SIMH_ESTADO_TRAMITE PRIMARY KEY (ID_HISTORIAL),
    CONSTRAINT FK_SIMT_TRAMITE_SIMH_ESTADO_TRAMITE
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMM_ESTADO_TRAMITE_SIMH_ANTERIOR
        FOREIGN KEY (ID_ESTADO_ANTERIOR) REFERENCES SIMM_ESTADO_TRAMITE (ID_ESTADO_TRAMITE),
    CONSTRAINT FK_SIMM_ESTADO_TRAMITE_SIMH_NUEVO
        FOREIGN KEY (ID_ESTADO_NUEVO) REFERENCES SIMM_ESTADO_TRAMITE (ID_ESTADO_TRAMITE),
    CONSTRAINT FK_SIMF_USUARIO_SIMH_ESTADO_TRAMITE
        FOREIGN KEY (ID_USUARIO) REFERENCES SIMF_USUARIO (ID_USUARIO)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Historial de cambios de estado de un expediente de trámite.';

CREATE INDEX IDX_SIMH_ESTADO_TRAMITE_ID_TRAMITE ON SIMH_ESTADO_TRAMITE (ID_TRAMITE);

-- ---------------------------------------------------------------------------
-- Bitácora del expediente (HU-018).
-- Toda acción sobre una solicitud queda registrada. Las acciones admitidas superan
-- tres valores, por lo que se catalogan en una tabla de tipo C (estándar B07).
-- Un comentario registrado por error se marca INA en lugar de borrarse, y su
-- eliminación queda a su vez registrada, de modo que no se pierde la auditoría.
-- ---------------------------------------------------------------------------

CREATE TABLE SIMC_ACCION_BITACORA (
    ACCION          VARCHAR(30)     NOT NULL,
    DESCRIPCION     VARCHAR(100)    NOT NULL,
    CONSTRAINT PK_SIMC_ACCION_BITACORA PRIMARY KEY (ACCION)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Acciones que se registran en la bitácora de un expediente.';

CREATE TABLE SIML_BITACORA (
    ID_BITACORA             INT             NOT NULL AUTO_INCREMENT,
    ID_TRAMITE              INT             NOT NULL,
    ID_USUARIO              INT             NOT NULL,
    ACCION                  VARCHAR(30)     NOT NULL,
    ENTIDAD_AFECTADA        VARCHAR(45)     NOT NULL,
    ID_REGISTRO_AFECTADO    INT             NULL,
    DATOS_ANTERIORES        TEXT            NULL,
    DATOS_NUEVOS            TEXT            NULL,
    ESTADO                  CHAR(3)         NOT NULL DEFAULT 'ACT',
    FECHA_HORA              DATETIME        NOT NULL,
    CONSTRAINT PK_SIML_BITACORA PRIMARY KEY (ID_BITACORA),
    CONSTRAINT FK_SIMT_TRAMITE_SIML_BITACORA
        FOREIGN KEY (ID_TRAMITE) REFERENCES SIMT_TRAMITE (ID_TRAMITE),
    CONSTRAINT FK_SIMF_USUARIO_SIML_BITACORA
        FOREIGN KEY (ID_USUARIO) REFERENCES SIMF_USUARIO (ID_USUARIO),
    CONSTRAINT FK_SIMC_ACCION_BITACORA_SIML_BITACORA
        FOREIGN KEY (ACCION) REFERENCES SIMC_ACCION_BITACORA (ACCION),
    CONSTRAINT CK_ESTADO_BITACORA_VALOR_DEFECTO CHECK (ESTADO IN ('ACT', 'INA'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bitácora de acciones realizadas sobre un expediente de trámite.';

CREATE INDEX IDX_SIML_BITACORA_ID_TRAMITE ON SIML_BITACORA (ID_TRAMITE);
