import { useState } from "react";

import { Cmp_SIM_AyudaCampo } from "./Cmp_SIM_Ayuda.jsx";
import { cambiarReferencia } from "../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 23/09/2026
 * Descripción: Cambio de referencia de un expediente. Corrige la referencia interna y, cuando la
 *   sigla nueva pertenece a otro tipo de trámite, reclasifica el expediente sin perder su
 *   documentación ni su bitácora. Pide los datos que exige el tipo al que pasa el expediente.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: el contrato SICOP es opcional al pasar a exoneración (hay exoneraciones que no
 *     van por SICOP). El componente también se usa en el expediente de donación (caso D → DE).
 */

const TIPOS_EXONERACION = [
  { codigo: "COMPRA_INSTITUCIONAL", nombre: "Compra institucional" },
  { codigo: "DONACION", nombre: "Donación" },
  { codigo: "CLIENTE_EXTERNO", nombre: "Cliente externo con contrato" },
  { codigo: "VEHICULOS", nombre: "Vehículos" },
  { codigo: "MOVIM_TRASPASO", nombre: "Movimiento de traspaso" },
];

const SIGLAS = [
  { prefijo: "EXO-", codigo: "EXONERACION", nombre: "Solicitud de exoneración" },
  { prefijo: "DE-", codigo: "DESALM_ESPECIAL", nombre: "Desalmacenaje especial" },
];

const FORMULARIO_VACIO = {
  referenciaInterna: "",
  justificacion: "",
  origenDependencia: "",
  tipoExoneracion: "",
  numeroContratacion: "",
  contratoSicop: "",
  ordenCompraGeco: "",
};

/**
 * Reconoce el tipo de trámite al que pertenece la sigla de una referencia.
 *
 * @param {string} referencia referencia digitada
 * @returns {object|null} tipo de trámite reconocido o null
 */
export function reconocerTipo(referencia) {
  const texto = (referencia ?? "").toUpperCase();
  return SIGLAS.find((sigla) => texto.startsWith(sigla.prefijo)) ?? null;
}

