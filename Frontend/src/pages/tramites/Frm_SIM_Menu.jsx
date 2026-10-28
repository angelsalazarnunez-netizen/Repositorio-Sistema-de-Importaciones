import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import { useSesionActual } from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { refrescarSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { agruparOpcionesDisponibles, MODULO_GENERAL } from "../../utilerias/OpcionesSistema.js";

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 01/10/2026
 * Descripción: Menú de trámites que se muestra después del inicio de sesión. Presenta una tarjeta
 *   por cada tipo de trámite disponible y lleva a su pantalla de consulta.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se agrega la tarjeta de donaciones (HU-024 y HU-044).
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la tarjeta de trámites temporales pasa a ser la de importaciones temporales
 *     (HU-025), desde la que se llega también a las alertas de vencimiento (HU-027). La
 *     exportación temporal tendrá su propia tarjeta en su historia de usuario.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agrega la tarjeta de exportaciones temporales (HU-026).
 *
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 05/10/2026
 *   Descripción: se agrega la tarjeta del reporte de consultas internas (HU-036).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega la tarjeta de aprobaciones de exoneración (HU-022), visible solo para
 *     quien tiene el permiso de aprobar.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega la tarjeta del panel de trámites activos (HU-030), primera del menú
 *     porque es la vista operativa del día.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: se agregan las tarjetas de gestión de roles y de asignación de roles (HU-003).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: "Importaciones" pasa a llamarse "Pedidos de importación" (comentario de la Unidad).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003).
 *     - Las tarjetas salen del catálogo utilerias/OpcionesSistema.js y se agrupan por módulo de
 *       acceso; solo se muestran las que habilitan los permisos de la sesión.
 *     - Al abrir el menú se consultan los permisos vigentes (GET /sesion), así un cambio de roles
 *       se refleja sin volver a iniciar sesión. También se puede actualizar con un botón.
 *     - Muestra los roles de la persona, un aviso cuando se intentó abrir una pantalla sin permiso
 *       y un mensaje cuando no tiene ninguna opción habilitada.
 *     - La ayuda explica que las opciones dependen de los roles y menciona los roles actuales.
 */

/**
 * Arma la ayuda del menú con los roles y los módulos de la sesión.
 *
 * @param {string[]} roles roles activos de la persona
 * @param {{modulo: {nombre: string}}[]} grupos módulos con opciones habilitadas
 * @returns {object} propiedades de Cmp_SIM_VentanaAyuda
 */
function crearAyuda(roles, grupos) {
  const textoRoles = roles.length > 0
    ? ` Sus roles actuales son: ${roles.join(", ")}.`
    : " Por ahora no tiene roles activos.";
  const modulos = grupos
    .filter((grupo) => grupo.modulo.codigo !== MODULO_GENERAL.codigo)
    .map((grupo) => grupo.modulo.nombre);
  const textoModulos = modulos.length > 0
    ? ` Con ellos puede trabajar en: ${modulos.join(", ")}.`
    : "";

  return {
    titulo: "Ayuda: Menú principal",
    tituloPasos: "Cómo usar el menú",
    introduccion:
      "Esta pantalla reúne las funcionalidades del sistema que usted puede usar. Las opciones "
      + "dependen de los roles que le asignó la jefatura de la Unidad: cada rol habilita uno o varios "
      + "permisos, y usted dispone de la combinación de los permisos de todos sus roles."
      + textoRoles
      + textoModulos,
    pasos: [
      "Ubique la sección del módulo con el que desea trabajar, por ejemplo «Gestión de donaciones».",
      "Presione el botón de la tarjeta para abrir su pantalla.",
      "En «Trámites activos» y «Buscar trámites» verá solamente los tipos de trámite de los módulos que sus roles le permiten consultar.",
      "Para volver a este menú desde cualquier pantalla, presione el nombre del sistema en la barra superior.",
    ],
    dudas: [
      {
        pregunta: "¿Por qué no aparece una opción o un tipo de trámite?",
        respuesta:
          "Solo se muestran las opciones que habilitan sus roles. Si necesita otra, solicite a la "
          + "jefatura de la Unidad que le asigne el rol correspondiente.",
      },
      {
        pregunta: "Me asignaron o retiraron un rol, ¿qué debo hacer?",
        respuesta:
          "Nada: el menú consulta sus permisos cada vez que lo abre. Si ya lo tenía abierto, presione "
          + "«Actualizar opciones». No es necesario cerrar la sesión.",
      },
      {
        pregunta: "¿Por qué puedo consultar un trámite pero no registrarlo o modificarlo?",
        respuesta:
          "Un rol puede permitir solo consultar, o también registrar y modificar. Si intenta una "
          + "acción que sus roles no permiten, el sistema le indica que no cuenta con autorización.",
      },
      {
        pregunta: "¿Qué significa el aviso «No cuenta con autorización»?",
        respuesta:
          "Que la pantalla u operación que intentó usar requiere un permiso que sus roles no "
          + "incluyen. Solicite el rol a la jefatura si lo necesita para su trabajo.",
      },
    ],
  };
}

