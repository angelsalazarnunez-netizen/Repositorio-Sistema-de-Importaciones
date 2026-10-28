import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { obtenerExoneraciones } from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 25/10/2026
 * Descripción: Lista de expedientes de exoneración. Comentario de las pruebas de usabilidad: era la
 *   única funcionalidad que no entraba primero a una pantalla con todos sus trámites; el menú
 *   llevaba directo al registro. Como las listas de donaciones y de desalmacenajes, permite buscar
 *   por referencia o contratación, filtrar por estado y tipo, abrir el expediente y registrar una
 *   solicitud nueva.
 */

export const NOMBRES_TIPO_EXONERACION = {
  COMPRA_INSTITUCIONAL: "Compra institucional",
  DONACION: "Donación",
  CLIENTE_EXTERNO: "Cliente externo con contrato",
  VEHICULOS: "Vehículos",
  MOVIM_TRASPASO: "Movimiento de traspaso",
};

const AYUDA = {
  titulo: "Ayuda: expedientes de exoneración",
  introduccion:
    "Esta pantalla reúne las solicitudes de exoneración registradas. Desde aquí se abre cada "
    + "expediente para consultar sus datos, su documentación, sus observaciones y su bitácora.",
  pasos: [
    "Escriba parte de la referencia o del número de contratación para encontrar un expediente.",
    "Filtre por estado o por tipo de exoneración si busca un grupo específico.",
    "Presione Ver expediente para abrir el detalle y darle seguimiento.",
    "Para registrar una solicitud nueva, presione Registrar solicitud.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué no aparece un trámite?",
      respuesta:
        "Solo se listan las exoneraciones (referencia EXO-). Los demás tipos de trámite se "
        + "consultan en sus propias pantallas o en Buscar trámites.",
    },
    {
      pregunta: "¿Dónde apruebo una solicitud?",
      respuesta:
        "La jefatura decide las solicitudes desde «Aprobaciones de exoneración» en el menú principal.",
    },
  ],
};

export function claseEstadoExoneracion(estado) {
  const texto = (estado ?? "").toLowerCase();
  if (texto.includes("rechaz") || texto.includes("cancel")) {
    return "text-bg-secondary";
  }
  if (texto.includes("aprob") || texto.includes("final") || texto.includes("autoriz")) {
    return "text-bg-success";
  }
  if (texto.startsWith("pendiente")) {
    return "text-bg-warning";
  }
  return "text-bg-info";
}

function formatearFecha(valor) {
  return valor
    ? new Date(valor).toLocaleDateString("es-CR", { day: "2-digit", month: "short", year: "numeric" })
    : "—";
}

