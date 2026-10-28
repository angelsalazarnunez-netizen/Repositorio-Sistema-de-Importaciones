import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { Cmp_SIM_BotonAyuda, Cmp_SIM_VentanaAyuda } from "./Cmp_SIM_Ayuda.jsx";
import { cerrarSesion as cerrarSesionApi } from "../servicios/Srv_SIM_Autenticacion.js";
import { obtenerNotificacionesPedidoImportacion } from "../servicios/Srv_SIM_Tramites.js";
import { useSesionActual } from "./seguridad/Cmp_SIM_ConPermiso.jsx";
import { ACCESOS } from "../utilerias/Permisos.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Marco común de las pantallas de trámites: barra de navegación institucional con
 *   el usuario de la sesión, el panel de notificaciones y el botón de ayuda, el contenido de la
 *   pantalla y el pie de página.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 30/09/2026
 *   Descripción: se agrega el enlace a la búsqueda de trámites (HU-034).
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 01/10/2026
 *   Descripción: el nombre del sistema lleva al menú de trámites.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). El botón de búsqueda solo se muestra a quien puede
 *     consultar trámites, y las notificaciones de pagos (y su consulta periódica) solo a quien
 *     gestiona pedidos de importación. La barra se actualiza si cambian los permisos de la sesión.
 */
function Cmp_SIM_Marco({ usuario, ayuda, children }) {
  const [ayudaVisible, setAyudaVisible] = useState(false);
  const [errorSesion, setErrorSesion] = useState("");
  const [notificaciones, setNotificaciones] = useState([]);
  const [errorNotificaciones, setErrorNotificaciones] = useState("");
  const [panelNotificacionesVisible, setPanelNotificacionesVisible] = useState(false);
  const navegar = useNavigate();
  const { sesion, tieneAlguno } = useSesionActual();
  const verBusqueda = tieneAlguno(ACCESOS.CONSULTA_TRAMITES);
  const verNotificaciones = tieneAlguno(ACCESOS.IMPORTACION_NOTIFICACIONES);

  useEffect(() => {
    if (!verNotificaciones) {
      setNotificaciones([]);
      setErrorNotificaciones("");
      setPanelNotificacionesVisible(false);
      return undefined;
    }
    let vigente = true;
    async function cargarNotificaciones() {
      try {
        const resultado = await obtenerNotificacionesPedidoImportacion();
        if (vigente) {
          setNotificaciones(resultado);
          setErrorNotificaciones("");
        }
      } catch (problema) {
        if (vigente) {
          setErrorNotificaciones(problema.message);
        }
      }
    }

    cargarNotificaciones();
    const intervalo = window.setInterval(cargarNotificaciones, 300000);
    return () => {
      vigente = false;
      window.clearInterval(intervalo);
    };
  }, [verNotificaciones]);

  async function salir() {
    setErrorSesion("");
    try {
      await cerrarSesionApi();
      navegar("/login", { replace: true });
    } catch (problema) {
      setErrorSesion(problema.message);
    }
  }

  return (
    <>
      <nav className="navbar navbar-expand-lg sim-navbar shadow-sm">
        <div className="container d-flex flex-wrap gap-2">
          <Link className="navbar-brand fw-semibold d-flex align-items-center" to="/menu">
            <img src="/img/logo-ucr.png" alt="Universidad de Costa Rica" className="sim-logo me-2" />
            SIM · Sistema de Importaciones
          </Link>

          <div className="d-flex align-items-center gap-3 ms-auto">
            {verBusqueda && (
              <Link className="btn btn-sm btn-outline-light" to="/tramites/busqueda">
                <i className="bi bi-search me-1" aria-hidden="true"></i>
                Buscar trámites
              </Link>
            )}
            {verNotificaciones && (
              <button
                className="btn btn-sm btn-outline-light position-relative"
                type="button"
                aria-expanded={panelNotificacionesVisible}
                aria-controls="panel-notificaciones-importacion"
                aria-label={`Notificaciones de importación: ${notificaciones.length}`}
                onClick={() => setPanelNotificacionesVisible((visible) => !visible)}
              >
                <i className="bi bi-bell" aria-hidden="true"></i>
                {notificaciones.length > 0 && (
                  <span className="position-absolute top-0 start-100 translate-middle badge rounded-pill text-bg-danger">
                    {notificaciones.length}
                  </span>
                )}
              </button>
            )}
            {ayuda && <Cmp_SIM_BotonAyuda alPresionar={() => setAyudaVisible(true)} />}
            <span className="navbar-text small">
              <i className="bi bi-person-circle me-1" aria-hidden="true"></i>
              {sesion?.nombreCompleto || usuario}
            </span>
            <button className="btn btn-sm btn-outline-light" onClick={salir} type="button">
              Cerrar sesión
            </button>
          </div>
        </div>
      </nav>

      {verNotificaciones && panelNotificacionesVisible && (
        <section className="container mt-3" id="panel-notificaciones-importacion"
                 aria-label="Notificaciones de importación">
          <div className="card sim-card shadow-sm">
            <div className="card-header">
              <i className="bi bi-bell me-2" aria-hidden="true"></i>
              Notificaciones de importación
            </div>
            <div className="card-body">
              {errorNotificaciones && (
                <div className="alert alert-warning mb-0" role="status">
                  No fue posible cargar las notificaciones. {errorNotificaciones}
                </div>
              )}
              {!errorNotificaciones && notificaciones.length === 0 && (
                <p className="text-muted mb-0">No hay pagos pendientes de revisión.</p>
              )}
              {!errorNotificaciones && notificaciones.length > 0 && (
                <ul className="list-group list-group-flush">
                  {notificaciones.map((notificacion) => (
                    <li className="list-group-item px-0" key={notificacion.referenciaInterna}>
                      <strong>{notificacion.referenciaInterna}</strong>
                      <span className="ms-2">{notificacion.mensaje}</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </section>
      )}

      {errorSesion && (
        <div className="container mt-2">
          <div className="alert alert-danger mb-0" role="alert">{errorSesion}</div>
        </div>
      )}

      <main className="container my-4">{children}</main>

      <footer className="sim-pie text-center text-muted small py-4">
        Universidad de Costa Rica · Oficina de Suministros · Unidad de Importaciones
      </footer>

      {ayuda && ayudaVisible && (
        <Cmp_SIM_VentanaAyuda {...ayuda} alCerrar={() => setAyudaVisible(false)} />
      )}
    </>
  );
}

export default Cmp_SIM_Marco;
