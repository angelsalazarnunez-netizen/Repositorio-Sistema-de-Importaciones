/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Códigos de permiso y módulos de acceso del sistema (HU-003). Es la copia, para las
 *   pantallas, de Backend .../utilerias/Permisos.java y Enumerados.ModuloPermiso, y de la columna
 *   SIMM_PERMISO.MODULO (script 16-Modulos-Permisos-Roles-Base.sql). Al agregar un permiso o un
 *   módulo hay que agregarlo en los tres lugares.
 *
 *   Las pantallas usan los permisos solo para mostrar u ocultar opciones; el servidor vuelve a
 *   validar el permiso en cada operación.
 */

export const PERMISOS = Object.freeze({
  // Administración de usuarios.
  ROL_CONSULTAR: "ROL_CONSULTAR",
  ROL_CREAR: "ROL_CREAR",
  ROL_MODIFICAR: "ROL_MODIFICAR",
  ROL_ASIGNAR: "ROL_ASIGNAR",
  USUARIO_CONSULTAR: "USUARIO_CONSULTAR",
  USUARIO_MODIFICAR: "USUARIO_MODIFICAR",

  // Gestión de importaciones.
  IMPORTACION_CONSULTAR: "IMPORTACION_CONSULTAR",
  IMPORTACION_CREAR: "IMPORTACION_CREAR",
  IMPORTACION_MODIFICAR: "IMPORTACION_MODIFICAR",
  IMPORTACION_PAGO_GESTIONAR: "IMPORTACION_PAGO_GESTIONAR",
  IMPORTACION_DOCUMENTO_GENERAR: "IMPORTACION_DOCUMENTO_GENERAR",

  // Gestión de exoneraciones.
  EXONERACION_CONSULTAR: "EXONERACION_CONSULTAR",
  EXONERACION_CREAR: "EXONERACION_CREAR",
  EXONERACION_MODIFICAR: "EXONERACION_MODIFICAR",

  // Gestión de donaciones.
  DONACION_CONSULTAR: "DONACION_CONSULTAR",
  DONACION_CREAR: "DONACION_CREAR",
  DONACION_MODIFICAR: "DONACION_MODIFICAR",

  // Gestión aduanera.
  ADUANERO_TEMPORAL_CONSULTAR: "ADUANERO_TEMPORAL_CONSULTAR",
  ADUANERO_TEMPORAL_CREAR: "ADUANERO_TEMPORAL_CREAR",
  ADUANERO_TEMPORAL_MODIFICAR: "ADUANERO_TEMPORAL_MODIFICAR",
  ADUANERO_RECLAMO_GESTIONAR: "ADUANERO_RECLAMO_GESTIONAR",

  // Gestión de desalmacenajes especiales.
  DESALMACENAJE_CONSULTAR: "DESALMACENAJE_CONSULTAR",
  DESALMACENAJE_CREAR: "DESALMACENAJE_CREAR",
  DESALMACENAJE_MODIFICAR: "DESALMACENAJE_MODIFICAR",

  // Reportes y consultas.
  REPORTE_CONSULTAR: "REPORTE_CONSULTAR",
  REPORTE_FILTRAR: "REPORTE_FILTRAR",
  REPORTE_GENERAR: "REPORTE_GENERAR",
  REPORTE_EXPORTAR: "REPORTE_EXPORTAR",
  GRAFICO_GENERAR: "GRAFICO_GENERAR",
  BITACORA_CONSULTAR: "BITACORA_CONSULTAR",

  // Aprobaciones.
  EXONERACION_APROBAR: "EXONERACION_APROBAR",
  APROBACION_FONDOS: "APROBACION_FONDOS",
});

/** Módulos de acceso de la HU-003, en el orden en que se muestran. */
export const MODULOS = Object.freeze([
  { codigo: "IMPORTACIONES", nombre: "Gestión de importaciones", icono: "bi-box-seam" },
  { codigo: "EXONERACIONES", nombre: "Gestión de exoneraciones", icono: "bi-file-earmark-check" },
  { codigo: "ADUANERA", nombre: "Gestión aduanera", icono: "bi-globe-americas" },
  { codigo: "DONACIONES", nombre: "Gestión de donaciones", icono: "bi-gift" },
  { codigo: "DESALMACENAJES", nombre: "Gestión de desalmacenajes especiales", icono: "bi-truck" },
  { codigo: "REPORTES", nombre: "Reportes y consultas", icono: "bi-clipboard-data" },
  { codigo: "APROBACIONES", nombre: "Aprobaciones", icono: "bi-patch-check" },
  { codigo: "ADMIN_USUARIOS", nombre: "Administración de usuarios", icono: "bi-people" },
]);

/**
 * Grupos de permisos que se usan juntos. Una opción o ruta con un grupo se habilita si la sesión
 * tiene al menos uno de sus permisos. Coinciden con las reglas del servidor (ReglasAcceso.java).
 */
export const ACCESOS = Object.freeze({
  // Panel de trámites activos y búsqueda: cualquier permiso de consultar trámites, o reportes.
  // El servidor filtra los resultados a los tipos de trámite que la persona puede consultar.
  CONSULTA_TRAMITES: Object.freeze([
    PERMISOS.IMPORTACION_CONSULTAR,
    PERMISOS.EXONERACION_CONSULTAR,
    PERMISOS.ADUANERO_TEMPORAL_CONSULTAR,
    PERMISOS.DONACION_CONSULTAR,
    PERMISOS.DESALMACENAJE_CONSULTAR,
    PERMISOS.REPORTE_CONSULTAR,
  ]),
  // Expediente de exoneración y su decisión: quien la gestiona o quien la aprueba.
  EXONERACION_CONSULTA_O_APROBACION: Object.freeze([
    PERMISOS.EXONERACION_CONSULTAR,
    PERMISOS.EXONERACION_APROBAR,
  ]),
  // Notificaciones de pagos de pedidos de importación.
  IMPORTACION_NOTIFICACIONES: Object.freeze([
    PERMISOS.IMPORTACION_CONSULTAR,
    PERMISOS.IMPORTACION_PAGO_GESTIONAR,
  ]),
  // Pantalla de gestión de roles (lista de roles y catálogo de permisos).
  GESTION_ROLES: Object.freeze([PERMISOS.ROL_CONSULTAR, PERMISOS.ROL_MODIFICAR]),
});

/**
 * Permisos que permiten abrir el expediente de cada tipo de trámite (SIMM_TIPO_TRAMITE.CODIGO). Es
 * la copia de Enumerados.AccesoTipoTramite del servidor. Se usa en las vistas que reúnen varios
 * tipos (panel de trámites activos): quien ve todos los tipos por REPORTE_CONSULTAR solo puede
 * abrir los expedientes de los módulos que consulta.
 */
export const CONSULTA_POR_TIPO_TRAMITE = Object.freeze({
  PEDIDO_PROV: Object.freeze([PERMISOS.IMPORTACION_CONSULTAR]),
  EXONERACION: Object.freeze([PERMISOS.EXONERACION_CONSULTAR, PERMISOS.EXONERACION_APROBAR]),
  IMPORT_TEMPORAL: Object.freeze([PERMISOS.ADUANERO_TEMPORAL_CONSULTAR]),
  EXPORT_TEMPORAL: Object.freeze([PERMISOS.ADUANERO_TEMPORAL_CONSULTAR]),
  DONACION: Object.freeze([PERMISOS.DONACION_CONSULTAR]),
  DESALM_ESPECIAL: Object.freeze([PERMISOS.DESALMACENAJE_CONSULTAR]),
});
