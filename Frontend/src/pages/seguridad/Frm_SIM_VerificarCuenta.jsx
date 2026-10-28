import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { RUTA_VERIFICAR } from "../../servicios/Srv_SIM_Seguridad.js";
import "./Frm_SIM_Seguridad.css";
import "./Frm_SIM_RegistrarUsuario.css";

/**
 * Descripción: Validación del correo institucional con el enlace enviado al registrar la cuenta
 *   (HU-001).
 */

function Frm_SIM_VerificarCuenta() {
    const [parametros] = useSearchParams();

    const token =
        parametros.get("token") || "";

    const [respuestaValidacion, setRespuestaValidacion] =
        useState(null);

    const [mensaje, setMensaje] =
        useState("Validando correo institucional...");

    const [error, setError] =
        useState("");

    useEffect(() => {
        async function verificar() {
            if (!token) {
                setError(
                    "El enlace de validación no es válido."
                );
                setMensaje("");
                return;
            }

            try {
                const respuesta = await fetch(
                    `${RUTA_VERIFICAR}?token=${encodeURIComponent(
                        token
                    )}`
                );

                const cuerpo =
                    await respuesta.json();

                if (!respuesta.ok) {
                    setError(
                        cuerpo.mensaje ||
                        "No fue posible validar el correo."
                    );

                    setMensaje("");
                    return;
                }

                setRespuestaValidacion(
                    cuerpo
                );

                setMensaje(
                    cuerpo.mensaje
                );
            } catch {
                setError(
                    "No se pudo conectar con el servicio de validación."
                );

                setMensaje("");
            }
        }

        verificar();
    }, [token]);

    return (
        <main className="sim-seguridad pantalla-registro">
            <section className="tarjeta-registro">

                <header className="encabezado-registro">
                    <div
                        className="logo-ucr"
                        aria-hidden="true"
                    >
                        UCR
                    </div>

                    <h1>
                        Validar cuenta - Sistema de importaciones
                    </h1>
                </header>

                <div className="cuerpo-registro">

                    {mensaje ? (
                        <div
                            className="alert alert-success"
                            role="status"
                        >
                            {mensaje}
                        </div>
                    ) : null}

                    {respuestaValidacion ? (
                        <div className="resumen-registro">
                            <p>
                                <strong>
                                    Correo institucional:
                                </strong>{" "}
                                {
                                    respuestaValidacion.correoInstitucional
                                }
                            </p>

                            <p>
                                <strong>
                                    Estado de validación:
                                </strong>{" "}
                                {
                                    respuestaValidacion.estadoValidacion
                                }
                            </p>

                            <p>
                                <strong>
                                    Estado de asignación de rol:
                                </strong>{" "}
                                {
                                    respuestaValidacion.estadoAsignacionRol
                                }
                            </p>

                            <p className="nota">
                                La cuenta ya puede continuar con el proceso
                                de asignación de roles por parte de la
                                jefatura.
                            </p>
                        </div>
                    ) : null}

                    {error ? (
                        <div
                            className="alert alert-danger"
                            role="alert"
                        >
                            {error}
                        </div>
                    ) : null}

                    <div className="acciones">
                        <Link
                            className="btn btn-outline-secondary"
                            to="/registro"
                            style={{
                                textDecoration: "none",
                            }}
                        >
                            Ir al registro
                        </Link>

                        <Link
                            className="btn btn-primary"
                            to="/login"
                            style={{
                                textDecoration: "none",
                            }}
                        >
                            Ir a iniciar sesión
                        </Link>
                    </div>

                </div>
            </section>
        </main>
    );
}

export default Frm_SIM_VerificarCuenta;
