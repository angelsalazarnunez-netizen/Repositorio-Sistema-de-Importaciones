/**
 * Descripción: Inicio y cierre de sesión, y datos de la sesión guardados en el navegador.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003).
 *     - tieneAlgunPermiso: indica si la sesión tiene al menos uno de varios permisos.
 *     - refrescarSesion: consulta GET /api/autenticacion/sesion y actualiza los roles y permisos
 *       guardados, para que el menú refleje un cambio de roles sin volver a iniciar sesión.
 *     - suscribirCambiosSesion: avisa a las pantallas cuando cambian los datos de la sesión.
 *     - notificarAccesoDenegado: los servicios lo llaman al recibir un 403, porque puede deberse a
 *       que a la persona le retiraron un rol; se refrescan los permisos para ocultar lo que ya no
 *       puede usar.
 */

const CLAVE_SESION = "sim.sesion";
const RUTA_LOGIN = "/api/autenticacion/login";
const RUTA_SESION = "/api/autenticacion/sesion";
const EVENTO_SESION = "sim:sesion-actualizada";
// Tiempo mínimo entre dos refrescos provocados por respuestas 403.
const ESPERA_REFRESCO_POR_403_MS = 5000;

let refrescoEnCurso = null;
let ultimoRefrescoPor403 = 0;

async function leerRespuesta(respuesta) {
  const cuerpo = await respuesta.json().catch(() => ({}));
  if (!respuesta.ok) {
    throw new Error(cuerpo.mensaje || `El servidor respondió con el código ${respuesta.status}.`);
  }
  return cuerpo;
}

function avisarCambioSesion() {
  window.dispatchEvent(new Event(EVENTO_SESION));
}

export async function iniciarSesion(credenciales) {
  const respuesta = await fetch(RUTA_LOGIN, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credenciales),
  });
  return leerRespuesta(respuesta);
}

export function guardarSesion(sesion) {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
  avisarCambioSesion();
}

export function obtenerSesion() {
  const contenido = sessionStorage.getItem(CLAVE_SESION);
  if (!contenido) {
    return null;
  }
  try {
    const sesion = JSON.parse(contenido);
    return sesion && typeof sesion.token === "string" && sesion.token
      ? sesion
      : null;
  } catch (error) {
    sessionStorage.removeItem(CLAVE_SESION);
    return null;
  }
}

/**
 * Indica si la sesión actual tiene un permiso. Solo sirve para mostrar u ocultar opciones: el
 * servidor vuelve a validar el permiso en cada operación.
 *
 * @param {string} codigo código del permiso, por ejemplo EXONERACION_APROBAR
 * @returns {boolean} verdadero si la sesión tiene el permiso
 */
export function tienePermiso(codigo) {
  const permisos = obtenerSesion()?.permisos;
  return Array.isArray(permisos) && permisos.includes(codigo);
}

/**
 * Indica si la sesión actual tiene al menos uno de los permisos indicados. Una lista vacía o sin
 * indicar no exige ningún permiso (basta con tener sesión).
 *
 * @param {string[]} [codigos] códigos de permiso (ver utilerias/Permisos.js)
 * @returns {boolean} verdadero si tiene alguno, o si no se exige ninguno
 */
export function tieneAlgunPermiso(codigos) {
  if (!Array.isArray(codigos) || codigos.length === 0) {
    return Boolean(obtenerSesion());
  }
  const permisos = obtenerSesion()?.permisos;
  return Array.isArray(permisos) && codigos.some((codigo) => permisos.includes(codigo));
}

export function obtenerToken() {
  return obtenerSesion()?.token ?? null;
}

export function limpiarSesion() {
  sessionStorage.removeItem(CLAVE_SESION);
  avisarCambioSesion();
}

/**
 * Suscribe una función a los cambios de la sesión (inicio, refresco de permisos o cierre).
 *
 * @param {() => void} alCambiar función que se llama en cada cambio
 * @returns {() => void} función para cancelar la suscripción
 */
export function suscribirCambiosSesion(alCambiar) {
  window.addEventListener(EVENTO_SESION, alCambiar);
  return () => window.removeEventListener(EVENTO_SESION, alCambiar);
}

/**
 * Consulta los roles y permisos vigentes en el servidor y los guarda en la sesión, conservando el
 * token. Si ya hay un refresco en curso, devuelve ese mismo. Si la sesión ya no es válida, la
 * limpia y regresa al inicio de sesión.
 *
 * @returns {Promise<object|null>} la sesión actualizada, o null si no hay sesión
 */
export function refrescarSesion() {
  const token = obtenerToken();
  if (!token) {
    return Promise.resolve(null);
  }
  if (refrescoEnCurso) {
    return refrescoEnCurso;
  }

  refrescoEnCurso = (async () => {
    try {
      const respuesta = await fetch(RUTA_SESION, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (respuesta.status === 401) {
        limpiarSesion();
        if (window.location.pathname !== "/login") {
          window.location.assign("/login");
        }
        return null;
      }
      const vigente = await leerRespuesta(respuesta);
      const actual = obtenerSesion();
      if (!actual || actual.token !== token) {
        // La sesión cambió mientras se consultaba (por ejemplo, se cerró): no se sobrescribe.
        return actual;
      }
      const actualizada = {
        ...actual,
        nombreCompleto: vigente.nombreCompleto ?? actual.nombreCompleto,
        correoInstitucional: vigente.correoInstitucional ?? actual.correoInstitucional,
        roles: Array.isArray(vigente.roles) ? vigente.roles : [],
        permisos: Array.isArray(vigente.permisos) ? vigente.permisos : [],
      };
      guardarSesion(actualizada);
      return actualizada;
    } finally {
      refrescoEnCurso = null;
    }
  })();
  return refrescoEnCurso;
}

/**
 * Los servicios lo llaman al recibir un 403: refresca los permisos en segundo plano, como máximo
 * una vez cada pocos segundos. Los errores del refresco se ignoran; el 403 ya se informó.
 */
export function notificarAccesoDenegado() {
  const ahora = Date.now();
  if (ahora - ultimoRefrescoPor403 < ESPERA_REFRESCO_POR_403_MS) {
    return;
  }
  ultimoRefrescoPor403 = ahora;
  refrescarSesion().catch(() => {});
}

export async function cerrarSesion() {
  const token = obtenerToken();
  if (!token) {
    limpiarSesion();
    return;
  }

  const respuesta = await fetch("/api/autenticacion/logout", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (respuesta.status === 401) {
    limpiarSesion();
    return;
  }
  if (!respuesta.ok) {
    throw new Error("No fue posible cerrar la sesión en el servidor.");
  }
  limpiarSesion();
}
