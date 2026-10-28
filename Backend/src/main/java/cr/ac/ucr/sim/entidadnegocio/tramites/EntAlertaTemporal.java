package cr.ac.ucr.sim.entidadnegocio.tramites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Entidad de negocio de la tabla SIMT_ALERTA_TEMPORAL. Es la notificación en el
 *   sistema y la cola del correo de vencimiento de un trámite temporal (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la alerta referencia el expediente por ID_TRAMITE y se dirige a un usuario
 *     destinatario, que puede ser el responsable del expediente o un agente asignado (HU-025).
 */
@Entity
@Table(name = "SIMT_ALERTA_TEMPORAL")
public class EntAlertaTemporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ALERTA_TEMPORAL")
    private Integer vgn_IdAlertaTemporal;

    @Column(name = "ID_TRAMITE", nullable = false)
    private Integer vgn_IdTramite;

    @Column(name = "ID_USUARIO_DESTINO", nullable = false)
    private Integer vgn_IdUsuarioDestino;

    @Column(name = "FECHA_VENCIMIENTO", nullable = false)
    private LocalDate vgd_FechaVencimiento;

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDate vgd_FechaGeneracion;

    @Column(name = "ESTADO", nullable = false, length = 10)
    private String vgc_Estado;

    @Column(name = "ESTADO_CORREO", nullable = false, length = 10)
    private String vgc_EstadoCorreo;

    @Column(name = "FECHA_ENVIO_CORREO")
    private LocalDateTime vgd_FechaEnvioCorreo;

    public Integer getIdAlertaTemporal() {
        return this.vgn_IdAlertaTemporal;
    }

    public void setIdAlertaTemporal(Integer pvn_IdAlertaTemporal) {
        this.vgn_IdAlertaTemporal = pvn_IdAlertaTemporal;
    }

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public Integer getIdUsuarioDestino() {
        return this.vgn_IdUsuarioDestino;
    }

    public void setIdUsuarioDestino(Integer pvn_IdUsuarioDestino) {
        this.vgn_IdUsuarioDestino = pvn_IdUsuarioDestino;
    }

    public LocalDate getFechaVencimiento() {
        return this.vgd_FechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate pvd_FechaVencimiento) {
        this.vgd_FechaVencimiento = pvd_FechaVencimiento;
    }

    public LocalDate getFechaGeneracion() {
        return this.vgd_FechaGeneracion;
    }

    public void setFechaGeneracion(LocalDate pvd_FechaGeneracion) {
        this.vgd_FechaGeneracion = pvd_FechaGeneracion;
    }

    public String getEstado() {
        return this.vgc_Estado;
    }

    public void setEstado(String pvc_Estado) {
        this.vgc_Estado = pvc_Estado;
    }

    public String getEstadoCorreo() {
        return this.vgc_EstadoCorreo;
    }

    public void setEstadoCorreo(String pvc_EstadoCorreo) {
        this.vgc_EstadoCorreo = pvc_EstadoCorreo;
    }

    public LocalDateTime getFechaEnvioCorreo() {
        return this.vgd_FechaEnvioCorreo;
    }

    public void setFechaEnvioCorreo(LocalDateTime pvd_FechaEnvioCorreo) {
        this.vgd_FechaEnvioCorreo = pvd_FechaEnvioCorreo;
    }
}
