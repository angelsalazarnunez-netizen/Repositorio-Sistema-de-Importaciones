/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Llamadas al servicio REST del módulo de trámites. Centraliza las rutas y la
 *   lectura del mensaje de error que devuelve el servidor, para que las pantallas solo se
 *   ocupen de mostrarlo.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 30/09/2026
 *   Descripción: se agregan la búsqueda de trámites, las opciones de sus filtros y la exportación
 *     del listado (HU-034).
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se agregan el registro de donaciones (HU-024) y la gestión de sus expedientes
 *     (HU-044).
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: se agregan las importaciones temporales (HU-025). Las alertas de vencimiento
 *     (HU-027) usan las rutas nuevas y el registro y la extensión anteriores se reemplazan por los
 *     del expediente de importación temporal.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: las llamadas de los trámites temporales reciben el servicio del tipo
 *     (importaciones-temporales o exportaciones-temporales) para atender también la exportación
 *     temporal (HU-026).
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agregan el control de documentos obligatorios de la exoneración y su
 *     continuación a aprobación (HU-021).
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agregan la bandeja de aprobaciones y el registro de la decisión de la
 *     exoneración (HU-022).
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 05/10/2026
 *   Descripción: se agrega el panel de trámites activos (HU-030).
 *   Descripción: se agregan las solicitudes / autorizaciones EXONET del expediente de exoneración
 *     (HU-023). El usuario lo toma el servidor de la sesión: estas llamadas nunca lo envían.
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: ante un 403 se refrescan los permisos de la sesión, por si se retiró un rol
 *     (restricciones por rol, HU-003). El mensaje del servidor se sigue mostrando en la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: se agrega obtenerExoneraciones, para la lista de exoneraciones.
 */

import {
  limpiarSesion,
  notificarAccesoDenegado,
  obtenerSesion,
  obtenerToken,
} from "./Srv_SIM_Autenticacion.js";

const TRAMITES = "/api/v1/tramites";
const DESALMACENAJE = "/api/v1/tramites/desalmacenaje-especial";
const EXONERACIONES = "/api/v1/tramites/exoneraciones";
const IMPORTACIONES = "/api/v1/tramites/importaciones";
const DONACIONES = "/api/v1/tramites/donaciones";
const BUSQUEDA = "/api/v1/tramites/busqueda";
const TEMPORALES = "/api/v1/tramites/temporales";
const OBSERVACIONES_EXONERACION = "/api/v1/exoneraciones";

function obtenerIdUsuarioResponsable() {
  const idUsuario = obtenerSesion()?.idUsuario;
  if (!idUsuario) {
    throw new Error("La sesión no contiene un usuario válido.");
  }
  return idUsuario;
}

function crearCabecerasAutenticadas(opciones) {
  const cabeceras = new Headers(opciones?.headers);
  const token = obtenerToken();
  if (token) {
    cabeceras.set("Authorization", `Bearer ${token}`);
  }
  return cabeceras;
}

function redirigirAlLogin() {
  limpiarSesion();
  if (window.location.pathname !== "/login") {
    window.location.assign("/login");
  }
}

async function leerMensajeError(respuesta) {
  try {
    const cuerpo = await respuesta.json();
    if (cuerpo?.mensaje) {
      return cuerpo.mensaje;
    }
  } catch (error) {
    // La respuesta no traía JSON: se usa el código de estado.
  }
  return `El servidor respondió con el código ${respuesta.status}.`;
}

async function pedir(ruta, opciones) {
  const respuesta = await fetch(ruta, {
    ...opciones,
    headers: crearCabecerasAutenticadas(opciones),
  });
  if (respuesta.status === 401) {
    redirigirAlLogin();
  }
  if (respuesta.status === 403) {
    notificarAccesoDenegado();
  }
  if (!respuesta.ok) {
    throw new Error(await leerMensajeError(respuesta));
  }
  return respuesta.json();
}

async function descargar(ruta, nombreArchivo) {
  const respuesta = await fetch(ruta, {
    headers: crearCabecerasAutenticadas(),
  });
  if (respuesta.status === 401) {
    redirigirAlLogin();
  }
  if (respuesta.status === 403) {
    notificarAccesoDenegado();
  }
  if (!respuesta.ok) {
    throw new Error(await leerMensajeError(respuesta));
  }

  const url = URL.createObjectURL(await respuesta.blob());
  const enlace = document.createElement("a");
  enlace.href = url;
  enlace.download = nombreArchivo;
  enlace.click();
  window.setTimeout(() => URL.revokeObjectURL(url), 0);
}

