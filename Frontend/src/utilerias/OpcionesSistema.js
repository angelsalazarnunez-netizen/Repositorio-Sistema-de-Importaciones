import { ACCESOS, MODULOS, PERMISOS } from "./Permisos.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Catálogo de las opciones del menú principal (HU-003). Cada opción indica el módulo
 *   de acceso al que pertenece y los permisos que la habilitan: se muestra si la sesión tiene al
 *   menos uno. Las rutas de App.jsx exigen los mismos permisos, y el servidor los vuelve a validar.
 *
 *   Para agregar una funcionalidad nueva al menú basta con agregar su opción aquí (y su ruta
 *   protegida en App.jsx). Si es de un módulo nuevo, el módulo se agrega en Permisos.js.
 *
 *   El módulo GENERAL agrupa las vistas que reúnen trámites de varios módulos (panel de trámites
 *   activos y búsqueda); muestran solo los tipos de trámite que la persona puede consultar.
 *
 * Historial de cambios:*
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: la opción Exoneraciones entra a la lista de expedientes, como las demás
 *     funcionalidades.
 */

export const MODULO_GENERAL = Object.freeze({
  codigo: "GENERAL",
  nombre: "Vista general",
  icono: "bi-grid",
});

export const OPCIONES_SISTEMA = Object.freeze([
  // Vista general.
  {
    modulo: MODULO_GENERAL.codigo,
    titulo: "Trámites activos",
    descripcion: "Vista operativa del día: trámites en proceso, pendientes o por llegar de los tipos que usted gestiona.",
    icono: "bi-speedometer2",
    ruta: "/tramites/activos",
    permisos: ACCESOS.CONSULTA_TRAMITES,
  },
  {
    modulo: MODULO_GENERAL.codigo,
    titulo: "Buscar trámites",
    descripcion: "Buscar y filtrar los trámites de los tipos que usted gestiona y exportar el listado.",
    icono: "bi-search",
    ruta: "/tramites/busqueda",
    permisos: ACCESOS.CONSULTA_TRAMITES,
  },

  // Gestión de importaciones.
  {
    modulo: "IMPORTACIONES",
    titulo: "Pedidos de importación",
    descripcion: "Registrar los pedidos de importación a proveedores y consultar su avance.",
    icono: "bi-box-seam",
    ruta: "/tramites/importaciones",
    permisos: [PERMISOS.IMPORTACION_CONSULTAR],
  },

  // Gestión de exoneraciones.
  {
    modulo: "EXONERACIONES",
    titulo: "Exoneraciones",
    descripcion: "Consultar las solicitudes de exoneración, dar seguimiento a sus expedientes y registrar solicitudes nuevas.",
    icono: "bi-file-earmark-check",
    // Como las demás funcionalidades, entra primero a la lista de sus trámites (pruebas de usabilidad).
    ruta: "/tramites/exoneraciones",
    permisos: [PERMISOS.EXONERACION_CONSULTAR],
  },

  // Gestión aduanera.
  {
    modulo: "ADUANERA",
    titulo: "Importaciones temporales",
    descripcion: "Registrar la entrada temporal de equipos, seguir su vencimiento y atender sus alertas.",
    icono: "bi-box-arrow-in-down",
    ruta: "/tramites/importaciones-temporales",
    permisos: [PERMISOS.ADUANERO_TEMPORAL_CONSULTAR],
  },
  {
    modulo: "ADUANERA",
    titulo: "Exportaciones temporales",
    descripcion: "Registrar la salida temporal de equipos al extranjero, seguir su vencimiento y su reimportación.",
    icono: "bi-box-arrow-up",
    ruta: "/tramites/exportaciones-temporales",
    permisos: [PERMISOS.ADUANERO_TEMPORAL_CONSULTAR],
  },

  // Gestión de donaciones.
  {
    modulo: "DONACIONES",
    titulo: "Donaciones",
    descripcion: "Registrar donaciones y dar seguimiento a sus expedientes.",
    icono: "bi-gift",
    ruta: "/tramites/donaciones",
    permisos: [PERMISOS.DONACION_CONSULTAR],
  },

  // Gestión de desalmacenajes especiales.
  {
    modulo: "DESALMACENAJES",
    titulo: "Desalmacenaje especial",
    descripcion: "Registrar desalmacenajes especiales y dar seguimiento a sus expedientes.",
    icono: "bi-truck",
    ruta: "/tramites/expedientes",
    permisos: [PERMISOS.DESALMACENAJE_CONSULTAR],
  },

  // Reportes y consultas.
  {
    modulo: "REPORTES",
    titulo: "Reportes de consultas internas",
    descripcion: "Resumir totales y conteos de los trámites registrados y descargar el reporte.",
    icono: "bi-clipboard-data",
    ruta: "/reportes/consultas-internas",
    permisos: [PERMISOS.REPORTE_CONSULTAR],
  },

  // Aprobaciones.
  {
    modulo: "APROBACIONES",
    titulo: "Aprobaciones de exoneración",
    descripcion: "Aprobar, rechazar o pedir modificaciones a las solicitudes de exoneración.",
    icono: "bi-patch-check",
    ruta: "/tramites/exoneraciones/aprobaciones",
    permisos: [PERMISOS.EXONERACION_APROBAR],
  },

  // Administración de usuarios.
  {
    modulo: "ADMIN_USUARIOS",
    titulo: "Gestionar roles",
    descripcion: "Registrar roles del sistema, definir sus permisos y activarlos o inactivarlos.",
    icono: "bi-shield-lock",
    ruta: "/roles",
    permisos: ACCESOS.GESTION_ROLES,
  },
  {
    modulo: "ADMIN_USUARIOS",
    titulo: "Asignar roles",
    descripcion: "Buscar una cuenta por correo institucional y asignarle o retirarle roles.",
    icono: "bi-person-gear",
    ruta: "/asignar-roles",
    permisos: [PERMISOS.ROL_ASIGNAR],
  },
]);

/**
 * Agrupa las opciones que habilitan los permisos dados, por módulo y en el orden de la HU-003.
 * Los módulos sin opciones habilitadas no se incluyen.
 *
 * @param {string[]} permisos permisos de la sesión
 * @returns {{modulo: {codigo: string, nombre: string, icono: string}, opciones: object[]}[]}
 */
export function agruparOpcionesDisponibles(permisos) {
  const conjunto = new Set(Array.isArray(permisos) ? permisos : []);
  const disponibles = OPCIONES_SISTEMA.filter((opcion) =>
    opcion.permisos.some((permiso) => conjunto.has(permiso)));

  return [MODULO_GENERAL, ...MODULOS]
    .map((modulo) => ({
      modulo,
      opciones: disponibles.filter((opcion) => opcion.modulo === modulo.codigo),
    }))
    .filter((grupo) => grupo.opciones.length > 0);
}
