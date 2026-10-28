import { useEffect, useState } from "react";

import { Cmp_SIM_AyudaCampo } from "./Cmp_SIM_Ayuda.jsx";
import { registrarCostosTramite } from "../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "./Cmp_SIM_Aviso.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Datos de costo del expediente (HU-036): a quién se le carga el gasto y los montos
 *   de impuestos y de bodegaje. Son los datos que totaliza el reporte de consultas internas, de
 *   modo que esta es la pantalla donde se registran. Cada cambio queda anotado en la bitácora.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: el bodegaje se indica en colones (₡). Con soloImpuestosExonerados la exoneración
 *     muestra solo los impuestos exonerados: la Unidad indicó que el bodegaje, la partida y los
 *     impuestos pagados no son parte de la exoneración. Los datos ocultos no se modifican.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: con soloLectura los costos se muestran sin poder modificarlos (restricciones por
 *     rol, HU-003): quien solo consulta el expediente no tiene el permiso de modificarlo.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: el aviso lleva la vista hasta el mensaje (H-07 del Sprint 2).
 */

const FORMATO_COLONES = new Intl.NumberFormat("es-CR", {
  style: "currency", currency: "CRC", minimumFractionDigits: 2,
});

/**
 * Muestra un monto en colones, por ejemplo ₡152 500,00.
 *
 * @param {string|number} valor monto digitado
 * @returns {string} monto con formato, o cadena vacía si no hay un número
 */
export function formatearColones(valor) {
  if (valor === "" || valor === null || valor === undefined || Number.isNaN(Number(valor))) {
    return "";
  }
  return FORMATO_COLONES.format(Number(valor));
}

/** Opciones del partido responsabilizado, según lo define la Unidad de Importaciones. */
const TIPOS_PARTIDO = [
  {
    codigo: "UNIDAD_IMPORTACIONES",
    nombre: "Unidad de Importaciones",
    pideDetalle: false,
  },
  {
    codigo: "PARTIDO_SOLICITANTE",
    nombre: "Partido solicitante",
    pideDetalle: true,
    etiqueta: "Nombre del partido solicitante",
    ejemplo: "Escuela de Química",
  },
  {
    codigo: "ORDEN_COMPRA",
    nombre: "Orden de compra",
    pideDetalle: true,
    etiqueta: "Número de la orden de compra",
    ejemplo: "2026-000456",
  },
];