/** Agrega los archivos de un campo al formulario que se envía al servidor. */
function agregarArchivos(datos, campo, archivos) {
  (archivos ?? []).forEach((archivo) => datos.append(campo, archivo));
}

// ------------------------------------------------- Desalmacenaje especial (HU-031 y HU-032)

export function obtenerExpedientes() {
  return pedir(DESALMACENAJE);
}

export function obtenerExpediente(referenciaInterna) {
  return pedir(`${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}`);
}

export function obtenerEstados() {
  return pedir(`${DESALMACENAJE}/estados`);
}

export function registrarDesalmacenaje(formulario, archivos) {
  const datos = new FormData();
  Object.entries(formulario).forEach(([campo, valor]) => {
    if (valor !== null && valor !== undefined && valor !== "") {
      datos.append(campo, valor);
    }
  });
  datos.set("idUsuarioResponsable", obtenerIdUsuarioResponsable());
  Object.entries(archivos).forEach(([campo, lista]) => agregarArchivos(datos, campo, lista));

  return pedir(DESALMACENAJE, { method: "POST", body: datos });
}

export function cambiarEstado(referenciaInterna, nuevoEstado, justificacion) {
  return pedir(`${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}/estado`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      nuevoEstado,
      justificacion,
      idUsuarioResponsable: obtenerIdUsuarioResponsable(),
    }),
  });
}

export function agregarDocumentos(referenciaInterna, archivos) {
  const datos = new FormData();
  datos.set("idUsuarioResponsable", obtenerIdUsuarioResponsable());
  Object.entries(archivos).forEach(([campo, lista]) => agregarArchivos(datos, campo, lista));

  return pedir(`${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}/documentos`, {
    method: "POST",
    body: datos,
  });
}

export function eliminarDocumento(referenciaInterna, idDocumento) {
  return pedir(
    `${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}/documentos/${idDocumento}`
      + `?idUsuarioResponsable=${obtenerIdUsuarioResponsable()}`,
    { method: "DELETE" }
  );
}

export function descargarDocumento(idDocumento, nombreArchivo) {
  return descargar(`${DESALMACENAJE}/documentos/${idDocumento}`, nombreArchivo);
}

// ------------------------------------------------- Donaciones (HU-024 y HU-044)

export function registrarDonacion(formulario, archivos) {
  const datos = new FormData();
  Object.entries(formulario).forEach(([campo, valor]) => {
    if (valor !== null && valor !== undefined && valor !== "") {
      datos.append(campo, valor);
    }
  });
  datos.set("idUsuarioResponsable", obtenerIdUsuarioResponsable());
  Object.entries(archivos).forEach(([campo, lista]) => agregarArchivos(datos, campo, lista));

  return pedir(DONACIONES, { method: "POST", body: datos });
}

export function obtenerDonaciones() {
  return pedir(DONACIONES);
}

export function obtenerDonacion(referenciaInterna) {
  return pedir(`${DONACIONES}/${encodeURIComponent(referenciaInterna)}`);
}

export function obtenerEstadosDonacion() {
  return pedir(`${DONACIONES}/estados`);
}

/**
 * Mueve el expediente de donación a otro estado. Las observaciones son opcionales y, si se
 * escriben, reemplazan las observaciones del expediente.
 *
 * @param {string} referenciaInterna referencia del expediente
 * @param {object} cambio nuevo estado, justificación y observaciones
 * @returns {Promise<object>} expediente con su estado e historial actualizados
 */
export function cambiarEstadoDonacion(referenciaInterna, cambio) {
  return pedir(`${DONACIONES}/${encodeURIComponent(referenciaInterna)}/estado`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...cambio, idUsuarioResponsable: obtenerIdUsuarioResponsable() }),
  });
}

export function agregarDocumentosDonacion(referenciaInterna, archivos) {
  const datos = new FormData();
  datos.set("idUsuarioResponsable", obtenerIdUsuarioResponsable());
  Object.entries(archivos).forEach(([campo, lista]) => agregarArchivos(datos, campo, lista));

  return pedir(`${DONACIONES}/${encodeURIComponent(referenciaInterna)}/documentos`, {
    method: "POST",
    body: datos,
  });
}

export function eliminarDocumentoDonacion(referenciaInterna, idDocumento) {
  return pedir(
    `${DONACIONES}/${encodeURIComponent(referenciaInterna)}/documentos/${idDocumento}`
      + `?idUsuarioResponsable=${obtenerIdUsuarioResponsable()}`,
    { method: "DELETE" }
  );
}

export function descargarDocumentoDonacion(idDocumento, nombreArchivo) {
  return descargar(`${DONACIONES}/documentos/${idDocumento}`, nombreArchivo);
}

