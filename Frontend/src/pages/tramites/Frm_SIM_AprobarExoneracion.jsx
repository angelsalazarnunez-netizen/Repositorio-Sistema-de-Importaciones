import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { PERMISOS } from "../../utilerias/Permisos.js";
import Cmp_SIM_ConPermiso, { useSesionActual } from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import {
  obtenerDecisionExoneracion,
  obtenerExoneracion,
  registrarDecisionExoneracion,
} from "../../servicios/Srv_SIM_Tramites.js";

import "./Frm_SIM_AprobarExoneracion.css";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Aprobación de una solicitud de exoneración (HU-022). Muestra el resumen de la
 *   revisión documental y de las observaciones, y permite a la jefatura aprobar, rechazar o
 *   solicitar modificación. Solo quien tiene el permiso de aprobar puede registrar la decisión;
 *   el servidor lo vuelve a validar. Cada decisión queda en el historial con su fecha, su hora y
 *   su responsable, y se notifica a la técnica que envió el expediente a aprobación.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: el permiso de aprobar se lee con useSesionActual, que se actualiza si cambian los
 *     roles de la sesión (restricciones por rol, HU-003).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla; PU-04: botón "Volver a aprobaciones" y el regreso
 *     al expediente conserva que se llegó desde la bandeja; PU-05: el historial de decisiones se
 *     muestra antes del formulario; el aviso lleva la vista hasta el mensaje.
 */

const PERMISO_APROBAR = "EXONERACION_APROBAR";
const LONGITUD_MAXIMA_JUSTIFICACION = 1000;

const NOMBRES_TIPO = {
  COMPRA_INSTITUCIONAL: "Compra institucional",
  DONACION: "Donación",
  CLIENTE_EXTERNO: "Cliente externo con contrato",
  VEHICULOS: "Vehículos",
  MOVIM_TRASPASO: "Movimiento de traspaso",
};

const DECISIONES = [
  {
    valor: "APROBAR", texto: "Aprobar", icono: "bi-check-circle", boton: "btn-success",
    descripcion: "La exoneración queda aprobada y continúa con el registro EXONET / Hacienda.",
  },
  {
    valor: "RECHAZAR", texto: "Rechazar", icono: "bi-x-circle", boton: "btn-danger",
    descripcion: "La solicitud se rechaza y el expediente se cierra.",
  },
  {
    valor: "MODIFICAR", texto: "Solicitar modificación", icono: "bi-pencil-square", boton: "btn-warning",
    descripcion: "El expediente vuelve a la técnica para corregirlo y reenviarlo a aprobación.",
  },
];

const CLASES_DECISION = { APR: "text-bg-success", REC: "text-bg-danger", MOD: "text-bg-warning" };

const ESTADOS_NOTIFICACION = {
  ENV: { texto: "Correo enviado", clase: "text-success" },
  ERR: { texto: "Correo no enviado", clase: "text-danger" },
  PEN: { texto: "Correo pendiente", clase: "text-muted" },
};

const AYUDA = {
  titulo: "Ayuda: aprobar solicitud de exoneración",
  introduccion:
    "Esta pantalla permite a la jefatura registrar su decisión sobre una solicitud de exoneración "
    + "cuya documentación ya fue validada.",
  pasos: [
    "Revise el resumen: documentos recibidos, observaciones y quién envió el expediente a aprobación.",
    "Seleccione la decisión: aprobar, rechazar o solicitar modificación.",
    "Escriba la justificación. Es obligatoria al rechazar o al solicitar una modificación.",
    "Presione Registrar decisión y confírmela.",
  ],
  dudas: [
    {
      pregunta: "¿Quién puede registrar la decisión?",
      respuesta: "Solo los perfiles con el permiso de aprobar exoneraciones, como la jefatura de la unidad.",
    },
    {
      pregunta: "¿Qué pasa al solicitar una modificación?",
      respuesta:
        "El expediente pasa a \"Pendiente de modificación\". La técnica lo corrige y lo vuelve a "
        + "enviar a aprobación desde el control documental.",
    },
    {
      pregunta: "¿Se puede cambiar una decisión registrada?",
      respuesta:
        "No. Cada decisión queda en el historial con su fecha, su hora y su responsable.",
    },
  ],
};

function normalizarIdTramite(valor) {
  const numero = Number(valor);
  return Number.isInteger(numero) && numero > 0 ? numero : null;
}

function formatearFechaHora(valor) {
  return valor
    ? new Date(valor).toLocaleString("es-CR", {
      day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit",
    })
    : "—";
}

