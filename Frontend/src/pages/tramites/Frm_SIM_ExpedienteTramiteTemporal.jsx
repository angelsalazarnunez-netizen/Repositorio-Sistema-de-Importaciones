import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import Cmp_SIM_ImprimirBitacora from "../../componentes/Cmp_SIM_ImprimirBitacora.jsx";
import Cmp_SIM_Documentos, {
  Cmp_SIM_IndicacionArchivos,
  contarArchivos,
} from "../../componentes/Cmp_SIM_Documentos.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import Cmp_SIM_CuentaRegresiva, {
  fechaHoyLocal,
  formatearFechaLocal,
} from "../../componentes/Cmp_SIM_CuentaRegresiva.jsx";
import {
  NOMBRES_ESTADO_MERCANCIA,
  TIPOS_TRAMITE_TEMPORAL,
  claseEstadoTemporal,
  crearAyudaExpediente,
  formatearMonto,
} from "../../componentes/Cmp_SIM_TramitesTemporales.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  agregarDocumentosTramiteTemporal,
  cambiarEstadoTramiteTemporal,
  descargarDocumentoTramiteTemporal,
  eliminarDocumentoTramiteTemporal,
  guardarDatosTramiteTemporal,
  obtenerOpcionesTramiteTemporal,
  obtenerTramiteTemporal,
  registrarProrrogaTemporal,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";
import Cmp_SIM_Pestanas, { ultimoPorFecha, usePestanaActiva } from "../../componentes/Cmp_SIM_Pestanas.jsx";

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Expediente de una importación temporal y su seguimiento (HU-025). Muestra el
 *   cronograma con la cuenta regresiva del vencimiento y los pendientes de la etapa, permite
 *   cambiar el estado sin editar el resto del expediente, completar los datos de aduana y de
 *   salida, registrar la prórroga y administrar la documentación. En Retenido en aduana los
 *   pendientes se pausan, pero la cuenta regresiva sigue. Un expediente cerrado solo admite
 *   corregir el DUA de cierre.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende también la exportación temporal (HU-026): muestra el antecedente, la
 *     garantía, el peso y el país de destino, agrega al cronograma el reingreso y el desalmacenaje,
 *     y divide los datos en Salida y Reimportación, con la referencia de reimportación (RI-). El
 *     tipo llega por la ruta; antes se llamaba Frm_SIM_ExpedienteImportacionTemporal.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: comentario de la Unidad: la reimportación (RI-) indica si paga impuestos por los
 *     trabajos o no los paga por garantía, y el monto en colones. El historial se puede imprimir.
 *
 * @param {object} props
 * @param {string} props.tipo importacion o exportacion
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin ADUANERO_TEMPORAL_MODIFICAR el expediente es
 *     de consulta: los datos de aduana se muestran sin poder cambiarlos, y no se ofrecen el cambio de
 *     estado, el registro de la prórroga (su historial sí se muestra) ni agregar o eliminar documentos.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: PU-02: apartados en pestañas con el último movimiento visible al entrar; H-08:
 *     asteriscos en los campos obligatorios de la prórroga; H-09: confirmación antes de un estado
 *     que cierra el expediente; H-10: el aviso lleva la vista hasta el mensaje; H4: formato y
 *     tamaño de los archivos; descripción.
 */

const LONGITUD_MAXIMA_JUSTIFICACION = 255;
const LONGITUD_MAXIMA_OBSERVACIONES = 4000;

const LONGITUD_MAXIMA_MOTIVO_PRORROGA = 500;

const PRORROGA_VACIA = {
  motivoProrroga: "",
  oficioProrroga: "",
  numeroGestionProrroga: "",
  mismoNumeroGestion: false,
  autorizacionProrroga: "",
  numeroContrato: "",
  permanecePorAnos: false,
  archivoOficioSolicitud: null,
  archivoResolucion: null,
};

function formatearFechaHora(valor) {
  return valor
    ? new Date(valor).toLocaleString("es-CR", {
        day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit",
      })
    : "—";
}

function sumarSeisMeses(fecha) {
  if (!fecha) return "";
  const [ano, mes, dia] = fecha.split("-").map(Number);
  const fechaNueva = new Date(ano, mes - 1 + 6, dia);
  if (fechaNueva.getDate() !== dia) fechaNueva.setDate(0);
  return `${fechaNueva.getFullYear()}-${String(fechaNueva.getMonth() + 1).padStart(2, "0")}-${String(fechaNueva.getDate()).padStart(2, "0")}`;
}

/** Valores del formulario de ingreso y salida a partir del expediente. */
function datosDesdeExpediente(expediente) {
  const temporal = expediente.datosTemporal ?? {};
  return {
    aduanaControl: expediente.aduanaControl ?? "",
    numeroFactura: temporal.numeroFactura ?? "",
    oficioAduana: temporal.oficioAduana ?? "",
    numeroGestion: temporal.numeroGestion ?? "",
    resolucionAutorizacion: temporal.resolucionAutorizacion ?? "",
    numeroDuaInicio: temporal.numeroDuaInicio ?? "",
    fechaDuaInicio: temporal.fechaDuaInicio ?? "",
    fechaVencimiento: temporal.fechaVencimiento ?? "",
    numeroDuaCierre: temporal.numeroDuaCierre ?? "",
    fechaDuaCierre: temporal.fechaDuaCierre ?? "",
    cancelacionRegimen: temporal.cancelacionRegimen ?? "",
    fechaCancelacion: temporal.fechaCancelacion ?? "",
    observaciones: expediente.observaciones ?? "",
    // Solo los registra la exportación temporal; la importación los ignora.
    oficioNotificacionUsuario: temporal.oficioNotificacionUsuario ?? "",
    notificacionDuaAduana: temporal.notificacionDuaAduana ?? "",
    solicitudIngreso: temporal.solicitudIngreso ?? "",
    fechaReingreso: temporal.fechaReingreso ?? "",
    documentoEmbarque: temporal.documentoEmbarque ?? "",
    fechaDesalmacenaje: temporal.fechaDesalmacenaje ?? "",
    // "", "SI" o "NO": la lista necesita texto; al guardar se envía como true/false.
    pagaImpuestosReimportacion: temporal.pagaImpuestosReimportacion == null
      ? ""
      : (temporal.pagaImpuestosReimportacion ? "SI" : "NO"),
    montoImpuestosReimportacion: temporal.montoImpuestosReimportacion ?? "",
  };
}

