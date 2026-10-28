import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import Cmp_SIM_CambioReferencia from "../../componentes/Cmp_SIM_CambioReferencia.jsx";
import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import Cmp_SIM_ImprimirBitacora from "../../componentes/Cmp_SIM_ImprimirBitacora.jsx";
import Cmp_SIM_Documentos, { contarArchivos } from "../../componentes/Cmp_SIM_Documentos.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  agregarDocumentosDonacion,
  cambiarEstadoDonacion,
  descargarDocumentoDonacion,
  eliminarDocumentoDonacion,
  obtenerDonacion,
  obtenerEstadosDonacion,
} from "../../servicios/Srv_SIM_Tramites.js";
import { claseEstadoDonacion, nombreTipoDonacion } from "./Lst_SIM_Donaciones.jsx";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";
import Cmp_SIM_Pestanas, { ultimoPorFecha, usePestanaActiva } from "../../componentes/Cmp_SIM_Pestanas.jsx";
import Cmp_SIM_ConfirmarCierre from "../../componentes/Cmp_SIM_ConfirmarCierre.jsx";

/**
 * Autor: Angel Salazar Nunez
 * Fecha: 16/09/2026
 * Descripción: Expediente de una donación y su seguimiento (HU-044). Muestra los datos de la
 *   donación, su documentación y su historial, permite cambiar el estado con una justificación
 *   obligatoria y agregar documentación posterior. Un expediente finalizado o cancelado queda
 *   cerrado: conserva su información pero ya no admite cambios.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se integra a main: el expediente se separa de la lista y tiene su propia ruta,
 *     usa el marco y la documentación comunes, descarga los documentos con el token de la sesión
 *     y muestra el historial con los datos que devuelve el servidor.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se permite eliminar documentos ya guardados mientras el expediente siga abierto.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: la referencia FUNDEVI pasa a llamarse referencia de la Fundación UCR.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: comentarios de la Unidad: se agrega el cambio de referencia (por ejemplo de D a
 *     DE después de que los agentes revisan la documentación) y la impresión del historial.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin DONACION_MODIFICAR el expediente es de
 *     consulta: no se ofrecen el cambio de referencia, el cambio de estado ni agregar o eliminar
 *     documentos.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: PU-02: apartados en pestañas con el último movimiento visible al entrar; PU-03:
 *     botón "Cambiar estado" junto al estado; H-09: confirmación antes de un estado que cierra el
 *     expediente; el aviso lleva la vista hasta el mensaje; descripción de la pantalla.
 */

const LONGITUD_MAXIMA_JUSTIFICACION = 255;
const LONGITUD_MAXIMA_OBSERVACIONES = 4000;

const DOCUMENTOS_ADICIONALES = [
  { codigo: "resolucionAceptacion", nombre: "Resolución o acta de aceptación", obligatorio: false },
  {
    codigo: "documentosRespaldo",
    nombre: "Documentos de respaldo adicionales",
    obligatorio: false,
    multiple: true,
  },
  { codigo: "packingList", nombre: "Lista de empaque", obligatorio: false },
  { codigo: "factura", nombre: "Factura o documento equivalente", obligatorio: false },
  { codigo: "documentoEmbarque", nombre: "Documento de embarque", obligatorio: false },
];

const ARCHIVOS_VACIOS = Object.fromEntries(
  DOCUMENTOS_ADICIONALES.map((documento) => [documento.codigo, []])
);

