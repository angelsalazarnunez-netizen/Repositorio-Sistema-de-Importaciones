-- Autor: Equipo de Desarrollo SIM
-- Fecha: 05/10/2026
-- Descripción: Script incremental de la HU-030 Consultar trámites activos. Agrega al catálogo de
--   estados el grupo con el que cada estado aparece en el panel de trámites activos, de modo que
--   la regla "qué está activo, pendiente o por llegar" se define por datos y no en Java.
-- Caso de uso asociado: HU-030 Consultar trámites activos
-- Motor: MySQL 8+
--
-- GRUPO_PANEL:
--   POR_LLEGAR  = el trámite aún no ingresa (estado inicial de pedidos, temporales, donaciones...).
--   EN_PROCESO  = el trámite está en gestión.
--   PERMISOS    = pendiente de permisos.
--   APROBACION  = pendiente de aprobación (HU-022).
--   EXONERACION = pendiente de exoneración.
--   FUERA       = no aparece en el panel: el trámite se desalmacenó o finalizó. Se conserva en la
--                 base de datos y en la bitácora; se consulta en el histórico del panel.
--   NULL        = estado nuevo sin clasificar: el panel lo ubica por su CATEGORIA_ESTADO.
-- Un estado final (ES_FINAL = 1 o CATEGORIA_ESTADO = 'FIN') nunca aparece en el panel.
--
-- Requisito previo: ejecutar los scripts 02 a 10.
-- El script puede ejecutarse más de una vez.

SET NAMES utf8mb4;

USE sim;

-- MySQL 8 no admite ADD COLUMN IF NOT EXISTS: la columna se agrega solo si no existe.
SET @vlc_Sentencia = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE SIMM_ESTADO_TRAMITE ADD COLUMN GRUPO_PANEL VARCHAR(15) NULL AFTER CATEGORIA_ESTADO',
              'SELECT 1')
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMM_ESTADO_TRAMITE'
      AND COLUMN_NAME = 'GRUPO_PANEL'
);
PREPARE vlo_Sentencia FROM @vlc_Sentencia;
EXECUTE vlo_Sentencia;
DEALLOCATE PREPARE vlo_Sentencia;

SET @vlc_Sentencia = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE SIMM_ESTADO_TRAMITE ADD CONSTRAINT CK_GRUPO_PANEL_VALOR CHECK (GRUPO_PANEL IS NULL OR GRUPO_PANEL IN (''POR_LLEGAR'', ''EN_PROCESO'', ''PERMISOS'', ''APROBACION'', ''EXONERACION'', ''FUERA''))',
              'SELECT 1')
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMM_ESTADO_TRAMITE'
      AND CONSTRAINT_NAME = 'CK_GRUPO_PANEL_VALOR'
);
PREPARE vlo_Sentencia FROM @vlc_Sentencia;
EXECUTE vlo_Sentencia;
DEALLOCATE PREPARE vlo_Sentencia;

-- ---------------------------------------------------------------------------
-- Clasificación general por categoría: PEN = por llegar, ACT = en proceso, FIN = fuera.
-- ---------------------------------------------------------------------------

UPDATE SIMM_ESTADO_TRAMITE
SET GRUPO_PANEL = CASE
        WHEN ES_FINAL = 1 OR CATEGORIA_ESTADO = 'FIN' THEN 'FUERA'
        WHEN CATEGORIA_ESTADO = 'PEN' THEN 'POR_LLEGAR'
        ELSE 'EN_PROCESO'
    END;

-- ---------------------------------------------------------------------------
-- Estados con un grupo propio.
-- ---------------------------------------------------------------------------

-- Pendientes de permisos (donación y desalmacenaje especial).
UPDATE SIMM_ESTADO_TRAMITE
SET GRUPO_PANEL = 'PERMISOS'
WHERE ES_FINAL = 0
  AND NOMBRE IN ('Pendiente de permisos', 'Pendiente de permisos especiales');

-- Pendiente de exoneración (donación).
UPDATE SIMM_ESTADO_TRAMITE
SET GRUPO_PANEL = 'EXONERACION'
WHERE ES_FINAL = 0
  AND NOMBRE = 'Pendiente de exoneración';

-- Pendiente de aprobación (exoneración, HU-022).
UPDATE SIMM_ESTADO_TRAMITE
SET GRUPO_PANEL = 'APROBACION'
WHERE ES_FINAL = 0
  AND NOMBRE = 'Pendiente de aprobación';

-- La solicitud de exoneración nace en revisión documental: ya está en gestión, no "por llegar".
UPDATE SIMM_ESTADO_TRAMITE E
JOIN SIMM_TIPO_TRAMITE T ON T.ID_TIPO_TRAMITE = E.ID_TIPO_TRAMITE
SET E.GRUPO_PANEL = 'EN_PROCESO'
WHERE T.CODIGO = 'EXONERACION'
  AND E.NOMBRE IN ('Pendiente revisión documental', 'Pendiente de modificación');

-- La donación en "Desalmacenaje" ya fue desalmacenada: sale del panel operativo aunque su
-- estado no sea final, y se consulta en el histórico.
UPDATE SIMM_ESTADO_TRAMITE E
JOIN SIMM_TIPO_TRAMITE T ON T.ID_TIPO_TRAMITE = E.ID_TIPO_TRAMITE
SET E.GRUPO_PANEL = 'FUERA'
WHERE T.CODIGO = 'DONACION'
  AND E.NOMBRE = 'Desalmacenaje';

-- Resultado de la clasificación.
SELECT T.NOMBRE AS TIPO_TRAMITE, E.NOMBRE AS ESTADO, E.CATEGORIA_ESTADO, E.GRUPO_PANEL
FROM SIMM_ESTADO_TRAMITE E
JOIN SIMM_TIPO_TRAMITE T ON T.ID_TIPO_TRAMITE = E.ID_TIPO_TRAMITE
ORDER BY T.NOMBRE, E.ORDEN;
