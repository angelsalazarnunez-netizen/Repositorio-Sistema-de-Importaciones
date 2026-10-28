/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Configuración y utilidades comunes de los trámites temporales: la importación
 *   temporal (HU-025) y la exportación temporal (HU-026) comparten la lista, el registro y el
 *   expediente; aquí está lo que cambia entre ellas (rutas, sigla, estados, DUA, documentos,
 *   campos propios y textos de ayuda).
 *
 * Historial de cambios:*
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: PU-11: "DUA de reexportación" pasa a "DUA de cancelación"; H-11: la ayuda
 *     describe la prórroga como funciona (el sistema suma seis meses y pide el oficio y la
 *     resolución).
 */

export const NOMBRES_ESTADO_MERCANCIA = {
  NUEVA: "Nueva",
  USADA: "Usada",
  REACONDICIONADA: "Reacondicionada",
};

// Meses del plazo estándar de la exportación: el vencimiento es la fecha del DUA de salida más 6 meses.
export const MESES_PLAZO_ESTANDAR_EXPORTACION = 6;

/** Documentos de ayuda que se repiten en ambos tipos. */
const DOCUMENTO_RESPALDO = {
  codigo: "RESPALDO",
  nombre: "Otros documentos de respaldo",
  multiple: true,
  alRegistrar: true,
  ayuda: "Correos u otros documentos que respaldan el trámite. Puede adjuntar varios a la vez.",
};

/**
 * Configuración de cada tipo de trámite temporal. "servicio" es el segmento de la ruta del
 * servicio REST; "ruta" es la ruta de la pantalla. En los documentos, "alRegistrar" indica los que
 * se ofrecen en el registro; los demás llegan en etapas posteriores.
 */
