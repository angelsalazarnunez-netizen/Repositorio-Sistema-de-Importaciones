package cr.ac.ucr.sim.utilerias;

/**
 * Descripcion: Codigos de permisos
 * utilizados para el control de acceso del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: la clase pasa a ser la única fuente de los códigos de permiso del backend (HU-003).
 *     Cada código coincide con SIMM_PERMISO.CODIGO y está agrupado según SIMM_PERMISO.MODULO
 *     (ver Enumerados.ModuloPermiso y 16-Modulos-Permisos-Roles-Base.sql). Se reemplaza
 *     APROBACION_EXONERACION por EXONERACION_APROBAR, el código que ya usaban la HU-022, la base de
 *     datos y el frontend. Al agregar un permiso nuevo: insertarlo en SIMM_PERMISO con su módulo,
 *     declararlo aquí y en Frontend/src/utilerias/Permisos.js.
 */
public final class Permisos {

    private Permisos() {
    }

    // -----------------------------------------------------------------------
    // Módulo ADMIN_USUARIOS: Administración de usuarios.
    // -----------------------------------------------------------------------

    public static final String ROL_CONSULTAR =
            "ROL_CONSULTAR";

    public static final String ROL_CREAR =
            "ROL_CREAR";

    public static final String ROL_MODIFICAR =
            "ROL_MODIFICAR";

    public static final String ROL_ASIGNAR =
            "ROL_ASIGNAR";

    public static final String USUARIO_CONSULTAR =
            "USUARIO_CONSULTAR";

    public static final String USUARIO_MODIFICAR =
            "USUARIO_MODIFICAR";

    // -----------------------------------------------------------------------
    // Módulo IMPORTACIONES: Gestión de importaciones.
    // -----------------------------------------------------------------------

    public static final String IMPORTACION_CONSULTAR =
            "IMPORTACION_CONSULTAR";

    public static final String IMPORTACION_CREAR =
            "IMPORTACION_CREAR";

    public static final String IMPORTACION_MODIFICAR =
            "IMPORTACION_MODIFICAR";

    public static final String IMPORTACION_PAGO_GESTIONAR =
            "IMPORTACION_PAGO_GESTIONAR";

    public static final String IMPORTACION_DOCUMENTO_GENERAR =
            "IMPORTACION_DOCUMENTO_GENERAR";

    // -----------------------------------------------------------------------
    // Módulo EXONERACIONES: Gestión de exoneraciones.
    // -----------------------------------------------------------------------

    public static final String EXONERACION_CONSULTAR =
            "EXONERACION_CONSULTAR";

    public static final String EXONERACION_CREAR =
            "EXONERACION_CREAR";

    public static final String EXONERACION_MODIFICAR =
            "EXONERACION_MODIFICAR";

    // -----------------------------------------------------------------------
    // Módulo DONACIONES: Gestión de donaciones.
    // -----------------------------------------------------------------------

    public static final String DONACION_CONSULTAR =
            "DONACION_CONSULTAR";

    public static final String DONACION_CREAR =
            "DONACION_CREAR";

    public static final String DONACION_MODIFICAR =
            "DONACION_MODIFICAR";

    // -----------------------------------------------------------------------
    // Módulo ADUANERA: Gestión aduanera.
    // -----------------------------------------------------------------------

    public static final String ADUANERO_TEMPORAL_CONSULTAR =
            "ADUANERO_TEMPORAL_CONSULTAR";

    public static final String ADUANERO_TEMPORAL_CREAR =
            "ADUANERO_TEMPORAL_CREAR";

    public static final String ADUANERO_TEMPORAL_MODIFICAR =
            "ADUANERO_TEMPORAL_MODIFICAR";

    public static final String ADUANERO_RECLAMO_GESTIONAR =
            "ADUANERO_RECLAMO_GESTIONAR";

    // -----------------------------------------------------------------------
    // Módulo DESALMACENAJES: Gestión de desalmacenajes especiales.
    // -----------------------------------------------------------------------

    public static final String DESALMACENAJE_CONSULTAR =
            "DESALMACENAJE_CONSULTAR";

    public static final String DESALMACENAJE_CREAR =
            "DESALMACENAJE_CREAR";

    public static final String DESALMACENAJE_MODIFICAR =
            "DESALMACENAJE_MODIFICAR";

    // -----------------------------------------------------------------------
    // Módulo REPORTES: Reportes y consultas.
    // -----------------------------------------------------------------------

    public static final String REPORTE_CONSULTAR =
            "REPORTE_CONSULTAR";

    public static final String REPORTE_FILTRAR =
            "REPORTE_FILTRAR";

    public static final String REPORTE_GENERAR =
            "REPORTE_GENERAR";

    public static final String REPORTE_EXPORTAR =
            "REPORTE_EXPORTAR";

    public static final String GRAFICO_GENERAR =
            "GRAFICO_GENERAR";

    public static final String BITACORA_CONSULTAR =
            "BITACORA_CONSULTAR";

    // -----------------------------------------------------------------------
    // Módulo APROBACIONES: Aprobaciones.
    // -----------------------------------------------------------------------

    public static final String EXONERACION_APROBAR =
            "EXONERACION_APROBAR";

    public static final String APROBACION_FONDOS =
            "APROBACION_FONDOS";
}
