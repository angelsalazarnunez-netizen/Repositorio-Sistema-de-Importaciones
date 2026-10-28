import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso, {
  Cmp_SIM_AvisoSoloConsulta,
  useSesionActual,
} from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";

import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";

import {
    obtenerExoneracion,
    obtenerHistorialRevisionDocumento,
    obtenerRevisionDocumental,
    registrarRevisionDocumento,
} from "../../servicios/Srv_SIM_Tramites.js";

import "./Frm_SIM_RevisarDocumentacionExoneracion.css";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Revisión documental de un expediente de exoneración (HU-019).
 * Permite consultar el checklist documental del expediente, registrar el estado
 * de revisión de cada documento y consultar el historial de revisiones.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003). Sin EXONERACION_MODIFICAR la revisión es de
 *     consulta: el estado y la observación de cada documento se muestran deshabilitados, sin el botón
 *     Guardar; el historial de cada documento sigue disponible.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: hallazgos de la evaluación heurística del Sprint 2 y de las pruebas con usuarios.
 *     - H-01: los estados se muestran con su nombre normal (Recibido), no en mayúsculas.
 *     - H-02: «Guardar revisiones» registra en una sola acción todos los documentos modificados.
 *     - H-03: el mensaje de error aparece dentro de la fila del documento que lo causó, y el aviso
 *       general lleva la vista hasta él.
 *     - PU-10: resumen «X de Y documentos obligatorios recibidos» con la lista de los pendientes.
 */

const USUARIO_ACTUAL = "Técnica de Exoneraciones";
const LONGITUD_MAXIMA_OBSERVACION = 1000;

const AYUDA = {
    titulo: "Ayuda: revisión documental",
    introduccion:
        "Esta pantalla permite revisar los documentos asociados al expediente "
        + "de exoneración y registrar el resultado de la revisión de cada uno.",
    pasos: [
        "Revise los documentos requeridos para el expediente.",
        "Los documentos que todavía no han sido adjuntados se muestran como pendientes.",
        "Seleccione Pendiente, Recibido o Rechazado para cada documento disponible.",
        "Si rechaza un documento, indique obligatoriamente la razón.",
        "Use Historial para consultar las revisiones anteriores del documento.",
    ],
    dudas: [
        {
            pregunta: "¿Por qué algunos documentos no se pueden revisar?",
            respuesta:
                "Porque todavía no han sido adjuntados al expediente. Permanecen como pendientes "
                + "hasta que exista un documento disponible.",
        },
        {
            pregunta: "¿Qué sucede si cambio la revisión de un documento?",
            respuesta:
                "La revisión anterior se conserva en el historial y la nueva pasa a ser la revisión vigente.",
        },
        {
            pregunta: "¿La observación es obligatoria?",
            respuesta:
                "Solo es obligatoria cuando el documento se marca como Rechazado.",
        },
    ],
};

/**
 * Convierte una fecha recibida del servidor a una representación
 * adecuada para Costa Rica.
 */
function formatearFechaHora(valor) {
    return valor
        ? new Date(valor).toLocaleString("es-CR", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        })
        : "—";
}

/**
 * Acepta únicamente identificadores enteros positivos.
 */
function normalizarIdTramite(valor) {
    const numero = Number(valor);

    return Number.isInteger(numero) && numero > 0
        ? numero
        : null;
}

/**
 * Agrega a los documentos los campos locales utilizados para editar
 * el estado y la observación antes de enviarlos al servidor.
 */
function prepararDocumentos(respuesta) {
    return (respuesta ?? []).map((documento) => ({
        ...documento,
        estadoSeleccionado:
            documento.estadoRevision ?? "PENDIENTE",
        observacionEdicion:
            documento.observacion ?? "",
    }));
}

/** Nombres de los estados de revisión, como se leen en el resto de las pantallas (H-01). */
const NOMBRES_ESTADO_REVISION = {
    PENDIENTE: "Pendiente",
    RECIBIDO: "Recibido",
    RECHAZADO: "Rechazado",
};

export function nombreEstadoRevision(estado) {
    return NOMBRES_ESTADO_REVISION[estado] ?? estado ?? "Pendiente";
}

