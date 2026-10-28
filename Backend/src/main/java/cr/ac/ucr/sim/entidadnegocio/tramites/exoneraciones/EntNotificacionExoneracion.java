package cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Entidad de negocio de la tabla SIMT_NOTIFICACION_EXONERACION (HU-022). Es la
 *   notificación que recibe la técnica responsable por cada decisión de la jefatura. Se registra
 *   junto con la decisión y su ESTADO_ENVIO indica si el correo salió: PEN = pendiente,
 *   ENV = enviado, ERR = no se pudo enviar.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Entity
@Table(name = "SIMT_NOTIFICACION_EXONERACION")
public class EntNotificacionExoneracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICACION")
    private Integer vgn_IdNotificacion;

    @Column(name = "ID_DECISION", nullable = false, updatable = false)
    private Integer vgn_IdDecision;

    @Column(name = "ID_USUARIO_DESTINO", nullable = false, updatable = false)
    private Integer vgn_IdUsuarioDestino;

    @Column(name = "CORREO_DESTINO", updatable = false, length = 120)
    private String vgc_CorreoDestino;

    @Column(name = "ASUNTO", nullable = false, updatable = false, length = 150)
    private String vgc_Asunto;

    @Column(name = "MENSAJE", nullable = false, updatable = false, length = 2000)
    private String vgc_Mensaje;

    @Column(name = "ESTADO_ENVIO", nullable = false, length = 3)
    private String vgc_EstadoEnvio;

    @Column(name = "FECHA_CREACION", nullable = false, updatable = false)
    private LocalDateTime vgd_FechaCreacion;

    @Column(name = "FECHA_ENVIO")
    private LocalDateTime vgd_FechaEnvio;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Asigna la fecha de creación, el estado de envío y el estado de registro cuando
     *   no vienen indicados.
     */
    @PrePersist
    public void AsignarValoresPorDefecto() {
        if (this.vgd_FechaCreacion == null) {
            this.vgd_FechaCreacion = LocalDateTime.now();
        }
        if (this.vgc_EstadoEnvio == null) {
            this.vgc_EstadoEnvio = "PEN";
        }
        if (this.vgc_Estado == null) {
            this.vgc_Estado = "ACT";
        }
    }

    public Integer getIdNotificacion() {
        return this.vgn_IdNotificacion;
    }

    public void setIdNotificacion(Integer pvn_IdNotificacion) {
        this.vgn_IdNotificacion = pvn_IdNotificacion;
    }

    public Integer getIdDecision() {
        return this.vgn_IdDecision;
    }

    public void setIdDecision(Integer pvn_IdDecision) {
        this.vgn_IdDecision = pvn_IdDecision;
    }

    public Integer getIdUsuarioDestino() {
        return this.vgn_IdUsuarioDestino;
    }

    public void setIdUsuarioDestino(Integer pvn_IdUsuarioDestino) {
        this.vgn_IdUsuarioDestino = pvn_IdUsuarioDestino;
    }

    public String getCorreoDestino() {
        return this.vgc_CorreoDestino;
    }

    public void setCorreoDestino(String pvc_CorreoDestino) {
        this.vgc_CorreoDestino = pvc_CorreoDestino;
    }

    public String getAsunto() {
        return this.vgc_Asunto;
    }

    public void setAsunto(String pvc_Asunto) {
        this.vgc_Asunto = pvc_Asunto;
    }

    public String getMensaje() {
        return this.vgc_Mensaje;
    }

    public void setMensaje(String pvc_Mensaje) {
        this.vgc_Mensaje = pvc_Mensaje;
    }

    public String getEstadoEnvio() {
        return this.vgc_EstadoEnvio;
    }

    public void setEstadoEnvio(String pvc_EstadoEnvio) {
        this.vgc_EstadoEnvio = pvc_EstadoEnvio;
    }

    public LocalDateTime getFechaCreacion() {
        return this.vgd_FechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime pvd_FechaCreacion) {
        this.vgd_FechaCreacion = pvd_FechaCreacion;
    }

    public LocalDateTime getFechaEnvio() {
        return this.vgd_FechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime pvd_FechaEnvio) {
        this.vgd_FechaEnvio = pvd_FechaEnvio;
    }

    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }
}
