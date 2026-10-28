import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useSesionActual } from "./Cmp_SIM_ConPermiso.jsx";

/**
 * Descripción: Protege un grupo de rutas.
 *   - Sin sesión, lleva al inicio de sesión.
 *   - Con la propiedad permisos, exige al menos uno de ellos; si la sesión no tiene ninguno, lleva
 *     al menú principal con un aviso (el menú solo muestra las opciones habilitadas).
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): se agrega la propiedad permisos. Se usa anidada
 *     en App.jsx, un grupo de rutas por cada permiso exigido.
 *
 * @param {{permisos?: string[]}} propiedades permisos que habilitan las rutas del grupo
 */
function Cmp_SIM_RutaProtegida({ permisos }) {
  const { sesion, tieneAlguno } = useSesionActual();
  const ubicacion = useLocation();

  if (!sesion?.token) {
    return <Navigate to="/login" replace />;
  }
  if (!tieneAlguno(permisos)) {
    return (
      <Navigate
        to="/menu"
        replace
        state={{ accesoDenegado: true, rutaSolicitada: ubicacion.pathname }}
      />
    );
  }
  return <Outlet />;
}

export default Cmp_SIM_RutaProtegida;