function Frm_SIM_AprobarExoneracion() {
  const { referenciaInterna } = useParams();
  const { state: estadoNavegacion } = useLocation();
  const idTramiteNavegacion = normalizarIdTramite(estadoNavegacion?.idTramite);

  const [resumen, setResumen] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [decision, setDecision] = useState("APROBAR");
  const [justificacion, setJustificacion] = useState("");
  const [confirmando, setConfirmando] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const usuario = obtenerSesion()?.nombreCompleto || "Jefatura";
  // Se actualiza si cambian los permisos de la sesión (restricciones por rol, HU-003).
  const autorizadoEnSesion = useSesionActual().tieneAlguno([PERMISO_APROBAR]);

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
        const respuesta = await obtenerDecisionExoneracion(id);
        if (vigente) {
          setResumen(respuesta);
          // Con la documentación incompleta no se puede aprobar: se propone pedir la modificación.
          if (respuesta?.cumpleDocumentacion !== true) {
            setDecision("MODIFICAR");
          }
        }
      } catch (problema) {
        if (vigente) {
          setErrorCarga(`No fue posible cargar la solicitud. ${problema.message}`);
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

  const justificacionLimpia = justificacion.trim();
  const justificacionObligatoria = decision !== "APROBAR";
  const opcionDecision = DECISIONES.find((opcion) => opcion.valor === decision);

  function solicitarConfirmacion(evento) {
    evento.preventDefault();
    setAlerta(null);
    if (justificacionObligatoria && justificacionLimpia.length === 0) {
      setAlerta({
        tipo: "warning",
        texto: "Escriba la justificación: es obligatoria al rechazar o al solicitar una modificación.",
      });
      return;
    }
    setConfirmando(true);
  }

  async function registrar() {
    setEnviando(true);
    try {
      const respuesta = await registrarDecisionExoneracion(resumen.idTramite, {
        decision,
        justificacion: justificacionLimpia.length > 0 ? justificacionLimpia : null,
      });
      setResumen(respuesta);
      setJustificacion("");
      setAlerta({ tipo: "success", texto: respuesta.mensaje });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: problema.message });
      try {
        setResumen(await obtenerDecisionExoneracion(resumen.idTramite));
      } catch {
        // Se conserva el resumen anterior; el mensaje del servidor ya se mostró.
      }
    } finally {
      setConfirmando(false);
      setEnviando(false);
    }
  }

  const rutaExpediente = `/tramites/exoneraciones/${encodeURIComponent(referenciaInterna)}`;

  const migas = (
    <nav aria-label="Ruta de navegación">
      <ol className="breadcrumb small">
        {autorizadoEnSesion
          ? <li className="breadcrumb-item"><Link to="/tramites/exoneraciones/aprobaciones">Aprobaciones</Link></li>
          : <li className="breadcrumb-item"><Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CONSULTAR]} alterno="Exoneraciones"><Link to="/tramites/exoneraciones">Exoneraciones</Link></Cmp_SIM_ConPermiso></li>}
        <li className="breadcrumb-item">
          <Link to={rutaExpediente}
                state={{ idTramite: resumen?.idTramite, estado: resumen?.estadoExpediente, desdeAprobaciones: autorizadoEnSesion }}>
            {referenciaInterna}
          </Link>
        </li>
        <li className="breadcrumb-item active" aria-current="page">Aprobación</li>
      </ol>
    </nav>
  );

  if (cargando || errorCarga) {
    return (
      <Cmp_SIM_Marco usuario={usuario} ayuda={AYUDA}>
        {migas}
        {cargando ? (
          <p className="text-muted">Cargando la solicitud...</p>
        ) : (
          <>
            <div className="alert alert-danger" role="alert">{errorCarga}</div>
            <div className="d-flex gap-2">
              <Link className="btn btn-outline-secondary" to={rutaExpediente}>Volver al expediente</Link>
              <button type="button" className="btn btn-sim" id="btnReintentarAprobacion"
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

  const documentacionCompleta = resumen.cumpleDocumentacion === true;

  return (
    <Cmp_SIM_Marco usuario={usuario} ayuda={AYUDA}>
      {migas}

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">
            Aprobar solicitud de exoneración{" "}
            <span className="sim-referencia">{resumen.referenciaInterna}</span>
          </h1>
          <div className="d-flex flex-wrap align-items-center gap-2">
            {autorizadoEnSesion && (
              <Link className="btn btn-sm btn-outline-secondary" id="btnVolverAprobaciones"
                    to="/tramites/exoneraciones/aprobaciones">
                <i className="bi bi-arrow-left me-1" aria-hidden="true"></i>Volver a aprobaciones
              </Link>
            )}
            {resumen.estadoExpediente && (
              <span className="badge text-bg-secondary fs-6" id="lblEstadoExpediente">
                {resumen.estadoExpediente}
              </span>
            )}
            <span className={`badge fs-6 ${documentacionCompleta ? "text-bg-success" : "text-bg-danger"}`}>
              {documentacionCompleta ? "Documentación validada" : "Documentación incompleta"}
            </span>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Revise la documentación, las observaciones y la bitácora de la solicitud y registre la decisión de la jefatura: aprobar, rechazar o solicitar modificaciones.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}

      <div className="card sim-card mb-4" id="seccionResumenAprobacion">
        <div className="card-header">
          <i className="bi bi-clipboard-check me-2" aria-hidden="true"></i>
          Resumen de la revisión
        </div>
        <div className="card-body">
          <div className="d-flex flex-wrap align-items-center gap-3 mb-3 sim-aprobacion-expediente">
            <div className="flex-grow-1">
              <div className="fw-semibold">
                {resumen.referenciaInterna} · Exoneración{" "}
                {(NOMBRES_TIPO[resumen.tipoExoneracion] ?? resumen.tipoExoneracion ?? "").toLowerCase()}
              </div>
              <small className="text-muted">
                Contratación {resumen.numeroContratacion ?? "—"} · contrato SICOP {resumen.contratoSicop ?? "—"}
                {resumen.ordenCompraGeco && ` · orden de compra ${resumen.ordenCompraGeco}`}
                {" · "}responsable: {resumen.usuarioResponsable ?? "—"}
              </small>
            </div>
          </div>

          <ul className="list-group">
            <li className="list-group-item d-flex flex-wrap justify-content-between gap-2">
              <span>
                <i className={`bi me-2 ${documentacionCompleta
                  ? "bi-check-circle-fill text-success" : "bi-exclamation-circle-fill text-danger"}`}
                   aria-hidden="true"></i>
                {resumen.documentosRecibidos} de {resumen.totalObligatorios} documentos obligatorios recibidos
              </span>
              <span className="small text-muted">
                {resumen.enviadoPor
                  ? `Enviado a aprobación el ${formatearFechaHora(resumen.fechaEnvio)} · ${resumen.enviadoPor}`
                  : "Sin envío a aprobación registrado"}
              </span>
            </li>
            <li className="list-group-item d-flex flex-wrap justify-content-between gap-2">
              <span>
                <i className="bi bi-chat-left-text me-2 text-secondary" aria-hidden="true"></i>
                {resumen.observacionesRegistradas === 0
                  ? "Sin observaciones registradas"
                  : `${resumen.observacionesRegistradas} observación(es) registrada(s)`}
              </span>
              <span className="small text-muted">
                {resumen.ultimaObservacion && `Última el ${formatearFechaHora(resumen.ultimaObservacion)} · `}
                <Link to={`${rutaExpediente}/observaciones`} state={{ idTramite: resumen.idTramite }}>
                  Ver observaciones
                </Link>
              </span>
            </li>
          </ul>

          {!documentacionCompleta && resumen.documentosPendientes.length > 0 && (
            <div className="alert alert-danger mt-3 mb-0" role="alert">
              No podrá aprobarse mientras la documentación esté incompleta. Pendientes:{" "}
              {resumen.documentosPendientes.join("; ")}.
            </div>
          )}
        </div>
      </div>

      {/* PU-05: lo ya registrado se lee antes que el formulario para registrar algo nuevo. */}
      <div className="card sim-card mb-4" id="seccionHistorialDecisiones">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-clock-history me-2" aria-hidden="true"></i>Historial de decisiones</span>
          <span className="badge text-bg-light text-dark">{resumen.decisiones.length}</span>
        </div>
        <div className="card-body">
          {resumen.decisiones.length === 0 ? (
            <p className="text-muted small mb-0">Todavía no hay decisiones registradas para esta solicitud.</p>
          ) : (
            <div className="table-responsive">
              <table className="table align-middle mb-0">
                <thead>
                  <tr>
                    <th scope="col">Fecha</th>
                    <th scope="col">Decisión</th>
                    <th scope="col">Responsable</th>
                    <th scope="col">Estado</th>
                    <th scope="col">Justificación</th>
                    <th scope="col">Notificación</th>
                  </tr>
                </thead>
                <tbody>
                  {resumen.decisiones.map((registro) => {
                    const notificacion = ESTADOS_NOTIFICACION[registro.estadoNotificacion];
                    return (
                      <tr key={registro.idDecision}>
                        <td className="text-nowrap">{formatearFechaHora(registro.fechaHora)}</td>
                        <td>
                          <span className={`badge ${CLASES_DECISION[registro.decision] ?? "text-bg-secondary"}`}>
                            {registro.nombreDecision}
                          </span>
                        </td>
                        <td>{registro.decisor}</td>
                        <td className="small">{registro.estadoAnterior} → {registro.estadoNuevo}</td>
                        <td className="small">{registro.justificacion || "Sin justificación"}</td>
                        <td className="small">
                          {registro.notificado ?? "—"}
                          {notificacion && <span className={`d-block ${notificacion.clase}`}>{notificacion.texto}</span>}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {resumen.puedeDecidir ? (
        <form className="card sim-card mb-4" id="frmDecisionExoneracion" onSubmit={solicitarConfirmacion} noValidate>
          <div className="card-header">
            <i className="bi bi-patch-check me-2" aria-hidden="true"></i>
            Decisión
          </div>
          <div className="card-body">
            <fieldset>
              <legend className="form-label fs-6">Seleccione la decisión</legend>
              <div className="row g-2">
                {DECISIONES.map((opcion) => (
                  <div className="col-12 col-md-4" key={opcion.valor}>
                    <input type="radio" className="btn-check" name="decision" autoComplete="off"
                           id={`rdbDecision${opcion.valor}`} value={opcion.valor}
                           checked={decision === opcion.valor}
                           disabled={opcion.valor === "APROBAR" && !documentacionCompleta}
                           onChange={() => setDecision(opcion.valor)} />
                    <label className="btn btn-outline-secondary w-100 h-100 text-start sim-aprobacion-opcion"
                           htmlFor={`rdbDecision${opcion.valor}`}>
                      <i className={`bi ${opcion.icono} me-2`} aria-hidden="true"></i>
                      <span className="fw-semibold">{opcion.texto}</span>
                      <small className="d-block mt-1">{opcion.descripcion}</small>
                    </label>
                  </div>
                ))}
              </div>
            </fieldset>

            <div className="mt-3">
              <label className={`form-label ${justificacionObligatoria ? "sim-requerido" : ""}`}
                     htmlFor="txtJustificacionDecision">
                Justificación de la decisión
                <Cmp_SIM_AyudaCampo titulo="la justificación">
                  Explique el motivo de la decisión. Es obligatoria al rechazar o al solicitar una
                  modificación, para que la técnica sepa qué corregir.
                </Cmp_SIM_AyudaCampo>
              </label>
              <textarea className="form-control" id="txtJustificacionDecision" rows={3}
                        maxLength={LONGITUD_MAXIMA_JUSTIFICACION} value={justificacion}
                        placeholder={justificacionObligatoria
                          ? "Indique el motivo y lo que debe corregirse"
                          : "Opcional: por ejemplo, expediente completo y conforme a los requisitos"}
                        onChange={(evento) => setJustificacion(evento.target.value)} />
              <div className="form-text text-end">{justificacion.length}/{LONGITUD_MAXIMA_JUSTIFICACION}</div>
            </div>

            <div className="alert alert-info small mb-0" role="note">
              Solo los perfiles con permiso de aprobación pueden registrar la decisión. Al guardar se
              notifica a la técnica responsable y queda constancia en la bitácora con fecha, hora y usuario.
            </div>
          </div>
          <div className="card-footer d-flex flex-wrap justify-content-between align-items-center gap-2">
            <span className="small text-muted">Decide: {usuario}</span>
            <div className="d-flex gap-2">
              <Link className="btn btn-outline-secondary" to="/tramites/exoneraciones/aprobaciones">
                Cancelar
              </Link>
              <button type="submit" className="btn btn-sim" id="btnRegistrarDecision" disabled={enviando}>
                <i className="bi bi-save me-1" aria-hidden="true"></i>
                Registrar decisión
              </button>
            </div>
          </div>
        </form>
      ) : (
        <div className="alert alert-secondary" role="status" id="lblMotivoNoDecide">
          <i className="bi bi-info-circle me-2" aria-hidden="true"></i>
          {resumen.motivoNoDecide}
        </div>
      )}

      {confirmando && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
               aria-labelledby="ttlConfirmarDecision">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5" id="ttlConfirmarDecision">
                    <i className={`bi ${opcionDecision.icono} me-2`} aria-hidden="true"></i>
                    Confirmar decisión: {opcionDecision.texto.toLowerCase()}
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setConfirmando(false)} disabled={enviando}></button>
                </div>
                <div className="modal-body">
                  <p>{opcionDecision.descripcion}</p>
                  {justificacionLimpia && (
                    <p className="small text-muted text-break">Justificación: {justificacionLimpia}</p>
                  )}
                  <p className="mb-0">
                    La decisión no podrá modificarse: queda registrada en la bitácora y se notifica a
                    la técnica responsable.
                  </p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setConfirmando(false)} disabled={enviando}>
                    Cancelar
                  </button>
                  <button type="button" className={`btn ${opcionDecision.boton}`} id="btnConfirmarDecision"
                          onClick={registrar} disabled={enviando}>
                    {enviando ? "Registrando..." : `Confirmar: ${opcionDecision.texto.toLowerCase()}`}
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

export default Frm_SIM_AprobarExoneracion;
