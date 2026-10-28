package cr.ac.ucr.sim.seguridad;

import cr.ac.ucr.sim.utilerias.Permisos;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Reglas de acceso de los servicios REST (HU-003). Cada constante es una expresión de
 *   Spring Security que se usa en @PreAuthorize, por ejemplo:
 *     {@code @PreAuthorize(ReglasAcceso.DONACION_CONSULTAR)}
 *   Los permisos efectivos del usuario son la unión de los permisos de sus roles activos; los carga
 *   FiltroAutenticacionToken en cada solicitud, por lo que un cambio de roles aplica de inmediato.
 *
 *   Convención por módulo:
 *     X_CONSULTAR / X_CREAR / X_MODIFICAR = el permiso de la operación.
 *     X_ACCESO = cualquiera de los tres; para catálogos y opciones que necesitan tanto la consulta
 *                como el registro o la modificación.
 *   Las reglas que dependen del tipo del trámite se resuelven en SrvAutorizacion (bean
 *   "autorizacion").
 *
 *   Todo servicio nuevo debe declarar su regla: la prueba CoberturaPermisosTest falla si un servicio
 *   REST no tiene @PreAuthorize ni @AccesoPublico.
 */
public final class ReglasAcceso {

    private ReglasAcceso() {
    }

    private static final String UNO = "hasAuthority('";
    private static final String ALGUNO = "hasAnyAuthority('";
    private static final String SEPARADOR = "', '";
    private static final String CIERRE = "')";

    // -----------------------------------------------------------------------
    // Generales.
    // -----------------------------------------------------------------------

    /** Cualquier persona con sesión válida (y al menos un rol activo). */
    public static final String CON_SESION = "isAuthenticated()";

    // -----------------------------------------------------------------------
    // Administración de usuarios.
    // -----------------------------------------------------------------------

    /** Ver roles: la gestión de roles y la asignación de roles a usuarios. */
    public static final String ROL_CONSULTA = ALGUNO + Permisos.ROL_CONSULTAR + SEPARADOR
            + Permisos.ROL_MODIFICAR + SEPARADOR + Permisos.ROL_ASIGNAR + CIERRE;
    public static final String ROL_CREAR = UNO + Permisos.ROL_CREAR + CIERRE;
    public static final String ROL_MODIFICAR = UNO + Permisos.ROL_MODIFICAR + CIERRE;
    public static final String ROL_ASIGNAR = UNO + Permisos.ROL_ASIGNAR + CIERRE;
    /** Ver el catálogo de permisos: quien consulta, crea o modifica roles. */
    public static final String PERMISO_CONSULTA = ALGUNO + Permisos.ROL_CONSULTAR + SEPARADOR
            + Permisos.ROL_CREAR + SEPARADOR + Permisos.ROL_MODIFICAR + CIERRE;
    /** Buscar usuarios y ver sus roles: quien consulta usuarios o asigna roles. */
    public static final String USUARIO_ROLES_CONSULTA = ALGUNO + Permisos.USUARIO_CONSULTAR + SEPARADOR
            + Permisos.ROL_ASIGNAR + CIERRE;

    // -----------------------------------------------------------------------
    // Gestión de importaciones (pedidos de importación).
    // -----------------------------------------------------------------------

    public static final String IMPORTACION_CONSULTAR = UNO + Permisos.IMPORTACION_CONSULTAR + CIERRE;
    public static final String IMPORTACION_CREAR = UNO + Permisos.IMPORTACION_CREAR + CIERRE;
    public static final String IMPORTACION_MODIFICAR = UNO + Permisos.IMPORTACION_MODIFICAR + CIERRE;
    public static final String IMPORTACION_ACCESO = ALGUNO + Permisos.IMPORTACION_CONSULTAR + SEPARADOR
            + Permisos.IMPORTACION_CREAR + SEPARADOR + Permisos.IMPORTACION_MODIFICAR + CIERRE;
    /** Notificaciones de pagos pendientes de revisión. */
    public static final String IMPORTACION_NOTIFICACIONES = ALGUNO + Permisos.IMPORTACION_CONSULTAR
            + SEPARADOR + Permisos.IMPORTACION_PAGO_GESTIONAR + CIERRE;

    // -----------------------------------------------------------------------
    // Gestión de exoneraciones.
    // -----------------------------------------------------------------------

