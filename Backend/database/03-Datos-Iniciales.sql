-- Autor: Katherine Susana Valverde Fallas
-- Fecha: 10/09/2026
-- Descripción: Datos iniciales de los catálogos del módulo de trámites.
-- Caso de uso asociado: HU-031 Registrar trámite de desalmacenamiento especial
-- Motor: MySQL 8+
--
-- Historial de cambios:
--   Responsable: Josue Guevara Lostalo. Fecha: 10/09/2026.
--   Descripción: se catalogan el estado inicial y el tipo de documento de la
--     HU-024 Registrar donaciones, y se corrige el indicador de orden de compra
--     del tipo de trámite DONACION.
--   Responsable: Katherine Susana Valverde Fallas. Fecha: 15/09/2026.
--   Descripción: se catalogan el tipo de trámite, el estado inicial, los tipos de
--     documento y las acciones de bitácora de la HU-018 Registrar solicitud de exoneración.
--   Responsable: Katherine Susana Valverde Fallas. Fecha: 16/09/2026.
--   Descripción: se catalogan los ocho estados del desalmacenaje especial que
--     define la HU-032 Gestionar desalmacenaje especial, en lugar de los tres
--     provisionales (Pendiente, En revisión y Desalmacenado), y la acción de
--     bitácora CAMBIO_ESTADO para auditar sus cambios de estado.
--   Responsable: Josue Guevara Lostalo. Fecha: 16/09/2026.
--   Descripción: se declara la categoría funcional de cada estado (pendiente de ingreso,
--     trámite activo o trámite finalizado) para la HU-034 Buscar y filtrar datos.
--
--   Responsable: Josue Guevara Lostalo. Fecha: 15/09/2026.
--   Descripción: se catalogan los estados de seguimiento de la HU-044 Gestionar
--     donaciones, que quedaron pendientes cuando se registró la HU-024.
--     Cada uno declara su CATEGORIA_ESTADO (ACT o FIN) para la búsqueda de la HU-034.
--
--   Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
--   Descripción: los requisitos documentales de la exoneración usan TIPO_EXONERACION =
--     'GENERAL' en lugar de NULL, porque la columna no admite nulos (HU-021).
--
-- Requisito previo: ejecutar 01-Tablas.sql y 02-Tablas-Tramites.sql.
--
-- Cómo cargarlo (PowerShell o MySQL Workbench):
--   mysql -u root -p < database/03-Datos-Iniciales.sql

-- Declara la codificacion de la conexion: sin esto el cliente mysql de Windows
-- interpreta el archivo con la codificacion de la consola y corrompe las tildes.
SET NAMES utf8mb4;

USE sim;

-- ---------------------------------------------------------------------------
-- Monedas en las que la Unidad de Importaciones declara el valor de la mercancía.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_MONEDA (CODIGO_ISO, NOMBRE, SIMBOLO) VALUES
    ('CRC', 'Colón costarricense', '₡'),
    ('USD', 'Dólar estadounidense', '$'),
    ('EUR', 'Euro', '€');

-- ---------------------------------------------------------------------------
-- Países de origen ofrecidos por el formulario de la HU-031.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_PAIS (CODIGO_ISO, NOMBRE) VALUES
    ('DEU', 'Alemania'),
    ('BRA', 'Brasil'),
    ('CAN', 'Canadá'),
    ('CHL', 'Chile'),
    ('CHN', 'China'),
    ('COL', 'Colombia'),
    ('CRI', 'Costa Rica'),
    ('ESP', 'España'),
    ('USA', 'Estados Unidos'),
    ('FRA', 'Francia'),
    ('ITA', 'Italia'),
    ('JPN', 'Japón'),
    ('MEX', 'México'),
    ('NLD', 'Países Bajos'),
    ('PAN', 'Panamá'),
    ('GBR', 'Reino Unido');

