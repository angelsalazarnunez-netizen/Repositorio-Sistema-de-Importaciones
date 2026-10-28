import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { obtenerExpedientes } from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Lista de expedientes de desalmacenaje especial (HU-031). Permite buscar por
 *   referencia o dependencia y filtrar por estado, y abre el expediente para su seguimiento.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 01/10/2026
 *   Descripción: se quita el acceso a las solicitudes de importación, que pasa al menú de trámites.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): el botón de registrar desalmacenaje solo se
 *     muestra con DESALMACENAJE_CREAR.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla.
 */

const USUARIO_ACTUAL = "Analista de Importaciones";

const AYUDA = {
  titulo: "Ayuda: expedientes de desalmacenaje especial",
  introduccion:
    "Esta pantalla reúne los expedientes registrados. Desde aquí se abre cada uno para consultar "
    + "sus datos, su documentación y su historial, y para actualizar su estado.",
  pasos: [
    "Escriba parte de la referencia o de la dependencia para encontrar un expediente.",
    "Filtre por estado si busca los trámites que están en una etapa específica.",
    "Presione Ver expediente para abrir el detalle y darle seguimiento.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué no aparece un trámite?",
      respuesta:
        "Solo se listan los trámites de desalmacenaje especial, es decir los que tienen referencia "
        + "DE-. Las donaciones y las exoneraciones se consultan en sus propias pantallas.",
    },
    {
      pregunta: "¿Qué significa cada color?",
      respuesta:
        "El color acompaña al estado: amarillo para los trámites pendientes, azul para los que están "
        + "en proceso, verde para los finalizados, rojo para los retenidos y gris para los cancelados.",
    },
  ],
};

function claseEstado(estado) {
  if (estado === "Pendiente" || estado === "Pendiente de permisos especiales") {
    return "text-bg-warning";
  }
  if (estado === "Finalizada") {
    return "text-bg-success";
  }
  if (estado === "Cancelada") {
    return "text-bg-secondary";
  }
  if (estado === "Retenido en Almacén Fiscal") {
    return "text-bg-danger";
  }
  return "text-bg-info";
}

function formatearMonto(valor, moneda) {
  if (valor === null || valor === undefined) {
    return "—";
  }
  return `${moneda ?? ""} ${Number(valor).toLocaleString("es-CR", {
    minimumFractionDigits: 2, maximumFractionDigits: 2,
  })}`.trim();
}

function formatearFecha(valor) {
  return valor
    ? new Date(valor).toLocaleDateString("es-CR", { day: "2-digit", month: "short", year: "numeric" })
    : "—";
}

function Lst_SIM_Expedientes() {
  const [expedientes, setExpedientes] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [estado, setEstado] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    obtenerExpedientes()
      .then(setExpedientes)
      .catch((problema) => setError(`No fue posible cargar los expedientes. ${problema.message}`))
      .finally(() => setCargando(false));
  }, []);

  const estados = useMemo(
    () => [...new Set(expedientes.map((expediente) => expediente.estado))].sort(),
    [expedientes]
  );

  const filtrados = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    return expedientes.filter((expediente) => {
      const coincideTexto = !texto
        || expediente.referenciaInterna.toLowerCase().includes(texto)
        || (expediente.origenDependencia ?? "").toLowerCase().includes(texto);
      const coincideEstado = !estado || expediente.estado === estado;
      return coincideTexto && coincideEstado;
    });
  }, [expedientes, busqueda, estado]);

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Expedientes de desalmacenaje especial</h1>
          </div>
          <Cmp_SIM_ConPermiso permisos={[PERMISOS.DESALMACENAJE_CREAR]}>
            <Link className="btn btn-sim" to="/tramites/desalmacenaje">
              <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>
              Registrar desalmacenaje
            </Link>
          </Cmp_SIM_ConPermiso>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Consulte los desalmacenajes especiales registrados y abra su expediente para darle seguimiento. Para uno nuevo, use el botón «Registrar desalmacenaje».
        </p>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <div className="card sim-card shadow-sm">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span><i className="bi bi-folder2-open me-2" aria-hidden="true"></i>Expedientes</span>
          <span className="badge text-bg-light text-dark">{filtrados.length}</span>
        </div>

        <div className="card-body">
          <div className="row g-2 mb-3">
            <div className="col-12 col-md-6">
              <label className="form-label" htmlFor="txtBuscar">Buscar</label>
              <input type="search" className="form-control" id="txtBuscar"
                     placeholder="Referencia o dependencia"
                     value={busqueda} onChange={(evento) => setBusqueda(evento.target.value)} />
            </div>
            <div className="col-12 col-md-4">
              <label className="form-label" htmlFor="cmbEstado">Estado</label>
              <select className="form-select" id="cmbEstado" value={estado}
                      onChange={(evento) => setEstado(evento.target.value)}>
                <option value="">Todos los estados</option>
                {estados.map((nombre) => <option value={nombre} key={nombre}>{nombre}</option>)}
              </select>
            </div>
            <div className="col-12 col-md-2 d-flex align-items-end">
              <button type="button" className="btn btn-outline-secondary w-100" id="btnLimpiarFiltros"
                      onClick={() => { setBusqueda(""); setEstado(""); }}>
                Limpiar
              </button>
            </div>
          </div>

          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Expedientes de desalmacenaje especial</caption>
              <thead>
                <tr>
                  <th scope="col">Referencia</th>
                  <th scope="col">Dependencia</th>
                  <th scope="col">Valor estimado</th>
                  <th scope="col">Documentos</th>
                  <th scope="col">Registrado</th>
                  <th scope="col">Estado</th>
                  <th scope="col"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {cargando && (
                  <tr><td colSpan={7} className="text-muted small">Cargando expedientes...</td></tr>
                )}

                {!cargando && filtrados.length === 0 && (
                  <tr>
                    <td colSpan={7} className="text-muted small">
                      No hay expedientes que coincidan con la búsqueda.
                    </td>
                  </tr>
                )}

                {filtrados.map((expediente) => (
                  <tr key={expediente.referenciaInterna}>
                    <td className="sim-referencia">
                      <Link to={`/tramites/expedientes/${encodeURIComponent(expediente.referenciaInterna)}`}>
                        {expediente.referenciaInterna}
                      </Link>
                    </td>
                    <td>{expediente.origenDependencia ?? "—"}</td>
                    <td>{formatearMonto(expediente.valorEstimado, expediente.moneda)}</td>
                    <td className="text-center">{expediente.cantidadDocumentos}</td>
                    <td>{formatearFecha(expediente.fechaCreacion)}</td>
                    <td><span className={`badge ${claseEstado(expediente.estado)}`}>{expediente.estado}</span></td>
                    <td className="text-end">
                      <Link className="btn btn-sm btn-outline-secondary"
                            to={`/tramites/expedientes/${encodeURIComponent(expediente.referenciaInterna)}`}>
                        Ver expediente
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

export default Lst_SIM_Expedientes;
