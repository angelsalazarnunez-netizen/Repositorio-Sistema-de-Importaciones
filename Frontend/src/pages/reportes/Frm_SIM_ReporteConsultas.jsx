import { useEffect, useMemo, useState } from "react";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import {
  descargarReporteConsultas,
  generarReporteConsultas,
  obtenerPartidosResponsables,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Reporte de consultas internas (HU-036). La persona agente de aduanas elige uno o
 *   varios criterios de agrupación, acota el periodo y el partido responsabilizado por el costo,
 *   y recibe un reporte tabulado con la cantidad de trámites y los totales de CIF, impuestos,
 *   bodegajes y permisos, con su fila de totales y la descarga en Excel o en PDF.
 *
 * Historial de cambios:
 *   Responsable: Katherine Susana Valverde Fallas
 *   Fecha: 05/10/2026
 *   Descripción: la pantalla pasa a admitir varios criterios de agrupación a la vez y a mostrar
 *     una tabla por criterio, con sus totales.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): los botones de exportar solo se muestran con
 *     REPORTE_EXPORTAR.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: se agrega el encabezado con la descripción de la pantalla; se recupera H-05
 *     ("Todo el periodo"), que estaba en una rama sin integrar; el aviso lleva la vista hasta el
 *     mensaje.
 */

const USUARIO_ACTUAL = "Agente de Aduanas";

/** Criterios con los que se puede desglosar el reporte. */
const AGRUPACIONES = [
  { codigo: "TIPO_TRAMITE", nombre: "Tipo de trámite" },
  { codigo: "REGIMEN_ADUANERO", nombre: "Régimen aduanero" },
  { codigo: "PARTIDO_RESPONSABLE", nombre: "Partido responsabilizado por el costo" },
  { codigo: "TIPO_EXONERACION", nombre: "Tipo de exoneración" },
  { codigo: "MES", nombre: "Mes de registro" },
];

/** Columnas de totales del reporte, en el orden en el que se leen. */
const COLUMNAS = [
  { codigo: "cif", titulo: "Total CIF", campo: "totalCif" },
  { codigo: "impuestos", titulo: "Impuestos pagados", campo: "totalImpuestosPagados" },
  { codigo: "exonerados", titulo: "Exonerados", campo: "totalImpuestosExonerados" },
  { codigo: "bodegajes", titulo: "Bodegajes (₡)", campo: "totalBodegaje" },
  { codigo: "permisos", titulo: "Permisos", campo: "cantidadPermisos" },
];

const CRITERIOS_INICIALES = ["TIPO_TRAMITE", "REGIMEN_ADUANERO"];

const FILTROS_VACIOS = {
  fechaDesde: "",
  fechaHasta: "",
  partido: "",
  totalCif: true,
  totalBodegajes: true,
  enumerarTramites: false,
  combinarCriterios: false,
};

const AYUDA = {
  titulo: "Ayuda: reporte de consultas internas",
  introduccion:
    "El reporte resume los trámites ya registrados para responder consultas internas sobre costos "
    + "y actividad de la Unidad de Importaciones.",
  pasos: [
    "Elija uno o varios criterios de agrupación: el reporte entrega una tabla por cada uno.",
    "Acote el periodo y, si la consulta lo pide, el partido responsabilizado por el costo.",
    "Marque los totales que quiere ver y si necesita la lista de trámites individuales.",
    "Presione Generar reporte y expórtelo a Excel o a PDF.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué las tablas separan las monedas?",
      respuesta:
        "Porque el sistema no registra tipo de cambio. Cada línea totaliza su moneda y la fila de "
        + "totales se calcula por moneda, de modo que los colones y los dólares nunca se suman "
        + "entre sí.",
    },
    {
      pregunta: "¿De dónde salen los montos?",
      respuesta:
        "Del expediente: el valor estimado hace de monto CIF, y los impuestos y el bodegaje son "
        + "los que se registran en el trámite. Un expediente en proceso puede no declararlos "
        + "todavía, y en ese caso suma cero.",
    },
    {
      pregunta: "¿Qué es el partido responsabilizado por el costo?",
      respuesta:
        "La unidad a la que se le carga el gasto: la Unidad de Importaciones, el partido "
        + "solicitante o una orden de compra. El dato está en desarrollo: por ahora solo lo "
        + "registran el desalmacenaje especial y la exoneración, desde su expediente, así que "
        + "el desglose y el filtro muestran únicamente los trámites que ya lo tienen.",
    },
  ],
};