-- ---------------------------------------------------------------------------
-- Tipos de trámite.
-- Cada tipo posee un prefijo de referencia único. Ese prefijo es el que permite
-- distinguir la referencia interna de un desalmacenaje especial de la que se
-- utiliza en donaciones, compras de la Fundación y pedidos a proveedor.
-- El desalmacenaje especial (HU-031) y la donación (HU-024) son los tipos que no
-- exigen contrato SICOP ni orden de compra, según sus reglas de negocio.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_TIPO_TRAMITE (CODIGO, NOMBRE, PREFIJO_REFERENCIA, REQUIERE_SICOP, REQUIERE_ORDEN_COMPRA) VALUES
    ('DESALM_ESPECIAL', 'Desalmacenaje especial', 'DE-', 0, 0),
    ('DONACION', 'Donación', 'D-', 0, 0),
    ('COMPRA_FUND', 'Compra de la Fundación', 'CF-', 1, 1),
    ('PEDIDO_PROV', 'Pedido a proveedor', 'PP-', 1, 1),
    ('EXONERACION', 'Solicitud de exoneración', 'EXO-', 1, 0);

-- ---------------------------------------------------------------------------
-- Estados del desalmacenaje especial (HU-031 y HU-032).
-- El expediente nace en Pendiente, marcado con ES_INICIAL = 1. Finalizada y
-- Cancelada son los estados de cierre del trámite (ES_FINAL = 1); el expediente
-- cerrado sigue disponible para consulta y reportes.
-- CATEGORIA_ESTADO (HU-034): PEN = pendiente de ingreso, ACT = trámite activo,
-- FIN = trámite finalizado.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente', 1, 0, 'PEN', 1 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Documentación en revisión', 0, 0, 'ACT', 2 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente de permisos especiales', 0, 0, 'ACT', 3 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'En trámite', 0, 0, 'ACT', 4 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'En aduana', 0, 0, 'ACT', 5 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Retenido en Almacén Fiscal', 0, 0, 'ACT', 6 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Finalizada', 0, 1, 'FIN', 7 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Cancelada', 0, 1, 'FIN', 8 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DESALM_ESPECIAL';

-- ---------------------------------------------------------------------------
-- Estados de la donación.
-- El expediente nace en Pendiente, marcado con ES_INICIAL = 1 por la HU-024.
-- Los estados restantes son los del seguimiento de la HU-044: la donación puede
-- requerir exoneración, permisos regulatorios o desalmacenaje según su tipo, por
-- lo que el expediente recorre solo los estados que su trámite necesite.
-- Finalizada y Cancelada llevan ES_FINAL = 1: cierran el expediente, que conserva
-- su información y su historial pero ya no admite cambios.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente', 1, 0, 'PEN', 1 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Documentación en revisión', 0, 0, 'ACT', 2 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente de exoneración', 0, 0, 'ACT', 3 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente de permisos', 0, 0, 'ACT', 4 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Exoneración aprobada', 0, 0, 'ACT', 5 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'En aduana', 0, 0, 'ACT', 6 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Desalmacenaje', 0, 0, 'ACT', 7 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Finalizada', 0, 1, 'FIN', 8 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Cancelada', 0, 1, 'FIN', 9 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'DONACION';

-- ---------------------------------------------------------------------------
-- Tipos de documento admitidos por el expediente.
-- RESPALDO cubre la documentación atípica: actas de salida históricas,
-- justificaciones y oficios no estandarizados.
-- RESOLUCION_ACTA es el documento obligatorio que acepta la donación (HU-024).
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_TIPO_DOCUMENTO (CODIGO, NOMBRE) VALUES
    ('RESPALDO', 'Justificación, acta de salida histórica o documento atípico'),
    ('PACKING_LIST', 'Lista de empaque'),
    ('FACTURA', 'Factura o documento equivalente'),
    ('EMBARQUE', 'Documento de embarque'),
    ('RESOLUCION_ACTA', 'Resolución o acta de aceptación de la donación');

-- ---------------------------------------------------------------------------
-- Solicitud de exoneración (HU-018).
-- La HU-018 define como estado inicial "Pendiente revisión documental". Los estados
-- posteriores se catalogarán cuando su flujo esté especificado.
-- La factura comercial, la lista de empaque y el documento de embarque reutilizan
-- los tipos FACTURA, PACKING_LIST y EMBARQUE ya catalogados.
-- ---------------------------------------------------------------------------

