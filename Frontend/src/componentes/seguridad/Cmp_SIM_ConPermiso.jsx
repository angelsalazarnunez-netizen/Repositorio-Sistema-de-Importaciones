import { useMemo, useSyncExternalStore } from "react";

import {
  obtenerSesion,
  suscribirCambiosSesion,
} from "../../servicios/Srv_SIM_Autenticacion.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Apoyo de las restricciones por rol en las pantallas (HU-003).
 *   - useSesionActual: devuelve la sesión y se vuelve a dibujar cuando cambia (inicio, refresco de
 *     permisos o cierre).
 *   - Cmp_SIM_ConPermiso: muestra su contenido solo si la sesión tiene al menos uno de los
 *     permisos indicados; si no, muestra el contenido alterno (por defecto, nada).
 *       <Cmp_SIM_ConPermiso permisos={[PERMISOS.DONACION_CREAR]}>
 *         <Link to="/tramites/donaciones/nueva">Nueva donación</Link>
 *       </Cmp_SIM_ConPermiso>
 *   - Cmp_SIM_AvisoSoloConsulta: aviso que se muestra en un expediente cuando la persona puede
 *     consultarlo pero sus roles no le permiten modificarlo.
 *   Ocultar una opción es una ayuda para la persona usuaria; el servidor valida cada operación.
 */

// La instantánea es el texto guardado: así React compara un valor simple entre dibujos.
function leerInstantanea() {
  return sessionStorage.getItem("sim.sesion") ?? "";
}

/**
 * Devuelve la sesión actual y dibuja de nuevo el componente cuando cambia.
 *
 * @returns {{sesion: object|null, permisos: string[], roles: string[],
 *            tieneAlguno: (codigos?: string[]) => boolean}}
 */
export function useSesionActual() {
  const instantanea = useSyncExternalStore(suscribirCambiosSesion, leerInstantanea);

  return useMemo(() => {
    const sesion = instantanea ? obtenerSesion() : null;
    const permisos = Array.isArray(sesion?.permisos) ? sesion.permisos : [];
    const roles = Array.isArray(sesion?.roles) ? sesion.roles : [];
    const conjunto = new Set(permisos);
    return {
      sesion,
      permisos,
      roles,
      tieneAlguno: (codigos) =>
        !Array.isArray(codigos) || codigos.length === 0
          ? Boolean(sesion)
          : codigos.some((codigo) => conjunto.has(codigo)),
    };
  }, [instantanea]);
}

/** Muestra su contenido solo si la sesión tiene alguno de los permisos indicados. */
function Cmp_SIM_ConPermiso({ permisos, alterno = null, children }) {
  const { tieneAlguno } = useSesionActual();
  return tieneAlguno(permisos) ? children : alterno;
}

/** Aviso de expediente en modo de consulta: la persona puede verlo, pero no modificarlo. */
export function Cmp_SIM_AvisoSoloConsulta({ texto }) {
  return (
    <div className="alert alert-info d-flex align-items-start py-2 small" role="status">
      <i className="bi bi-eye me-2 fs-6" aria-hidden="true"></i>
      <span>
        {texto ?? "Usted puede consultar este expediente, pero sus roles no le permiten modificarlo. "
          + "Si lo necesita para su trabajo, solicite el rol correspondiente a la jefatura de la Unidad."}
      </span>
    </div>
  );
}

export default Cmp_SIM_ConPermiso;
