import { useState } from "react";

import { Cmp_SIM_AyudaCampo } from "./Cmp_SIM_Ayuda.jsx";

/** Límites de la carga de archivos que acepta el servidor (spring.servlet.multipart). */
export const TAMANO_MAXIMO_ARCHIVO_MB = 10;
export const TAMANO_MAXIMO_ENVIO_MB = 50;
const BYTES_POR_MB = 1024 * 1024;

/**
 * Separa los archivos que superan el tamaño máximo por archivo (H5 del Sprint 1), para avisarlo
 * antes de enviar en lugar de recibir un error técnico del servidor.
 *
 * @param {FileList|File[]} lista archivos elegidos
 * @returns {{aceptados: File[], rechazados: File[]}} archivos dentro y fuera del límite
 */
export function separarArchivosPorTamano(lista) {
  const archivos = [...(lista ?? [])];
  return {
    aceptados: archivos.filter((archivo) => archivo.size <= TAMANO_MAXIMO_ARCHIVO_MB * BYTES_POR_MB),
    rechazados: archivos.filter((archivo) => archivo.size > TAMANO_MAXIMO_ARCHIVO_MB * BYTES_POR_MB),
  };
}

/** Mensaje para los archivos que no se adjuntaron por su tamaño. */
export function describirRechazados(rechazados) {
  if (!rechazados || rechazados.length === 0) {
    return "";
  }
  const nombres = rechazados.map((archivo) => archivo.name).join(", ");
  return `No se adjuntó ${nombres}: cada archivo puede pesar hasta ${TAMANO_MAXIMO_ARCHIVO_MB} MB.`;
}

/**
 * Indicación de los formatos y del tamaño que se aceptan (H4 del Sprint 1). Se muestra junto a
 * cada campo de documentación.
 */
export function Cmp_SIM_IndicacionArchivos({ className = "form-text" }) {
  return (
    <div className={className}>
      <i className="bi bi-info-circle me-1" aria-hidden="true"></i>
      Se acepta PDF, imagen, Word, Excel u otro formato. Máximo {TAMANO_MAXIMO_ARCHIVO_MB} MB por
      archivo y {TAMANO_MAXIMO_ENVIO_MB} MB por envío.
    </div>
  );
}

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Documentación de un expediente. Muestra un campo por tipo de documento, indica
 *   cuáles son obligatorios y cuáles ya se adjuntaron, y avisa lo que queda pendiente. Se usa
 *   tanto en el registro de la solicitud de exoneración (HU-018) como en el desalmacenaje
 *   especial (HU-031), que admite documentación atípica.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: hallazgos H4 y H5 del Sprint 1. Se indica el formato y el tamaño máximo que se
 *     aceptan, y un archivo de más de 10 MB no se adjunta: el campo explica por qué.
 */
function Cmp_SIM_Documentos({ documentos, archivos, alCambiar }) {
  const [rechazos, setRechazos] = useState({});

  function alSeleccionar(codigo, listaArchivos) {
    const { aceptados, rechazados } = separarArchivosPorTamano(listaArchivos);
    setRechazos((anterior) => ({ ...anterior, [codigo]: describirRechazados(rechazados) }));
    alCambiar({ ...archivos, [codigo]: aceptados });
  }

  return (
    <div className="row g-3">
      <div className="col-12">
        <Cmp_SIM_IndicacionArchivos className="small text-muted" />
      </div>
      {documentos.map((documento) => {
        const seleccionados = archivos[documento.codigo] ?? [];
        const pendiente = documento.obligatorio && seleccionados.length === 0;
        const identificador = `fil${documento.codigo.charAt(0).toUpperCase()}${documento.codigo.slice(1)}`;

        return (
          <div className="col-12 col-md-6" key={documento.codigo}>
            <div className="d-flex justify-content-between align-items-center">
              <label className="form-label mb-1" htmlFor={identificador}>
                {documento.nombre}
                {documento.obligatorio
                  ? <span className="text-danger"> *</span>
                  : <span className="sim-opcional"> (si aplica)</span>}
                {documento.ayuda && (
                  <Cmp_SIM_AyudaCampo titulo={documento.nombre}>{documento.ayuda}</Cmp_SIM_AyudaCampo>
                )}
              </label>

              <span className={`badge ${pendiente ? "text-bg-warning" : seleccionados.length > 0
                ? "text-bg-success" : "text-bg-light text-dark"}`}>
                {pendiente ? "Pendiente" : seleccionados.length > 0
                  ? `${seleccionados.length} archivo(s)` : "Opcional"}
              </span>
            </div>

            <input
              type="file"
              className="form-control"
              id={identificador}
              multiple={Boolean(documento.multiple)}
              onChange={(evento) => alSeleccionar(documento.codigo, evento.target.files)}
            />

            {rechazos[documento.codigo] && (
              <div className="small text-danger mt-1" role="alert">{rechazos[documento.codigo]}</div>
            )}

            <Cmp_SIM_ListaArchivos
              archivos={seleccionados}
              alQuitar={(indice) => alCambiar({
                ...archivos,
                [documento.codigo]: seleccionados.filter((_, posicion) => posicion !== indice),
              })}
            />
          </div>
        );
      })}
    </div>
  );
}

/**
 * Archivos seleccionados que todavía no se han guardado. Se pueden quitar de la lista: como el
 * expediente aún no los tiene, basta con retirarlos antes de guardar.
 */
export function Cmp_SIM_ListaArchivos({ archivos, alQuitar }) {
  return (
    <ul className="sim-archivos">
      {(archivos ?? []).map((archivo, indice) => (
        <li className="d-flex justify-content-between align-items-center" key={`${archivo.name}-${indice}`}>
          <span>{archivo.name}</span>
          <button type="button" className="btn btn-link btn-sm text-danger p-0 ms-2"
                  aria-label={`Quitar ${archivo.name}`}
                  onClick={() => alQuitar(indice)}>
            Quitar
          </button>
        </li>
      ))}
    </ul>
  );
}

/** Devuelve los documentos obligatorios que todavía no se han adjuntado. */
export function obtenerPendientes(documentos, archivos) {
  return documentos.filter(
    (documento) => documento.obligatorio && (archivos[documento.codigo] ?? []).length === 0
  );
}

/** Cuenta todos los archivos seleccionados. */
export function contarArchivos(archivos) {
  return Object.values(archivos).reduce((total, lista) => total + (lista?.length ?? 0), 0);
}

export default Cmp_SIM_Documentos;