INSERT INTO SIMM_ESTADO_TRAMITE (ID_TIPO_TRAMITE, NOMBRE, ES_INICIAL, ES_FINAL, CATEGORIA_ESTADO, ORDEN)
SELECT ID_TIPO_TRAMITE, 'Pendiente revisión documental', 1, 0, 'PEN', 1 FROM SIMM_TIPO_TRAMITE WHERE CODIGO = 'EXONERACION';

INSERT INTO SIMM_TIPO_DOCUMENTO (CODIGO, NOMBRE) VALUES
    ('FORMULARIO_S1', 'Formulario S1'),
    ('EXONET', 'EXONET'),
    ('PERMISO_REGULATORIO', 'Permiso regulatorio'),
    ('TRADUCCION_FACTURA', 'Traducción de factura');

-- CAMBIO_ESTADO lo usan el seguimiento del desalmacenaje (HU-032) y el de la donación
-- (HU-044): mover el estado del expediente también queda en la bitácora.
INSERT INTO SIMC_ACCION_BITACORA (ACCION, DESCRIPCION) VALUES
    ('REGISTRO', 'Registro de la solicitud'),
    ('DOCUMENTO_AGREGADO', 'Documentación agregada al expediente'),
    ('COMENTARIO', 'Comentario sobre la solicitud'),
    ('COMENTARIO_ELIMINADO', 'Eliminación de un comentario registrado por error'),
    ('CAMBIO_ESTADO', 'Cambio de estado del expediente'),
    ('DOCUMENTO_ELIMINADO', 'Documento eliminado del expediente'),
    ('DATOS_MODIFICADOS', 'Datos del trámite corregidos'),
    ('REFERENCIA_MODIFICADA', 'Cambio de referencia del expediente'),
    ('DOCUMENTO_CLASIFICADO', 'Clasificación de un documento del expediente'),
    ('DOCUMENTO_REVISADO', 'Revisión del estado de un documento del expediente'),
    ('ESTADO_DOCUMENTAL', 'Estado documental del expediente'),
    ('COSTOS_MODIFICADOS', 'Datos de costo del expediente');

-- ---------------------------------------------------------------------------
-- Requisitos documentales generales de la solicitud de exoneración.
-- TIPO_EXONERACION = 'GENERAL' indica que aplica a todas las modalidades.
-- La obligatoriedad específica por modalidad se completará cuando la
-- Unidad de Importaciones confirme la matriz documental.
-- ---------------------------------------------------------------------------

INSERT INTO SIMT_REQUISITO_DOCUMENTAL (ID_TIPO_TRAMITE,ID_TIPO_DOCUMENTO,TIPO_EXONERACION,OBLIGATORIO)
SELECT TT.ID_TIPO_TRAMITE, TD.ID_TIPO_DOCUMENTO, 'GENERAL', 1
FROM SIMM_TIPO_TRAMITE TT JOIN SIMM_TIPO_DOCUMENTO TD ON TD.CODIGO = 'FACTURA'
WHERE TT.CODIGO = 'EXONERACION';

INSERT INTO SIMT_REQUISITO_DOCUMENTAL (ID_TIPO_TRAMITE, ID_TIPO_DOCUMENTO, TIPO_EXONERACION,OBLIGATORIO)
SELECT TT.ID_TIPO_TRAMITE, TD.ID_TIPO_DOCUMENTO, 'GENERAL', 1
FROM SIMM_TIPO_TRAMITE TT JOIN SIMM_TIPO_DOCUMENTO TD ON TD.CODIGO = 'PACKING_LIST'
WHERE TT.CODIGO = 'EXONERACION';

INSERT INTO SIMT_REQUISITO_DOCUMENTAL (ID_TIPO_TRAMITE,ID_TIPO_DOCUMENTO,TIPO_EXONERACION,OBLIGATORIO)
SELECT TT.ID_TIPO_TRAMITE, TD.ID_TIPO_DOCUMENTO, 'GENERAL', 1
FROM SIMM_TIPO_TRAMITE TT JOIN SIMM_TIPO_DOCUMENTO TD ON TD.CODIGO = 'EMBARQUE'
WHERE TT.CODIGO = 'EXONERACION';