function formatearMonto(valor) {
  return Number(valor ?? 0).toLocaleString("es-CR", {
    minimumFractionDigits: 2, maximumFractionDigits: 2,
  });
}

function formatearFecha(valor) {
  return valor ? new Date(`${valor}T00:00:00`).toLocaleDateString("es-CR") : "";
}

/**
 * H-05 (Sprint 2): describe el periodo consultado con las palabras que usa la persona agente.
 * Cuando no se indica ninguna fecha se dice "Todo el periodo", en lugar de hablar de límites.
 * (Corrección de la rama feature/usabilidad-hallazgos-sprint2, que no se había integrado.)
 *
 * @param {string} desde fecha inicial del filtro, si se indicó
 * @param {string} hasta fecha final del filtro, si se indicó
 * @returns {string} periodo listo para leerse
 */
function describirPeriodo(desde, hasta) {
  if (!desde && !hasta) {
    return "Todo el periodo";
  }
  if (desde && hasta) {
    return `Periodo del ${formatearFecha(desde)} al ${formatearFecha(hasta)}`;
  }
  return desde
    ? `Periodo a partir del ${formatearFecha(desde)}`
    : `Periodo hasta el ${formatearFecha(hasta)}`;
}

/**
 * Suma las líneas de un desglose por moneda, para la fila de totales de cada tabla.
 *
 * @param {Array} lineas líneas del desglose
 * @returns {Array} un total por cada moneda presente
 */
function totalizarPorMoneda(lineas) {
  const totales = new Map();
  (lineas ?? []).forEach((linea) => {
    const total = totales.get(linea.moneda) ?? {
      moneda: linea.moneda,
      cantidadTramites: 0,
      totalCif: 0,
      totalImpuestosPagados: 0,
      totalImpuestosExonerados: 0,
      totalBodegaje: 0,
      cantidadPermisos: 0,
    };
    total.cantidadTramites += linea.cantidadTramites;
    total.totalCif += Number(linea.totalCif ?? 0);
    total.totalImpuestosPagados += Number(linea.totalImpuestosPagados ?? 0);
    total.totalImpuestosExonerados += Number(linea.totalImpuestosExonerados ?? 0);
    total.totalBodegaje += Number(linea.totalBodegaje ?? 0);
    total.cantidadPermisos += linea.cantidadPermisos;
    totales.set(linea.moneda, total);
  });
  return [...totales.values()];
}

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Tabla de un desglose del reporte, con su fila de totales por moneda.
 */
