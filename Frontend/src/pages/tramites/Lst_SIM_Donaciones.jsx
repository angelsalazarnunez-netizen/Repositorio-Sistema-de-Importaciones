import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { obtenerDonaciones } from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Angel Salazar Nunez
 * Fecha: 16/09/2026
 * Descripción: Lista de expedientes de donación (HU-044). Permite buscar por referencia o
 *   donante y filtrar por estado y tipo de donación, y abre el expediente para su seguimiento.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se integra a main: la lista se separa del expediente, usa el marco común y
 *     consulta el servidor con el token de la sesión.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: FUNDEVI pasa a llamarse Fundación UCR y la ayuda usa las siglas D y DF.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: el tipo se muestra y se filtra como Donación (D) o Fundación UCR (DF): la Unidad
 *     no distingue donaciones nacionales e internacionales.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): el botón de registrar donación solo se muestra con DONACION_CREAR.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla; PU-09: la tarjeta de la lista se titula "Buscar
 *     donaciones registradas".
 */

const NOMBRES_TIPO_DONACION = {
  // La Unidad no distingue donaciones nacionales e internacionales: ambas son Donación (D).
  NACIONAL: "Donación (D)",
  INTERNACIONAL: "Donación (D)",
  FUNDACION_UCR: "Fundación UCR (DF)",
};

/** Opciones del filtro por tipo, según las siglas de la Unidad. */
const FILTROS_TIPO_DONACION = [
  { valor: "D", nombre: "Donación (D)" },
  { valor: "DF", nombre: "Fundación UCR (DF)" },
];

function coincideTipoDonacion(tipoDonacion, filtro) {
  if (!filtro) {
    return true;
  }
  const esFundacion = tipoDonacion === "FUNDACION_UCR";
  return filtro === "DF" ? esFundacion : !esFundacion;
}

const AYUDA = {
  titulo: "Ayuda: expedientes de donación",
  introduccion:
    "Esta pantalla reúne las donaciones registradas. Desde aquí se abre cada expediente para "
    + "consultar sus datos, su documentación y su historial, y para actualizar su estado.",
  pasos: [
    "Escriba parte de la referencia o del nombre del donante para encontrar un expediente.",
    "Filtre por estado o por tipo de donación si busca un grupo específico.",
    "Presione Ver expediente para abrir el detalle y darle seguimiento.",
    "Para registrar una donación nueva, presione Registrar donación.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué no aparece un trámite?",
      respuesta:
        "Solo se listan las donaciones, es decir los trámites con referencia D- o, si son por medio "
        + "de la Fundación UCR, DF-. Los demás tipos "
        + "de trámite se consultan en sus propias pantallas o en Buscar trámites.",
    },
    {
      pregunta: "¿Qué significa cada color?",
      respuesta:
        "El color acompaña al estado: amarillo para los trámites pendientes, azul para los que están "
        + "en proceso, verde para los finalizados y gris para los cancelados.",
    },
  ],
};

export function claseEstadoDonacion(estado) {
  if (estado?.startsWith("Pendiente")) {
    return "text-bg-warning";
  }
  if (estado === "Finalizada") {
    return "text-bg-success";
  }
  if (estado === "Cancelada") {
    return "text-bg-secondary";
  }
  return "text-bg-info";
}

