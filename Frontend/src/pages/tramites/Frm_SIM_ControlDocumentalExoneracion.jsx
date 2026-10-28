import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso, {
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  continuarAprobacionExoneracion,
  obtenerControlDocumental,
  obtenerExoneracion,
} from "../../servicios/Srv_SIM_Tramites.js";

import "./Frm_SIM_ControlDocumentalExoneracion.css";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Control de documentos obligatorios de una solicitud de exoneración (HU-021).
 *   Valida la existencia de los documentos que exige el tipo de trámite, lista los pendientes,
 *   confirma el cumplimiento documental y solo permite continuar a aprobación cuando toda la
 *   documentación obligatoria fue adjuntada y recibida en la revisión documental (HU-019).
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *   05/10/2026 Equipo de Desarrollo SIM: con la HU-022 el expediente puede estar aprobado,
 *     rechazado o pendiente de modificación; el mensaje indica el estado real del expediente.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Adjuntar documentos y continuar a aprobación exigen
 *     EXONERACION_MODIFICAR; la revisión documental, EXONERACION_CONSULTAR. Quien solo aprueba ve el
 *     control sin esas acciones.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla; la ruta de navegación lleva a la lista de
 *     exoneraciones; el aviso lleva la vista hasta el mensaje.
 */

const USUARIO_ACTUAL = "Técnica de Exoneraciones";

const NOMBRES_TIPO = {
  COMPRA_INSTITUCIONAL: "Compra institucional",
  DONACION: "Donación",
  CLIENTE_EXTERNO: "Cliente externo con contrato",
  VEHICULOS: "Vehículos",
  MOVIM_TRASPASO: "Movimiento de traspaso",
};

const ESTADOS_CONTROL = {
  CUMPLIDO: { texto: "Recibido", clase: "text-bg-success", icono: "bi-check-circle-fill text-success" },
  FALTANTE: { texto: "Faltante", clase: "text-bg-danger", icono: "bi-exclamation-circle-fill text-danger" },
  POR_REVISAR: {
    texto: "Pendiente de revisión", clase: "text-bg-warning", icono: "bi-hourglass-split text-warning",
  },
  RECHAZADO: { texto: "Rechazado", clase: "text-bg-danger", icono: "bi-x-circle-fill text-danger" },
};

const AYUDA = {
  titulo: "Ayuda: control de documentos obligatorios",
  introduccion:
    "Esta pantalla valida que el expediente tenga todos los documentos que exige su tipo de trámite "
    + "antes de continuar a aprobación.",
  pasos: [
    "Revise la lista de requisitos: cada documento obligatorio indica si ya se cumple.",
    "Un documento se cumple cuando está adjunto y se marcó como Recibido en la revisión documental.",
    "Use Adjuntar documentos para cargar los faltantes desde el expediente.",
    "Use Revisión documental para marcar como Recibido los documentos adjuntos.",
    "Cuando no queden pendientes, use Continuar a aprobación.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué no puedo continuar a aprobación?",
      respuesta:
        "Porque al menos un documento obligatorio falta, no se ha revisado o fue rechazado. "
        + "La lista de pendientes indica cuál y por qué.",
    },
    {
      pregunta: "¿Los documentos opcionales impiden continuar?",
      respuesta:
        "No. EXONET, los permisos regulatorios y la traducción de la factura se muestran como "
        + "referencia, pero solo los obligatorios se exigen.",
    },
    {
      pregunta: "¿Queda registro del envío a aprobación?",
      respuesta:
        "Sí. El cambio de estado queda en el historial y en la bitácora con la fecha, la hora y "
        + "el usuario responsable.",
    },
  ],
};

function normalizarIdTramite(valor) {
  const numero = Number(valor);
  return Number.isInteger(numero) && numero > 0 ? numero : null;
}

function formatearFecha(valor) {
  return valor
    ? new Date(valor).toLocaleDateString("es-CR", { day: "2-digit", month: "2-digit", year: "numeric" })
    : "";
}

/**
 * Une los nombres con comas y una "y" final: "A, B y C".
 */
function unirNombres(nombres) {
  if (nombres.length <= 1) {
    return nombres.join("");
  }
  return `${nombres.slice(0, -1).join(", ")} y ${nombres[nombres.length - 1]}`;
}

