package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Solicitud / autorización EXONET de un expediente tal como la muestra la pantalla
 *   (HU-023), con los nombres de usuario y el documento ya resueltos. Representa tanto la
 *   vigente como cada fila del historial. La cadena de sustituciones se recorre con
 *   idAutorizacionAnterior (dato registrado) e idAutorizacionSustituta (dato calculado).
 *   No expone identificadores de usuario ni la ruta física del documento.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
public class ModeloAutorizacionExonetRespuesta {

    private Long vgn_IdAutorizacion;
    private Integer vgn_IdTramite;
    private String vgc_NumeroSolicitudExonet;
    private LocalDate vgd_FechaEnvioExonet;
    private String vgc_NumeroAutorizacion;
    private LocalDate vgd_FechaAutorizacion;
    private LocalDate vgd_FechaComunicacion;
    private String vgc_Observaciones;
    private String vgc_Estado;
    private String vgc_NombreEstado;
    private Boolean vgb_Vigente;
    private Long vgn_IdAutorizacionAnterior;
    private Long vgn_IdAutorizacionSustituta;
    private String vgc_UsuarioRegistro;
    private LocalDateTime vgd_FechaRegistro;
    private String vgc_UsuarioCambioEstado;
    private LocalDateTime vgd_FechaCambioEstado;
    private String vgc_MotivoCambioEstado;
    private Integer vgn_IdDocumento;
    private String vgc_NombreArchivoDocumento;
    private String vgc_EstadoDocumento;

    public ModeloAutorizacionExonetRespuesta() {
    }

    /**
     * Identificador de la autorización.
     */
    public Long getIdAutorizacion() {
        return this.vgn_IdAutorizacion;
    }

    public void setIdAutorizacion(Long pvn_IdAutorizacion) {
        this.vgn_IdAutorizacion = pvn_IdAutorizacion;
    }

    /**
     * Expediente de exoneración al que pertenece.
     */
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    /**
     * Número de solicitud EXONET, o null.
     */
    public String getNumeroSolicitudExonet() {
        return this.vgc_NumeroSolicitudExonet;
    }

    public void setNumeroSolicitudExonet(String pvc_NumeroSolicitudExonet) {
        this.vgc_NumeroSolicitudExonet = pvc_NumeroSolicitudExonet;
    }

    /**
     * Fecha de envío o presentación en EXONET, o null.
     */
    public LocalDate getFechaEnvioExonet() {
        return this.vgd_FechaEnvioExonet;
    }

    public void setFechaEnvioExonet(LocalDate pvd_FechaEnvioExonet) {
        this.vgd_FechaEnvioExonet = pvd_FechaEnvioExonet;
    }

    /**
     * Número de la autorización externa, o null.
     */
    public String getNumeroAutorizacion() {
        return this.vgc_NumeroAutorizacion;
    }

    public void setNumeroAutorizacion(String pvc_NumeroAutorizacion) {
        this.vgc_NumeroAutorizacion = pvc_NumeroAutorizacion;
    }

    /**
     * Fecha de la autorización externa, o null.
     */
    public LocalDate getFechaAutorizacion() {
        return this.vgd_FechaAutorizacion;
    }

    public void setFechaAutorizacion(LocalDate pvd_FechaAutorizacion) {
        this.vgd_FechaAutorizacion = pvd_FechaAutorizacion;
    }

    /**
     * Fecha de comunicación al interesado, o null.
     */
    public LocalDate getFechaComunicacion() {
        return this.vgd_FechaComunicacion;
    }

    public void setFechaComunicacion(LocalDate pvd_FechaComunicacion) {
        this.vgd_FechaComunicacion = pvd_FechaComunicacion;
    }

