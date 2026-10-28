import { useState } from "react";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Campo de contraseña con un botón de ojo que muestra u oculta lo escrito. Lo usan el
 *   inicio de sesión y el registro de usuario. Recibe las mismas propiedades que un input
 *   (id, name, value, onChange, autoComplete, required...) y las pasa al campo.
 */
function Cmp_SIM_CampoContrasena({ className = "form-control", ...propiedades }) {
  const [visible, setVisible] = useState(false);

  return (
    <div className="input-group">
      <input {...propiedades} className={className} type={visible ? "text" : "password"} />
      <button
        type="button"
        className="btn btn-outline-secondary"
        aria-label={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
        aria-controls={propiedades.id}
        aria-pressed={visible}
        title={visible ? "Ocultar contraseña" : "Mostrar contraseña"}
        onClick={() => setVisible((estado) => !estado)}
      >
        <i className={visible ? "bi bi-eye-slash" : "bi bi-eye"} aria-hidden="true"></i>
      </button>
    </div>
  );
}

export default Cmp_SIM_CampoContrasena;