function Cmp_SIM_CostosTramite({
  referenciaInterna, expediente, alGuardar, soloImpuestosExonerados = false, soloLectura = false,
}) {
  const [costos, setCostos] = useState(null);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [guardando, setGuardando] = useState(false);

  useEffect(() => {
    const tipo = expediente.tipoPartidoResponsable ?? "";
    setCostos({
      tipoPartidoResponsable: tipo,
      partidoResponsable: tipo === "UNIDAD_IMPORTACIONES" ? "" : (expediente.partidoResponsable ?? ""),
      impuestosPagados: expediente.impuestosPagados ?? "",
      impuestosExonerados: expediente.impuestosExonerados ?? "",
      montoBodegaje: expediente.montoBodegaje ?? "",
    });
  }, [expediente]);

  if (!costos) {
    return null;
  }

  const tipoElegido = TIPOS_PARTIDO.find((tipo) => tipo.codigo === costos.tipoPartidoResponsable);

  function cambiar(campo, valor) {
    setCostos((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function numeroONulo(valor) {
    return valor === "" || valor === null ? null : Number(valor);
  }

  async function guardar(evento) {
    evento.preventDefault();
    setAlerta(null);
    setGuardando(true);
    try {
      const respuesta = await registrarCostosTramite(referenciaInterna, {
        tipoPartidoResponsable: costos.tipoPartidoResponsable,
        partidoResponsable: costos.partidoResponsable,
        impuestosPagados: numeroONulo(costos.impuestosPagados),
        impuestosExonerados: numeroONulo(costos.impuestosExonerados),
        montoBodegaje: numeroONulo(costos.montoBodegaje),
      });
      setAlerta({ tipo: "success", texto: respuesta.mensaje });
      if (alGuardar) {
        alGuardar();
      }
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: problema.message });
    } finally {
      setGuardando(false);
    }
  }

  return (
    <div className="card sim-card mb-4">
      <div className="card-header">
        <i className="bi bi-cash-coin me-2" aria-hidden="true"></i>
        {soloImpuestosExonerados ? "Impuestos exonerados" : "Costos del trámite"}
      </div>
      <div className="card-body">
        {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}

        <form onSubmit={guardar} noValidate>
          <fieldset disabled={soloLectura}>
            <div className="row g-3">
              {!soloImpuestosExonerados && (
              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="cmbTipoPartido">
                  Partido responsabilizado por el costo
                  <Cmp_SIM_AyudaCampo titulo="el partido responsabilizado">
                    Es a quien se le carga el gasto del trámite: la propia Unidad de Importaciones, el
                    partido que solicitó la importación, o una orden de compra. El reporte de consultas
                    internas agrupa y filtra por este dato. Está en desarrollo: los demás tipos de
                    trámite todavía no lo registran, así que el reporte los muestra como Sin registrar.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <select className="form-select" id="cmbTipoPartido" value={costos.tipoPartidoResponsable}
                        onChange={(evento) => {
                          cambiar("tipoPartidoResponsable", evento.target.value);
                          cambiar("partidoResponsable", "");
                        }}>
                  <option value="">Sin indicar</option>
                  {TIPOS_PARTIDO.map((tipo) => (
                    <option value={tipo.codigo} key={tipo.codigo}>{tipo.nombre}</option>
                  ))}
                </select>
              </div>
              )}

              {!soloImpuestosExonerados && tipoElegido?.pideDetalle && (
                <div className="col-12 col-md-6">
                  <label className="form-label sim-requerido" htmlFor="txtDetallePartido">
                    {tipoElegido.etiqueta}
                  </label>
                  <input type="text" className="form-control" id="txtDetallePartido" maxLength={120}
                         placeholder={tipoElegido.ejemplo}
                         value={costos.partidoResponsable}
                         onChange={(evento) => cambiar("partidoResponsable", evento.target.value)} />
                </div>
              )}

              {!soloImpuestosExonerados && (
              <div className="col-12 col-md-4">
                <label className="form-label" htmlFor="numImpuestosPagados">Impuestos pagados</label>
                <input type="number" className="form-control" id="numImpuestosPagados" min="0" step="0.01"
                       value={costos.impuestosPagados}
                       onChange={(evento) => cambiar("impuestosPagados", evento.target.value)} />
              </div>
              )}

              <div className="col-12 col-md-4">
                <label className="form-label" htmlFor="numImpuestosExonerados">Impuestos exonerados</label>
                <input type="number" className="form-control" id="numImpuestosExonerados" min="0" step="0.01"
                       value={costos.impuestosExonerados}
                       onChange={(evento) => cambiar("impuestosExonerados", evento.target.value)} />
              </div>

              {!soloImpuestosExonerados && (
              <div className="col-12 col-md-4">
                <label className="form-label" htmlFor="numMontoBodegaje">
                  Bodegaje (₡)
                  <Cmp_SIM_AyudaCampo titulo="el bodegaje">
                    El bodegaje se paga en colones. Digite el monto sin símbolo ni separadores de miles.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <div className="input-group">
                  <span className="input-group-text" aria-hidden="true">₡</span>
                  <input type="number" className="form-control" id="numMontoBodegaje" min="0" step="0.01"
                         aria-describedby="txtBodegajeColones"
                         value={costos.montoBodegaje}
                         onChange={(evento) => cambiar("montoBodegaje", evento.target.value)} />
                </div>
                <div className="form-text" id="txtBodegajeColones">{formatearColones(costos.montoBodegaje)}</div>
              </div>
              )}
            </div>

            {!soloLectura && (
              <div className="d-flex justify-content-end mt-3">
                <button type="submit" className="btn btn-sim" id="btnGuardarCostos" disabled={guardando}>
                  {guardando ? "Guardando..." : "Guardar costos"}
                </button>
              </div>
            )}
          </fieldset>
        </form>
      </div>
    </div>
  );
}

export default Cmp_SIM_CostosTramite;
