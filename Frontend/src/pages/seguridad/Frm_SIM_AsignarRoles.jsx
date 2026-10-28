import { useMemo, useState } from "react";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { RUTA_SEGURIDAD, solicitarSeguridad } from "../../servicios/Srv_SIM_Seguridad.js";
import "./Frm_SIM_Seguridad.css";
import "./Frm_SIM_AsignarRoles.css";
import { DOMINIO_CORREO_INSTITUCIONAL, usuarioDeCorreo } from "../../componentes/Cmp_SIM_Mascaras.js";

/**
 * Descripción: Asignación de roles a un usuario del sistema (HU-003). Busca la cuenta por correo
 *   institucional, muestra sus roles activos y los permisos efectivos de la combinación elegida,
 *   y registra los roles agregados o retirados con su motivo.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. La pantalla usa el marco común de main (Cmp_SIM_Marco)
 *     en lugar de Cmp_SIM_Encabezado y las llamadas envían el token de la sesión
 *     (Srv_SIM_Seguridad). Se quita el usuario asignador fijo: el servidor lo toma de la sesión.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: heurísticas de usabilidad del sprint anterior. H1 (EH2-01, EH10-01): cada
 *     permiso efectivo muestra su descripción al pasar el cursor. H2 (EH5-01): guardar sin
 *     ningún rol pide una confirmación que explica que la persona no podrá iniciar sesión. Se
 *     agrega la ayuda de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: comentarios de las pruebas de usabilidad. El correo usa una máscara: se escribe
 *     solo el usuario y la terminación @ucr.ac.cr queda fija (si se pega el correo completo, se
 *     quita). Se agrega la descripción breve de la pantalla.
 */

const AYUDA = {
    titulo: "Ayuda: Asignar roles",
    introduccion:
        "En esta pantalla se asignan o retiran los roles de una cuenta. Los permisos de la "
        + "persona son la suma de los permisos de sus roles.",
    tituloPasos: "Pasos a seguir",
    pasos: [
        "Escriba el usuario del correo institucional (la parte antes de @ucr.ac.cr) y presione \"Buscar\".",
        "Marque los roles que debe tener y desmarque los que se le retiran.",
        "Revise los \"Permisos efectivos\" que resultan de la combinación.",
        "Indique el motivo del cambio.",
        "Presione \"Guardar roles\".",
    ],
    dudas: [
        {
            pregunta: "¿Puedo asignarme roles a mí mismo?",
            respuesta: "No; otra persona debe hacerlo.",
        },
        {
            pregunta: "¿Por qué no aparece un rol?",
            respuesta: "Solo se muestran los roles activos.",
        },
        {
            pregunta: "¿Qué pasa si le quito todos los roles?",
            respuesta: "La persona no podrá iniciar sesión hasta que se le asigne uno.",
        },
        {
            pregunta: "¿Por qué no encuentra la cuenta?",
            respuesta: "Verifique el correo; la persona debe haberse registrado antes.",
        },
    ],
};

