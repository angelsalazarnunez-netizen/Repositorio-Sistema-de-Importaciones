import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";

import Cmp_SIM_CambioReferencia from "../../componentes/Cmp_SIM_CambioReferencia.jsx";
import Cmp_SIM_CostosTramite from "../../componentes/Cmp_SIM_CostosTramite.jsx";
import Cmp_SIM_ImprimirBitacora from "../../componentes/Cmp_SIM_ImprimirBitacora.jsx";
import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso, {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import Cmp_SIM_Documentos, { contarArchivos } from "../../componentes/Cmp_SIM_Documentos.jsx";
import { PLANTILLAS, alEscribirConMascara } from "../../componentes/Cmp_SIM_Mascaras.js";
import {
  agregarComentario,
  agregarDocumentosExoneracion,
  corregirDatosExoneracion,
  descargarDocumento,
  eliminarComentario,
  eliminarDocumentoExoneracion,
  obtenerExoneracion,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";
import Cmp_SIM_Pestanas, { ultimoPorFecha, usePestanaActiva } from "../../componentes/Cmp_SIM_Pestanas.jsx";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Expediente de una solicitud de exoneración (HU-018). Muestra los datos de la
 *   solicitud, su documentación y su bitácora. Permite corregir los datos contractuales cuando
 *   los agentes los verifican con el usuario, agregar o retirar documentación, comentar la
 *   gestión e imprimir la bitácora. Nada se pierde: cada cambio queda anotado.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega el acceso a las observaciones del expediente (HU-020).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega el acceso al control de documentos obligatorios (HU-021).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega el acceso a la aprobación de la solicitud (HU-022), visible solo para
 *     quien tiene el permiso de aprobar.
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega el acceso a la solicitud / autorización EXONET (HU-023). Solo se
 *     agrega el enlace; el resto de la pantalla no cambia.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: comentarios de la Unidad: sin permisos regulatorios; se agregan el expediente de
 *     SICOP, la autorización EXONET, el DUA de aplicación, el comprobante de impuestos y otros
 *     documentos (se pueden adjuntar también después de la aprobación). Los costos muestran solo
 *     los impuestos exonerados: bodegaje, partida e impuestos pagados no son de la exoneración.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin EXONERACION_MODIFICAR el expediente es de
 *     consulta: los datos y los impuestos se muestran sin poder cambiarlos, y no se ofrecen el cambio
 *     de referencia, agregar o eliminar documentos ni los comentarios. Quien solo aprueba no ve los
 *     accesos a observaciones, revisión documental y autorización EXONET.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: PU-02: apartados en pestañas con la última anotación de la bitácora visible al
 *     entrar; PU-04: si se llega desde aprobaciones, la ruta y el botón de regreso llevan a la
 *     bandeja; PU-11: "Observaciones" pasa a "Descripción del bien exonerado"; la ruta lleva a la
 *     lista de exoneraciones; el aviso lleva la vista hasta el mensaje (H-07); descripción de la
 *     pantalla.
 */

const USUARIO_ACTUAL = "Técnica de Exoneraciones";

const DOCUMENTOS_EXONERACION = [
  {
    codigo: "documentoEmbarque", nombre: "Documento de embarque",
    nombreCatalogo: "Documento de embarque", obligatorio: true,
  },
  {
    codigo: "facturaComercial", nombre: "Factura comercial",
    nombreCatalogo: "Factura o documento equivalente", obligatorio: true,
  },
  { codigo: "formularioS1", nombre: "Formulario S1", nombreCatalogo: "Formulario S1", obligatorio: true },
  { codigo: "listaEmpaque", nombre: "Lista de empaque", nombreCatalogo: "Lista de empaque", obligatorio: true },
  { codigo: "exonet", nombre: "EXONET", nombreCatalogo: "EXONET", obligatorio: false },
  {
    codigo: "traduccionFactura", nombre: "Traducción de factura",
    nombreCatalogo: "Traducción de factura", obligatorio: false,
  },
  {
    codigo: "expedienteSicop", nombre: "Expediente de SICOP",
    nombreCatalogo: "Expediente de SICOP", obligatorio: false, multiple: true,
    ayuda: "El expediente de la contratación en PDF, para la jefatura.",
  },
  {
    codigo: "autorizacionExonet", nombre: "Autorización EXONET",
    nombreCatalogo: "Autorización EXONET", obligatorio: false,
    ayuda: "Se puede adjuntar después de que la jefatura aprueba la solicitud.",
  },
  {
    codigo: "duaAplicacion", nombre: "DUA de aplicación",
    nombreCatalogo: "DUA de aplicación", obligatorio: false,
    ayuda: "El DUA en el que se aplicó la exoneración.",
  },
  {
    codigo: "comprobanteImpuestos", nombre: "Comprobante de impuestos",
    nombreCatalogo: "Comprobante de impuestos", obligatorio: false, multiple: true,
  },
  {
    codigo: "otrosDocumentos", nombre: "Otros documentos",
    nombreCatalogo: "Otros documentos", obligatorio: false, multiple: true,
    ayuda: "Correos, oficios u otro respaldo del trámite.",
  },
];

const ARCHIVOS_VACIOS = Object.fromEntries(
  DOCUMENTOS_EXONERACION.map((documento) => [documento.codigo, []])
);

const NOMBRES_TIPO = {
  COMPRA_INSTITUCIONAL: "Compra institucional",
  DONACION: "Donación",
  CLIENTE_EXTERNO: "Cliente externo con contrato",
  VEHICULOS: "Vehículos",
  MOVIM_TRASPASO: "Movimiento de traspaso",
};

const AYUDA = {
  titulo: "Ayuda: expediente de la solicitud de exoneración",
  introduccion:
    "El expediente reúne los datos de la solicitud, su documentación y la bitácora con todo lo que "
    + "se ha hecho sobre ella.",
  pasos: [
    "Revise los datos de la solicitud y corrija los números si los agentes los verificaron con el usuario.",
    "Adjunte la documentación que llegue después; la que falta aparece marcada como pendiente. "
    + "Después de la aprobación puede adjuntar la autorización EXONET, el DUA de aplicación y el "
    + "comprobante de impuestos.",
    "Retire un documento cargado por error: se conserva como inactivo y queda anotado en la bitácora.",
    "Agregue comentarios para dejar constancia de la gestión.",
    "Use Imprimir bitácora cuando necesite el respaldo en papel o en PDF.",
  ],
  dudas: [
    {
      pregunta: "¿Se puede cambiar la referencia interna?",
      respuesta:
        "Sí, con el botón Cambiar referencia. Si la sigla nueva es de otro tipo de trámite (por "
        + "ejemplo DE-), el expediente se reclasifica y conserva su documentación y su bitácora.",
    },
    {
      pregunta: "¿Qué pasa con lo que corrijo?",
      respuesta:
        "La bitácora guarda el valor anterior y el nuevo, con el usuario y la fecha, de modo que se "
        + "puede rastrear qué cambió y cuándo.",
    },
    {
      pregunta: "¿Puedo borrar un comentario equivocado?",
      respuesta:
        "Sí. El comentario deja de mostrarse, pero se conserva su texto original y la eliminación "
        + "queda registrada.",
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

function Frm_SIM_ExpedienteExoneracion() {
  // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
  const { tieneAlguno } = useSesionActual();
  const puedeModificar = tieneAlguno([PERMISOS.EXONERACION_MODIFICAR]);
  // Quien solo aprueba ve el expediente, pero no las pantallas de gestión de la exoneración.
  const puedeConsultar = tieneAlguno([PERMISOS.EXONERACION_CONSULTAR]);
  const puedeAprobar = tieneAlguno([PERMISOS.EXONERACION_APROBAR]);
  const { referenciaInterna } = useParams();
  // PU-04: si se llegó desde la bandeja de aprobaciones, la ruta de navegación y el botón de
  // regreso llevan a la bandeja, en lugar de obligar a entrar de nuevo desde el menú.
  const { state: estadoNavegacion } = useLocation();
  const desdeAprobaciones = Boolean(estadoNavegacion?.desdeAprobaciones) || (puedeAprobar && !puedeConsultar);
  const [expediente, setExpediente] = useState(null);
  const [datos, setDatos] = useState(null);
  const [archivos, setArchivos] = useState(ARCHIVOS_VACIOS);
  const [comentario, setComentario] = useState("");
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [pestana, setPestana] = usePestanaActiva(["datos", "documentos", "bitacora"], "datos");
  const [guardando, setGuardando] = useState(false);
  const [documentoPorEliminar, setDocumentoPorEliminar] = useState(null);
  const [pendientesAbiertos, setPendientesAbiertos] = useState(false);
  const navegar = useNavigate();

  useEffect(() => {
    let vigente = true;

    obtenerExoneracion(referenciaInterna)
      .then((detalle) => {
        if (!vigente) {
          return;
        }
        setExpediente(detalle);
        setDatos({
          numeroContratacion: detalle.numeroContratacion ?? "",
          ordenCompraGeco: detalle.ordenCompraGeco ?? "",
          contratoSicop: detalle.contratoSicop ?? "",
          numeroExonet: detalle.numeroExonet ?? "",
          numeroAutorizacionExonet: detalle.numeroAutorizacionExonet ?? "",
          observaciones: detalle.observaciones ?? "",
        });
      })
      .catch((problema) => {
        if (vigente) {
          setAlerta({ tipo: "danger", texto: `No fue posible cargar el expediente. ${problema.message}` });
        }
      });

    return () => { vigente = false; };
  }, [referenciaInterna]);

  /**
   * Lleva a la persona usuaria al expediente con su referencia nueva. Cuando el expediente
   * se reclasifica, la pantalla correcta es la del tipo al que quedó.
   *
   * @param {object} respuesta datos que devuelve el cambio de referencia
   */
  function irAlExpediente(respuesta) {
    const ruta = respuesta.codigoTipoTramite === "EXONERACION"
      ? "/tramites/exoneraciones/"
      : "/tramites/expedientes/";
    navegar(`${ruta}${encodeURIComponent(respuesta.referenciaInterna)}`);
  }

  function refrescar(detalle) {
    setExpediente(detalle);
    setDatos({
      numeroContratacion: detalle.numeroContratacion ?? "",
      ordenCompraGeco: detalle.ordenCompraGeco ?? "",
      contratoSicop: detalle.contratoSicop ?? "",
      numeroExonet: detalle.numeroExonet ?? "",
      numeroAutorizacionExonet: detalle.numeroAutorizacionExonet ?? "",
      observaciones: detalle.observaciones ?? "",
    });
  }

  async function guardarDatos(evento) {
    evento.preventDefault();
    setAlerta(null);
    setGuardando(true);
    try {
      refrescar(await corregirDatosExoneracion(referenciaInterna, datos));
      setAlerta({
        tipo: "success",
        texto: "Los datos de la solicitud se corrigieron y el cambio quedó anotado en la bitácora.",
      });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible corregir los datos. ${problema.message}` });
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
      refrescar(await agregarDocumentosExoneracion(referenciaInterna, archivos));
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
      refrescar(await eliminarDocumentoExoneracion(referenciaInterna, documentoPorEliminar.idDocumento));
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

  async function comentar(evento) {
    evento.preventDefault();
    setAlerta(null);

    if (comentario.trim().length === 0) {
      setAlerta({ tipo: "warning", texto: "Escriba el comentario antes de agregarlo." });
      return;
    }

    setGuardando(true);
    try {
      const bitacora = await agregarComentario(referenciaInterna, comentario.trim());
      setExpediente((anterior) => ({ ...anterior, bitacora }));
      setComentario("");
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible agregar el comentario. ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  async function borrarComentario(idBitacora) {
    setAlerta(null);
    setGuardando(true);
    try {
      const bitacora = await eliminarComentario(referenciaInterna, idBitacora);
      setExpediente((anterior) => ({ ...anterior, bitacora }));
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible eliminar el comentario. ${problema.message}` });
    } finally {
      setGuardando(false);
    }
  }

  if (!expediente || !datos) {
    return (
      <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
        {alerta
          ? <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>
          : <p className="text-muted">Cargando el expediente...</p>}
        <Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CREAR]}>
          <Link className="btn btn-outline-secondary" to="/tramites/exoneracion">
            Registrar una solicitud
          </Link>
        </Cmp_SIM_ConPermiso>
      </Cmp_SIM_Marco>
    );
  }

  const documentosVigentes = (expediente.documentos ?? [])
    .filter((documento) => documento.estadoDocumento === "ACT");
  const pendientes = expediente.documentosPendientes ?? [];
  const documentosPorAdjuntar = DOCUMENTOS_EXONERACION.map((documento) => ({
    ...documento,
    obligatorio: documento.obligatorio && pendientes.includes(documento.nombreCatalogo),
  }));

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          {desdeAprobaciones && puedeAprobar
            ? <li className="breadcrumb-item"><Link to="/tramites/exoneraciones/aprobaciones">Aprobaciones</Link></li>
            : <li className="breadcrumb-item"><Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CONSULTAR]} alterno="Exoneraciones"><Link to="/tramites/exoneraciones">Exoneraciones</Link></Cmp_SIM_ConPermiso></li>}
          <li className="breadcrumb-item active" aria-current="page">{expediente.referenciaInterna}</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">
            Expediente <span className="sim-referencia">{expediente.referenciaInterna}</span>
          </h1>
          <div className="d-flex align-items-center gap-2">
            {puedeConsultar && (
              <Link className="btn btn-sm btn-outline-secondary" id="btnObservaciones"
                    to={`/tramites/exoneraciones/${encodeURIComponent(expediente.referenciaInterna)}/observaciones`}
                    state={{ idTramite: expediente.idTramite, estado: expediente.estado }}>
                <i className="bi bi-chat-square-text me-1" aria-hidden="true"></i>Observaciones
              </Link>
            )}
            {puedeConsultar && (
              <Link className="btn btn-sm btn-outline-secondary" id="btnRevisionDocumental"
                    to={`/tramites/exoneraciones/`+ `${encodeURIComponent(expediente.referenciaInterna)}`+ "/revision-documental"}
                    state={{ idTramite: expediente.idTramite, estado: expediente.estado }}>
                    <i className="bi bi-file-earmark-check me-1" aria-hidden="true" /> Revisar documentos
                </Link>
            )}
            <Link className="btn btn-sm btn-outline-secondary" id="btnControlDocumental"
                  to={`/tramites/exoneraciones/${encodeURIComponent(expediente.referenciaInterna)}/control-documental`}
                  state={{ idTramite: expediente.idTramite, estado: expediente.estado }}>
              <i className="bi bi-list-check me-1" aria-hidden="true"></i> Control documental
            </Link>
            {puedeAprobar && (
              <Link className="btn btn-sm btn-outline-secondary" id="btnAprobacionExoneracion"
                    to={`/tramites/exoneraciones/${encodeURIComponent(expediente.referenciaInterna)}/aprobacion`}
                    state={{ idTramite: expediente.idTramite, desdeAprobaciones }}>
                <i className="bi bi-patch-check me-1" aria-hidden="true"></i> Aprobación
              </Link>
            )}
            {puedeConsultar && (
              <Link className="btn btn-sm btn-outline-secondary" id="btnAutorizacionExonet"
                    to={`/tramites/exoneraciones/${encodeURIComponent(expediente.referenciaInterna)}/autorizacion-exonet`}
                    state={{ idTramite: expediente.idTramite, estado: expediente.estado }}>
                <i className="bi bi-patch-check me-1" aria-hidden="true"></i>Autorización EXONET
              </Link>
            )}
            {puedeModificar && (
              <Cmp_SIM_CambioReferencia referenciaInterna={expediente.referenciaInterna}
                                        codigoTipoActual="EXONERACION"
                                        onCambiado={irAlExpediente} />
            )}
            <span className="badge text-bg-warning fs-6" id="lblEstadoActual">{expediente.estado}</span>
            {desdeAprobaciones && puedeAprobar && (
              <Link className="btn btn-sm btn-outline-secondary" id="btnVolverAprobaciones"
                    to="/tramites/exoneraciones/aprobaciones">
                <i className="bi bi-arrow-left me-1" aria-hidden="true"></i>Volver a aprobaciones
              </Link>
            )}
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Consulte y corrija los datos de la solicitud de exoneración, su documentación y su bitácora. Desde aquí se abren la revisión documental, las observaciones y la autorización EXONET.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}
      {!puedeModificar && <Cmp_SIM_AvisoSoloConsulta />}

      {pendientes.length > 0 && (
        <div className="alert alert-warning py-2" role="alert">
          <button type="button"
                  className="btn btn-link p-0 text-start text-reset text-decoration-none w-100 d-flex align-items-center gap-2"
                  id="btnPendientes"
                  aria-expanded={pendientesAbiertos}
                  aria-controls="lstPendientes"
                  onClick={() => setPendientesAbiertos(!pendientesAbiertos)}>
            <i className="bi bi-exclamation-triangle" aria-hidden="true"></i>
            <span className="text-truncate flex-grow-1">
              <strong>Pendiente de adjuntar:</strong> {pendientes.join(", ")}
            </span>
            <span className="badge text-bg-warning">{pendientes.length}</span>
            <i className={`bi ${pendientesAbiertos ? "bi-chevron-up" : "bi-chevron-down"}`}
               aria-hidden="true"></i>
          </button>
          {pendientesAbiertos && (
            <ul className="mb-0 mt-2" id="lstPendientes">
              {pendientes.map((nombre) => <li key={nombre}>{nombre}</li>)}
            </ul>
          )}
        </div>
      )}

      {/* PU-02 y comentario de la Unidad: la última entrada de la bitácora se ve al entrar, y cada
          apartado del expediente queda en su pestaña, sin tener que bajar toda la página. */}
      {ultimoPorFecha(expediente.bitacora) && (
        <p className="small text-muted mb-2" id="lblUltimoMovimiento">
          <i className="bi bi-journal-text me-1" aria-hidden="true"></i>
          Última anotación de la bitácora: {formatearFechaHora(ultimoPorFecha(expediente.bitacora).fechaHora)} ·{" "}
          {ultimoPorFecha(expediente.bitacora).descripcionAccion ?? ultimoPorFecha(expediente.bitacora).accion} ·{" "}
          {ultimoPorFecha(expediente.bitacora).usuario}
          <button type="button" className="btn btn-link btn-sm p-0 ms-2 align-baseline"
                  onClick={() => setPestana("bitacora")}>Ver bitácora</button>
        </p>
      )}
      <Cmp_SIM_Pestanas
        activa={pestana}
        alCambiar={setPestana}
        pestanas={[
          { id: "datos", titulo: "Datos de la solicitud", icono: "bi-file-earmark-text" },
          { id: "documentos", titulo: "Documentación", icono: "bi-paperclip", contador: documentosVigentes.length },
          { id: "bitacora", titulo: "Bitácora", icono: "bi-journal-text", contador: (expediente.bitacora ?? []).length },
        ]}
      />

      {pestana === "datos" && (<>
      <div className="card sim-card mb-4">
        <div className="card-header">
          <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>Datos de la solicitud
        </div>
        <div className="card-body">
          <dl className="row">
            <dt className="col-12 col-md-4">Tipo de exoneración</dt>
            <dd className="col-12 col-md-8">
              {NOMBRES_TIPO[expediente.tipoExoneracion] ?? expediente.tipoExoneracion}
            </dd>
            <dt className="col-12 col-md-4">Usuario responsable</dt>
            <dd className="col-12 col-md-8">{expediente.usuarioResponsable ?? "—"}</dd>
            <dt className="col-12 col-md-4">Fecha de registro</dt>
            <dd className="col-12 col-md-8">{formatearFechaHora(expediente.fechaCreacion)}</dd>
          </dl>

          <hr />

          <form onSubmit={guardarDatos} noValidate>
            <fieldset disabled={!puedeModificar}>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtNumeroContratacion">
                    Número de contratación
                    <Cmp_SIM_AyudaCampo titulo="el número de contratación">
                      Corrija el número cuando los agentes lo verifiquen con el usuario. El valor
                      anterior queda registrado en la bitácora.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control sim-referencia" id="txtNumeroContratacion"
                         value={datos.numeroContratacion}
                         onChange={alEscribirConMascara(
                           (valor) => setDatos({ ...datos, numeroContratacion: valor }),
                           PLANTILLAS.contratacion
                         )} />
                </div>

                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtOrdenCompraGeco">Orden de compra GECO</label>
                  <input type="text" className="form-control sim-referencia" id="txtOrdenCompraGeco"
                         value={datos.ordenCompraGeco}
                         onChange={alEscribirConMascara(
                           (valor) => setDatos({ ...datos, ordenCompraGeco: valor }),
                           PLANTILLAS.ordenCompra
                         )} />
                </div>

                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtContratoSicop">
                    Contrato SICOP <span className="sim-opcional">(opcional)</span>
                  </label>
                  <input type="text" className="form-control sim-referencia" id="txtContratoSicop"
                         value={datos.contratoSicop}
                         onChange={alEscribirConMascara(
                           (valor) => setDatos({ ...datos, contratoSicop: valor }),
                           PLANTILLAS.contrato
                         )} />
                </div>

                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtNumeroExonet">
                    Número de solicitud EXONET
                    <Cmp_SIM_AyudaCampo titulo="el número de solicitud EXONET">
                      Primer número que devuelve EXONET al presentar la solicitud. Antótelo apenas
                      lo reciba para darle seguimiento al trámite.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control sim-referencia" id="txtNumeroExonet"
                         maxLength={45} value={datos.numeroExonet}
                         onChange={(evento) =>
                           setDatos({ ...datos, numeroExonet: evento.target.value.toUpperCase() })} />
                </div>

                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtNumeroAutorizacionExonet">
                    Número de autorización EXONET
                    <Cmp_SIM_AyudaCampo titulo="el número de autorización EXONET">
                      Segundo número de EXONET, el de la autorización. Es el que se presenta en
                      aduana y el que cierra el seguimiento de la exoneración.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control sim-referencia"
                         id="txtNumeroAutorizacionExonet" maxLength={45}
                         value={datos.numeroAutorizacionExonet}
                         onChange={(evento) => setDatos({
                           ...datos, numeroAutorizacionExonet: evento.target.value.toUpperCase(),
                         })} />
                </div>

                <div className="col-12">
                  {/* PU-11: el campo guarda la descripción del bien que se exonera, no observaciones. */}
                  <label className="form-label" htmlFor="txtObservaciones">Descripción del bien exonerado</label>
                  <textarea className="form-control" id="txtObservaciones" rows={3} maxLength={4000}
                            value={datos.observaciones}
                            onChange={(evento) => setDatos({ ...datos, observaciones: evento.target.value })} />
                  <div className="form-text text-end">{datos.observaciones.length}/4000</div>
                </div>
              </div>

              {puedeModificar && (
                <div className="d-flex justify-content-end gap-2 mt-3">
                  <button type="button" className="btn btn-outline-secondary" id="btnDeshacerDatos"
                          onClick={() => refrescar(expediente)}>
                    Deshacer
                  </button>
                  <button type="submit" className="btn btn-sim" id="btnGuardarDatos" disabled={guardando}>
                    <i className="bi bi-save me-1" aria-hidden="true"></i>Guardar correcciones
                  </button>
                </div>
              )}
            </fieldset>
          </form>
        </div>
      </div>

      <Cmp_SIM_CostosTramite referenciaInterna={referenciaInterna} expediente={expediente}
                             soloImpuestosExonerados
                             soloLectura={!puedeModificar}
                             alGuardar={() => obtenerExoneracion(referenciaInterna).then(refrescar)} />

      </>)}

      {pestana === "documentos" && (<>
      <div className="card sim-card mb-4">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-paperclip me-2" aria-hidden="true"></i>Documentación del expediente</span>
          <span className="badge text-bg-light text-dark">{documentosVigentes.length}</span>
        </div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Documentos asociados a la solicitud</caption>
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
                      La solicitud todavía no tiene documentación adjunta.
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
              <Cmp_SIM_Documentos documentos={documentosPorAdjuntar} archivos={archivos}
                                  alCambiar={setArchivos} />

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

      {pestana === "bitacora" && (<>
      <div className="card sim-card mb-5" id="seccionBitacora">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span>
            <i className="bi bi-journal-text me-2" aria-hidden="true"></i>Bitácora del expediente
            <span className="d-none d-print-inline"> {expediente.referenciaInterna}</span>
          </span>
          <Cmp_SIM_ImprimirBitacora idSeccion="seccionBitacora" />
        </div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Acciones registradas sobre la solicitud</caption>
              <thead>
                <tr>
                  <th scope="col">Fecha</th>
                  <th scope="col">Acción</th>
                  <th scope="col">Usuario</th>
                  <th scope="col">Detalle</th>
                  <th scope="col" className="text-end d-print-none">
                    <span className="visually-hidden">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {(expediente.bitacora ?? []).length === 0 && (
                  <tr><td colSpan={5} className="text-muted small">La bitácora está vacía.</td></tr>
                )}

                {(expediente.bitacora ?? []).map((movimiento) => (
                  <tr key={movimiento.idBitacora}>
                    <td>{formatearFechaHora(movimiento.fechaHora)}</td>
                    <td>{movimiento.descripcionAccion ?? movimiento.accion}</td>
                    <td>{movimiento.usuario}</td>
                    <td>
                      {movimiento.datosAnteriores && (
                        <div className="text-muted small">Antes: {movimiento.datosAnteriores}</div>
                      )}
                      <div>{movimiento.datosNuevos}</div>
                    </td>
                    <td className="text-end d-print-none">
                      {movimiento.accion === "COMENTARIO" && puedeModificar && (
                        <button type="button" className="btn btn-sm btn-outline-danger"
                                onClick={() => borrarComentario(movimiento.idBitacora)}
                                disabled={guardando}>
                          Eliminar
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {puedeModificar && (
            <form className="mt-3 d-print-none" onSubmit={comentar} noValidate>
              <label className="form-label" htmlFor="txtComentario">
                Agregar un comentario
                <Cmp_SIM_AyudaCampo titulo="los comentarios">
                  Sirven para dejar constancia de la gestión: llamadas, correos o acuerdos con la
                  persona usuaria. Quedan en la bitácora con su fecha y su usuario.
                </Cmp_SIM_AyudaCampo>
              </label>
              <textarea className="form-control" id="txtComentario" rows={2} maxLength={1000}
                        value={comentario}
                        onChange={(evento) => setComentario(evento.target.value)} />
              <div className="d-flex justify-content-between align-items-center mt-2">
                <span className="form-text mb-0">{comentario.length}/1000</span>
                <button type="submit" className="btn btn-sim btn-sm" id="btnAgregarComentario"
                        disabled={guardando}>
                  <i className="bi bi-chat-left-text me-1" aria-hidden="true"></i>Agregar comentario
                </button>
              </div>
            </form>
          )}
        </div>
      </div>

      </>)}

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
                    Como ya estaba guardado, el archivo se conserva y la eliminación queda registrada
                    en la bitácora, con su usuario y su fecha.
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

export default Frm_SIM_ExpedienteExoneracion;
