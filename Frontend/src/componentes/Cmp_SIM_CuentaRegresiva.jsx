/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Cuenta regresiva del vencimiento de un trámite temporal (HU-025). Muestra los días
 *   que faltan con un color según su cercanía: verde en plazo, amarillo a 45 días o menos, rojo si
 *   ya venció y gris si la aduana aún no fija el vencimiento. El nivel lo calcula el servidor.
 */

const NIVELES = {
  EN_PLAZO: { clase: "text-bg-success", icono: "bi-calendar-check" },
  PROXIMO: { clase: "text-bg-warning", icono: "bi-hourglass-split" },
  VENCIDO: { clase: "text-bg-danger", icono: "bi-exclamation-octagon" },
  SIN_FECHA: { clase: "text-bg-secondary", icono: "bi-calendar-x" },
};

/**
 * Da formato a una fecha sin hora (AAAA-MM-DD). Se arma en la zona local para no correr el día.
 *
 * @param {string} valor fecha que devuelve el servidor
 * @returns {string} fecha legible o una raya si no hay fecha
 */
export function formatearFechaLocal(valor) {
  if (!valor) {
    return "—";
  }
  const [anio, mes, dia] = valor.slice(0, 10).split("-").map(Number);
  return new Date(anio, mes - 1, dia).toLocaleDateString("es-CR", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

/** Fecha de hoy en formato AAAA-MM-DD, para limitar los calendarios. */
export function fechaHoyLocal() {
  const hoy = new Date();
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, "0")}-${String(hoy.getDate()).padStart(2, "0")}`;
}

/**
 * Texto de la cuenta regresiva.
 *
 * @param {object} vencimiento indicador que devuelve el servidor
 * @returns {string} descripción de los días restantes
 */
export function describirCuentaRegresiva(vencimiento) {
  if (!vencimiento?.nivel) {
    return "Expediente cerrado";
  }
  if (vencimiento.nivel === "SIN_FECHA") {
    return "Sin vencimiento autorizado";
  }
  const dias = vencimiento.diasRestantes;
  if (dias < 0) {
    return `Venció hace ${Math.abs(dias)} ${Math.abs(dias) === 1 ? "día" : "días"}`;
  }
  if (dias === 0) {
    return "Vence hoy";
  }
  return `Faltan ${dias} ${dias === 1 ? "día" : "días"}`;
}

function Cmp_SIM_CuentaRegresiva({ vencimiento, grande = false }) {
  const nivel = NIVELES[vencimiento?.nivel] ?? { clase: "text-bg-light text-dark", icono: "bi-lock" };

  return (
    <span className={`badge ${nivel.clase} ${grande ? "fs-6" : ""}`}>
      <i className={`bi ${nivel.icono} me-1`} aria-hidden="true"></i>
      {describirCuentaRegresiva(vencimiento)}
    </span>
  );
}

export default Cmp_SIM_CuentaRegresiva;
