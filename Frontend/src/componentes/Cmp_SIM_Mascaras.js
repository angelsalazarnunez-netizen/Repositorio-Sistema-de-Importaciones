/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Máscaras de captura para los códigos que digita la Unidad de Importaciones
 *   (referencias internas, contrataciones, órdenes de compra y contratos). Acomodan lo que la
 *   persona escribe o pega, sin bloquearle el teclado: colocan los guiones en su lugar, pasan
 *   las letras a mayúscula y descartan lo que no corresponde al formato.
 *
 *   Plantillas admitidas:
 *     #  un dígito
 *     A  una letra
 *     *  una letra o un dígito
 *   Cualquier otro carácter de la plantilla, como el guion, se escribe solo.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: pruebas de usabilidad (PU-06 y comentarios de la Unidad).
 *     - Plantilla de la cédula (9 dígitos, con el ejemplo 9-9999-9999 para que no se confunda con
 *       un dato real).
 *     - completarConsecutivo: al salir del campo, completa con ceros a la izquierda el consecutivo
 *       de una referencia (2026-1 queda 2026-0001), en lugar de obligar a digitar el cero.
 *     - usuarioDeCorreo: deja solo la parte del usuario de un correo institucional, para el campo
 *       con la terminación @ucr.ac.cr fija.
 */

const VALIDACIONES = {
  "#": (caracter) => /[0-9]/.test(caracter),
  A: (caracter) => /[A-ZÑ]/.test(caracter),
  "*": (caracter) => /[0-9A-ZÑ]/.test(caracter),
};

/** Plantillas de los códigos que se usan en los trámites. */
export const PLANTILLAS = {
  referencia: "####-####",
  contratacion: "####AA-######-##########",
  ordenCompra: "####-######",
  contrato: "####-######",
  cedula: "#-####-####",
};

/** Ejemplos que se muestran en los campos con máscara: 9 es un dígito, A una letra. */
export const EJEMPLOS_MASCARA = {
  cedula: "9-9999-9999",
};

/** Terminación fija de los correos institucionales. */
export const DOMINIO_CORREO_INSTITUCIONAL = "@ucr.ac.cr";

/**
 * Acomoda el texto según la plantilla indicada.
 *
 * @param {string} texto texto escrito o pegado por la persona
 * @param {string} plantilla plantilla de la máscara
 * @param {string} prefijo prefijo que se descarta si lo digitan o lo pegan
 * @returns {string} texto con el formato de la plantilla
 */
export function aplicarMascara(texto, plantilla, prefijo = "") {
  let limpio = (texto ?? "").toUpperCase().trim();

  if (prefijo) {
    const mayuscula = prefijo.toUpperCase();
    const inicios = [mayuscula, mayuscula.replace("-", ""), mayuscula.replace("-", " ")];
    for (const inicio of inicios) {
      if (inicio && limpio.startsWith(inicio)) {
        limpio = limpio.slice(inicio.length);
        break;
      }
    }
  }

  const caracteres = [...limpio].filter((caracter) => /[0-9A-ZÑ]/.test(caracter));
  let resultado = "";
  let posicion = 0;

  for (const simbolo of plantilla) {
    if (posicion >= caracteres.length) {
      break;
    }

    const validacion = VALIDACIONES[simbolo];
    if (!validacion) {
      resultado += simbolo;
      continue;
    }

    while (posicion < caracteres.length && !validacion(caracteres[posicion])) {
      posicion += 1;
    }

    if (posicion < caracteres.length) {
      resultado += caracteres[posicion];
      posicion += 1;
    }
  }

  return resultado;
}

/**
 * Devuelve el manejador de cambio de un campo con máscara.
 *
 * @param {Function} asignar función que guarda el valor en el estado
 * @param {string} plantilla plantilla de la máscara
 * @param {string} prefijo prefijo que no se digita
 */
export function alEscribirConMascara(asignar, plantilla, prefijo = "") {
  return (evento) => asignar(aplicarMascara(evento.target.value, plantilla, prefijo));
}

/**
 * Completa con ceros a la izquierda el último grupo de dígitos del texto, según la plantilla. Se
 * usa al salir del campo: si la persona escribió 2026-1 o 2026-001, queda 2026-0001.
 *
 * @param {string} texto valor del campo, ya con la máscara aplicada
 * @param {string} plantilla plantilla de la máscara
 * @returns {string} texto con el consecutivo completo
 */
export function completarConsecutivo(texto, plantilla) {
  const grupos = plantilla.split("-");
  const partes = (texto ?? "").split("-");
  if (!texto || partes.length !== grupos.length) {
    return texto ?? "";
  }
  const ultimo = partes.length - 1;
  const longitud = grupos[ultimo].length;
  const anteriorCompleto = partes.slice(0, ultimo).every((parte, indice) => parte.length === grupos[indice].length);
  if (!anteriorCompleto || !/^\d+$/.test(partes[ultimo]) || partes[ultimo].length >= longitud) {
    return texto;
  }
  partes[ultimo] = partes[ultimo].padStart(longitud, "0");
  return partes.join("-");
}

/**
 * Deja solo el usuario de un correo institucional: si la persona pega el correo completo, se
 * quita la terminación, porque el campo ya la muestra fija.
 *
 * @param {string} texto texto escrito o pegado
 * @returns {string} usuario del correo, en minúscula y sin espacios
 */
export function usuarioDeCorreo(texto) {
  const limpio = (texto ?? "").trim().toLowerCase().replace(/\s+/g, "");
  const arroba = limpio.indexOf("@");
  return arroba >= 0 ? limpio.slice(0, arroba) : limpio;
}
