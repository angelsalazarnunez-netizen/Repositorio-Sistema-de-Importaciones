import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import "./Frm_SIM_AutorizacionExonet.css";
import { Cmp_SIM_IndicacionArchivos } from "../../componentes/Cmp_SIM_Documentos.jsx";
import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso, {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  anularAutorizacionExonet,
  completarAutorizacionExonet,
  descargarDocumento,
  obtenerAutorizacionesExonet,
  obtenerExoneracion,
  registrarAutorizacionExonet,
  sustituirAutorizacionExonet,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Solicitud / autorización EXONET de un expediente de exoneración (HU-023).
 *   Muestra la autorización vigente y el historial completo, con la relación entre las
 *   autorizaciones sustituidas y sus sustitutas. Permite registrar una autorización cuando no
 *   hay vigente (con la precarga de la solicitud si el expediente no tiene historial), completar
 *   los datos que la vigente tenga vacíos, anularla o sustituirla, y adjuntar un documento
 *   opcional. El usuario responsable lo toma el servidor de la sesión: la pantalla nunca lo envía.
 *   Las reglas se validan primero aquí para orientar a la persona usuaria, pero quien decide es el
 *   servidor: sus mensajes de error se muestran tal como llegan.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin EXONERACION_MODIFICAR la pantalla es de
 *     consulta: se muestran la autorización vigente y el historial, sin el formulario ni las acciones
 *     de anular, sustituir o completar.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla; indicación de formato y tamaño del archivo (H4 del
 *     Sprint 1); la ruta de navegación lleva a la lista de exoneraciones; el aviso lleva la vista
 *     hasta el mensaje.
 */

const USUARIO_ACTUAL = "Técnica de Exoneraciones";
// Texto provisional (K-10): la denominación visible del número AI-####-## aún puede cambiar.
const ETIQUETA_NUMERO_AUTORIZACION = "Número de autorización";
const LONGITUD_MAXIMA_NUMERO = 45;
const LONGITUD_MAXIMA_OBSERVACIONES = 1000;
const LONGITUD_MAXIMA_MOTIVO = 500;

const MODO_REGISTRAR = "registrar";
const MODO_COMPLETAR = "completar";
const MODO_SUSTITUIR = "sustituir";

const FORMULARIO_VACIO = {
  numeroSolicitudExonet: "",
  fechaEnvioExonet: "",
  numeroAutorizacion: "",
  fechaAutorizacion: "",
  fechaComunicacion: "",
  observaciones: "",
  motivo: "",
};
const AUTORIZACIONES_VACIAS = { vigente: null, historial: [], precarga: null };

const CAMPOS_COMPLETABLES = [
  "numeroSolicitudExonet",
  "fechaEnvioExonet",
  "numeroAutorizacion",
  "fechaAutorizacion",
  "fechaComunicacion",
  "observaciones",
];

const CLASE_ESTADO = {
  VIG: "text-bg-success",
  ANU: "text-bg-danger",
  SUS: "text-bg-secondary",
};

const AYUDA = {
  titulo: "Ayuda: solicitud y autorización EXONET",
  introduccion:
    "Aquí se registra lo que ocurre en EXONET con la solicitud de exoneración: el número de "
    + "solicitud y su fecha de envío, el número y la fecha de la autorización, y la fecha en que se "
    + "comunicó al interesado. El sistema solo registra la resolución: no genera autorizaciones.",
  pasos: [
    "Si el expediente no tiene una autorización vigente, registre al menos el número de solicitud EXONET.",
    "Cuando llegue la autorización, use Completar datos pendientes para agregar su número y su fecha.",
    "Si un dato ya registrado es incorrecto o la autorización cambió, use Sustituir: la anterior se conserva.",
    "Si la autorización dejó de tener efecto, use Anular. Después podrá registrar una nueva.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué no puedo corregir un dato que ya registré?",
      respuesta:
        "Para conservar el historial del expediente. Completar solo llena datos vacíos; para "
        + "cambiar un dato existente se registra una autorización sustituta.",
    },
    {
      pregunta: "¿Se borra algo al anular o sustituir?",
      respuesta:
        "No. La autorización anterior queda en el historial con quién la anuló o sustituyó, cuándo "
        + "y por qué.",
    },
    {
      pregunta: "¿De dónde salen los números precargados?",
      respuesta:
        "De la solicitud de exoneración, cuando el expediente todavía no tiene ninguna autorización "
        + "registrada aquí. Verifíquelos antes de guardar.",
    },
  ],
};

/** Fecha local de hoy en el formato del control de fecha (aaaa-mm-dd). */
function obtenerHoy() {
  const hoy = new Date();
  const mes = String(hoy.getMonth() + 1).padStart(2, "0");
  const dia = String(hoy.getDate()).padStart(2, "0");
  return `${hoy.getFullYear()}-${mes}-${dia}`;
}

