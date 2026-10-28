import { useCallback, useEffect, useRef, useState } from "react";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_TablaDesplazable from "../../componentes/Cmp_SIM_TablaDesplazable.jsx";
import Cmp_SIM_SeleccionMultiple, { useCierreExterno } from "../../componentes/Cmp_SIM_SeleccionMultiple.jsx";
import {
  buscarTramites,
  exportarTramites,
  obtenerOpcionesBusqueda,
} from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 30/09/2026
 * Descripción: Búsqueda y filtrado de trámites (HU-034). Envía los criterios al servicio REST,
 *   que genera el listado desde la base de datos, dibuja las columnas que define el servidor y
 *   exporta el listado en los formatos que el servidor ofrece, con los mismos criterios de la
 *   última búsqueda mostrada.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: pruebas de usabilidad con usuarios.
 *     - PU-01: al presionar Buscar el botón indica que está buscando y, al terminar, la vista baja a
 *       los resultados con el texto "N trámites encontrados" (o el aviso de que no hay resultados).
 *     - PU-12: la tabla tiene la barra de desplazamiento horizontal también arriba y la columna de
 *       la referencia queda fija al desplazarse.
 */

// El inicio de sesión todavía no está integrado: el nombre del usuario es fijo.
const USUARIO_ACTUAL = "Analista de Importaciones";

const TEXTO_NO_REGISTRADO_AUN = "No registrado aún";
const TEXTO_SIN_DATO = "—";

const CLASES_CATEGORIA_ESTADO = {
  PEN: "text-bg-warning",
  ACT: "text-bg-info",
  FIN: "text-bg-success",
};

const ICONOS_FORMATO = {
  EXCEL: "bi-file-earmark-excel text-success",
  PDF: "bi-file-earmark-pdf text-danger",
  WORD: "bi-file-earmark-word text-primary",
};

// Cada selección múltiple se envía al servicio con el nombre de parámetro de su clave.
const FILTROS_SELECCION = [
  { parametro: "tiposTramite", id: "cmbTiposTramite", etiqueta: "Tipo de contratación",
    textoTodos: "Todos los tipos" },
  { parametro: "categoriasEstado", id: "cmbCategoriasEstado", etiqueta: "Estado",
    textoTodos: "Todos los estados" },
  { parametro: "monedas", id: "cmbMonedas", etiqueta: "Moneda", textoTodos: "Todas las monedas" },
  { parametro: "regimenesAduaneros", id: "cmbRegimenesAduaneros", etiqueta: "Régimen aduanero",
    textoTodos: "Todos los regímenes",
    textoSinOpciones: "Ningún trámite tiene régimen aduanero registrado." },
  { parametro: "tiposExoneracion", id: "cmbTiposExoneracion", etiqueta: "Tipo de exoneración",
    textoTodos: "Todos los tipos" },
  { parametro: "aduanasControl", id: "cmbAduanasControl", etiqueta: "Aduana de control",
    textoTodos: "Todas las aduanas",
    textoSinOpciones: "Ningún trámite tiene aduana de control registrada." },
];

const CRITERIOS_VACIOS = {
  fechaDesde: "",
  fechaHasta: "",
  referenciaInterna: "",
  tiposTramite: [],
  categoriasEstado: [],
  monedas: [],
  regimenesAduaneros: [],
  tiposExoneracion: [],
  aduanasControl: [],
};

/** Convierte las opciones que envía el servidor al formato { valor, nombre } de cada filtro. */
function convertirOpciones(opciones) {
  const aTexto = (valor) => ({ valor, nombre: valor });
  return {
    tiposTramite: opciones.tiposTramite.map((tipo) => ({ valor: tipo.idTipoTramite, nombre: tipo.nombre })),
    categoriasEstado: opciones.categoriasEstado.map((categoria) => ({
      valor: categoria.codigo, nombre: categoria.nombre,
    })),
    monedas: opciones.monedas.map((moneda) => ({
      valor: moneda.idMoneda, nombre: `${moneda.codigoIso} · ${moneda.nombre}`,
    })),
    regimenesAduaneros: opciones.regimenesAduaneros.map(aTexto),
    tiposExoneracion: opciones.tiposExoneracion.map((tipo) => ({ valor: tipo.codigo, nombre: tipo.nombre })),
    aduanasControl: opciones.aduanasControl.map(aTexto),
  };
}

