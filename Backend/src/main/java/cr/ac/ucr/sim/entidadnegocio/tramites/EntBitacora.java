package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Entidad de negocio de la tabla SIML_BITACORA. Cada registro es una acción realizada
 *   sobre un expediente: su registro, la documentación agregada o un comentario.
 */
@Entity
@Table(name = "SIML_BITACORA")
public class EntBitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_BITACORA")
    private Integer vgn_IdBitacora;

    @Column(name = "ID_TRAMITE", nullable = false)
    private Integer vgn_IdTramite;

    @Column(name = "ID_USUARIO", nullable = false)
    private Long vgn_IdUsuario;

    @Column(name = "ACCION", nullable = false, length = 30)
    private String vgc_Accion;

    @Column(name = "ENTIDAD_AFECTADA", nullable = false, length = 45)
    private String vgc_EntidadAfectada;

    @Column(name = "ID_REGISTRO_AFECTADO")
    private Integer vgn_IdRegistroAfectado;

    @Column(name = "DATOS_ANTERIORES")
    private String vgc_DatosAnteriores;

    @Column(name = "DATOS_NUEVOS")
    private String vgc_DatosNuevos;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime vgd_FechaHora;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Asigna la fecha de la acción cuando no viene indicada.
     */
    @PrePersist
    public void AsignarFechaHora() {
        if (this.vgd_FechaHora == null) {
            this.vgd_FechaHora = LocalDateTime.now();
        }
    }

    @JsonProperty("idBitacora")
    public Integer getIdBitacora() {
        return this.vgn_IdBitacora;
    }

    public void setIdBitacora(Integer pvn_IdBitacora) {
        this.vgn_IdBitacora = pvn_IdBitacora;
    }

    @JsonProperty("idTramite")
    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    @JsonProperty("idUsuario")
    public Long getIdUsuario() { return this.vgn_IdUsuario;}

    public void setIdUsuario(Long vln_IdUsuario) { this.vgn_IdUsuario = vln_IdUsuario;}

    @JsonProperty("accion")
    public String getAccion() {
        return this.vgc_Accion;
    }

    public void setAccion(String pvc_Accion) {
        this.vgc_Accion = pvc_Accion;
    }

    @JsonProperty("entidadAfectada")
    public String getEntidadAfectada() {
        return this.vgc_EntidadAfectada;
    }

    public void setEntidadAfectada(String pvc_EntidadAfectada) {
        this.vgc_EntidadAfectada = pvc_EntidadAfectada;
    }

    @JsonProperty("idRegistroAfectado")
    public Integer getIdRegistroAfectado() {
        return this.vgn_IdRegistroAfectado;
    }

    public void setIdRegistroAfectado(Integer pvn_IdRegistroAfectado) {
        this.vgn_IdRegistroAfectado = pvn_IdRegistroAfectado;
    }

    @JsonProperty("datosAnteriores")
    public String getDatosAnteriores() {
        return this.vgc_DatosAnteriores;
    }

    public void setDatosAnteriores(String pvc_DatosAnteriores) {
        this.vgc_DatosAnteriores = pvc_DatosAnteriores;
    }

    @JsonProperty("datosNuevos")
    public String getDatosNuevos() {
        return this.vgc_DatosNuevos;
    }

    public void setDatosNuevos(String pvc_DatosNuevos) {
        this.vgc_DatosNuevos = pvc_DatosNuevos;
    }

    @JsonProperty("estado")
    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    @JsonProperty("fechaHora")
    public LocalDateTime getFechaHora() {
        return this.vgd_FechaHora;
    }

    public void setFechaHora(LocalDateTime pvd_FechaHora) {
        this.vgd_FechaHora = pvd_FechaHora;
    }
}
