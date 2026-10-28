import { useEffect, useMemo, useState } from "react";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import {
    Cmp_SIM_AvisoSoloConsulta,
    useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { MODULOS, PERMISOS } from "../../utilerias/Permisos.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { RUTA_SEGURIDAD, solicitarSeguridad } from "../../servicios/Srv_SIM_Seguridad.js";
import "./Frm_SIM_Seguridad.css";
import "./Frm_SIM_GestionarRoles.css";

/**
 * Descripción: Gestión de los roles del sistema (HU-003): registrar roles con sus permisos,
 *   modificarlos y activarlos o inactivarlos.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. La pantalla usa el marco común de main (Cmp_SIM_Marco)
 *     en lugar de Cmp_SIM_Encabezado y las llamadas envían el token de la sesión
 *     (Srv_SIM_Seguridad).
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: heurísticas de usabilidad del sprint anterior. H1 (EH2-01, EH10-01): cada
 *     permiso muestra su descripción en lugar del código. H2 (EH5-01): antes de inactivar un rol
 *     se advierte cuántas personas quedarían sin roles activos. H3 (EH9-01): al guardar, los
 *     campos obligatorios faltantes se marcan en rojo. Se agrega la ayuda de la pantalla.
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003).
 *     - Los permisos se agrupan por módulo de acceso (Gestión de importaciones, Gestión de
 *       exoneraciones...), con un botón para marcar o quitar todos los de un módulo. Un permiso de
 *       un módulo nuevo aparece en su grupo sin cambiar esta pantalla; uno sin módulo, en "Otros".
 *     - Registrar un rol exige ROL_CREAR; editarlo, activarlo o inactivarlo, ROL_MODIFICAR. Con
 *       solo ROL_CONSULTAR la pantalla es de consulta.
 *     - La ayuda explica los módulos, los roles base y cómo se combinan los permisos.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción de la pantalla.
 */

const AYUDA = {
    titulo: "Ayuda: Gestionar roles",
    introduccion:
        "En esta pantalla se definen los roles o cargos del sistema y los permisos de cada uno. "
        + "Los permisos están agrupados por módulo (Gestión de importaciones, Gestión de exoneraciones, "
        + "Reportes y consultas, etc.). Los roles se asignan después a las cuentas desde Asignar roles; "
        + "una persona con varios roles recibe la combinación de los permisos de todos ellos.",
    tituloPasos: "Pasos a seguir",
    pasos: [
        "Escriba el nombre del rol y, si desea, una descripción.",
        "Marque los permisos del rol en cada módulo; lea la descripción de cada uno para saber qué acceso otorga. Con \"Marcar todos\" se seleccionan todos los permisos de un módulo.",
        "Presione \"Guardar rol\".",
        "Para cambiar un rol, presione \"Editar\" en la lista, ajuste los datos y presione \"Guardar cambios\".",
        "Para dejar de usar un rol, presione \"Inactivar\".",
    ],
    dudas: [
        {
            pregunta: "¿Qué pasa al inactivar un rol?",
            respuesta:
                "Las personas que lo tengan dejan de recibir sus permisos. Si era su único rol, no "
                + "podrán iniciar sesión hasta que se les asigne otro.",
        },
        {
            pregunta: "¿Se puede borrar un rol?",
            respuesta:
                "No. Se inactiva y su historial se conserva; se puede volver a activar.",
        },
        {
            pregunta: "¿Por qué no se guarda el rol?",
            respuesta:
                "El nombre y al menos un permiso son obligatorios; los campos faltantes se marcan en rojo.",
        },
        {
            pregunta: "¿Qué son los roles como «Gestión de donaciones» o «Aprobaciones»?",
            respuesta:
                "Son los roles base del sistema: cada uno reúne los permisos de un módulo. Se pueden "
                + "asignar tal como están o usar de guía para crear roles propios con otra combinación.",
        },
        {
            pregunta: "¿Qué diferencia hay entre consultar, registrar y modificar?",
            respuesta:
                "Consultar permite ver las listas y los expedientes; registrar, crear trámites nuevos; y "
                + "modificar, cambiar sus datos, su estado o su documentación. Sin el permiso de "
                + "modificar, los expedientes se muestran en modo de consulta.",
        },
        {
            pregunta: "¿Por qué no puedo registrar o editar roles?",
            respuesta:
                "Registrar roles requiere el permiso «Crear roles», y editarlos, activarlos o "
                + "inactivarlos requiere «Modificar roles». Si solo tiene «Consultar roles», la "
                + "pantalla le muestra los roles sin poder cambiarlos.",
        },
    ],
};

const ERRORES_INICIALES = {
    nombre: "",
    permisos: "",
};

const ESTADO_INICIAL = {
    nombre: "",
    descripcion: "",
    idPermisos: [],
};

function Frm_SIM_GestionarRoles() {
    const [formulario, setFormulario] = useState(ESTADO_INICIAL);
    const [permisos, setPermisos] = useState([]);
    const [roles, setRoles] = useState([]);

    const [idRolEdicion, setIdRolEdicion] = useState(null);

    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);

    const [error, setError] = useState("");
    const [exito, setExito] = useState("");
    const [errores, setErrores] = useState(ERRORES_INICIALES);

    const estaEditando = idRolEdicion !== null;

    // Restricciones por rol (HU-003).
    const { tieneAlguno } = useSesionActual();
    const puedeCrear = tieneAlguno([PERMISOS.ROL_CREAR]);
    const puedeModificar = tieneAlguno([PERMISOS.ROL_MODIFICAR]);
    const mostrarFormulario = estaEditando ? puedeModificar : puedeCrear;

    // Permisos agrupados por módulo, en el orden de la HU-003; los que no tienen módulo van al final.
    const gruposPermisos = useMemo(() => {
        const nombres = new Map(MODULOS.map((modulo) => [modulo.codigo, modulo.nombre]));
        const orden = new Map(MODULOS.map((modulo, indice) => [modulo.codigo, indice]));
        const grupos = new Map();
        permisos.forEach((permiso) => {
            const codigo = permiso.modulo || "OTROS";
            if (!grupos.has(codigo)) {
                grupos.set(codigo, {
                    codigo,
                    nombre: permiso.nombreModulo || nombres.get(codigo) || "Otros permisos",
                    permisos: [],
                });
            }
            grupos.get(codigo).permisos.push(permiso);
        });
        return [...grupos.values()].sort((grupoA, grupoB) =>
            (orden.get(grupoA.codigo) ?? Number.MAX_SAFE_INTEGER)
            - (orden.get(grupoB.codigo) ?? Number.MAX_SAFE_INTEGER));
    }, [permisos]);

    useEffect(() => {
        cargarDatos();
    }, []);

    async function cargarDatos() {
        setCargando(true);
        setError("");

        try {
            const [respuestaPermisos, respuestaRoles] = await Promise.all([
                solicitarSeguridad(`${RUTA_SEGURIDAD}/permisos`),
                solicitarSeguridad(`${RUTA_SEGURIDAD}/roles`),
            ]);

            const cuerpoPermisos = await respuestaPermisos.json();
            const cuerpoRoles = await respuestaRoles.json();

            if (!respuestaPermisos.ok) {
                throw new Error(
                    cuerpoPermisos.mensaje ||
                    "No fue posible obtener los permisos."
                );
            }

            if (!respuestaRoles.ok) {
                throw new Error(
                    cuerpoRoles.mensaje ||
                    "No fue posible obtener los roles."
                );
            }

            setPermisos(cuerpoPermisos);
            setRoles(cuerpoRoles);
        } catch (errorPeticion) {
            setError(
                errorPeticion.message ||
                "No se pudo conectar con el servicio de seguridad."
            );
        } finally {
            setCargando(false);
        }
    }

    function actualizarCampo(evento) {
        const { name, value } = evento.target;

        setFormulario((previo) => ({
            ...previo,
            [name]: value,
        }));

        setErrores((previos) => ({
            ...previos,
            [name]: "",
        }));

        setError("");
        setExito("");
    }

    function alternarPermiso(idPermiso) {
        setFormulario((previo) => {
            const seleccionado =
                previo.idPermisos.includes(idPermiso);

            return {
                ...previo,
                idPermisos: seleccionado
                    ? previo.idPermisos.filter(
                        (idActual) => idActual !== idPermiso
                    )
                    : [...previo.idPermisos, idPermiso],
            };
        });

        setErrores((previos) => ({
            ...previos,
            permisos: "",
        }));

        setError("");
        setExito("");
    }

    /**
     * Marca todos los permisos de un módulo o, si ya estaban todos marcados, los quita.
     *
     * @param {number[]} idsModulo permisos del módulo
     */
    function alternarModulo(idsModulo) {
        setFormulario((previo) => {
            const todos = idsModulo.every((id) => previo.idPermisos.includes(id));
            return {
                ...previo,
                idPermisos: todos
                    ? previo.idPermisos.filter((id) => !idsModulo.includes(id))
                    : [...new Set([...previo.idPermisos, ...idsModulo])],
            };
        });

        setErrores((previos) => ({
            ...previos,
            permisos: "",
        }));

        setError("");
        setExito("");
    }

    function cancelarEdicion() {
        setFormulario(ESTADO_INICIAL);
        setIdRolEdicion(null);
        setErrores(ERRORES_INICIALES);
        setError("");
        setExito("");
    }

    function editarRol(rol) {
        setIdRolEdicion(rol.idRol);

        setFormulario({
            nombre: rol.nombre,
            descripcion: rol.descripcion || "",
            idPermisos: rol.permisos.map(
                (permiso) => permiso.idPermiso
            ),
        });

        setErrores(ERRORES_INICIALES);
        setError("");
        setExito("");

        window.scrollTo({
            top: 0,
            behavior: "smooth",
        });
    }

    async function guardarRol(evento) {
        evento.preventDefault();

        setError("");
        setExito("");

        const erroresFormulario = {
            nombre: formulario.nombre.trim()
                ? ""
                : "El nombre del rol es obligatorio.",
            permisos: formulario.idPermisos.length > 0
                ? ""
                : "Debe seleccionar al menos un permiso para el rol.",
        };

        setErrores(erroresFormulario);

        if (erroresFormulario.nombre || erroresFormulario.permisos) {
            setError(
                "Complete los campos obligatorios marcados en rojo."
            );
            return;
        }

        setGuardando(true);

        try {
            if (estaEditando) {
                await actualizarRol();
                setExito("El rol fue actualizado correctamente.");
            } else {
                await crearRol();
                setExito("El rol fue registrado correctamente.");
            }

            setFormulario(ESTADO_INICIAL);
            setIdRolEdicion(null);

            await cargarDatos();
        } catch (errorPeticion) {
            setError(
                errorPeticion.message ||
                "No fue posible guardar el rol."
            );
        } finally {
            setGuardando(false);
        }
    }

    async function crearRol() {
        const respuesta = await solicitarSeguridad(
            `${RUTA_SEGURIDAD}/roles`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    nombre: formulario.nombre,
                    descripcion: formulario.descripcion,
                    idPermisos: formulario.idPermisos,
                }),
            }
        );

        const cuerpo = await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(
                cuerpo.mensaje ||
                "No fue posible registrar el rol."
            );
        }
    }

    async function actualizarRol() {
        const rolOriginal = roles.find(
            (rol) => rol.idRol === idRolEdicion
        );

        if (!rolOriginal) {
            throw new Error(
                "No fue posible localizar el rol seleccionado."
            );
        }

        const respuesta = await solicitarSeguridad(
            `${RUTA_SEGURIDAD}/roles/${idRolEdicion}`,
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    nombre: formulario.nombre,
                    descripcion: formulario.descripcion,
                    idPermisos: formulario.idPermisos,
                }),
            }
        );

        const cuerpo = await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(
                cuerpo.mensaje ||
                "No fue posible actualizar el rol."
            );
        }

        const permisosOriginales =
            rolOriginal.permisos.map(
                (permiso) => permiso.idPermiso
            );

        const permisosAgregar =
            formulario.idPermisos.filter(
                (idPermiso) =>
                    !permisosOriginales.includes(idPermiso)
            );

        const permisosEliminar =
            permisosOriginales.filter(
                (idPermiso) =>
                    !formulario.idPermisos.includes(idPermiso)
            );

        for (const idPermiso of permisosAgregar) {
            await modificarPermisoRol(
                idPermiso,
                "POST"
            );
        }

        for (const idPermiso of permisosEliminar) {
            await modificarPermisoRol(
                idPermiso,
                "DELETE"
            );
        }
    }

    async function modificarPermisoRol(
        idPermiso,
        metodo
    ) {
        const respuesta = await solicitarSeguridad(
            `${RUTA_SEGURIDAD}/roles/${idRolEdicion}/permisos/${idPermiso}`,
            {
                method: metodo,
            }
        );

        const cuerpo = await respuesta.json();

        if (!respuesta.ok) {
            throw new Error(
                cuerpo.mensaje ||
                "No fue posible modificar los permisos del rol."
            );
        }
    }

    async function cambiarEstadoRol(rol) {
        const nuevoEstado =
            rol.estado === "ACT" ? "INA" : "ACT";

        const accion =
            nuevoEstado === "ACT"
                ? "activar"
                : "inactivar";

        setError("");
        setExito("");

        try {
            let advertencia = "";

            if (nuevoEstado === "INA") {
                const respuestaImpacto = await solicitarSeguridad(
                    `${RUTA_SEGURIDAD}/roles/${rol.idRol}/impacto-inactivacion`
                );

                const impacto = await respuestaImpacto.json();

                if (!respuestaImpacto.ok) {
                    throw new Error(
                        impacto.mensaje ||
                        "No fue posible revisar a quién afecta el rol."
                    );
                }

                if (impacto.usuariosSinRolesActivos > 0) {
                    advertencia = impacto.usuariosSinRolesActivos === 1
                        ? "\n\n1 persona quedará sin roles activos y no podrá iniciar sesión "
                        + "hasta que se le asigne otro rol."
                        : `\n\n${impacto.usuariosSinRolesActivos} personas quedarán sin roles `
                        + "activos y no podrán iniciar sesión hasta que se les asigne otro rol.";
                } else if (impacto.usuariosConRol > 0) {
                    advertencia = impacto.usuariosConRol === 1
                        ? "\n\n1 persona tiene este rol y dejará de recibir sus permisos."
                        : `\n\n${impacto.usuariosConRol} personas tienen este rol y dejarán `
                        + "de recibir sus permisos.";
                }
            }

            const confirmado = window.confirm(
                `¿Desea ${accion} el rol "${rol.nombre}"?${advertencia}`
            );

            if (!confirmado) {
                return;
            }

            const respuesta = await solicitarSeguridad(
                `${RUTA_SEGURIDAD}/roles/${rol.idRol}/estado`,
                {
                    method: "PATCH",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        estado: nuevoEstado,
                    }),
                }
            );

            const cuerpo = await respuesta.json();

            if (!respuesta.ok) {
                throw new Error(
                    cuerpo.mensaje ||
                    "No fue posible modificar el estado del rol."
                );
            }

            setExito(
                nuevoEstado === "ACT"
                    ? "El rol fue activado correctamente."
                    : "El rol fue inactivado correctamente."
            );

            await cargarDatos();
        } catch (errorPeticion) {
            setError(
                errorPeticion.message ||
                "No fue posible modificar el estado del rol."
            );
        }
    }

    const cantidadPermisos =
        formulario.idPermisos.length;

    const rolesOrdenados = useMemo(() => {
        return [...roles].sort((rolA, rolB) =>
            rolA.nombre.localeCompare(
                rolB.nombre,
                "es"
            )
        );
    }, [roles]);

    return (
        <Cmp_SIM_Marco
            usuario={obtenerSesion()?.nombreCompleto || "Usuario de la sesión"}
            ayuda={AYUDA}
        >
            <div className="sim-seguridad pantalla-gestion-roles">
                <div className="contenedor-gestion-roles">

                    <div className="ruta-navegacion">
                        Usuarios / Gestión de roles
                    </div>

                    <h1>Gestionar roles del sistema</h1>
                    <p className="descripcion-pantalla">
                        Registre los roles del sistema, defina qué permisos incluye cada uno y
                        actívelos o inactívelos. Los roles se asignan a las personas desde
                        «Asignar roles».
                    </p>

                    {!mostrarFormulario && (
                        <Cmp_SIM_AvisoSoloConsulta
                            texto={puedeModificar
                                ? "Sus roles no le permiten registrar roles nuevos. Para modificar un "
                                    + "rol existente, presione \"Editar\" en la lista."
                                : "Usted puede consultar los roles registrados y sus permisos, pero sus "
                                    + "roles no le permiten registrarlos ni modificarlos."}
                        />
                    )}

                {mostrarFormulario && (
                  <section className="tarjeta-rol">
                      <div className="encabezado-seccion">
                          <div>
                              <h2>
                                  {estaEditando
                                      ? "Modificar rol"
                                      : "Registrar rol"}
                              </h2>

                              <p>
                                  Defina el cargo institucional y
                                  seleccione los permisos que tendrá
                                  asociados.
                              </p>
                          </div>

                          {estaEditando ? (
                              <span className="etiqueta-edicion">
                  Editando
                </span>
                          ) : null}
                      </div>

                      <form onSubmit={guardarRol} noValidate>
                          <div className="row g-3">
                              <div className="col-md-6">
                                  <label
                                      htmlFor="txtNombreRol"
                                      className="form-label"
                                  >
                                      Nombre del rol o cargo
                                  </label>

                                  <input
                                      id="txtNombreRol"
                                      name="nombre"
                                      className={
                                          errores.nombre
                                              ? "form-control is-invalid"
                                              : "form-control"
                                      }
                                      value={formulario.nombre}
                                      onChange={actualizarCampo}
                                      maxLength={50}
                                      required
                                      aria-invalid={Boolean(errores.nombre)}
                                      aria-describedby={
                                          errores.nombre
                                              ? "errNombreRol"
                                              : undefined
                                      }
                                  />

                                  {errores.nombre ? (
                                      <div
                                          id="errNombreRol"
                                          className="invalid-feedback"
                                      >
                                          {errores.nombre}
                                      </div>
                                  ) : null}
                              </div>

                              <div className="col-md-6">
                                  <label
                                      htmlFor="txtDescripcionRol"
                                      className="form-label"
                                  >
                                      Descripción
                                  </label>

                                  <input
                                      id="txtDescripcionRol"
                                      name="descripcion"
                                      className="form-control"
                                      value={formulario.descripcion}
                                      onChange={actualizarCampo}
                                      maxLength={255}
                                  />
                              </div>
                          </div>

                          <div
                              className={
                                  errores.permisos
                                      ? "seccion-permisos con-error"
                                      : "seccion-permisos"
                              }
                          >
                              <div className="titulo-permisos">
                                  <div>
                                      <h3>Permisos del rol</h3>
                                      <span>
                      Se puede seleccionar más de uno
                    </span>
                                  </div>

                                  <span className="contador-permisos">
                    {cantidadPermisos} seleccionados
                  </span>
                              </div>

                              {errores.permisos ? (
                                  <p
                                      className="mensaje-error-permisos"
                                      role="alert"
                                  >
                                      {errores.permisos}
                                  </p>
                              ) : null}

                              {cargando ? (
                                  <p className="texto-secundario">
                                      Cargando permisos...
                                  </p>
                              ) : (
                                  <div className="grupos-permisos">
                                      {gruposPermisos.map((grupo) => {
                                          const idsModulo = grupo.permisos.map((permiso) => permiso.idPermiso);
                                          const marcados = idsModulo.filter((id) =>
                                              formulario.idPermisos.includes(id)).length;
                                          const todosMarcados = marcados === idsModulo.length;

                                          return (
                                              <fieldset
                                                  key={grupo.codigo}
                                                  className="grupo-permisos-modulo"
                                              >
                                                  <legend className="encabezado-grupo-permisos">
                                                      <span className="nombre-grupo-permisos">
                                                          {grupo.nombre}
                                                      </span>
                                                      <span className="contador-grupo-permisos">
                                                          {marcados}/{idsModulo.length}
                                                      </span>
                                                      <button
                                                          type="button"
                                                          className="btn btn-link btn-sm p-0 ms-auto"
                                                          onClick={() => alternarModulo(idsModulo)}
                                                          aria-label={`${todosMarcados ? "Quitar" : "Marcar"} todos los permisos de ${grupo.nombre}`}
                                                      >
                                                          {todosMarcados ? "Quitar todos" : "Marcar todos"}
                                                      </button>
                                                  </legend>

                                                  <div className="rejilla-permisos">
                                                    {grupo.permisos.map((permiso) => {
                                                      const seleccionado =
                                                          formulario.idPermisos.includes(
                                                              permiso.idPermiso
                                                          );

                                                      return (
                                                          <label
                                                              key={permiso.idPermiso}
                                                              className={
                                                                  seleccionado
                                                                      ? "opcion-permiso seleccionado"
                                                                      : "opcion-permiso"
                                                              }
                                                          >
                                                              <input
                                                                  type="checkbox"
                                                                  className="form-check-input"
                                                                  checked={seleccionado}
                                                                  onChange={() =>
                                                                      alternarPermiso(
                                                                          permiso.idPermiso
                                                                      )
                                                                  }
                                                              />

                                                              <div>
                                                                  <strong>
                                                                      {permiso.nombre}
                                                                  </strong>

                                                                  <small>
                                                                      {permiso.descripcion ||
                                                                          permiso.codigo}
                                                                  </small>
                                                              </div>
                                                          </label>
                                                      );
                                                    })}
                                                  </div>
                                              </fieldset>
                                          );
                                      })}
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

                          <div className="acciones-formulario">
                              <button
                                  id="btnCancelarRol"
                                  type="button"
                                  className="btn btn-outline-secondary"
                                  onClick={cancelarEdicion}
                              >
                                  Cancelar
                              </button>

                              <button
                                  id="btnGuardarRol"
                                  type="submit"
                                  className="btn btn-primary"
                                  disabled={
                                      guardando || cargando
                                  }
                              >
                                  {guardando
                                      ? "Guardando..."
                                      : estaEditando
                                          ? "Guardar cambios"
                                          : "Guardar rol"}
                              </button>
                          </div>
                      </form>
                  </section>
                )}

                <section className="tarjeta-listado-roles">
                    <div className="encabezado-seccion">
                        <div>
                            <h2>Roles registrados</h2>
                            <p>
                                Consulte los cargos institucionales,
                                sus permisos y su estado.
                            </p>
                        </div>
                    </div>

                    {cargando ? (
                        <p className="texto-secundario">
                            Cargando roles...
                        </p>
                    ) : rolesOrdenados.length === 0 ? (
                        <p className="texto-secundario">
                            No existen roles registrados.
                        </p>
                    ) : (
                        <div className="lista-roles">
                            {rolesOrdenados.map((rol) => (
                                <article
                                    key={rol.idRol}
                                    className="fila-rol"
                                >
                                    <div className="informacion-rol">
                                        <div className="cabecera-fila-rol">
                                            <h3>{rol.nombre}</h3>

                                            <span
                                                className={
                                                    rol.estado === "ACT"
                                                        ? "estado-rol estado-activo"
                                                        : "estado-rol estado-inactivo"
                                                }
                                            >
                        {rol.estado === "ACT"
                            ? "Activo"
                            : "Inactivo"}
                      </span>
                                        </div>

                                        <p>
                                            {rol.descripcion ||
                                                "Sin descripción registrada."}
                                        </p>

                                        <div className="permisos-resumen">
                                            {rol.permisos.map(
                                                (permiso) => (
                                                    <span
                                                        key={permiso.idPermiso}
                                                        className="etiqueta-permiso"
                                                        title={
                                                            permiso.descripcion ||
                                                            permiso.codigo
                                                        }
                                                    >
                            {permiso.nombre}
                          </span>
                                                )
                                            )}
                                        </div>
                                    </div>

                                    {puedeModificar && (
                                      <div className="acciones-rol">
                                          <button
                                              type="button"
                                              className="btn btn-outline-primary btn-sm"
                                              onClick={() =>
                                                  editarRol(rol)
                                              }
                                          >
                                              Editar
                                          </button>

                                          <button
                                              type="button"
                                              className={
                                                  rol.estado === "ACT"
                                                      ? "btn btn-outline-danger btn-sm"
                                                      : "btn btn-outline-success btn-sm"
                                              }
                                              onClick={() =>
                                                  cambiarEstadoRol(rol)
                                              }
                                          >
                                              {rol.estado === "ACT"
                                                  ? "Inactivar"
                                                  : "Activar"}
                                          </button>
                                      </div>
                                    )}
                                </article>
                            ))}
                        </div>
                    )}
                </section>
                </div>
            </div>
        </Cmp_SIM_Marco>
    );
}

export default Frm_SIM_GestionarRoles;