import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import { useSesionActual } from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { CONSULTA_POR_TIPO_TRAMITE } from "../../utilerias/Permisos.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { obtenerPanelTramites } from "../../servicios/Srv_SIM_Tramites.js";

import "./Lst_SIM_TramitesActivos.css";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Panel de trámites activos (HU-030). Vista operativa del día con los trámites de
 *   todos los tipos que están en proceso, pendientes de permisos, aprobaciones o exoneraciones, o
 *   por llegar. Se actualiza solo cada minuto: un trámite que se desalmacena o finaliza sale del
 *   panel sin que haya que hacer nada, y se sigue consultando en el histórico con su bitácora.
 *   Cada fila abre el expediente de su tipo, donde se gestiona el trámite.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). El servidor muestra solo los tipos de trámite que la
 *     persona consulta; con REPORTE_CONSULTAR ve todos, pero solo puede abrir el expediente de los
 *     tipos de sus módulos (los demás se muestran sin enlace).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla; H-04 (Sprint 2): la columna "Motivo" pasa a
 *     "Descripción del equipo".
 */

const INTERVALO_ACTUALIZACION_MS = 60000;

const ALCANCE_ACTIVOS = "ACTIVOS";
const ALCANCE_HISTORICO = "HISTORICO";

// Filtros rápidos de la vista activa. "PROXIMOS" no es un grupo: son los temporales por vencer.
// "EN_CURSO" (indicador "en proceso") reúne todo lo que ya no está por llegar.
const FILTROS = [
  { valor: "TODOS", texto: "Todos" },
  { valor: "POR_LLEGAR", texto: "Por llegar" },
  { valor: "PERMISOS", texto: "Pendiente de permisos" },
  { valor: "APROBACION", texto: "Pendiente de aprobación" },
  { valor: "EXONERACION", texto: "Pendiente de exoneración" },
  { valor: "PROXIMOS", texto: "Próximos a vencer" },
];

const CLASES_GRUPO = {
  POR_LLEGAR: "text-bg-primary",
  EN_PROCESO: "text-bg-success",
  PERMISOS: "text-bg-warning",
  APROBACION: "text-bg-warning",
  EXONERACION: "text-bg-warning",
  FUERA: "text-bg-secondary",
};

// Pantalla del expediente de cada tipo de trámite, donde se gestiona.
const RUTAS_EXPEDIENTE = {
  DESALM_ESPECIAL: "/tramites/expedientes",
  DONACION: "/tramites/donaciones",
  EXONERACION: "/tramites/exoneraciones",
  PEDIDO_PROV: "/tramites/importaciones",
  IMPORT_TEMPORAL: "/tramites/importaciones-temporales",
  EXPORT_TEMPORAL: "/tramites/exportaciones-temporales",
};

const AYUDA = {
  titulo: "Ayuda: trámites activos",
  introduccion:
    "Este panel muestra solo los trámites del día a día: en proceso, pendientes de permisos, "
    + "aprobaciones o exoneraciones, o por llegar. Los datos históricos no saturan la vista.",
  pasos: [
    "Revise los indicadores: cada uno funciona también como filtro.",
    "Use los filtros rápidos o escriba parte de la referencia o de la descripción del equipo para buscar.",
    "Los temporales vencidos o próximos a vencer aparecen primero, con sus días restantes.",
    "Presione la referencia para abrir el expediente y gestionar el trámite.",
    "Use la pestaña Histórico para consultar los trámites que ya salieron del panel.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué desapareció un trámite del panel?",
      respuesta:
        "Porque se desalmacenó, se finalizó o se canceló. No se elimina: aparece en el histórico y "
        + "conserva su bitácora en el expediente.",
    },
    {
      pregunta: "¿Cada cuánto se actualiza?",
      respuesta: "Cada minuto, mientras la pantalla está abierta. También puede presionar Actualizar.",
    },
    {
      pregunta: "¿Qué significa próximo a vencer?",
      respuesta:
        "Que al trámite temporal le quedan 45 días o menos para vencer, el mismo margen que usan las "
        + "alertas de vencimiento.",
    },
  ],
};

function formatearMonto(valor, moneda) {
  if (valor === null || valor === undefined) {
    return "—";
  }
  try {
    return new Intl.NumberFormat("es-CR", {
      style: "currency", currency: moneda || "CRC", minimumFractionDigits: 2,
    }).format(valor);
  } catch {
    return `${moneda ?? ""} ${Number(valor).toLocaleString("es-CR", { minimumFractionDigits: 2 })}`;
  }
}

