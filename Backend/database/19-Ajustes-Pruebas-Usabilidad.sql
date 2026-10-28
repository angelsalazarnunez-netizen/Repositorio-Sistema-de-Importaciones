-- Autor: Equipo de Desarrollo SIM
-- Fecha: 25/10/2026
-- Descripción: ajustes que piden las pruebas de usabilidad con usuarios (PU-11). En la importación
--   temporal, los agentes de aduanas llaman "DUA de cancelación" al DUA con el que se cierra el
--   régimen (antes "DUA de reexportación"). Solo cambia el nombre visible: el código del catálogo
--   (DUA_REEXPORTACION) se conserva, así no cambian las reglas ni los documentos ya registrados.
-- Migración incremental. Motor: MySQL 8+.

SET NAMES utf8mb4;
USE sim;

UPDATE SIMM_TIPO_DOCUMENTO
SET NOMBRE = 'DUA de cancelación'
WHERE CODIGO = 'DUA_REEXPORTACION';