function Frm_SIM_ControlDocumentalExoneracion() {
  // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
  const { tieneAlguno } = useSesionActual();
  const puedeModificar = tieneAlguno([PERMISOS.EXONERACION_MODIFICAR]);
  // Quien solo aprueba ve el control, pero no la revisión documental.
  const puedeConsultar = tieneAlguno([PERMISOS.EXONERACION_CONSULTAR]);
  const { referenciaInterna } = useParams();
  const { state: estadoNavegacion } = useLocation();
  const idTramiteNavegacion = normalizarIdTramite(estadoNavegacion?.idTramite);

  const [control, setControl] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [confirmando, setConfirmando] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const responsable = obtenerSesion()?.nombreCompleto || USUARIO_ACTUAL;

  /*
   * El idTramite llega en el estado de navegación. Si la página se recargó o se abrió
   * directamente, se recupera mediante la referencia interna.
   */
  useEffect(() => {
    let vigente = true;

    async function cargar() {
      setCargando(true);
      setErrorCarga(null);
      try {
        let id = idTramiteNavegacion;
        if (id === null) {
          const detalle = await obtenerExoneracion(referenciaInterna);
          id = normalizarIdTramite(detalle?.idTramite);
          if (id === null) {
            throw new Error("El expediente no indica su identificador.");
          }
        }
        const respuesta = await obtenerControlDocumental(id);
        if (vigente) {
          setControl(respuesta);
        }
      } catch (problema) {
        if (vigente) {
          setErrorCarga(`No fue posible cargar el control documental. ${problema.message}`);
        }
      } finally {
        if (vigente) {
          setCargando(false);
        }
      }
    }

    cargar();
    return () => {
      vigente = false;
    };
  }, [referenciaInterna, idTramiteNavegacion, intentoCarga]);

  async function continuarAprobacion() {
    setAlerta(null);
    setEnviando(true);
    try {
      setControl(await continuarAprobacionExoneracion(control.idTramite));
      setAlerta({
        tipo: "success",
        texto: "La documentación obligatoria está completa: el expediente pasó a \"Pendiente de "
          + "aprobación\" y el cambio quedó anotado en la bitácora.",
      });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: problema.message });
      // La documentación pudo cambiar mientras se revisaba: se vuelve a evaluar.
      try {
        setControl(await obtenerControlDocumental(control.idTramite));
      } catch {
        // Se conserva el control anterior; el mensaje del servidor ya se mostró.
      }
    } finally {
      setConfirmando(false);
      setEnviando(false);
    }
  }

  const rutaExpediente = `/tramites/exoneraciones/${encodeURIComponent(referenciaInterna)}`;
  const estadoEnlace = { idTramite: control?.idTramite, estado: control?.estadoExpediente };

  const migas = (
    <nav aria-label="Ruta de navegación">
      <ol className="breadcrumb small">
        <li className="breadcrumb-item"><Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CONSULTAR]} alterno="Exoneraciones"><Link to="/tramites/exoneraciones">Exoneraciones</Link></Cmp_SIM_ConPermiso></li>
        <li className="breadcrumb-item">
          <Link to={rutaExpediente} state={estadoEnlace}>{referenciaInterna}</Link>
        </li>
        <li className="breadcrumb-item active" aria-current="page">Control documental</li>
      </ol>
    </nav>
  );

  if (cargando || errorCarga) {
    return (
      <Cmp_SIM_Marco usuario={responsable} ayuda={AYUDA}>
        {migas}
        {cargando ? (
          <p className="text-muted">Cargando el control documental...</p>
        ) : (
          <>
            <div className="alert alert-danger" role="alert">{errorCarga}</div>
            <div className="d-flex gap-2">
              <Link className="btn btn-outline-secondary" to={rutaExpediente}>Volver al expediente</Link>
              <button type="button" className="btn btn-sim" id="btnReintentarControl"
                      onClick={() => setIntentoCarga((intento) => intento + 1)}>
                <i className="bi bi-arrow-clockwise me-1" aria-hidden="true"></i>
                Reintentar
              </button>
            </div>
          </>
        )}
      </Cmp_SIM_Marco>
    );
  }

  const obligatorios = control.requisitos.filter((requisito) => requisito.obligatorio);
  const opcionales = control.requisitos.filter((requisito) => !requisito.obligatorio);
  const completo = control.cumpleDocumentacion === true;
  // Documentación completa pero sin poder enviarse: el expediente ya está en aprobación o la
  // jefatura ya decidió (HU-022).
  const enAprobacion = completo && !control.puedeContinuarAprobacion;
  const textoNoEnvio = control.estadoExpediente === "Pendiente de aprobación"
    ? "El expediente ya continuó a aprobación."
    : `El expediente no puede enviarse a aprobación en su estado actual (${control.estadoExpediente}).`;
  const nombresPendientes = obligatorios
    .filter((requisito) => requisito.estadoControl !== "CUMPLIDO")
    .map((requisito) => requisito.nombreTipoDocumento);

  function renderRequisito(requisito) {
    const estado = ESTADOS_CONTROL[requisito.estadoControl] ?? ESTADOS_CONTROL.FALTANTE;
    const pendiente = requisito.obligatorio && requisito.estadoControl !== "CUMPLIDO";
    return (
      <li key={requisito.idTipoDocumento}
          className={`list-group-item d-flex flex-wrap justify-content-between align-items-center gap-2 ${
            pendiente ? "sim-control-pendiente" : ""}`}>
        <span className="d-flex align-items-center gap-2">
          <i className={`bi ${requisito.obligatorio ? estado.icono : "bi-dash-circle text-secondary"}`}
             aria-hidden="true"></i>
          <span>
            <span className="fw-semibold">{requisito.nombreTipoDocumento}</span>
            {requisito.idDocumento && (
              <small className="d-block text-muted text-break">
                {requisito.nombreArchivo}
                {requisito.fechaCarga && ` · ${formatearFecha(requisito.fechaCarga)}`}
              </small>
            )}
          </span>
        </span>
        <span className={`badge ${requisito.obligatorio ? estado.clase : "text-bg-light text-dark border"}`}>
          {requisito.obligatorio || requisito.idDocumento ? estado.texto : "No adjuntado"}
        </span>
      </li>
    );
  }

  return (
    <Cmp_SIM_Marco usuario={responsable} ayuda={AYUDA}>
      {migas}

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">
            Control de documentos obligatorios{" "}
            <span className="sim-referencia">{referenciaInterna}</span>
          </h1>
          <div className="d-flex flex-wrap gap-2">
            {control.estadoExpediente && (
              <span className="badge text-bg-secondary fs-6">{control.estadoExpediente}</span>
            )}
            <span className={`badge fs-6 ${completo ? "text-bg-success" : "text-bg-danger"}`}
                  id="lblEstadoDocumental">
              {completo ? "Documentación completa" : "Documentación incompleta"}
            </span>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Verifique que el expediente tenga recibidos todos los documentos obligatorios antes de enviarlo a la aprobación de la jefatura.
        </p>
      </section>

      {alerta && (
        <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>
      )}

      <div className="card sim-card mb-4" id="seccionControlDocumental">
        <div className="card-header">
          <i className="bi bi-list-check me-2" aria-hidden="true"></i>
          Requisitos según el tipo de trámite
        </div>
        <div className="card-body">
          <div className="row g-3 mb-4">
            <div className="col-12 col-md-6">
              <label className="form-label" htmlFor="txtReferenciaControl">Referencia interna</label>
              <input type="text" className="form-control sim-referencia" id="txtReferenciaControl"
                     value={control.referenciaInterna ?? referenciaInterna} readOnly />
            </div>
            <div className="col-12 col-md-6">
              <label className="form-label" htmlFor="txtTipoTramiteControl">
                Tipo de trámite
                <Cmp_SIM_AyudaCampo titulo="el tipo de trámite">
                  Es la modalidad de exoneración definida al registrar la solicitud. Determina los
                  documentos obligatorios que se exigen.
                </Cmp_SIM_AyudaCampo>
              </label>
              <input type="text" className="form-control" id="txtTipoTramiteControl"
                     value={`Exoneración · ${NOMBRES_TIPO[control.tipoExoneracion] ?? control.tipoExoneracion ?? "—"}`}
                     readOnly />
            </div>
          </div>

          <h2 className="h6 text-muted">Documentos obligatorios</h2>
          {obligatorios.length === 0 ? (
            <p className="text-muted small">
              No hay documentos obligatorios configurados para este tipo de exoneración.
            </p>
          ) : (
            <ul className="list-group mb-3" id="lstDocumentosObligatorios">
              {obligatorios.map(renderRequisito)}
            </ul>
          )}

          {opcionales.length > 0 && (
            <>
              <h2 className="h6 text-muted mt-4">
                Documentos opcionales <span className="fw-normal small">· no impiden continuar</span>
              </h2>
              <ul className="list-group mb-3" id="lstDocumentosOpcionales">
                {opcionales.map(renderRequisito)}
              </ul>
            </>
          )}

          {obligatorios.length > 0 && (completo ? (
            <div className="alert alert-success mb-0" role="status">
              Cumplimiento documental confirmado: los {control.totalObligatorios} documentos obligatorios
              están adjuntos y recibidos.
              {enAprobacion && ` ${textoNoEnvio}`}
            </div>
          ) : (
            <div className="alert alert-danger mb-0" role="alert" id="lblDocumentosPendientes">
              <p className="mb-1">
                {control.obligatoriosPendientes === 1
                  ? `Falta 1 documento obligatorio: ${unirNombres(nombresPendientes)}.`
                  : `Faltan ${control.obligatoriosPendientes} documentos obligatorios: `
                    + `${unirNombres(nombresPendientes)}.`}
                {" "}La aprobación se habilita cuando el expediente esté completo.
              </p>
              <ul className="mb-0 small">
                {control.documentosPendientes.map((pendiente) => <li key={pendiente}>{pendiente}</li>)}
              </ul>
            </div>
          ))}

          <div className="row g-2 mt-3 text-center">
            <div className="col-4">
              <div className="sim-control-indicador">
                <div className="sim-control-valor">{control.obligatoriosCumplidos}</div>
                <div className="small text-muted">documentos recibidos</div>
              </div>
            </div>
            <div className="col-4">
              <div className={`sim-control-indicador ${control.obligatoriosPendientes > 0 ? "sim-control-alerta" : ""}`}>
                <div className="sim-control-valor">{control.obligatoriosPendientes}</div>
                <div className="small">pendientes</div>
              </div>
            </div>
            <div className="col-4">
              <div className="sim-control-indicador">
                <div className="sim-control-valor text-secondary">{control.porcentajeCumplimiento}%</div>
                <div className="small text-muted">cumplimiento documental</div>
              </div>
            </div>
          </div>
        </div>
        <div className="card-footer d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span className="small text-muted">
            Estado documental: {completo ? "completo" : "incompleto"}
          </span>
          <div className="d-flex flex-wrap gap-2">
            {puedeModificar && (
              <Link className="btn btn-outline-secondary" id="btnAdjuntarDocumentos"
                    to={rutaExpediente} state={estadoEnlace}>
                <i className="bi bi-paperclip me-1" aria-hidden="true"></i>
                Adjuntar documentos
              </Link>
            )}
            {puedeConsultar && (
              <Link className="btn btn-outline-secondary" id="btnIrRevisionDocumental"
                    to={`${rutaExpediente}/revision-documental`} state={estadoEnlace}>
                <i className="bi bi-file-earmark-check me-1" aria-hidden="true"></i>
                Revisión documental
              </Link>
            )}
            {puedeModificar && (
              <button type="button" className="btn btn-sim" id="btnContinuarAprobacion"
                      disabled={!control.puedeContinuarAprobacion || enviando}
                      title={control.puedeContinuarAprobacion ? undefined
                        : enAprobacion ? textoNoEnvio
                          : "Se habilita cuando la documentación obligatoria esté completa."}
                      onClick={() => setConfirmando(true)}>
                <i className="bi bi-send-check me-1" aria-hidden="true"></i>
                Continuar a aprobación
              </button>
            )}
          </div>
        </div>
      </div>

      {confirmando && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
               aria-labelledby="ttlConfirmarAprobacion">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5" id="ttlConfirmarAprobacion">
                    <i className="bi bi-send-check text-primary me-2" aria-hidden="true"></i>
                    Continuar a aprobación
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setConfirmando(false)} disabled={enviando}></button>
                </div>
                <div className="modal-body">
                  <p>
                    El expediente <strong>{referenciaInterna}</strong> pasará al estado
                    {" "}<strong>Pendiente de aprobación</strong>.
                  </p>
                  <p className="mb-0">
                    El cumplimiento documental y el cambio de estado quedan registrados en la bitácora,
                    con su usuario, su fecha y su hora.
                  </p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setConfirmando(false)} disabled={enviando}>
                    Cancelar
                  </button>
                  <button type="button" className="btn btn-sim" id="btnConfirmarAprobacion"
                          onClick={continuarAprobacion} disabled={enviando}>
                    {enviando ? "Enviando..." : "Continuar a aprobación"}
                  </button>
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

export default Frm_SIM_ControlDocumentalExoneracion;