    /**
     * Observaciones, o null.
     */
    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
    }

    /**
     * Código del estado: VIG, ANU o SUS.
     */
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    /**
     * Nombre visible del estado: Vigente, Anulada o Sustituida.
     */
    public String getNombreEstado() {
        return this.vgc_NombreEstado;
    }

    public void setNombreEstado(String pvc_NombreEstado) {
        this.vgc_NombreEstado = pvc_NombreEstado;
    }

    /**
     * true solo para la autorización vigente: la única que puede completarse, anularse o sustituirse.
     */
    public Boolean getVigente() {
        return this.vgb_Vigente;
    }

    public void setVigente(Boolean pvb_Vigente) {
        this.vgb_Vigente = pvb_Vigente;
    }

    /**
     * Autorización que esta sustituyó, o null si no sustituyó a ninguna.
     */
    public Long getIdAutorizacionAnterior() {
        return this.vgn_IdAutorizacionAnterior;
    }

    public void setIdAutorizacionAnterior(Long pvn_IdAutorizacionAnterior) {
        this.vgn_IdAutorizacionAnterior = pvn_IdAutorizacionAnterior;
    }

    /**
     * Autorización que sustituyó a esta, o null. Se calcula a partir del historial: es la fila cuyo idAutorizacionAnterior apunta a esta.
     */
    public Long getIdAutorizacionSustituta() {
        return this.vgn_IdAutorizacionSustituta;
    }

    public void setIdAutorizacionSustituta(Long pvn_IdAutorizacionSustituta) {
        this.vgn_IdAutorizacionSustituta = pvn_IdAutorizacionSustituta;
    }

    /**
     * Nombre completo de quien la registró.
     */
    public String getUsuarioRegistro() {
        return this.vgc_UsuarioRegistro;
    }

    public void setUsuarioRegistro(String pvc_UsuarioRegistro) {
        this.vgc_UsuarioRegistro = pvc_UsuarioRegistro;
    }

    /**
     * Fecha y hora del registro.
     */
    public LocalDateTime getFechaRegistro() {
        return this.vgd_FechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime pvd_FechaRegistro) {
        this.vgd_FechaRegistro = pvd_FechaRegistro;
    }

    /**
     * Nombre completo de quien la anuló o sustituyó, o null si sigue vigente.
     */
    public String getUsuarioCambioEstado() {
        return this.vgc_UsuarioCambioEstado;
    }

    public void setUsuarioCambioEstado(String pvc_UsuarioCambioEstado) {
        this.vgc_UsuarioCambioEstado = pvc_UsuarioCambioEstado;
    }

    /**
     * Fecha y hora de la anulación o sustitución, o null si sigue vigente. En una sustitución coincide con la fecha de registro de la sustituta.
     */
    public LocalDateTime getFechaCambioEstado() {
        return this.vgd_FechaCambioEstado;
    }

    public void setFechaCambioEstado(LocalDateTime pvd_FechaCambioEstado) {
        this.vgd_FechaCambioEstado = pvd_FechaCambioEstado;
    }

    /**
     * Motivo de la anulación o sustitución, o null si sigue vigente.
     */
    public String getMotivoCambioEstado() {
        return this.vgc_MotivoCambioEstado;
    }

    public void setMotivoCambioEstado(String pvc_MotivoCambioEstado) {
        this.vgc_MotivoCambioEstado = pvc_MotivoCambioEstado;
    }

    /**
     * Documento de respaldo asociado, o null. Se descarga por su identificador con el servicio de documentos existente.
     */
    public Integer getIdDocumento() {
        return this.vgn_IdDocumento;
    }

    public void setIdDocumento(Integer pvn_IdDocumento) {
        this.vgn_IdDocumento = pvn_IdDocumento;
    }

    /**
     * Nombre del archivo del documento asociado, o null.
     */
    public String getNombreArchivoDocumento() {
        return this.vgc_NombreArchivoDocumento;
    }

    public void setNombreArchivoDocumento(String pvc_NombreArchivoDocumento) {
        this.vgc_NombreArchivoDocumento = pvc_NombreArchivoDocumento;
    }

    /**
     * Estado del documento asociado (ACT o INA), o null. Un documento inactivo sigue disponible: forma parte del historial.
     */
    public String getEstadoDocumento() {
        return this.vgc_EstadoDocumento;
    }

    public void setEstadoDocumento(String pvc_EstadoDocumento) {
        this.vgc_EstadoDocumento = pvc_EstadoDocumento;
    }
}
