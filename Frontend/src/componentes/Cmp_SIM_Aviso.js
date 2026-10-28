import { useEffect, useRef } from "react";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 25/10/2026
 * Descripción: Lleva la vista hasta el aviso de la pantalla cada vez que cambia, y le pasa el foco.
 *   Corrige el caso que se repitió en las evaluaciones (H-03, H-07 y H-10 del Sprint 2, y PU-01 de
 *   las pruebas con usuarios): en las pantallas largas el mensaje se escribe al inicio y queda fuera
 *   de la vista de quien presionó el botón, que no se entera de si la acción se completó.
 *
 *   Uso: const refAviso = useVistaAlAviso(alerta);
 *        <div className="alert ..." role="alert" tabIndex={-1} ref={refAviso}>...</div>
 *
 * @param {unknown} aviso valor del aviso; cuando cambia y no está vacío se mueve la vista
 * @returns {import("react").RefObject<HTMLElement>} referencia que se asigna al elemento del aviso
 */
export function useVistaAlAviso(aviso) {
  const referencia = useRef(null);

  useEffect(() => {
    if (aviso && referencia.current) {
      referencia.current.scrollIntoView({ behavior: "smooth", block: "center" });
      referencia.current.focus({ preventScroll: true });
    }
  }, [aviso]);

  return referencia;
}
