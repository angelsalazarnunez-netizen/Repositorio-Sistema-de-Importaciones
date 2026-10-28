import { useSearchParams } from "react-router-dom";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 25/10/2026
 * Descripción: Pestañas horizontales de navegación de un expediente. Responden a las pruebas de
 *   usabilidad (PU-02 y comentario de la Unidad): en los expedientes había que bajar toda la página
 *   para llegar al historial o a la bitácora, que es lo primero que la Unidad revisa en el expediente
 *   físico. Cada apartado queda en su pestaña y la barra se mantiene visible al desplazarse.
 *
 *   La pestaña elegida se guarda en la dirección (?seccion=historial), así un enlace o el botón
 *   Atrás del navegador vuelven al mismo apartado.
 */

/**
 * Devuelve la pestaña activa y la función para cambiarla.
 *
 * @param {string[]} identificadores pestañas disponibles
 * @param {string} inicial pestaña que se abre si la dirección no indica otra
 * @returns {[string, Function]} pestaña activa y función para elegir otra
 */
export function usePestanaActiva(identificadores, inicial) {
  const [parametros, setParametros] = useSearchParams();
  const pedida = parametros.get("seccion");
  const activa = identificadores.includes(pedida) ? pedida : inicial;

  function elegir(identificador) {
    const nuevos = new URLSearchParams(parametros);
    nuevos.set("seccion", identificador);
    setParametros(nuevos, { replace: true });
  }

  return [activa, elegir];
}

/**
 * @param {object} propiedades
 * @param {{id: string, titulo: string, icono?: string, contador?: number}[]} propiedades.pestanas
 * @param {string} propiedades.activa pestaña activa
 * @param {Function} propiedades.alCambiar recibe el identificador de la pestaña elegida
 */
function Cmp_SIM_Pestanas({ pestanas, activa, alCambiar }) {
  return (
    <nav className="sim-pestanas mb-3 d-print-none" aria-label="Apartados del expediente">
      <ul className="nav nav-tabs flex-nowrap overflow-auto" role="tablist">
        {pestanas.map((pestana) => (
          <li className="nav-item" role="presentation" key={pestana.id}>
            <button
              type="button"
              role="tab"
              id={`tab-${pestana.id}`}
              aria-selected={pestana.id === activa}
              className={`nav-link text-nowrap ${pestana.id === activa ? "active" : ""}`}
              onClick={() => alCambiar(pestana.id)}
            >
              {pestana.icono && <i className={`bi ${pestana.icono} me-1`} aria-hidden="true"></i>}
              {pestana.titulo}
              {pestana.contador !== undefined && pestana.contador !== null && (
                <span className="badge rounded-pill text-bg-light border ms-2">{pestana.contador}</span>
              )}
            </button>
          </li>
        ))}
      </ul>
    </nav>
  );
}

export default Cmp_SIM_Pestanas;

/**
 * Devuelve el movimiento más reciente de una lista (historial o bitácora), sin depender del orden
 * en que la envía el servidor.
 *
 * @param {object[]} lista movimientos
 * @param {string} campo nombre del campo con la fecha
 * @returns {object|null} movimiento más reciente
 */
export function ultimoPorFecha(lista, campo = "fechaHora") {
  return (lista ?? []).reduce((ultimo, movimiento) => (
    !ultimo || new Date(movimiento[campo]) > new Date(ultimo[campo]) ? movimiento : ultimo
  ), null);
}
