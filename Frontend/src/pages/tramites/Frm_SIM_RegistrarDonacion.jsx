import { useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_Documentos, { obtenerPendientes } from "../../componentes/Cmp_SIM_Documentos.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { PLANTILLAS, alEscribirConMascara, completarConsecutivo } from "../../componentes/Cmp_SIM_Mascaras.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { registrarDonacion } from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Angel Salazar Nunez
 * Fecha: 15/09/2026
 * Descripción: Registro de un trámite de donación (HU-024). Solicita el tipo de donación, el
 *   donante, el valor estimado y la documentación del expediente, que exige la resolución o el
 *   acta de aceptación, y crea el expediente en estado Pendiente. No exige contrato SICOP ni
 *   orden de compra.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se integra a main: usa el marco, la ayuda y la documentación comunes, envía el
 *     usuario de la sesión con su token y lleva al expediente de la donación registrada.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: FUNDEVI pasa a llamarse Fundación UCR, su nombre actual. La referencia usa las
 *     siglas de la Unidad: D- para la donación y DF- para la donación por medio de la Fundación UCR.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: comentario de la Unidad: no existen donaciones "nacionales" o "internacionales".
 *     La pantalla ofrece Donación (D) y Donación por medio de la Fundación UCR (DF); el país de
 *     origen queda como dato aparte. Para no cambiar la base, al registrar se envía NACIONAL o
 *     INTERNACIONAL según el país elegido (sin país o Costa Rica = NACIONAL).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción y leyenda de obligatorios (PU-08); PU-06: la referencia completa el
 *     consecutivo con ceros; el aviso lleva la vista hasta el mensaje.
 */

const PREFIJO_REFERENCIA_DONACION = "D-";
const PREFIJO_REFERENCIA_FUNDACION_UCR = "DF-";
const LONGITUD_MAXIMA_REFERENCIA_FUNDACION_UCR = 80;
const LONGITUD_MAXIMA_OBSERVACIONES = 4000;

const TIPO_DONACION_NORMAL = "DONACION";
const TIPO_DONACION_FUNDACION_UCR = "FUNDACION_UCR";
const PAIS_LOCAL = "Costa Rica";

const TIPOS_DONACION = [
  { valor: TIPO_DONACION_NORMAL, nombre: "Donación (D)" },
  { valor: TIPO_DONACION_FUNDACION_UCR, nombre: "Donación por medio de la Fundación UCR (DF)" },
];

/**
 * Traduce el tipo elegido al código que guarda el servidor. La donación normal se guarda como
 * NACIONAL o INTERNACIONAL según su país de origen, para conservar el dato en los reportes.
 *
 * @param {object} formulario datos del formulario
 * @returns {string} código del tipo de donación
 */
function obtenerTipoParaGuardar(formulario) {
  if (formulario.tipoDonacion === TIPO_DONACION_FUNDACION_UCR) {
    return TIPO_DONACION_FUNDACION_UCR;
  }
  return formulario.paisOrigen && formulario.paisOrigen !== PAIS_LOCAL ? "INTERNACIONAL" : "NACIONAL";
}

// Mismos nombres que el catálogo de países: el servidor busca el país por su nombre.
const PAISES = [
  "Alemania", "Brasil", "Canadá", "Chile", "China", "Colombia", "Costa Rica", "España",
  "Estados Unidos", "Francia", "Italia", "Japón", "México", "Países Bajos", "Panamá", "Reino Unido",
];
const MONEDAS = ["CRC", "USD", "EUR"];

const DOCUMENTOS_DONACION = [
  {
    codigo: "resolucionAceptacion",
    nombre: "Resolución o acta de aceptación",
    obligatorio: true,
    ayuda: "Documento con el que la institución acepta la donación. Sin él no se crea el expediente.",
  },
  {
    codigo: "documentosRespaldo",
    nombre: "Documentos de respaldo adicionales",
    obligatorio: false,
    multiple: true,
    ayuda: "Puede adjuntar varios archivos a la vez.",
  },
  { codigo: "packingList", nombre: "Lista de empaque", obligatorio: false },
  { codigo: "factura", nombre: "Factura o documento equivalente", obligatorio: false },
  { codigo: "documentoEmbarque", nombre: "Documento de embarque", obligatorio: false },
];

const AYUDA = {
  titulo: "Ayuda: registrar una donación",
  introduccion:
    "Esta pantalla abre el expediente de una donación recibida por la institución. Las donaciones "
    + "no requieren contrato SICOP ni orden de compra, pero sí la resolución o el acta de aceptación.",
  pasos: [
    "Seleccione el tipo de donación: Donación (D) o por medio de la Fundación UCR (DF).",
    "Digite la referencia interna: el sistema agrega la sigla D-, o DF- si la donación es por medio "
    + "de la Fundación UCR; usted escribe el año y el consecutivo.",
    "Escriba el nombre de la entidad o persona donante.",
    "Si la mercancía viene del extranjero, indique el país de origen; si es por medio de la "
    + "Fundación UCR, la referencia de la Fundación.",
    "Indique la moneda y el valor estimado de la mercancía.",
    "Adjunte la resolución o el acta de aceptación y, si los tiene, los demás documentos.",
    "Presione Crear expediente. El sistema confirma la referencia y el estado inicial.",
  ],
  dudas: [
    {
      pregunta: "¿Puedo registrar la donación sin la resolución de aceptación?",
      respuesta:
        "No. La resolución o el acta de aceptación es el único documento obligatorio. Los demás "
        + "se pueden agregar después desde el expediente.",
    },
    {
      pregunta: "¿Qué sigla lleva la referencia?",
      respuesta:
        "D para la donación normal y DF para la donación por medio de la Fundación UCR, según las "
        + "siglas de la Unidad. La pantalla la agrega al elegir el tipo de donación.",
    },
    {
      pregunta: "¿Cuándo se usa la referencia de la Fundación UCR?",
      respuesta:
        "Solo en las donaciones por medio de la Fundación UCR (antes FUNDEVI). Para los demás tipos "
        + "el campo no se muestra.",
    },
    {
      pregunta: "¿Dónde le doy seguimiento a la donación?",
      respuesta:
        "En la lista de donaciones. Desde ahí se abre el expediente para cambiar su estado, "
        + "agregar documentación y consultar su historial.",
    },
  ],
};

const FORMULARIO_VACIO = {
  referenciaInterna: "",
  tipoDonacion: "",
  nombreDonante: "",
  paisOrigen: "",
  moneda: "CRC",
  valorEstimado: "",
  referenciaFundacionUcr: "",
  observaciones: "",
};

const ARCHIVOS_VACIOS = Object.fromEntries(
  DOCUMENTOS_DONACION.map((documento) => [documento.codigo, []])
);

function Frm_SIM_RegistrarDonacion() {
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [archivos, setArchivos] = useState(ARCHIVOS_VACIOS);
  // Cambia al limpiar para que los campos de archivo se vuelvan a crear vacíos.
  const [versionArchivos, setVersionArchivos] = useState(0);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [faltantes, setFaltantes] = useState([]);
  const [expediente, setExpediente] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";
  const fechaRegistro = new Date().toLocaleDateString("es-CR", {
    day: "2-digit", month: "long", year: "numeric",
  });
  const esFundacionUcr = formulario.tipoDonacion === TIPO_DONACION_FUNDACION_UCR;
  const prefijoReferencia = esFundacionUcr ? PREFIJO_REFERENCIA_FUNDACION_UCR : PREFIJO_REFERENCIA_DONACION;

  function cambiar(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
  }

  /** La referencia de la Fundación UCR solo aplica a las donaciones por medio de la Fundación. */
  function cambiarTipoDonacion(tipoDonacion) {
    setFormulario((anterior) => ({
      ...anterior,
      tipoDonacion,
      referenciaFundacionUcr: tipoDonacion === TIPO_DONACION_FUNDACION_UCR ? anterior.referenciaFundacionUcr : "",
    }));
  }

  /** Revisa lo obligatorio y devuelve, en lenguaje claro, lo que falta. */
  function revisarDatos() {
    const pendientes = [];
    if (!/^\d{4}-\d{4}$/.test(formulario.referenciaInterna)) {
      pendientes.push("la referencia interna, con el formato 2026-0001");
    }
    if (!formulario.tipoDonacion) {
      pendientes.push("el tipo de donación");
    }
    if (!formulario.nombreDonante.trim()) {
      pendientes.push("el nombre de la entidad o persona donante");
    }
    if (esFundacionUcr && !formulario.referenciaFundacionUcr.trim()) {
      pendientes.push("la referencia de la Fundación UCR");
    }
    if (!formulario.valorEstimado || Number(formulario.valorEstimado) <= 0) {
      pendientes.push("el valor estimado de la mercancía, mayor que cero");
    }
    obtenerPendientes(DOCUMENTOS_DONACION, archivos)
      .forEach((documento) => pendientes.push(`la ${documento.nombre.toLowerCase()}`));
    return pendientes;
  }

  function limpiar() {
    setFormulario(FORMULARIO_VACIO);
    setArchivos(ARCHIVOS_VACIOS);
    setVersionArchivos((version) => version + 1);
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
      const respuesta = await registrarDonacion(
        {
          ...formulario,
          tipoDonacion: obtenerTipoParaGuardar(formulario),
          referenciaInterna: prefijoReferencia + formulario.referenciaInterna,
        },
        archivos
      );
      setExpediente(respuesta);
      limpiar();
    } catch (error) {
      setAlerta({ tipo: "danger", texto: `No fue posible registrar la donación. ${error.message}` });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/tramites/donaciones">Donaciones</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Nuevo trámite</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Registrar donación</h1>
          </div>
          <div className="text-md-end">
            <span className="d-block text-muted small">Estado inicial</span>
            <span className="badge text-bg-warning fs-6">Pendiente</span>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Registre una donación recibida por la Universidad con los datos del donante y su documentación. El expediente se crea en estado Pendiente.
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
            <i className="bi bi-gift me-2" aria-hidden="true"></i>
            Datos de la donación
          </div>
          <div className="card-body">
            <div className="row g-3">

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="cmbTipoDonacion">
                  Tipo de donación
                </label>
                <select
                  className="form-select"
                  id="cmbTipoDonacion"
                  value={formulario.tipoDonacion}
                  onChange={(evento) => cambiarTipoDonacion(evento.target.value)}
                >
                  <option value="">Seleccione el tipo de donación</option>
                  {TIPOS_DONACION.map((tipo) => (
                    <option value={tipo.valor} key={tipo.valor}>{tipo.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtReferenciaInterna">
                  Referencia interna
                  <Cmp_SIM_AyudaCampo titulo="la referencia interna">
                    Escriba el <strong>año y el consecutivo</strong>: al digitar 20260001 el sistema
                    lo acomoda como 2026-0001. La pantalla agrega la sigla D-, o DF- si la donación es
                    por medio de la Fundación UCR. La referencia no se puede repetir en otro trámite.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <div className="input-group">
                  <span className="input-group-text sim-referencia">{prefijoReferencia}</span>
                  <input
                    type="text"
                    className="form-control sim-referencia"
                    id="txtReferenciaInterna"
                    value={formulario.referenciaInterna}
                    placeholder={`${new Date().getFullYear()}-0001`}
                    onChange={alEscribirConMascara(
                      (valor) => cambiar("referenciaInterna", valor),
                      PLANTILLAS.referencia,
                      prefijoReferencia
                    )}
                    onBlur={() => cambiar("referenciaInterna",
                      completarConsecutivo(formulario.referenciaInterna, PLANTILLAS.referencia))}
                  />
                </div>
                <div className="form-text">Año y consecutivo, por ejemplo {new Date().getFullYear()}-0001. Si escribe menos dígitos, el sistema completa con ceros.</div>
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtNombreDonante">
                  Entidad o persona donante
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="txtNombreDonante"
                  maxLength={150}
                  placeholder="Nombre de la entidad o persona donante"
                  value={formulario.nombreDonante}
                  onChange={(evento) => cambiar("nombreDonante", evento.target.value)}
                />
              </div>

              {formulario.tipoDonacion && (
                <div className="col-md-6">
                  <label className="form-label" htmlFor="cmbPaisOrigen">
                    País de origen <span className="sim-opcional">(opcional)</span>
                    <Cmp_SIM_AyudaCampo titulo="el país de origen">
                      País desde donde llega la mercancía. Déjelo sin definir si la donación es local.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <select
                    className="form-select"
                    id="cmbPaisOrigen"
                    value={formulario.paisOrigen}
                    onChange={(evento) => cambiar("paisOrigen", evento.target.value)}
                  >
                    <option value="">Sin definir</option>
                    {PAISES.map((pais) => <option value={pais} key={pais}>{pais}</option>)}
                  </select>
                </div>
              )}

              {esFundacionUcr && (
                <div className="col-md-6">
                  <label className="form-label sim-requerido" htmlFor="txtReferenciaFundacionUcr">
                    Referencia de la Fundación UCR
                    <Cmp_SIM_AyudaCampo titulo="la referencia de la Fundación UCR">
                      Referencia con la que la Fundación UCR identifica la donación. Solo se solicita
                      en las donaciones por medio de la Fundación UCR.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input
                    type="text"
                    className="form-control"
                    id="txtReferenciaFundacionUcr"
                    maxLength={LONGITUD_MAXIMA_REFERENCIA_FUNDACION_UCR}
                    placeholder="Referencia asignada por la Fundación UCR"
                    value={formulario.referenciaFundacionUcr}
                    onChange={(evento) => cambiar("referenciaFundacionUcr", evento.target.value)}
                  />
                </div>
              )}

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="numValorEstimado">
                  Valor estimado de la mercancía
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
                       value={usuarioActual} readOnly />
              </div>

              <div className="col-md-6">
                <label className="form-label" htmlFor="txtFechaRegistro">Fecha de registro</label>
                <input type="text" className="form-control" id="txtFechaRegistro"
                       value={fechaRegistro} readOnly />
              </div>

              <div className="col-12">
                <label className="form-label" htmlFor="txtObservaciones">
                  Observaciones <span className="sim-opcional">(si aplica)</span>
                </label>
                <textarea
                  className="form-control"
                  id="txtObservaciones"
                  rows={3}
                  maxLength={LONGITUD_MAXIMA_OBSERVACIONES}
                  placeholder="Agregue cualquier observación relevante sobre la donación."
                  value={formulario.observaciones}
                  onChange={(evento) => cambiar("observaciones", evento.target.value)}
                />
                <div className="form-text text-end">
                  {formulario.observaciones.length}/{LONGITUD_MAXIMA_OBSERVACIONES}
                </div>
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
            <Cmp_SIM_Documentos key={versionArchivos} documentos={DOCUMENTOS_DONACION}
                                archivos={archivos} alCambiar={setArchivos} />
            <div className="alert alert-info mt-3 mb-0 py-2" role="note">
              <i className="bi bi-info-circle me-2" aria-hidden="true"></i>
              Las donaciones no requieren contrato SICOP ni orden de compra.
            </div>
          </div>
        </div>

        <div className="d-flex justify-content-end gap-2 mb-5">
          <Link className="btn btn-outline-secondary" to="/tramites/donaciones">Ver donaciones</Link>
          <button type="button" className="btn btn-outline-secondary" id="btnLimpiarDonacion" onClick={limpiar}>
            Limpiar
          </button>
          <button type="submit" className="btn btn-sim" id="btnRegistrarDonacion" disabled={enviando}>
            <i className="bi bi-save me-1" aria-hidden="true"></i>
            {enviando ? "Registrando..." : "Crear expediente"}
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
                    Donación registrada
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setExpediente(null)}></button>
                </div>
                <div className="modal-body">
                  <p>{expediente.mensaje}</p>
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
                    Registrar otra
                  </button>
                  <Link className="btn btn-sim"
                        to={`/tramites/donaciones/${encodeURIComponent(expediente.referenciaInterna)}`}>
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

export default Frm_SIM_RegistrarDonacion;
