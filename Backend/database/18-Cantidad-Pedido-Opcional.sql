-- Autor: Equipo de Desarrollo SIM
-- Fecha: 24/10/2026
-- Descripción: la cantidad del pedido de importación pasa a ser opcional.
-- Migración incremental. Motor: MySQL 8+.

SET NAMES utf8mb4;
USE sim;

ALTER TABLE SIMT_PEDIDO_PROV
    MODIFY CANTIDAD_SOLICITADA DECIMAL(13,3) NULL;
