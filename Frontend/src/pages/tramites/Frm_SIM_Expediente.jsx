import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import Cmp_SIM_CambioReferencia from "../../componentes/Cmp_SIM_CambioReferencia.jsx";
import Cmp_SIM_CostosTramite from "../../componentes/Cmp_SIM_CostosTramite.jsx";
import {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import Cmp_SIM_ImprimirBitacora from "../../componentes/Cmp_SIM_ImprimirBitacora.jsx";
import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import {
  Cmp_SIM_IndicacionArchivos,
  Cmp_SIM_ListaArchivos,
  describirRechazados,
  separarArchivosPorTamano,
} from "../../componentes/Cmp_SIM_Documentos.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import {
  agregarDocumentos,
  cambiarEstado,
  corregirDatosDesalmacenaje,
  descargarDocumento,
  eliminarDocumento,
  obtenerEstados,
  obtenerExpediente,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";
import Cmp_SIM_Pestanas, { ultimoPorFecha, usePestanaActiva } from "../../componentes/Cmp_SIM_Pestanas.jsx";
import Cmp_SIM_ConfirmarCierre from "../../componentes/Cmp_SIM_ConfirmarCierre.jsx";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Expediente de un desalmacenaje especial y su seguimiento (HU-032). Muestra los
 *   datos del trámite, su documentación y su historial, permite actualizar el estado con una
 *   justificación obligatoria y agregar documentación posterior. Las actualizaciones no
 *   sustituyen ni eliminan la información anterior.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin DESALMACENAJE_MODIFICAR el expediente es de
 *     consulta: los datos y los costos se muestran sin poder cambiarlos, y no se ofrecen el cambio de
 *     referencia, el cambio de estado ni agregar o eliminar documentos.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: PU-02: apartados en pestañas con el último movimiento visible al entrar; PU-03:
 *     botón "Cambiar estado" junto al estado; H-09: confirmación antes de un estado que cierra el
 *     expediente; H4/H5: formato, tamaño y aviso de archivos grandes; el aviso lleva la vista
 *     hasta el mensaje (H-07); descripción de la pantalla.
 */

const USUARIO_ACTUAL = "Analista de Importaciones";
const LONGITUD_MINIMA_JUSTIFICACION = 10;
const LONGITUD_MAXIMA_JUSTIFICACION = 255;

const AYUDA = {
  titulo: "Ayuda: seguimiento del expediente",
  introduccion:
    "El expediente reúne los datos del trámite, su documentación y el historial de sus estados. "
    + "Cada actualización queda registrada con la fecha, la hora, el usuario y su justificación.",
  pasos: [
    "Revise los datos del trámite y la documentación ya asociada.",
    "Para actualizar el avance, elija el nuevo estado de la lista.",
    "Escriba la justificación del cambio, entre 10 y 255 caracteres.",
    "Presione Guardar actualización: el movimiento queda en el historial y en la bitácora.",
    "Si llega documentación nueva, adjúntela en la sección Agregar documentación.",
  ],
  dudas: [
    {
      pregunta: "¿Se pierde el estado anterior?",
      respuesta:
        "No. El historial conserva cada movimiento con su estado anterior, el nuevo, la fecha, el "
        + "usuario responsable y la justificación.",
    },
    {
      pregunta: "¿Puedo corregir un documento equivocado?",
      respuesta:
        "Sí: adjunte de nuevo el mismo tipo de documento. La versión anterior se conserva marcada "
        + "como sustituida, de modo que el expediente mantiene su historia.",
    },
    {
      pregunta: "¿Por qué no aparece el estado que busco?",
      respuesta:
        "La lista muestra los estados catalogados para el desalmacenaje especial, sin el estado "
        + "actual del expediente. Si falta alguno, debe agregarse al catálogo.",
    },
  ],
};

function claseEstado(estado) {
  if (estado === "Pendiente" || estado === "Pendiente de permisos especiales") {
    return "text-bg-warning";
  }
  if (estado === "Finalizada") {
    return "text-bg-success";
  }
  if (estado === "Cancelada") {
    return "text-bg-secondary";
  }
  if (estado === "Retenido en Almacén Fiscal") {
    return "text-bg-danger";
  }
  return "text-bg-info";
}

function formatearFechaHora(valor) {
  return valor
    ? new Date(valor).toLocaleString("es-CR", {
        day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit",
      })
    : "—";
}

// Documentación que el expediente espera reunir. Puede quedar pendiente: el expediente recuerda
// lo que falta cada vez que se abre.
const DOCUMENTACION_ESPERADA = [
  { nombre: "Justificación, acta de salida histórica o documento atípico" },
  { nombre: "Lista de empaque" },
  { nombre: "Factura o documento equivalente" },
  { nombre: "Documento de embarque" },
];

const ARCHIVOS_VACIOS = {
  documentosRespaldo: [],
  packingList: [],
  factura: [],
  documentoEmbarque: [],
};

function Frm_SIM_Expediente() {
  // Restricciones por rol (HU-003): sin el permiso de modificar, el expediente es de consulta.
  const puedeModificar = useSesionActual().tieneAlguno([PERMISOS.DESALMACENAJE_MODIFICAR]);
  const { referenciaInterna } = useParams();
  const [expediente, setExpediente] = useState(null);
  const [estados, setEstados] = useState([]);
  // H-09 / EH5-03: los estados que cierran el expediente piden confirmación antes de aplicarse.
  const [estadosFinales, setEstadosFinales] = useState([]);
  const [confirmacionCierre, setConfirmacionCierre] = useState(false);
  const [nuevoEstado, setNuevoEstado] = useState("");
  const [justificacion, setJustificacion] = useState("");
  const [archivos, setArchivos] = useState(ARCHIVOS_VACIOS);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [pestana, setPestana] = usePestanaActiva(["datos", "estado", "documentos", "historial"], "datos");
  const [guardando, setGuardando] = useState(false);
  const [documentoPorEliminar, setDocumentoPorEliminar] = useState(null);
  const [pendientesAbiertos, setPendientesAbiertos] = useState(false);
  const [datos, setDatos] = useState(null);
  const navegar = useNavigate();

  /**
   * Lleva a la persona usuaria al expediente con su referencia nueva. Cuando el expediente
   * se reclasifica, la pantalla correcta es la del tipo al que quedó.
   *
   * @param {object} respuesta datos que devuelve el cambio de referencia
   */
  /**
   * Carga en el formulario los datos que trae el expediente, para poder corregirlos.
   *
   * @param {object} detalle expediente recibido del servicio
   */
  function cargarDatos(detalle) {
    setDatos({
      origenDependencia: detalle.origenDependencia ?? "",
      justificacion: detalle.justificacion ?? "",
      paisOrigen: detalle.paisOrigen ?? "",
      moneda: detalle.moneda ?? "",
      valorEstimado: detalle.valorEstimado ?? "",
      descripcion: detalle.descripcion ?? "",
      regimenAduanero: detalle.regimenAduanero ?? "",
      aduanaControl: detalle.aduanaControl ?? "",
      observaciones: detalle.observaciones ?? "",
    });
  }

  /**
   * Guarda las correcciones de los datos del expediente.
   *
   * @param {object} evento envío del formulario
   */
  async function guardarDatos(evento) {
    evento.preventDefault();
    setAlerta(null);
    setGuardando(true);
    try {
      const actualizado = await corregirDatosDesalmacenaje(referenciaInterna, {
        ...datos,
        valorEstimado: datos.valorEstimado === "" ? null : Number(datos.valorEstimado),
      });
      setExpediente(actualizado);
      cargarDatos(actualizado);
      setAlerta({
        tipo: "success",
        texto: "Los datos del expediente se corrigieron y el cambio quedó anotado en la bitácora.",
      });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible guardar los datos. ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  function irAlExpediente(respuesta) {
    const ruta = respuesta.codigoTipoTramite === "EXONERACION"
      ? "/tramites/exoneraciones/"
      : "/tramites/expedientes/";
    navegar(`${ruta}${encodeURIComponent(respuesta.referenciaInterna)}`);
  }

  useEffect(() => {
    let vigente = true;

    Promise.all([obtenerExpediente(referenciaInterna), obtenerEstados()])
      .then(([detalle, catalogo]) => {
        if (!vigente) {
          return;
        }
        setExpediente(detalle);
        cargarDatos(detalle);
        setEstados(catalogo.map((estado) => estado.nombre));
        setEstadosFinales(catalogo.filter((estado) => estado.esFinal).map((estado) => estado.nombre));
      })
      .catch((problema) => {
        if (vigente) {
          setAlerta({ tipo: "danger", texto: `No fue posible cargar el expediente. ${problema.message}` });
        }
      });

    return () => { vigente = false; };
  }, [referenciaInterna]);

  async function actualizarEstado(evento) {
    evento.preventDefault();
    setAlerta(null);

    if (!nuevoEstado) {
      setAlerta({ tipo: "danger", texto: "Seleccione el nuevo estado del trámite." });
      return;
    }
    if (justificacion.trim().length < LONGITUD_MINIMA_JUSTIFICACION) {
      setAlerta({
        tipo: "danger",
        texto: `Escriba la justificación del cambio, de al menos ${LONGITUD_MINIMA_JUSTIFICACION} caracteres.`,
      });
      return;
    }

    if (estadosFinales.includes(nuevoEstado)) {
      setConfirmacionCierre(true);
      return;
    }
    await aplicarCambioEstado();
  }

  async function aplicarCambioEstado() {
    setConfirmacionCierre(false);
    setGuardando(true);
    try {
      const actualizado = await cambiarEstado(referenciaInterna, nuevoEstado, justificacion.trim());
      setExpediente(actualizado);
      cargarDatos(actualizado);
      setNuevoEstado("");
      setJustificacion("");
      setAlerta({ tipo: "success", texto: "El estado del trámite se actualizó correctamente." });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible actualizar el estado. ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  async function adjuntarDocumentos(evento) {
    evento.preventDefault();
    setAlerta(null);

    const total = Object.values(archivos).reduce((suma, lista) => suma + lista.length, 0);
    if (total === 0) {
      setAlerta({ tipo: "warning", texto: "Seleccione al menos un archivo para agregarlo al expediente." });
      return;
    }

    setGuardando(true);
    try {
      const actualizado = await agregarDocumentos(referenciaInterna, archivos);
      setExpediente(actualizado);
      cargarDatos(actualizado);
      setArchivos(ARCHIVOS_VACIOS);
      setAlerta({ tipo: "success", texto: "La documentación se agregó al expediente." });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible agregar la documentación. ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  async function confirmarEliminacion() {
    setAlerta(null);
    setGuardando(true);
    try {
      const actualizado = await eliminarDocumento(referenciaInterna, documentoPorEliminar.idDocumento);
      setExpediente(actualizado);
      cargarDatos(actualizado);
      setAlerta({
        tipo: "success",
        texto: `El documento ${documentoPorEliminar.nombreArchivo} se retiró del expediente y la `
          + "eliminación quedó anotada en la bitácora.",
      });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible eliminar el documento. ${problema.message}` });
    } finally {
      setDocumentoPorEliminar(null);
      setGuardando(false);
    }
  }

  if (!expediente) {
    return (
      <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
        {alerta
          ? <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>
          : <p className="text-muted">Cargando el expediente...</p>}
        <Link className="btn btn-outline-secondary" to="/tramites/expedientes">Volver a la lista</Link>
      </Cmp_SIM_Marco>
    );
  }

  const estadosDisponibles = estados.filter((estado) => estado !== expediente.estado);
  const documentosVigentes = (expediente.documentos ?? [])
    .filter((documento) => documento.estadoDocumento === "ACT");
  const documentacionPendiente = DOCUMENTACION_ESPERADA.filter(
    (esperado) => !documentosVigentes.some((documento) => documento.tipoDocumento === esperado.nombre)
  );

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/tramites/expedientes">Expedientes</Link></li>
          <li className="breadcrumb-item active" aria-current="page">{expediente.referenciaInterna}</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <div>
            <h1 className="h4 mb-1">
              Expediente <span className="sim-referencia">{expediente.referenciaInterna}</span>
            </h1>
            <p className="mb-0 text-muted small">{expediente.tipoTramite}</p>
          </div>
          <div className="d-flex align-items-center gap-2">
            <Link className="btn btn-outline-secondary btn-sm"
                  to={`/tramites/expedientes/${encodeURIComponent(expediente.referenciaInterna)}/documentacion`}>
              <i className="bi bi-folder2-open me-1" aria-hidden="true"></i>
              {puedeModificar ? "Gestionar documentación" : "Ver documentación"}
            </Link>
            {puedeModificar && (
              <Cmp_SIM_CambioReferencia referenciaInterna={expediente.referenciaInterna}
                                        codigoTipoActual="DESALM_ESPECIAL"
                                        onCambiado={irAlExpediente} />
            )}
            <span className="small text-muted">Estado actual</span>
            <span className={`badge fs-6 ${claseEstado(expediente.estado)}`} id="lblEstadoActual">{expediente.estado}</span>
            {/* PU-03: la acción para cambiar el estado queda junto al estado, visible sin desplazarse. */}
            {puedeModificar && (
              <button type="button" className="btn btn-sm btn-sim" id="btnIrCambiarEstado"
                      onClick={() => setPestana("estado")}>
                <i className="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Cambiar estado
              </button>
            )}
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Consulte los datos del desalmacenaje especial, actualice su estado, gestione su documentación y revise el historial de estados.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}
      {!puedeModificar && <Cmp_SIM_AvisoSoloConsulta />}

      {/* PU-02 y comentario de la Unidad: el estado y el último movimiento se ven al entrar, y cada
          apartado del expediente queda en su pestaña, sin tener que bajar toda la página. */}
      {ultimoPorFecha(expediente.historial) && (
        <p className="small text-muted mb-2" id="lblUltimoMovimiento">
          <i className="bi bi-clock-history me-1" aria-hidden="true"></i>
          Último movimiento: {formatearFechaHora(ultimoPorFecha(expediente.historial).fechaHora)} · pasó a{" "}
          <strong>{ultimoPorFecha(expediente.historial).estadoNuevo}</strong> · {ultimoPorFecha(expediente.historial).usuario}
        </p>
      )}
      <Cmp_SIM_Pestanas
        activa={pestana}
        alCambiar={setPestana}
        pestanas={[
          { id: "datos", titulo: "Datos del desalmacenaje", icono: "bi-truck" },
          ...(puedeModificar
            ? [{ id: "estado", titulo: "Actualizar estado", icono: "bi-arrow-repeat" }] : []),
          { id: "documentos", titulo: "Documentación", icono: "bi-paperclip",
            contador: (expediente.documentos ?? []).length },
          { id: "historial", titulo: "Historial de estados", icono: "bi-clock-history",
            contador: (expediente.historial ?? []).length },
        ]}
      />

      {pestana === "datos" && (<>
      <div className="card sim-card mb-4">
        <div className="card-header">
          <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>Datos del trámite
        </div>
        <div className="card-body">
          <dl className="row">
            <dt className="col-12 col-md-4">Usuario responsable</dt>
            <dd className="col-12 col-md-8">{expediente.usuarioResponsable ?? "—"}</dd>
            <dt className="col-12 col-md-4">Fecha de registro</dt>
            <dd className="col-12 col-md-8">{formatearFechaHora(expediente.fechaCreacion)}</dd>
          </dl>

          {datos && (
            <form onSubmit={guardarDatos} noValidate>
              <fieldset disabled={!puedeModificar}>
                <div className="row g-3">
                  <div className="col-12 col-md-6">
                    <label className="form-label sim-requerido" htmlFor="txtOrigenDependencia">
                      Origen o dependencia responsable
                      <Cmp_SIM_AyudaCampo titulo="los datos del expediente">
                        Corrija aquí lo que la dependencia aclare después del registro. Cada cambio
                        queda en la bitácora con su valor anterior, su responsable y su fecha.
                      </Cmp_SIM_AyudaCampo>
                    </label>
                    <input type="text" className="form-control" id="txtOrigenDependencia" maxLength={120}
                           value={datos.origenDependencia}
                           onChange={(evento) =>
                             setDatos({ ...datos, origenDependencia: evento.target.value })} />
                  </div>

                  <div className="col-12 col-md-3">
                    <label className="form-label" htmlFor="txtPaisOrigen">País de origen</label>
                    <input type="text" className="form-control" id="txtPaisOrigen" maxLength={50}
                           value={datos.paisOrigen}
                           onChange={(evento) => setDatos({ ...datos, paisOrigen: evento.target.value })} />
                  </div>

                  <div className="col-6 col-md-1">
                    <label className="form-label" htmlFor="cmbMoneda">Moneda</label>
                    <select className="form-select" id="cmbMoneda" value={datos.moneda}
                            onChange={(evento) => setDatos({ ...datos, moneda: evento.target.value })}>
                      <option value="">—</option>
                      <option value="CRC">CRC</option>
                      <option value="USD">USD</option>
                      <option value="EUR">EUR</option>
                    </select>
                  </div>

                  <div className="col-6 col-md-2">
                    <label className="form-label" htmlFor="numValorEstimado">Valor estimado</label>
                    <input type="number" className="form-control" id="numValorEstimado" min="0" step="0.01"
                           value={datos.valorEstimado}
                           onChange={(evento) =>
                             setDatos({ ...datos, valorEstimado: evento.target.value })} />
                  </div>

                  <div className="col-12 col-md-6">
                    <label className="form-label" htmlFor="txtRegimenAduanero">Régimen aduanero</label>
                    <input type="text" className="form-control" id="txtRegimenAduanero" maxLength={60}
                           value={datos.regimenAduanero}
                           onChange={(evento) =>
                             setDatos({ ...datos, regimenAduanero: evento.target.value })} />
                  </div>

                  <div className="col-12 col-md-6">
                    <label className="form-label" htmlFor="txtAduanaControl">Aduana de control</label>
                    <input type="text" className="form-control" id="txtAduanaControl" maxLength={60}
                           value={datos.aduanaControl}
                           onChange={(evento) =>
                             setDatos({ ...datos, aduanaControl: evento.target.value })} />
                  </div>

                  <div className="col-12">
                    <label className="form-label" htmlFor="txtDescripcion">Descripción de la mercancía</label>
                    <input type="text" className="form-control" id="txtDescripcion" maxLength={255}
                           value={datos.descripcion}
                           onChange={(evento) => setDatos({ ...datos, descripcion: evento.target.value })} />
                  </div>

                  <div className="col-12">
                    <label className="form-label sim-requerido" htmlFor="txtJustificacionDatos">
                      Justificación del trámite
                    </label>
                    <textarea className="form-control" id="txtJustificacionDatos" rows={3} maxLength={1000}
                              value={datos.justificacion}
                              onChange={(evento) =>
                                setDatos({ ...datos, justificacion: evento.target.value })} />
                    <div className="form-text text-end">{datos.justificacion.length}/1000</div>
                  </div>

                  <div className="col-12">
                    <label className="form-label" htmlFor="txtObservacionesDatos">Observaciones</label>
                    <textarea className="form-control" id="txtObservacionesDatos" rows={2} maxLength={4000}
                              value={datos.observaciones}
                              onChange={(evento) =>
                                setDatos({ ...datos, observaciones: evento.target.value })} />
                  </div>
                </div>

                {puedeModificar && (
                  <div className="d-flex justify-content-end gap-2 mt-3">
                    <button type="button" className="btn btn-outline-secondary" id="btnDeshacerDatos"
                            onClick={() => cargarDatos(expediente)}>
                      Deshacer
                    </button>
                    <button type="submit" className="btn btn-sim" id="btnGuardarDatos" disabled={guardando}>
                      {guardando ? "Guardando..." : "Guardar cambios"}
                    </button>
                  </div>
                )}
              </fieldset>
            </form>
          )}
        </div>
      </div>

      <Cmp_SIM_CostosTramite referenciaInterna={referenciaInterna} expediente={expediente}
                             soloLectura={!puedeModificar}
                             alGuardar={() => obtenerExpediente(referenciaInterna).then((detalle) => {
                               setExpediente(detalle);
                               cargarDatos(detalle);
                             })} />

      </>)}

      {pestana === "estado" && (<>
      {puedeModificar && (
        <div className="card sim-card mb-4">
          <div className="card-header">
            <i className="bi bi-arrow-repeat me-2" aria-hidden="true"></i>Actualizar estado del trámite
          </div>
          <div className="card-body">
            <form onSubmit={actualizarEstado} noValidate>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label sim-requerido" htmlFor="cmbNuevoEstado">
                    Nuevo estado
                    <Cmp_SIM_AyudaCampo titulo="el nuevo estado">
                      La lista muestra los estados catalogados para este tipo de trámite, sin el estado
                      actual del expediente.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <select className="form-select" id="cmbNuevoEstado" value={nuevoEstado}
                          onChange={(evento) => setNuevoEstado(evento.target.value)}>
                    <option value="">Seleccione el nuevo estado</option>
                    {estadosDisponibles.map((estado) => (
                      <option value={estado} key={estado}>{estado}</option>
                    ))}
                  </select>
                </div>

                <div className="col-12">
                  <label className="form-label sim-requerido" htmlFor="txtJustificacionEstado">
                    Nota, observación o justificación
                    <Cmp_SIM_AyudaCampo titulo="la justificación del cambio">
                      Explique el motivo del cambio de estado, entre {LONGITUD_MINIMA_JUSTIFICACION} y{" "}
                      {LONGITUD_MAXIMA_JUSTIFICACION} caracteres. Queda en el historial y en la bitácora.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <textarea className="form-control" id="txtJustificacionEstado" rows={3}
                            maxLength={LONGITUD_MAXIMA_JUSTIFICACION}
                            placeholder="Indique el motivo o detalle de la actualización realizada."
                            value={justificacion}
                            onChange={(evento) => setJustificacion(evento.target.value)} />
                  <div className="form-text text-end">
                    {justificacion.length}/{LONGITUD_MAXIMA_JUSTIFICACION}
                  </div>
                </div>
              </div>

              <div className="d-flex justify-content-end gap-2 mt-3">
                <button type="button" className="btn btn-outline-secondary" id="btnLimpiarCambioEstado"
                        onClick={() => { setNuevoEstado(""); setJustificacion(""); }}>
                  Limpiar
                </button>
                <button type="submit" className="btn btn-sim" id="btnCambiarEstado" disabled={guardando}>
                  <i className="bi bi-check-circle me-1" aria-hidden="true"></i>
                  Guardar actualización
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      </>)}

      {pestana === "documentos" && (<>
      {documentacionPendiente.length > 0 && (
        <div className="alert alert-warning py-2" role="alert">
          <button type="button"
                  className="btn btn-link p-0 text-start text-reset text-decoration-none w-100 d-flex align-items-center gap-2"
                  id="btnPendientes"
                  aria-expanded={pendientesAbiertos}
                  aria-controls="lstPendientes"
                  onClick={() => setPendientesAbiertos(!pendientesAbiertos)}>
            <i className="bi bi-exclamation-triangle" aria-hidden="true"></i>
            <span className="text-truncate flex-grow-1">
              <strong>Pendiente de adjuntar:</strong>{" "}
              {documentacionPendiente.map((documento) => documento.nombre).join(", ")}
            </span>
            <span className="badge text-bg-warning">{documentacionPendiente.length}</span>
            <i className={`bi ${pendientesAbiertos ? "bi-chevron-up" : "bi-chevron-down"}`}
               aria-hidden="true"></i>
          </button>
          {pendientesAbiertos && (
            <ul className="mb-0 mt-2" id="lstPendientes">
              {documentacionPendiente.map((documento) => (
                <li key={documento.nombre}>{documento.nombre}</li>
              ))}
            </ul>
          )}
        </div>
      )}

      <div className="card sim-card mb-4">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-paperclip me-2" aria-hidden="true"></i>Documentación del expediente</span>
          <span className="badge text-bg-light text-dark">{(expediente.documentos ?? []).length}</span>
        </div>
        <div className="card-body">
          <div className="table-responsive">
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
                {(expediente.documentos ?? []).length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-muted small">
                      El expediente todavía no tiene documentación asociada.
                    </td>
                  </tr>
                )}

                {(expediente.documentos ?? []).map((documento) => {
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
                          <button
                             className="btn btn-outline-secondary"
                             onClick={() => descargarDocumento(
                               documento.idDocumento,
                               documento.nombreArchivo
                             ).catch((problema) => setAlerta({
                               tipo: "danger",
                               texto: `No fue posible descargar el documento. ${problema.message}`,
                             }))}
                             type="button">
                            Descargar
                          </button>
                          {vigente && puedeModificar && (
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
        </div>
      </div>

      {puedeModificar && (
        <div className="card sim-card mb-4">
          <div className="card-header">
            <i className="bi bi-cloud-arrow-up me-2" aria-hidden="true"></i>Agregar documentación
          </div>
          <div className="card-body">
            <form onSubmit={adjuntarDocumentos} noValidate>
              <div className="row g-3">
                {[
                  { campo: "documentosRespaldo", etiqueta: "Justificación, acta o documento de respaldo", multiple: true },
                  { campo: "packingList", etiqueta: "Lista de empaque" },
                  { campo: "factura", etiqueta: "Factura o documento equivalente" },
                  { campo: "documentoEmbarque", etiqueta: "Documento de embarque" },
                ].map(({ campo, etiqueta, multiple }) => (
                  <div className="col-12 col-md-6" key={campo}>
                    <label className="form-label" htmlFor={`fil${campo}`}>
                      {etiqueta} {!multiple && <span className="sim-opcional">(si aplica)</span>}
                    </label>
                    <input type="file" className="form-control" id={`fil${campo}`} multiple={Boolean(multiple)}
                           onChange={(evento) => {
                             const { aceptados, rechazados } = separarArchivosPorTamano(evento.target.files);
                             if (rechazados.length > 0) {
                               setAlerta({ tipo: "warning", texto: describirRechazados(rechazados) });
                             }
                             setArchivos((anterior) => ({ ...anterior, [campo]: aceptados }));
                           }} />
                    <Cmp_SIM_IndicacionArchivos />
                    <Cmp_SIM_ListaArchivos
                      archivos={archivos[campo]}
                      alQuitar={(indice) => setArchivos((anterior) => ({
                        ...anterior,
                        [campo]: anterior[campo].filter((_, posicion) => posicion !== indice),
                      }))}
                    />
                  </div>
                ))}
              </div>

              <div className="d-flex justify-content-end gap-2 mt-3">
                <button type="button" className="btn btn-outline-secondary" id="btnLimpiarDocumentos"
                        onClick={() => setArchivos(ARCHIVOS_VACIOS)}>
                  Limpiar
                </button>
                <button type="submit" className="btn btn-sim" id="btnAgregarDocumentos" disabled={guardando}>
                  <i className="bi bi-plus-circle me-1" aria-hidden="true"></i>Agregar al expediente
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      </>)}

      {pestana === "historial" && (<>
      <div className="card sim-card mb-5" id="seccionBitacora">
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
                {(expediente.historial ?? []).length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-muted small">
                      El expediente no registra movimientos de estado.
                    </td>
                  </tr>
                )}

                {(expediente.historial ?? []).map((movimiento, indice) => (
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
        <Cmp_SIM_ConfirmarCierre referencia={expediente.referenciaInterna} estado={nuevoEstado}
                                 alConfirmar={aplicarCambioEstado} guardando={guardando}
                                 alCancelar={() => setConfirmacionCierre(false)} />
      )}

      {documentoPorEliminar && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5">
                    <i className="bi bi-trash text-danger me-2" aria-hidden="true"></i>
                    Eliminar documento del expediente
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setDocumentoPorEliminar(null)}></button>
                </div>
                <div className="modal-body">
                  <p>
                    Se eliminará del expediente el documento{" "}
                    <strong>{documentoPorEliminar.nombreArchivo}</strong>.
                  </p>
                  <p className="mb-0">
                    Como ya estaba guardado, el archivo se conserva como sustituido y la eliminación
                    queda registrada en la bitácora del expediente, con su usuario y su fecha.
                  </p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setDocumentoPorEliminar(null)}>
                    Cancelar
                  </button>
                  <button type="button" className="btn btn-danger" onClick={confirmarEliminacion}
                          disabled={guardando}>
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

export default Frm_SIM_Expediente;