function Cmp_SIM_CambioReferencia({ referenciaInterna, codigoTipoActual, onCambiado }) {
  const [abierto, setAbierto] = useState(false);
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);

  const destino = reconocerTipo(formulario.referenciaInterna);
  const reclasifica = destino !== null && destino.codigo !== codigoTipoActual;

  function cambiar(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function cerrar() {
    setAbierto(false);
    setFormulario(FORMULARIO_VACIO);
    setError("");
  }

  function revisarDatos() {
    if (!destino) {
      return "Digite la referencia nueva con una sigla del catálogo, por ejemplo DE- o EXO-.";
    }
    if (formulario.referenciaInterna.trim().length <= destino.prefijo.length) {
      return "Después de la sigla debe digitar el año y el consecutivo.";
    }
    if (formulario.justificacion.trim().length < 10) {
      return "Escriba la justificación del cambio, con al menos 10 caracteres.";
    }
    if (reclasifica && destino.codigo === "DESALM_ESPECIAL" && !formulario.origenDependencia.trim()) {
      return "Indique la dependencia de origen del desalmacenaje.";
    }
    if (reclasifica && destino.codigo === "EXONERACION"
        && (!formulario.tipoExoneracion || !formulario.numeroContratacion.trim())) {
      return "Complete el tipo de exoneración y el número de contratación.";
    }
    return "";
  }

  async function guardar(evento) {
    evento.preventDefault();
    const falta = revisarDatos();
    setError(falta);
    if (falta) {
      return;
    }

    setGuardando(true);
    try {
      const respuesta = await cambiarReferencia(referenciaInterna, formulario);
      cerrar();
      onCambiado(respuesta);
    } catch (problema) {
      setError(problema.message);
    } finally {
      setGuardando(false);
    }
  }

  return (
    <>
      <button type="button" className="btn btn-outline-secondary btn-sm" id="btnCambiarReferencia"
              onClick={() => setAbierto(true)}>
        <i className="bi bi-pencil-square me-1" aria-hidden="true"></i>
        Cambiar referencia
      </button>

      {abierto && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
               aria-labelledby="lblCambioReferencia">
            <div className="modal-dialog modal-lg">
              <div className="modal-content">
                <form onSubmit={guardar} noValidate>
                  <div className="modal-header">
                    <h2 className="modal-title h5" id="lblCambioReferencia">Cambiar referencia</h2>
                    <button type="button" className="btn-close" aria-label="Cerrar" onClick={cerrar}></button>
                  </div>

                  <div className="modal-body">
                    {error && <div className="alert alert-danger" role="alert">{error}</div>}

                    <div className="row g-3">
                      <div className="col-12 col-md-6">
                        <label className="form-label sim-requerido" htmlFor="txtReferenciaNueva">
                          Referencia nueva
                          <Cmp_SIM_AyudaCampo titulo="la referencia nueva">
                            Digite la referencia completa, con su sigla. Si la sigla es de otro tipo de
                            trámite, el expediente se reclasifica y conserva su documentación y su bitácora.
                          </Cmp_SIM_AyudaCampo>
                        </label>
                        <input type="text" className="form-control sim-referencia" id="txtReferenciaNueva"
                               placeholder="DE-2026-0001" maxLength={45}
                               value={formulario.referenciaInterna}
                               onChange={(evento) =>
                                 cambiar("referenciaInterna", evento.target.value.toUpperCase())} />
                      </div>

                      <div className="col-12 col-md-6">
                        <label className="form-label" htmlFor="txtReferenciaActual">Referencia actual</label>
                        <input type="text" className="form-control sim-referencia" id="txtReferenciaActual"
                               value={referenciaInterna} readOnly />
                      </div>

                      {destino && (
                        <div className="col-12">
                          <p className="mb-0 small">
                            <i className="bi bi-info-circle me-1" aria-hidden="true"></i>
                            {reclasifica
                              ? `El expediente pasa a ${destino.nombre}. Se conservan los documentos y la `
                                + "bitácora, y el trámite arranca en el estado inicial de ese tipo."
                              : `El expediente se mantiene como ${destino.nombre} y solo cambia su número.`}
                          </p>
                        </div>
                      )}

                      {reclasifica && destino.codigo === "DESALM_ESPECIAL" && (
                        <div className="col-12 col-md-6">
                          <label className="form-label sim-requerido" htmlFor="txtOrigenDependencia">
                            Dependencia de origen
                          </label>
                          <input type="text" className="form-control" id="txtOrigenDependencia" maxLength={120}
                                 value={formulario.origenDependencia}
                                 onChange={(evento) => cambiar("origenDependencia", evento.target.value)} />
                        </div>
                      )}

                      {reclasifica && destino.codigo === "EXONERACION" && (
                        <>
                          <div className="col-12 col-md-6">
                            <label className="form-label sim-requerido" htmlFor="cmbTipoExoneracion">
                              Tipo de exoneración
                            </label>
                            <select className="form-select" id="cmbTipoExoneracion"
                                    value={formulario.tipoExoneracion}
                                    onChange={(evento) => cambiar("tipoExoneracion", evento.target.value)}>
                              <option value="">Seleccione el tipo de exoneración</option>
                              {TIPOS_EXONERACION.map((tipo) => (
                                <option value={tipo.codigo} key={tipo.codigo}>{tipo.nombre}</option>
                              ))}
                            </select>
                          </div>

                          <div className="col-12 col-md-6">
                            <label className="form-label sim-requerido" htmlFor="txtNumeroContratacionDestino">
                              Número de contratación
                            </label>
                            <input type="text" className="form-control sim-referencia"
                                   id="txtNumeroContratacionDestino" maxLength={45}
                                   value={formulario.numeroContratacion}
                                   onChange={(evento) =>
                                     cambiar("numeroContratacion", evento.target.value.toUpperCase())} />
                          </div>

                          <div className="col-12 col-md-6">
                            <label className="form-label" htmlFor="txtContratoSicopDestino">
                              Contrato SICOP <span className="sim-opcional">(opcional)</span>
                            </label>
                            <input type="text" className="form-control sim-referencia"
                                   id="txtContratoSicopDestino" maxLength={45}
                                   value={formulario.contratoSicop}
                                   onChange={(evento) =>
                                     cambiar("contratoSicop", evento.target.value.toUpperCase())} />
                          </div>

                          <div className="col-12 col-md-6">
                            <label className="form-label" htmlFor="txtOrdenCompraGecoDestino">
                              Orden de compra GECO <span className="sim-opcional">(opcional)</span>
                            </label>
                            <input type="text" className="form-control sim-referencia"
                                   id="txtOrdenCompraGecoDestino" maxLength={45}
                                   value={formulario.ordenCompraGeco}
                                   onChange={(evento) =>
                                     cambiar("ordenCompraGeco", evento.target.value.toUpperCase())} />
                          </div>
                        </>
                      )}

                      <div className="col-12">
                        <label className="form-label sim-requerido" htmlFor="txtJustificacionReferencia">
                          Justificación del cambio
                          <Cmp_SIM_AyudaCampo titulo="la justificación">
                            Explique por qué cambia la referencia. El texto queda en la bitácora junto con
                            la referencia anterior, para que la Unidad pueda rastrear el expediente.
                          </Cmp_SIM_AyudaCampo>
                        </label>
                        <textarea className="form-control" id="txtJustificacionReferencia" rows={3}
                                  maxLength={255} value={formulario.justificacion}
                                  onChange={(evento) => cambiar("justificacion", evento.target.value)} />
                        <div className="form-text text-end">{formulario.justificacion.length}/255</div>
                      </div>
                    </div>
                  </div>

                  <div className="modal-footer">
                    <button type="button" className="btn btn-outline-secondary" id="btnCancelarReferencia"
                            onClick={cerrar}>
                      Cancelar
                    </button>
                    <button type="submit" className="btn btn-sim" id="btnGuardarReferencia" disabled={guardando}>
                      {guardando ? "Guardando..." : "Cambiar referencia"}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
          <div className="modal-backdrop fade show"></div>
        </>
      )}
    </>
  );
}

export default Cmp_SIM_CambioReferencia;
