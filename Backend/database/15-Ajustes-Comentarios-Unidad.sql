-- Autor: Equipo de Desarrollo SIM
-- Fecha: 18/10/2026
-- Descripción: Ajustes pedidos por la Unidad de Importaciones en la revisión de los videos y
--   mockups (Sprint 1 a Sprint 3), según el diagnóstico del 18/10/2026:
--   1. La exoneración se puede registrar sin contrato SICOP (trámites que no van por SICOP).
--   2. La exoneración ya no lleva permisos regulatorios, y admite el expediente de SICOP, el DUA
--      de aplicación, el comprobante de impuestos y otros documentos (además de la autorización
--      EXONET, que ya existía en el catálogo).
--   3. El estado inicial del pedido de importación es "Pendiente de pago" (ante la OAF) y el
--      pedido tiene un catálogo de estados para elegirlos de una lista.
--   4. La reimportación de una exportación temporal registra si pagó impuestos y cuánto.
-- Migración incremental: no elimina información y puede ejecutarse más de una vez.
-- Motor: MySQL 8+
--
-- Requisito previo: ejecutar los scripts 02 al 14.

SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- 1. Contrato SICOP opcional en la exoneración.
-- ---------------------------------------------------------------------------

ALTER TABLE SIMT_EXONERACION
    MODIFY CONTRATO_SICOP VARCHAR(45) NULL;

-- ---------------------------------------------------------------------------
-- 2. Documentos del expediente de exoneración.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_TIPO_DOCUMENTO (CODIGO, NOMBRE)
SELECT n.CODIGO, n.NOMBRE
FROM (
    SELECT 'EXPEDIENTE_SICOP' AS CODIGO, 'Expediente de SICOP' AS NOMBRE
    UNION ALL SELECT 'DUA_APLICACION', 'DUA de aplicación'
    UNION ALL SELECT 'COMPROB_IMPUESTOS', 'Comprobante de impuestos'
    UNION ALL SELECT 'OTROS_DOCUMENTOS', 'Otros documentos'
) n
WHERE NOT EXISTS (
    SELECT 1 FROM SIMM_TIPO_DOCUMENTO x WHERE x.CODIGO = n.CODIGO
);

-- Los documentos nuevos son opcionales: se muestran en el control documental (HU-021), pero no
-- impiden continuar a aprobación.
INSERT IGNORE INTO SIMT_REQUISITO_DOCUMENTAL
    (ID_TIPO_TRAMITE, ID_TIPO_DOCUMENTO, TIPO_EXONERACION, OBLIGATORIO)
SELECT TT.ID_TIPO_TRAMITE, TD.ID_TIPO_DOCUMENTO, 'GENERAL', 0
FROM SIMM_TIPO_TRAMITE TT
JOIN SIMM_TIPO_DOCUMENTO TD
    ON TD.CODIGO IN ('EXPEDIENTE_SICOP', 'AUTORIZACION_EXONET', 'DUA_APLICACION',
                     'COMPROB_IMPUESTOS', 'OTROS_DOCUMENTOS')
WHERE TT.CODIGO = 'EXONERACION';

-- La exoneración no requiere permisos regulatorios. El requisito se inactiva (no se borra) para
-- conservar la historia de los expedientes que ya tenían permisos adjuntos.
UPDATE SIMT_REQUISITO_DOCUMENTAL RD
JOIN SIMM_TIPO_TRAMITE TT ON TT.ID_TIPO_TRAMITE = RD.ID_TIPO_TRAMITE
JOIN SIMM_TIPO_DOCUMENTO TD ON TD.ID_TIPO_DOCUMENTO = RD.ID_TIPO_DOCUMENTO
SET RD.ESTADO = 'INA'
WHERE TT.CODIGO = 'EXONERACION'
  AND TD.CODIGO = 'PERMISO_REGULATORIO';

-- ---------------------------------------------------------------------------
-- 3. Estados del pedido de importación.
-- El estado inicial "Pendiente" pasa a llamarse "Pendiente de pago". Si alguien ya había
-- escrito "Pendiente de pago" a mano, se conserva ese y el viejo "Pendiente" se inactiva.
-- ---------------------------------------------------------------------------