function construirParametros(criterios) {
  const parametros = new URLSearchParams();
  if (criterios.fechaDesde) {
    parametros.set("fechaDesde", criterios.fechaDesde);
  }
  if (criterios.fechaHasta) {
    parametros.set("fechaHasta", criterios.fechaHasta);
  }
  const referencia = criterios.referenciaInterna.trim();
  if (referencia) {
    parametros.set("referenciaInterna", referencia);
  }
  FILTROS_SELECCION.forEach(({ parametro }) => {
    criterios[parametro].forEach((valor) => parametros.append(parametro, valor));
  });
  return parametros;
}

function periodoEsInvalido(criterios) {
  return Boolean(criterios.fechaDesde && criterios.fechaHasta && criterios.fechaDesde > criterios.fechaHasta);
}

function describirCriterios(parametros) {
  const cantidad = new Set(parametros.keys()).size;
  if (cantidad === 0) {
    return "Mostrando todos los trámites registrados.";
  }
  return cantidad === 1 ? "Resultados filtrados por 1 criterio." : `Resultados filtrados por ${cantidad} criterios.`;
}

function formatearFecha(valor) {
  if (!valor) {
    return TEXTO_SIN_DATO;
  }
  return new Date(valor).toLocaleDateString("es-CR", { day: "2-digit", month: "2-digit", year: "numeric" });
}