/** Datos listos para el servidor: la respuesta Sí/No viaja como valor lógico. */
function datosParaGuardar(datos) {
  const paga = datos.pagaImpuestosReimportacion;
  return {
    ...datos,
    pagaImpuestosReimportacion: paga === "" ? null : paga === "SI",
    montoImpuestosReimportacion: paga === "SI" ? datos.montoImpuestosReimportacion : null,
  };
}

/** Estados que cierran el expediente, por si el catálogo no los marca (H-09). */
const ESTADOS_QUE_CIERRAN = ["Finalizado", "Cancelado", "Salida definitiva", "Anulado"];

/** Campo del formulario de ingreso y salida. */
function Cmp_Campo({
  id, etiqueta, valor, alCambiar, tipo = "text", maxLength, max, placeholder, deshabilitado, requerido = false,
}) {
  return (
    <div className="col-md-6">
      <label className={`form-label ${requerido ? "sim-requerido" : ""}`} htmlFor={id}>{etiqueta}</label>
      <input type={tipo} className="form-control" id={id} value={valor} maxLength={maxLength} max={max}
             placeholder={placeholder} disabled={deshabilitado}
             onChange={(evento) => alCambiar(evento.target.value)} />
    </div>
  );
}

/** Hito del cronograma: su fecha o, si aún no ocurre, la indicación de pendiente. */
function Cmp_Hito({ titulo, fecha, detalle, children }) {
  return (
    <li className="list-group-item d-flex justify-content-between align-items-start gap-2">
      <div>
        <i className={`bi ${fecha ? "bi-check-circle-fill text-success" : "bi-circle text-muted"} me-2`}
           aria-hidden="true"></i>
        <span className="fw-semibold">{titulo}</span>
        {detalle && <span className="d-block small text-muted ms-4">{detalle}</span>}
        {children}
      </div>
      <span className={`text-nowrap ${fecha ? "" : "text-muted small"}`}>
        {fecha ? formatearFechaLocal(fecha) : "Pendiente"}
      </span>
    </li>
  );
}