SET @vln_IdTipoPedido = (SELECT ID_TIPO_TRAMITE FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'PEDIDO_PROV');
SET @vln_ExistePendientePago = (
    SELECT COUNT(*) FROM SIMM_ESTADO_TRAMITE
    WHERE ID_TIPO_TRAMITE = @vln_IdTipoPedido AND NOMBRE = 'Pendiente de pago'
);

UPDATE SIMM_ESTADO_TRAMITE
SET NOMBRE = 'Pendiente de pago'
WHERE ID_TIPO_TRAMITE = @vln_IdTipoPedido
  AND NOMBRE = 'Pendiente'
  AND @vln_ExistePendientePago = 0;

UPDATE SIMM_ESTADO_TRAMITE
SET ESTADO = 'INA', ES_INICIAL = 0
WHERE ID_TIPO_TRAMITE = @vln_IdTipoPedido
  AND NOMBRE = 'Pendiente'
  AND @vln_ExistePendientePago > 0;

-- Catálogo de estados del pedido. Se basa en las fechas de seguimiento que ya registra el pedido
-- (pago ante la OAF, transferencia, despacho, ingreso al país y desalmacenaje). Confirmar los
-- nombres con la Unidad; los estados que se hayan escrito a mano se conservan.
INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT @vln_IdTipoPedido, n.NOMBRE, n.ES_INICIAL, n.ES_FINAL, n.CATEGORIA, n.ORDEN
FROM (
    SELECT 'Pendiente de pago' AS NOMBRE, 1 AS ES_INICIAL, 0 AS ES_FINAL, 'PEN' AS CATEGORIA, 1 AS ORDEN
    UNION ALL SELECT 'Pago transferido', 0, 0, 'ACT', 2
    UNION ALL SELECT 'En tránsito', 0, 0, 'ACT', 3
    UNION ALL SELECT 'En aduana', 0, 0, 'ACT', 4
    UNION ALL SELECT 'Desalmacenado', 0, 0, 'ACT', 5
    UNION ALL SELECT 'Finalizado', 0, 1, 'FIN', 6
    UNION ALL SELECT 'Cancelado', 0, 1, 'FIN', 7
) n
WHERE @vln_IdTipoPedido IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM SIMM_ESTADO_TRAMITE e
      WHERE e.ID_TIPO_TRAMITE = @vln_IdTipoPedido AND e.NOMBRE = n.NOMBRE
  );

UPDATE SIMM_ESTADO_TRAMITE
SET ES_INICIAL = 1, CATEGORIA_ESTADO = 'PEN', ORDEN = 1, ESTADO = 'ACT'
WHERE ID_TIPO_TRAMITE = @vln_IdTipoPedido
  AND NOMBRE = 'Pendiente de pago';

-- ---------------------------------------------------------------------------
-- 4. Reimportación de la exportación temporal: ¿paga impuestos? y monto en colones.
-- MySQL no admite ADD COLUMN IF NOT EXISTS, por eso se revisa information_schema.
-- ---------------------------------------------------------------------------

SET @vln_ExisteColumna = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMT_TRAMITE_TEMPORAL'
      AND COLUMN_NAME = 'PAGA_IMPUESTOS_REIMPORTACION'
);
SET @vlc_Sentencia = IF(@vln_ExisteColumna = 0,
    'ALTER TABLE SIMT_TRAMITE_TEMPORAL
         ADD COLUMN PAGA_IMPUESTOS_REIMPORTACION TINYINT(1) NULL
             COMMENT ''exportación: 1 = la reimportación paga impuestos, 0 = no (garantía)'' AFTER FECHA_DESALMACENAJE,
         ADD COLUMN MONTO_IMPUESTOS_REIMPORTACION DECIMAL(15,2) NULL
             COMMENT ''exportación: impuestos pagados en la reimportación, en colones'' AFTER PAGA_IMPUESTOS_REIMPORTACION',
    'SELECT ''Las columnas de impuestos de la reimportación ya existen'' AS MENSAJE');
PREPARE vlo_Sentencia FROM @vlc_Sentencia;
EXECUTE vlo_Sentencia;
DEALLOCATE PREPARE vlo_Sentencia;