function Cmp_SIM_TablaDesglose({ bloque, columnas, variasMonedas }) {
  const totales = totalizarPorMoneda(bloque.lineas);
  const encabezados = bloque.encabezados.length > 0 ? bloque.encabezados : ["Grupo"];
  const celdas = (fila) => columnas.map((columna) => (
    <td className="text-end" key={columna.codigo}>
      {columna.campo === "cantidadPermisos"
        ? fila.cantidadPermisos
        : formatearMonto(fila[columna.campo])}
    </td>
  ));

  return (
    <section className="mb-4">
      <h2 className="h6 mb-2">{bloque.titulo}</h2>
      <div className="table-responsive">
        <table className="table table-sm align-middle mb-0">
          <caption className="visually-hidden">{bloque.titulo}</caption>
          <thead>
            <tr>
              {encabezados.map((encabezado) => (
                <th scope="col" key={encabezado}>{encabezado}</th>
              ))}
              {variasMonedas && <th scope="col">Moneda</th>}
              <th scope="col" className="text-end">Trámites</th>
              {columnas.map((columna) => (
                <th scope="col" className="text-end" key={columna.codigo}>{columna.titulo}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {bloque.lineas.length === 0 && (
              <tr>
                <td colSpan={columnas.length + encabezados.length + (variasMonedas ? 2 : 1)}
                    className="text-muted small">
                  Ningún trámite cumple los criterios indicados.
                </td>
              </tr>
            )}

            {bloque.lineas.map((linea) => (
              <tr key={`${linea.grupo}-${linea.moneda}`}>
                {encabezados.map((encabezado, posicion) => (
                  <td key={encabezado}>{linea.valores[posicion] ?? linea.grupo}</td>
                ))}
                {variasMonedas && <td>{linea.moneda}</td>}
                <td className="text-end">{linea.cantidadTramites}</td>
                {celdas(linea)}
              </tr>
            ))}
          </tbody>

          {totales.length > 0 && (
            <tfoot>
              {totales.map((total) => (
                <tr className="fw-semibold" key={`total-${total.moneda}`}>
                  <td colSpan={encabezados.length}>Total</td>
                  {variasMonedas && <td>{total.moneda}</td>}
                  <td className="text-end">{total.cantidadTramites}</td>
                  {celdas(total)}
                </tr>
              ))}
            </tfoot>
          )}
        </table>
      </div>
    </section>
  );
}

function Frm_SIM_ReporteConsultas() {
  const [criterios, setCriterios] = useState(CRITERIOS_INICIALES);
  const [filtros, setFiltros] = useState(FILTROS_VACIOS);
  const [partidos, setPartidos] = useState([]);
  const [reporte, setReporte] = useState(null);
  const [filtrosDelReporte, setFiltrosDelReporte] = useState(FILTROS_VACIOS);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [generando, setGenerando] = useState(false);
  const [descargando, setDescargando] = useState("");

  useEffect(() => {
    obtenerPartidosResponsables()
      .then(setPartidos)
      .catch(() => setPartidos([]));
  }, []);

  function alternarCriterio(codigo) {
    setCriterios((anteriores) => (anteriores.includes(codigo)
      ? anteriores.filter((criterio) => criterio !== codigo)
      : [...anteriores, codigo]));
  }

  function cambiar(campo, valor) {
    setFiltros((anterior) => ({ ...anterior, [campo]: valor }));
  }

  /** Arma los criterios con los que el servicio genera o descarga el reporte. */
  function armarCriterios() {
    return { ...filtros, dependencia: filtros.partido, agrupaciones: criterios };
  }

  async function generar(evento) {
    evento.preventDefault();
    setAlerta(null);

    if (criterios.length === 0) {
      setAlerta({ tipo: "danger", texto: "Elija al menos un criterio de agrupación." });
      return;
    }
    if (filtros.fechaDesde && filtros.fechaHasta && filtros.fechaHasta < filtros.fechaDesde) {
      setAlerta({ tipo: "danger", texto: "La fecha final del periodo no puede ser anterior a la inicial." });
      return;
    }

    setGenerando(true);
    try {
      setReporte(await generarReporteConsultas(armarCriterios()));
      setFiltrosDelReporte({ ...filtros });
    } catch (problema) {
      setReporte(null);
      setAlerta({ tipo: "danger", texto: `No fue posible generar el reporte. ${problema.message}` });
    } finally {
      setGenerando(false);
    }
  }

  async function descargar(formato) {
    setAlerta(null);
    setDescargando(formato);
    try {
      await descargarReporteConsultas(armarCriterios(), formato);
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible exportar el reporte. ${problema.message}` });
    } finally {
      setDescargando("");
    }
  }

  const columnas = useMemo(
    () => COLUMNAS.filter((columna) => {
      if (columna.codigo === "cif") {
        return filtrosDelReporte.totalCif;
      }
      if (columna.codigo === "bodegajes") {
        return filtrosDelReporte.totalBodegajes;
      }
      return true;
    }),
    [filtrosDelReporte]
  );

  const variasMonedas = useMemo(() => {
    const monedas = new Set();
    (reporte?.bloques ?? []).forEach((bloque) =>
      bloque.lineas.forEach((linea) => monedas.add(linea.moneda)));
    return monedas.size > 1;
  }, [reporte]);

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <h1 className="h4 mb-1">Reporte de consultas internas</h1>
        <p className="sim-descripcion-pantalla" id="lblDescripcionPantalla">
          Resuma los totales y conteos de los trámites registrados, agrupándolos por uno o varios criterios, y
          descargue el reporte en PDF o en Excel.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}

      <form onSubmit={generar} noValidate className="card sim-card mb-4 d-print-none">
        <div className="card-body">
          <fieldset className="mb-3">
            <legend className="form-label">
              Agrupar por
              <Cmp_SIM_AyudaCampo titulo="los criterios de agrupación">
                El reporte entrega una tabla por cada criterio elegido. El de partido
                responsabilizado por el costo está en desarrollo: todavía no todos los trámites
                registran ese dato, así que su desglose puede salir incompleto.
              </Cmp_SIM_AyudaCampo>
            </legend>
            <div className="d-flex flex-wrap gap-2">
              {AGRUPACIONES.map((agrupacion) => {
                const elegida = criterios.includes(agrupacion.codigo);
                return (
                  <button type="button" key={agrupacion.codigo}
                          id={`btnAgrupar${agrupacion.codigo}`}
                          className={`btn btn-sm rounded-pill ${elegida ? "btn-sim" : "btn-outline-secondary"}`}
                          aria-pressed={elegida}
                          onClick={() => alternarCriterio(agrupacion.codigo)}>
                    {agrupacion.nombre}
                    {elegida && <i className="bi bi-x ms-1" aria-hidden="true"></i>}
                  </button>
                );
              })}
            </div>
          </fieldset>

          <div className="row g-3">
            <div className="col-12 col-md-3">
              <label className="form-label" htmlFor="txtFechaDesde">Desde</label>
              <input type="date" className="form-control" id="txtFechaDesde"
                     value={filtros.fechaDesde}
                     onChange={(evento) => cambiar("fechaDesde", evento.target.value)} />
            </div>

            <div className="col-12 col-md-3">
              <label className="form-label" htmlFor="txtFechaHasta">Hasta</label>
              <input type="date" className="form-control" id="txtFechaHasta"
                     value={filtros.fechaHasta}
                     onChange={(evento) => cambiar("fechaHasta", evento.target.value)} />
            </div>

            <div className="col-12 col-md-6">
              <label className="form-label" htmlFor="cmbPartido">
                Partido responsabilizado
                <Cmp_SIM_AyudaCampo titulo="el partido responsabilizado">
                  El dato apenas se está empezando a registrar: el expediente de desalmacenaje y el de exoneración ya lo piden, pero los demás trámites todavía no, y los expedientes anteriores no lo traen. Mientras tanto, este desglose y este filtro muestran solo lo que ya tiene partido registrado, y el resto aparece como Sin registrar.
                </Cmp_SIM_AyudaCampo>
              </label>
              <select className="form-select" id="cmbPartido" value={filtros.partido}
                      onChange={(evento) => cambiar("partido", evento.target.value)}>
                <option value="">Todos</option>
                {partidos.map((partido) => <option value={partido} key={partido}>{partido}</option>)}
              </select>
            </div>
          </div>

          <fieldset className="mt-3">
            <legend className="form-label">Incluir en el reporte</legend>
            <div className="row g-2">
              <div className="col-12 col-md-4">
                <div className="form-check">
                  <input className="form-check-input" type="checkbox" id="chkTotalCif"
                         checked={filtros.totalCif}
                         onChange={(evento) => cambiar("totalCif", evento.target.checked)} />
                  <label className="form-check-label" htmlFor="chkTotalCif">Total de montos CIF</label>
                </div>
              </div>

              <div className="col-12 col-md-4">
                <div className="form-check">
                  <input className="form-check-input" type="checkbox" id="chkTotalBodegajes"
                         checked={filtros.totalBodegajes}
                         onChange={(evento) => cambiar("totalBodegajes", evento.target.checked)} />
                  <label className="form-check-label" htmlFor="chkTotalBodegajes">Total de bodegajes</label>
                </div>
              </div>

              <div className="col-12 col-md-4">
                <div className="form-check">
                  <input className="form-check-input" type="checkbox" id="chkCombinarCriterios"
                         checked={filtros.combinarCriterios}
                         onChange={(evento) => cambiar("combinarCriterios", evento.target.checked)} />
                  <label className="form-check-label" htmlFor="chkCombinarCriterios">
                    Combinar los criterios en una sola tabla
                    <Cmp_SIM_AyudaCampo titulo="la tabla combinada">
                      Marcada, el reporte entrega una sola tabla con una columna por criterio y una
                      línea por cada combinación: por ejemplo, cuánto suma cada tipo de trámite
                      dentro de cada régimen aduanero. Sin marcar, entrega una tabla por criterio.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                </div>
              </div>

              <div className="col-12 col-md-4">
                <div className="form-check">
                  <input className="form-check-input" type="checkbox" id="chkEnumerarTramites"
                         checked={filtros.enumerarTramites}
                         onChange={(evento) => cambiar("enumerarTramites", evento.target.checked)} />
                  <label className="form-check-label" htmlFor="chkEnumerarTramites">
                    Enumerar trámites individuales
                  </label>
                </div>
              </div>
            </div>
          </fieldset>

          <div className="d-flex justify-content-end gap-2 mt-3">
            <button type="button" className="btn btn-outline-secondary" id="btnLimpiar"
                    onClick={() => {
                      setCriterios(CRITERIOS_INICIALES);
                      setFiltros(FILTROS_VACIOS);
                      setReporte(null);
                      setAlerta(null);
                    }}>
              Limpiar
            </button>
            <button type="submit" className="btn btn-sim" id="btnGenerarReporte" disabled={generando}>
              {generando ? "Generando..." : "Generar reporte"}
            </button>
          </div>
        </div>
      </form>

      {reporte && (
        <>
          <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
            <p className="mb-0">
              {describirPeriodo(filtrosDelReporte.fechaDesde, filtrosDelReporte.fechaHasta)}{" · "}{reporte.cantidadTramites} trámites
            </p>
            <Cmp_SIM_ConPermiso permisos={[PERMISOS.REPORTE_EXPORTAR]}>
              <div className="d-print-none d-flex gap-2">
                <button type="button" className="btn btn-outline-secondary" id="btnDescargarPdf"
                        onClick={() => descargar("PDF")} disabled={descargando !== ""}>
                  {descargando === "PDF" ? "Exportando..." : "Exportar PDF"}
                </button>
                <button type="button" className="btn btn-outline-secondary" id="btnDescargarExcel"
                        onClick={() => descargar("EXCEL")} disabled={descargando !== ""}>
                  {descargando === "EXCEL" ? "Exportando..." : "Exportar Excel"}
                </button>
              </div>
            </Cmp_SIM_ConPermiso>
          </div>

          {reporte.bloques.map((bloque) => (
            <Cmp_SIM_TablaDesglose bloque={bloque} columnas={columnas} variasMonedas={variasMonedas}
                                   key={bloque.criterio} />
          ))}

          {filtrosDelReporte.enumerarTramites && (
            <section className="mb-4">
              <h2 className="h6 mb-2">Trámites incluidos</h2>
              <div className="table-responsive">
                <table className="table table-sm align-middle mb-0">
                  <caption className="visually-hidden">Trámites que incluye el reporte</caption>
                  <thead>
                    <tr>
                      <th scope="col">Referencia</th>
                      <th scope="col">Tipo de trámite</th>
                      <th scope="col">Régimen aduanero</th>
                      <th scope="col">Partido responsable</th>
                      <th scope="col" className="text-end">CIF</th>
                      <th scope="col">Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reporte.tramites.length === 0 && (
                      <tr>
                        <td colSpan={6} className="text-muted small">
                          Ningún trámite cumple los criterios indicados.
                        </td>
                      </tr>
                    )}

                    {reporte.tramites.map((tramite) => (
                      <tr key={tramite.referenciaInterna}>
                        <td className="sim-referencia">{tramite.referenciaInterna}</td>
                        <td>{tramite.tipoTramite}</td>
                        <td>{tramite.regimenAduanero ?? "—"}</td>
                        <td>{tramite.partidoResponsable ?? "—"}</td>
                        <td className="text-end">
                          {tramite.valorCif === null || tramite.valorCif === undefined
                            ? "—"
                            : `${tramite.moneda ?? ""} ${formatearMonto(tramite.valorCif)}`.trim()}
                        </td>
                        <td>{tramite.estado}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          )}
        </>
      )}
    </Cmp_SIM_Marco>
  );
}

export default Frm_SIM_ReporteConsultas;