/** Muestra una fecha aaaa-mm-dd como dd/mm/aaaa, sin conversión de zona horaria. */
function formatearFecha(valor) {
  if (!valor) {
    return "—";
  }
  const [anio, mes, dia] = String(valor).split("-");
  return `${dia}/${mes}/${anio}`;
}

function formatearFechaHora(valor) {
  return valor
    ? new Date(valor).toLocaleString("es-CR", {
        day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit",
      })
    : "—";
}

function normalizarIdTramite(valor) {
  const numero = Number(valor);
  return Number.isInteger(numero) && numero > 0 ? numero : null;
}

function normalizarAutorizaciones(respuesta) {
  return {
    vigente: respuesta?.vigente ?? null,
    historial: respuesta?.historial ?? [],
    precarga: respuesta?.precarga ?? null,
  };
}

function tieneValor(valor) {
  return valor !== undefined && valor !== null && String(valor).trim() !== "";
}

/** Nombre corto de una autorización para describir la cadena de sustituciones. */
function describirAutorizacion(autorizacion) {
  if (!autorizacion) {
    return "";
  }
  const numero = autorizacion.numeroAutorizacion ?? autorizacion.numeroSolicitudExonet;
  return numero ? `#${autorizacion.idAutorizacion} · ${numero}` : `#${autorizacion.idAutorizacion}`;
}

/** Datos pendientes de la vigente: los que siguen vacíos, más el documento si no tiene. */
function obtenerPendientes(vigente) {
  if (!vigente) {
    return [];
  }
  const pendientes = CAMPOS_COMPLETABLES.filter((campo) => !tieneValor(vigente[campo]));
  if (!vigente.idDocumento) {
    pendientes.push("documento");
  }
  return pendientes;
}

function formularioDesdeAutorizacion(autorizacion) {
  return {
    ...FORMULARIO_VACIO,
    numeroSolicitudExonet: autorizacion?.numeroSolicitudExonet ?? "",
    fechaEnvioExonet: autorizacion?.fechaEnvioExonet ?? "",
    numeroAutorizacion: autorizacion?.numeroAutorizacion ?? "",
    fechaAutorizacion: autorizacion?.fechaAutorizacion ?? "",
    fechaComunicacion: autorizacion?.fechaComunicacion ?? "",
    observaciones: autorizacion?.observaciones ?? "",
  };
}

/**
 * Validación previa en la pantalla, sobre el estado final que quedaría guardado. Repite las
 * reglas del servidor solo para orientar; el servidor vuelve a aplicarlas y es el que decide.
 */
function validarFormulario(formulario, modo, vigente, archivo) {
  const errores = {};
  // Al completar, los datos ya registrados de la vigente forman parte del estado final.
  const final = modo === MODO_COMPLETAR
    ? Object.fromEntries(CAMPOS_COMPLETABLES.map((campo) => [
        campo, tieneValor(vigente?.[campo]) ? String(vigente[campo]) : formulario[campo].trim(),
      ]))
    : Object.fromEntries(CAMPOS_COMPLETABLES.map((campo) => [campo, formulario[campo].trim()]));
  const hoy = obtenerHoy();

  if (final.numeroSolicitudExonet.length > LONGITUD_MAXIMA_NUMERO) {
    errores.numeroSolicitudExonet = `Máximo ${LONGITUD_MAXIMA_NUMERO} caracteres.`;
  }
  if (final.numeroAutorizacion.length > LONGITUD_MAXIMA_NUMERO) {
    errores.numeroAutorizacion = `Máximo ${LONGITUD_MAXIMA_NUMERO} caracteres.`;
  }
  if (final.observaciones.length > LONGITUD_MAXIMA_OBSERVACIONES) {
    errores.observaciones = `Máximo ${LONGITUD_MAXIMA_OBSERVACIONES} caracteres.`;
  }
  if (!final.numeroSolicitudExonet && !final.numeroAutorizacion) {
    errores.numeroSolicitudExonet = "Indique el número de solicitud EXONET o el número de autorización.";
  }
  if (final.fechaEnvioExonet && !final.numeroSolicitudExonet) {
    errores.fechaEnvioExonet = "La fecha de envío requiere el número de solicitud EXONET.";
  }
  ["fechaEnvioExonet", "fechaAutorizacion", "fechaComunicacion"].forEach((campo) => {
    if (final[campo] && final[campo] > hoy) {
      errores[campo] = "La fecha no puede ser posterior a hoy.";
    }
  });
  if (final.numeroAutorizacion && !final.fechaAutorizacion) {
    errores.fechaAutorizacion = "Indique la fecha de la autorización.";
  } else if (!final.numeroAutorizacion && final.fechaAutorizacion) {
    errores.numeroAutorizacion = `Indique el ${ETIQUETA_NUMERO_AUTORIZACION.toLowerCase()}.`;
  }
  if (final.fechaComunicacion && !final.numeroAutorizacion) {
    errores.fechaComunicacion = "La fecha de comunicación requiere la autorización.";
  }
  if (!errores.fechaAutorizacion && final.fechaEnvioExonet && final.fechaAutorizacion
      && final.fechaEnvioExonet > final.fechaAutorizacion) {
    errores.fechaAutorizacion = "La autorización no puede ser anterior al envío.";
  }
  if (!errores.fechaComunicacion && final.fechaComunicacion
      && ((final.fechaAutorizacion && final.fechaAutorizacion > final.fechaComunicacion)
        || (final.fechaEnvioExonet && final.fechaEnvioExonet > final.fechaComunicacion))) {
    errores.fechaComunicacion = "La comunicación no puede ser anterior al envío ni a la autorización.";
  }

  if (modo === MODO_COMPLETAR) {
    const completa = CAMPOS_COMPLETABLES.some((campo) =>
      !tieneValor(vigente?.[campo]) && formulario[campo].trim() !== "");
    if (!completa && !archivo) {
      errores.general = "Indique al menos un dato pendiente o adjunte el documento.";
    }
  }
  if (modo === MODO_SUSTITUIR) {
    const motivo = formulario.motivo.trim();
    if (!motivo) {
      errores.motivo = "Indique el motivo de la sustitución.";
    } else if (motivo.length > LONGITUD_MAXIMA_MOTIVO) {
      errores.motivo = `Máximo ${LONGITUD_MAXIMA_MOTIVO} caracteres.`;
    }
  }
  return errores;
}

