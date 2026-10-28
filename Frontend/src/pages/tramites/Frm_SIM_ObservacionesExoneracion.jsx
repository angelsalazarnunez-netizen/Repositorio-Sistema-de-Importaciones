import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso, {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import "./Frm_SIM_ObservacionesExoneracion.css";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  confirmarObservacion,
  eliminarObservacion,
  modificarObservacion,
  obtenerExoneracion,
  obtenerObservaciones,
  registrarObservacion,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Observaciones y aclaraciones de un expediente de exoneración (HU-020). Permite
 *   redactar una observación y guardarla como borrador o confirmarla de una vez; editar,
 *   eliminar o confirmar los borradores propios; y consultar el historial confirmado, que es
 *   compartido y no se modifica. El usuario responsable lo toma el servidor de la sesión: la
 *   pantalla nunca lo envía.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin EXONERACION_MODIFICAR la pantalla es de
 *     consulta: se muestra el historial confirmado, pero no el formulario ni las acciones sobre los
 *     borradores.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla; PU-05: el historial confirmado se muestra antes del
 *     formulario; la ruta lleva a la lista de exoneraciones; el aviso lleva la vista hasta el
 *     mensaje.
 */

const USUARIO_ACTUAL = "Técnica de Exoneraciones";
const LONGITUD_MAXIMA_MOTIVO = 100;
const LONGITUD_MAXIMA_OBSERVACION = 1000;
const FORMULARIO_VACIO = { motivo: "", observacion: "" };
const OBSERVACIONES_VACIAS = { borradores: [], historial: [] };

const AYUDA = {
  titulo: "Ayuda: observaciones del expediente",
  introduccion:
    "Las observaciones dejan constancia de los hallazgos y de las aclaraciones solicitadas sobre "
    + "el expediente. Primero pueden guardarse como borrador y, cuando estén listas, se confirman.",
  pasos: [
    "Escriba la observación. El motivo es opcional.",
    "Use Guardar borrador si todavía va a revisarla, o Confirmar observación si ya es definitiva.",
    "En Mis borradores puede editar, eliminar o confirmar lo que dejó pendiente.",
    "Al confirmarla, la observación pasa al historial del expediente con su nombre, la fecha y la hora.",
  ],
  dudas: [
    {
      pregunta: "¿Quién ve mis borradores?",
      respuesta: "Solo usted. Las demás personas ven únicamente las observaciones confirmadas.",
    },
    {
      pregunta: "¿Puedo corregir una observación confirmada?",
      respuesta:
        "No. Una vez confirmada forma parte del historial del expediente y no se modifica ni se "
        + "elimina. Revise el borrador antes de confirmarlo.",
    },
    {
      pregunta: "¿Qué pasa con un borrador que elimino?",
      respuesta: "Deja de mostrarse y nunca llega al historial del expediente.",
    },
  ],
};

function formatearFechaHora(valor) {
  return valor
    ? new Date(valor).toLocaleString("es-CR", {
        day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit",
      })
    : "—";
}

/**
 * Acepta solo identificadores enteros positivos: el idTramite llega del estado de navegación o
 * del detalle del expediente, nunca se escribe en la pantalla.
 */
function normalizarIdTramite(valor) {
  const numero = Number(valor);
  return Number.isInteger(numero) && numero > 0 ? numero : null;
}

function normalizarObservaciones(respuesta) {
  return {
    borradores: respuesta?.borradores ?? [],
    historial: respuesta?.historial ?? [],
  };
}

/**
 * Validación previa en la pantalla. Las mismas reglas las vuelve a aplicar el servidor, que es
 * el que decide.
 */
function validarFormulario(formulario) {
  const errores = {};
  const observacion = formulario.observacion.trim();
  if (observacion.length === 0) {
    errores.observacion = "Escriba la observación.";
  } else if (observacion.length > LONGITUD_MAXIMA_OBSERVACION) {
    errores.observacion = `La observación no puede pasar de ${LONGITUD_MAXIMA_OBSERVACION} caracteres.`;
  }
  if (formulario.motivo.trim().length > LONGITUD_MAXIMA_MOTIVO) {
    errores.motivo = `El motivo no puede pasar de ${LONGITUD_MAXIMA_MOTIVO} caracteres.`;
  }
  return errores;
}

function datosDelFormulario(formulario) {
  const motivo = formulario.motivo.trim();
  return {
    motivo: motivo.length > 0 ? motivo : null,
    observacion: formulario.observacion.trim(),
  };
}

function Frm_SIM_ObservacionesExoneracion() {
  // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
  const puedeModificar = useSesionActual().tieneAlguno([PERMISOS.EXONERACION_MODIFICAR]);
  const { referenciaInterna } = useParams();
  const { state: estadoNavegacion } = useLocation();
  const idTramiteNavegacion = normalizarIdTramite(estadoNavegacion?.idTramite);

  const [idTramite, setIdTramite] = useState(null);
  const [estadoExpediente, setEstadoExpediente] = useState(estadoNavegacion?.estado ?? null);
  const [observaciones, setObservaciones] = useState(OBSERVACIONES_VACIAS);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [errores, setErrores] = useState({});
  const [idEnEdicion, setIdEnEdicion] = useState(null);
  const [accionPendiente, setAccionPendiente] = useState(null);
  const [procesando, setProcesando] = useState(false);

  const responsable = obtenerSesion()?.nombreCompleto || USUARIO_ACTUAL;
  const enEdicion = idEnEdicion !== null;

  // Obtiene el idTramite del flujo del expediente y carga sus observaciones. Si se llegó desde
  // el expediente, el idTramite viene en el estado de navegación; si se entró directo por la
  // dirección o se recargó la página, se toma del detalle del expediente por su referencia.
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
          if (vigente) {
            setEstadoExpediente(detalle?.estado ?? null);
          }
          if (id === null) {
            throw new Error("El expediente no indica su identificador.");
          }
        }
        const respuesta = await obtenerObservaciones(id);
        if (vigente) {
          setIdTramite(id);
          setObservaciones(normalizarObservaciones(respuesta));
        }
      } catch (problema) {
        if (vigente) {
          setErrorCarga(`No fue posible cargar las observaciones. ${problema.message}`);
        }
      } finally {
        if (vigente) {
          setCargando(false);
        }
      }
    }

    cargar();
    return () => { vigente = false; };
  }, [referenciaInterna, idTramiteNavegacion, intentoCarga]);

  /**
   * Vuelve a consultar borradores e historial después de una operación. Si la operación ya se
   * hizo pero la lista no se puede refrescar, se avisa sin ocultar el resultado.
   */
  async function refrescarObservaciones() {
    try {
      setObservaciones(normalizarObservaciones(await obtenerObservaciones(idTramite)));
    } catch (problema) {
      setAlerta({
        tipo: "warning",
        texto: "La operación se realizó, pero no fue posible actualizar la lista de observaciones. "
          + `Recargue la página. ${problema.message}`,
      });
    }
  }

  function limpiarFormulario() {
    setFormulario(FORMULARIO_VACIO);
    setErrores({});
    setIdEnEdicion(null);
  }

  function actualizarCampo(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined }));
  }

  function iniciarEdicion(borrador) {
    setAlerta(null);
    setIdEnEdicion(borrador.idObservacion);
    setFormulario({ motivo: borrador.motivo ?? "", observacion: borrador.observacion ?? "" });
    setErrores({});
    document.getElementById("frmObservacion")?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  /**
   * Valida el formulario y muestra los errores junto a cada campo.
   *
   * @return {boolean} true si el formulario puede enviarse
   */
  function formularioValido() {
    const encontrados = validarFormulario(formulario);
    setErrores(encontrados);
    return Object.keys(encontrados).length === 0;
  }

  function guardarBorrador(evento) {
    evento.preventDefault();
    setAlerta(null);
    if (formularioValido()) {
      crearObservacion(false);
    }
  }

  function solicitarConfirmacionNueva() {
    setAlerta(null);
    if (formularioValido()) {
      setAccionPendiente({ tipo: "confirmarNueva" });
    }
  }

  async function crearObservacion(confirmar) {
    setProcesando(true);
    try {
      await registrarObservacion(idTramite, datosDelFormulario(formulario), confirmar);
      limpiarFormulario();
      setAlerta({
        tipo: "success",
        texto: confirmar
          ? "La observación se confirmó y quedó en el historial del expediente."
          : "La observación se guardó como borrador. Puede editarla o confirmarla cuando esté lista.",
      });
      await refrescarObservaciones();
    } catch (problema) {
      setAlerta({
        tipo: "danger",
        texto: confirmar
          ? `No fue posible confirmar la observación. ${problema.message}`
          : `No fue posible guardar el borrador. ${problema.message}`,
      });
    } finally {
      setProcesando(false);
    }
  }

  async function guardarEdicion(evento) {
    evento.preventDefault();
    setAlerta(null);
    if (!formularioValido()) {
      return;
    }
    setProcesando(true);
    try {
      await modificarObservacion(idTramite, idEnEdicion, datosDelFormulario(formulario));
      limpiarFormulario();
      setAlerta({ tipo: "success", texto: "Los cambios del borrador se guardaron." });
      await refrescarObservaciones();
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible editar el borrador. ${problema.message}` });
    } finally {
      setProcesando(false);
    }
  }

  /**
   * Ejecuta la acción que la persona usuaria aceptó en la ventana de confirmación: confirmar una
   * observación nueva, confirmar un borrador existente o eliminar un borrador.
   */
  async function ejecutarAccionPendiente() {
    const accion = accionPendiente;
    setAlerta(null);

    if (accion.tipo === "confirmarNueva") {
      setAccionPendiente(null);
      await crearObservacion(true);
      return;
    }

    setProcesando(true);
    try {
      if (accion.tipo === "eliminarBorrador") {
        // El DELETE ya devuelve los borradores y el historial actualizados.
        const respuesta = await eliminarObservacion(idTramite, accion.borrador.idObservacion);
        setObservaciones(normalizarObservaciones(respuesta));
        setAlerta({ tipo: "success", texto: "El borrador se eliminó." });
      } else {
        await confirmarObservacion(idTramite, accion.borrador.idObservacion);
        setAlerta({
          tipo: "success",
          texto: "El borrador se confirmó y quedó en el historial del expediente.",
        });
        await refrescarObservaciones();
      }
    } catch (problema) {
      setAlerta({
        tipo: "danger",
        texto: accion.tipo === "eliminarBorrador"
          ? `No fue posible eliminar el borrador. ${problema.message}`
          : `No fue posible confirmar la observación. ${problema.message}`,
      });
    } finally {
      setAccionPendiente(null);
      setProcesando(false);
    }
  }

  const rutaExpediente = `/tramites/exoneraciones/${encodeURIComponent(referenciaInterna)}`;

  const migas = (
    <nav aria-label="Ruta de navegación">
      <ol className="breadcrumb small">
        <li className="breadcrumb-item"><Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CONSULTAR]} alterno="Exoneraciones"><Link to="/tramites/exoneraciones">Exoneraciones</Link></Cmp_SIM_ConPermiso></li>
        <li className="breadcrumb-item"><Link to={rutaExpediente}>{referenciaInterna}</Link></li>
        <li className="breadcrumb-item active" aria-current="page">Observaciones</li>
      </ol>
    </nav>
  );

  if (cargando || errorCarga) {
    return (
      <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
        {migas}
        {cargando
          ? <p className="text-muted">Cargando las observaciones...</p>
          : (
            <>
              <div className="alert alert-danger" role="alert">{errorCarga}</div>
              <div className="d-flex gap-2">
                <Link className="btn btn-outline-secondary" to={rutaExpediente}>Volver al expediente</Link>
                <button type="button" className="btn btn-sim" id="btnReintentarCarga"
                        onClick={() => setIntentoCarga((intento) => intento + 1)}>
                  <i className="bi bi-arrow-clockwise me-1" aria-hidden="true"></i>Reintentar
                </button>
              </div>
            </>
          )}
      </Cmp_SIM_Marco>
    );
  }

  const borradorEnEdicion = observaciones.borradores
    .find((borrador) => borrador.idObservacion === idEnEdicion);

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      {migas}

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">
            Observaciones del expediente <span className="sim-referencia">{referenciaInterna}</span>
          </h1>
          {estadoExpediente && <span className="badge text-bg-warning fs-6">{estadoExpediente}</span>}
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Registre las observaciones de la revisión del expediente y la aclaración que se solicita a la unidad. Puede guardarlas como borrador antes de confirmarlas.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}
      {!puedeModificar && (
        <Cmp_SIM_AvisoSoloConsulta texto={"Usted puede consultar el historial de observaciones, pero sus roles "
          + "no le permiten registrarlas. Si lo necesita, solicite el rol correspondiente a la jefatura de la Unidad."} />
      )}

      {/* Historial confirmado: compartido, cronológico y sin acciones. PU-05: se muestra antes del
          formulario, para leer lo ya registrado por la técnica antes de escribir algo nuevo. */}
      <div className="card sim-card mb-4 sim-observaciones-historial" id="seccionHistorial">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span>
            <i className="bi bi-clock-history me-2" aria-hidden="true"></i>Historial de observaciones
            <span className="text-muted small fw-normal"> · se conserva completo</span>
          </span>
          <span className="badge text-bg-light text-dark">{observaciones.historial.length}</span>
        </div>
        <div className="card-body">
          {observaciones.historial.length === 0 ? (
            <p className="text-muted small mb-0">El expediente todavía no tiene observaciones confirmadas.</p>
          ) : (
            <ol className="list-group list-group-numbered">
              {observaciones.historial.map((observacion) => (
                <li key={observacion.idObservacion}
                    className="list-group-item d-flex gap-2 sim-observacion-historial">
                  <div className="flex-grow-1">
                    <div className="d-flex flex-wrap align-items-center gap-2 mb-1">
                      <span className="badge text-bg-success">Confirmada</span>
                      {observacion.motivo && <strong>{observacion.motivo}</strong>}
                    </div>
                    <p className="mb-1 text-break sim-observacion-texto">{observacion.observacion}</p>
                    <small className="text-muted">
                      {observacion.responsable ?? "—"} · {formatearFechaHora(observacion.fechaConfirmacion)}
                    </small>
                  </div>
                </li>
              ))}
            </ol>
          )}
          <p className="form-text mb-0 mt-2">
            Las observaciones confirmadas no se modifican ni se eliminan: cada una conserva quién la
            registró y cuándo.
          </p>
        </div>
      </div>

      {/* Formulario: registra una observación nueva o edita un borrador propio. */}
      {puedeModificar && (
        <div className="card sim-card mb-4" id="frmObservacion">
          <div className="card-header">
            {enEdicion
              ? <><i className="bi bi-pencil-square me-2" aria-hidden="true"></i>Editar borrador</>
              : <><i className="bi bi-chat-square-text me-2" aria-hidden="true"></i>Registrar observación</>}
          </div>
          <div className="card-body">
            <form onSubmit={enEdicion ? guardarEdicion : guardarBorrador} noValidate>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtExpediente">Expediente</label>
                  <input type="text" className="form-control sim-referencia" id="txtExpediente"
                         value={referenciaInterna} readOnly />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtResponsable">
                    Responsable
                    <Cmp_SIM_AyudaCampo titulo="el responsable">
                      Es la persona con la sesión iniciada. El sistema la registra al guardar; no se
                      puede cambiar.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control" id="txtResponsable"
                         value={responsable} readOnly />
                </div>
                <div className="col-12">
                  <label className="form-label" htmlFor="txtMotivo">
                    Motivo <span className="text-muted small">(opcional)</span>
                    <Cmp_SIM_AyudaCampo titulo="el motivo">
                      Resuma en pocas palabras por qué se registra la observación, por ejemplo
                      &quot;Documento ilegible&quot;. Puede dejarlo en blanco.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" id="txtMotivo"
                         className={`form-control${errores.motivo ? " is-invalid" : ""}`}
                         maxLength={LONGITUD_MAXIMA_MOTIVO}
                         value={formulario.motivo}
                         aria-describedby="hlpMotivo"
                         onChange={(evento) => actualizarCampo("motivo", evento.target.value)} />
                  {errores.motivo && <div className="invalid-feedback">{errores.motivo}</div>}
                  <div className="form-text text-end" id="hlpMotivo">
                    {formulario.motivo.length}/{LONGITUD_MAXIMA_MOTIVO}
                  </div>
                </div>
                <div className="col-12">
                  <label className="form-label" htmlFor="txtObservacion">
                    Observación <span className="text-danger" aria-hidden="true">*</span>
                    <span className="visually-hidden">(obligatoria)</span>
                  </label>
                  <textarea id="txtObservacion" rows={4}
                            className={`form-control${errores.observacion ? " is-invalid" : ""}`}
                            maxLength={LONGITUD_MAXIMA_OBSERVACION}
                            placeholder="Describa el hallazgo y la aclaración solicitada a la unidad"
                            value={formulario.observacion}
                            aria-describedby="hlpObservacion"
                            aria-required="true"
                            onChange={(evento) => actualizarCampo("observacion", evento.target.value)} />
                  {errores.observacion && <div className="invalid-feedback">{errores.observacion}</div>}
                  <div className="form-text text-end" id="hlpObservacion">
                    {formulario.observacion.length}/{LONGITUD_MAXIMA_OBSERVACION}
                  </div>
                </div>
              </div>

              <p className="form-text mb-0 mt-2">
                {enEdicion
                  ? "Está editando un borrador. Al guardar los cambios sigue siendo borrador; "
                    + "para confirmarlo use el botón Confirmar de la lista de borradores."
                  : "Un borrador solo lo ve usted. Al confirmar, la observación pasa al historial con su "
                    + "nombre, la fecha y la hora, y ya no puede modificarse ni eliminarse."}
              </p>

              <div className="d-flex flex-wrap justify-content-end gap-2 mt-3">
                {enEdicion ? (
                  <>
                    <button type="button" className="btn btn-outline-secondary" id="btnCancelarEdicion"
                            onClick={limpiarFormulario} disabled={procesando}>
                      Cancelar edición
                    </button>
                    <button type="submit" className="btn btn-sim" id="btnGuardarEdicion"
                            disabled={procesando || !borradorEnEdicion}>
                      <i className="bi bi-save me-1" aria-hidden="true"></i>Guardar cambios
                    </button>
                  </>
                ) : (
                  <>
                    <button type="button" className="btn btn-outline-secondary" id="btnLimpiarObservacion"
                            onClick={limpiarFormulario} disabled={procesando}>
                      Limpiar
                    </button>
                    <button type="submit" className="btn btn-outline-secondary" id="btnGuardarBorrador"
                            disabled={procesando}>
                      <i className="bi bi-file-earmark me-1" aria-hidden="true"></i>Guardar borrador
                    </button>
                    <button type="button" className="btn btn-sim" id="btnConfirmarObservacion"
                            onClick={solicitarConfirmacionNueva} disabled={procesando}>
                      <i className="bi bi-check2-circle me-1" aria-hidden="true"></i>Confirmar observación
                    </button>
                  </>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Borradores propios: solo los del usuario autenticado, editables. */}
      <div className="card sim-card mb-4 sim-observaciones-borradores" id="seccionBorradores">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-pencil me-2" aria-hidden="true"></i>Mis borradores</span>
          <span className="badge text-bg-light text-dark">{observaciones.borradores.length}</span>
        </div>
        <div className="card-body">
          <p className="form-text mt-0">
            Solo usted ve sus borradores. Puede editarlos, eliminarlos o confirmarlos.
          </p>
          {observaciones.borradores.length === 0 ? (
            <p className="text-muted small mb-0">No tiene borradores en este expediente.</p>
          ) : (
            <ul className="list-group">
              {observaciones.borradores.map((borrador) => {
                const editable = borrador.editable !== false;
                const esEsteEnEdicion = borrador.idObservacion === idEnEdicion;
                return (
                  <li key={borrador.idObservacion}
                      className="list-group-item sim-observacion-borrador"
                      aria-current={esEsteEnEdicion ? "true" : undefined}>
                    <div className="d-flex flex-wrap justify-content-between align-items-start gap-2">
                      <div className="flex-grow-1">
                        <div className="d-flex flex-wrap align-items-center gap-2 mb-1">
                          <span className="badge text-bg-warning">Borrador</span>
                          {esEsteEnEdicion && <span className="badge text-bg-light text-dark">En edición</span>}
                          {borrador.motivo && <strong>{borrador.motivo}</strong>}
                        </div>
                        <p className="mb-1 text-break sim-observacion-texto">{borrador.observacion}</p>
                        <small className="text-muted">
                          Creado {formatearFechaHora(borrador.fechaCreacion)}
                          {borrador.fechaModificacion
                            && ` · modificado ${formatearFechaHora(borrador.fechaModificacion)}`}
                        </small>
                      </div>
                      {editable && puedeModificar && (
                        <div className="btn-group btn-group-sm" role="group"
                             aria-label="Acciones del borrador">
                          <button type="button" className="btn btn-outline-secondary"
                                  onClick={() => iniciarEdicion(borrador)}
                                  disabled={procesando || enEdicion}>
                            <i className="bi bi-pencil me-1" aria-hidden="true"></i>Editar
                          </button>
                          <button type="button" className="btn btn-outline-danger"
                                  onClick={() => setAccionPendiente({ tipo: "eliminarBorrador", borrador })}
                                  disabled={procesando || enEdicion}>
                            <i className="bi bi-trash me-1" aria-hidden="true"></i>Eliminar
                          </button>
                          <button type="button" className="btn btn-sim"
                                  onClick={() => setAccionPendiente({ tipo: "confirmarBorrador", borrador })}
                                  disabled={procesando || enEdicion}>
                            <i className="bi bi-check2-circle me-1" aria-hidden="true"></i>Confirmar
                          </button>
                        </div>
                      )}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
          {enEdicion && observaciones.borradores.length > 0 && (
            <p className="form-text mb-0 mt-2">
              Guarde o cancele la edición para usar las acciones de los borradores.
            </p>
          )}
        </div>
      </div>

      {accionPendiente && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
               aria-labelledby="ttlAccionObservacion">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5" id="ttlAccionObservacion">
                    {accionPendiente.tipo === "eliminarBorrador" ? (
                      <><i className="bi bi-trash text-danger me-2" aria-hidden="true"></i>Eliminar borrador</>
                    ) : (
                      <><i className="bi bi-check2-circle me-2" aria-hidden="true"></i>Confirmar observación</>
                    )}
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setAccionPendiente(null)} disabled={procesando}></button>
                </div>
                <div className="modal-body">
                  {accionPendiente.tipo === "eliminarBorrador" ? (
                    <p className="mb-0">
                      Se eliminará el borrador. Nunca llegó al historial del expediente, así que
                      nadie más lo verá.
                    </p>
                  ) : (
                    <>
                      <p>
                        La observación pasará al historial del expediente con su nombre, la fecha y
                        la hora.
                      </p>
                      <p className="mb-0">
                        <strong>Después de confirmarla no podrá modificarla ni eliminarla.</strong>
                      </p>
                    </>
                  )}
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setAccionPendiente(null)} disabled={procesando}>
                    Cancelar
                  </button>
                  {accionPendiente.tipo === "eliminarBorrador" ? (
                    <button type="button" className="btn btn-danger" id="btnAceptarEliminacion"
                            onClick={ejecutarAccionPendiente} disabled={procesando}>
                      Eliminar borrador
                    </button>
                  ) : (
                    <button type="button" className="btn btn-sim" id="btnAceptarConfirmacion"
                            onClick={ejecutarAccionPendiente} disabled={procesando}>
                      Confirmar observación
                    </button>
                  )}
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

export default Frm_SIM_ObservacionesExoneracion;