// ------------------------------------------------- Solicitud de exoneración (HU-018)

export function registrarExoneracion(formulario, archivos) {
  const datos = new FormData();
  Object.entries(formulario).forEach(([campo, valor]) => {
    if (valor !== null && valor !== undefined && valor !== "") {
      datos.append(campo, valor);
    }
  });
  datos.set("idUsuarioResponsable", obtenerIdUsuarioResponsable());
  Object.entries(archivos).forEach(([campo, lista]) => agregarArchivos(datos, campo, lista));

  return pedir(EXONERACIONES, { method: "POST", body: datos });
}

export function corregirDatosExoneracion(referenciaInterna, datos) {
  return pedir(`${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}/datos`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...datos, idUsuarioResponsable: obtenerIdUsuarioResponsable() }),
  });
}

export function eliminarDocumentoExoneracion(referenciaInterna, idDocumento) {
  return pedir(
    `${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}/documentos/${idDocumento}`
      + `?idUsuarioResponsable=${obtenerIdUsuarioResponsable()}`,
    { method: "DELETE" }
  );
}

/** Lista de expedientes de exoneración, para la pantalla de entrada del módulo. */
export function obtenerExoneraciones() {
  return pedir(EXONERACIONES);
}

export function obtenerExoneracion(referenciaInterna) {
  return pedir(`${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}`);
}

export function agregarDocumentosExoneracion(referenciaInterna, archivos) {
  const datos = new FormData();
  datos.set("idUsuarioResponsable", obtenerIdUsuarioResponsable());
  Object.entries(archivos).forEach(([campo, lista]) => agregarArchivos(datos, campo, lista));

  return pedir(`${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}/documentos`, {
    method: "POST",
    body: datos,
  });
}

export function agregarComentario(referenciaInterna, comentario) {
  return pedir(`${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}/bitacora/comentarios`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ comentario, idUsuario: obtenerIdUsuarioResponsable() }),
  });
}

export function eliminarComentario(referenciaInterna, idBitacora) {
  return pedir(
    `${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}/bitacora/comentarios/${idBitacora}`
      + `?idUsuario=${obtenerIdUsuarioResponsable()}`,
    { method: "DELETE" }
  );
}

export function obtenerBitacora(referenciaInterna) {
  return pedir(`${EXONERACIONES}/${encodeURIComponent(referenciaInterna)}/bitacora`);
}

/**
 * Cambia la referencia interna de un expediente. Si la sigla nueva es de otro tipo de
 * trámite, el expediente se reclasifica y la respuesta indica a dónde quedó.
 *
 * @param {string} referenciaInterna referencia actual del expediente
 * @param {object} cambio referencia nueva, justificación y datos del tipo destino
 * @returns {Promise<object>} referencia anterior y nueva, tipo resultante y reclasificación
 */
export function cambiarReferencia(referenciaInterna, cambio) {
  return pedir(`${TRAMITES}/${encodeURIComponent(referenciaInterna)}/referencia`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...cambio, idUsuarioResponsable: obtenerIdUsuarioResponsable() }),
  });
}

// ------------------------------------------------- Gestión documental (HU-045)

/**
 * Cambia la clasificación de un documento del expediente, sin tocar el archivo ni su versión.
 *
 * @param {string} referenciaInterna referencia del expediente
 * @param {number} idDocumento documento que se reclasifica
 * @param {string} tipoDocumento código del tipo de documento del catálogo
 * @returns {Promise<object>} expediente con su documentación actualizada
 */
export function clasificarDocumento(referenciaInterna, idDocumento, tipoDocumento) {
  return pedir(
    `${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}/documentos/${idDocumento}/clasificacion`,
    {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        tipoDocumento,
        idUsuarioResponsable: obtenerIdUsuarioResponsable(),
      }),
    }
  );
}

/**
 * Guarda si el expediente está documentalmente completo o incompleto, con sus observaciones.
 *
 * @param {string} referenciaInterna referencia del expediente
 * @param {string} estadoDocumental COMPLETO o INCOMPLETO
 * @param {string} observaciones observaciones documentales, opcionales
 * @returns {Promise<object>} expediente con su estado documental actualizado
 */
export function guardarEstadoDocumental(referenciaInterna, estadoDocumental, observaciones) {
  return pedir(`${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}/estado-documental`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      estadoDocumental,
      observaciones,
      idUsuarioResponsable: obtenerIdUsuarioResponsable(),
    }),
  });
}

// ------------------------------------------------- Reportes (HU-036)

