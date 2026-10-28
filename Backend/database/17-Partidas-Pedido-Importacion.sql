-- Autor: Equipo de Desarrollo SIM
-- Fecha: 24/10/2026
-- Descripción: permite registrar una o dos partidas (1, 2 o 5) en un pedido de importación.
-- Migración incremental e idempotente. Motor: MySQL 8+.

SET NAMES utf8mb4;
USE sim;

SET @vln_ExistePartida2 = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMT_PEDIDO_PROV'
      AND COLUMN_NAME = 'PARTIDA_2'
);
SET @vlc_AgregarPartida2 = IF(
    @vln_ExistePartida2 = 0,
    'ALTER TABLE SIMT_PEDIDO_PROV ADD COLUMN PARTIDA_2 SMALLINT NULL AFTER PARTIDA',
    'SELECT 1'
);
PREPARE stmt_AgregarPartida2 FROM @vlc_AgregarPartida2;
EXECUTE stmt_AgregarPartida2;
DEALLOCATE PREPARE stmt_AgregarPartida2;

SET @vln_ExisteCheckPartida = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMT_PEDIDO_PROV'
      AND CONSTRAINT_NAME = 'CK_PARTIDA_PEDIDO_PROV'
      AND CONSTRAINT_TYPE = 'CHECK'
);
SET @vlc_EliminarCheckPartida = IF(
    @vln_ExisteCheckPartida > 0,
    'ALTER TABLE SIMT_PEDIDO_PROV DROP CHECK CK_PARTIDA_PEDIDO_PROV',
    'SELECT 1'
);
PREPARE stmt_EliminarCheckPartida FROM @vlc_EliminarCheckPartida;
EXECUTE stmt_EliminarCheckPartida;
DEALLOCATE PREPARE stmt_EliminarCheckPartida;

SET @vln_ExisteCheckPartida2 = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'SIMT_PEDIDO_PROV'
      AND CONSTRAINT_NAME = 'CK_PARTIDA_2_PEDIDO_PROV'
      AND CONSTRAINT_TYPE = 'CHECK'
);
SET @vlc_EliminarCheckPartida2 = IF(
    @vln_ExisteCheckPartida2 > 0,
    'ALTER TABLE SIMT_PEDIDO_PROV DROP CHECK CK_PARTIDA_2_PEDIDO_PROV',
    'SELECT 1'
);
PREPARE stmt_EliminarCheckPartida2 FROM @vlc_EliminarCheckPartida2;
EXECUTE stmt_EliminarCheckPartida2;
DEALLOCATE PREPARE stmt_EliminarCheckPartida2;

ALTER TABLE SIMT_PEDIDO_PROV
    ADD CONSTRAINT CK_PARTIDA_PEDIDO_PROV
        CHECK (PARTIDA IS NULL OR PARTIDA IN (1, 2, 5)),
    ADD CONSTRAINT CK_PARTIDA_2_PEDIDO_PROV
        CHECK (PARTIDA_2 IS NULL OR PARTIDA_2 IN (1, 2, 5));
