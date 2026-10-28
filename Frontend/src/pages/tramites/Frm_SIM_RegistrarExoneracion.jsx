import { useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import Cmp_SIM_Documentos, {
  contarArchivos,
  obtenerPendientes,
} from "../../componentes/Cmp_SIM_Documentos.jsx";
import { PLANTILLAS, alEscribirConMascara, completarConsecutivo } from "../../componentes/Cmp_SIM_Mascaras.js";
import { registrarExoneracion } from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Registro de una solicitud de exoneración (HU-018). Crea el expediente con sus
 *   datos contractuales y su documentación de respaldo, advierte la documentación obligatoria
 *   que falta y permite registrar la solicitud aun con pendientes, para adjuntarla después.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 01/10/2026
 *   Descripción: Cancelar vuelve al menú de trámites. (25/10/2026: vuelve a la lista de exoneraciones.)
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: comentarios de la Unidad: el contrato SICOP es opcional (hay exoneraciones que no
 *     van por SICOP), se quitan los permisos regulatorios y se agregan el expediente de SICOP y
 *     otros documentos, ambos con varios archivos. Se corrige la sigla de la ayuda (EXO-).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción y leyenda de obligatorios (PU-08); PU-06: la referencia completa el
 *     consecutivo con ceros; PU-11: "Descripción del bien exonerado"; se aclara que los documentos
 *     obligatorios se piden para la aprobación; Cancelar vuelve a la lista de exoneraciones; el
 *     aviso lleva la vista hasta el mensaje.
 */

const PREFIJO_REFERENCIA = "EXO-";
const USUARIO_ACTUAL = "Técnica de Exoneraciones";

const TIPOS_EXONERACION = [
  { codigo: "COMPRA_INSTITUCIONAL", nombre: "Compra institucional" },
  { codigo: "DONACION", nombre: "Donación" },
  { codigo: "CLIENTE_EXTERNO", nombre: "Cliente externo con contrato" },
  { codigo: "VEHICULOS", nombre: "Vehículos" },
  { codigo: "MOVIM_TRASPASO", nombre: "Movimiento de traspaso" },
];

const DOCUMENTOS_EXONERACION = [
  {
    codigo: "documentoEmbarque",
    nombre: "Documento de embarque",
    obligatorio: true,
    ayuda: "Guía aérea, conocimiento de embarque o carta de porte.",
  },
  { codigo: "facturaComercial", nombre: "Factura comercial", obligatorio: true },
  { codigo: "formularioS1", nombre: "Formulario S1", obligatorio: true },
  { codigo: "listaEmpaque", nombre: "Lista de empaque", obligatorio: true },
  {
    codigo: "exonet",
    nombre: "EXONET",
    obligatorio: false,
    ayuda: "Formulario de EXONET; adjúntelo cuando ya cuente con la solicitud o la autorización.",
  },
  {
    codigo: "traduccionFactura",
    nombre: "Traducción de factura",
    obligatorio: false,
    ayuda: "Solo cuando la factura no está en español.",
  },
  {
    codigo: "expedienteSicop",
    nombre: "Expediente de SICOP",
    obligatorio: false,
    multiple: true,
    ayuda: "El expediente de la contratación en PDF, para que la jefatura lo revise.",
  },
  {
    codigo: "otrosDocumentos",
    nombre: "Otros documentos",
    obligatorio: false,
    multiple: true,
    ayuda: "Correos, oficios u otro respaldo que la jefatura necesite ver.",
  },
];

const AYUDA = {
  titulo: "Ayuda: registrar una solicitud de exoneración",
  introduccion:
    "La solicitud de exoneración inicia el trámite de exención de impuestos de una importación "
    + "institucional. Al registrarla se crea su expediente y toda acción queda anotada en la bitácora.",
  pasos: [
    "Digite la referencia interna: el sistema agrega el prefijo EXO-, usted escribe el año y el consecutivo.",
    "Elija el tipo de exoneración según el origen de la mercancía.",
    "Complete los datos contractuales. Solo el número de contratación es obligatorio: el contrato "
    + "SICOP (no todas las exoneraciones van por SICOP), la orden de compra GECO y los números de "
    + "EXONET se pueden llenar después.",
    "Adjunte la documentación de respaldo. Los campos marcados con asterisco son los obligatorios.",
    "Presione Registrar. Si falta documentación obligatoria, el sistema la advierte y usted decide "
    + "si registra la solicitud igual.",
  ],
  dudas: [
    {
      pregunta: "¿Qué sigla usa la Unidad para estos trámites?",
      respuesta:
        "Las exoneraciones se identifican con la sigla EXO, tal como las registra la Unidad de "
        + "Importaciones. El desalmacenaje especial usa DE.",
    },
    {
      pregunta: "¿Puedo registrar sin toda la documentación?",
      respuesta:
        "Sí. El sistema advierte cuáles documentos obligatorios faltan y permite registrar la "
        + "solicitud; el expediente queda marcado con la documentación pendiente.",
    },
    {
      pregunta: "¿Dónde queda el registro?",
      respuesta:
        "En la bitácora del expediente, junto con el usuario responsable, la fecha y la hora. Ahí se "
        + "anotan también los documentos que se agreguen después.",
    },
  ],
};

const FORMULARIO_VACIO = {
  referenciaInterna: "",
  tipoExoneracion: "",
  numeroContratacion: "",
  ordenCompraGeco: "",
  contratoSicop: "",
  numeroExonet: "",
  numeroAutorizacionExonet: "",
  observaciones: "",
};

const ARCHIVOS_VACIOS = Object.fromEntries(
  DOCUMENTOS_EXONERACION.map((documento) => [documento.codigo, []])
);

function Frm_SIM_RegistrarExoneracion() {
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [archivos, setArchivos] = useState(ARCHIVOS_VACIOS);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [faltantes, setFaltantes] = useState([]);
  const [pendientes, setPendientes] = useState(null);
  const [solicitud, setSolicitud] = useState(null);
  const [enviando, setEnviando] = useState(false);

  function cambiar(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function revisarDatos() {
    const faltas = [];
    if (!/^\d{4}-\d{4}$/.test(formulario.referenciaInterna)) {
      faltas.push("la referencia interna, con el formato 2026-0001");
    }
    if (!formulario.tipoExoneracion) {
      faltas.push("el tipo de exoneración");
    }
    if (!formulario.numeroContratacion.trim()) {
      faltas.push("el número de contratación");
    }
    return faltas;
  }

  function limpiar() {
    setFormulario(FORMULARIO_VACIO);
    setArchivos(ARCHIVOS_VACIOS);
    setFaltantes([]);
    setAlerta(null);
  }

  async function enviarSolicitud() {
    setEnviando(true);
    setPendientes(null);
    try {
      const respuesta = await registrarExoneracion(
        { ...formulario, referenciaInterna: PREFIJO_REFERENCIA + formulario.referenciaInterna },
        archivos
      );
      setSolicitud(respuesta);
      limpiar();
    } catch (problema) {
      setAlerta({
        tipo: "danger",
        texto: `No fue posible registrar la solicitud. ${problema.message} Revise los datos e intente de nuevo.`,
      });
    } finally {
      setEnviando(false);
    }
  }

  function registrar(evento) {
    evento.preventDefault();
    setAlerta(null);

    const faltas = revisarDatos();
    setFaltantes(faltas);
    if (faltas.length > 0) {
      setAlerta({ tipo: "danger", texto: "Falta completar la información que se indica abajo." });
      return;
    }

    const documentosPendientes = obtenerPendientes(DOCUMENTOS_EXONERACION, archivos);
    if (documentosPendientes.length > 0) {
      setPendientes(documentosPendientes.map((documento) => documento.nombre));
      return;
    }

    enviarSolicitud();
  }

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">Registro de solicitud de exoneración</h1>
          </div>
          <div className="text-md-end">
            <span className="d-block text-muted small">Estado inicial</span>
            <span className="badge text-bg-warning fs-6">Pendiente revisión documental</span>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Registre una solicitud de exoneración de impuestos con sus datos contractuales y su documentación. Puede registrarla aunque falten documentos y adjuntarlos después desde el expediente.
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
        <div className="card sim-card mb-4">
          <div className="card-header">
            <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>Datos de la solicitud
          </div>
          <div className="card-body">
            <div className="row g-3">

              <div className="col-12 col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtReferenciaInterna">
                  Referencia interna del trámite
                  <Cmp_SIM_AyudaCampo titulo="la referencia interna">
                    Escriba el <strong>año y el consecutivo</strong>: al digitar 20260001 el sistema lo
                    acomoda como 2026-0001. El prefijo EXO- lo agrega la pantalla y la referencia no se
                    puede repetir en otro trámite.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <div className="input-group">
                  <span className="input-group-text sim-referencia">{PREFIJO_REFERENCIA}</span>
                  <input type="text" className="form-control sim-referencia" id="txtReferenciaInterna"
                         placeholder={`${new Date().getFullYear()}-0001`}
                         value={formulario.referenciaInterna}
                         onChange={alEscribirConMascara(
                           (valor) => cambiar("referenciaInterna", valor),
                           PLANTILLAS.referencia,
                           PREFIJO_REFERENCIA
                         )}
                         onBlur={() => cambiar("referenciaInterna",
                           completarConsecutivo(formulario.referenciaInterna, PLANTILLAS.referencia))} />
                </div>
                <div className="form-text">Año y consecutivo, por ejemplo {new Date().getFullYear()}-0001. Si escribe menos dígitos, el sistema completa con ceros.</div>
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label sim-requerido" htmlFor="cmbTipoExoneracion">
                  Tipo de exoneración
                  <Cmp_SIM_AyudaCampo titulo="el tipo de exoneración">
                    Compra institucional para adquisiciones de la Universidad, donación cuando la
                    mercancía proviene de un tercero, y cliente externo con contrato cuando el trámite
                    se hace para una entidad con convenio.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <select className="form-select" id="cmbTipoExoneracion" value={formulario.tipoExoneracion}
                        onChange={(evento) => cambiar("tipoExoneracion", evento.target.value)}>
                  <option value="">Seleccione el tipo de exoneración</option>
                  {TIPOS_EXONERACION.map((tipo) => (
                    <option value={tipo.codigo} key={tipo.codigo}>{tipo.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtNumeroContratacion">
                  Número de contratación
                  <Cmp_SIM_AyudaCampo titulo="el número de contratación">
                    Número del procedimiento de contratación, por ejemplo 2026LE-000123-0000900001.
                    Al digitarlo, la pantalla acomoda los guiones.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="text" className="form-control sim-referencia" id="txtNumeroContratacion"
                       placeholder="2026LE-000123-0000900001"
                       value={formulario.numeroContratacion}
                       onChange={alEscribirConMascara(
                         (valor) => cambiar("numeroContratacion", valor),
                         PLANTILLAS.contratacion
                       )} />
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="txtOrdenCompraGeco">
                  Orden de compra GECO <span className="sim-opcional">(opcional)</span>
                  <Cmp_SIM_AyudaCampo titulo="la orden de compra GECO">
                    Número de la orden que genera GECO. En las exoneraciones no siempre existe al
                    registrar, así que puede dejarlo en blanco y agregarlo después desde el expediente.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="text" className="form-control sim-referencia" id="txtOrdenCompraGeco"
                       placeholder="2026-000456"
                       value={formulario.ordenCompraGeco}
                       onChange={alEscribirConMascara(
                         (valor) => cambiar("ordenCompraGeco", valor),
                         PLANTILLAS.ordenCompra
                       )} />
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="txtContratoSicop">
                  Contrato SICOP <span className="sim-opcional">(opcional)</span>
                  <Cmp_SIM_AyudaCampo titulo="el contrato SICOP">
                    Número de contrato que genera SICOP para la contratación. Déjelo en blanco si la
                    exoneración no se tramita por SICOP.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="text" className="form-control sim-referencia" id="txtContratoSicop"
                       placeholder="2026-000789"
                       value={formulario.contratoSicop}
                       onChange={alEscribirConMascara(
                         (valor) => cambiar("contratoSicop", valor),
                         PLANTILLAS.contrato
                       )} />
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="txtNumeroExonet">
                  Número de solicitud EXONET <span className="sim-opcional">(opcional)</span>
                  <Cmp_SIM_AyudaCampo titulo="el número de solicitud EXONET">
                    Primer número que devuelve EXONET al presentar la solicitud. Sirve para darle
                    seguimiento mientras Hacienda la resuelve.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="text" className="form-control sim-referencia" id="txtNumeroExonet"
                       placeholder="2026-118745" maxLength={45}
                       value={formulario.numeroExonet}
                       onChange={(evento) => cambiar("numeroExonet", evento.target.value.toUpperCase())} />
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="txtNumeroAutorizacionExonet">
                  Número de autorización EXONET <span className="sim-opcional">(opcional)</span>
                  <Cmp_SIM_AyudaCampo titulo="el número de autorización EXONET">
                    Segundo número que devuelve EXONET cuando la exoneración queda autorizada. Es el
                    que se presenta en aduana.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="text" className="form-control sim-referencia" id="txtNumeroAutorizacionExonet"
                       placeholder="AUT-2026-9981" maxLength={45}
                       value={formulario.numeroAutorizacionExonet}
                       onChange={(evento) =>
                         cambiar("numeroAutorizacionExonet", evento.target.value.toUpperCase())} />
              </div>

              <div className="col-12 col-md-6">
                <label className="form-label" htmlFor="txtUsuarioResponsable">Usuario responsable</label>
                <input type="text" className="form-control" id="txtUsuarioResponsable"
                       value={USUARIO_ACTUAL} readOnly />
              </div>

              <div className="col-12">
                <label className="form-label" htmlFor="txtObservaciones">
                  Descripción del bien exonerado <span className="sim-opcional">(opcional)</span>
                </label>
                <textarea className="form-control" id="txtObservaciones" rows={3} maxLength={4000}
                          placeholder="Ej.: 2 microscopios de fluorescencia para el laboratorio de biología."
                          value={formulario.observaciones}
                          onChange={(evento) => cambiar("observaciones", evento.target.value)} />
                <div className="form-text text-end">{formulario.observaciones.length}/4000</div>
              </div>

            </div>
          </div>
        </div>

        <div className="card sim-card mb-4">
          <div className="card-header d-flex justify-content-between align-items-center">
            <span><i className="bi bi-paperclip me-2" aria-hidden="true"></i>Documentación de respaldo</span>
            <span className="badge text-bg-light text-dark">{contarArchivos(archivos)} archivo(s)</span>
          </div>
          <div className="card-body">
            {/* Prueba T04: no quedaba claro si los obligatorios se piden para registrar o para aprobar. */}
            <p className="small text-muted" id="lblAclaracionObligatorios">
              Los documentos marcados con <span className="text-danger fw-bold">*</span> son obligatorios para
              enviar la solicitud a aprobación. Puede registrar la solicitud sin ellos y adjuntarlos después
              desde el expediente.
            </p>
            <Cmp_SIM_Documentos documentos={DOCUMENTOS_EXONERACION} archivos={archivos}
                                alCambiar={setArchivos} />
          </div>
        </div>

        <div className="d-flex justify-content-end gap-2 mb-5">
          <Link className="btn btn-outline-secondary" to="/tramites/exoneraciones">Cancelar</Link>
          <button type="submit" className="btn btn-sim" id="btnRegistrarExoneracion" disabled={enviando}>
            <i className="bi bi-save me-1" aria-hidden="true"></i>
            {enviando ? "Registrando..." : "Registrar"}
          </button>
        </div>
      </form>

      {pendientes && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5">
                    <i className="bi bi-exclamation-triangle text-warning me-2" aria-hidden="true"></i>
                    Documentación obligatoria pendiente
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setPendientes(null)}></button>
                </div>
                <div className="modal-body">
                  <p>Falta adjuntar la siguiente documentación obligatoria:</p>
                  <ul>
                    {pendientes.map((nombre) => <li key={nombre}>{nombre}</li>)}
                  </ul>
                  <p className="mb-0">
                    Puede registrar la solicitud y adjuntarla después desde el expediente.
                  </p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary" id="btnAdjuntarAhora"
                          onClick={() => setPendientes(null)}>
                    Adjuntar ahora
                  </button>
                  <button type="button" className="btn btn-sim" id="btnRegistrarConPendientes"
                          onClick={enviarSolicitud} disabled={enviando}>
                    Registrar con pendientes
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop fade show"></div>
        </>
      )}

      {solicitud && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5">
                    <i className="bi bi-check-circle text-success me-2" aria-hidden="true"></i>
                    Solicitud registrada
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setSolicitud(null)}></button>
                </div>
                <div className="modal-body">
                  <p>La solicitud de exoneración se registró y su expediente quedó creado.</p>
                  <dl className="row mb-0">
                    <dt className="col-5">Referencia interna</dt>
                    <dd className="col-7 sim-referencia">{solicitud.referenciaInterna}</dd>
                    <dt className="col-5">Tipo</dt>
                    <dd className="col-7">{solicitud.tipoExoneracion}</dd>
                    <dt className="col-5">Estado</dt>
                    <dd className="col-7"><span className="badge text-bg-warning">{solicitud.estado}</span></dd>
                    <dt className="col-5">Responsable</dt>
                    <dd className="col-7">{solicitud.usuarioResponsable}</dd>
                    <dt className="col-5">Documentos adjuntos</dt>
                    <dd className="col-7">{solicitud.cantidadDocumentos} archivo(s)</dd>
                  </dl>

                  {(solicitud.documentosPendientes ?? []).length > 0 && (
                    <div className="alert alert-warning mt-3 mb-0 small">
                      Documentación pendiente: {solicitud.documentosPendientes.join(", ")}.
                    </div>
                  )}
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary" id="btnRegistrarOtraExoneracion"
                          onClick={() => setSolicitud(null)}>
                    Registrar otra solicitud
                  </button>
                  <Link className="btn btn-sim"
                        to={`/tramites/exoneraciones/${encodeURIComponent(solicitud.referenciaInterna)}`}>
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

export default Frm_SIM_RegistrarExoneracion;
