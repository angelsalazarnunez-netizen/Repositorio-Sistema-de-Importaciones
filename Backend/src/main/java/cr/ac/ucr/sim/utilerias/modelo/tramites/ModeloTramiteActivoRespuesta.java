package cr.ac.ucr.sim.utilerias.modelo.tramites;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Fila del panel de trámites activos (HU-030): referencia, motivo, DUA, valor declarado, estado,
 *   grupo del panel y cuenta regresiva del vencimiento cuando el trámite es temporal.
 */
public class ModeloTramiteActivoRespuesta {

    private Integer vgn_IdTramite;
    private String vgc_ReferenciaInterna;
    private String vgc_CodigoTipoTramite;
    private String vgc_TipoTramite;
    private String vgc_Motivo;
    private String vgc_NumeroDua;
    private BigDecimal vgn_ValorDeclarado;
    private String vgc_Moneda;
    private String vgc_Estado;
    private String vgc_GrupoPanel;
    private String vgc_NombreGrupoPanel;
    private LocalDate vgd_FechaVencimiento;
    private Long vgn_DiasRestantes;
    private String vgc_NivelVencimiento;
    private LocalDateTime vgd_FechaRegistro;
    private String vgc_Responsable;

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

    public String getCodigoTipoTramite() {
        return this.vgc_CodigoTipoTramite;
    }

    public void setCodigoTipoTramite(String pvc_CodigoTipoTramite) {
        this.vgc_CodigoTipoTramite = pvc_CodigoTipoTramite;
    }

    public String getTipoTramite() {
        return this.vgc_TipoTramite;
    }

    public void setTipoTramite(String pvc_TipoTramite) {
        this.vgc_TipoTramite = pvc_TipoTramite;
    }

    public String getMotivo() {
        return this.vgc_Motivo;
    }

    public void setMotivo(String pvc_Motivo) {
        this.vgc_Motivo = pvc_Motivo;
    }

    public String getNumeroDua() {
        return this.vgc_NumeroDua;
    }

    public void setNumeroDua(String pvc_NumeroDua) {
        this.vgc_NumeroDua = pvc_NumeroDua;
    }

    public BigDecimal getValorDeclarado() {
        return this.vgn_ValorDeclarado;
    }

    public void setValorDeclarado(BigDecimal pvn_ValorDeclarado) {
        this.vgn_ValorDeclarado = pvn_ValorDeclarado;
    }

    public String getMoneda() {
        return this.vgc_Moneda;
    }

    public void setMoneda(String pvc_Moneda) {
        this.vgc_Moneda = pvc_Moneda;
    }

    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    public String getGrupoPanel() {
        return this.vgc_GrupoPanel;
    }

    public void setGrupoPanel(String pvc_GrupoPanel) {
        this.vgc_GrupoPanel = pvc_GrupoPanel;
    }

    public String getNombreGrupoPanel() {
        return this.vgc_NombreGrupoPanel;
    }

    public void setNombreGrupoPanel(String pvc_NombreGrupoPanel) {
        this.vgc_NombreGrupoPanel = pvc_NombreGrupoPanel;
    }

    public LocalDate getFechaVencimiento() {
        return this.vgd_FechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate pvd_FechaVencimiento) {
        this.vgd_FechaVencimiento = pvd_FechaVencimiento;
    }

    public Long getDiasRestantes() {
        return this.vgn_DiasRestantes;
    }

    public void setDiasRestantes(Long pvn_DiasRestantes) {
        this.vgn_DiasRestantes = pvn_DiasRestantes;
    }

    public String getNivelVencimiento() {
        return this.vgc_NivelVencimiento;
    }

    public void setNivelVencimiento(String pvc_NivelVencimiento) {
        this.vgc_NivelVencimiento = pvc_NivelVencimiento;
    }

    public LocalDateTime getFechaRegistro() {
        return this.vgd_FechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime pvd_FechaRegistro) {
        this.vgd_FechaRegistro = pvd_FechaRegistro;
    }

    public String getResponsable() {
        return this.vgc_Responsable;
    }

    public void setResponsable(String pvc_Responsable) {
        this.vgc_Responsable = pvc_Responsable;
    }
}
