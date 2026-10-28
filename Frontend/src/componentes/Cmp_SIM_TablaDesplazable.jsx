import { useEffect, useRef, useState } from "react";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 25/10/2026
 * Descripción: Contenedor de una tabla más ancha que la pantalla, con la barra de desplazamiento
 *   horizontal también arriba (PU-12 de las pruebas con usuarios): en Buscar trámites la barra
 *   solo estaba al final de la tabla y había que bajar para alcanzar las columnas de la derecha.
 *   Las dos barras se mueven juntas. Si la tabla cabe en la pantalla, la barra de arriba no se
 *   muestra.
 */
function Cmp_SIM_TablaDesplazable({ children, className = "" }) {
  const barraSuperior = useRef(null);
  const contenedor = useRef(null);
  const [anchoContenido, setAnchoContenido] = useState(0);
  const [desborda, setDesborda] = useState(false);

  useEffect(() => {
    const elemento = contenedor.current;
    if (!elemento) {
      return undefined;
    }
    function medir() {
      setAnchoContenido(elemento.scrollWidth);
      setDesborda(elemento.scrollWidth > elemento.clientWidth + 1);
    }
    medir();
    const observador = new ResizeObserver(medir);
    observador.observe(elemento);
    if (elemento.firstElementChild) {
      observador.observe(elemento.firstElementChild);
    }
    return () => observador.disconnect();
  }, [children]);

  function sincronizar(origen, destino) {
    if (origen.current && destino.current && destino.current.scrollLeft !== origen.current.scrollLeft) {
      destino.current.scrollLeft = origen.current.scrollLeft;
    }
  }

  return (
    <>
      {desborda && (
        <div className="sim-scroll-superior" ref={barraSuperior} aria-hidden="true"
             onScroll={() => sincronizar(barraSuperior, contenedor)}>
          <div style={{ width: anchoContenido }}></div>
        </div>
      )}
      <div className={`table-responsive ${className}`} ref={contenedor}
           onScroll={() => sincronizar(contenedor, barraSuperior)}>
        {children}
      </div>
    </>
  );
}

export default Cmp_SIM_TablaDesplazable;