function Frm_SIM_Menu() {
  const { sesion, permisos, roles } = useSesionActual();
  const ubicacion = useLocation();
  const navegar = useNavigate();
  const [accesoDenegado] = useState(() => Boolean(ubicacion.state?.accesoDenegado));
  const [avisoAccesoVisible, setAvisoAccesoVisible] = useState(accesoDenegado);
  const [actualizando, setActualizando] = useState(false);
  const [errorActualizacion, setErrorActualizacion] = useState("");

  const nombreUsuario = sesion?.nombreCompleto || "Usuario de la sesión";
  const grupos = useMemo(() => agruparOpcionesDisponibles(permisos), [permisos]);
  const ayuda = useMemo(() => crearAyuda(roles, grupos), [roles, grupos]);

  async function actualizarPermisos() {
    setActualizando(true);
    setErrorActualizacion("");
    try {
      await refrescarSesion();
    } catch (problema) {
      setErrorActualizacion(
        `No fue posible actualizar sus opciones; se muestran las del inicio de sesión. ${problema.message}`);
    } finally {
      setActualizando(false);
    }
  }

  useEffect(() => {
    // El aviso de acceso denegado se muestra una sola vez: se quita del historial.
    if (accesoDenegado) {
      navegar(ubicacion.pathname, { replace: true, state: null });
    }
    actualizarPermisos();
    // Solo al abrir el menú.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Cmp_SIM_Marco usuario={nombreUsuario} ayuda={ayuda}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap align-items-start gap-2">
          <div className="flex-grow-1">
            <h1 className="h4 mb-1">Menú principal</h1>
            <p className="small text-muted mb-2">
              Bienvenido(a), {nombreUsuario}. Seleccione la funcionalidad con la que desea trabajar.
            </p>
            {roles.length > 0 && (
              <div className="d-flex flex-wrap align-items-center gap-1 small" aria-label="Sus roles">
                <span className="text-muted me-1">Sus roles:</span>
                {roles.map((rol) => (
                  <span className="badge rounded-pill text-bg-light border" key={rol}>{rol}</span>
                ))}
              </div>
            )}
          </div>
          <button
            type="button"
            className="btn btn-sm btn-outline-secondary"
            onClick={actualizarPermisos}
            disabled={actualizando}
          >
            {actualizando
              ? <span className="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
              : <i className="bi bi-arrow-clockwise me-1" aria-hidden="true"></i>}
            {actualizando ? "Actualizando…" : "Actualizar opciones"}
          </button>
        </div>
      </section>

      {avisoAccesoVisible && (
        <div className="alert alert-warning d-flex align-items-start" role="alert">
          <i className="bi bi-shield-exclamation me-2 fs-5" aria-hidden="true"></i>
          <div className="flex-grow-1">
            No cuenta con autorización para abrir esa pantalla. Solicite a la jefatura de la Unidad el
            rol correspondiente si lo necesita.
          </div>
          <button type="button" className="btn-close ms-2" aria-label="Cerrar aviso"
                  onClick={() => setAvisoAccesoVisible(false)}></button>
        </div>
      )}

      {errorActualizacion && (
        <div className="alert alert-secondary small" role="status">{errorActualizacion}</div>
      )}

      {grupos.length === 0 && (
        <div className="card sim-card shadow-sm">
          <div className="card-body text-center py-5">
            <i className="bi bi-person-lock fs-1 text-muted d-block mb-2" aria-hidden="true"></i>
            <h2 className="h5">Aún no tiene opciones habilitadas</h2>
            <p className="text-muted mb-0">
              Sus roles no incluyen permisos para ninguna funcionalidad. Solicite a la jefatura de la
              Unidad que le asigne los roles que necesita y luego presione «Actualizar opciones».
            </p>
          </div>
        </div>
      )}

      {grupos.map(({ modulo, opciones }) => (
        <section className="mb-4" key={modulo.codigo} aria-labelledby={`modulo-${modulo.codigo}`}>
          <h2 className="h6 text-uppercase text-muted mb-2" id={`modulo-${modulo.codigo}`}>
            <i className={`bi ${modulo.icono} me-2`} aria-hidden="true"></i>
            {modulo.nombre}
          </h2>
          <div className="row g-3">
            {opciones.map((opcion) => (
              <div className="col-12 col-md-6 col-lg-3" key={opcion.ruta}>
                <div className="card sim-card shadow-sm h-100">
                  <div className="card-body d-flex flex-column">
                    <i className={`bi ${opcion.icono} fs-2 text-primary mb-2`} aria-hidden="true"></i>
                    <h3 className="h5">{opcion.titulo}</h3>
                    <p className="text-muted small flex-grow-1">{opcion.descripcion}</p>
                    <Link className="btn btn-sim align-self-start" to={opcion.ruta}>
                      {opcion.textoBoton ?? "Entrar"}
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      ))}
    </Cmp_SIM_Marco>
  );
}

export default Frm_SIM_Menu;
