import { useCallback, useEffect, useRef, useState } from "react";

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 30/09/2026
 * Descripción: Selección múltiple de un filtro (HU-034). Dibuja un campo con el aspecto de un
 *   select de Bootstrap que despliega una lista de casillas, para elegir uno o varios valores.
 *   Sin valores elegidos, el filtro no restringe. useCierreExterno cierra un desplegable al
 *   hacer clic fuera de él o al presionar Escape, porque la aplicación no carga el JavaScript
 *   de Bootstrap.
 */

/**
 * Cierra un desplegable abierto cuando se hace clic fuera de su contenedor o se presiona Escape.
 *
 * @param {object} referencia referencia al contenedor del desplegable
 * @param {boolean} abierto indica si el desplegable está abierto
 * @param {Function} alCerrar función que lo cierra
 */
export function useCierreExterno(referencia, abierto, alCerrar) {
  useEffect(() => {
    if (!abierto) {
      return undefined;
    }
    const cerrarAlSalir = (evento) => {
      if (referencia.current && !referencia.current.contains(evento.target)) {
        alCerrar();
      }
    };
    const cerrarConEscape = (evento) => {
      if (evento.key === "Escape") {
        alCerrar();
      }
    };
    document.addEventListener("mousedown", cerrarAlSalir);
    document.addEventListener("keydown", cerrarConEscape);
    return () => {
      document.removeEventListener("mousedown", cerrarAlSalir);
      document.removeEventListener("keydown", cerrarConEscape);
    };
  }, [referencia, abierto, alCerrar]);
}

function resumirSeleccion(opciones, seleccion, textoTodos) {
  if (seleccion.length === 0) {
    return textoTodos;
  }
  if (seleccion.length === 1) {
    return opciones.find((opcion) => String(opcion.valor) === seleccion[0])?.nombre ?? textoTodos;
  }
  return `${seleccion.length} seleccionados`;
}

/**
 * @param {string} id identificador del botón, para asociarle su etiqueta con htmlFor
 * @param {object[]} opciones lista de objetos { valor, nombre }
 * @param {string[]} seleccion valores elegidos, como texto
 * @param {Function} alCambiar recibe la nueva lista de valores elegidos, en el orden de la lista
 * @param {string} textoTodos texto del campo cuando no hay valores elegidos
 * @param {string} textoSinOpciones texto de la lista cuando no hay opciones
 */
function Cmp_SIM_SeleccionMultiple({
  id,
  opciones,
  seleccion,
  alCambiar,
  textoTodos = "Todos",
  textoSinOpciones = "No hay opciones registradas.",
}) {
  const [abierto, setAbierto] = useState(false);
  const contenedor = useRef(null);
  const cerrar = useCallback(() => setAbierto(false), []);

  useCierreExterno(contenedor, abierto, cerrar);

  function alternar(valor) {
    const elegido = seleccion.includes(valor);
    alCambiar(opciones
      .map((opcion) => String(opcion.valor))
      .filter((actual) => (actual === valor ? !elegido : seleccion.includes(actual))));
  }

  return (
    <div className="dropdown sim-seleccion-multiple" ref={contenedor}>
      <button type="button" id={id} className="form-select text-start text-truncate"
              aria-expanded={abierto} disabled={opciones.length === 0}
              onClick={() => setAbierto((estado) => !estado)}>
        {resumirSeleccion(opciones, seleccion, textoTodos)}
      </button>

      <div className={`dropdown-menu w-100 px-3 py-2${abierto ? " show" : ""}`} data-bs-popper="static">
        {opciones.length === 0 && (
          <span className="dropdown-item-text small text-muted">{textoSinOpciones}</span>
        )}

        {opciones.map((opcion, indice) => {
          const valor = String(opcion.valor);
          return (
            <div className="form-check mb-1" key={valor}>
              <input type="checkbox" className="form-check-input" id={`chk${id}${indice}`}
                     checked={seleccion.includes(valor)} onChange={() => alternar(valor)} />
              <label className="form-check-label" htmlFor={`chk${id}${indice}`}>{opcion.nombre}</label>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default Cmp_SIM_SeleccionMultiple;
