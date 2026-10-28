/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 12/10/2026
 * Descripción: Llamadas al servicio REST del módulo de seguridad (HU-001 Registrar cuenta de
 *   usuario y HU-003 Administrar y asignar roles). Las rutas de roles, permisos y asignaciones
 *   requieren la sesión: se envía el token de Srv_SIM_Autenticacion y, si el servidor responde
 *   que la sesión no es válida, se limpia y se regresa al inicio de sesión. El registro y la
 *   validación de la cuenta son públicos y no envían token.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: ante un 403 se refrescan los permisos de la sesión (restricciones por rol, HU-003).
 */

import { limpiarSesion, notificarAccesoDenegado, obtenerToken } from "./Srv_SIM_Autenticacion.js";

export const RUTA_SEGURIDAD = "/api/v1/seguridad";
export const RUTA_REGISTRO = "/api/autenticacion/registro";
export const RUTA_VERIFICAR = "/api/autenticacion/verificar";

/**
 * Hace una petición a un servicio protegido del módulo de seguridad con el token de la sesión.
 *
 * @param {string} ruta ruta del servicio, por ejemplo `${RUTA_SEGURIDAD}/roles`
 * @param {RequestInit} opciones opciones de fetch
 * @returns {Promise<Response>} respuesta del servidor
 */
export async function solicitarSeguridad(ruta, opciones = {}) {
  const token = obtenerToken();
  const respuesta = await fetch(ruta, {
    ...opciones,
    headers: {
      ...(opciones.headers ?? {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (respuesta.status === 401) {
    limpiarSesion();
    window.location.assign("/login");
  }
  if (respuesta.status === 403) {
    // Puede deberse a que se retiró un rol: se refrescan los permisos para el menú (HU-003).
    notificarAccesoDenegado();
  }

  return respuesta;
}
