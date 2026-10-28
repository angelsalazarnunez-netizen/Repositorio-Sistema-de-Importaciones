package cr.ac.ucr.sim.utilerias.modelo.exoneraciones;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Fila de la bandeja de solicitudes de exoneración pendientes de aprobación (HU-022).
 */
public class ModeloAprobacionPendienteRespuesta {

    private Integer vgn_IdTramite;
    private String vgc_ReferenciaInterna;
    private String vgc_TipoExoneracion;
    private String vgc_NumeroContratacion;
    private String vgc_EnviadoPor;
    private LocalDateTime vgd_FechaEnvio;

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public String getTipoExoneracion() {
        return this.vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(String pvc_TipoExoneracion) {
        this.vgc_TipoExoneracion = pvc_TipoExoneracion;
    }

    public String getNumeroContratacion() {
        return this.vgc_NumeroContratacion;
    }

    public void setNumeroContratacion(String pvc_NumeroContratacion) {
        this.vgc_NumeroContratacion = pvc_NumeroContratacion;
    }

    public String getEnviadoPor() {
        return this.vgc_EnviadoPor;
    }

    public void setEnviadoPor(String pvc_EnviadoPor) {
        this.vgc_EnviadoPor = pvc_EnviadoPor;
    }

    public LocalDateTime getFechaEnvio() {
        return this.vgd_FechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime pvd_FechaEnvio) {
        this.vgd_FechaEnvio = pvd_FechaEnvio;
    }
}
