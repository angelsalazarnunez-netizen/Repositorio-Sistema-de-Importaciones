import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import Cmp_SIM_CuentaRegresiva, { formatearFechaLocal } from "../../componentes/Cmp_SIM_CuentaRegresiva.jsx";
import {
  TIPOS_TRAMITE_TEMPORAL,
  claseEstadoTemporal,
  crearAyudaLista,
  formatearMonto,
} from "../../componentes/Cmp_SIM_TramitesTemporales.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { obtenerTramitesTemporales } from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Vista de trámites activos de importación temporal (HU-025). Muestra por defecto los
 *   expedientes que siguen en gestión o por llegar, ordenados por la cercanía de su vencimiento,
 *   con su cuenta regresiva. Permite consultar también los cerrados y abrir cada expediente.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la lista atiende también la exportación temporal (HU-026), que muestra además el
 *     país de destino. El tipo llega por la ruta; antes se llamaba Lst_SIM_ImportacionesTemporales.
 *
 * @param {object} props
 * @param {string} props.tipo importacion o exportacion
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): el botón de registrar solo se muestra con
 *     ADUANERO_TEMPORAL_CREAR.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla.
 */

const ALCANCES = [
  { valor: "ACTIVOS", nombre: "Trámites activos" },
  { valor: "CERRADOS", nombre: "Cerrados" },
  { valor: "TODOS", nombre: "Todos" },
];

function Lst_SIM_TramitesTemporales({ tipo }) {
  const configuracion = TIPOS_TRAMITE_TEMPORAL[tipo];
  const [alcance, setAlcance] = useState("ACTIVOS");
  const [expedientes, setExpedientes] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";
  const titulo = configuracion.titulo;
  const columnas = configuracion.esExportacion ? 9 : 8;

  useEffect(() => {
    let vigente = true;
    setCargando(true);
    setError("");
    obtenerTramitesTemporales(configuracion.servicio, alcance)
      .then((lista) => vigente && setExpedientes(lista))
      .catch((problema) => vigente
        && setError(`No fue posible cargar las ${titulo.toLowerCase()}. ${problema.message}`))
      .finally(() => vigente && setCargando(false));
    return () => { vigente = false; };
  }, [alcance, configuracion.servicio, titulo]);

  const filtrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    if (!texto) {
      return expedientes;
    }
    return expedientes.filter((expediente) => [
      expediente.referenciaInterna, expediente.unidadSolicitante, expediente.descripcionMercancia,
      expediente.paisDestino,
    ].some((valor) => (valor ?? "").toLowerCase().includes(texto)));
  }, [expedientes, busqueda]);

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={crearAyudaLista(configuracion)}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">{titulo}</h1>
          <div className="d-flex flex-wrap gap-2">
            <Link className="btn btn-outline-secondary" to="/tramites/temporales/alertas">
              <i className="bi bi-bell me-1" aria-hidden="true"></i>
              Alertas de vencimiento
            </Link>
            <Cmp_SIM_ConPermiso permisos={[PERMISOS.ADUANERO_TEMPORAL_CREAR]}>
              <Link className="btn btn-sim" to={`${configuracion.ruta}/nueva`}>
                <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>
                Registrar {configuracion.singular}
              </Link>
            </Cmp_SIM_ConPermiso>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          {configuracion.esExportacion
            ? "Consulte las exportaciones temporales registradas, su estado y los días que faltan para su vencimiento."
            : "Consulte las importaciones temporales registradas, su estado y los días que faltan para su vencimiento."}
        </p>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <div className="card sim-card shadow-sm">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span>
            <i className={`bi ${configuracion.icono} me-2`} aria-hidden="true"></i>
            {ALCANCES.find((opcion) => opcion.valor === alcance)?.nombre}
          </span>
          <span className="badge text-bg-light text-dark">{filtrados.length}</span>
        </div>

        <div className="card-body">
          <div className="row g-2 mb-3">
            <div className="col-12 col-md-6">
              <label className="form-label" htmlFor="txtBuscar">Buscar</label>
              <input type="search" className="form-control" id="txtBuscar"
                     placeholder={configuracion.esExportacion
                       ? "Referencia, unidad, mercancía o país"
                       : "Referencia, unidad o mercancía"}
                     value={busqueda} onChange={(evento) => setBusqueda(evento.target.value)} />
            </div>
            <div className="col-12 col-md-6">
              <span className="form-label d-block">Mostrar</span>
              <div className="btn-group w-100" role="group" aria-label="Expedientes que se muestran">
                {ALCANCES.map((opcion) => (
                  <button type="button" key={opcion.valor} id={`btnAlcance${opcion.valor}`}
                          className={`btn ${alcance === opcion.valor ? "btn-sim" : "btn-outline-secondary"}`}
                          aria-pressed={alcance === opcion.valor}
                          onClick={() => setAlcance(opcion.valor)}>
                    {opcion.nombre}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">{titulo}</caption>
              <thead>
                <tr>
                  <th scope="col">Referencia</th>
                  <th scope="col">Unidad solicitante</th>
                  <th scope="col">Mercancía</th>
                  {configuracion.esExportacion && <th scope="col">País de destino</th>}
                  <th scope="col">Valor declarado</th>
                  <th scope="col">Recepción</th>
                  <th scope="col">Estado</th>
                  <th scope="col">Vencimiento</th>
                  <th scope="col"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {cargando && (
                  <tr><td colSpan={columnas} className="text-muted small">Cargando {titulo.toLowerCase()}...</td></tr>
                )}

                {!cargando && filtrados.length === 0 && (
                  <tr>
                    <td colSpan={columnas} className="text-muted small">
                      No hay {titulo.toLowerCase()} que coincidan con la búsqueda.
                    </td>
                  </tr>
                )}

                {!cargando && filtrados.map((expediente) => {
                  const ruta = `${configuracion.ruta}/${encodeURIComponent(expediente.referenciaInterna)}`;
                  return (
                    <tr key={expediente.referenciaInterna}>
                      <td className="sim-referencia text-nowrap"><Link to={ruta}>{expediente.referenciaInterna}</Link></td>
                      <td>{expediente.unidadSolicitante}</td>
                      <td>{expediente.descripcionMercancia}</td>
                      {configuracion.esExportacion && <td>{expediente.paisDestino ?? "—"}</td>}
                      <td className="text-nowrap">{formatearMonto(expediente.valorEquipo, expediente.moneda)}</td>
                      <td className="text-nowrap">{formatearFechaLocal(expediente.fechaRecepcion)}</td>
                      <td>
                        <span className={`badge ${claseEstadoTemporal(expediente.estado)}`}>{expediente.estado}</span>
                      </td>
                      <td>
                        <Cmp_SIM_CuentaRegresiva vencimiento={expediente.vencimiento} />
                        {expediente.vencimiento?.fechaVencimiento && (
                          <span className="d-block small text-muted">
                            {formatearFechaLocal(expediente.vencimiento.fechaVencimiento)}
                            {expediente.vencimiento.prorrogado && " (prorrogado)"}
                          </span>
                        )}
                      </td>
                      <td className="text-end">
                        <Link className="btn btn-sm btn-outline-secondary" to={ruta}>Ver expediente</Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </Cmp_SIM_Marco>
  );
}

export default Lst_SIM_TramitesTemporales;