export function nombreTipoDonacion(tipo) {
  return NOMBRES_TIPO_DONACION[tipo] ?? tipo ?? "—";
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

function Lst_SIM_Donaciones() {
  const [donaciones, setDonaciones] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [estado, setEstado] = useState("");
  const [tipoDonacion, setTipoDonacion] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";

  useEffect(() => {
    obtenerDonaciones()
      .then(setDonaciones)
      .catch((problema) => setError(`No fue posible cargar las donaciones. ${problema.message}`))
      .finally(() => setCargando(false));
  }, []);

  const estados = useMemo(
    () => [...new Set(donaciones.map((donacion) => donacion.estado))].sort(),
    [donaciones]
  );

  const filtradas = useMemo(() => {
    const texto = busqueda.trim().toLowerCase();
    return donaciones.filter((donacion) => {
      const coincideTexto = !texto
        || donacion.referenciaInterna.toLowerCase().includes(texto)
        || (donacion.nombreDonante ?? "").toLowerCase().includes(texto);
      const coincideEstado = !estado || donacion.estado === estado;
      const coincideTipo = coincideTipoDonacion(donacion.tipoDonacion, tipoDonacion);
      return coincideTexto && coincideEstado && coincideTipo;
    });
  }, [donaciones, busqueda, estado, tipoDonacion]);

  function limpiarFiltros() {
    setBusqueda("");
    setEstado("");
    setTipoDonacion("");
  }

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={AYUDA}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Expedientes de donación</h1>
          </div>
          <Cmp_SIM_ConPermiso permisos={[PERMISOS.DONACION_CREAR]}>
            <Link className="btn btn-sim" to="/tramites/donaciones/nueva">
              <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>
              Registrar donación
            </Link>
          </Cmp_SIM_ConPermiso>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Consulte las donaciones registradas y abra su expediente para darle seguimiento. Para una donación nueva, use el botón «Registrar donación».
        </p>
      </section>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <div className="card sim-card shadow-sm">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          {/* PU-09: el título deja claro que aquí se busca; registrar es el botón del encabezado. */}
          <span><i className="bi bi-search me-2" aria-hidden="true"></i>Buscar donaciones registradas</span>
          <span className="badge text-bg-light text-dark">{filtradas.length}</span>
        </div>

        <div className="card-body">
          <div className="row g-2 mb-3">
            <div className="col-12 col-md-5">
              <label className="form-label" htmlFor="txtBuscar">Buscar</label>
              <input type="search" className="form-control" id="txtBuscar"
                     placeholder="Referencia o donante"
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
              <label className="form-label" htmlFor="cmbTipoDonacion">Tipo</label>
              <select className="form-select" id="cmbTipoDonacion" value={tipoDonacion}
                      onChange={(evento) => setTipoDonacion(evento.target.value)}>
                <option value="">Todos</option>
                {FILTROS_TIPO_DONACION.map((opcion) => (
                  <option value={opcion.valor} key={opcion.valor}>{opcion.nombre}</option>
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
              <caption className="visually-hidden">Expedientes de donación</caption>
              <thead>
                <tr>
                  <th scope="col">Referencia</th>
                  <th scope="col">Tipo</th>
                  <th scope="col">Donante</th>
                  <th scope="col">Valor estimado</th>
                  <th scope="col">Documentos</th>
                  <th scope="col">Responsable</th>
                  <th scope="col">Registrado</th>
                  <th scope="col">Estado</th>
                  <th scope="col"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {cargando && (
                  <tr><td colSpan={9} className="text-muted small">Cargando donaciones...</td></tr>
                )}

                {!cargando && filtradas.length === 0 && (
                  <tr>
                    <td colSpan={9} className="text-muted small">
                      No hay donaciones que coincidan con la búsqueda.
                    </td>
                  </tr>
                )}

                {filtradas.map((donacion) => (
                  <tr key={donacion.referenciaInterna}>
                    <td className="sim-referencia">
                      <Link to={`/tramites/donaciones/${encodeURIComponent(donacion.referenciaInterna)}`}>
                        {donacion.referenciaInterna}
                      </Link>
                    </td>
                    <td>{nombreTipoDonacion(donacion.tipoDonacion)}</td>
                    <td>{donacion.nombreDonante}</td>
                    <td>{formatearMonto(donacion.valorEstimado, donacion.moneda)}</td>
                    <td className="text-center">{donacion.cantidadDocumentos}</td>
                    <td>{donacion.usuarioResponsable}</td>
                    <td>{formatearFecha(donacion.fechaCreacion)}</td>
                    <td><span className={`badge ${claseEstadoDonacion(donacion.estado)}`}>{donacion.estado}</span></td>
                    <td className="text-end">
                      <Link className="btn btn-sm btn-outline-secondary"
                            to={`/tramites/donaciones/${encodeURIComponent(donacion.referenciaInterna)}`}>
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

export default Lst_SIM_Donaciones;