/**
 * Genera el reporte de consultas internas con los criterios indicados. Los filtros vacíos no
 * se envían, para que no acoten el reporte.
 *
 * @param {object} filtros rango de fechas, filtros, agrupación y opciones
 * @returns {Promise<object>} reporte con sus totales, desgloses y notas
 */
function parametrosDelReporte(filtros) {
  const parametros = new URLSearchParams();
  ["fechaDesde", "fechaHasta", "tipoTramite", "regimenAduanero", "tipoExoneracion", "dependencia"]
    .forEach((campo) => {
      const valor = (filtros[campo] ?? "").trim();
      if (valor) {
        parametros.set(campo, valor);
      }
    });
  (filtros.agrupaciones ?? []).forEach((criterio) => parametros.append("agruparPor", criterio));
  parametros.set("enumerarTramites", String(Boolean(filtros.enumerarTramites)));
  parametros.set("combinarCriterios", String(Boolean(filtros.combinarCriterios)));
  parametros.set("incluirTotalCif", String(Boolean(filtros.totalCif)));
  parametros.set("incluirTotalBodegaje", String(Boolean(filtros.totalBodegajes)));
  return parametros;
}

/**
 * Genera el reporte de consultas internas con los criterios indicados.
 *
 * @param {object} filtros rango de fechas, filtros, agrupación y opciones
 * @returns {Promise<object>} reporte con sus totales, desgloses y notas
 */
export function generarReporteConsultas(filtros) {
  return pedir(`${TRAMITES}/reportes/consultas-internas?${parametrosDelReporte(filtros).toString()}`);
}

export function obtenerCatalogosPedidoImportacion() {
  return pedir(`${IMPORTACIONES}/catalogos`);
}

export function obtenerPedidosImportacion() {
  return pedir(IMPORTACIONES);
}

export function obtenerPedidoImportacion(referenciaInterna) {
  return pedir(`${IMPORTACIONES}/${encodeURIComponent(referenciaInterna)}`);
}

export function registrarPedidoImportacion(formulario) {
  return pedir(IMPORTACIONES, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(formulario),
  });
}