/** Al completar solo se envían los datos que la vigente tenía vacíos. */
function datosParaEnviar(formulario, modo, vigente) {
  return Object.fromEntries(CAMPOS_COMPLETABLES
    .filter((campo) => modo !== MODO_COMPLETAR || !tieneValor(vigente?.[campo]))
    .map((campo) => [campo, formulario[campo].trim()]));
}

function Frm_SIM_AutorizacionExonet() {
  // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
  const puedeModificar = useSesionActual().tieneAlguno([PERMISOS.EXONERACION_MODIFICAR]);
  const { referenciaInterna } = useParams();
  const { state: estadoNavegacion } = useLocation();
  const idTramiteNavegacion = normalizarIdTramite(estadoNavegacion?.idTramite);

  const [idTramite, setIdTramite] = useState(null);
  const [estadoExpediente, setEstadoExpediente] = useState(estadoNavegacion?.estado ?? null);
  const [autorizaciones, setAutorizaciones] = useState(AUTORIZACIONES_VACIAS);
  const [cargando, setCargando] = useState(true);
  const [errorCarga, setErrorCarga] = useState(null);
  const [intentoCarga, setIntentoCarga] = useState(0);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [modo, setModo] = useState(null);
  const [formulario, setFormulario] = useState(FORMULARIO_VACIO);
  const [archivo, setArchivo] = useState(null);
  const [claveArchivo, setClaveArchivo] = useState(0);
  const [errores, setErrores] = useState({});
  const [usaPrecarga, setUsaPrecarga] = useState(false);
  const [anulacion, setAnulacion] = useState(null);
  const [procesando, setProcesando] = useState(false);

  const responsable = obtenerSesion()?.nombreCompleto || USUARIO_ACTUAL;
  const hoy = obtenerHoy();
  const { vigente, historial } = autorizaciones;
  const pendientes = obtenerPendientes(vigente);

  /**
   * Aplica el historial que devuelve el servidor y deja el formulario en el modo que corresponde:
   * registrar si no hay vigente (con la precarga si el expediente no tiene historial) o solo
   * consulta si la hay.
   */
  function aplicarAutorizaciones(respuesta) {
    const datos = normalizarAutorizaciones(respuesta);
    setAutorizaciones(datos);
    setErrores({});
    setArchivo(null);
    setClaveArchivo((clave) => clave + 1);
    if (datos.vigente) {
      setModo(null);
      setFormulario(FORMULARIO_VACIO);
      setUsaPrecarga(false);
    } else {
      setModo(MODO_REGISTRAR);
      const precarga = datos.historial.length === 0 ? datos.precarga : null;
      setUsaPrecarga(Boolean(precarga));
      setFormulario({
        ...FORMULARIO_VACIO,
        numeroSolicitudExonet: precarga?.numeroSolicitudExonet ?? "",
        numeroAutorizacion: precarga?.numeroAutorizacion ?? "",
      });
    }
  }

  // Obtiene el idTramite del flujo del expediente y carga sus autorizaciones. Si se entró directo
  // por la dirección o se recargó la página, se toma del detalle del expediente por su referencia.
  useEffect(() => {
    let activa = true;

    async function cargar() {
      setCargando(true);
      setErrorCarga(null);
      try {
        let id = idTramiteNavegacion;
        if (id === null) {
          const detalle = await obtenerExoneracion(referenciaInterna);
          id = normalizarIdTramite(detalle?.idTramite);
          if (activa) {
            setEstadoExpediente(detalle?.estado ?? null);
          }
          if (id === null) {
            throw new Error("El expediente no indica su identificador.");
          }
        }
        const respuesta = await obtenerAutorizacionesExonet(id);
        if (activa) {
          setIdTramite(id);
          aplicarAutorizaciones(respuesta);
        }
      } catch (problema) {
        if (activa) {
          setErrorCarga(`No fue posible cargar la autorización EXONET. ${problema.message}`);
        }
      } finally {
        if (activa) {
          setCargando(false);
        }
      }
    }

    cargar();
    return () => { activa = false; };
    // aplicarAutorizaciones solo usa setters de estado, que no cambian entre renderizados.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [referenciaInterna, idTramiteNavegacion, intentoCarga]);

  function actualizarCampo(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: undefined, general: undefined }));
  }

  function iniciarModo(nuevoModo) {
    setAlerta(null);
    setErrores({});
    setArchivo(null);
    setClaveArchivo((clave) => clave + 1);
    setModo(nuevoModo);
    // Al sustituir se precargan los datos de la vigente para corregir solo lo necesario; la
    // nueva no hereda nada que no se envíe.
    setFormulario(nuevoModo === MODO_SUSTITUIR ? formularioDesdeAutorizacion(vigente) : FORMULARIO_VACIO);
    window.setTimeout(() => {
      document.getElementById("frmAutorizacion")?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 0);
  }

  function cancelarModo() {
    setErrores({});
    setArchivo(null);
    setClaveArchivo((clave) => clave + 1);
    setFormulario(FORMULARIO_VACIO);
    setModo(vigente ? null : MODO_REGISTRAR);
  }

  async function guardar(evento) {
    evento.preventDefault();
    setAlerta(null);
    const encontrados = validarFormulario(formulario, modo, vigente, archivo);
    setErrores(encontrados);
    if (Object.keys(encontrados).length > 0) {
      return;
    }

    const datos = datosParaEnviar(formulario, modo, vigente);
    setProcesando(true);
    try {
      let respuesta;
      let mensaje;
      if (modo === MODO_REGISTRAR) {
        respuesta = await registrarAutorizacionExonet(idTramite, datos, archivo);
        mensaje = "La autorización EXONET se registró como vigente.";
      } else if (modo === MODO_COMPLETAR) {
        respuesta = await completarAutorizacionExonet(idTramite, vigente.idAutorizacion, datos, archivo);
        mensaje = "Los datos pendientes se completaron.";
      } else {
        respuesta = await sustituirAutorizacionExonet(
          idTramite, vigente.idAutorizacion, datos, formulario.motivo.trim(), archivo);
        mensaje = "La autorización se sustituyó. La anterior se conserva en el historial.";
      }
      aplicarAutorizaciones(respuesta);
      setAlerta({ tipo: "success", texto: mensaje });
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible guardar. ${problema.message}` });
    } finally {
      setProcesando(false);
    }
  }

  async function confirmarAnulacion() {
    const motivo = anulacion.motivo.trim();
    if (!motivo || motivo.length > LONGITUD_MAXIMA_MOTIVO) {
      setAnulacion({
        ...anulacion,
        error: motivo ? `Máximo ${LONGITUD_MAXIMA_MOTIVO} caracteres.` : "Indique el motivo de la anulación.",
      });
      return;
    }
    setAlerta(null);
    setProcesando(true);
    try {
      const respuesta = await anularAutorizacionExonet(idTramite, vigente.idAutorizacion, motivo);
      aplicarAutorizaciones(respuesta);
      setAnulacion(null);
      setAlerta({
        tipo: "success",
        texto: "La autorización se anuló y se conserva en el historial. Puede registrar una nueva.",
      });
    } catch (problema) {
      setAnulacion({ ...anulacion, error: problema.message });
    } finally {
      setProcesando(false);
    }
  }

  async function descargar(autorizacion) {
    try {
      await descargarDocumento(autorizacion.idDocumento, autorizacion.nombreArchivoDocumento);
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible descargar el documento. ${problema.message}` });
    }
  }

  const rutaExpediente = `/tramites/exoneraciones/${encodeURIComponent(referenciaInterna)}`;

  const migas = (
    <nav aria-label="Ruta de navegación">
      <ol className="breadcrumb small">
        <li className="breadcrumb-item"><Cmp_SIM_ConPermiso permisos={[PERMISOS.EXONERACION_CONSULTAR]} alterno="Exoneraciones"><Link to="/tramites/exoneraciones">Exoneraciones</Link></Cmp_SIM_ConPermiso></li>
        <li className="breadcrumb-item"><Link to={rutaExpediente}>{referenciaInterna}</Link></li>
        <li className="breadcrumb-item active" aria-current="page">Autorización EXONET</li>
      </ol>
    </nav>
  );

  if (cargando || errorCarga) {
    return (
      <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
        {migas}
        {cargando
          ? <p className="text-muted">Cargando la autorización EXONET...</p>
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

  const porId = new Map(historial.map((autorizacion) => [autorizacion.idAutorizacion, autorizacion]));

  /**
   * Control de un dato del formulario. Al completar, un dato ya registrado se muestra de solo
   * lectura: no puede sobrescribirse.
   */
  function campo({ nombre, etiqueta, tipo = "text", ayuda, columnas = "col-12 col-md-6", mayusculas = false }) {
    const bloqueado = modo === MODO_COMPLETAR && tieneValor(vigente?.[nombre]);
    const valor = bloqueado ? String(vigente[nombre]) : formulario[nombre];
    const id = `txt${nombre.charAt(0).toUpperCase()}${nombre.slice(1)}`;
    return (
      <div className={columnas}>
        <label className="form-label" htmlFor={id}>
          {etiqueta}
          {ayuda && <Cmp_SIM_AyudaCampo titulo={etiqueta.toLowerCase()}>{ayuda}</Cmp_SIM_AyudaCampo>}
          {bloqueado && <span className="text-muted small ms-1">(registrado)</span>}
        </label>
        {tipo === "textarea" ? (
          <textarea id={id} rows={3}
                    className={`form-control${errores[nombre] ? " is-invalid" : ""}`}
                    maxLength={LONGITUD_MAXIMA_OBSERVACIONES}
                    value={valor} readOnly={bloqueado}
                    onChange={(evento) => actualizarCampo(nombre, evento.target.value)} />
        ) : (
          <input type={tipo} id={id}
                 className={`form-control${mayusculas ? " sim-referencia" : ""}${errores[nombre] ? " is-invalid" : ""}`}
                 maxLength={tipo === "text" ? LONGITUD_MAXIMA_NUMERO : undefined}
                 max={tipo === "date" ? hoy : undefined}
                 value={valor} readOnly={bloqueado}
                 onChange={(evento) => actualizarCampo(
                   nombre, mayusculas ? evento.target.value.toUpperCase() : evento.target.value)} />
        )}
        {errores[nombre] && <div className="invalid-feedback">{errores[nombre]}</div>}
      </div>
    );
  }

  const tituloFormulario = {
    [MODO_REGISTRAR]: "Registrar solicitud / autorización EXONET",
    [MODO_COMPLETAR]: "Completar datos pendientes",
    [MODO_SUSTITUIR]: "Sustituir la autorización vigente",
  }[modo];

  const textoBotonGuardar = {
    [MODO_REGISTRAR]: "Registrar",
    [MODO_COMPLETAR]: "Completar datos",
    [MODO_SUSTITUIR]: "Sustituir autorización",
  }[modo];

  const documentoBloqueado = modo === MODO_COMPLETAR && Boolean(vigente?.idDocumento);

  return (
    <Cmp_SIM_Marco usuario={USUARIO_ACTUAL} ayuda={AYUDA}>
      {migas}

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">
            Autorización EXONET <span className="sim-referencia">{referenciaInterna}</span>
          </h1>
          {estadoExpediente && <span className="badge text-bg-warning fs-6">{estadoExpediente}</span>}
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Registre la solicitud de autorización EXONET del expediente, complétela cuando Hacienda la resuelva o sustitúyala si cambia. Cada movimiento queda en el historial.
        </p>
      </section>

      {alerta && <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>{alerta.texto}</div>}
      {!puedeModificar && (
        <Cmp_SIM_AvisoSoloConsulta texto={"Usted puede consultar la autorización EXONET y su historial, pero sus "
          + "roles no le permiten registrarla, completarla, anularla ni sustituirla."} />
      )}

      {/* Autorización vigente y sus acciones. */}
      <div className="card sim-card mb-4 sim-autorizacion-vigente" id="seccionVigente">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span><i className="bi bi-patch-check me-2" aria-hidden="true"></i>Autorización vigente</span>
          {vigente && <span className="badge text-bg-success">Vigente</span>}
        </div>
        <div className="card-body">
          {!vigente ? (
            <p className="text-muted small mb-0">
              {historial.length === 0
                ? "El expediente todavía no tiene una solicitud / autorización EXONET registrada."
                : "El expediente no tiene una autorización vigente: la última fue anulada. "
                  + "Puede registrar una nueva."}
            </p>
          ) : (
            <>
              <dl className="row mb-0 small">
                <dt className="col-sm-4">Número de solicitud EXONET</dt>
                <dd className="col-sm-8 sim-referencia">{vigente.numeroSolicitudExonet ?? "—"}</dd>
                <dt className="col-sm-4">Fecha de envío a EXONET</dt>
                <dd className="col-sm-8">{formatearFecha(vigente.fechaEnvioExonet)}</dd>
                <dt className="col-sm-4">{ETIQUETA_NUMERO_AUTORIZACION}</dt>
                <dd className="col-sm-8 sim-referencia">{vigente.numeroAutorizacion ?? "—"}</dd>
                <dt className="col-sm-4">Fecha de autorización</dt>
                <dd className="col-sm-8">{formatearFecha(vigente.fechaAutorizacion)}</dd>
                <dt className="col-sm-4">Fecha de comunicación</dt>
                <dd className="col-sm-8">{formatearFecha(vigente.fechaComunicacion)}</dd>
                <dt className="col-sm-4">Documento</dt>
                <dd className="col-sm-8">
                  {vigente.idDocumento ? (
                    <button type="button" className="btn btn-link btn-sm p-0"
                            onClick={() => descargar(vigente)}>
                      <i className="bi bi-download me-1" aria-hidden="true"></i>
                      {vigente.nombreArchivoDocumento ?? "Descargar"}
                    </button>
                  ) : "—"}
                </dd>
                <dt className="col-sm-4">Observaciones</dt>
                <dd className="col-sm-8 text-break">{vigente.observaciones ?? "—"}</dd>
                <dt className="col-sm-4">Registrada por</dt>
                <dd className="col-sm-8">
                  {vigente.usuarioRegistro ?? "—"} · {formatearFechaHora(vigente.fechaRegistro)}
                </dd>
                {vigente.idAutorizacionAnterior && (
                  <>
                    <dt className="col-sm-4">Sustituye a</dt>
                    <dd className="col-sm-8">
                      <a href={`#autorizacion-${vigente.idAutorizacionAnterior}`}>
                        {describirAutorizacion(porId.get(vigente.idAutorizacionAnterior))
                          || `#${vigente.idAutorizacionAnterior}`}
                      </a>
                    </dd>
                  </>
                )}
              </dl>

              {pendientes.length > 0 && (
                <div className="alert alert-warning py-2 small mt-3 mb-0" role="status">
                  Datos pendientes: {pendientes.length} sin registrar. Puede completarlos sin
                  cambiar los que ya están registrados.
                </div>
              )}

              {modo === null && puedeModificar && (
                <div className="d-flex flex-wrap justify-content-end gap-2 mt-3">
                  <button type="button" className="btn btn-outline-danger" id="btnAnular"
                          onClick={() => { setAlerta(null); setAnulacion({ motivo: "", error: null }); }}
                          disabled={procesando}>
                    <i className="bi bi-x-circle me-1" aria-hidden="true"></i>Anular
                  </button>
                  <button type="button" className="btn btn-outline-secondary" id="btnSustituir"
                          onClick={() => iniciarModo(MODO_SUSTITUIR)} disabled={procesando}>
                    <i className="bi bi-arrow-left-right me-1" aria-hidden="true"></i>Sustituir
                  </button>
                  <button type="button" className="btn btn-sim" id="btnCompletar"
                          onClick={() => iniciarModo(MODO_COMPLETAR)}
                          disabled={procesando || pendientes.length === 0}>
                    <i className="bi bi-pencil-square me-1" aria-hidden="true"></i>Completar datos pendientes
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Formulario: registrar, completar o sustituir. */}
      {modo && puedeModificar && (
        <div className="card sim-card mb-4 sim-autorizacion-formulario" id="frmAutorizacion">
          <div className="card-header">
            <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>{tituloFormulario}
          </div>
          <div className="card-body">
            {usaPrecarga && modo === MODO_REGISTRAR && (
              <div className="alert alert-info py-2 small" role="status">
                Se precargaron los números EXONET registrados en la solicitud de exoneración.
                Verifíquelos antes de guardar.
              </div>
            )}
            {modo === MODO_COMPLETAR && (
              <p className="form-text mt-0">
                Solo puede llenar los datos que siguen vacíos. Para cambiar un dato ya registrado,
                cancele y use Sustituir.
              </p>
            )}
            {modo === MODO_SUSTITUIR && (
              <div className="alert alert-warning py-2 small" role="status">
                La autorización {describirAutorizacion(vigente)} quedará como sustituida y se
                conservará en el historial. Los datos se precargaron de ella: corrija lo necesario.
                El documento no se copia; adjunte el de la nueva autorización si corresponde.
              </div>
            )}
            {errores.general && <div className="alert alert-danger py-2 small" role="alert">{errores.general}</div>}

            <form onSubmit={guardar} noValidate>
              <div className="row g-3">
                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtExpediente">Referencia interna</label>
                  <input type="text" className="form-control sim-referencia" id="txtExpediente"
                         value={referenciaInterna} readOnly />
                </div>
                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="txtResponsable">
                    Responsable
                    <Cmp_SIM_AyudaCampo titulo="el responsable">
                      Es la persona con la sesión iniciada. El sistema la registra al guardar.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control" id="txtResponsable" value={responsable} readOnly />
                </div>

                {campo({
                  nombre: "numeroSolicitudExonet", etiqueta: "Número de solicitud EXONET", mayusculas: true,
                  ayuda: "Número que devuelve EXONET al presentar la solicitud. Puede registrarse solo, "
                    + "antes de recibir la autorización.",
                })}
                {campo({ nombre: "fechaEnvioExonet", etiqueta: "Fecha de envío a EXONET", tipo: "date" })}
                {campo({
                  nombre: "numeroAutorizacion", etiqueta: ETIQUETA_NUMERO_AUTORIZACION, mayusculas: true,
                  ayuda: "Número de la autorización externa. Se registra junto con su fecha.",
                })}
                {campo({ nombre: "fechaAutorizacion", etiqueta: "Fecha de autorización", tipo: "date" })}
                {campo({
                  nombre: "fechaComunicacion", etiqueta: "Fecha de comunicación", tipo: "date",
                  ayuda: "Fecha en que se comunicó la autorización al interesado. Requiere la autorización.",
                })}

                <div className="col-12 col-md-6">
                  <label className="form-label" htmlFor="filDocumento">
                    Documento de respaldo <span className="text-muted small">(opcional)</span>
                  </label>
                  {documentoBloqueado ? (
                    <input type="text" className="form-control" id="filDocumento"
                           value={vigente.nombreArchivoDocumento ?? "Documento registrado"} readOnly />
                  ) : (
                    <input type="file" className="form-control" id="filDocumento" key={claveArchivo}
                           onChange={(evento) => {
                             setArchivo(evento.target.files?.[0] ?? null);
                             setErrores((anteriores) => ({ ...anteriores, general: undefined }));
                           }} />
                  )}
                  <Cmp_SIM_IndicacionArchivos />
                </div>

                {campo({
                  nombre: "observaciones", etiqueta: "Observaciones", tipo: "textarea", columnas: "col-12",
                })}

                {modo === MODO_SUSTITUIR && (
                  <div className="col-12">
                    <label className="form-label" htmlFor="txtMotivoSustitucion">
                      Motivo de la sustitución <span className="text-danger" aria-hidden="true">*</span>
                      <span className="visually-hidden">(obligatorio)</span>
                    </label>
                    <textarea id="txtMotivoSustitucion" rows={2}
                              className={`form-control${errores.motivo ? " is-invalid" : ""}`}
                              maxLength={LONGITUD_MAXIMA_MOTIVO} aria-required="true"
                              value={formulario.motivo}
                              onChange={(evento) => actualizarCampo("motivo", evento.target.value)} />
                    {errores.motivo && <div className="invalid-feedback">{errores.motivo}</div>}
                    <div className="form-text text-end">{formulario.motivo.length}/{LONGITUD_MAXIMA_MOTIVO}</div>
                  </div>
                )}
              </div>

              <div className="d-flex flex-wrap justify-content-end gap-2 mt-3">
                {vigente && (
                  <button type="button" className="btn btn-outline-secondary" id="btnCancelarAutorizacion"
                          onClick={cancelarModo} disabled={procesando}>
                    Cancelar
                  </button>
                )}
                <button type="submit" className="btn btn-sim" id="btnGuardarAutorizacion" disabled={procesando}>
                  <i className="bi bi-save me-1" aria-hidden="true"></i>{textoBotonGuardar}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Historial completo: vigente, anuladas y sustituidas, con la cadena de sustituciones. */}
      <div className="card sim-card mb-5 sim-autorizacion-historial" id="seccionHistorial">
        <div className="card-header d-flex justify-content-between align-items-center">
          <span>
            <i className="bi bi-clock-history me-2" aria-hidden="true"></i>Historial de autorizaciones
            <span className="text-muted small fw-normal"> · se conserva completo</span>
          </span>
          <span className="badge text-bg-light text-dark">{historial.length}</span>
        </div>
        <div className="card-body">
          {historial.length === 0 ? (
            <p className="text-muted small mb-0">El expediente todavía no tiene historial de autorizaciones.</p>
          ) : (
            <ol className="list-group">
              {[...historial].reverse().map((autorizacion) => (
                <li key={autorizacion.idAutorizacion} id={`autorizacion-${autorizacion.idAutorizacion}`}
                    className="list-group-item sim-autorizacion-item">
                  <div className="d-flex flex-wrap align-items-center gap-2 mb-1">
                    <span className={`badge ${CLASE_ESTADO[autorizacion.estado] ?? "text-bg-light"}`}>
                      {autorizacion.nombreEstado ?? autorizacion.estado}
                    </span>
                    <strong>{describirAutorizacion(autorizacion)}</strong>
                  </div>
                  <div className="small">
                    Solicitud EXONET: <span className="sim-referencia">{autorizacion.numeroSolicitudExonet ?? "—"}</span>
                    {" · "}envío {formatearFecha(autorizacion.fechaEnvioExonet)}
                    {" · "}{ETIQUETA_NUMERO_AUTORIZACION.toLowerCase()}:{" "}
                    <span className="sim-referencia">{autorizacion.numeroAutorizacion ?? "—"}</span>
                    {" · "}autorizada {formatearFecha(autorizacion.fechaAutorizacion)}
                    {" · "}comunicada {formatearFecha(autorizacion.fechaComunicacion)}
                  </div>
                  {autorizacion.observaciones && (
                    <p className="small mb-1 text-break">{autorizacion.observaciones}</p>
                  )}
                  {autorizacion.idDocumento && (
                    <button type="button" className="btn btn-link btn-sm p-0"
                            onClick={() => descargar(autorizacion)}>
                      <i className="bi bi-download me-1" aria-hidden="true"></i>
                      {autorizacion.nombreArchivoDocumento ?? "Documento"}
                    </button>
                  )}
                  <div className="small text-muted mt-1">
                    Registrada por {autorizacion.usuarioRegistro ?? "—"} · {formatearFechaHora(autorizacion.fechaRegistro)}
                  </div>
                  {autorizacion.idAutorizacionAnterior && (
                    <div className="small text-muted">
                      Sustituye a{" "}
                      <a href={`#autorizacion-${autorizacion.idAutorizacionAnterior}`}>
                        {describirAutorizacion(porId.get(autorizacion.idAutorizacionAnterior))
                          || `#${autorizacion.idAutorizacionAnterior}`}
                      </a>
                    </div>
                  )}
                  {autorizacion.fechaCambioEstado && (
                    <div className="small text-muted">
                      {autorizacion.estado === "SUS" ? "Sustituida" : "Anulada"} por{" "}
                      {autorizacion.usuarioCambioEstado ?? "—"} · {formatearFechaHora(autorizacion.fechaCambioEstado)}
                      {autorizacion.idAutorizacionSustituta && (
                        <>
                          {" · reemplazada por "}
                          <a href={`#autorizacion-${autorizacion.idAutorizacionSustituta}`}>
                            {describirAutorizacion(porId.get(autorizacion.idAutorizacionSustituta))
                              || `#${autorizacion.idAutorizacionSustituta}`}
                          </a>
                        </>
                      )}
                      {autorizacion.motivoCambioEstado && (
                        <span className="d-block text-break">Motivo: {autorizacion.motivoCambioEstado}</span>
                      )}
                    </div>
                  )}
                </li>
              ))}
            </ol>
          )}
          <p className="form-text mb-0 mt-2">
            Las autorizaciones anuladas o sustituidas no se borran: conservan quién las registró,
            quién las cambió, cuándo y por qué. Se muestran de la más reciente a la más antigua.
          </p>
        </div>
      </div>

      <p className="form-text mb-5">
        El sistema solo registra la resolución de EXONET: no genera autorizaciones externas ni
        cambia el estado del expediente.
      </p>

      {anulacion && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
               aria-labelledby="ttlAnularAutorizacion">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5" id="ttlAnularAutorizacion">
                    <i className="bi bi-x-circle text-danger me-2" aria-hidden="true"></i>Anular autorización
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar"
                          onClick={() => setAnulacion(null)} disabled={procesando}></button>
                </div>
                <div className="modal-body">
                  <p>
                    La autorización {describirAutorizacion(vigente)} quedará anulada y se conservará en
                    el historial. El expediente quedará sin autorización vigente.
                  </p>
                  <label className="form-label" htmlFor="txtMotivoAnulacion">
                    Motivo de la anulación <span className="text-danger" aria-hidden="true">*</span>
                    <span className="visually-hidden">(obligatorio)</span>
                  </label>
                  <textarea id="txtMotivoAnulacion" rows={3}
                            className={`form-control${anulacion.error ? " is-invalid" : ""}`}
                            maxLength={LONGITUD_MAXIMA_MOTIVO} aria-required="true"
                            value={anulacion.motivo}
                            onChange={(evento) => setAnulacion({ motivo: evento.target.value, error: null })} />
                  {anulacion.error && <div className="invalid-feedback">{anulacion.error}</div>}
                  <div className="form-text text-end">{anulacion.motivo.length}/{LONGITUD_MAXIMA_MOTIVO}</div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary"
                          onClick={() => setAnulacion(null)} disabled={procesando}>
                    Cancelar
                  </button>
                  <button type="button" className="btn btn-danger" id="btnAceptarAnulacion"
                          onClick={confirmarAnulacion} disabled={procesando}>
                    Anular autorización
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

export default Frm_SIM_AutorizacionExonet;
