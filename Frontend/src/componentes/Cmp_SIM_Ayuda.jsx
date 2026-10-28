import { useEffect, useId, useState } from "react";

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Ayuda en pantalla del SIM. Cmp_SIM_AyudaCampo coloca un botón de interrogación
 *   junto a la etiqueta de un campo y muestra una explicación breve al presionarlo.
 *   Cmp_SIM_VentanaAyuda arma la ayuda general del trámite, con los pasos a seguir y las dudas
 *   frecuentes, y Cmp_SIM_BotonAyuda es el botón que la abre.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: Cmp_SIM_VentanaAyuda recibe la propiedad opcional tituloPasos para las pantallas
 *     que no son trámites, como las de roles (HU-003). Si no se indica, conserva el título
 *     "Pasos para completar el trámite".
 */

/** Botón de ayuda de un campo, con su explicación emergente. */
export function Cmp_SIM_AyudaCampo({ titulo, children }) {
  const [visible, setVisible] = useState(false);
  const identificador = useId();

  useEffect(() => {
    if (!visible) {
      return undefined;
    }
    const cerrarConEscape = (evento) => {
      if (evento.key === "Escape") {
        setVisible(false);
      }
    };
    document.addEventListener("keydown", cerrarConEscape);
    return () => document.removeEventListener("keydown", cerrarConEscape);
  }, [visible]);

  return (
    <span className="position-relative d-inline-block">
      <button
        type="button"
        className="btn btn-link p-0 ms-1 align-baseline"
        aria-label={`Ayuda sobre ${titulo}`}
        aria-expanded={visible}
        aria-controls={identificador}
        onClick={() => setVisible((estado) => !estado)}
        onBlur={() => window.setTimeout(() => setVisible(false), 150)}
      >
        <i className="bi bi-question-circle" aria-hidden="true"></i>
      </button>

      {visible && (
        <span
          id={identificador}
          role="tooltip"
          className="position-absolute top-100 start-0 mt-1 p-2 bg-white border rounded shadow-sm small text-body"
          style={{ width: "20rem", zIndex: 1080 }}
        >
          <strong className="d-block mb-1">{titulo}</strong>
          {children}
        </span>
      )}
    </span>
  );
}

/** Botón que abre la ayuda general de la pantalla. */
export function Cmp_SIM_BotonAyuda({ alPresionar }) {
  return (
    <button type="button" className="btn btn-sm btn-outline-light" onClick={alPresionar}>
      <i className="bi bi-question-circle me-1" aria-hidden="true"></i>
      Ayuda
    </button>
  );
}

/** Ventana con la ayuda general del trámite: de qué se trata, pasos y dudas frecuentes. */
export function Cmp_SIM_VentanaAyuda({
  titulo,
  introduccion,
  pasos = [],
  dudas = [],
  tituloPasos = "Pasos para completar el trámite",
  alCerrar,
}) {
  return (
    <>
      <div className="modal fade show d-block" tabIndex="-1" role="dialog"
           aria-modal="true" aria-labelledby="lblTituloAyuda">
        <div className="modal-dialog modal-lg modal-dialog-scrollable">
          <div className="modal-content">
            <div className="modal-header">
              <h2 className="modal-title h5" id="lblTituloAyuda">
                <i className="bi bi-life-preserver me-2" aria-hidden="true"></i>
                {titulo}
              </h2>
              <button type="button" className="btn-close" aria-label="Cerrar" onClick={alCerrar}></button>
            </div>

            <div className="modal-body">
              <p>{introduccion}</p>

              {pasos.length > 0 && (
                <>
                  <h3 className="h6">{tituloPasos}</h3>
                  <ol className="mb-3">
                    {pasos.map((paso) => (
                      <li className="mb-1" key={paso}>{paso}</li>
                    ))}
                  </ol>
                </>
              )}

              {dudas.length > 0 && (
                <>
                  <h3 className="h6">Dudas frecuentes</h3>
                  <dl className="row mb-0 small">
                    {dudas.map((duda) => (
                      <div className="row g-0 mb-2" key={duda.pregunta}>
                        <dt className="col-12 col-md-4">{duda.pregunta}</dt>
                        <dd className="col-12 col-md-8 mb-0">{duda.respuesta}</dd>
                      </div>
                    ))}
                  </dl>
                </>
              )}
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={alCerrar}>
                Cerrar
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="modal-backdrop fade show"></div>
    </>
  );
}
