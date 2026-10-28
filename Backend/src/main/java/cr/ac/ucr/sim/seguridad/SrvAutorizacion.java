package cr.ac.ucr.sim.seguridad;

import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import cr.ac.ucr.sim.utilerias.Enumerados.AccesoTipoTramite;
import cr.ac.ucr.sim.utilerias.Permisos;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Reglas de acceso que dependen del tipo del trámite (HU-003). Se usa desde
 *   @PreAuthorize con el nombre "autorizacion" (ver ReglasAcceso) y desde los servicios del panel de
 *   trámites activos y de la búsqueda, que muestran varios tipos de trámite a la vez.
 *   Reglas:
 *   - El permiso de cada tipo de trámite sale de Enumerados.AccesoTipoTramite. Un tipo que no esté
 *     ahí no se puede consultar ni modificar desde los servicios compartidos.
 *   - REPORTE_CONSULTAR permite ver todos los tipos en el panel y en la búsqueda (es el rol
 *     transversal de "Reportes y consultas"), pero no permite modificar trámites.
 *   - Si el trámite o el documento no existe, la regla lo deja pasar para que la lógica de negocio
 *     responda que no se encontró; así no se cambia el mensaje que ya da cada servicio.
 */
@Component("autorizacion")
public class SrvAutorizacion {

    private final DalTramite vgo_DalTramite;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalDocumento vgo_DalDocumento;