function Frm_SIM_AsignarRoles() {
    const [correoBusqueda, setCorreoBusqueda] = useState("");
    const [usuario, setUsuario] = useState(null);

    const [rolesDisponibles, setRolesDisponibles] = useState([]);
    const [rolesOriginales, setRolesOriginales] = useState([]);
    const [rolesSeleccionados, setRolesSeleccionados] = useState([]);

    const [motivo, setMotivo] = useState("");

    const [buscando, setBuscando] = useState(false);
    const [guardando, setGuardando] = useState(false);

    const [error, setError] = useState("");
    const [exito, setExito] = useState("");

    async function buscarUsuario(evento) {
        evento.preventDefault();

        setError("");
        setExito("");
        setUsuario(null);
        setRolesOriginales([]);
        setRolesSeleccionados([]);

        if (!correoBusqueda.trim()) {
            setError("Debe ingresar el correo institucional del usuario.");
            return;
        }

        setBuscando(true);

        try {
            const respuestaUsuario = await solicitarSeguridad(
                `${RUTA_SEGURIDAD}/usuarios/buscar?correo=${encodeURIComponent(
                    correoBusqueda.trim() + DOMINIO_CORREO_INSTITUCIONAL
                )}`
            );

            const cuerpoUsuario = await respuestaUsuario.json();

            if (!respuestaUsuario.ok) {
                throw new Error(
                    cuerpoUsuario.mensaje ||
                    "No fue posible localizar al usuario."
                );
            }

            const [
                respuestaRolesDisponibles,
                respuestaRolesUsuario,
            ] = await Promise.all([
                solicitarSeguridad(`${RUTA_SEGURIDAD}/roles`),
                solicitarSeguridad(
                    `${RUTA_SEGURIDAD}/usuarios/${cuerpoUsuario.idUsuario}/roles`
                ),
            ]);

            const cuerpoRolesDisponibles =
                await respuestaRolesDisponibles.json();

            const cuerpoRolesUsuario =
                await respuestaRolesUsuario.json();

            if (!respuestaRolesDisponibles.ok) {
                throw new Error(
                    cuerpoRolesDisponibles.mensaje ||
                    "No fue posible obtener los roles disponibles."
                );
            }

            if (!respuestaRolesUsuario.ok) {
                throw new Error(
                    cuerpoRolesUsuario.mensaje ||
                    "No fue posible obtener los roles del usuario."
                );
            }

            const rolesActivosDisponibles =
                cuerpoRolesDisponibles.filter(
                    (rol) => rol.estado === "ACT"
                );

            const idsRolesUsuario =
                cuerpoRolesUsuario.map(
                    (rol) => rol.idRol
                );

            setUsuario(cuerpoUsuario);

            setRolesDisponibles(
                rolesActivosDisponibles
            );

            setRolesOriginales(
                idsRolesUsuario
            );

            setRolesSeleccionados(
                idsRolesUsuario
            );
        } catch (errorPeticion) {
            setError(
                errorPeticion.message ||
                "No se pudo conectar con el servicio de seguridad."
            );
        } finally {
            setBuscando(false);
        }
    }

    function alternarRol(idRol) {
        setRolesSeleccionados((previos) => {
            const seleccionado =
                previos.includes(idRol);

            if (seleccionado) {
                return previos.filter(
                    (idActual) => idActual !== idRol
                );
            }

            return [...previos, idRol];
        });

        setError("");
        setExito("");
    }

    const cantidadRolesSeleccionados =
        rolesSeleccionados.length;

    const hayCambios =
        rolesSeleccionados.some(
            (idRol) =>
                !rolesOriginales.includes(idRol)
        ) ||
        rolesOriginales.some(
            (idRol) =>
                !rolesSeleccionados.includes(idRol)
        );

    const permisosEfectivos = useMemo(() => {
        const mapaPermisos = new Map();

        rolesDisponibles
            .filter((rol) =>
                rolesSeleccionados.includes(
                    rol.idRol
                )
            )
            .forEach((rol) => {
                rol.permisos.forEach((permiso) => {
                    mapaPermisos.set(
                        permiso.idPermiso,
                        permiso
                    );
                });
            });

        return Array.from(
            mapaPermisos.values()
        ).sort((permisoA, permisoB) =>
            permisoA.nombre.localeCompare(
                permisoB.nombre,
                "es"
            )
        );
    }, [
        rolesDisponibles,
        rolesSeleccionados,
    ]);

    async function guardarRoles() {
        if (!usuario) {
            setError(
                "Debe seleccionar un usuario antes de guardar."
            );
            return;
        }

        const rolesAgregar =
            rolesSeleccionados.filter(
                (idRol) =>
                    !rolesOriginales.includes(idRol)
            );

        const rolesEliminar =
            rolesOriginales.filter(
                (idRol) =>
                    !rolesSeleccionados.includes(
                        idRol
                    )
            );

        if (
            rolesAgregar.length === 0 &&
            rolesEliminar.length === 0
        ) {
            setError(
                "No existen cambios de roles para guardar."
            );
            return;
        }

        if (rolesSeleccionados.length === 0) {
            const confirmado = window.confirm(
                `${usuario.nombreCompleto} quedará sin roles activos y no podrá iniciar `
                + "sesión hasta que se le asigne otro rol.\n\n¿Desea guardar el cambio?"
            );

            if (!confirmado) {
                return;
            }
        }

        setGuardando(true);
        setError("");
        setExito("");

        try {
            if (rolesAgregar.length > 0) {
                await asignarRoles(
                    rolesAgregar
                );
            }

            for (const idRol of rolesEliminar) {
                await retirarRol(idRol);
            }

            setExito(
                "Los roles del usuario fueron actualizados correctamente."
            );

            await recargarRolesUsuario();
        } catch (errorPeticion) {
            setError(
                errorPeticion.message ||
                "No fue posible actualizar los roles del usuario."
            );
        } finally {
            setGuardando(false);
        }
    }

    async function asignarRoles(
        idsRoles
    ) {
        const respuesta = await solicitarSeguridad(
            `${RUTA_SEGURIDAD}/usuarios/roles`,
            {
                method: "POST",
                headers: {
                    "Content-Type":
                        "application/json",
                },
                body: JSON.stringify({
                    idUsuario: usuario.idUsuario,
                    idRoles: idsRoles,
                    motivo:
                        motivo.trim() || null,
                }),
            }
        );

        const cuerpo =
            await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(
                cuerpo.mensaje ||
                "No fue posible asignar los roles."
            );
        }
    }

    async function retirarRol(idRol) {
        const respuesta = await solicitarSeguridad(
            `${RUTA_SEGURIDAD}/usuarios/${usuario.idUsuario}/roles/${idRol}`,
            {
                method: "DELETE",
                headers: {
                    "Content-Type":
                        "application/json",
                },
                body: JSON.stringify({
                    motivo:
                        motivo.trim() || null,
                }),
            }
        );

        const cuerpo =
            await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(
                cuerpo.mensaje ||
                "No fue posible retirar uno de los roles."
            );
        }
    }

    async function recargarRolesUsuario() {
        const respuesta = await solicitarSeguridad(
            `${RUTA_SEGURIDAD}/usuarios/${usuario.idUsuario}/roles`
        );

        const cuerpo =
            await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(
                cuerpo.mensaje ||
                "No fue posible actualizar la información de roles."
            );
        }

        const idsRoles =
            cuerpo.map(
                (rol) => rol.idRol
            );

        setRolesOriginales(idsRoles);
        setRolesSeleccionados(idsRoles);
    }

    function cancelarCambios() {
        setRolesSeleccionados([
            ...rolesOriginales,
        ]);

        setMotivo("");
        setError("");
        setExito("");
    }

    function obtenerIniciales(
        nombreCompleto
    ) {
        return nombreCompleto
            .split(" ")
            .filter(Boolean)
            .slice(0, 2)
            .map((parte) =>
                parte.charAt(0).toUpperCase()
            )
            .join("");
    }

    return (
        <Cmp_SIM_Marco
            usuario={obtenerSesion()?.nombreCompleto || "Usuario de la sesión"}
            ayuda={AYUDA}
        >
            <div className="sim-seguridad pantalla-asignar-roles">
                <div className="contenedor-asignar-roles">

                    <div className="ruta-navegacion">
                        Usuarios / Asignar roles
                    </div>

                    <h1>
                        Asignar rol de usuario del sistema
                    </h1>
                    <p className="descripcion-pantalla">
                        Busque la cuenta por su correo institucional, marque los roles que debe tener
                        y guarde el cambio con su motivo. Los permisos de la persona son la suma de
                        los permisos de sus roles.
                    </p>

                    <section className="tarjeta-busqueda-usuario">
                        <form
                            className="formulario-busqueda"
                            onSubmit={buscarUsuario}
                        >
                            <div className="campo-busqueda">
                                <label
                                    htmlFor="txtCorreoUsuario"
                                    className="form-label"
                                >
                                    Correo institucional
                                </label>

                                <div className="input-group">
                                    <input
                                        id="txtCorreoUsuario"
                                        type="text"
                                        className="form-control"
                                        value={correoBusqueda}
                                        onChange={(evento) =>
                                            setCorreoBusqueda(
                                                usuarioDeCorreo(evento.target.value)
                                            )
                                        }
                                        placeholder="nombre.apellido"
                                        autoComplete="off"
                                        aria-describedby="lblDominioCorreo"
                                        required
                                    />
                                    <span className="input-group-text" id="lblDominioCorreo">
                                        {DOMINIO_CORREO_INSTITUCIONAL}
                                    </span>
                                </div>
                            </div>

                            <button
                                id="btnBuscarUsuario"
                                type="submit"
                                className="btn btn-primary"
                                disabled={buscando}
                            >
                                {buscando
                                    ? "Buscando..."
                                    : "Buscar"}
                            </button>
                        </form>
                    </section>

                    {usuario ? (
                        <>
                            <section className="tarjeta-usuario">
                                <div className="avatar-usuario-grande">
                                    {obtenerIniciales(
                                        usuario.nombreCompleto
                                    )}
                                </div>

                                <div className="informacion-usuario">
                                    <h2>
                                        {usuario.nombreCompleto}
                                    </h2>

                                    <p>
                                        {
                                            usuario.correoInstitucional
                                        }
                                    </p>
                                </div>

                                <span
                                    className={
                                        usuario.estado === "ACT"
                                            ? "estado-usuario estado-usuario-activo"
                                            : "estado-usuario estado-usuario-inactivo"
                                    }
                                >
                  {usuario.estado === "ACT"
                      ? "Activo"
                      : "Inactivo"}
                </span>
                            </section>

                            <section className="tarjeta-asignacion">
                                <div className="encabezado-seccion encabezado-roles">
                                    <div>
                                        <h2>Roles institucionales</h2>

                                        <p>
                                            Se pueden asignar varios roles simultáneamente.
                                        </p>
                                    </div>

                                    <span className="contador-roles">{cantidadRolesSeleccionados} seleccionados
                                    </span>
                                </div>

                                <div className="rejilla-roles-asignacion">
                                    {rolesDisponibles.map(
                                        (rol) => {
                                            const seleccionado =
                                                rolesSeleccionados.includes(
                                                    rol.idRol
                                                );

                                            const asignadoActualmente =
                                                rolesOriginales.includes(
                                                    rol.idRol
                                                );

                                            return (
                                                <label
                                                    key={rol.idRol}
                                                    className={
                                                        seleccionado
                                                            ? "opcion-rol seleccionado"
                                                            : "opcion-rol"
                                                    }
                                                >
                                                    <input
                                                        type="checkbox"
                                                        className="form-check-input"
                                                        checked={seleccionado}
                                                        onChange={() =>
                                                            alternarRol(rol.idRol)
                                                        }
                                                    />

                                                    <div className="contenido-opcion-rol">
                                                        <div className="titulo-opcion-rol">
                                                            <strong>
                                                                {rol.nombre}
                                                            </strong>

                                                            {asignadoActualmente ? (
                                                                <span className="etiqueta-asignado">Asignado actualmente
                                                                </span>
                                                            ) : null}
                                                        </div>

                                                        {rol.descripcion ? (
                                                            <small>
                                                                {rol.descripcion}
                                                            </small>
                                                        ) : null}
                                                    </div>
                                                </label>
                                            );
                                        }
                                    )}
                                </div>

                                <div className="campo-motivo">
                                    <label
                                        htmlFor="txtMotivoCambio"
                                        className="form-label"
                                    >
                                        Motivo del cambio
                                    </label>

                                    <textarea
                                        id="txtMotivoCambio"
                                        className="form-control"
                                        rows="3"
                                        maxLength={255}
                                        value={motivo}
                                        onChange={(evento) =>
                                            setMotivo(
                                                evento.target.value
                                            )
                                        }
                                        placeholder="Indique el motivo del cambio de roles"
                                    />
                                </div>

                                <div className="seccion-permisos-efectivos">
                                    <h3>
                                        Permisos efectivos con esta combinación
                                    </h3>

                                    {permisosEfectivos.length ===
                                    0 ? (
                                        <p className="texto-secundario">
                                            No existen permisos efectivos porque no hay roles seleccionados.
                                        </p>
                                    ) : (
                                        <div className="permisos-efectivos">
                                            {permisosEfectivos.map(
                                                (permiso) => (
                                                    <span
                                                        key={
                                                            permiso.idPermiso
                                                        }
                                                        className="etiqueta-permiso"
                                                        title={
                                                            permiso.descripcion ||
                                                            permiso.codigo
                                                        }
                                                    >
                            {
                                permiso.nombre
                            }
                          </span>
                                                )
                                            )}
                                        </div>
                                    )}
                                </div>

                                {error ? (
                                    <div
                                        className="alert alert-danger mt-3"
                                        role="alert"
                                    >
                                        {error}
                                    </div>
                                ) : null}

                                {exito ? (
                                    <div
                                        className="alert alert-success mt-3"
                                        role="status"
                                    >
                                        {exito}
                                    </div>
                                ) : null}

                                <div className="pie-asignacion">
                                    <p>
                                        El cambio queda registrado
                                        con fecha, hora, responsable
                                        y roles agregados o eliminados.
                                    </p>

                                    <div className="acciones-formulario">
                                        <button
                                            id="btnCancelarAsignacion"
                                            type="button"
                                            className="btn btn-outline-secondary"
                                            onClick={
                                                cancelarCambios
                                            }
                                        >
                                            Cancelar
                                        </button>

                                        <button
                                            id="btnGuardarAsignacion"
                                            type="button"
                                            className="btn btn-primary"
                                            onClick={guardarRoles}
                                            disabled={
                                                guardando || !hayCambios
                                            }
                                        >
                                            {guardando
                                                ? "Guardando..."
                                                : "Guardar roles"}
                                        </button>
                                    </div>
                                </div>
                            </section>
                            {hayCambios ? (
                                <p className="aviso-cambios">
                                    Hay cambios de roles pendientes de guardar.
                                </p>
                            ) : null}
                        </>
                    ) : null}

                    {!usuario && error ? (
                        <div
                            className="alert alert-danger mt-3"
                            role="alert"
                        >
                            {error}
                        </div>
                    ) : null}

                </div>
            </div>
        </Cmp_SIM_Marco>
    );
}

export default Frm_SIM_AsignarRoles;