function Frm_SIM_ExpedienteTramiteTemporal({ tipo }) {
  // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
  const puedeModificar = useSesionActual().tieneAlguno([PERMISOS.ADUANERO_TEMPORAL_MODIFICAR]);
  const configuracion = TIPOS_TRAMITE_TEMPORAL[tipo];
  const servicio = configuracion.servicio;
  const esExportacion = configuracion.esExportacion;
  const ayuda = crearAyudaExpediente(configuracion);
  const documentosExpediente = configuracion.documentos.map((documento) => ({ ...documento, obligatorio: false }));
  const documentosCierre = documentosExpediente.filter((documento) => documento.codigo === configuracion.duaCierre.codigo);
  const { referenciaInterna } = useParams();
  const [expediente, setExpediente] = useState(null);
  const [estados, setEstados] = useState([]);
  // H-09: estados que cierran el expediente; pasar a uno de ellos pide confirmación.
  const [estadosFinales, setEstadosFinales] = useState(ESTADOS_QUE_CIERRAN);
  const [confirmacionCierre, setConfirmacionCierre] = useState(false);
  const [cambio, setCambio] = useState({ nuevoEstado: "", justificacion: "" });
  const [datos, setDatos] = useState(null);
  const [prorroga, setProrroga] = useState(PRORROGA_VACIA);
  const [archivos, setArchivos] = useState({});
  // Cambia al vaciar los archivos para que los campos de archivo se vuelvan a crear limpios.
  const [versionArchivos, setVersionArchivos] = useState(0);
  const [versionArchivosProrroga, setVersionArchivosProrroga] = useState(0);
  const [documentoPorEliminar, setDocumentoPorEliminar] = useState(null);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [pestana, setPestana] = usePestanaActiva(["seguimiento", "datos", "prorroga", "documentos", "historial"], "seguimiento");
  const [guardando, setGuardando] = useState(false);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";
  const hoy = fechaHoyLocal();

  useEffect(() => {
    let vigente = true;
    Promise.all([obtenerTramiteTemporal(servicio, referenciaInterna), obtenerOpcionesTramiteTemporal(servicio)])
      .then(([detalle, opciones]) => {
        if (vigente) {
          setExpediente(detalle);
          setDatos(datosDesdeExpediente(detalle));
          setEstados(opciones.estados.map((estado) => estado.nombre));
          const finales = opciones.estados.filter((estado) => estado.esFinal).map((estado) => estado.nombre);
          if (finales.length > 0) {
            setEstadosFinales(finales);
          }
        }
      })
      .catch((problema) => vigente
        && setAlerta({ tipo: "danger", texto: `No fue posible cargar el expediente. ${problema.message}` }));
    return () => { vigente = false; };
  }, [referenciaInterna, servicio]);

  function mostrarExpediente(detalle) {
    setExpediente(detalle);
    setDatos(datosDesdeExpediente(detalle));
  }

  function cambiarDato(campo, valor) {
    setDatos((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function cambiarProrroga(campo, valor) {
    setProrroga((anterior) => ({ ...anterior, [campo]: valor }));
  }

  function limpiarArchivos() {
    setArchivos({});
    setVersionArchivos((version) => version + 1);
  }

  /** Ejecuta una acción sobre el expediente y muestra su resultado. */
  async function ejecutar(accion, textoExito, textoError, alTerminar) {
    setAlerta(null);
    setGuardando(true);
    try {
      mostrarExpediente(await accion());
      alTerminar?.();
      setAlerta({ tipo: "success", texto: textoExito });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `${textoError} ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  function actualizarEstado(evento) {
    evento.preventDefault();
    if (!cambio.nuevoEstado || !cambio.justificacion.trim()) {
      setAlerta({ tipo: "danger", texto: "Seleccione el nuevo estado y escriba la justificación del cambio." });
      return;
    }
    // H-09 (Sprint 2): un estado que cierra el expediente no se puede deshacer, así que se confirma.
    if (estadosFinales.includes(cambio.nuevoEstado)) {
      setConfirmacionCierre(true);
      return;
    }
    aplicarCambioEstado();
  }

  function aplicarCambioEstado() {
    setConfirmacionCierre(false);
    ejecutar(
      () => cambiarEstadoTramiteTemporal(servicio, referenciaInterna, {
        nuevoEstado: cambio.nuevoEstado,
        justificacion: cambio.justificacion.trim(),
      }),
      `El expediente pasó a ${cambio.nuevoEstado}.`,
      "No fue posible cambiar el estado.",
      () => setCambio({ nuevoEstado: "", justificacion: "" })
    );
  }

  function guardarDatos(evento) {
    evento.preventDefault();
    ejecutar(
      () => guardarDatosTramiteTemporal(servicio, referenciaInterna, datosParaGuardar(datos)),
      "Los datos del expediente se guardaron y el cambio quedó anotado en la bitácora.",
      "No fue posible guardar los datos."
    );
  }

  function guardarProrroga(evento) {
    evento.preventDefault();
    const numeroGestion = prorroga.mismoNumeroGestion
      ? temporal.numeroGestion
      : prorroga.numeroGestionProrroga;
    if (!prorroga.motivoProrroga.trim() || !prorroga.oficioProrroga.trim()
        || !numeroGestion?.trim() || !prorroga.autorizacionProrroga.trim()
        || !vencimiento.fechaVencimiento
        || (prorroga.permanecePorAnos && !prorroga.numeroContrato.trim())) {
      setAlerta({ tipo: "danger", texto: "Complete el motivo, oficio, número de gestión, autorización y los datos requeridos del trámite." });
      return;
    }
    if (!prorroga.archivoOficioSolicitud || !prorroga.archivoResolucion) {
      setAlerta({ tipo: "danger", texto: "Adjunte el oficio enviado a la aduana y la resolución emitida por la aduana." });
      return;
    }
    ejecutar(
      () => registrarProrrogaTemporal(servicio, referenciaInterna, {
        ...prorroga,
        numeroGestionProrroga: prorroga.mismoNumeroGestion ? "" : prorroga.numeroGestionProrroga,
        fechaVencimientoActual: vencimiento.fechaVencimiento,
      }),
      "La prórroga quedó registrada en el expediente.",
      "No fue posible registrar la prórroga.",
      () => {
        setProrroga(PRORROGA_VACIA);
        setVersionArchivosProrroga((version) => version + 1);
      }
    );
  }

  function adjuntarDocumentos(evento) {
    evento.preventDefault();
    if (contarArchivos(archivos) === 0) {
      setAlerta({ tipo: "warning", texto: "Seleccione al menos un archivo para agregarlo al expediente." });
      return;
    }
    ejecutar(
      () => agregarDocumentosTramiteTemporal(servicio, referenciaInterna, archivos),
      "La documentación se agregó al expediente.",
      "No fue posible agregar la documentación.",
      limpiarArchivos
    );
  }

  function confirmarEliminacion() {
    const documento = documentoPorEliminar;
    setDocumentoPorEliminar(null);
    ejecutar(
      () => eliminarDocumentoTramiteTemporal(servicio, referenciaInterna, documento.idDocumento),
      `El documento ${documento.nombreArchivo} se retiró del expediente y la eliminación quedó en la bitácora.`,
      "No fue posible eliminar el documento."
    );
  }

  if (!expediente || !datos) {
    return (
      <Cmp_SIM_Marco usuario={usuarioActual} ayuda={ayuda}>
        {alerta
          ? <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>
          : <p className="text-muted">Cargando el expediente...</p>}
        <Link className="btn btn-outline-secondary" to={configuracion.ruta}>Volver a la lista</Link>
      </Cmp_SIM_Marco>
    );
  }

  const cerrado = Boolean(expediente.estadoFinal);
  const temporal = expediente.datosTemporal ?? {};
  const vencimiento = expediente.vencimiento ?? {};
  const estadosDisponibles = estados.filter((estado) => estado !== expediente.estado);
  const documentos = expediente.documentos ?? [];
  const historial = expediente.historial ?? [];
  const historialProrrogas = expediente.historialProrrogas ?? [];
  const agentes = expediente.agentes ?? [];
  const tieneProrroga = temporal.motivoProrroga || temporal.oficioProrroga || temporal.numeroGestionProrroga
    || temporal.autorizacionProrroga;
  const prorrogaExportacionRegistrada = esExportacion && (historialProrrogas.length > 0 || tieneProrroga);
  const nombreDuaInicio = configuracion.duaInicio.nombre;
  const nombreDuaCierre = configuracion.duaCierre.nombre;

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={ayuda}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to={configuracion.ruta}>{configuracion.titulo}</Link></li>
          <li className="breadcrumb-item active" aria-current="page">{expediente.referenciaInterna}</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">
              Expediente <span className="sim-referencia">{expediente.referenciaInterna}</span>
            </h1>
            <p className="mb-0 text-muted small">{expediente.tipoTramite} · {expediente.descripcionMercancia}</p>
          </div>
          <div className="d-flex flex-wrap align-items-center gap-2">
            <span className={`badge fs-6 ${claseEstadoTemporal(expediente.estado)}`}>{expediente.estado}</span>
            {!cerrado && <Cmp_SIM_CuentaRegresiva vencimiento={vencimiento} grande />}
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Dé seguimiento al trámite temporal: sus datos, el cronograma, los cambios de estado, la prórroga y la documentación del expediente.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}

      {!puedeModificar && <Cmp_SIM_AvisoSoloConsulta />}

      {cerrado && (
        <div className="alert alert-secondary" role="status">
          <i className="bi bi-lock me-2" aria-hidden="true"></i>
          El expediente está cerrado desde el {formatearFechaLocal(expediente.fechaCierre)}. Su información y su
          historial se conservan para consulta; solo se puede corregir el DUA de cierre.
        </div>
      )}

      {expediente.validacionesPausadas && (
        <div className="alert alert-info" role="status">
          <i className="bi bi-pause-circle me-2" aria-hidden="true"></i>
          El equipo está retenido en aduana: los pendientes operativos están en pausa. La cuenta regresiva y las
          alertas del vencimiento legal siguen activas.
        </div>
      )}

      {!cerrado && !expediente.validacionesPausadas && expediente.pendientes?.length > 0 && (
        <div className="alert alert-warning" role="status">
          <i className="bi bi-list-check me-2" aria-hidden="true"></i>
          Pendientes de esta etapa (con su número o con su documento):
          <ul className="mb-0 mt-1">
            {expediente.pendientes.map((pendiente) => <li key={pendiente}>{pendiente}</li>)}
          </ul>
        </div>
      )}

      {vencimiento.indicacion && <div className="alert alert-danger" role="status">{vencimiento.indicacion}</div>}

      {/* PU-02 y comentario de la Unidad: el estado y el último movimiento se ven al entrar, y cada
          apartado del expediente queda en su pestaña, sin tener que bajar toda la página. */}
      {ultimoPorFecha(historial) && (
        <p className="small text-muted mb-2" id="lblUltimoMovimiento">
          <i className="bi bi-clock-history me-1" aria-hidden="true"></i>
          Último movimiento: {formatearFechaHora(ultimoPorFecha(historial).fechaHora)} · pasó a{" "}
          <strong>{ultimoPorFecha(historial).estadoNuevo}</strong> · {ultimoPorFecha(historial).usuario}
        </p>
      )}
      <Cmp_SIM_Pestanas
        activa={pestana}
        alCambiar={setPestana}
        pestanas={[
          { id: "seguimiento", titulo: "Cronograma y estado", icono: "bi-calendar-range" },
          { id: "datos", titulo: "Datos del trámite", icono: "bi-file-earmark-text" },
          ...(!cerrado ? [{ id: "prorroga", titulo: "Prórroga", icono: "bi-calendar-plus" }] : []),
          { id: "documentos", titulo: "Documentación", icono: "bi-paperclip", contador: documentos.length },
          { id: "historial", titulo: "Historial de estados", icono: "bi-clock-history", contador: historial.length },
        ]}
      />

      {pestana === "seguimiento" && (<>
      <div className="row g-4 mb-4">
        <div className="col-12 col-lg-6">
          <div className="card sim-card shadow-sm h-100">
            <div className="card-header">
              <i className="bi bi-calendar-range me-2" aria-hidden="true"></i>Cronograma
            </div>
            <ul className="list-group list-group-flush">
              <Cmp_Hito titulo="Recepción del oficio" fecha={temporal.fechaRecepcion} detalle={temporal.oficioSolicitud} />
              <Cmp_Hito titulo={nombreDuaInicio} fecha={temporal.fechaDuaInicio}
                        detalle={temporal.numeroDuaInicio} />
              <Cmp_Hito titulo="Vencimiento autorizado" fecha={vencimiento.fechaVencimiento}
                        detalle={vencimiento.prorrogado ? "Prorrogado" : temporal.resolucionAutorizacion}>
                {!cerrado && (
                  <span className="d-block ms-4 mt-1"><Cmp_SIM_CuentaRegresiva vencimiento={vencimiento} /></span>
                )}
              </Cmp_Hito>
              {esExportacion && (
                <Cmp_Hito titulo="Reingreso al país" fecha={temporal.fechaReingreso}
                          detalle={temporal.documentoEmbarque && `Documento de embarque ${temporal.documentoEmbarque}`} />
              )}
              <Cmp_Hito titulo={nombreDuaCierre} fecha={temporal.fechaDuaCierre} detalle={temporal.numeroDuaCierre} />
              {esExportacion && <Cmp_Hito titulo="Desalmacenaje" fecha={temporal.fechaDesalmacenaje} />}
              <Cmp_Hito titulo="Cancelación del régimen" fecha={temporal.fechaCancelacion}
                        detalle={temporal.cancelacionRegimen} />
            </ul>
          </div>
        </div>

        <div className="col-12 col-lg-6">
          {!cerrado && puedeModificar ? (
            <div className="card sim-card shadow-sm h-100">
              <div className="card-header">
                <i className="bi bi-arrow-repeat me-2" aria-hidden="true"></i>Cambiar estado
              </div>
              <div className="card-body">
                <form onSubmit={actualizarEstado} noValidate>
                  <label className="form-label sim-requerido" htmlFor="cmbNuevoEstado">
                    Nuevo estado
                    <Cmp_SIM_AyudaCampo titulo="el nuevo estado">
                      {configuracion.estadoEnDestino} exige la fecha de vencimiento. Finalizado exige
                      el {nombreDuaCierre} y la cancelación del régimen. {configuracion.estadosCierre} cierran
                      el expediente.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <select className="form-select mb-3" id="cmbNuevoEstado" value={cambio.nuevoEstado}
                          onChange={(evento) => setCambio({ ...cambio, nuevoEstado: evento.target.value })}>
                    <option value="">Seleccione el nuevo estado</option>
                    {estadosDisponibles.map((estado) => <option value={estado} key={estado}>{estado}</option>)}
                  </select>
                  <label className="form-label sim-requerido" htmlFor="txtJustificacionEstado">Justificación</label>
                  <textarea className="form-control" id="txtJustificacionEstado" rows={3}
                            maxLength={LONGITUD_MAXIMA_JUSTIFICACION}
                            placeholder="Indique el motivo del cambio de estado."
                            value={cambio.justificacion}
                            onChange={(evento) => setCambio({ ...cambio, justificacion: evento.target.value })} />
                  <div className="form-text text-end">{cambio.justificacion.length}/{LONGITUD_MAXIMA_JUSTIFICACION}</div>
                  <div className="d-flex justify-content-end mt-2">
                    <button type="submit" className="btn btn-sim" id="btnCambiarEstado" disabled={guardando}>
                      <i className="bi bi-check-circle me-1" aria-hidden="true"></i>Cambiar estado
                    </button>
                  </div>
                </form>
              </div>
            </div>
          ) : cerrado && (
            <div className="card sim-card shadow-sm h-100">
              <div className="card-header">
                <i className="bi bi-info-circle me-2" aria-hidden="true"></i>Cierre
              </div>
              <div className="card-body">
                <dl className="row mb-0">
                  <dt className="col-6">Fecha de cierre</dt>
                  <dd className="col-6">{formatearFechaLocal(expediente.fechaCierre)}</dd>
                  <dt className="col-6">Vencimiento que tenía</dt>
                  <dd className="col-6">{formatearFechaLocal(vencimiento.fechaVencimiento)}</dd>
                </dl>
              </div>
            </div>
          )}
        </div>
      </div>

      </>)}

      {pestana === "datos" && (<>
      <div className="card sim-card shadow-sm mb-4">
        <div className="card-header">
          <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>Datos de la solicitud
        </div>
        <div className="card-body">
          <dl className="row mb-0">
            <dt className="col-12 col-md-4">Unidad solicitante</dt>
            <dd className="col-12 col-md-8">{temporal.unidadSolicitante}</dd>
            <dt className="col-12 col-md-4">Oficio del usuario solicitante</dt>
            <dd className="col-12 col-md-8">{temporal.oficioSolicitud}</dd>
            <dt className="col-12 col-md-4">Fecha de recepción</dt>
            <dd className="col-12 col-md-8">{formatearFechaLocal(temporal.fechaRecepcion)}</dd>
            <dt className="col-12 col-md-4">Mercancía</dt>
            <dd className="col-12 col-md-8">
              {expediente.descripcionMercancia} ({NOMBRES_ESTADO_MERCANCIA[temporal.estadoMercancia] ?? temporal.estadoMercancia})
            </dd>
            <dt className="col-12 col-md-4">Valor declarado</dt>
            <dd className="col-12 col-md-8">{formatearMonto(expediente.valorEquipo, expediente.moneda)}</dd>
            {esExportacion && (
              <>
                <dt className="col-12 col-md-4">Antecedente</dt>
                <dd className="col-12 col-md-8">{temporal.antecedente ?? "—"}</dd>
                <dt className="col-12 col-md-4">En periodo de garantía</dt>
                <dd className="col-12 col-md-8">{temporal.enGarantia ? "Sí" : "No"}</dd>
                <dt className="col-12 col-md-4">Peso</dt>
                <dd className="col-12 col-md-8">
                  {temporal.pesoKg !== null && temporal.pesoKg !== undefined
                    ? `${Number(temporal.pesoKg).toLocaleString("es-CR")} kg`
                    : "—"}
                </dd>
                <dt className="col-12 col-md-4">País de destino</dt>
                <dd className="col-12 col-md-8">{expediente.paisDestino ?? "—"}</dd>
              </>
            )}
            <dt className="col-12 col-md-4">Usuario responsable</dt>
            <dd className="col-12 col-md-8">{expediente.usuarioResponsable ?? "—"}</dd>
            <dt className="col-12 col-md-4">Agentes que reciben alertas</dt>
            <dd className="col-12 col-md-8">
              {agentes.length > 0 ? agentes.map((agente) => agente.nombreCompleto).join(", ") : "Solo el responsable"}
            </dd>
            <dt className="col-12 col-md-4">Fecha de registro</dt>
            <dd className="col-12 col-md-8">{formatearFechaHora(expediente.fechaCreacion)}</dd>
          </dl>
        </div>
      </div>

      <div className="card sim-card shadow-sm mb-4">
        <div className="card-header">
          <i className="bi bi-building me-2" aria-hidden="true"></i>{configuracion.seccionDatos}
        </div>
        <div className="card-body">
          <form onSubmit={guardarDatos} noValidate>
            <fieldset disabled={!puedeModificar}>
              <h2 className="h6 text-muted">{configuracion.seccionInicio}</h2>
              <div className="row g-3 mb-4">
                <Cmp_Campo id="txtAduanaControl" etiqueta="Aduana de control" valor={datos.aduanaControl} maxLength={60}
                           alCambiar={(valor) => cambiarDato("aduanaControl", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="txtNumeroFactura" etiqueta="Número de factura del exportador" valor={datos.numeroFactura}
                           maxLength={80} placeholder={esExportacion ? "Por ejemplo, 002-2025" : "SR si no trae factura"}
                           alCambiar={(valor) => cambiarDato("numeroFactura", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="txtOficioAduana" etiqueta="Oficio enviado a la aduana" valor={datos.oficioAduana}
                           maxLength={120} placeholder="Oficio o correo con su fecha"
                           alCambiar={(valor) => cambiarDato("oficioAduana", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="txtNumeroGestion" etiqueta="Número de gestión" valor={datos.numeroGestion} maxLength={80}
                           alCambiar={(valor) => cambiarDato("numeroGestion", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="txtResolucionAutorizacion" etiqueta="Resolución de autorización"
                           valor={datos.resolucionAutorizacion} maxLength={120}
                           alCambiar={(valor) => cambiarDato("resolucionAutorizacion", valor)} deshabilitado={cerrado} />
                {esExportacion && (
                  <Cmp_Campo id="txtOficioNotificacionUsuario" etiqueta="Oficio de notificación al usuario"
                             valor={datos.oficioNotificacionUsuario} maxLength={120}
                             alCambiar={(valor) => cambiarDato("oficioNotificacionUsuario", valor)} deshabilitado={cerrado} />
                )}
                <Cmp_Campo id="dtpFechaVencimiento" tipo="date" valor={datos.fechaVencimiento}
                           etiqueta={esExportacion ? "Fecha de vencimiento del régimen" : "Fecha de vencimiento autorizada"}
                           alCambiar={(valor) => cambiarDato("fechaVencimiento", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="txtNumeroDuaInicio" etiqueta={`Número del ${nombreDuaInicio}`}
                           valor={datos.numeroDuaInicio} maxLength={80}
                           alCambiar={(valor) => cambiarDato("numeroDuaInicio", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="dtpFechaDuaInicio" etiqueta={`Fecha del ${nombreDuaInicio}`} tipo="date" max={hoy}
                           valor={datos.fechaDuaInicio}
                           alCambiar={(valor) => cambiarDato("fechaDuaInicio", valor)} deshabilitado={cerrado} />
                {esExportacion && (
                  <Cmp_Campo id="txtNotificacionDuaAduana" etiqueta="Notificación del DUA de salida a la aduana"
                             valor={datos.notificacionDuaAduana} maxLength={120} placeholder="Por ejemplo, correo 17-03-2025"
                             alCambiar={(valor) => cambiarDato("notificacionDuaAduana", valor)} deshabilitado={cerrado} />
                )}
              </div>

              <h2 className="h6 text-muted">{configuracion.seccionCierre}</h2>
              {esExportacion && (
                <p className="small text-muted mb-2">
                  Referencia de reimportación:{" "}
                  <span className="sim-referencia">{configuracion.prefijoReimportacion}{expediente.referenciaInterna}</span>
                </p>
              )}
              <div className="row g-3">
                {esExportacion && (
                  <>
                    <Cmp_Campo id="txtSolicitudIngreso" etiqueta="Solicitud de reingreso del usuario"
                               valor={datos.solicitudIngreso} maxLength={120} placeholder="Oficio o correo"
                               alCambiar={(valor) => cambiarDato("solicitudIngreso", valor)} deshabilitado={cerrado} />
                    <Cmp_Campo id="dtpFechaReingreso" etiqueta="Fecha de reingreso al país" tipo="date" max={hoy}
                               valor={datos.fechaReingreso}
                               alCambiar={(valor) => cambiarDato("fechaReingreso", valor)} deshabilitado={cerrado} />
                    <Cmp_Campo id="txtDocumentoEmbarque" etiqueta="Número del documento de embarque"
                               valor={datos.documentoEmbarque} maxLength={80} placeholder="Guía aérea o conocimiento de embarque"
                               alCambiar={(valor) => cambiarDato("documentoEmbarque", valor)} deshabilitado={cerrado} />
                  </>
                )}
                <Cmp_Campo id="txtNumeroDuaCierre" etiqueta={`Número del ${nombreDuaCierre}`}
                           valor={datos.numeroDuaCierre} maxLength={80}
                           alCambiar={(valor) => cambiarDato("numeroDuaCierre", valor)} />
                <Cmp_Campo id="dtpFechaDuaCierre" etiqueta={`Fecha del ${nombreDuaCierre}`} tipo="date" max={hoy}
                           valor={datos.fechaDuaCierre} alCambiar={(valor) => cambiarDato("fechaDuaCierre", valor)} />
                {esExportacion && (
                  <Cmp_Campo id="dtpFechaDesalmacenaje" etiqueta="Fecha de desalmacenaje" tipo="date" max={hoy}
                             valor={datos.fechaDesalmacenaje}
                             alCambiar={(valor) => cambiarDato("fechaDesalmacenaje", valor)} deshabilitado={cerrado} />
                )}
                {esExportacion && (
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="cmbPagaImpuestosReimportacion">
                      ¿La reimportación paga impuestos?
                      <Cmp_SIM_AyudaCampo titulo="los impuestos de la reimportación">
                        Sí, cuando se pagan impuestos por los trabajos hechos al equipo en el exterior.
                        No, cuando la reparación fue por garantía y no se pagan.
                      </Cmp_SIM_AyudaCampo>
                    </label>
                    <select className="form-select" id="cmbPagaImpuestosReimportacion" disabled={cerrado}
                            value={datos.pagaImpuestosReimportacion}
                            onChange={(evento) => cambiarDato("pagaImpuestosReimportacion", evento.target.value)}>
                      <option value="">Sin indicar</option>
                      <option value="SI">Sí, por los trabajos realizados</option>
                      <option value="NO">No, por garantía</option>
                    </select>
                  </div>
                )}
                {esExportacion && datos.pagaImpuestosReimportacion === "SI" && (
                  <div className="col-md-6">
                    <label className="form-label" htmlFor="numMontoImpuestosReimportacion">
                      Impuestos pagados en la reimportación (₡)
                    </label>
                    <div className="input-group">
                      <span className="input-group-text" aria-hidden="true">₡</span>
                      <input type="number" className="form-control" id="numMontoImpuestosReimportacion"
                             min="0" step="0.01" disabled={cerrado}
                             value={datos.montoImpuestosReimportacion}
                             onChange={(evento) => cambiarDato("montoImpuestosReimportacion", evento.target.value)} />
                    </div>
                  </div>
                )}
                <Cmp_Campo id="txtCancelacionRegimen" etiqueta="Oficio de cancelación del régimen"
                           valor={datos.cancelacionRegimen} maxLength={120} placeholder="Oficio o correo"
                           alCambiar={(valor) => cambiarDato("cancelacionRegimen", valor)} deshabilitado={cerrado} />
                <Cmp_Campo id="dtpFechaCancelacion" etiqueta="Fecha de cancelación del régimen" tipo="date" max={hoy}
                           valor={datos.fechaCancelacion}
                           alCambiar={(valor) => cambiarDato("fechaCancelacion", valor)} deshabilitado={cerrado} />
                <div className="col-12">
                  <label className="form-label" htmlFor="txtObservaciones">Observaciones</label>
                  <textarea className="form-control" id="txtObservaciones" rows={2} disabled={cerrado}
                            maxLength={LONGITUD_MAXIMA_OBSERVACIONES} placeholder="Notas de seguimiento del trámite."
                            value={datos.observaciones} onChange={(evento) => cambiarDato("observaciones", evento.target.value)} />
                </div>
              </div>

              {puedeModificar && (
                <div className="d-flex justify-content-end gap-2 mt-3">
                  <button type="button" className="btn btn-outline-secondary" id="btnDeshacerDatos"
                          onClick={() => setDatos(datosDesdeExpediente(expediente))}>
                    Deshacer cambios
                  </button>
                  <button type="submit" className="btn btn-sim" id="btnGuardarDatos" disabled={guardando}>
                    <i className="bi bi-save me-1" aria-hidden="true"></i>Guardar datos
                  </button>
                </div>
              )}
            </fieldset>
          </form>
        </div>
      </div>

      </>)}

      {pestana === "prorroga" && (<>
      {!cerrado && (
        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-calendar-plus me-2" aria-hidden="true"></i>Prórroga
          </div>
          <div className="card-body">
            {tieneProrroga && (
              <dl className="row small mb-3">
                <dt className="col-12 col-md-4">Motivo de prórroga</dt>
                <dd className="col-12 col-md-8">{temporal.motivoProrroga ?? "—"}</dd>
                <dt className="col-12 col-md-4">Oficio de prórroga</dt>
                <dd className="col-12 col-md-8">{temporal.oficioProrroga ?? "—"}</dd>
                <dt className="col-12 col-md-4">Número de gestión</dt>
                <dd className="col-12 col-md-8">{temporal.numeroGestionProrroga ?? "—"}</dd>
                <dt className="col-12 col-md-4">Autorización</dt>
                <dd className="col-12 col-md-8">{temporal.autorizacionProrroga ?? "—"}</dd>
              </dl>
            )}
            <p className="small text-muted">
              La prórroga requiere el oficio enviado y la resolución de la aduana. Al guardarla, el sistema
              agrega seis meses al vencimiento vigente y conserva el historial y sus documentos.
            </p>
            {prorrogaExportacionRegistrada && (
              <div className="alert alert-info" role="status">
                Esta exportación temporal ya tiene su única prórroga registrada.
              </div>
            )}
            {!prorrogaExportacionRegistrada && puedeModificar && (
            <form onSubmit={guardarProrroga} noValidate>
              <div className="row g-3">
                <div className="col-12">
                  <label className="form-label sim-requerido" htmlFor="txtMotivoProrroga">
                    Motivo de prórroga
                    <Cmp_SIM_AyudaCampo titulo="el motivo de la prórroga">
                      Explique por qué la unidad necesita más plazo, por ejemplo, que la investigación se
                      extiende o que el envío al exterior aún no se concreta.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <textarea className="form-control" id="txtMotivoProrroga" rows={2}
                            required
                            maxLength={LONGITUD_MAXIMA_MOTIVO_PRORROGA}
                            placeholder="Por qué se solicita más plazo a la aduana."
                            value={prorroga.motivoProrroga}
                            onChange={(evento) => cambiarProrroga("motivoProrroga", evento.target.value)} />
                  <div className="form-text text-end">
                    {prorroga.motivoProrroga.length}/{LONGITUD_MAXIMA_MOTIVO_PRORROGA}
                  </div>
                </div>
                <Cmp_Campo id="txtOficioProrroga" etiqueta="Número del oficio de solicitud" maxLength={120} requerido
                           valor={prorroga.oficioProrroga} alCambiar={(valor) => cambiarProrroga("oficioProrroga", valor)} />
                <div className="col-md-6">
                  <label className="form-label sim-requerido" htmlFor="txtNumeroGestionProrroga">Número de gestión ingresado a la aduana</label>
                  <input type="text" className="form-control" id="txtNumeroGestionProrroga" maxLength={80} required
                         disabled={prorroga.mismoNumeroGestion}
                         value={prorroga.mismoNumeroGestion ? temporal.numeroGestion ?? "" : prorroga.numeroGestionProrroga}
                         onChange={(evento) => cambiarProrroga("numeroGestionProrroga", evento.target.value)} />
                  <div className="form-check mt-1">
                    <input className="form-check-input" type="checkbox" id="chkMismoNumeroGestion"
                           checked={prorroga.mismoNumeroGestion} disabled={!temporal.numeroGestion}
                           onChange={(evento) => cambiarProrroga("mismoNumeroGestion", evento.target.checked)} />
                    <label className="form-check-label small" htmlFor="chkMismoNumeroGestion">
                      Mismo número de gestión que el inicial
                      {temporal.numeroGestion ? ` (${temporal.numeroGestion})` : " (no registrado)"}
                    </label>
                  </div>
                </div>
                <Cmp_Campo id="txtAutorizacionProrroga" etiqueta="Número de oficio de autorización o documento de respaldo" maxLength={120} requerido
                           valor={prorroga.autorizacionProrroga}
                           alCambiar={(valor) => cambiarProrroga("autorizacionProrroga", valor)} />
                <div className="col-md-6">
                  <label className="form-label" htmlFor="dtpVencimientoActual">Fecha de vencimiento actual</label>
                  <input type="date" className="form-control" id="dtpVencimientoActual"
                         value={vencimiento.fechaVencimiento ?? ""} readOnly />
                  {!vencimiento.fechaVencimiento && (
                    <div className="form-text">Registre primero el vencimiento autorizado en {configuracion.seccionDatos}.</div>
                  )}
                </div>
                <div className="col-md-6">
                  <label className="form-label" htmlFor="dtpNuevoVencimiento">Nuevo vencimiento calculado</label>
                  <input type="date" className="form-control" id="dtpNuevoVencimiento"
                         value={sumarSeisMeses(vencimiento.fechaVencimiento)} readOnly />
                </div>
                <div className="col-12">
                  <div className="form-check">
                    <input className="form-check-input" type="checkbox" id="chkPermanecePorAnos"
                           checked={prorroga.permanecePorAnos}
                           onChange={(evento) => cambiarProrroga("permanecePorAnos", evento.target.checked)} />
                    <label className="form-check-label" htmlFor="chkPermanecePorAnos">
                      El bien permanecerá en el país por años
                    </label>
                  </div>
                </div>
                {prorroga.permanecePorAnos && (
                  <Cmp_Campo id="txtNumeroContratoProrroga" etiqueta="Número de contrato" maxLength={45} requerido
                             valor={prorroga.numeroContrato}
                             alCambiar={(valor) => cambiarProrroga("numeroContrato", valor)} />
                )}
                <div className="col-md-6">
                  <label className="form-label sim-requerido" htmlFor="fileOficioSolicitudProrroga">Oficio de solicitud enviado a la aduana</label>
                  <input key={versionArchivosProrroga} type="file" className="form-control" id="fileOficioSolicitudProrroga" required
                         onChange={(evento) => cambiarProrroga("archivoOficioSolicitud", evento.target.files?.[0] ?? null)} />
                </div>
                <div className="col-md-6">
                  <label className="form-label sim-requerido" htmlFor="fileResolucionProrroga">Resolución emitida por la aduana</label>
                  <input key={versionArchivosProrroga} type="file" className="form-control" id="fileResolucionProrroga" required
                         onChange={(evento) => cambiarProrroga("archivoResolucion", evento.target.files?.[0] ?? null)} />
                </div>
                <div className="col-12 mt-0">
                  <Cmp_SIM_IndicacionArchivos />
                </div>
              </div>
              <div className="d-flex justify-content-end mt-3">
                <button type="submit" className="btn btn-sim" id="btnRegistrarProrroga" disabled={guardando}>
                  <i className="bi bi-save me-1" aria-hidden="true"></i>Registrar prórroga
                </button>
              </div>
            </form>
            )}
            {historialProrrogas.length > 0 && (
              <div className="mt-4">
                <h3 className="h6">Historial de prórrogas</h3>
                <div className="table-responsive">
                  <table className="table table-sm align-middle">
                    <thead>
                      <tr>
                        <th scope="col">Fecha de registro</th>
                        <th scope="col">Vencimiento anterior</th>
                        <th scope="col">Nuevo vencimiento</th>
                        <th scope="col">Gestión / autorización</th>
                        <th scope="col">Documentos</th>
                      </tr>
                    </thead>
                    <tbody>
                      {historialProrrogas.map((registro) => {
                        const oficio = documentos.find((documento) => documento.idDocumento === registro.idDocumentoOficio);
                        const resolucion = documentos.find((documento) => documento.idDocumento === registro.idDocumentoResolucion);
                        return (
                          <tr key={registro.idHistorialProrroga}>
                            <td>{formatearFechaHora(registro.fechaRegistro)}</td>
                            <td>{formatearFechaLocal(registro.fechaVencimientoAnterior)}</td>
                            <td>{formatearFechaLocal(registro.fechaVencimientoNueva)}</td>
                            <td>{registro.numeroGestion} / {registro.numeroAutorizacion}</td>
                            <td>
                              {[
                                [registro.idDocumentoOficio, oficio?.nombreArchivo ?? "Oficio"],
                                [registro.idDocumentoResolucion, resolucion?.nombreArchivo ?? "Resolución"],
                              ].map(([idDocumento, nombre]) => (
                                <button key={idDocumento} type="button" className="btn btn-link btn-sm p-0 me-2"
                                        onClick={() => descargarDocumentoTramiteTemporal(servicio, idDocumento, nombre)
                                          .catch((problema) => setAlerta({
                                            tipo: "danger", texto: `No fue posible descargar el documento. ${problema.message}`,
                                          }))}>
                                  {nombre}
                                </button>
                              ))}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      </>)}

      {pestana === "documentos" && (<>
      <div className="card sim-card shadow-sm mb-4">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-paperclip me-2" aria-hidden="true"></i>Documentación del expediente</span>
          <span className="badge text-bg-light text-dark">{documentos.length}</span>
        </div>
        <div className="card-body">
          <div className="table-responsive mb-4">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Documentos asociados al expediente</caption>
              <thead>
                <tr>
                  <th scope="col">Archivo</th>
                  <th scope="col">Tipo de documento</th>
                  <th scope="col" className="text-center">Versión</th>
                  <th scope="col">Cargado</th>
                  <th scope="col" className="text-end"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {documentos.length === 0 && (
                  <tr><td colSpan={5} className="text-muted small">El expediente todavía no tiene documentación asociada.</td></tr>
                )}
                {documentos.map((documento) => {
                  const vigente = documento.estadoDocumento === "ACT";
                  return (
                    <tr className={vigente ? "" : "text-muted"} key={documento.idDocumento}>
                      <td>
                        {documento.nombreArchivo}
                        {!vigente && <span className="badge text-bg-secondary ms-2">Sustituido</span>}
                      </td>
                      <td>{documento.tipoDocumento}</td>
                      <td className="text-center">{documento.version}</td>
                      <td>{formatearFechaHora(documento.fechaCarga)}</td>
                      <td className="text-end">
                        <div className="btn-group btn-group-sm">
                          <button type="button" className="btn btn-outline-secondary"
                                  onClick={() => descargarDocumentoTramiteTemporal(servicio, documento.idDocumento, documento.nombreArchivo)
                                    .catch((problema) => setAlerta({
                                      tipo: "danger", texto: `No fue posible descargar el documento. ${problema.message}`,
                                    }))}>
                            Descargar
                          </button>
                          {vigente && !cerrado && puedeModificar && (
                            <button type="button" className="btn btn-outline-danger"
                                    onClick={() => setDocumentoPorEliminar(documento)}>
                              <i className="bi bi-trash me-1" aria-hidden="true"></i>Eliminar
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {puedeModificar && <h2 className="h6 text-muted">Agregar documentación</h2>}
          {puedeModificar && (
            <form onSubmit={adjuntarDocumentos} noValidate>
              <Cmp_SIM_Documentos key={versionArchivos} documentos={cerrado ? documentosCierre : documentosExpediente}
                                  archivos={archivos} alCambiar={setArchivos} />
              <div className="d-flex justify-content-end gap-2 mt-3">
                <button type="button" className="btn btn-outline-secondary" id="btnLimpiarDocumentos" onClick={limpiarArchivos}>
                  Limpiar
                </button>
                <button type="submit" className="btn btn-sim" id="btnAgregarDocumentos" disabled={guardando}>
                  <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>Agregar al expediente
                </button>
              </div>
            </form>
          )}
        </div>
      </div>

      </>)}

      {pestana === "historial" && (<>
      <div className="card sim-card shadow-sm mb-5" id="seccionBitacora">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span>
            <i className="bi bi-clock-history me-2" aria-hidden="true"></i>Historial de estados
            <span className="d-none d-print-inline"> del expediente {expediente.referenciaInterna}</span>
          </span>
          <Cmp_SIM_ImprimirBitacora idSeccion="seccionBitacora" texto="Imprimir historial" />
        </div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Historial de cambios de estado del expediente</caption>
              <thead>
                <tr>
                  <th scope="col">Fecha</th>
                  <th scope="col">Estado anterior</th>
                  <th scope="col">Estado nuevo</th>
                  <th scope="col">Usuario</th>
                  <th scope="col">Justificación</th>
                </tr>
              </thead>
              <tbody>
                {historial.length === 0 && (
                  <tr><td colSpan={5} className="text-muted small">El expediente no registra movimientos de estado.</td></tr>
                )}
                {historial.map((movimiento, indice) => (
                  <tr key={`${movimiento.fechaHora}-${indice}`}>
                    <td>{formatearFechaHora(movimiento.fechaHora)}</td>
                    <td>{movimiento.estadoAnterior ?? "Registro inicial"}</td>
                    <td>{movimiento.estadoNuevo}</td>
                    <td>{movimiento.usuario}</td>
                    <td>{movimiento.justificacion}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      </>)}

      {confirmacionCierre && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
               aria-labelledby="ttlConfirmarCierre">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5" id="ttlConfirmarCierre">
                    <i className="bi bi-exclamation-triangle text-warning me-2" aria-hidden="true"></i>
                    Cerrar el expediente
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setConfirmacionCierre(false)}></button>
                </div>
                <div className="modal-body">
                  <p>
                    El expediente <strong className="sim-referencia">{expediente.referenciaInterna}</strong> pasará
                    a <strong>{cambio.nuevoEstado}</strong> y quedará cerrado.
                  </p>
                  <p className="mb-0">
                    Esta acción no se puede deshacer: un expediente cerrado no se puede reabrir y solo admite
                    cambios en el {nombreDuaCierre}.
                  </p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setConfirmacionCierre(false)}>
                    Cancelar
                  </button>
                  <button type="button" className="btn btn-danger" id="btnConfirmarCierre"
                          onClick={aplicarCambioEstado} disabled={guardando}>
                    Sí, cerrar el expediente
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop fade show"></div>
        </>
      )}

      {documentoPorEliminar && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5">
                    <i className="bi bi-trash text-danger me-2" aria-hidden="true"></i>Eliminar documento del expediente
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar" onClick={() => setDocumentoPorEliminar(null)}></button>
                </div>
                <div className="modal-body">
                  <p>Se eliminará del expediente el documento <strong>{documentoPorEliminar.nombreArchivo}</strong>.</p>
                  <p className="mb-0">
                    El archivo se conserva como sustituido y la eliminación queda registrada en la bitácora del
                    expediente, con su usuario y su fecha.
                  </p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-link" onClick={() => setDocumentoPorEliminar(null)}>Cancelar</button>
                  <button type="button" className="btn btn-danger" id="btnConfirmarEliminacion" onClick={confirmarEliminacion}>
                    Eliminar documento
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

export default Frm_SIM_ExpedienteTramiteTemporal;