export const TIPOS_TRAMITE_TEMPORAL = {
  importacion: {
    clave: "importacion",
    servicio: "importaciones-temporales",
    ruta: "/tramites/importaciones-temporales",
    nombreTipoTramite: "Importación temporal",
    titulo: "Importaciones temporales",
    singular: "importación temporal",
    icono: "bi-box-arrow-in-down",
    sigla: "IT-",
    estadoInicial: "Por llegar",
    estadoEnDestino: "Activo en el país",
    estadosCierre: "Finalizado y Cancelado",
    monedaPorDefecto: "USD",
    esExportacion: false,
    proposito: "un equipo que ingresa temporalmente al país: préstamos, exhibiciones o equipos de investigación compartida",
    ejemplos: {
      unidad: "Por ejemplo, CIGRAS",
      oficio: "Por ejemplo, CIGRAS-53-2025",
      descripcion: "Por ejemplo, fluorómetro en préstamo para investigación",
    },
    duaInicio: { codigo: "DUA_IMPORT_TEMPORAL", nombre: "DUA de importación temporal" },
    duaCierre: { codigo: "DUA_REEXPORTACION", nombre: "DUA de cancelación" },
    seccionDatos: "Ingreso y salida",
    seccionInicio: "Ingreso",
    seccionCierre: "Salida",
    documentos: [
      { codigo: "OFICIO_SOLICITUD", nombre: "Oficio del usuario solicitante", alRegistrar: true },
      { codigo: "FACTURA", nombre: "Factura del exportador", alRegistrar: true },
      { codigo: "OFICIO_ADUANA", nombre: "Oficio enviado a la aduana", alRegistrar: true },
      { codigo: "RESOL_AUTORIZACION", nombre: "Resolución de autorización", alRegistrar: true },
      { codigo: "DUA_IMPORT_TEMPORAL", nombre: "DUA de importación temporal", alRegistrar: true },
      { codigo: "OFICIO_PRORROGA", nombre: "Oficio de solicitud de prórroga" },
      { codigo: "AUTORIZ_PRORROGA", nombre: "Autorización de prórroga" },
      { codigo: "DUA_REEXPORTACION", nombre: "DUA de cancelación" },
      { codigo: "OFICIO_CANCELACION", nombre: "Oficio de cancelación del régimen" },
      DOCUMENTO_RESPALDO,
    ],
  },
  exportacion: {
    clave: "exportacion",
    servicio: "exportaciones-temporales",
    ruta: "/tramites/exportaciones-temporales",
    nombreTipoTramite: "Exportación temporal",
    titulo: "Exportaciones temporales",
    singular: "exportación temporal",
    icono: "bi-box-arrow-up",
    sigla: "EXP-T-",
    estadoInicial: "Por salir",
    estadoEnDestino: "Fuera del país",
    estadosCierre: "Finalizado, Salida definitiva y Anulado",
    monedaPorDefecto: "CRC",
    esExportacion: true,
    prefijoReimportacion: "RI-",
    proposito: "un equipo de la Universidad que sale temporalmente del país: reparación, garantía, préstamos, exhibiciones o investigación compartida",
    ejemplos: {
      unidad: "Por ejemplo, CICIMA",
      oficio: "Por ejemplo, CICIMA-246-2025",
      descripcion: "Por ejemplo, cuadrupolo para espectroscopía de masas, marca, modelo, serie y placa",
    },
    duaInicio: { codigo: "DUA_EXPORT_TEMPORAL", nombre: "DUA de exportación temporal" },
    duaCierre: { codigo: "DUA_REIMPORTACION", nombre: "DUA de reingreso" },
    seccionDatos: "Salida y reimportación",
    seccionInicio: "Salida",
    seccionCierre: "Reimportación",
    documentos: [
      { codigo: "OFICIO_SOLICITUD", nombre: "Oficio del usuario solicitante", alRegistrar: true },
      { codigo: "FACTURA", nombre: "Factura del exportador", alRegistrar: true },
      { codigo: "OFICIO_ADUANA", nombre: "Oficio enviado a la aduana", alRegistrar: true },
      { codigo: "RESOL_AUTORIZACION", nombre: "Resolución de autorización", alRegistrar: true },
      { codigo: "OFICIO_NOTIF_USUARIO", nombre: "Oficio de notificación al usuario", alRegistrar: true },
      { codigo: "DUA_EXPORT_TEMPORAL", nombre: "DUA de exportación temporal", alRegistrar: true },
      { codigo: "OFICIO_PRORROGA", nombre: "Oficio de solicitud de prórroga" },
      { codigo: "AUTORIZ_PRORROGA", nombre: "Autorización de prórroga" },
      { codigo: "SOLICITUD_REINGRESO", nombre: "Solicitud de reingreso del usuario" },
      { codigo: "EMBARQUE", nombre: "Documento de embarque" },
      { codigo: "DUA_REIMPORTACION", nombre: "DUA de reingreso" },
      { codigo: "OFICIO_CANCELACION", nombre: "Oficio de cancelación del régimen" },
      DOCUMENTO_RESPALDO,
    ],
  },
};

/**
 * Tipo de trámite temporal a partir del nombre del tipo que devuelve el servidor, por ejemplo en
 * las alertas de vencimiento. Si no se reconoce, se asume la importación temporal.
 *
 * @param {string} nombreTipoTramite nombre del tipo de trámite
 * @returns {object} configuración del tipo
 */
export function obtenerTipoTemporalPorNombre(nombreTipoTramite) {
  return Object.values(TIPOS_TRAMITE_TEMPORAL)
    .find((tipo) => tipo.nombreTipoTramite === nombreTipoTramite) ?? TIPOS_TRAMITE_TEMPORAL.importacion;
}

/** Texto con la primera letra en mayúscula. */
export function capitalizar(texto) {
  return texto ? texto.charAt(0).toUpperCase() + texto.slice(1) : texto;
}

/** Color del estado del trámite. */
export function claseEstadoTemporal(estado) {
  if (estado === "Por llegar" || estado === "Por salir") {
    return "text-bg-warning";
  }
  if (estado === "Retenido en aduana" || estado === "Vencido") {
    return "text-bg-danger";
  }
  if (estado === "Finalizado") {
    return "text-bg-success";
  }
  if (estado === "Salida definitiva") {
    return "text-bg-dark";
  }
  if (estado === "Cancelado" || estado === "Anulado") {
    return "text-bg-secondary";
  }
  return "text-bg-info";
}

