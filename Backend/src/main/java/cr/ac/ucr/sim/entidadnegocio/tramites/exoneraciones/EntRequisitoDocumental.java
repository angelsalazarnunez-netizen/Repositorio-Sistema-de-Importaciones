package cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: representa la configuracion de un documento
 * requerido u opcional para un tipo y modalidad de tramite.
 */
@Entity
@Table(name = "SIMT_REQUISITO_DOCUMENTAL", uniqueConstraints = {
        @UniqueConstraint(name = "AK_SIMT_REQUISITO_DOCUMENTAL",
                columnNames = {"ID_TIPO_TRAMITE", "ID_TIPO_DOCUMENTO", "TIPO_EXONERACION"})})

public class EntRequisitoDocumental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_REQUISITO_DOCUMENTAL")
    private Integer vgn_IdRequisitoDocumental;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_TIPO_TRAMITE",
            nullable = false
    )
    private EntTipoTramite vgo_EnTipoTramite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ID_TIPO_DOCUMENTO",
            nullable = false
    )
    private EntTipoDocumento vgo_EnTipoDocumento;

    @Column(
            name = "TIPO_EXONERACION",
            length = 20
    )
    private String vgc_TipoExoneracion;

    @Column(
            name = "OBLIGATORIO",
            nullable = false
    )
    private Boolean vgb_Obligatorio;

    @Column(
            name = "ESTADO",
            nullable = false,
            length = 3
    )
    private String vgc_Estado;

    @Column(
            name = "TIME_STAMP",
            nullable = false
    )
    private LocalDateTime vgd_TimeStamp;

    public EntRequisitoDocumental() {
    }

    /**
     * Descripcion: asigna los valores por defecto
     * antes de crear el requisito documental.
     */
    @PrePersist
    protected void AntesDeGuardar() {

        if (vgb_Obligatorio == null) {
            vgb_Obligatorio = true;
        }

        if (vgc_Estado == null
                || vgc_Estado.isBlank()) {

            vgc_Estado = "ACT";
        }

        if (vgd_TimeStamp == null) {
            vgd_TimeStamp =
                    LocalDateTime.now();
        }
    }

    /**
     * Descripcion: actualiza la marca temporal
     * antes de modificar el requisito.
     */
    @PreUpdate
    protected void AntesDeActualizar() {
        vgd_TimeStamp =
                LocalDateTime.now();
    }

    public Integer getIdRequisitoDocumental() {
        return vgn_IdRequisitoDocumental;
    }

    public void setIdRequisitoDocumental(
            Integer vln_IdRequisitoDocumental) {

        this.vgn_IdRequisitoDocumental =
                vln_IdRequisitoDocumental;
    }

    public EntTipoTramite getTipoTramite() {
        return vgo_EnTipoTramite;
    }

    public void setTipoTramite(
            EntTipoTramite vlo_EnTipoTramite) {

        this.vgo_EnTipoTramite =
                vlo_EnTipoTramite;
    }

    public EntTipoDocumento getTipoDocumento() {
        return vgo_EnTipoDocumento;
    }

    public void setTipoDocumento(
            EntTipoDocumento vlo_EnTipoDocumento) {

        this.vgo_EnTipoDocumento =
                vlo_EnTipoDocumento;
    }

    public String getTipoExoneracion() {
        return vgc_TipoExoneracion;
    }

    public void setTipoExoneracion(
            String vlc_TipoExoneracion) {

        this.vgc_TipoExoneracion =
                vlc_TipoExoneracion;
    }

    public Boolean getObligatorio() {
        return vgb_Obligatorio;
    }

    public void setObligatorio(
            Boolean vlb_Obligatorio) {

        this.vgb_Obligatorio =
                vlb_Obligatorio;
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(
            String vlc_Estado) {

        this.vgc_Estado =
                vlc_Estado;
    }

    public LocalDateTime getTimeStamp() {
        return vgd_TimeStamp;
    }

    public void setTimeStamp(
            LocalDateTime vld_TimeStamp) {

        this.vgd_TimeStamp =
                vld_TimeStamp;
    }
}