    public static final String EXONERACION_CONSULTAR = UNO + Permisos.EXONERACION_CONSULTAR + CIERRE;
    public static final String EXONERACION_CREAR = UNO + Permisos.EXONERACION_CREAR + CIERRE;
    public static final String EXONERACION_MODIFICAR = UNO + Permisos.EXONERACION_MODIFICAR + CIERRE;
    /** Ver el expediente o la decisión: quien gestiona la exoneración o quien la aprueba. */
    public static final String EXONERACION_CONSULTA_O_APROBACION = ALGUNO + Permisos.EXONERACION_CONSULTAR
            + SEPARADOR + Permisos.EXONERACION_APROBAR + CIERRE;

    // -----------------------------------------------------------------------
    // Aprobaciones.
    // -----------------------------------------------------------------------

    public static final String EXONERACION_APROBAR = UNO + Permisos.EXONERACION_APROBAR + CIERRE;

    // -----------------------------------------------------------------------
    // Gestión aduanera (importaciones y exportaciones temporales).
    // -----------------------------------------------------------------------

    public static final String ADUANERO_TEMPORAL_CONSULTAR = UNO + Permisos.ADUANERO_TEMPORAL_CONSULTAR + CIERRE;
    public static final String ADUANERO_TEMPORAL_CREAR = UNO + Permisos.ADUANERO_TEMPORAL_CREAR + CIERRE;
    public static final String ADUANERO_TEMPORAL_MODIFICAR = UNO + Permisos.ADUANERO_TEMPORAL_MODIFICAR + CIERRE;
    public static final String ADUANERO_TEMPORAL_ACCESO = ALGUNO + Permisos.ADUANERO_TEMPORAL_CONSULTAR
            + SEPARADOR + Permisos.ADUANERO_TEMPORAL_CREAR + SEPARADOR + Permisos.ADUANERO_TEMPORAL_MODIFICAR
            + CIERRE;

    // -----------------------------------------------------------------------
    // Gestión de donaciones.
    // -----------------------------------------------------------------------

    public static final String DONACION_CONSULTAR = UNO + Permisos.DONACION_CONSULTAR + CIERRE;
    public static final String DONACION_CREAR = UNO + Permisos.DONACION_CREAR + CIERRE;
    public static final String DONACION_MODIFICAR = UNO + Permisos.DONACION_MODIFICAR + CIERRE;
    public static final String DONACION_ACCESO = ALGUNO + Permisos.DONACION_CONSULTAR + SEPARADOR
            + Permisos.DONACION_CREAR + SEPARADOR + Permisos.DONACION_MODIFICAR + CIERRE;

    // -----------------------------------------------------------------------
    // Gestión de desalmacenajes especiales.
    // -----------------------------------------------------------------------

    public static final String DESALMACENAJE_CONSULTAR = UNO + Permisos.DESALMACENAJE_CONSULTAR + CIERRE;
    public static final String DESALMACENAJE_CREAR = UNO + Permisos.DESALMACENAJE_CREAR + CIERRE;
    public static final String DESALMACENAJE_MODIFICAR = UNO + Permisos.DESALMACENAJE_MODIFICAR + CIERRE;
    public static final String DESALMACENAJE_ACCESO = ALGUNO + Permisos.DESALMACENAJE_CONSULTAR + SEPARADOR
            + Permisos.DESALMACENAJE_CREAR + SEPARADOR + Permisos.DESALMACENAJE_MODIFICAR + CIERRE;

    // -----------------------------------------------------------------------
    // Reportes y consultas.
    // -----------------------------------------------------------------------

    public static final String REPORTE_CONSULTAR = UNO + Permisos.REPORTE_CONSULTAR + CIERRE;
    public static final String REPORTE_EXPORTAR = UNO + Permisos.REPORTE_EXPORTAR + CIERRE;

    // -----------------------------------------------------------------------
    // Servicios compartidos por varios tipos de trámite (ver SrvAutorizacion).
    // -----------------------------------------------------------------------

    /**
     * Panel de trámites activos y búsqueda: quien pueda consultar al menos un tipo de trámite, o
     * tenga REPORTE_CONSULTAR. La lista se filtra a los tipos que puede consultar.
     */
    public static final String CONSULTA_TRAMITES = "@autorizacion.TieneConsultaDeTramites()";

    /** Cambio de referencia y costos: el permiso de modificar del tipo del trámite. */
    public static final String MODIFICAR_TRAMITE_POR_REFERENCIA =
            "@autorizacion.PuedeModificarTramite(#pvc_ReferenciaInterna)";

    /** Descarga de documentos: el permiso de consultar del tipo del trámite al que pertenece. */
    public static final String CONSULTAR_DOCUMENTO =
            "@autorizacion.PuedeConsultarDocumento(#pvn_IdDocumento)";
}