function Lst_SIM_Exoneraciones() {
  const [exoneraciones, setExoneraciones] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [estado, setEstado] = useState("");
  const [tipo, setTipo] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";

  useEffect(() => {
    obtenerExoneraciones()
      .then(setExoneraciones)
      .catch((problema) => setError(`No fue posible cargar las exoneraciones. ${problema.message}`))
      .finally(() => setCargando(false));
  }, []);

  const estados = useMemo(
    () => [...new Set(exoneraciones.map((exoneracion) => exoneracion.estado))].sort(),
    [exoneraciones]
  );

  const filtradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    return exoneraciones.filter((exoneracion) => {
      const coincideTexto = !texto
        || exoneracion.referenciaInterna.toLowerCase().includes(texto)
        || (exoneracion.numeroContratacion ?? "").toLowerCase().includes(texto);
      const coincideEstado = !estado || exoneracion.estado === estado;
      const coincideTipo = !tipo || exoneracion.tipoExoneracion === tipo;
      return coincideTexto && coincideEstado && coincideTipo;
    });
  }, [exoneraciones, busqueda, estado, tipo]);

  function limpiarFiltros() {
    setBusqueda("");
    setEstado("");
    setTipo("");
  }

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={AYUDA}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-1">Expedientes de exoneración</h1>
          <Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CREAR]}>
            <Link className="btn btn-sim" to="/tramites/exoneracion" id="btnRegistrarExoneracion">
              <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>
              Registrar solicitud
            </Link>
          </Cmp_SIM_ConPermiso>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Consulte las solicitudes de exoneración registradas y abra su expediente para revisar la
          documentación, las observaciones y la bitácora. Para una solicitud nueva, use el botón
          «Registrar solicitud».
        </p>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <div className="card sim-card shadow-sm">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span><i className="bi bi-folder2-open me-2" aria-hidden="true"></i>Buscar exoneraciones registradas</span>
          <span className="badge text-bg-light text-dark" aria-live="polite">{filtradas.length}</span>
        </div>

        <div className="card-body">
          <div className="row g-2 mb-3">
            <div className="col-12 col-md-5">
              <label className="form-label" htmlFor="txtBuscar">Buscar</label>
              <input type="search" className="form-control" id="txtBuscar"
                     placeholder="Referencia o número de contratación"
                     value={busqueda} onChange={(evento) => setBusqueda(evento.target.value)} />
            </div>
            <div className="col-12 col-md-3">
              <label className="form-label" htmlFor="cmbEstado">Estado</label>
              <select className="form-select" id="cmbEstado" value={estado}
                      onChange={(evento) => setEstado(evento.target.value)}>
                <option value="">Todos los estados</option>
                {estados.map((nombre) => <option value={nombre} key={nombre}>{nombre}</option>)}
              </select>
            </div>
            <div className="col-12 col-md-2">
              <label className="form-label" htmlFor="cmbTipoExoneracion">Tipo</label>
              <select className="form-select" id="cmbTipoExoneracion" value={tipo}
                      onChange={(evento) => setTipo(evento.target.value)}>
                <option value="">Todos</option>
                {Object.entries(NOMBRES_TIPO_EXONERACION).map(([codigo, nombre]) => (
                  <option value={codigo} key={codigo}>{nombre}</option>
                ))}
              </select>
            </div>
            <div className="col-12 col-md-2 d-flex align-items-end">
              <button type="button" className="btn btn-outline-secondary w-100" id="btnLimpiarFiltros"
                      onClick={limpiarFiltros}>
                Limpiar
              </button>
            </div>
          </div>

          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Expedientes de exoneración</caption>
              <thead>
                <tr>
                  <th scope="col">Referencia</th>
                  <th scope="col">Tipo</th>
                  <th scope="col">Contratación</th>
                  <th scope="col">Documentos</th>
                  <th scope="col">Responsable</th>
                  <th scope="col">Registrado</th>
                  <th scope="col">Estado</th>
                  <th scope="col"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {cargando && (
                  <tr><td colSpan={8} className="text-muted small">Cargando exoneraciones...</td></tr>
                )}

                {!cargando && !error && filtradas.length === 0 && (
                  <tr>
                    <td colSpan={8} className="text-muted small">
                      {exoneraciones.length === 0
                        ? "Todavía no hay solicitudes de exoneración registradas."
                        : "No hay exoneraciones que coincidan con la búsqueda."}
                    </td>
                  </tr>
                )}

                {filtradas.map((exoneracion) => {
                  const ruta = `/tramites/exoneraciones/${encodeURIComponent(exoneracion.referenciaInterna)}`;
                  return (
                    <tr key={exoneracion.referenciaInterna}>
                      <td className="sim-referencia"><Link to={ruta}>{exoneracion.referenciaInterna}</Link></td>
                      <td>{NOMBRES_TIPO_EXONERACION[exoneracion.tipoExoneracion] ?? exoneracion.tipoExoneracion}</td>
                      <td className="sim-referencia small">{exoneracion.numeroContratacion}</td>
                      <td className="text-center">{exoneracion.cantidadDocumentos}</td>
                      <td>{exoneracion.usuarioResponsable}</td>
                      <td>{formatearFecha(exoneracion.fechaCreacion)}</td>
                      <td>
                        <span className={`badge ${claseEstadoExoneracion(exoneracion.estado)}`}>
                          {exoneracion.estado}
                        </span>
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

export default Lst_SIM_Exoneraciones;
