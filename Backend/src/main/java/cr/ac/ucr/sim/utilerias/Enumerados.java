package cr.ac.ucr.sim.utilerias;

import java.util.HashSet;
import java.util.Set;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Enumerados de uso común del sistema (estándar P03).
 */
public final class Enumerados {

    private Enumerados() {
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Tipos de trámite temporal. La importación temporal (HU-025) y la exportación
     *   temporal (HU-026) comparten el expediente, sus reglas y sus alertas; aquí está lo que cambia
     *   entre ellas: la ruta del servicio, el tipo del catálogo, los estados con reglas propias, los
     *   DUA que abren y cierran el régimen, los documentos admitidos y sus mensajes.
     */
    public enum TipoTramiteTemporal {

        IMPORTACION(
                "importaciones-temporales",
                Constantes.CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL,
                Constantes.REGIMEN_IMPORTACION_TEMPORAL,
                Constantes.ESTADO_TEMPORAL_ACTIVO,
                Constantes.ESTADO_TEMPORAL_EN_REEXPORTACION,
                Constantes.CODIGO_TIPO_DOCUMENTO_DUA_IMPORTACION_TEMPORAL,
                "DUA de importación temporal",
                Constantes.CODIGO_TIPO_DOCUMENTO_DUA_REEXPORTACION,
                // PU-11 (pruebas con usuarios, 25/10/2026): los agentes lo llaman DUA de cancelación.
                "DUA de cancelación",
                Set.of(),
                Constantes.MENSAJE_TIPO_TRAMITE_TEMPORAL_NO_CONFIGURADO,
                Constantes.MENSAJE_ESTADO_INICIAL_TEMPORAL_NO_CONFIGURADO,
                Constantes.MENSAJE_REFERENCIA_TEMPORAL_INVALIDA,
                Constantes.MENSAJE_EXPEDIENTE_TEMPORAL_NO_ENCONTRADO,
                Constantes.MENSAJE_ESTADO_TEMPORAL_INVALIDO,
                Constantes.MENSAJE_VENCIMIENTO_REQUERIDO_ACTIVO,
                Constantes.MENSAJE_VENCIMIENTO_NO_SE_PUEDE_BORRAR,
                Constantes.MENSAJE_REQUISITOS_FINALIZACION,
                Constantes.MENSAJE_TIPO_DOCUMENTO_TEMPORAL_INVALIDO,
                Constantes.MENSAJE_REGISTRO_TEMPORAL_EXITOSO,
                Constantes.MENSAJE_REGISTRO_TEMPORAL_SIN_VENCIMIENTO),

        EXPORTACION(
                "exportaciones-temporales",
                Constantes.CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL,
                Constantes.REGIMEN_EXPORTACION_TEMPORAL,
                Constantes.ESTADO_TEMPORAL_FUERA_DEL_PAIS,
                Constantes.ESTADO_TEMPORAL_EN_REIMPORTACION,
                Constantes.CODIGO_TIPO_DOCUMENTO_DUA_EXPORTACION_TEMPORAL,
                "DUA de exportación temporal",
                Constantes.CODIGO_TIPO_DOCUMENTO_DUA_REIMPORTACION,
                "DUA de reingreso",
                Set.of(
                        Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_NOTIFICACION_USUARIO,
                        Constantes.CODIGO_TIPO_DOCUMENTO_SOLICITUD_REINGRESO,
                        Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE),
                Constantes.MENSAJE_TIPO_TRAMITE_EXPORTACION_TEMPORAL_NO_CONFIGURADO,
                Constantes.MENSAJE_ESTADO_INICIAL_EXPORTACION_TEMPORAL_NO_CONFIGURADO,
                Constantes.MENSAJE_REFERENCIA_EXPORTACION_TEMPORAL_INVALIDA,
                Constantes.MENSAJE_EXPEDIENTE_EXPORTACION_TEMPORAL_NO_ENCONTRADO,
                Constantes.MENSAJE_ESTADO_EXPORTACION_TEMPORAL_INVALIDO,
                Constantes.MENSAJE_VENCIMIENTO_REQUERIDO_FUERA_DEL_PAIS,
                Constantes.MENSAJE_VENCIMIENTO_NO_SE_PUEDE_BORRAR_EXPORTACION,
                Constantes.MENSAJE_REQUISITOS_FINALIZACION_EXPORTACION,
                Constantes.MENSAJE_TIPO_DOCUMENTO_EXPORTACION_TEMPORAL_INVALIDO,
                Constantes.MENSAJE_REGISTRO_EXPORTACION_TEMPORAL_EXITOSO,
                Constantes.MENSAJE_REGISTRO_EXPORTACION_TEMPORAL_SIN_VENCIMIENTO);

        private final String vgc_Ruta;
        private final String vgc_CodigoTipoTramite;
        private final String vgc_Regimen;
        private final String vgc_EstadoEquipoEnDestino;
        private final String vgc_EstadoRetorno;
        private final String vgc_CodigoDuaInicio;
        private final String vgc_NombreDuaInicio;
        private final String vgc_CodigoDuaCierre;
        private final String vgc_NombreDuaCierre;
        private final Set<String> vgo_LsTiposDocumento;
        private final String vgc_MensajeTipoNoConfigurado;
        private final String vgc_MensajeEstadoInicialNoConfigurado;
        private final String vgc_MensajeReferenciaInvalida;
        private final String vgc_MensajeExpedienteNoEncontrado;
        private final String vgc_MensajeEstadoInvalido;
        private final String vgc_MensajeVencimientoRequerido;
        private final String vgc_MensajeVencimientoNoSeBorra;
        private final String vgc_MensajeRequisitosFinalizacion;
        private final String vgc_MensajeTipoDocumentoInvalido;
        private final String vgc_MensajeRegistroExitoso;
        private final String vgc_MensajeRegistroSinVencimiento;

        /**
         * @param pvc_Ruta segmento de la ruta del servicio REST
         * @param pvc_CodigoTipoTramite código del tipo en SIMM_TIPO_TRAMITE
         * @param pvc_Regimen régimen aduanero que se registra en el expediente
         * @param pvc_EstadoEquipoEnDestino estado en que el equipo ya está en su destino temporal
         *   (Activo en el país o Fuera del país); exige la fecha de vencimiento
         * @param pvc_EstadoRetorno estado en que se gestiona el retorno del equipo
         * @param pvc_CodigoDuaInicio documento del DUA que inicia el régimen
         * @param pvc_NombreDuaInicio nombre del DUA que inicia el régimen
         * @param pvc_CodigoDuaCierre documento del DUA que cierra el régimen
         * @param pvc_NombreDuaCierre nombre del DUA que cierra el régimen
         * @param pvo_LsTiposDocumentoPropios documentos que solo admite este tipo, además de los
         *   comunes y de los dos DUA
         */
        TipoTramiteTemporal(
                String pvc_Ruta,
                String pvc_CodigoTipoTramite,
                String pvc_Regimen,
                String pvc_EstadoEquipoEnDestino,
                String pvc_EstadoRetorno,
                String pvc_CodigoDuaInicio,
                String pvc_NombreDuaInicio,
                String pvc_CodigoDuaCierre,
                String pvc_NombreDuaCierre,
                Set<String> pvo_LsTiposDocumentoPropios,
                String pvc_MensajeTipoNoConfigurado,
                String pvc_MensajeEstadoInicialNoConfigurado,
                String pvc_MensajeReferenciaInvalida,
                String pvc_MensajeExpedienteNoEncontrado,
                String pvc_MensajeEstadoInvalido,
                String pvc_MensajeVencimientoRequerido,
                String pvc_MensajeVencimientoNoSeBorra,
                String pvc_MensajeRequisitosFinalizacion,
                String pvc_MensajeTipoDocumentoInvalido,
                String pvc_MensajeRegistroExitoso,
                String pvc_MensajeRegistroSinVencimiento) {
            this.vgc_Ruta = pvc_Ruta;
            this.vgc_CodigoTipoTramite = pvc_CodigoTipoTramite;
            this.vgc_Regimen = pvc_Regimen;
            this.vgc_EstadoEquipoEnDestino = pvc_EstadoEquipoEnDestino;
            this.vgc_EstadoRetorno = pvc_EstadoRetorno;
            this.vgc_CodigoDuaInicio = pvc_CodigoDuaInicio;
            this.vgc_NombreDuaInicio = pvc_NombreDuaInicio;
            this.vgc_CodigoDuaCierre = pvc_CodigoDuaCierre;
            this.vgc_NombreDuaCierre = pvc_NombreDuaCierre;
            this.vgc_MensajeTipoNoConfigurado = pvc_MensajeTipoNoConfigurado;
            this.vgc_MensajeEstadoInicialNoConfigurado = pvc_MensajeEstadoInicialNoConfigurado;
            this.vgc_MensajeReferenciaInvalida = pvc_MensajeReferenciaInvalida;
            this.vgc_MensajeExpedienteNoEncontrado = pvc_MensajeExpedienteNoEncontrado;
            this.vgc_MensajeEstadoInvalido = pvc_MensajeEstadoInvalido;
            this.vgc_MensajeVencimientoRequerido = pvc_MensajeVencimientoRequerido;
            this.vgc_MensajeVencimientoNoSeBorra = pvc_MensajeVencimientoNoSeBorra;
            this.vgc_MensajeRequisitosFinalizacion = pvc_MensajeRequisitosFinalizacion;
            this.vgc_MensajeTipoDocumentoInvalido = pvc_MensajeTipoDocumentoInvalido;
            this.vgc_MensajeRegistroExitoso = pvc_MensajeRegistroExitoso;
            this.vgc_MensajeRegistroSinVencimiento = pvc_MensajeRegistroSinVencimiento;

            Set<String> vlo_LsTipos = new HashSet<>(TiposDocumentoComunes());
            vlo_LsTipos.add(pvc_CodigoDuaInicio);
            vlo_LsTipos.add(pvc_CodigoDuaCierre);
            vlo_LsTipos.addAll(pvo_LsTiposDocumentoPropios);
            this.vgo_LsTiposDocumento = Set.copyOf(vlo_LsTipos);
        }

        /**
         * Autor: Josue Guevara Lostalo
         * Fecha: 04/10/2026
         * Descripción: Documentos que admiten ambos tipos: la solicitud, la gestión ante la aduana,
         *   la prórroga y la cancelación del régimen. Es un método y no un campo estático porque el
         *   constructor de las constantes del enumerado no puede leer los campos estáticos.
         *
         * @return códigos de los documentos comunes
         */
        private static Set<String> TiposDocumentoComunes() {
            return Set.of(
                    Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_SOLICITUD,
                    Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA,
                    Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_ADUANA,
                    Constantes.CODIGO_TIPO_DOCUMENTO_RESOLUCION_AUTORIZACION,
                    Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_PRORROGA,
                    Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_PRORROGA,
                    Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_CANCELACION,
                    Constantes.CODIGO_TIPO_DOCUMENTO_RESPALDO);
        }

        /**
         * Autor: Josue Guevara Lostalo
         * Fecha: 04/10/2026
         * Descripción: Obtiene el tipo de trámite temporal que atiende un segmento de la ruta.
         *
         * @param pvc_Ruta importaciones-temporales o exportaciones-temporales
         * @return tipo de trámite temporal, o null si la ruta no corresponde a ninguno
         */
        public static TipoTramiteTemporal ObtenerPorRuta(String pvc_Ruta) {
            for (TipoTramiteTemporal vlo_Tipo : values()) {
                if (vlo_Tipo.vgc_Ruta.equals(pvc_Ruta)) {
                    return vlo_Tipo;
                }
            }
            return null;
        }

        public String getRuta() {
            return this.vgc_Ruta;
        }

        public String getCodigoTipoTramite() {
            return this.vgc_CodigoTipoTramite;
        }

        public String getRegimen() {
            return this.vgc_Regimen;
        }

        public String getEstadoEquipoEnDestino() {
            return this.vgc_EstadoEquipoEnDestino;
        }

        public String getEstadoRetorno() {
            return this.vgc_EstadoRetorno;
        }

        public String getCodigoDuaInicio() {
            return this.vgc_CodigoDuaInicio;
        }

        public String getNombreDuaInicio() {
            return this.vgc_NombreDuaInicio;
        }

        public String getCodigoDuaCierre() {
            return this.vgc_CodigoDuaCierre;
        }

        public String getNombreDuaCierre() {
            return this.vgc_NombreDuaCierre;
        }

        public Set<String> getTiposDocumento() {
            return this.vgo_LsTiposDocumento;
        }

        public String getMensajeTipoNoConfigurado() {
            return this.vgc_MensajeTipoNoConfigurado;
        }

        public String getMensajeEstadoInicialNoConfigurado() {
            return this.vgc_MensajeEstadoInicialNoConfigurado;
        }

        public String getMensajeReferenciaInvalida() {
            return this.vgc_MensajeReferenciaInvalida;
        }

        public String getMensajeExpedienteNoEncontrado() {
            return this.vgc_MensajeExpedienteNoEncontrado;
        }

        public String getMensajeEstadoInvalido() {
            return this.vgc_MensajeEstadoInvalido;
        }

        public String getMensajeVencimientoRequerido() {
            return this.vgc_MensajeVencimientoRequerido;
        }

        public String getMensajeVencimientoNoSeBorra() {
            return this.vgc_MensajeVencimientoNoSeBorra;
        }

        public String getMensajeRequisitosFinalizacion() {
            return this.vgc_MensajeRequisitosFinalizacion;
        }

        public String getMensajeTipoDocumentoInvalido() {
            return this.vgc_MensajeTipoDocumentoInvalido;
        }

        public String getMensajeRegistroExitoso() {
            return this.vgc_MensajeRegistroExitoso;
        }

        public String getMensajeRegistroSinVencimiento() {
            return this.vgc_MensajeRegistroSinVencimiento;
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Módulos de acceso de la HU-003. Agrupan los permisos de SIMM_PERMISO (columna
     *   MODULO) y fijan el orden y el nombre con que se muestran en el menú y en la pantalla de
     *   roles. El código coincide con 16-Modulos-Permisos-Roles-Base.sql y con
     *   Frontend/src/utilerias/Permisos.js. Un módulo nuevo se agrega como un valor más.
     */
    public enum ModuloPermiso {

        IMPORTACIONES(1, "Gestión de importaciones"),
        EXONERACIONES(2, "Gestión de exoneraciones"),
        ADUANERA(3, "Gestión aduanera"),
        DONACIONES(4, "Gestión de donaciones"),
        DESALMACENAJES(5, "Gestión de desalmacenajes especiales"),
        REPORTES(6, "Reportes y consultas"),
        APROBACIONES(7, "Aprobaciones"),
        ADMIN_USUARIOS(8, "Administración de usuarios");

        private final int vgn_Orden;
        private final String vgc_Nombre;

        ModuloPermiso(int pvn_Orden, String pvc_Nombre) {
            this.vgn_Orden = pvn_Orden;
            this.vgc_Nombre = pvc_Nombre;
        }

        public int getOrden() {
            return this.vgn_Orden;
        }

        public String getNombre() {
            return this.vgc_Nombre;
        }

        /**
         * Autor: Equipo de Desarrollo SIM
         * Fecha: 19/10/2026
         * Descripción: Busca el módulo por su código de base de datos.
         *
         * @param pvc_Codigo valor de SIMM_PERMISO.MODULO
         * @return el módulo, o null si el código es nulo o no corresponde a ningún módulo
         */
        public static ModuloPermiso DesdeCodigo(String pvc_Codigo) {
            if (pvc_Codigo == null) {
                return null;
            }
            for (ModuloPermiso vlo_Modulo : values()) {
                if (vlo_Modulo.name().equalsIgnoreCase(pvc_Codigo.trim())) {
                    return vlo_Modulo;
                }
            }
            return null;
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Permisos de cada tipo de trámite (SIMM_TIPO_TRAMITE.CODIGO). Se usa en los
     *   servicios compartidos por varios tipos (cambio de referencia, costos, descarga de
     *   documentos, panel de trámites activos y búsqueda), donde el permiso depende del tipo del
     *   trámite. Un tipo de trámite nuevo se agrega como un valor más; un tipo que no esté aquí no
     *   lo puede consultar ni modificar nadie desde esos servicios.
     */
    public enum AccesoTipoTramite {

        PEDIDO_IMPORTACION(
                Constantes.CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION,
                Permisos.IMPORTACION_CONSULTAR,
                Permisos.IMPORTACION_MODIFICAR),
        EXONERACION(
                Constantes.CODIGO_TIPO_TRAMITE_EXONERACION,
                Permisos.EXONERACION_CONSULTAR,
                Permisos.EXONERACION_MODIFICAR),
        IMPORTACION_TEMPORAL(
                Constantes.CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL,
                Permisos.ADUANERO_TEMPORAL_CONSULTAR,
                Permisos.ADUANERO_TEMPORAL_MODIFICAR),
        EXPORTACION_TEMPORAL(
                Constantes.CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL,
                Permisos.ADUANERO_TEMPORAL_CONSULTAR,
                Permisos.ADUANERO_TEMPORAL_MODIFICAR),
        DONACION(
                Constantes.CODIGO_TIPO_TRAMITE_DONACION,
                Permisos.DONACION_CONSULTAR,
                Permisos.DONACION_MODIFICAR),
        DESALMACENAJE(
                Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE,
                Permisos.DESALMACENAJE_CONSULTAR,
                Permisos.DESALMACENAJE_MODIFICAR);

        private final String vgc_CodigoTipoTramite;
        private final String vgc_PermisoConsultar;
        private final String vgc_PermisoModificar;

        AccesoTipoTramite(String pvc_CodigoTipoTramite, String pvc_PermisoConsultar,
                          String pvc_PermisoModificar) {
            this.vgc_CodigoTipoTramite = pvc_CodigoTipoTramite;
            this.vgc_PermisoConsultar = pvc_PermisoConsultar;
            this.vgc_PermisoModificar = pvc_PermisoModificar;
        }

        public String getCodigoTipoTramite() {
            return this.vgc_CodigoTipoTramite;
        }

        public String getPermisoConsultar() {
            return this.vgc_PermisoConsultar;
        }

        public String getPermisoModificar() {
            return this.vgc_PermisoModificar;
        }

        /**
         * Autor: Equipo de Desarrollo SIM
         * Fecha: 19/10/2026
         * Descripción: Busca los permisos de un tipo de trámite por su código.
         *
         * @param pvc_CodigoTipoTramite valor de SIMM_TIPO_TRAMITE.CODIGO
         * @return el acceso del tipo, o null si el código es nulo o no está configurado
         */
        public static AccesoTipoTramite DesdeCodigo(String pvc_CodigoTipoTramite) {
            if (pvc_CodigoTipoTramite == null) {
                return null;
            }
            for (AccesoTipoTramite vlo_Acceso : values()) {
                if (vlo_Acceso.vgc_CodigoTipoTramite.equalsIgnoreCase(pvc_CodigoTipoTramite.trim())) {
                    return vlo_Acceso;
                }
            }
            return null;
        }
    }
}