function formatearHora(valor) {
  return valor ? new Date(valor).toLocaleTimeString("es-CR", { hour: "2-digit", minute: "2-digit" }) : "";
}

function esProximo(tramite) {
  return tramite.nivelVencimiento === "PROXIMO" || tramite.nivelVencimiento === "VENCIDO";
}

function textoDias(tramite) {
  if (tramite.diasRestantes === null || tramite.diasRestantes === undefined) {
    return tramite.nivelVencimiento === "SIN_FECHA" ? "Sin fecha" : "—";
  }
  return tramite.diasRestantes < 0 ? `Venció hace ${Math.abs(tramite.diasRestantes)}` : tramite.diasRestantes;
}

function claseDias(tramite) {
  switch (tramite.nivelVencimiento) {
    case "VENCIDO": return "sim-panel-dias-vencido";
    case "PROXIMO": return "sim-panel-dias-proximo";
    default: return "text-muted";
  }
}

function Lst_SIM_TramitesActivos() {
  const [alcance, setAlcance] = useState(ALCANCE_ACTIVOS);
  const [panel, setPanel] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [filtro, setFiltro] = useState("TODOS");
  const [texto, setTexto] = useState("");
  const [tipo, setTipo] = useState("");

  const usuario = obtenerSesion()?.nombreCompleto || "Agente de aduanas";
  const { tieneAlguno } = useSesionActual();

  const cargar = useCallback(async (silencioso) => {
    if (!silencioso) {
      setCargando(true);
    }
    try {
      setPanel(await obtenerPanelTramites(alcance));
      setError(null);
    } catch (problema) {
      setError(`No fue posible cargar el panel. ${problema.message}`);
    } finally {
      setCargando(false);
    }
  }, [alcance]);

  useEffect(() => {
    cargar(false);
    // Actualización automática: solo mientras la pestaña está visible.
    const intervalo = setInterval(() => {
      if (document.visibilityState === "visible") {
        cargar(true);
      }
    }, INTERVALO_ACTUALIZACION_MS);
    return () => clearInterval(intervalo);
  }, [cargar]);

  function cambiarAlcance(nuevo) {
    setAlcance(nuevo);
    setFiltro("TODOS");
  }

  const tramites = useMemo(() => panel?.tramites ?? [], [panel]);

  const tiposDisponibles = useMemo(
    () => [...new Set(tramites.map((tramite) => tramite.tipoTramite).filter(Boolean))].sort(),
    [tramites]
  );

  const visibles = useMemo(() => {
    const busqueda = texto.trim().toLowerCase();
    return tramites.filter((tramite) => {
      if (alcance === ALCANCE_ACTIVOS && filtro !== "TODOS") {
        const coincide = filtro === "PROXIMOS"
          ? esProximo(tramite)
          : filtro === "EN_CURSO"
            ? tramite.grupoPanel !== "POR_LLEGAR"
            : tramite.grupoPanel === filtro;
        if (!coincide) {
          return false;
        }
      }
      if (tipo && tramite.tipoTramite !== tipo) {
        return false;
      }
      return !busqueda
        || tramite.referenciaInterna?.toLowerCase().includes(busqueda)
        || tramite.motivo?.toLowerCase().includes(busqueda)
        || tramite.numeroDua?.toLowerCase().includes(busqueda);
    });
  }, [tramites, alcance, filtro, tipo, texto]);

  const conteos = {
    TODOS: panel?.totalActivos ?? 0,
    POR_LLEGAR: panel?.totalPorLlegar ?? 0,
    PERMISOS: panel?.totalPermisos ?? 0,
    APROBACION: panel?.totalAprobacion ?? 0,
    EXONERACION: panel?.totalExoneracion ?? 0,
    PROXIMOS: panel?.totalProximosVencer ?? 0,
  };

  const indicadores = [
    { valor: "EN_CURSO", cantidad: panel?.totalEnProceso ?? 0, texto: "en proceso", clase: "" },
    { valor: "POR_LLEGAR", cantidad: conteos.POR_LLEGAR, texto: "por llegar", clase: "" },
    { valor: "PERMISOS", cantidad: conteos.PERMISOS, texto: "pendientes de permisos", clase: "sim-panel-indicador-aviso" },
    { valor: "PROXIMOS", cantidad: conteos.PROXIMOS, texto: "próximos a vencer", clase: "sim-panel-indicador-alerta" },
  ];

  function rutaExpediente(tramite) {
    // Sin permiso para el tipo (por ejemplo, con solo REPORTE_CONSULTAR) la referencia no es enlace.
    const permisosTipo = CONSULTA_POR_TIPO_TRAMITE[tramite.codigoTipoTramite];
    if (!permisosTipo || !tieneAlguno(permisosTipo)) {
      return null;
    }
    const base = RUTAS_EXPEDIENTE[tramite.codigoTipoTramite];
    return base ? `${base}/${encodeURIComponent(tramite.referenciaInterna)}` : null;
  }

  return (
    <Cmp_SIM_Marco usuario={usuario} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/menu">Trámites</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Trámites activos</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-3 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Trámites activos</h1>
            <span className="badge sim-panel-badge">Vista operativa del día</span>
          </div>
          <div className="d-flex align-items-center gap-2">
            {panel?.fechaConsulta && (
              <span className="small text-muted" aria-live="polite">
                Actualizado a las {formatearHora(panel.fechaConsulta)}
              </span>
            )}
            <button type="button" className="btn btn-outline-secondary btn-sm" id="btnActualizarPanel"
                    onClick={() => cargar(false)} disabled={cargando}>
              <i className="bi bi-arrow-clockwise me-1" aria-hidden="true"></i>
              Actualizar
            </button>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Trámites en proceso, pendientes o por llegar de los tipos que usted gestiona. Use los indicadores para filtrar y abra un trámite para ver su expediente.
        </p>
      </section>

      <div className="row g-2 mb-3" role="group" aria-label="Indicadores de la vista operativa">
        {indicadores.map((indicador) => (
          <div className="col-6 col-md-3" key={indicador.texto}>
            <button type="button"
                    className={`sim-panel-indicador w-100 ${indicador.clase} ${
                      alcance === ALCANCE_ACTIVOS && filtro === indicador.valor ? "sim-panel-indicador-activo" : ""}`}
                    onClick={() => { cambiarAlcance(ALCANCE_ACTIVOS); setFiltro(indicador.valor); }}
                    aria-label={`${indicador.cantidad} ${indicador.texto}. Filtrar la lista.`}>
              <span className="sim-panel-indicador-valor">{indicador.cantidad}</span>
              <span className="small d-block">{indicador.texto}</span>
            </button>
          </div>
        ))}
      </div>

      <ul className="nav nav-tabs mb-3" role="tablist">
        <li className="nav-item" role="presentation">
          <button type="button" role="tab" id="tabTramitesActivos" aria-selected={alcance === ALCANCE_ACTIVOS}
                  className={`nav-link ${alcance === ALCANCE_ACTIVOS ? "active" : ""}`}
                  onClick={() => cambiarAlcance(ALCANCE_ACTIVOS)}>
            Activos <span className="badge text-bg-light text-dark ms-1">{conteos.TODOS}</span>
          </button>
        </li>
        <li className="nav-item" role="presentation">
          <button type="button" role="tab" id="tabTramitesHistorico" aria-selected={alcance === ALCANCE_HISTORICO}
                  className={`nav-link ${alcance === ALCANCE_HISTORICO ? "active" : ""}`}
                  onClick={() => cambiarAlcance(ALCANCE_HISTORICO)}>
            Histórico
          </button>
        </li>
      </ul>

      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <div className="card sim-card mb-4" id="seccionPanelTramites">
        <div className="card-body">
          {alcance === ALCANCE_ACTIVOS && (
            <div className="d-flex flex-wrap gap-2 mb-3" role="group" aria-label="Filtros rápidos">
              {FILTROS.map((opcion) => (
                <button type="button" key={opcion.valor}
                        className={`btn btn-sm rounded-pill ${filtro === opcion.valor ? "btn-sim" : "btn-outline-secondary"}`}
                        aria-pressed={filtro === opcion.valor}
                        onClick={() => setFiltro(opcion.valor)}>
                  {opcion.texto} <span className="ms-1">({conteos[opcion.valor]})</span>
                </button>
              ))}
            </div>
          )}

          <div className="row g-2 mb-3">
            <div className="col-12 col-md-7">
              <label className="visually-hidden" htmlFor="txtBuscarPanel">Buscar</label>
              <input type="search" className="form-control" id="txtBuscarPanel" value={texto}
                     placeholder="Buscar por referencia, descripción o DUA"
                     onChange={(evento) => setTexto(evento.target.value)} />
            </div>
            <div className="col-12 col-md-5">
              <label className="visually-hidden" htmlFor="cmbTipoPanel">Tipo de trámite</label>
              <select className="form-select" id="cmbTipoPanel" value={tipo}
                      onChange={(evento) => setTipo(evento.target.value)}>
                <option value="">Todos los tipos de trámite</option>
                {tiposDisponibles.map((nombre) => <option key={nombre} value={nombre}>{nombre}</option>)}
              </select>
            </div>
          </div>

          {cargando && !panel ? (
            <p className="text-muted mb-0">Cargando los trámites...</p>
          ) : visibles.length === 0 ? (
            <p className="text-muted small mb-0" id="lblPanelVacio">
              {alcance === ALCANCE_ACTIVOS
                ? "No hay trámites activos que coincidan con los filtros."
                : "No hay trámites en el histórico que coincidan con los filtros."}
            </p>
          ) : (
            <div className="table-responsive">
              <table className="table align-middle mb-0 sim-panel-tabla">
                <caption className="small">
                  {visibles.length} trámite(s){alcance === ALCANCE_HISTORICO && " · histórico, del más reciente al más antiguo"}
                </caption>
                <thead>
                  <tr>
                    <th scope="col">Referencia</th>
                    {/* H-04 (Sprint 2): la columna muestra la descripción del equipo, no un motivo. */}
                    <th scope="col">Descripción del equipo</th>
                    <th scope="col">DUA</th>
                    <th scope="col" className="text-end">Valor declarado</th>
                    <th scope="col">Estado</th>
                    {alcance === ALCANCE_ACTIVOS && <th scope="col" className="text-end">Días restantes</th>}
                  </tr>
                </thead>
                <tbody>
                  {visibles.map((tramite) => {
                    const ruta = rutaExpediente(tramite);
                    const esTemporal = tramite.codigoTipoTramite?.endsWith("_TEMPORAL");
                    return (
                      <tr key={tramite.idTramite}>
                        <td className="text-nowrap">
                          {ruta
                            ? <Link className="sim-referencia" to={ruta} state={{ idTramite: tramite.idTramite }}>
                                {tramite.referenciaInterna}
                              </Link>
                            : <span className="sim-referencia">{tramite.referenciaInterna}</span>}
                          <small className="d-block text-muted">{tramite.tipoTramite}</small>
                        </td>
                        <td className="sim-panel-motivo"><span title={tramite.motivo}>{tramite.motivo}</span></td>
                        <td className="text-nowrap small">
                          {tramite.numeroDua ?? <span className="text-muted">{esTemporal ? "Por asignar" : "—"}</span>}
                        </td>
                        <td className="text-end text-nowrap">{formatearMonto(tramite.valorDeclarado, tramite.moneda)}</td>
                        <td>
                          <span className={`badge ${CLASES_GRUPO[tramite.grupoPanel] ?? "text-bg-secondary"}`}>
                            {tramite.estado}
                          </span>
                          {tramite.grupoPanel !== "EN_PROCESO" && tramite.grupoPanel !== "FUERA"
                            && tramite.nombreGrupoPanel !== tramite.estado && (
                            <small className="d-block text-muted">{tramite.nombreGrupoPanel}</small>
                          )}
                        </td>
                        {alcance === ALCANCE_ACTIVOS && (
                          <td className={`text-end text-nowrap ${claseDias(tramite)}`}>{textoDias(tramite)}</td>
                        )}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          <div className="alert alert-secondary small mt-3 mb-0" role="note">
            Cuando un trámite se desalmacena o se finaliza, sale automáticamente de este panel y se conserva
            en el histórico con su bitácora; no se elimina del sistema.
            {alcance === ALCANCE_HISTORICO && (
              <> Para buscar con más criterios use <Link to="/tramites/busqueda">Buscar trámites</Link>.</>
            )}
          </div>
        </div>
      </div>
    </Cmp_SIM_Marco>
  );
}

export default Lst_SIM_TramitesActivos;
