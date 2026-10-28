import { useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import {
  Cmp_SIM_IndicacionArchivos,
  Cmp_SIM_ListaArchivos,
  describirRechazados,
  separarArchivosPorTamano,
} from "../../componentes/Cmp_SIM_Documentos.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { PLANTILLAS, alEscribirConMascara, completarConsecutivo } from "../../componentes/Cmp_SIM_Mascaras.js";
import { registrarDesalmacenaje } from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Registro de un trámite de desalmacenamiento especial (HU-031). Solicita los datos
 *   generales del trámite y su documentación de respaldo, que puede ser atípica, y crea el
 *   expediente. No exige contrato SICOP ni orden de compra.
 *
 * Historial de cambios:*
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción y leyenda de obligatorios (PU-08); PU-06: la referencia completa el
 *     consecutivo con ceros; H4/H5: formato, tamaño y aviso de archivos grandes; el aviso lleva la
 *     vista hasta el mensaje.
 */

const PREFIJO_REFERENCIA = "DE-";
const USUARIO_ACTUAL = "Analista de Importaciones";

const DEPENDENCIAS = [
  "Escuela de Informática",
  "Escuela de Química",
  "Escuela de Ciencias Biológicas",
  "Laboratorio de Investigación",
  "Oficina de Suministros",
  "Vicerrectoría de Investigación",
];

const PAISES = ["Estados Unidos", "China", "Alemania", "México", "España", "Japón"];
const MONEDAS = ["CRC", "USD", "EUR"];

const AYUDA = {
  titulo: "Ayuda: registrar un desalmacenaje especial",
  introduccion:
    "El desalmacenaje especial se usa cuando la mercancía ya es propiedad de la institución y no "
    + "corresponde a una donación, a una compra de la Fundación UCR ni a un pedido a proveedor. "
    + "No requiere contrato SICOP ni orden de compra.",
  pasos: [
    "Digite la referencia interna: el sistema agrega el prefijo DE-, usted escribe el año y el consecutivo.",
    "Indique la dependencia responsable; puede escribirla aunque no esté en la lista de sugerencias.",
    "Complete el país de origen, la moneda y el valor estimado. El país queda opcional si todavía no se conoce.",
    "Escriba la justificación: por qué la mercancía requiere desalmacenaje especial.",
    "Adjunte la documentación que tenga. Puede quedar pendiente y agregarse después desde el expediente.",
    "Presione Registrar. El sistema confirma la referencia, el estado inicial y el expediente creado.",
  ],
  dudas: [
    {
      pregunta: "¿Qué sigla le corresponde a cada trámite?",
      respuesta:
        "Según la Unidad de Importaciones: D donación, DF donación por medio de la Fundación UCR, "
        + "DE desalmacenaje especial, EXO exoneración, EXP-T reimportación y exportación temporal, "
        + "y P pedido. Esta pantalla registra únicamente los trámites DE.",
    },
    {
      pregunta: "¿Puedo registrar sin toda la documentación?",
      respuesta:
        "Sí. El expediente se puede abrir sin documentos, como cuando solo se cuenta con el correo "
        + "de la guía. El expediente indica después la documentación que falta adjuntar.",
    },
    {
      pregunta: "¿Puedo corregir la referencia luego?",
      respuesta:
        "Todavía no. La referencia se define al registrar y el sistema no permite cambiarla ni pasar "
        + "el trámite a otro tipo, así que conviene revisarla antes de guardar.",
    },
  ],
};

const FORMULARIO_VACIO = {
  referenciaInterna: "",
  origenDependencia: "",
  paisOrigen: "",
  moneda: "CRC",
  valorEstimado: "",
  justificacion: "",
};

const ARCHIVOS_VACIOS = {
  documentosRespaldo: [],
  packingList: [],
  factura: [],
  documentoEmbarque: [],
};

function Frm_SIM_RegistrarDesalmacenaje() {
  const [archivosRechazados, setArchivosRechazados] = useState("");
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [archivos, setArchivos] = useState(ARCHIVOS_VACIOS);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [faltantes, setFaltantes] = useState([]);
  const [expediente, setExpediente] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const fechaRegistro = new Date().toLocaleDateString("es-CR", {
    day: "2-digit", month: "long", year: "numeric",
  });

  function cambiar(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function seleccionarArchivos(campo, lista) {
    const { aceptados, rechazados } = separarArchivosPorTamano(lista);
    setArchivosRechazados(describirRechazados(rechazados));
    setArchivos((anterior) => ({ ...anterior, [campo]: aceptados }));
  }

  /** Quita un archivo que todavía no se ha guardado. */
  function quitarArchivo(campo, indice) {
    setArchivos((anterior) => ({
      ...anterior,
      [campo]: anterior[campo].filter((_, posicion) => posicion !== indice),
    }));
  }

  /** Revisa lo obligatorio y devuelve, en lenguaje claro, lo que falta. */
  function revisarDatos() {
    const pendientes = [];
    if (!/^\d{4}-\d{4}$/.test(formulario.referenciaInterna)) {
      pendientes.push("la referencia interna, con el formato 2026-0001");
    }
    if (!formulario.origenDependencia.trim()) {
      pendientes.push("la dependencia responsable");
    }
    if (!formulario.valorEstimado || Number(formulario.valorEstimado) <= 0) {
      pendientes.push("el valor estimado de la mercancía");
    }
    if (formulario.justificacion.trim().length < 10) {
      pendientes.push("la justificación del trámite, de al menos 10 caracteres");
    }
    return pendientes;
  }

  function limpiar() {
    setFormulario(FORMULARIO_VACIO);
    setArchivos(ARCHIVOS_VACIOS);
    setFaltantes([]);
    setAlerta(null);
  }

  async function registrar(evento) {
    evento.preventDefault();
    setAlerta(null);

    const pendientes = revisarDatos();
    setFaltantes(pendientes);
    if (pendientes.length > 0) {
      setAlerta({ tipo: "danger", texto: "Falta completar la información que se indica abajo." });
      return;
    }

    setEnviando(true);
    try {
      const respuesta = await registrarDesalmacenaje(
        { ...formulario, referenciaInterna: PREFIJO_REFERENCIA + formulario.referenciaInterna },
        archivos
      );
      setExpediente(respuesta);
      limpiar();
    } catch (error) {
      setAlerta({ tipo: "danger", texto: `No fue posible registrar el trámite. ${error.message}` });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Registrar desalmacenaje especial</h1>
          </div>
          <div className="text-md-end">
            <span className="d-block text-muted small">Estado inicial</span>
            <span className="badge text-bg-warning fs-6">Pendiente</span>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Registre un desalmacenaje especial con sus datos y su documentación de respaldo. El expediente se crea en estado Pendiente.
        </p>
        {/* PU-08: leyenda de los campos obligatorios. */}
        <p className="small text-muted mb-0 mt-1" id="lblLeyendaObligatorios">
          Los campos marcados con <span className="text-danger fw-bold">*</span> son obligatorios.
        </p>
      </section>

      {alerta && (
        <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>
          {alerta.texto}
          {faltantes.length > 0 && (
            <ul className="mb-0 mt-2">
              {faltantes.map((falta) => <li key={falta}>Indique {falta}.</li>)}
            </ul>
          )}
        </div>
      )}

      <form onSubmit={registrar} noValidate>
        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>
            Datos generales del trámite
          </div>
          <div className="card-body">
            <div className="row g-3">

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtReferenciaInterna">
                  Referencia interna
                  <Cmp_SIM_AyudaCampo titulo="la referencia interna">
                    Escriba el <strong>año y el consecutivo</strong>: al digitar 20260001 el sistema
                    lo acomoda como 2026-0001. El prefijo DE- lo agrega la pantalla. La referencia no
                    se puede repetir en otro trámite.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <div className="input-group">
                  <span className="input-group-text sim-referencia">{PREFIJO_REFERENCIA}</span>
                  <input
                    type="text"
                    className="form-control sim-referencia"
                    id="txtReferenciaInterna"
                    value={formulario.referenciaInterna}
                    placeholder={`${new Date().getFullYear()}-0001`}
                    onChange={alEscribirConMascara(
                      (valor) => cambiar("referenciaInterna", valor),
                      PLANTILLAS.referencia,
                      PREFIJO_REFERENCIA
                    )}
                    onBlur={() => cambiar("referenciaInterna",
                      completarConsecutivo(formulario.referenciaInterna, PLANTILLAS.referencia))}
                  />
                </div>
                <div className="form-text">Año y consecutivo, por ejemplo {new Date().getFullYear()}-0001. Si escribe menos dígitos, el sistema completa con ceros.</div>
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtOrigenDependencia">
                  Origen o dependencia responsable
                  <Cmp_SIM_AyudaCampo titulo="la dependencia responsable">
                    Escuela, laboratorio, oficina o dependencia que solicita el trámite. Puede
                    escribirla aunque no aparezca entre las sugerencias.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="txtOrigenDependencia"
                  list="listaDependencias"
                  maxLength={150}
                  placeholder="Escuela, laboratorio, oficina o dependencia"
                  value={formulario.origenDependencia}
                  onChange={(evento) => cambiar("origenDependencia", evento.target.value)}
                />
                <datalist id="listaDependencias">
                  {DEPENDENCIAS.map((dependencia) => <option value={dependencia} key={dependencia} />)}
                </datalist>
              </div>

              <div className="col-md-6">
                <label className="form-label" htmlFor="cmbPaisOrigen">
                  País de origen <span className="sim-opcional">(si aplica)</span>
                </label>
                <select
                  className="form-select"
                  id="cmbPaisOrigen"
                  value={formulario.paisOrigen}
                  onChange={(evento) => cambiar("paisOrigen", evento.target.value)}
                >
                  <option value="">No aplica / sin definir</option>
                  {PAISES.map((pais) => <option value={pais} key={pais}>{pais}</option>)}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="numValorEstimado">
                  Valor estimado de la mercancía
                  <Cmp_SIM_AyudaCampo titulo="el valor estimado">
                    Valor aproximado de la mercancía, en la moneda seleccionada. Si todavía no lo
                    conoce con exactitud, registre el dato de la factura o de la guía.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <div className="input-group">
                  <select
                    className="form-select flex-grow-0 w-auto"
                    id="cmbMoneda"
                    aria-label="Moneda"
                    value={formulario.moneda}
                    onChange={(evento) => cambiar("moneda", evento.target.value)}
                  >
                    {MONEDAS.map((moneda) => <option value={moneda} key={moneda}>{moneda}</option>)}
                  </select>
                  <input
                    type="number"
                    className="form-control"
                    id="numValorEstimado"
                    min="0"
                    step="0.01"
                    placeholder="0.00"
                    value={formulario.valorEstimado}
                    onChange={(evento) => cambiar("valorEstimado", evento.target.value)}
                  />
                </div>
              </div>

              <div className="col-md-6">
                <label className="form-label" htmlFor="txtUsuarioResponsable">Usuario responsable</label>
                <input type="text" className="form-control" id="txtUsuarioResponsable"
                       value={USUARIO_ACTUAL} readOnly />
              </div>

              <div className="col-md-6">
                <label className="form-label" htmlFor="txtFechaRegistro">Fecha de registro</label>
                <input type="text" className="form-control" id="txtFechaRegistro"
                       value={fechaRegistro} readOnly />
              </div>

              <div className="col-12">
                <label className="form-label sim-requerido" htmlFor="txtJustificacion">
                  Justificación del trámite
                  <Cmp_SIM_AyudaCampo titulo="la justificación">
                    Explique por qué la mercancía requiere desalmacenaje especial: por ejemplo, equipo
                    retenido en aduana o mercancía que ya es propiedad de la institución.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <textarea
                  className="form-control"
                  id="txtJustificacion"
                  rows={4}
                  maxLength={1000}
                  placeholder="Describa el motivo del desalmacenaje especial."
                  value={formulario.justificacion}
                  onChange={(evento) => cambiar("justificacion", evento.target.value)}
                />
                <div className="form-text text-end">{formulario.justificacion.length}/1000</div>
              </div>

            </div>
          </div>
        </div>

        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-paperclip me-2" aria-hidden="true"></i>
            Documentación del expediente
          </div>
          <div className="card-body">
            <div className="row g-3">
              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="filDocumentosRespaldo">
                  Justificación, acta o documento de respaldo
                  <Cmp_SIM_AyudaCampo titulo="la documentación de respaldo">
                    Puede adjuntar varios archivos a la vez. Si todavía no los tiene, deje el
                    expediente sin documentos: el sistema le recordará lo que falta.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="file" className="form-control" id="filDocumentosRespaldo" multiple
                       onChange={(evento) => seleccionarArchivos("documentosRespaldo", evento.target.files)} />
                <Cmp_SIM_IndicacionArchivos />
                <Cmp_SIM_ListaArchivos archivos={archivos.documentosRespaldo}
                                       alQuitar={(indice) => quitarArchivo("documentosRespaldo", indice)} />
              </div>

              {archivosRechazados && (
                <div className="col-12">
                  <div className="small text-danger" role="alert">{archivosRechazados}</div>
                </div>
              )}

              {[
                { campo: "packingList", etiqueta: "Lista de empaque" },
                { campo: "factura", etiqueta: "Factura o documento equivalente" },
                { campo: "documentoEmbarque", etiqueta: "Documento de embarque" },
              ].map(({ campo, etiqueta }) => (
                <div className="col-12 col-md-6" key={campo}>
                  <label className="form-label" htmlFor={`fil${campo}`}>
                    {etiqueta} <span className="sim-opcional">(si aplica)</span>
                  </label>
                  <input type="file" className="form-control" id={`fil${campo}`}
                         onChange={(evento) => seleccionarArchivos(campo, evento.target.files)} />
                  <Cmp_SIM_ListaArchivos archivos={archivos[campo]}
                                         alQuitar={(indice) => quitarArchivo(campo, indice)} />
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="d-flex justify-content-end gap-2 mb-5">
          <Link className="btn btn-outline-secondary" to="/tramites/expedientes">Ver expedientes</Link>
          <button type="button" className="btn btn-outline-secondary" id="btnLimpiarTramite" onClick={limpiar}>
            Limpiar
          </button>
          <button type="submit" className="btn btn-sim" id="btnRegistrarTramite" disabled={enviando}>
            <i className="bi bi-save me-1" aria-hidden="true"></i>
            {enviando ? "Registrando..." : "Registrar"}
          </button>
        </div>
      </form>

      {expediente && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5">
                    <i className="bi bi-check-circle text-success me-2" aria-hidden="true"></i>
                    Trámite registrado
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setExpediente(null)}></button>
                </div>
                <div className="modal-body">
                  <p>El trámite de desalmacenaje especial se registró y su expediente quedó creado.</p>
                  <dl className="row mb-0">
                    <dt className="col-5">Referencia interna</dt>
                    <dd className="col-7 sim-referencia">{expediente.referenciaInterna}</dd>
                    <dt className="col-5">Estado</dt>
                    <dd className="col-7"><span className="badge text-bg-warning">{expediente.estado}</span></dd>
                    <dt className="col-5">Responsable</dt>
                    <dd className="col-7">{expediente.usuarioResponsable}</dd>
                    <dt className="col-5">Documentos adjuntos</dt>
                    <dd className="col-7">{expediente.cantidadDocumentos} archivo(s)</dd>
                  </dl>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setExpediente(null)}>
                    Registrar otro
                  </button>
                  <Link className="btn btn-sim"
                        to={`/tramites/expedientes/${encodeURIComponent(expediente.referenciaInterna)}`}>
                    Ver expediente
                  </Link>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop fade show"></div>
        </>
      )}
    </Cmp_SIM_Marco>
  );
}

export default Frm_SIM_RegistrarDesalmacenaje;
