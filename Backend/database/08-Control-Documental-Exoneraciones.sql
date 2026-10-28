-- Autor: Equipo de Desarrollo SIM
-- Fecha: 05/10/2026
-- Descripción: Script incremental de la HU-021 Controlar documentos obligatorios del
--   trámite. Completa la matriz documental de la solicitud de exoneración y cataloga el
--   estado al que pasa el expediente cuando su documentación obligatoria está completa.
-- Caso de uso asociado: HU-021 Controlar documentos obligatorios del trámite
-- Motor: MySQL 8.0.16+
--
-- Requisito previo: ejecutar 02-Tablas-Tramites.sql y 03-Datos-Iniciales.sql.
-- El script puede ejecutarse más de una vez: no duplica registros.

SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- Matriz documental de la solicitud de exoneración.
-- Los cuatro documentos obligatorios son los mismos que exige el registro de la
-- HU-018: factura, documento de embarque, formulario S1 y lista de empaque.
-- EXONET, los permisos regulatorios y la traducción de la factura son opcionales:
-- se muestran en el control, pero no impiden continuar a aprobación.
-- TIPO_EXONERACION = 'GENERAL' indica que el requisito aplica a todas las
-- modalidades; un requisito propio de una modalidad se agrega con su código.
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO SIMT_REQUISITO_DOCUMENTAL
    (ID_TIPO_TRAMITE, ID_TIPO_DOCUMENTO, TIPO_EXONERACION, OBLIGATORIO)
SELECT TT.ID_TIPO_TRAMITE, TD.ID_TIPO_DOCUMENTO, 'GENERAL',
       CASE WHEN TD.CODIGO IN ('FACTURA', 'EMBARQUE', 'FORMULARIO_S1', 'PACKING_LIST')
            THEN 1 ELSE 0 END
FROM SIMM_TIPO_TRAMITE TT
JOIN SIMM_TIPO_DOCUMENTO TD
    ON TD.CODIGO IN ('FACTURA', 'EMBARQUE', 'FORMULARIO_S1', 'PACKING_LIST',
                     'EXONET', 'PERMISO_REGULATORIO', 'TRADUCCION_FACTURA')
WHERE TT.CODIGO = 'EXONERACION';

-- Si la matriz ya existía, se asegura la obligatoriedad acordada.
UPDATE SIMT_REQUISITO_DOCUMENTAL RD
JOIN SIMM_TIPO_TRAMITE TT ON TT.ID_TIPO_TRAMITE = RD.ID_TIPO_TRAMITE
JOIN SIMM_TIPO_DOCUMENTO TD ON TD.ID_TIPO_DOCUMENTO = RD.ID_TIPO_DOCUMENTO
SET RD.OBLIGATORIO = CASE WHEN TD.CODIGO IN ('FACTURA', 'EMBARQUE', 'FORMULARIO_S1', 'PACKING_LIST')
                          THEN 1 ELSE 0 END,
    RD.ESTADO = 'ACT'
WHERE TT.CODIGO = 'EXONERACION'
  AND RD.TIPO_EXONERACION = 'GENERAL'
  AND TD.CODIGO IN ('FACTURA', 'EMBARQUE', 'FORMULARIO_S1', 'PACKING_LIST',
                    'EXONET', 'PERMISO_REGULATORIO', 'TRADUCCION_FACTURA');

-- ---------------------------------------------------------------------------
-- Estado "Pendiente de aprobación" de la exoneración.
-- El expediente llega a este estado solo cuando la HU-021 confirma que toda su
-- documentación obligatoria fue adjuntada y recibida. La HU-022 Aprobar solicitud
-- de exoneración parte de este estado.
-- ---------------------------------------------------------------------------

INSERT IGNORE INTO SIMM_ESTADO_TRAMITE
    (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente de aprobación', 0, 0, 'ACT', 2
FROM SIMM_TIPO_TRAMITE
WHERE CODIGO = 'EXONERACION';
