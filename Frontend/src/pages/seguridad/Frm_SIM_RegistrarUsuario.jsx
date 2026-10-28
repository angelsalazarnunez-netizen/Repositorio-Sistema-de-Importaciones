import { useMemo, useState } from "react";
import { Link } from "react-router-dom";

import { RUTA_REGISTRO } from "../../servicios/Srv_SIM_Seguridad.js";
import Cmp_SIM_CampoContrasena from "../../componentes/seguridad/Cmp_SIM_CampoContrasena.jsx";
import { EJEMPLOS_MASCARA, PLANTILLAS, aplicarMascara } from "../../componentes/Cmp_SIM_Mascaras.js";
import "./Frm_SIM_Seguridad.css";
import "./Frm_SIM_RegistrarUsuario.css";

/**
 * Descripción: Registro de una cuenta de usuario con correo institucional (HU-001). La cuenta
 *   queda pendiente de validar el correo y de que la jefatura le asigne un rol.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: comentarios de las pruebas de usabilidad.
 *     - La cédula usa la máscara 9-9999-9999: el ejemplo con nueves no se confunde con un dato real.
 *     - La contraseña se digita dos veces y se avisa si no coinciden.
 *     - La fecha de nacimiento no acepta personas menores de 18 años (el servidor también lo valida).
 *     - Los campos obligatorios llevan asterisco y la pantalla tiene una descripción breve.
 */

const EDAD_MINIMA = 18;

/** Fecha más reciente que se acepta como nacimiento: hoy hace 18 años, en formato aaaa-mm-dd. */
function obtenerFechaMaximaNacimiento() {
    const hoy = new Date();
    const limite = new Date(hoy.getFullYear() - EDAD_MINIMA, hoy.getMonth(), hoy.getDate());
    const mes = String(limite.getMonth() + 1).padStart(2, "0");
    const dia = String(limite.getDate()).padStart(2, "0");
    return `${limite.getFullYear()}-${mes}-${dia}`;
}

const ESTADO_INICIAL = {
    nombre: "",
    primerApellido: "",
    segundoApellido: "",
    numeroCedula: "",
    fechaNacimiento: "",
    correoInstitucional: "",
    contrasena: "",
    confirmacionContrasena: "",
};

