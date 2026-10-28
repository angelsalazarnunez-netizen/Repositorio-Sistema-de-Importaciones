import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { obtenerPedidosImportacion } from "../../servicios/Srv_SIM_Tramites.js";

function formatearFecha(fecha) {
  return fecha
    ? new Date(`${fecha.slice(0, 10)}T00:00:00`).toLocaleDateString("es-CR", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    })
    : "—";
}

/**
 * Descripción: Lista de pedidos de importación (HU-006).
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): el botón de registrar pedido solo se muestra con
 *     IMPORTACION_CREAR, y el enlace de cada pedido dice «Ver» si no se tiene IMPORTACION_MODIFICAR.
 */
function Lst_SIM_Importaciones() {
  const [solicitudes, setSolicitudes] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [estado, setEstado] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    obtenerPedidosImportacion()
      .then(setSolicitudes)
      .catch((problema) => setError(`No fue posible cargar los pedidos. ${problema.message}`))
      .finally(() => setCargando(false));
  }, []);

  const estados = useMemo(
    () => [...new Set(solicitudes.map((solicitud) => solicitud.estado))].sort(),
    [solicitudes]
  );
  const filtradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    return solicitudes.filter((solicitud) => {
      const coincideTexto = !texto || [
        solicitud.referenciaInterna,
        solicitud.unidadSolicitante,
        solicitud.companiaAdjudicada,
        solicitud.numeroContratacion,
      ].some((valor) => (valor ?? "").toLowerCase().includes(texto));
      return coincideTexto && (!estado || solicitud.estado === estado);
    });
  }, [solicitudes, busqueda, estado]);

  return (
    <Cmp_SIM_Marco usuario={obtenerSesion()?.nombreCompleto || "Usuario de la sesión"}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Pedidos de importación</h1>
            <p className="small text-muted mb-0">
              Busque por número, unidad, compañía o contratación; filtre también por estado.
            </p>
          </div>
          <Cmp_SIM_ConPermiso permisos={[PERMISOS.IMPORTACION_CREAR]}>
            <Link className="btn btn-sim" to="/tramites/importaciones/nueva">
              <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>
              Registrar pedido
            </Link>
          </Cmp_SIM_ConPermiso>
        </div>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <div className="card sim-card shadow-sm">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-folder2-open me-2" aria-hidden="true"></i>Expedientes</span>
          <span className="badge text-bg-light text-dark">{filtradas.length}</span>
        </div>
        <div className="card-body">
          <div className="row g-3 mb-3">
            <div className="col-12 col-md-7">
              <label className="form-label" htmlFor="buscarImportacion">Buscar</label>
              <input
                className="form-control"
                id="buscarImportacion"
                type="search"
                value={busqueda}
                placeholder="Número, unidad, compañía o contratación"
                onChange={(evento) => setBusqueda(evento.target.value)}
              />
            </div>
            <div className="col-12 col-md-5">
              <label className="form-label" htmlFor="estadoImportacion">Estado</label>
              <select
                className="form-select"
                id="estadoImportacion"
                value={estado}
                onChange={(evento) => setEstado(evento.target.value)}
              >
                <option value="">Todos los estados</option>
                {estados.map((valor) => <option value={valor} key={valor}>{valor}</option>)}
              </select>
            </div>
          </div>
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Pedidos de importación registrados</caption>
              <thead>
                <tr>
                  <th scope="col">Referencia</th>
                  <th scope="col">Unidad solicitante</th>
                  <th scope="col">Compañía adjudicada</th>
                  <th scope="col">Contratación</th>
                  <th scope="col">Recibido</th>
                  <th scope="col">Estado</th>
                  <th scope="col"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {cargando && <tr><td colSpan={7} className="text-muted">Cargando pedidos...</td></tr>}
                {!cargando && filtradas.length === 0 && (
                  <tr><td colSpan={7} className="text-muted">No hay pedidos que coincidan.</td></tr>
                )}
                {filtradas.map((solicitud) => (
                  <tr key={solicitud.referenciaInterna}>
                    <td className="sim-referencia">{solicitud.referenciaInterna}</td>
                    <td>{solicitud.unidadSolicitante}</td>
                    <td>{solicitud.companiaAdjudicada}</td>
                    <td>{solicitud.numeroContratacion}</td>
                    <td>{formatearFecha(solicitud.fechaRecibidoExpediente)}</td>
                    <td><span className="badge text-bg-info">{solicitud.estado}</span></td>
                    <td className="text-end">
                      <Link
                        className="btn btn-sm btn-outline-secondary"
                        to={`/tramites/importaciones/${encodeURIComponent(solicitud.referenciaInterna)}`}
                      >
                        <Cmp_SIM_ConPermiso permisos={[PERMISOS.IMPORTACION_MODIFICAR]} alterno="Ver">
                          Ver / actualizar
                        </Cmp_SIM_ConPermiso>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </Cmp_SIM_Marco>
  );
}

export default Lst_SIM_Importaciones;