/** Indica si el documento tiene cambios sin guardar respecto de su revisión vigente. */
function tieneCambios(documento) {
    return Boolean(documento.idDocumento)
        && ((documento.estadoRevision ?? "PENDIENTE") !== documento.estadoSeleccionado
            || (documento.observacion ?? "") !== documento.observacionEdicion);
}

/**
 * Devuelve la clase Bootstrap correspondiente al estado de revisión.
 */
function claseEstado(estado) {
    switch (estado) {
        case "RECIBIDO":
            return "text-bg-success";

        case "RECHAZADO":
            return "text-bg-danger";

        default:
            return "text-bg-warning";
    }
}

function Frm_SIM_RevisarDocumentacionExoneracion() {
    // Restricciones por rol (HU-003): sin el permiso de modificar, la pantalla es de consulta.
    const puedeModificar = useSesionActual().tieneAlguno([PERMISOS.EXONERACION_MODIFICAR]);
    const { referenciaInterna } = useParams();
    const { state: estadoNavegacion } = useLocation();

    const idTramiteNavegacion =
        normalizarIdTramite(
            estadoNavegacion?.idTramite
        );

    const [idTramite, setIdTramite] =
        useState(null);

    const [estadoExpediente, setEstadoExpediente] =
        useState(
            estadoNavegacion?.estado ?? null
        );

    const [documentos, setDocumentos] =
        useState([]);

    const [cargando, setCargando] =
        useState(true);

    const [errorCarga, setErrorCarga] =
        useState(null);

    const [intentoCarga, setIntentoCarga] =
        useState(0);

    const [alerta, setAlerta] =
        useState(null);

    const refAviso = useVistaAlAviso(alerta);

    // H-03: errores de validación de cada documento, que se muestran en su propia fila.
    const [erroresFila, setErroresFila] =
        useState({});

    const [guardandoTodos, setGuardandoTodos] =
        useState(false);

    const [guardandoId, setGuardandoId] =
        useState(null);

    const [
        documentoHistorial,
        setDocumentoHistorial,
    ] = useState(null);

    const [historial, setHistorial] =
        useState([]);

    const [
        cargandoHistorial,
        setCargandoHistorial,
    ] = useState(false);

    const responsable =
        obtenerSesion()?.nombreCompleto
        || USUARIO_ACTUAL;

    /*
     * Obtiene el idTramite desde el estado de navegación.
     * Si la página fue recargada o se accedió directamente,
     * recupera el expediente mediante la referencia interna.
     */
    useEffect(() => {
        let vigente = true;

        async function cargar() {
            setCargando(true);
            setErrorCarga(null);

            try {
                let id = idTramiteNavegacion;

                if (id === null) {
                    const detalle =
                        await obtenerExoneracion(
                            referenciaInterna
                        );

                    id = normalizarIdTramite(
                        detalle?.idTramite
                    );

                    if (vigente) {
                        setEstadoExpediente(
                            detalle?.estado ?? null
                        );
                    }

                    if (id === null) {
                        throw new Error(
                            "El expediente no indica su identificador."
                        );
                    }
                }

                const respuesta =
                    await obtenerRevisionDocumental(id);

                if (vigente) {
                    setIdTramite(id);
                    setDocumentos(
                        prepararDocumentos(respuesta)
                    );
                }
            } catch (problema) {
                if (vigente) {
                    setErrorCarga(
                        "No fue posible cargar la revisión documental. "
                        + problema.message
                    );
                }
            } finally {
                if (vigente) {
                    setCargando(false);
                }
            }
        }

        cargar();

        return () => {
            vigente = false;
        };
    }, [
        referenciaInterna,
        idTramiteNavegacion,
        intentoCarga,
    ]);

    /**
     * Vuelve a consultar el checklist después de registrar una revisión.
     */
    async function refrescarChecklist() {
        try {
            const respuesta =
                await obtenerRevisionDocumental(
                    idTramite
                );

            setDocumentos(
                prepararDocumentos(respuesta)
            );
        } catch (problema) {
            setAlerta({
                tipo: "warning",
                texto:
                    "La revisión se registró, pero no fue posible actualizar el checklist. "
                    + `Recargue la página. ${problema.message}`,
            });
        }
    }

    /**
     * Actualiza localmente el estado seleccionado de un documento.
     */
    function cambiarEstado(
        idTipoDocumento,
        nuevoEstado
    ) {
        setDocumentos((actuales) =>
            actuales.map((documento) =>
                documento.idTipoDocumento
                === idTipoDocumento
                    ? {
                        ...documento,
                        estadoSeleccionado:
                        nuevoEstado,
                    }
                    : documento
            )
        );
    }

    /**
     * Actualiza localmente la observación de un documento.
     */
    function cambiarObservacion(
        idTipoDocumento,
        nuevaObservacion
    ) {
        setDocumentos((actuales) =>
            actuales.map((documento) =>
                documento.idTipoDocumento
                === idTipoDocumento
                    ? {
                        ...documento,
                        observacionEdicion:
                        nuevaObservacion,
                    }
                    : documento
            )
        );
    }

    /**
     * Valida la revisión de un documento. Devuelve el mensaje del problema, o null si es válida.
     */
    function validarRevision(documento) {
        if (!documento.idDocumento) {
            return "El documento todavía no ha sido adjuntado al expediente.";
        }
        const observacion = documento.observacionEdicion.trim();
        if (documento.estadoSeleccionado === "RECHAZADO" && observacion.length === 0) {
            return "Indique la razón por la cual se rechaza el documento.";
        }
        if (observacion.length > LONGITUD_MAXIMA_OBSERVACION) {
            return `La observación no puede superar ${LONGITUD_MAXIMA_OBSERVACION} caracteres.`;
        }
        return null;
    }

    function marcarErrorFila(idTipoDocumento, mensaje) {
        setErroresFila((anteriores) => ({ ...anteriores, [idTipoDocumento]: mensaje }));
    }

    /**
     * H-02: registra en una sola acción las revisiones de todos los documentos modificados. Si
     * alguno no es válido, se marca en su fila y no se envía ninguno.
     */
    async function guardarTodas() {
        setAlerta(null);
        const modificados = documentos.filter(tieneCambios);
        const errores = {};
        modificados.forEach((documento) => {
            const problema = validarRevision(documento);
            if (problema) {
                errores[documento.idTipoDocumento] = problema;
            }
        });
        setErroresFila(errores);
        if (Object.keys(errores).length > 0) {
            setAlerta({
                tipo: "warning",
                texto: "Revise los documentos marcados en la tabla: hay datos por corregir antes de guardar.",
            });
            return;
        }

        setGuardandoTodos(true);
        let guardados = 0;
        try {
            for (const documento of modificados) {
                const observacion = documento.observacionEdicion.trim();
                try {
                    await registrarRevisionDocumento(idTramite, documento.idDocumento, {
                        estadoRevision: documento.estadoSeleccionado,
                        observacion: observacion.length > 0 ? observacion : null,
                    });
                    guardados += 1;
                } catch (problema) {
                    marcarErrorFila(documento.idTipoDocumento,
                        `No fue posible registrar la revisión. ${problema.message}`);
                }
            }
            setAlerta(guardados === modificados.length
                ? {
                    tipo: "success",
                    texto: guardados === 1
                        ? "Se registró la revisión de 1 documento."
                        : `Se registraron las revisiones de ${guardados} documentos.`,
                }
                : {
                    tipo: "danger",
                    texto: `Se registraron ${guardados} de ${modificados.length} revisiones. `
                        + "Las que no se guardaron indican el motivo en su fila.",
                });
            await refrescarChecklist();
        } finally {
            setGuardandoTodos(false);
        }
    }

    /**
     * Valida y registra una revisión documental.
     */
    async function guardarRevision(documento) {
        setAlerta(null);
        marcarErrorFila(documento.idTipoDocumento, null);

        const problemaFila = validarRevision(documento);
        if (problemaFila) {
            marcarErrorFila(documento.idTipoDocumento, problemaFila);
            return;
        }

        if (!documento.idDocumento) {
            setAlerta({
                tipo: "warning",
                texto:
                    "El documento todavía no ha sido adjuntado al expediente.",
            });

            return;
        }

        const observacion =
            documento.observacionEdicion.trim();

        if (
            documento.estadoSeleccionado
            === "RECHAZADO"
            && observacion.length === 0
        ) {
            setAlerta({
                tipo: "warning",
                texto:
                    "Debe indicar la razón por la cual se rechaza el documento.",
            });

            return;
        }

        if (
            observacion.length
            > LONGITUD_MAXIMA_OBSERVACION
        ) {
            setAlerta({
                tipo: "warning",
                texto:
                    `La observación no puede superar `
                    + `${LONGITUD_MAXIMA_OBSERVACION} caracteres.`,
            });

            return;
        }

        setGuardandoId(
            documento.idDocumento
        );

        try {
            await registrarRevisionDocumento(
                idTramite,
                documento.idDocumento,
                {
                    estadoRevision:
                    documento.estadoSeleccionado,

                    observacion:
                        observacion.length > 0
                            ? observacion
                            : null,
                }
            );

            setAlerta({
                tipo: "success",
                texto:
                    "La revisión documental se registró correctamente.",
            });

            await refrescarChecklist();
        } catch (problema) {
            marcarErrorFila(documento.idTipoDocumento,
                "No fue posible registrar la revisión. " + problema.message);
        } finally {
            setGuardandoId(null);
        }
    }

    /**
     * Consulta el historial de revisiones de un documento.
     */
    async function consultarHistorial(
        documento
    ) {
        if (!documento.idDocumento) {
            return;
        }

        setAlerta(null);

        setDocumentoHistorial(
            documento
        );

        setHistorial([]);
        setCargandoHistorial(true);

        try {
            const respuesta =
                await obtenerHistorialRevisionDocumento(
                    idTramite,
                    documento.idDocumento
                );

            setHistorial(
                respuesta ?? []
            );
        } catch (problema) {
            setAlerta({
                tipo: "danger",
                texto:
                    "No fue posible consultar el historial. "
                    + problema.message,
            });
        } finally {
            setCargandoHistorial(false);
        }
    }

    /**
     * Cierra la sección de historial actualmente visible.
     */
    function cerrarHistorial() {
        setDocumentoHistorial(null);
        setHistorial([]);
    }

    const obligatorios = documentos.filter((documento) => documento.obligatorio);
    const obligatoriosPendientes = obligatorios.filter(
        (documento) => documento.estadoRevision !== "RECIBIDO");
    const cantidadModificados = documentos.filter(tieneCambios).length;

    const rutaExpediente =
        `/tramites/exoneraciones/`
        + encodeURIComponent(
            referenciaInterna
        );

    const migas = (
        <nav aria-label="Ruta de navegación">
            <ol className="breadcrumb small">

                <li className="breadcrumb-item">
                    <Cmp_SIM_ConPermiso
                        permisos={[PERMISOS.EXONERACION_CONSULTAR]}
                        alterno="Exoneraciones"
                    >
                        <Link to="/tramites/exoneraciones">
                            Exoneraciones
                        </Link>
                    </Cmp_SIM_ConPermiso>
                </li>

                <li className="breadcrumb-item">
                    <Link
                        to={rutaExpediente}
                        state={{
                            idTramite,
                            estado: estadoExpediente,
                        }}
                    >
                        {referenciaInterna}
                    </Link>
                </li>

                <li
                    className="breadcrumb-item active"
                    aria-current="page"
                >
                    Revisión documental
                </li>

            </ol>
        </nav>
    );

    /*
     * Estado inicial de carga o error general.
     */
    if (cargando || errorCarga) {
        return (
            <Cmp_SIM_Marco
                usuario={responsable}
                ayuda={AYUDA}
            >
                {migas}

                {cargando ? (
                    <p className="text-muted">
                        Cargando la documentación...
                    </p>
                ) : (
                    <>
                        <div
                            className="alert alert-danger"
                            role="alert"
                        >
                            {errorCarga}
                        </div>

                        <div className="d-flex gap-2">

                            <Link
                                className="btn btn-outline-secondary"
                                to={rutaExpediente}
                            >
                                Volver al expediente
                            </Link>

                            <button
                                type="button"
                                className="btn btn-sim"
                                id="btnReintentarRevision"
                                onClick={() =>
                                    setIntentoCarga(
                                        (intento) => intento + 1
                                    )
                                }
                            >
                                <i
                                    className="bi bi-arrow-clockwise me-1"
                                    aria-hidden="true"
                                />
                                Reintentar
                            </button>

                        </div>
                    </>
                )}
            </Cmp_SIM_Marco>
        );
    }

    return (
        <Cmp_SIM_Marco
            usuario={responsable}
            ayuda={AYUDA}
        >
            {migas}

            <section className="sim-encabezado p-3 mb-4 shadow-sm">

                <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">

                    <h1 className="h4 mb-0">
                        Revisión documental del expediente{" "}
                        <span className="sim-referencia">
              {referenciaInterna}
            </span>
                    </h1>

                    {estadoExpediente && (
                        <span className="badge text-bg-warning fs-6">
              {estadoExpediente}
            </span>
                    )}

                </div>

              <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
                Marque cada documento del expediente como recibido o rechazado y guarde la revisión. Un rechazo requiere su justificación.
              </p>
            </section>

            {alerta && (
                <div
                    className={`alert alert-${alerta.tipo}`}
                    role="alert"
                    tabIndex={-1}
                    ref={refAviso}
                >
                    {alerta.texto}
                </div>
            )}

            {!puedeModificar && (
                <Cmp_SIM_AvisoSoloConsulta
                    texto={"Usted puede consultar la revisión documental y su historial, pero sus roles "
                        + "no le permiten registrar revisiones."}
                />
            )}

            <div
                className="card sim-card mb-4"
                id="seccionRevisionDocumental"
            >

                <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">

          <span>
            <i
                className="bi bi-file-earmark-check me-2"
                aria-hidden="true"
            />
            Checklist documental
          </span>

                    <span className="badge text-bg-light text-dark">
            {documentos.length}
          </span>

                </div>

                <div className="card-body">

                    <div className="row g-3 mb-4">

                        <div className="col-12 col-md-6">

                            <label
                                className="form-label"
                                htmlFor="txtExpedienteRevision"
                            >
                                Expediente
                            </label>

                            <input
                                type="text"
                                className="form-control sim-referencia"
                                id="txtExpedienteRevision"
                                value={referenciaInterna}
                                readOnly
                            />

                        </div>

                        <div className="col-12 col-md-6">

                            <label
                                className="form-label"
                                htmlFor="txtResponsableRevision"
                            >
                                Responsable

                                <Cmp_SIM_AyudaCampo titulo="el responsable">
                                    Es la persona con la sesión iniciada.
                                    El sistema registra automáticamente
                                    quién realiza cada revisión.
                                </Cmp_SIM_AyudaCampo>

                            </label>

                            <input
                                type="text"
                                className="form-control"
                                id="txtResponsableRevision"
                                value={responsable}
                                readOnly
                            />

                        </div>

                    </div>

                    <p className="form-text mt-0">
                        Los documentos que todavía no han sido
                        adjuntados permanecen como pendientes.
                        Las revisiones anteriores se conservan
                        en el historial del documento.
                    </p>

                    {obligatorios.length > 0 && (
                        <div
                            className={`alert ${obligatoriosPendientes.length === 0 ? "alert-success" : "alert-warning"} py-2`}
                            id="lblResumenObligatorios"
                            role="status"
                        >
                            <strong>
                                {obligatorios.length - obligatoriosPendientes.length} de {obligatorios.length}
                                {" "}documentos obligatorios recibidos.
                            </strong>
                            {obligatoriosPendientes.length > 0 && (
                                <span>
                                    {" "}Pendientes:{" "}
                                    {obligatoriosPendientes
                                        .map((documento) => documento.nombreTipoDocumento)
                                        .join(", ")}.
                                </span>
                            )}
                        </div>
                    )}

                    {documentos.length === 0 ? (

                        <p className="text-muted small mb-0">
                            No existen requisitos documentales
                            configurados para este expediente.
                        </p>

                    ) : (

                        <div className="table-responsive">

                            <table className="table align-middle sim-tabla-revision">

                                <thead>
                                <tr>
                                    <th>Documento</th>
                                    <th>Archivo</th>
                                    <th>Estado actual</th>

                                    <th>
                                        Revisión

                                        <Cmp_SIM_AyudaCampo titulo="el estado de revisión">
                                            Recibido indica que el documento
                                            fue revisado satisfactoriamente.
                                            Pendiente indica que todavía
                                            requiere revisión. Rechazado
                                            indica que presenta un hallazgo
                                            que debe corregirse.
                                        </Cmp_SIM_AyudaCampo>
                                    </th>

                                    <th>Observación</th>
                                    <th>Acciones</th>
                                </tr>
                                </thead>

                                <tbody>

                                {documentos.map((documento) => (

                                    <tr
                                        key={
                                            documento.idTipoDocumento
                                        }
                                        className={erroresFila[documento.idTipoDocumento]
                                            ? "table-danger"
                                            : tieneCambios(documento) ? "table-warning" : ""}
                                    >

                                        <td>

                                            <div className="fw-semibold">
                                                {
                                                    documento
                                                        .nombreTipoDocumento
                                                }
                                            </div>

                                            <small className="text-muted">
                                                {
                                                    documento.obligatorio
                                                        ? "Obligatorio"
                                                        : "Opcional"
                                                }
                                            </small>

                                        </td>

                                        <td>

                                            {documento.idDocumento ? (
                                                <>
                                                    <div className="text-break">
                                                        {
                                                            documento
                                                                .nombreArchivo
                                                        }
                                                    </div>

                                                    <small className="text-muted">
                                                        Versión{" "}
                                                        {documento.version}
                                                    </small>
                                                </>
                                            ) : (
                                                <span className="text-muted">
                            No adjuntado
                          </span>
                                            )}

                                        </td>

                                        <td>

                        <span
                            className={
                                `badge ${
                                    claseEstado(
                                        documento
                                            .estadoRevision
                                    )
                                }`
                            }
                        >
                          {nombreEstadoRevision(documento.estadoRevision)}
                        </span>

                                        </td>

                                        <td className="sim-revision-estado">

                                            <select
                                                className="form-select"
                                                value={
                                                    documento
                                                        .estadoSeleccionado
                                                }
                                                disabled={
                                                    !documento.idDocumento
                                                    || !puedeModificar
                                                }
                                                onChange={(evento) =>
                                                    cambiarEstado(
                                                        documento
                                                            .idTipoDocumento,
                                                        evento.target.value
                                                    )
                                                }
                                            >

                                                <option value="PENDIENTE">
                                                    Pendiente
                                                </option>

                                                <option value="RECIBIDO">
                                                    Recibido
                                                </option>

                                                <option value="RECHAZADO">
                                                    Rechazado
                                                </option>

                                            </select>

                                        </td>

                                        <td className="sim-revision-observacion">

                        <textarea
                            className="form-control"
                            rows={2}
                            maxLength={
                                LONGITUD_MAXIMA_OBSERVACION
                            }
                            value={
                                documento
                                    .observacionEdicion
                            }
                            disabled={
                                !documento.idDocumento
                                || !puedeModificar
                            }
                            placeholder={
                                documento.idDocumento
                                    ? "Escriba una observación si aplica"
                                    : "Documento pendiente de adjuntar"
                            }
                            onChange={(evento) =>
                                cambiarObservacion(
                                    documento
                                        .idTipoDocumento,
                                    evento.target.value
                                )
                            }
                        />

                                            {erroresFila[documento.idTipoDocumento] && (
                                                <div className="small text-danger mt-1" role="alert">
                                                    <i className="bi bi-exclamation-circle me-1" aria-hidden="true" />
                                                    {erroresFila[documento.idTipoDocumento]}
                                                </div>
                                            )}

                                            {documento.idDocumento && (
                                                <div className="form-text text-end">
                                                    {
                                                        documento
                                                            .observacionEdicion
                                                            .length
                                                    }
                                                    /
                                                    {
                                                        LONGITUD_MAXIMA_OBSERVACION
                                                    }
                                                </div>
                                            )}

                                        </td>

                                        <td>

                                            <div className="d-flex flex-wrap gap-2">

                                                {puedeModificar && (
                                                  <button
                                                      type="button"
                                                      className="btn btn-sim btn-sm"
                                                      disabled={
                                                          !documento.idDocumento
                                                          || guardandoId
                                                          === documento.idDocumento
                                                      }
                                                      onClick={() =>
                                                          guardarRevision(
                                                              documento
                                                          )
                                                      }
                                                  >
                                                      <i
                                                          className="bi bi-save me-1"
                                                          aria-hidden="true"
                                                      />

                                                      {
                                                          guardandoId
                                                          === documento.idDocumento
                                                              ? "Guardando..."
                                                              : "Guardar"
                                                      }
                                                  </button>
                                                )}

                                                <button
                                                    type="button"
                                                    className="btn btn-outline-secondary btn-sm"
                                                    disabled={
                                                        !documento.idDocumento
                                                    }
                                                    onClick={() =>
                                                        consultarHistorial(
                                                            documento
                                                        )
                                                    }
                                                >
                                                    <i
                                                        className="bi bi-clock-history me-1"
                                                        aria-hidden="true"
                                                    />

                                                    Historial
                                                </button>

                                            </div>

                                        </td>

                                    </tr>
                                ))}

                                </tbody>

                            </table>

                        </div>
                    )}

                    {puedeModificar && documentos.length > 0 && (
                        <div className="d-flex flex-wrap justify-content-end align-items-center gap-2 mt-3">
                            <span className="small text-muted">
                                Las filas en amarillo tienen cambios sin guardar.
                            </span>
                            <button
                                type="button"
                                className="btn btn-sim"
                                id="btnGuardarRevisiones"
                                disabled={cantidadModificados === 0 || guardandoTodos}
                                onClick={guardarTodas}
                            >
                                <i className="bi bi-save me-1" aria-hidden="true" />
                                {guardandoTodos
                                    ? "Guardando..."
                                    : `Guardar revisiones (${cantidadModificados})`}
                            </button>
                        </div>
                    )}

                </div>

            </div>

            {documentoHistorial && (

                <div
                    className="card sim-card mb-5"
                    id="seccionHistorialRevision"
                >

                    <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">

            <span>
              <i
                  className="bi bi-clock-history me-2"
                  aria-hidden="true"
              />

              Historial de revisión

              <span className="text-muted small fw-normal">
                {" · "}
                  {
                      documentoHistorial
                          .nombreTipoDocumento
                  }
              </span>
            </span>

                        <button
                            type="button"
                            className="btn btn-outline-secondary btn-sm"
                            onClick={cerrarHistorial}
                        >
                            Cerrar
                        </button>

                    </div>

                    <div className="card-body">

                        {cargandoHistorial ? (

                            <p className="text-muted mb-0">
                                Cargando historial...
                            </p>

                        ) : historial.length === 0 ? (

                            <p className="text-muted small mb-0">
                                Este documento todavía no tiene
                                revisiones registradas.
                            </p>

                        ) : (

                            <div className="table-responsive">

                                <table className="table align-middle mb-0">

                                    <thead>
                                    <tr>
                                        <th>Fecha</th>
                                        <th>Resultado</th>
                                        <th>Responsable</th>
                                        <th>Observación</th>
                                    </tr>
                                    </thead>

                                    <tbody>

                                    {historial.map((registro) => (

                                        <tr
                                            key={
                                                registro
                                                    .idRevisionDocumento
                                            }
                                        >

                                            <td>
                                                {
                                                    formatearFechaHora(
                                                        registro
                                                            .fechaRevision
                                                    )
                                                }
                                            </td>

                                            <td>

                          <span
                              className={
                                  `badge ${
                                      claseEstado(
                                          registro
                                              .estadoRevision
                                      )
                                  }`
                              }
                          >
                            {nombreEstadoRevision(registro.estadoRevision)}
                          </span>

                                            </td>

                                            <td>
                                                {
                                                    registro
                                                        .nombreUsuarioRevisor
                                                    || "—"
                                                }
                                            </td>

                                            <td>
                                                {
                                                    registro.observacion
                                                    || "Sin observaciones"
                                                }
                                            </td>

                                        </tr>
                                    ))}

                                    </tbody>

                                </table>

                            </div>
                        )}

                        <p className="form-text mb-0 mt-3">
                            Las revisiones anteriores se conservan
                            para mantener la trazabilidad documental
                            del expediente.
                        </p>

                    </div>

                </div>
            )}

        </Cmp_SIM_Marco>
    );
}

export default Frm_SIM_RevisarDocumentacionExoneracion;