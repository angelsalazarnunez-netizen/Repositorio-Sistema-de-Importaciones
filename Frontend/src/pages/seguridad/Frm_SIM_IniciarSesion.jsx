import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";

import {
  guardarSesion,
  iniciarSesion,
  obtenerSesion,
} from "../../servicios/Srv_SIM_Autenticacion.js";
import Cmp_SIM_CampoContrasena from "../../componentes/seguridad/Cmp_SIM_CampoContrasena.jsx";
import "./Frm_SIM_IniciarSesion.css";

const FORMULARIO_INICIAL = {
  correoInstitucional: "",
  contrasena: "",
};

function Frm_SIM_IniciarSesion() {
  const [formulario, setFormulario] = useState(FORMULARIO_INICIAL);
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);
  const navegar = useNavigate();

  if (obtenerSesion()?.token) {
    return <Navigate to="/menu" replace />;
  }

  function actualizarCampo(evento) {
    const { name, value } = evento.target;
    setFormulario((anterior) => ({ ...anterior, [name]: value }));
    setError("");
  }

  async function enviarFormulario(evento) {
    evento.preventDefault();
    setError("");
    setEnviando(true);
    try {
      const sesion = await iniciarSesion(formulario);
      if (!sesion.accesoConcedido || !sesion.token) {
        setError(sesion.mensaje || "No fue posible iniciar sesión.");
        return;
      }
      guardarSesion(sesion);
      navegar("/menu", { replace: true });
    } catch (problema) {
      setError(problema.message || "No fue posible conectar con el servicio de autenticación.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main className="sim-login">
      <section className="sim-login__tarjeta" aria-labelledby="titulo-login">
        <header className="sim-login__encabezado">
          <img src="/img/logo-ucr.png" alt="Universidad de Costa Rica" />
          <p>SIM · Sistema de Importaciones</p>
        </header>

        <div className="sim-login__contenido">
          <h1 id="titulo-login">Iniciar sesión</h1>
          <p className="text-muted">
            Ingrese con su correo institucional y contraseña.
          </p>

          <form id="loginForm" onSubmit={enviarFormulario}>
            <div className="mb-3">
              <label className="form-label" htmlFor="correoInstitucional">
                Correo institucional
              </label>
              <input
                autoComplete="username"
                className="form-control"
                id="correoInstitucional"
                name="correoInstitucional"
                onChange={actualizarCampo}
                required
                type="email"
                value={formulario.correoInstitucional}
              />
            </div>

            <div className="mb-4">
              <label className="form-label" htmlFor="contrasena">
                Contraseña
              </label>
              <Cmp_SIM_CampoContrasena
                autoComplete="current-password"
                id="contrasena"
                name="contrasena"
                onChange={actualizarCampo}
                required
                value={formulario.contrasena}
              />
            </div>

            {error && (
              <div className="alert alert-danger" id="mensaje-login" role="alert">
                {error}
              </div>
            )}

            <button
              className="btn btn-sim w-100"
              disabled={enviando}
              type="submit"
            >
              {enviando ? "Ingresando..." : "Ingresar"}
            </button>
          </form>

          <p className="text-center small mt-3 mb-0">
            ¿No tiene una cuenta? <Link to="/registro">Crear cuenta</Link>
          </p>
        </div>
      </section>
    </main>
  );
}

export default Frm_SIM_IniciarSesion;