    public SrvAutorizacion(
            DalTramite pvo_DalTramite,
            DalTipoTramite pvo_DalTipoTramite,
            DalDocumento pvo_DalDocumento) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalDocumento = pvo_DalDocumento;
    }

    // -----------------------------------------------------------------------
    // Reglas usadas en @PreAuthorize.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Indica si el usuario puede entrar al panel de trámites activos o a la búsqueda:
     *   tiene REPORTE_CONSULTAR o el permiso de consultar al menos un tipo de trámite.
     *
     * @return verdadero si puede consultar trámites
     */
    public boolean TieneConsultaDeTramites() {
        Set<String> vlo_Permisos = this.PermisosDeLaSesion();
        return vlo_Permisos.contains(Permisos.REPORTE_CONSULTAR)
                || Arrays.stream(AccesoTipoTramite.values())
                        .anyMatch(vlo_Acceso -> vlo_Permisos.contains(vlo_Acceso.getPermisoConsultar()));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Indica si el usuario puede modificar el trámite de la referencia indicada, según
     *   el permiso de modificar de su tipo de trámite.
     *
     * @param pvc_ReferenciaInterna referencia interna del trámite
     * @return verdadero si puede modificarlo, o si el trámite no existe
     */
    @Transactional(readOnly = true)
    public boolean PuedeModificarTramite(String pvc_ReferenciaInterna) {
        EntTramite vlo_Tramite = pvc_ReferenciaInterna == null
                ? null
                : this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(pvc_ReferenciaInterna.trim());
        return vlo_Tramite == null
                || this.TienePermisoSobreTipo(vlo_Tramite.getIdTipoTramite(), AccesoTipoTramite::getPermisoModificar);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Indica si el usuario puede descargar un documento, según el permiso de consultar
     *   del tipo del trámite al que pertenece. Las pantallas de varios tipos de trámite descargan
     *   los documentos por su identificador.
     *
     * @param pvn_IdDocumento identificador del documento
     * @return verdadero si puede consultarlo, o si el documento no existe
     */
    @Transactional(readOnly = true)
    public boolean PuedeConsultarDocumento(Integer pvn_IdDocumento) {
        EntDocumento vlo_Documento = pvn_IdDocumento == null
                ? null
                : this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(pvn_IdDocumento);
        if (vlo_Documento == null) {
            return true;
        }
        EntTramite vlo_Tramite = this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(vlo_Documento.getIdTramite());
        return vlo_Tramite == null
                || this.TienePermisoSobreTipo(vlo_Tramite.getIdTipoTramite(), AccesoTipoTramite::getPermisoConsultar);
    }

    // -----------------------------------------------------------------------
    // Filtros del panel de trámites activos y de la búsqueda (decisión: cada quien ve los tipos que
    // puede consultar).
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Indica si el usuario puede ver todos los tipos de trámite (REPORTE_CONSULTAR).
     *
     * @return verdadero si no hay que filtrar por tipo
     */
    public boolean PuedeConsultarTodosLosTipos() {
        return this.PermisosDeLaSesion().contains(Permisos.REPORTE_CONSULTAR);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Códigos de los tipos de trámite (SIMM_TIPO_TRAMITE.CODIGO) que el usuario puede
     *   consultar. Con REPORTE_CONSULTAR devuelve null, que significa "todos".
     *
     * @return códigos permitidos, o null si puede consultar todos
     */
    public Set<String> CodigosTipoTramiteConsultables() {
        if (this.PuedeConsultarTodosLosTipos()) {
            return null;
        }
        Set<String> vlo_Permisos = this.PermisosDeLaSesion();
        return Arrays.stream(AccesoTipoTramite.values())
                .filter(vlo_Acceso -> vlo_Permisos.contains(vlo_Acceso.getPermisoConsultar()))
                .map(AccesoTipoTramite::getCodigoTipoTramite)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Indica si un tipo de trámite, por su código, está entre los que el usuario puede
     *   consultar.
     *
     * @param pvc_CodigoTipoTramite código del tipo de trámite
     * @return verdadero si lo puede consultar
     */
    public boolean PuedeConsultarTipo(String pvc_CodigoTipoTramite) {
        Set<String> vlo_Codigos = this.CodigosTipoTramiteConsultables();
        return vlo_Codigos == null
                || (pvc_CodigoTipoTramite != null && vlo_Codigos.stream()
                        .anyMatch(vlc_Codigo -> vlc_Codigo.equalsIgnoreCase(pvc_CodigoTipoTramite.trim())));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Ajusta los tipos de trámite pedidos en la búsqueda a los que el usuario puede
     *   consultar. Sin tipos pedidos, devuelve todos los permitidos; con tipos pedidos, solo los
     *   permitidos de esa lista. Si no queda ninguno, rechaza la consulta con 403.
     *
     * @param pvo_LsIdTiposSolicitados tipos de trámite pedidos (puede ser nulo o vacío)
     * @return tipos que se deben consultar; vacío significa "todos" (solo con REPORTE_CONSULTAR)
     */
    @Transactional(readOnly = true)
    public List<Integer> RestringirTiposTramite(List<Integer> pvo_LsIdTiposSolicitados) {
        List<Integer> vlo_LsSolicitados = pvo_LsIdTiposSolicitados == null ? List.of() : pvo_LsIdTiposSolicitados;
        if (this.PuedeConsultarTodosLosTipos()) {
            return vlo_LsSolicitados;
        }
        Set<Integer> vlo_Permitidos = this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerLista()
                .stream()
                .filter(vlo_Tipo -> this.PuedeConsultarTipo(vlo_Tipo.getCodigo()))
                .map(EntTipoTramite::getIdTipoTramite)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<Integer> vlo_LsResultado = vlo_LsSolicitados.isEmpty()
                ? List.copyOf(vlo_Permitidos)
                : vlo_LsSolicitados.stream().filter(vlo_Permitidos::contains).distinct().toList();
        if (vlo_LsResultado.isEmpty()) {
            throw new ExcepcionAutenticacion(
                    HttpStatus.FORBIDDEN, ConstantesSeguridad.MENSAJE_TIPOS_TRAMITE_NO_PERMITIDOS);
        }
        return vlo_LsResultado;
    }

    // -----------------------------------------------------------------------
    // Apoyo.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Revisa si la sesión tiene el permiso que corresponde al tipo de trámite.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @param pvo_Permiso permiso del tipo que se exige (consultar o modificar)
     * @return verdadero si la sesión tiene ese permiso
     */
    private boolean TienePermisoSobreTipo(Integer pvn_IdTipoTramite, Function<AccesoTipoTramite, String> pvo_Permiso) {
        EntTipoTramite vlo_Tipo = pvn_IdTipoTramite == null
                ? null
                : this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(pvn_IdTipoTramite);
        AccesoTipoTramite vlo_Acceso = vlo_Tipo == null ? null : AccesoTipoTramite.DesdeCodigo(vlo_Tipo.getCodigo());
        return vlo_Acceso != null && this.PermisosDeLaSesion().contains(pvo_Permiso.apply(vlo_Acceso));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 19/10/2026
     * Descripción: Permisos (y roles, con prefijo ROLE_) de la sesión actual, tal como los cargó
     *   FiltroAutenticacionToken.
     *
     * @return códigos de las autoridades de la sesión; vacío si no hay sesión
     */
    private Set<String> PermisosDeLaSesion() {
        Authentication vlo_Autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (vlo_Autenticacion == null || !vlo_Autenticacion.isAuthenticated()) {
            return Set.of();
        }
        return vlo_Autenticacion.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