function Frm_SIM_RegistrarUsuario() {
    const [formulario, setFormulario] = useState(ESTADO_INICIAL);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState("");
    const [exito, setExito] = useState(null);

    const requisitos = useMemo(() => {
        const clave = formulario.contrasena;

        return {
            longitud: clave.length >= 8,
            mayuscula: /[A-Z]/.test(clave),
            numero: /[0-9]/.test(clave),
            especial: /[^A-Za-z0-9]/.test(clave),
        };
    }, [formulario.contrasena]);

    const fechaMaximaNacimiento = obtenerFechaMaximaNacimiento();
    const contrasenasDistintas =
        formulario.confirmacionContrasena !== ""
        && formulario.confirmacionContrasena !== formulario.contrasena;

    function actualizarCampo(evento) {
        const { name } = evento.target;
        const value = name === "numeroCedula"
            ? aplicarMascara(evento.target.value, PLANTILLAS.cedula)
            : evento.target.value;

        setFormulario((previo) => ({
            ...previo,
            [name]: value,
        }));

        setError("");
    }

    function cancelarRegistro() {
        setFormulario(ESTADO_INICIAL);
        setError("");
        setExito(null);
    }

    async function registrarUsuario(evento) {
        evento.preventDefault();

        setError("");
        setExito(null);

        const correoNormalizado =
            formulario.correoInstitucional
                .trim()
                .toLowerCase();

        if (!correoNormalizado.endsWith("@ucr.ac.cr")) {
            setError(
                "Solo se aceptan correos del dominio institucional de la UCR."
            );
            return;
        }

        if (formulario.numeroCedula.replace(/\D/g, "").length !== 9) {
            setError("El número de cédula debe contener 9 dígitos, con el formato 9-9999-9999.");
            return;
        }

        if (!formulario.fechaNacimiento || formulario.fechaNacimiento > fechaMaximaNacimiento) {
            setError(`Debe ser mayor de ${EDAD_MINIMA} años para registrarse. Revise la fecha de nacimiento.`);
            return;
        }

        if (
            !requisitos.longitud ||
            !requisitos.mayuscula ||
            !requisitos.numero ||
            !requisitos.especial
        ) {
            setError(
                "La contraseña no cumple las políticas de seguridad: mínimo 8 caracteres, una letra mayúscula, un número y un carácter especial."
            );
            return;
        }

        if (formulario.contrasena !== formulario.confirmacionContrasena) {
            setError("Las contraseñas no coinciden. Escríbala de nuevo en la confirmación.");
            return;
        }

        setEnviando(true);

        try {
            const respuesta = await fetch(
                RUTA_REGISTRO,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        nombre: formulario.nombre.trim(),
                        primerApellido:
                            formulario.primerApellido.trim(),
                        segundoApellido:
                            formulario.segundoApellido.trim(),
                        numeroCedula:
                            formulario.numeroCedula.replace(/\D/g, ""),
                        fechaNacimiento:
                        formulario.fechaNacimiento,
                        correoInstitucional:
                        correoNormalizado,
                        contrasena:
                        formulario.contrasena,
                    }),
                }
            );

            const cuerpo = await respuesta.json();

            if (!respuesta.ok) {
                setError(
                    cuerpo.mensaje ||
                    "No fue posible completar el registro."
                );
                return;
            }

            setExito(cuerpo);
            setFormulario(ESTADO_INICIAL);
        } catch {
            setError(
                "No se pudo conectar con el servicio de registro."
            );
        } finally {
            setEnviando(false);
        }
    }

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

                    <div>
                        <h1>
                            Crear cuenta - Sistema de importaciones
                        </h1>
                        <p className="descripcion-registro mb-0">
                            Registre su cuenta con el correo institucional. Después de validar el
                            correo, la jefatura de la Unidad le asignará el rol con el que trabajará.
                        </p>
                    </div>
                </header>

                {exito ? (
                    <div className="cuerpo-registro">

                        <div
                            className="alert alert-success"
                            role="status"
                        >
                            {exito.mensaje}
                        </div>

                        <div className="resumen-registro">
                            <p>
                                <strong>
                                    Correo institucional:
                                </strong>{" "}
                                {exito.correoInstitucional}
                            </p>

                            <p>
                                <strong>
                                    Estado de validación:
                                </strong>{" "}
                                {exito.estadoValidacion}
                            </p>

                            <p>
                                <strong>
                                    Estado de asignación de rol:
                                </strong>{" "}
                                {exito.estadoAsignacionRol}
                            </p>
                        </div>

                        {exito.enlaceVerificacion ? (
                            <p className="ayuda">
                                Enlace de prueba:{" "}
                                <a
                                    href={exito.enlaceVerificacion}
                                >
                                    validar correo institucional
                                </a>
                            </p>
                        ) : null}

                        <div className="acciones">
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={cancelarRegistro}
                            >
                                Registrar otra cuenta
                            </button>
                        </div>

                        <p className="nota text-center">
                            <Link to="/login">Volver a iniciar sesión</Link>
                        </p>

                    </div>
                ) : (
                    <form
                        className="cuerpo-registro"
                        onSubmit={registrarUsuario}
                    >

                        <p className="nota mb-0">
                            Los campos marcados con <span className="text-danger">*</span> son obligatorios.
                        </p>

                        <div className="row g-3">

                            <div className="col-md-4">
                                <label
                                    htmlFor="txtNombre"
                                    className="form-label sim-requerido"
                                >
                                    Nombre
                                </label>

                                <input
                                    id="txtNombre"
                                    type="text"
                                    className="form-control"
                                    name="nombre"
                                    value={formulario.nombre}
                                    onChange={actualizarCampo}
                                    placeholder="Katherine"
                                    required
                                />
                            </div>

                            <div className="col-md-4">
                                <label
                                    htmlFor="txtPrimerApellido"
                                    className="form-label sim-requerido"
                                >
                                    Primer apellido
                                </label>

                                <input
                                    id="txtPrimerApellido"
                                    type="text"
                                    className="form-control"
                                    name="primerApellido"
                                    value={
                                        formulario.primerApellido
                                    }
                                    onChange={actualizarCampo}
                                    placeholder="Valverde"
                                    required
                                />
                            </div>

                            <div className="col-md-4">
                                <label
                                    htmlFor="txtSegundoApellido"
                                    className="form-label"
                                >
                                    Segundo apellido
                                </label>

                                <input
                                    id="txtSegundoApellido"
                                    type="text"
                                    className="form-control"
                                    name="segundoApellido"
                                    value={
                                        formulario.segundoApellido
                                    }
                                    onChange={actualizarCampo}
                                    placeholder="Fallas"
                                />
                            </div>

                        </div>

                        <div className="row g-3 mt-1">

                            <div className="col-md-6">
                                <label
                                    htmlFor="txtNumeroCedula"
                                    className="form-label sim-requerido"
                                >
                                    Número de cédula
                                </label>

                                <input
                                    id="txtNumeroCedula"
                                    type="text"
                                    className="form-control"
                                    name="numeroCedula"
                                    value={
                                        formulario.numeroCedula
                                    }
                                    onChange={actualizarCampo}
                                    placeholder={EJEMPLOS_MASCARA.cedula}
                                    inputMode="numeric"
                                    aria-describedby="lblAyudaCedula"
                                    required
                                />

                                <p className="ayuda mt-2" id="lblAyudaCedula">
                                    Escriba los 9 dígitos; los guiones se colocan solos.
                                </p>
                            </div>

                            <div className="col-md-6">
                                <label
                                    htmlFor="txtFechaNacimiento"
                                    className="form-label sim-requerido"
                                >
                                    Fecha de nacimiento
                                </label>

                                <input
                                    id="txtFechaNacimiento"
                                    type="date"
                                    className="form-control"
                                    name="fechaNacimiento"
                                    value={
                                        formulario.fechaNacimiento
                                    }
                                    onChange={actualizarCampo}
                                    max={fechaMaximaNacimiento}
                                    aria-describedby="lblAyudaFechaNacimiento"
                                    required
                                />

                                <p className="ayuda mt-2" id="lblAyudaFechaNacimiento">
                                    Debe ser mayor de {EDAD_MINIMA} años.
                                </p>
                            </div>

                        </div>

                        <div className="mt-3">
                            <label
                                htmlFor="txtCorreoInstitucional"
                                className="form-label sim-requerido"
                            >
                                Correo institucional
                            </label>

                            <input
                                id="txtCorreoInstitucional"
                                type="email"
                                className="form-control"
                                name="correoInstitucional"
                                value={
                                    formulario.correoInstitucional
                                }
                                onChange={actualizarCampo}
                                placeholder="katherine.valverde@ucr.ac.cr"
                                required
                            />

                            <p className="ayuda mt-2">
                                Solo se aceptan correos del dominio institucional de la UCR.
                            </p>
                        </div>

                        <div className="mt-3">
                            <label
                                htmlFor="txtContrasena"
                                className="form-label sim-requerido"
                            >
                                Contraseña
                            </label>

                            <Cmp_SIM_CampoContrasena
                                id="txtContrasena"
                                name="contrasena"
                                value={
                                    formulario.contrasena
                                }
                                onChange={actualizarCampo}
                                autoComplete="new-password"
                                required
                            />
                        </div>

                        <div className="mt-3">
                            <label
                                htmlFor="txtConfirmacionContrasena"
                                className="form-label sim-requerido"
                            >
                                Confirmar contraseña
                            </label>

                            <Cmp_SIM_CampoContrasena
                                id="txtConfirmacionContrasena"
                                name="confirmacionContrasena"
                                value={
                                    formulario.confirmacionContrasena
                                }
                                onChange={actualizarCampo}
                                autoComplete="new-password"
                                required
                            />

                            {contrasenasDistintas ? (
                                <p className="ayuda mt-2 text-danger" role="status">
                                    Las contraseñas no coinciden.
                                </p>
                            ) : null}
                        </div>

                        <ul className="requisitos">
                            <li
                                className={
                                    requisitos.longitud
                                        ? "ok"
                                        : "pendiente"
                                }
                            >
                <span>
                  {requisitos.longitud
                      ? "✓"
                      : "×"}
                </span>{" "}
                                Mínimo 8 caracteres
                            </li>

                            <li
                                className={
                                    requisitos.mayuscula
                                        ? "ok"
                                        : "pendiente"
                                }
                            >
                <span>
                  {requisitos.mayuscula
                      ? "✓"
                      : "×"}
                </span>{" "}
                                Una letra mayúscula
                            </li>

                            <li
                                className={
                                    requisitos.numero
                                        ? "ok"
                                        : "pendiente"
                                }
                            >
                <span>
                  {requisitos.numero
                      ? "✓"
                      : "×"}
                </span>{" "}
                                Un número
                            </li>

                            <li
                                className={
                                    requisitos.especial
                                        ? "ok"
                                        : "pendiente"
                                }
                            >
                <span>
                  {requisitos.especial
                      ? "✓"
                      : "×"}
                </span>{" "}
                                Un carácter especial
                            </li>
                        </ul>

                        <p className="nota">
                            Al registrarte se enviará un correo de
                            validación. La cuenta quedará pendiente
                            de validación y de asignación de rol por
                            parte de la jefatura.
                        </p>

                        {error ? (
                            <div
                                className="alert alert-danger"
                                role="alert"
                            >
                                {error}
                            </div>
                        ) : null}

                        <div className="acciones">
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                onClick={cancelarRegistro}
                                disabled={enviando}
                            >
                                Cancelar
                            </button>

                            <button
                                type="submit"
                                className="btn btn-primary"
                                disabled={enviando}
                            >
                                {enviando
                                    ? "Registrando..."
                                    : "Registrarme"}
                            </button>
                        </div>

                        <p className="nota text-center">
                            ¿Ya tiene una cuenta?{" "}
                            <Link to="/login">Iniciar sesión</Link>
                        </p>

                    </form>
                )}

            </section>
        </main>
    );
}

export default Frm_SIM_RegistrarUsuario;