export function formatearMonto(valor, moneda) {
  if (valor === null || valor === undefined) {
    return "—";
  }
  return `${moneda ?? ""} ${Number(valor).toLocaleString("es-CR", {
    minimumFractionDigits: 2, maximumFractionDigits: 2,
  })}`.trim();
}

/**
 * Suma meses a una fecha AAAA-MM-DD. Si el día no existe en el mes de llegada, usa el último día
 * del mes, igual que el servidor al calcular el plazo estándar.
 *
 * @param {string} fecha fecha AAAA-MM-DD
 * @param {number} meses meses que se suman
 * @returns {string} fecha resultante AAAA-MM-DD, o vacío si no hay fecha
 */
export function sumarMeses(fecha, meses) {
  if (!fecha) {
    return "";
  }
  const [anio, mes, dia] = fecha.split("-").map(Number);
  const totalMeses = (mes - 1) + meses;
  const anioNuevo = anio + Math.floor(totalMeses / 12);
  const mesNuevo = (totalMeses % 12) + 1;
  const ultimoDia = new Date(anioNuevo, mesNuevo, 0).getDate();
  const diaNuevo = Math.min(dia, ultimoDia);
  return `${anioNuevo}-${String(mesNuevo).padStart(2, "0")}-${String(diaNuevo).padStart(2, "0")}`;
}

/** Ayuda de la lista de trámites de un tipo. */
export function crearAyudaLista(tipo) {
  return {
    titulo: `Ayuda: ${tipo.titulo.toLowerCase()}`,
    introduccion:
      `Esta pantalla reúne los expedientes de ${tipo.titulo.toLowerCase()}. Por defecto muestra los trámites `
      + `activos: los que están ${tipo.estadoInicial.toLowerCase()} o en gestión, empezando por los que vencen primero.`,
    pasos: [
      "Revise la columna Vencimiento: el color indica qué tan cerca está la fecha que autorizó la aduana.",
      "Escriba parte de la referencia, la unidad o la mercancía para encontrar un expediente.",
      "Cambie a Cerrados o Todos para consultar los trámites cerrados.",
      "Presione Ver expediente para cambiar su estado, completar los datos de aduana o registrar una prórroga.",
      `Para registrar un equipo nuevo, presione Registrar ${tipo.singular}.`,
    ],
    dudas: [
      {
        pregunta: "¿Qué significa cada color del vencimiento?",
        respuesta:
          "Verde: en plazo. Amarillo: faltan 45 días o menos. Rojo: el plazo ya venció. Gris: la aduana "
          + "todavía no ha fijado el vencimiento.",
      },
      {
        pregunta: "¿Por qué un expediente retenido sigue con su cuenta regresiva?",
        respuesta:
          "Porque el vencimiento legal sigue corriendo aunque el equipo esté retenido en aduana. Lo que "
          + "se pausa son los pendientes operativos del expediente.",
      },
      {
        pregunta: "¿Dónde veo las alertas de vencimiento?",
        respuesta:
          "En Alertas de vencimiento. El sistema las genera para el responsable del expediente y los "
          + "agentes asignados cuando faltan 45 días o menos.",
      },
    ],
  };
}