function formatearMonto(valor) {
  if (valor === null || valor === undefined) {
    return TEXTO_SIN_DATO;
  }
  return Number(valor).toLocaleString("es-CR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function textoOSinDato(valor) {
  return valor === null || valor === undefined || String(valor).trim() === "" ? TEXTO_SIN_DATO : String(valor);
}

function descargarArchivo(archivo, nombreArchivo) {
  const enlace = document.createElement("a");
  enlace.href = URL.createObjectURL(archivo);
  enlace.download = nombreArchivo;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(enlace.href);
}

function CeldaTramite({ columna, tramite }) {
  if (!columna.disponible) {
    return <td className="sim-no-registrado">{TEXTO_NO_REGISTRADO_AUN}</td>;
  }
  switch (columna.codigo) {
    case "referenciaInterna":
      return <td className="sim-referencia fw-semibold text-nowrap">{tramite.referenciaInterna}</td>;
    case "fechaRegistro":
      return <td className="text-nowrap">{formatearFecha(tramite.fechaRegistro)}</td>;
    case "estado":
      return (
        <td>
          <span className={`badge ${CLASES_CATEGORIA_ESTADO[tramite.categoriaEstado] ?? "text-bg-secondary"}`}>
            {tramite.categoriaEstadoNombre ?? TEXTO_SIN_DATO}
          </span>
          <div className="small text-muted">{tramite.estadoTramite}</div>
        </td>
      );
    case "valorMercancia":
      return <td className="text-end text-nowrap">{formatearMonto(tramite.valorMercancia)}</td>;
    case "tipoExoneracion":
      return <td>{textoOSinDato(tramite.tipoExoneracionNombre)}</td>;
    default:
      return <td>{textoOSinDato(tramite[columna.codigo])}</td>;
  }
}

function Lst_SIM_BuscarTramites() {
  const [opcionesFiltros, setOpcionesFiltros] = useState(null);
  const [columnas, setColumnas] = useState([]);
  const [formatos, setFormatos] = useState([]);
  const [criterios, setCriterios] = useState(CRITERIOS_VACIOS);
  const [periodoInvalido, setPeriodoInvalido] = useState(false);
  const [tramites, setTramites] = useState([]);
  // Criterios de la búsqueda que se muestra en la tabla: la exportación usa exactamente estos.
  const [parametrosUltimaBusqueda, setParametrosUltimaBusqueda] = useState(null);
  const [cargando, setCargando] = useState(false);
  const [exportando, setExportando] = useState(false);
  const [menuExportarAbierto, setMenuExportarAbierto] = useState(false);
  const [alerta, setAlerta] = useState(null);

  const campoFechaHasta = useRef(null);
  const zonaAlerta = useRef(null);
  const zonaResultados = useRef(null);
  const busquedaSolicitada = useRef(false);
  const menuExportar = useRef(null);
  const cerrarMenuExportar = useCallback(() => setMenuExportarAbierto(false), []);

  useCierreExterno(menuExportar, menuExportarAbierto, cerrarMenuExportar);

  useEffect(() => {
    if (alerta) {
      zonaAlerta.current?.scrollIntoView({ behavior: "smooth", block: "center" });
    }
  }, [alerta]);

  const buscar = useCallback(async (criteriosBusqueda) => {
    setAlerta(null);
    if (periodoEsInvalido(criteriosBusqueda)) {
      setPeriodoInvalido(true);
      setAlerta({ tipo: "danger", mensaje: "La fecha inicial del periodo no puede ser posterior a la fecha final." });
      campoFechaHasta.current?.focus();
      return;
    }

    const parametros = construirParametros(criteriosBusqueda);
    setCargando(true);
    setTramites([]);
    try {
      setTramites(await buscarTramites(parametros));
      setParametrosUltimaBusqueda(parametros);
      // PU-01: si la persona presionó Buscar, la vista baja hasta el resultado.
      if (busquedaSolicitada.current) {
        busquedaSolicitada.current = false;
        window.setTimeout(() => zonaResultados.current?.scrollIntoView({ behavior: "smooth", block: "start" }), 0);
      }
    } catch (error) {
      setParametrosUltimaBusqueda(null);
      setAlerta({ tipo: "danger", mensaje: `No fue posible realizar la búsqueda. ${error.message}` });
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    let vigente = true;
    obtenerOpcionesBusqueda()
      .then((opciones) => {
        if (!vigente) {
          return;
        }
        setOpcionesFiltros(convertirOpciones(opciones));
        setColumnas(opciones.columnas);
        setFormatos(opciones.formatosExportacion);
        buscar(CRITERIOS_VACIOS);
      })
      .catch((error) => {
        if (vigente) {
          setAlerta({
            tipo: "danger",
            mensaje: `No fue posible cargar los criterios de búsqueda. ${error.message} `
              + "Recargue la página para intentarlo de nuevo.",
          });
        }
      });
    return () => {
      vigente = false;
    };
  }, [buscar]);

  function actualizarCriterio(campo, valor) {
    const nuevos = { ...criterios, [campo]: valor };
    setCriterios(nuevos);
    if (campo === "fechaDesde" || campo === "fechaHasta") {
      setPeriodoInvalido(periodoEsInvalido(nuevos));
    }
  }

  function enviarBusqueda(evento) {
    evento.preventDefault();
    busquedaSolicitada.current = true;
    buscar(criterios);
  }

  function limpiarFiltros() {
    setCriterios(CRITERIOS_VACIOS);
    setPeriodoInvalido(false);
    buscar(CRITERIOS_VACIOS);
  }

  async function exportar(formato) {
    setMenuExportarAbierto(false);
    if (parametrosUltimaBusqueda === null) {
      return;
    }
    setAlerta(null);
    setExportando(true);
    try {
      const { archivo, nombreArchivo } = await exportarTramites(parametrosUltimaBusqueda, formato.codigo);
      descargarArchivo(archivo, nombreArchivo);
    } catch (error) {
      setAlerta({
        tipo: "danger",
        mensaje: `No fue posible exportar el listado en ${formato.nombre}. ${error.message}`,
      });
    } finally {
      setExportando(false);
    }
  }

  const hayResultados = parametrosUltimaBusqueda !== null && tramites.length > 0;
  const exportarDeshabilitado = cargando || exportando || !hayResultados;

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <h1 className="h4 mb-1">Búsqueda de trámites</h1>
        <p className="mb-0 text-muted small">
          Filtre los trámites por uno o varios criterios a la vez. El listado lo genera el sistema con los
          criterios aplicados y puede exportarse en Excel, PDF o Word.
        </p>
      </section>

      <div ref={zonaAlerta}>
        {alerta && (
          <div className={`alert alert-${alerta.tipo} alert-dismissible`} role="alert">
            {alerta.mensaje}
            <button type="button" className="btn-close" aria-label="Cerrar" onClick={() => setAlerta(null)}></button>
          </div>
        )}
      </div>

      <form className="card sim-card mb-4" id="frmBusqueda" noValidate onSubmit={enviarBusqueda}>
        <div className="card-header">
          <i className="bi bi-funnel me-1" aria-hidden="true"></i>Criterios de búsqueda
        </div>
        <div className="card-body">
          <div className="row g-3">
            <div className="col-12 col-md-6">
              <span className="form-label d-block" id="lblPeriodo">Periodo de registro</span>
              <div className="row g-2" role="group" aria-labelledby="lblPeriodo">
                <div className="col-6">
                  <label htmlFor="dtpFechaDesde" className="form-label small text-muted mb-1">Desde</label>
                  <input type="date" className="form-control" id="dtpFechaDesde" value={criterios.fechaDesde}
                         onChange={(evento) => actualizarCriterio("fechaDesde", evento.target.value)} />
                </div>
                <div className="col-6">
                  <label htmlFor="dtpFechaHasta" className="form-label small text-muted mb-1">Hasta</label>
                  <input type="date" className={`form-control${periodoInvalido ? " is-invalid" : ""}`}
                         id="dtpFechaHasta" ref={campoFechaHasta} value={criterios.fechaHasta}
                         onChange={(evento) => actualizarCriterio("fechaHasta", evento.target.value)} />
                  <div className="invalid-feedback">La fecha final no puede ser anterior a la inicial.</div>
                </div>
              </div>
            </div>

            <div className="col-12 col-md-6">
              <label htmlFor="txtReferenciaInterna" className="form-label">Id de trámite</label>
              <input type="search" className="form-control sim-referencia" id="txtReferenciaInterna"
                     maxLength={45} placeholder="DE-2026-0001" aria-describedby="lblAyudaReferencia"
                     value={criterios.referenciaInterna}
                     onChange={(evento) => actualizarCriterio("referenciaInterna", evento.target.value)} />
              <div id="lblAyudaReferencia" className="form-text">
                Puede escribir solo una parte, por ejemplo <strong>EXO-2026</strong>.
              </div>
            </div>

            {FILTROS_SELECCION.map((filtro) => (
              <div className="col-12 col-md-6" key={filtro.parametro}>
                <label htmlFor={filtro.id} className="form-label">{filtro.etiqueta}</label>
                <Cmp_SIM_SeleccionMultiple
                  id={filtro.id}
                  opciones={opcionesFiltros?.[filtro.parametro] ?? []}
                  seleccion={criterios[filtro.parametro]}
                  alCambiar={(valores) => actualizarCriterio(filtro.parametro, valores)}
                  textoTodos={filtro.textoTodos}
                  textoSinOpciones={filtro.textoSinOpciones}
                />
              </div>
            ))}

            <div className="col-12 col-md-6">
              <label htmlFor="txtProveedor" className="form-label">Proveedor</label>
              <input type="text" className="form-control" id="txtProveedor" placeholder="No registrado aún"
                     disabled aria-describedby="lblAyudaProveedor" />
              <div id="lblAyudaProveedor" className="form-text">
                El sistema todavía no registra proveedores. El filtro se habilitará con el módulo de
                compra exterior.
              </div>
            </div>
          </div>
        </div>
        <div className="card-footer bg-white d-flex justify-content-end gap-2">
          <button type="button" className="btn btn-outline-secondary" id="btnLimpiarFiltros"
                  disabled={opcionesFiltros === null} onClick={limpiarFiltros}>
            <i className="bi bi-eraser me-1" aria-hidden="true"></i>Limpiar
          </button>
          <button type="submit" className="btn btn-primary" id="btnBuscarTramites"
                  disabled={opcionesFiltros === null || cargando}>
            {cargando
              ? <span className="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>
              : <i className="bi bi-search me-1" aria-hidden="true"></i>}
            {cargando ? "Buscando…" : "Buscar"}
          </button>
        </div>
      </form>

      <div className="card sim-card" ref={zonaResultados}>
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span>
            <i className="bi bi-list-ul me-1" aria-hidden="true"></i>Trámites encontrados
            <span className="badge text-bg-light text-dark ms-1" id="lblContadorTramites">{tramites.length}</span>
          </span>
          <div className="dropdown" ref={menuExportar}>
            <button type="button" className="btn btn-sm btn-outline-secondary dropdown-toggle"
                    id="btnExportarTramites" aria-expanded={menuExportarAbierto}
                    disabled={exportarDeshabilitado}
                    onClick={() => setMenuExportarAbierto((estado) => !estado)}>
              <i className="bi bi-download me-1" aria-hidden="true"></i>Exportar
            </button>
            <ul className={`dropdown-menu dropdown-menu-end${menuExportarAbierto ? " show" : ""}`}
                data-bs-popper="static" aria-labelledby="btnExportarTramites">
              {formatos.map((formato) => (
                <li key={formato.codigo}>
                  <button type="button" className="dropdown-item" id={`btnExportar${formato.nombre}`}
                          onClick={() => exportar(formato)}>
                    <i className={`bi ${ICONOS_FORMATO[formato.codigo] ?? "bi-file-earmark"} me-2`}
                       aria-hidden="true"></i>
                    {formato.nombre}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        </div>
        <div className="card-body">
          {parametrosUltimaBusqueda !== null && (
            <p className="small text-muted mb-3" id="lblCriteriosAplicados" aria-live="polite">
              {!cargando && (
                <strong className="text-body me-1" id="lblCantidadResultados">
                  {tramites.length === 0
                    ? "Sin resultados."
                    : `${tramites.length} ${tramites.length === 1 ? "trámite encontrado" : "trámites encontrados"}.`}
                </strong>
              )}
              {describirCriterios(parametrosUltimaBusqueda)}
            </p>
          )}
          <Cmp_SIM_TablaDesplazable>
            <table className="table table-striped table-sm align-middle mb-0 sim-tabla-busqueda sim-tabla-columna-fija">
              <caption className="visually-hidden">Trámites que cumplen los criterios de búsqueda</caption>
              <thead>
                <tr>
                  {columnas.map((columna) => (
                    <th scope="col" key={columna.codigo}
                        className={columna.tipoDato === "MONTO" ? "text-end" : undefined}>
                      {columna.titulo}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {tramites.map((tramite) => (
                  <tr key={tramite.idTramite}>
                    {columnas.map((columna) => (
                      <CeldaTramite columna={columna} tramite={tramite} key={columna.codigo} />
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </Cmp_SIM_TablaDesplazable>
          {cargando && (
            <div className="text-center text-muted py-4" id="lblCargando" role="status">
              <span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>Buscando trámites…
            </div>
          )}
          {!cargando && parametrosUltimaBusqueda !== null && tramites.length === 0 && (
            <p className="text-muted small mb-0 mt-3" id="lblSinResultados">
              No hay trámites que coincidan con los criterios aplicados. Ajuste los criterios e intente de nuevo.
            </p>
          )}
        </div>
      </div>
    </Cmp_SIM_Marco>
  );
}

export default Lst_SIM_BuscarTramites;