const AYUDA = {
  titulo: "Ayuda: seguimiento de la donación",
  introduccion:
    "El expediente reúne los datos de la donación, su documentación y el historial de sus estados. "
    + "Cada cambio queda registrado con la fecha, la hora, el usuario y su justificación.",
  pasos: [
    "Revise los datos de la donación y la documentación ya asociada.",
    "Para actualizar el avance, elija el nuevo estado de la lista y escriba la justificación.",
    "Si lo necesita, actualice también las observaciones del expediente.",
    "Presione Guardar actualización: el movimiento queda en el historial y en la bitácora.",
    "Si llega documentación nueva, adjúntela en la sección Agregar documentación.",
    "Si un documento se cargó por error, presione Eliminar en su fila y confirme.",
  ],
  dudas: [
    {
      pregunta: "¿Se pierde un documento al eliminarlo?",
      respuesta:
        "No. El archivo se conserva marcado como sustituido y la eliminación queda en la bitácora "
        + "con el usuario y la fecha. Solo se puede eliminar mientras el expediente esté abierto.",
    },
    {
      pregunta: "¿Por qué ya no puedo cambiar el estado?",
      respuesta:
        "Porque el expediente está Finalizado o Cancelado. Un expediente cerrado conserva su "
        + "información y su historial para consulta, pero ya no admite cambios: ni estados, ni "
        + "documentos nuevos, ni eliminación de documentos.",
    },
    {
      pregunta: "¿Tengo que pasar por todos los estados?",
      respuesta:
        "No. Cada donación recorre solo los estados que su trámite necesita: por ejemplo, una "
        + "donación que no requiere exoneración no pasa por Pendiente de exoneración.",
    },
    {
      pregunta: "¿Se pierde el estado anterior?",
      respuesta:
        "No. El historial conserva cada movimiento con su estado anterior, el nuevo, la fecha, el "
        + "usuario responsable y la justificación.",
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

function formatearFecha(valor) {
  // La fecha de cierre llega sin hora (AAAA-MM-DD): se arma en la zona local para no correr el día.
  if (!valor) {
    return "—";
  }
  const [anio, mes, dia] = valor.split("-").map(Number);
  return new Date(anio, mes - 1, dia).toLocaleDateString("es-CR", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function Frm_SIM_ExpedienteDonacion() {
  // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
  const puedeModificar = useSesionActual().tieneAlguno([PERMISOS.DONACION_MODIFICAR]);
  const { referenciaInterna } = useParams();
  const [expediente, setExpediente] = useState(null);
  const [estados, setEstados] = useState([]);
  // H-09 / EH5-03: los estados que cierran el expediente piden confirmación antes de aplicarse.
  const [estadosFinales, setEstadosFinales] = useState([]);
  const [confirmacionCierre, setConfirmacionCierre] = useState(false);
  const [nuevoEstado, setNuevoEstado] = useState("");
  const [justificacion, setJustificacion] = useState("");
  const [observaciones, setObservaciones] = useState("");
  const [archivos, setArchivos] = useState(ARCHIVOS_VACIOS);
  // Cambia al vaciar los archivos para que los campos de archivo se vuelvan a crear limpios.
  const [versionArchivos, setVersionArchivos] = useState(0);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [pestana, setPestana] = usePestanaActiva(["datos", "estado", "documentos", "historial"], "datos");
  const [guardando, setGuardando] = useState(false);
  const [documentoPorEliminar, setDocumentoPorEliminar] = useState(null);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";
  const navegar = useNavigate();

  /**
   * Lleva al expediente con su referencia nueva. Si la donación se reclasificó (por ejemplo a
   * desalmacenaje especial, DE-), abre la pantalla del tipo al que pasó.
   *
   * @param {object} respuesta datos que devuelve el cambio de referencia
   */
  function irAlExpediente(respuesta) {
    const rutas = {
      DONACION: "/tramites/donaciones/",
      EXONERACION: "/tramites/exoneraciones/",
    };
    const ruta = rutas[respuesta.codigoTipoTramite] ?? "/tramites/expedientes/";
    navegar(`${ruta}${encodeURIComponent(respuesta.referenciaInterna)}`);
  }

  useEffect(() => {
    let vigente = true;

    Promise.all([obtenerDonacion(referenciaInterna), obtenerEstadosDonacion()])
      .then(([detalle, catalogo]) => {
        if (!vigente) {
          return;
        }
        setExpediente(detalle);
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

  function limpiarArchivos() {
    setArchivos(ARCHIVOS_VACIOS);
    setVersionArchivos((version) => version + 1);
  }

  function limpiarCambioEstado() {
    setNuevoEstado("");
    setJustificacion("");
    setObservaciones("");
  }

  async function actualizarEstado(evento) {
    evento.preventDefault();
    setAlerta(null);

    if (!nuevoEstado) {
      setAlerta({ tipo: "danger", texto: "Seleccione el nuevo estado de la donación." });
      return;
    }
    if (!justificacion.trim()) {
      setAlerta({ tipo: "danger", texto: "Escriba la justificación del cambio de estado." });
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
      const actualizado = await cambiarEstadoDonacion(referenciaInterna, {
        nuevoEstado,
        justificacion: justificacion.trim(),
        observaciones: observaciones.trim(),
      });
      setExpediente(actualizado);
      limpiarCambioEstado();
      setAlerta({ tipo: "success", texto: "El estado de la donación se actualizó correctamente." });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible actualizar el estado. ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  async function adjuntarDocumentos(evento) {
    evento.preventDefault();
    setAlerta(null);

    if (contarArchivos(archivos) === 0) {
      setAlerta({ tipo: "warning", texto: "Seleccione al menos un archivo para agregarlo al expediente." });
      return;
    }

    setGuardando(true);
    try {
      const actualizado = await agregarDocumentosDonacion(referenciaInterna, archivos);
      setExpediente(actualizado);
      limpiarArchivos();
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
      const actualizado = await eliminarDocumentoDonacion(referenciaInterna, documentoPorEliminar.idDocumento);
      setExpediente(actualizado);
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
      <Cmp_SIM_Marco usuario={usuarioActual} ayuda={AYUDA}>
        {alerta
          ? <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>
          : <p className="text-muted">Cargando el expediente...</p>}
        <Link className="btn btn-outline-secondary" to="/tramites/donaciones">Volver a la lista</Link>
      </Cmp_SIM_Marco>
    );
  }

  const cerrado = Boolean(expediente.estadoFinal);
  const estadosDisponibles = estados.filter((estado) => estado !== expediente.estado);
  const documentos = expediente.documentos ?? [];
  const historial = expediente.historial ?? [];

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/tramites/donaciones">Donaciones</Link></li>
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
            {!cerrado && puedeModificar && (
              <Cmp_SIM_CambioReferencia referenciaInterna={expediente.referenciaInterna}
                                        codigoTipoActual="DONACION"
                                        onCambiado={irAlExpediente} />
            )}
            <span className="small text-muted">Estado actual</span>
            <span className={`badge fs-6 ${claseEstadoDonacion(expediente.estado)}`} id="lblEstadoActual">{expediente.estado}</span>
            {/* PU-03: la acción para cambiar el estado queda junto al estado, visible sin desplazarse. */}
            {!cerrado && puedeModificar && (
              <button type="button" className="btn btn-sm btn-sim" id="btnIrCambiarEstado"
                      onClick={() => setPestana("estado")}>
                <i className="bi bi-arrow-repeat me-1" aria-hidden="true"></i>Cambiar estado
              </button>
            )}
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Consulte los datos de la donación, actualice su estado, agregue documentación y revise el historial de estados.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}

      {!cerrado && !puedeModificar && <Cmp_SIM_AvisoSoloConsulta />}

      {cerrado && (
        <div className="alert alert-secondary" role="status">
          <i className="bi bi-lock me-2" aria-hidden="true"></i>
          El expediente está cerrado desde el {formatearFecha(expediente.fechaCierre)}. Su información
          y su historial se conservan para consulta, pero ya no admite cambios.
        </div>
      )}

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
          { id: "datos", titulo: "Datos de la donación", icono: "bi-gift" },
          ...(!cerrado && puedeModificar
            ? [{ id: "estado", titulo: "Actualizar estado", icono: "bi-arrow-repeat" }] : []),
          { id: "documentos", titulo: "Documentación", icono: "bi-paperclip", contador: documentos.length },
          { id: "historial", titulo: "Historial de estados", icono: "bi-clock-history", contador: historial.length },
        ]}
      />

      {pestana === "datos" && (<>
      <div className="card sim-card mb-4">
        <div className="card-header">
          <i className="bi bi-gift me-2" aria-hidden="true"></i>Datos de la donación
        </div>
        <div className="card-body">
          <dl className="row mb-0">
            <dt className="col-12 col-md-4">Tipo de donación</dt>
            <dd className="col-12 col-md-8">{nombreTipoDonacion(expediente.tipoDonacion)}</dd>
            <dt className="col-12 col-md-4">Entidad o persona donante</dt>
            <dd className="col-12 col-md-8">{expediente.nombreDonante}</dd>
            {expediente.referenciaFundacionUcr && (
              <>
                <dt className="col-12 col-md-4">Referencia de la Fundación UCR</dt>
                <dd className="col-12 col-md-8">{expediente.referenciaFundacionUcr}</dd>
              </>
            )}
            <dt className="col-12 col-md-4">País de origen</dt>
            <dd className="col-12 col-md-8">{expediente.paisOrigen ?? "No aplica"}</dd>
            <dt className="col-12 col-md-4">Valor estimado de la mercancía</dt>
            <dd className="col-12 col-md-8">
              {expediente.moneda} {Number(expediente.valorEstimado ?? 0).toLocaleString("es-CR", {
                minimumFractionDigits: 2, maximumFractionDigits: 2,
              })}
            </dd>
            <dt className="col-12 col-md-4">Usuario responsable</dt>
            <dd className="col-12 col-md-8">{expediente.usuarioResponsable ?? "—"}</dd>
            <dt className="col-12 col-md-4">Fecha de registro</dt>
            <dd className="col-12 col-md-8">{formatearFechaHora(expediente.fechaCreacion)}</dd>
            {cerrado && (
              <>
                <dt className="col-12 col-md-4">Fecha de cierre</dt>
                <dd className="col-12 col-md-8">{formatearFecha(expediente.fechaCierre)}</dd>
              </>
            )}
            <dt className="col-12 col-md-4">Observaciones</dt>
            <dd className="col-12 col-md-8">{expediente.observaciones || "—"}</dd>
          </dl>
        </div>
      </div>

      </>)}

      {pestana === "estado" && (<>
      {!cerrado && puedeModificar && (
        <div className="card sim-card mb-4">
          <div className="card-header">
            <i className="bi bi-arrow-repeat me-2" aria-hidden="true"></i>Actualizar estado de la donación
          </div>
          <div className="card-body">
            <form onSubmit={actualizarEstado} noValidate>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label sim-requerido" htmlFor="cmbNuevoEstado">
                    Nuevo estado
                    <Cmp_SIM_AyudaCampo titulo="el nuevo estado">
                      La lista muestra los estados catalogados para las donaciones, sin el estado
                      actual. Finalizada y Cancelada cierran el expediente.
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
                    Justificación del cambio
                    <Cmp_SIM_AyudaCampo titulo="la justificación del cambio">
                      Explique el motivo del cambio de estado, hasta {LONGITUD_MAXIMA_JUSTIFICACION}{" "}
                      caracteres. Queda en el historial y en la bitácora.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <textarea className="form-control" id="txtJustificacionEstado" rows={3}
                            maxLength={LONGITUD_MAXIMA_JUSTIFICACION}
                            placeholder="Indique el motivo del cambio de estado."
                            value={justificacion}
                            onChange={(evento) => setJustificacion(evento.target.value)} />
                  <div className="form-text text-end">
                    {justificacion.length}/{LONGITUD_MAXIMA_JUSTIFICACION}
                  </div>
                </div>

                <div className="col-12">
                  <label className="form-label" htmlFor="txtObservacionesEstado">
                    Observaciones del expediente <span className="sim-opcional">(si aplica)</span>
                    <Cmp_SIM_AyudaCampo titulo="las observaciones del expediente">
                      Si escribe algo aquí, reemplaza las observaciones actuales del expediente. Déjelo
                      vacío para conservarlas.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <textarea className="form-control" id="txtObservacionesEstado" rows={2}
                            maxLength={LONGITUD_MAXIMA_OBSERVACIONES}
                            placeholder="Comentarios adicionales sobre la donación."
                            value={observaciones}
                            onChange={(evento) => setObservaciones(evento.target.value)} />
                </div>
              </div>

              <div className="d-flex justify-content-end gap-2 mt-3">
                <button type="button" className="btn btn-outline-secondary" id="btnLimpiarCambioEstado"
                        onClick={limpiarCambioEstado}>
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
      <div className="card sim-card mb-4">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-paperclip me-2" aria-hidden="true"></i>Documentación del expediente</span>
          <span className="badge text-bg-light text-dark">{documentos.length}</span>
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
                {documentos.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-muted small">
                      El expediente todavía no tiene documentación asociada.
                    </td>
                  </tr>
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
                          <button
                            type="button"
                            className="btn btn-outline-secondary"
                            onClick={() => descargarDocumentoDonacion(
                              documento.idDocumento,
                              documento.nombreArchivo
                            ).catch((problema) => setAlerta({
                              tipo: "danger",
                              texto: `No fue posible descargar el documento. ${problema.message}`,
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
        </div>
      </div>

      {!cerrado && puedeModificar && (
        <div className="card sim-card mb-4">
          <div className="card-header">
            <i className="bi bi-cloud-arrow-up me-2" aria-hidden="true"></i>Agregar documentación
          </div>
          <div className="card-body">
            <form onSubmit={adjuntarDocumentos} noValidate>
              <Cmp_SIM_Documentos key={versionArchivos} documentos={DOCUMENTOS_ADICIONALES}
                                  archivos={archivos} alCambiar={setArchivos} />

              <div className="d-flex justify-content-end gap-2 mt-3">
                <button type="button" className="btn btn-outline-secondary" id="btnLimpiarDocumentos"
                        onClick={limpiarArchivos}>
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
                {historial.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-muted small">
                      El expediente no registra movimientos de estado.
                    </td>
                  </tr>
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
                  <button type="button" className="btn btn-danger" id="btnConfirmarEliminacion"
                          onClick={confirmarEliminacion} disabled={guardando}>
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

export default Frm_SIM_ExpedienteDonacion;