export function actualizarPedidoImportacion(referenciaInterna, formulario) {
  return pedir(`${IMPORTACIONES}/${encodeURIComponent(referenciaInterna)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(formulario),
  });
}

export function obtenerNotificacionesPedidoImportacion() {
  return pedir(`${IMPORTACIONES}/notificaciones`);
}

// ------------------------------------------------- Búsqueda de trámites (HU-034)

/** Agrega los criterios de búsqueda a la ruta, si hay alguno. */
function rutaConCriterios(ruta, criterios) {
  const consulta = criterios.toString();
  return consulta ? `${ruta}?${consulta}` : ruta;
}

/** Obtiene el nombre del archivo que indica el servidor en Content-Disposition. */
function obtenerNombreArchivo(respuesta, nombrePorDefecto) {
  const disposicion = respuesta.headers.get("Content-Disposition") ?? "";
  const codificado = disposicion.match(/filename\*=UTF-8''([^;]+)/i);
  if (codificado) {
    return decodeURIComponent(codificado[1]);
  }
  const simple = disposicion.match(/filename="?([^";]+)"?/i);
  return simple ? simple[1] : nombrePorDefecto;
}

/**
 * Obtiene los valores de los filtros, las columnas del listado y los formatos de exportación.
 *
 * @returns {Promise<object>} opciones de la pantalla de búsqueda
 */
export function obtenerOpcionesBusqueda() {
  return pedir(`${BUSQUEDA}/opciones`);
}

/**
 * Busca los trámites que cumplen todos los criterios. Un criterio omitido no restringe.
 *
 * @param {URLSearchParams} criterios criterios de la búsqueda; las listas repiten el parámetro
 * @returns {Promise<object[]>} trámites encontrados
 */
export function buscarTramites(criterios) {
  return pedir(rutaConCriterios(BUSQUEDA, criterios));
}

/**
 * Exporta el listado de trámites con los criterios indicados, en el formato pedido.
 *
 * @param {URLSearchParams} criterios criterios de la búsqueda que se exporta
 * @param {string} formato código del formato: EXCEL, PDF o WORD
 * @returns {Promise<{archivo: Blob, nombreArchivo: string}>} archivo generado y su nombre
 */
export async function exportarTramites(criterios, formato) {
  const ruta = rutaConCriterios(`${BUSQUEDA}/exportar/${encodeURIComponent(formato)}`, criterios);
  const respuesta = await fetch(ruta, { headers: crearCabecerasAutenticadas() });
  if (respuesta.status === 401) {
    redirigirAlLogin();
  }
  if (respuesta.status === 403) {
    notificarAccesoDenegado();
  }
  if (!respuesta.ok) {
    throw new Error(await leerMensajeError(respuesta));
  }
  return {
    archivo: await respuesta.blob(),
    nombreArchivo: obtenerNombreArchivo(respuesta, `listado-tramites.${formato.toLowerCase()}`),
  };
}

// ------------------------------------- Trámites temporales (HU-025 y HU-026) y alertas (HU-027)

/** Arma el cuerpo multipart: los datos no vacíos y cada archivo en el campo de su tipo de documento. */
function crearFormularioTemporal(formulario, archivos) {
  const datos = new FormData();
  Object.entries(formulario ?? {}).forEach(([campo, valor]) => {
    if (Array.isArray(valor)) {
      valor.forEach((elemento) => datos.append(campo, elemento));
    } else if (valor !== null && valor !== undefined && valor !== "") {
      datos.append(campo, valor);
    }
  });
  Object.entries(archivos ?? {}).forEach(([codigo, lista]) => agregarArchivos(datos, codigo, lista));
  return datos;
}

/** Envía un cuerpo JSON en el que los textos vacíos viajan como nulos. */
function enviarJsonTemporal(ruta, metodo, cuerpo) {
  const limpio = Object.fromEntries(
    Object.entries(cuerpo).map(([campo, valor]) => [campo, valor === "" ? null : valor])
  );
  return pedir(ruta, {
    method: metodo,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(limpio),
  });
}

/**
 * Ruta del servicio de un tipo de trámite temporal.
 *
 * @param {string} servicio importaciones-temporales o exportaciones-temporales
 */
function rutaTramitesTemporales(servicio) {
  return `${TRAMITES}/${servicio}`;
}

function rutaTramiteTemporal(servicio, referenciaInterna) {
  return `${rutaTramitesTemporales(servicio)}/${encodeURIComponent(referenciaInterna)}`;
}

/**
 * Obtiene los trámites temporales de un tipo en el alcance indicado.
 *
 * @param {string} servicio importaciones-temporales o exportaciones-temporales
 * @param {string} alcance ACTIVOS (vista de trámites activos), CERRADOS o TODOS
 * @returns {Promise<object[]>} expedientes con su cuenta regresiva
 */
export function obtenerTramitesTemporales(servicio, alcance) {
  return pedir(`${rutaTramitesTemporales(servicio)}?alcance=${encodeURIComponent(alcance)}`);
}

export function obtenerOpcionesTramiteTemporal(servicio) {
  return pedir(`${rutaTramitesTemporales(servicio)}/opciones`);
}

export function obtenerTramiteTemporal(servicio, referenciaInterna) {
  return pedir(rutaTramiteTemporal(servicio, referenciaInterna));
}

/**
 * Registra un trámite temporal. El usuario responsable lo toma el servidor del token.
 *
 * @param {string} servicio importaciones-temporales o exportaciones-temporales
 * @param {object} formulario datos del formulario; agentesResponsables es una lista de identificadores
 * @param {object} archivos archivos agrupados por código de tipo de documento
 * @returns {Promise<object>} confirmación con la referencia y el estado inicial
 */
export function registrarTramiteTemporal(servicio, formulario, archivos) {
  return pedir(rutaTramitesTemporales(servicio), {
    method: "POST",
    body: crearFormularioTemporal(formulario, archivos),
  });
}

export function cambiarEstadoTramiteTemporal(servicio, referenciaInterna, cambio) {
  return enviarJsonTemporal(`${rutaTramiteTemporal(servicio, referenciaInterna)}/estado`, "PUT", cambio);
}

export function guardarDatosTramiteTemporal(servicio, referenciaInterna, datos) {
  return enviarJsonTemporal(`${rutaTramiteTemporal(servicio, referenciaInterna)}/datos`, "PUT", datos);
}

export function registrarProrrogaTemporal(servicio, referenciaInterna, prorroga) {
  const formulario = new FormData();
  const {
    archivoOficioSolicitud,
    archivoResolucion,
    ...datosProrroga
  } = prorroga;
  formulario.append("prorroga", new Blob([JSON.stringify(datosProrroga)], { type: "application/json" }));
  formulario.append("oficioSolicitud", archivoOficioSolicitud);
  formulario.append("resolucion", archivoResolucion);
  return pedir(`${rutaTramiteTemporal(servicio, referenciaInterna)}/prorroga`, {
    method: "PUT",
    body: formulario,
  });
}

export function agregarDocumentosTramiteTemporal(servicio, referenciaInterna, archivos) {
  return pedir(`${rutaTramiteTemporal(servicio, referenciaInterna)}/documentos`, {
    method: "POST",
    body: crearFormularioTemporal({}, archivos),
  });
}

export function eliminarDocumentoTramiteTemporal(servicio, referenciaInterna, idDocumento) {
  return pedir(`${rutaTramiteTemporal(servicio, referenciaInterna)}/documentos/${idDocumento}`, { method: "DELETE" });
}

export function descargarDocumentoTramiteTemporal(servicio, idDocumento, nombreArchivo) {
  return descargar(`${rutaTramitesTemporales(servicio)}/documentos/${idDocumento}`, nombreArchivo);
}

export function obtenerAlertasTemporales() {
  return pedir(`${TEMPORALES}/alertas`);
}

export function marcarAlertaTemporalAtendida(idAlerta) {
  return pedir(`${TEMPORALES}/alertas/${idAlerta}/atendida`, { method: "PUT" });
}

/**
 * Descarga el reporte de consultas internas en el formato indicado, con los mismos
 * criterios con los que se generó en la pantalla.
 *
 * @param {object} filtros criterios del reporte
 * @param {string} formato código del formato: EXCEL o PDF
 * @returns {Promise<void>} termina cuando el archivo se descargó
 */
export function descargarReporteConsultas(filtros, formato) {
  const parametros = parametrosDelReporte(filtros);
  const extension = formato === "PDF" ? "pdf" : "xlsx";
  return descargar(
    `${TRAMITES}/reportes/consultas-internas/exportar/${encodeURIComponent(formato)}?${parametros.toString()}`,
    `reporte-consultas-internas.${extension}`
  );
}

/**
 * Corrige los datos ya guardados de un expediente de desalmacenaje especial. Cada cambio
 * queda anotado en la bitácora con su valor anterior.
 *
 * @param {string} referenciaInterna referencia del expediente
 * @param {object} datos datos corregidos
 * @returns {Promise<object>} expediente con sus datos actualizados
 */
export function corregirDatosDesalmacenaje(referenciaInterna, datos) {
  return pedir(`${DESALMACENAJE}/${encodeURIComponent(referenciaInterna)}/datos`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ ...datos, idUsuarioResponsable: obtenerIdUsuarioResponsable() }),
  });
}

// ---------------------------------------------------------------------------
// HU-019 Revisión documental de exoneraciones.
// ---------------------------------------------------------------------------

function rutaRevisionDocumental(idTramite) {
    return `${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/documentos/revision`;
}

/**
 * Obtiene el checklist documental de una solicitud de exoneración.
 *
 * @param {number} idTramite identificador del expediente
 * @returns {Promise<object[]>} requisitos documentales con su estado de revisión
 */
export function obtenerRevisionDocumental(idTramite) {
    return pedir(rutaRevisionDocumental(idTramite));
}

/**
 * Registra una nueva revisión sobre un documento del expediente.
 *
 * @param {number} idTramite identificador del expediente
 * @param {number} idDocumento identificador del documento
 * @param {object} datos estado y observación de revisión
 * @returns {Promise<object>} revisión registrada
 */
export function registrarRevisionDocumento(idTramite, idDocumento, datos) {
    return pedir(
        `${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}`
        + `/documentos/${encodeURIComponent(idDocumento)}/revision`,
        {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                estadoRevision: datos.estadoRevision,
                observacion: datos.observacion,
            }),
        }
    );
}

/**
 * Obtiene el historial de revisiones realizadas sobre un documento.
 *
 * @param {number} idTramite identificador del expediente
 * @param {number} idDocumento identificador del documento
 * @returns {Promise<object[]>} historial de revisiones
 */
export function obtenerHistorialRevisionDocumento(idTramite, idDocumento) {
    return pedir(
        `${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}`
        + `/documentos/${encodeURIComponent(idDocumento)}/revision/historial`
    );
}


// ---------------------------------------------------------------------------
// HU-021 Control de documentos obligatorios de la exoneración.
// ---------------------------------------------------------------------------

/**
 * Obtiene el control de documentos obligatorios del expediente: el estado de cada documento
 * exigido, los pendientes y si hay cumplimiento documental.
 *
 * @param {number} idTramite identificador del expediente
 * @returns {Promise<object>} control documental del expediente
 */
export function obtenerControlDocumental(idTramite) {
  return pedir(`${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/documentos/control`);
}

/**
 * Continúa el expediente a aprobación. El servidor lo rechaza si la documentación obligatoria
 * está incompleta; el usuario responsable se toma de la sesión.
 *
 * @param {number} idTramite identificador del expediente
 * @returns {Promise<object>} control documental del expediente ya en su nuevo estado
 */
export function continuarAprobacionExoneracion(idTramite) {
  return pedir(`${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/aprobacion`, {
    method: "POST",
  });
}

// ---------------------------------------------------------------------------
// HU-030 Panel de trámites activos.
// ---------------------------------------------------------------------------

/**
 * Obtiene el panel de trámites: los indicadores de la vista operativa y la lista del alcance.
 *
 * @param {string} alcance ACTIVOS o HISTORICO
 * @returns {Promise<object>} panel con indicadores y trámites
 */
export function obtenerPanelTramites(alcance) {
  return pedir(`${TRAMITES}/activos?alcance=${encodeURIComponent(alcance ?? "ACTIVOS")}`);
}

// ---------------------------------------------------------------------------
// HU-022 Aprobación de solicitudes de exoneración.
// ---------------------------------------------------------------------------

/**
 * Obtiene la bandeja de solicitudes de exoneración pendientes de aprobación.
 *
 * @returns {Promise<object[]>} solicitudes pendientes
 */
export function obtenerAprobacionesPendientes() {
  return pedir(`${OBSERVACIONES_EXONERACION}/aprobaciones/pendientes`);
}

/**
 * Obtiene el resumen de una solicitud para registrar la decisión de la jefatura.
 *
 * @param {number} idTramite identificador del expediente
 * @returns {Promise<object>} resumen, permisos e historial de decisiones
 */
export function obtenerDecisionExoneracion(idTramite) {
  return pedir(`${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/decision`);
}

/**
 * Registra la decisión de la jefatura. El usuario responsable se toma de la sesión.
 *
 * @param {number} idTramite identificador del expediente
 * @param {{decision: string, justificacion: string}} datos APROBAR, RECHAZAR o MODIFICAR y su justificación
 * @returns {Promise<object>} resumen con la decisión registrada
 */
export function registrarDecisionExoneracion(idTramite, datos) {
  return pedir(`${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/decision`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ decision: datos.decision, justificacion: datos.justificacion }),
  });
}

// ---------------------------------------------------------------------------
// HU-020 Observaciones y aclaraciones del expediente de exoneración.
// ---------------------------------------------------------------------------

function rutaObservaciones(idTramite) {
  return `${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/observaciones`;
}

export function obtenerObservaciones(idTramite) {
  return pedir(rutaObservaciones(idTramite));
}

export function registrarObservacion(idTramite, datos, confirmar) {
  return pedir(rutaObservaciones(idTramite), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      motivo: datos.motivo,
      observacion: datos.observacion,
      confirmar: confirmar === true,
    }),
  });
}

export function modificarObservacion(idTramite, idObservacion, datos) {
  // No se envía "confirmar": un borrador se confirma solo con confirmarObservacion.
  return pedir(`${rutaObservaciones(idTramite)}/${encodeURIComponent(idObservacion)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ motivo: datos.motivo, observacion: datos.observacion }),
  });
}

export function eliminarObservacion(idTramite, idObservacion) {
  return pedir(`${rutaObservaciones(idTramite)}/${encodeURIComponent(idObservacion)}`, {
    method: "DELETE",
  });
}

export function confirmarObservacion(idTramite, idObservacion) {
  return pedir(`${rutaObservaciones(idTramite)}/${encodeURIComponent(idObservacion)}/confirmacion`, {
    method: "POST",
  });
}


/**
 * Obtiene los partidos responsables del costo que los expedientes tienen registrados, para
 * ofrecerlos como filtro del reporte.
 *
 * @returns {Promise<string[]>} partidos registrados
 */
export function obtenerPartidosResponsables() {
  return pedir(`${TRAMITES}/reportes/consultas-internas/partidos`);
}

/**
 * Registra los datos de costo del expediente: a quien se le carga el gasto y los montos de
 * impuestos y de bodegaje. Cada cambio queda anotado en la bitácora.
 *
 * @param {string} referenciaInterna referencia del expediente
 * @param {object} costos partido responsable, impuestos y bodegaje
 * @returns {Promise<object>} confirmación con los datos guardados
 */
export function registrarCostosTramite(referenciaInterna, costos) {
    return pedir(`${TRAMITES}/${encodeURIComponent(referenciaInterna)}/costos`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...costos, idUsuarioResponsable: obtenerIdUsuarioResponsable() }),
    });
}
    
    
// ---------------------------------------------------------------------------
// HU-023 Solicitudes / autorizaciones EXONET del expediente de exoneración.
// Comparten la ruta base del módulo de exoneraciones (OBSERVACIONES_EXONERACION). Toda
// operación devuelve el historial actualizado: { idTramite, vigente, historial, precarga }.
// ---------------------------------------------------------------------------

const CAMPOS_AUTORIZACION_EXONET = [
  "numeroSolicitudExonet",
  "fechaEnvioExonet",
  "numeroAutorizacion",
  "fechaAutorizacion",
  "fechaComunicacion",
  "observaciones",
];

function rutaAutorizacionesExonet(idTramite) {
  return `${OBSERVACIONES_EXONERACION}/${encodeURIComponent(idTramite)}/autorizaciones`;
}

/**
 * Arma el multipart de una autorización. Solo agrega los datos con valor: un dato omitido le
 * indica al servidor que no se informa (o, al completar, que no se completa). Las fechas viajan
 * como aaaa-mm-dd, el formato del control de fecha.
 */
function crearFormularioAutorizacionExonet(datos, archivo, motivo) {
  const formulario = new FormData();
  CAMPOS_AUTORIZACION_EXONET.forEach((campo) => {
    const valor = datos?.[campo];
    if (valor !== undefined && valor !== null && String(valor).trim() !== "") {
      formulario.append(campo, String(valor).trim());
    }
  });
  if (motivo !== undefined) {
    formulario.append("motivo", motivo ?? "");
  }
  if (archivo) {
    formulario.append("archivo", archivo);
  }
  return formulario;
}

/**
 * Obtiene la autorización vigente, el historial completo y, si el expediente todavía no tiene
 * historial, la precarga con los números EXONET de la solicitud.
 *
 * @param {number} idTramite identificador del expediente
 * @returns {Promise<object>} historial de autorizaciones
 */
export function obtenerAutorizacionesExonet(idTramite) {
  return pedir(rutaAutorizacionesExonet(idTramite));
}

/**
 * Registra la autorización vigente de un expediente que no tiene ninguna.
 *
 * @param {number} idTramite identificador del expediente
 * @param {object} datos números, fechas y observaciones
 * @param {File|null} archivo documento de respaldo, opcional
 * @returns {Promise<object>} historial actualizado
 */
export function registrarAutorizacionExonet(idTramite, datos, archivo) {
  return pedir(rutaAutorizacionesExonet(idTramite), {
    method: "POST",
    body: crearFormularioAutorizacionExonet(datos, archivo),
  });
}

/**
 * Completa datos vacíos de la autorización vigente. Solo deben enviarse los datos que estaban
 * vacíos: el servidor rechaza cambiar un dato ya registrado.
 *
 * @param {number} idTramite identificador del expediente
 * @param {number} idAutorizacion autorización vigente
 * @param {object} datos datos que se completan
 * @param {File|null} archivo documento de respaldo, si la autorización no tenía
 * @returns {Promise<object>} historial actualizado
 */
export function completarAutorizacionExonet(idTramite, idAutorizacion, datos, archivo) {
  return pedir(`${rutaAutorizacionesExonet(idTramite)}/${encodeURIComponent(idAutorizacion)}/completar`, {
    method: "POST",
    body: crearFormularioAutorizacionExonet(datos, archivo),
  });
}

/**
 * Anula la autorización vigente sin borrarla.
 *
 * @param {number} idTramite identificador del expediente
 * @param {number} idAutorizacion autorización vigente
 * @param {string} motivo motivo de la anulación, obligatorio
 * @returns {Promise<object>} historial actualizado
 */
export function anularAutorizacionExonet(idTramite, idAutorizacion, motivo) {
  return pedir(`${rutaAutorizacionesExonet(idTramite)}/${encodeURIComponent(idAutorizacion)}/anulacion`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ motivo }),
  });
}

/**
 * Sustituye la autorización vigente por una nueva, conservando ambas en el historial.
 *
 * @param {number} idTramite identificador del expediente
 * @param {number} idAutorizacion autorización vigente que se sustituye
 * @param {object} datos datos completos de la nueva autorización
 * @param {string} motivo motivo de la sustitución, obligatorio
 * @param {File|null} archivo documento de respaldo de la nueva, opcional
 * @returns {Promise<object>} historial actualizado
 */
export function sustituirAutorizacionExonet(idTramite, idAutorizacion, datos, motivo, archivo) {
  return pedir(`${rutaAutorizacionesExonet(idTramite)}/${encodeURIComponent(idAutorizacion)}/sustitucion`, {
    method: "POST",
    body: crearFormularioAutorizacionExonet(datos, archivo, motivo),
  });
}
