package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Documento del expediente tal como se muestra en la pantalla de seguimiento.
 *   No expone la ruta física del archivo: la descarga se solicita por identificador.
 */
public class EntDocumentoExpediente {

    private final Integer vgn_IdDocumento;
    private final String vgc_NombreArchivo;
    private final String vgc_TipoDocumento;
    private final Short vgn_Version;
    private final String vgc_EstadoDocumento;
    private final LocalDateTime vgd_FechaCarga;

    public EntDocumentoExpediente(
            Integer pvn_IdDocumento,
            String pvc_NombreArchivo,
            String pvc_TipoDocumento,
            Short pvn_Version,
            String pvc_EstadoDocumento,
            LocalDateTime pvd_FechaCarga) {
        this.vgn_IdDocumento = pvn_IdDocumento;
        this.vgc_NombreArchivo = pvc_NombreArchivo;
        this.vgc_TipoDocumento = pvc_TipoDocumento;
        this.vgn_Version = pvn_Version;
        this.vgc_EstadoDocumento = pvc_EstadoDocumento;
        this.vgd_FechaCarga = pvd_FechaCarga;
    }

    @JsonProperty("idDocumento")
    public Integer getIdDocumento() {
        return this.vgn_IdDocumento;
    }

    @JsonProperty("nombreArchivo")
    public String getNombreArchivo() {
        return this.vgc_NombreArchivo;
    }

    @JsonProperty("tipoDocumento")
    public String getTipoDocumento() {
        return this.vgc_TipoDocumento;
    }

    @JsonProperty("version")
    public Short getVersion() {
        return this.vgn_Version;
    }

    @JsonProperty("estadoDocumento")
    public String getEstadoDocumento() {
        return this.vgc_EstadoDocumento;
    }

    @JsonProperty("fechaCarga")
    public LocalDateTime getFechaCarga() {
        return this.vgd_FechaCarga;
    }
}
