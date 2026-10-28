/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 18/10/2026
 * Descripción: Botón "Imprimir bitácora" común a los expedientes (comentario de la Unidad: poder
 *   imprimir la bitácora en todos los trámites). Imprime solo la sección indicada, no la pantalla
 *   completa: durante la impresión marca el cuerpo de la página y la sección, y los estilos de
 *   index.css ocultan lo demás. Sirve también para guardar la bitácora en PDF desde el navegador.
 */

const CLASE_CUERPO = "sim-imprimir-seccion";
const CLASE_SECCION = "sim-seccion-impresa";

/**
 * Imprime solo la sección con el identificador indicado.
 *
 * @param {string} idSeccion id del elemento que se imprime
 */
export function imprimirSeccion(idSeccion) {
  const seccion = document.getElementById(idSeccion);
  if (!seccion) {
    window.print();
    return;
  }

  function limpiar() {
    document.body.classList.remove(CLASE_CUERPO);
    seccion.classList.remove(CLASE_SECCION);
    window.removeEventListener("afterprint", limpiar);
  }

  document.body.classList.add(CLASE_CUERPO);
  seccion.classList.add(CLASE_SECCION);
  window.addEventListener("afterprint", limpiar);
  window.print();
}

function Cmp_SIM_ImprimirBitacora({ idSeccion, texto = "Imprimir bitácora" }) {
  return (
    <button type="button" className="btn btn-sm btn-outline-secondary d-print-none"
            id="btnImprimirBitacora" onClick={() => imprimirSeccion(idSeccion)}>
      <i className="bi bi-printer me-1" aria-hidden="true"></i>{texto}
    </button>
  );
}

export default Cmp_SIM_ImprimirBitacora;