/** Ayuda del registro de un tipo de trámite temporal. */
export function crearAyudaRegistro(tipo) {
  const dudas = [
    {
      pregunta: "¿Cuándo empieza la cuenta regresiva?",
      respuesta:
        "Cuando el expediente tiene la fecha de vencimiento que autorizó la aduana. Si aún no la tiene, "
        + "regístrela después desde el expediente.",
    },
    {
      pregunta: "¿Quién recibe las alertas de vencimiento?",
      respuesta:
        "Usted, como responsable del expediente, y los agentes de aduanas que marque. Los agentes son opcionales.",
    },
  ];
  if (tipo.esExportacion) {
    dudas.unshift({
      pregunta: "¿Qué pongo en el antecedente?",
      respuesta:
        "La forma en que la Universidad adquirió el equipo: su placa, el pedido (por ejemplo P-005-2024), "
        + "la donación o la compra local. Es opcional.",
    });
    dudas.push({
      pregunta: "¿Por qué se llena sola la fecha de vencimiento?",
      respuesta:
        "Porque el plazo estándar del régimen es la fecha del DUA de exportación más 6 meses. Si la aduana "
        + "autorizó otra fecha, cámbiela.",
    });
  } else {
    dudas.unshift({
      pregunta: "¿Qué pongo en la factura si el equipo no trae?",
      respuesta: "Escriba SR (sin registro), como en los registros de la Unidad.",
    });
  }
  return {
    titulo: `Ayuda: registrar una ${tipo.singular}`,
    introduccion:
      `Esta pantalla abre el expediente de ${tipo.proposito}. El expediente nace ${tipo.estadoInicial}.`,
    pasos: [
      `Digite la referencia: la pantalla agrega la sigla ${tipo.sigla} y usted escribe el consecutivo y el año, por ejemplo 001-26.`,
      "Complete los datos de la solicitud de la unidad: unidad, oficio, fecha de recepción y descripción del equipo.",
      tipo.esExportacion
        ? "Indique el estado físico del equipo, si está en garantía, su valor con la moneda, su peso y el país de destino."
        : "Indique el estado físico del equipo y su valor declarado con la moneda.",
      "Si ya tiene datos de la aduana (oficio, número de gestión, resolución, DUA o vencimiento), regístrelos; si no, los completa después desde el expediente.",
      "Adjunte los documentos que tenga. Ninguno es obligatorio para registrar.",
      "Presione Crear expediente.",
    ],
    dudas,
  };
}

/** Ayuda del expediente de un tipo de trámite temporal. */
export function crearAyudaExpediente(tipo) {
  return {
    titulo: `Ayuda: expediente de ${tipo.singular}`,
    introduccion:
      "El expediente reúne los datos del equipo, su cronograma de vencimiento, la gestión ante la aduana, "
      + "la documentación y el historial de estados.",
    pasos: [
      "Revise la cuenta regresiva y los pendientes de la etapa en la parte superior.",
      "Para avanzar el trámite, elija el nuevo estado en Cambiar estado y escriba la justificación.",
      `Conforme la aduana responda, complete el oficio, el número de gestión, la resolución, el DUA y el vencimiento en ${tipo.seccionDatos}.`,
      // H-11 (Sprint 2): la ayuda describe la prórroga tal como funciona ahora.
      "Si se necesita más tiempo, registre la prórroga en su pestaña con el motivo, los números de oficio, "
      + "de gestión y de autorización, y adjunte el oficio de solicitud enviado a la aduana y la resolución "
      + "que emitió. El sistema calcula el nuevo vencimiento: suma seis meses al vencimiento vigente.",
      "Adjunte los documentos que respaldan cada dato en Agregar documentación.",
    ],
    dudas: [
      {
        pregunta: `¿Por qué no puedo pasar a ${tipo.estadoEnDestino}?`,
        respuesta: `Porque falta la fecha de vencimiento que autorizó la aduana. Regístrela en ${tipo.seccionDatos}.`,
      },
      {
        pregunta: "¿Qué pide el sistema para finalizar?",
        respuesta: tipo.esExportacion
          ? "El DUA de reingreso y la cancelación del régimen, cada uno con su número, su fecha o su documento. "
            + "Si el equipo no regresa, use Salida definitiva."
          : "El DUA de cancelación y la cancelación del régimen, cada uno con su número, su fecha o su documento.",
      },
      {
        pregunta: "¿Qué pasa si el equipo queda retenido en aduana?",
        respuesta:
          "Los pendientes operativos se pausan mientras dure la retención, pero la cuenta regresiva y las "
          + "alertas del vencimiento legal siguen activas.",
      },
      {
        pregunta: "¿Puedo cambiar algo de un expediente cerrado?",
        respuesta: `Solo el número, la fecha y el documento del ${tipo.duaCierre.nombre}. Todo lo demás se conserva para consulta.`,
      },
    ],
  };
}
