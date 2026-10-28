package cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Entidad de negocio de la tabla SIMT_AUTORIZACION_EXONET (HU-023). Representa una
 *   solicitud / autorización EXONET de un expediente de exoneración, con sus dos identificadores
 *   (número de solicitud y número de autorización) y sus tres hitos (envío, autorización y
 *   comunicación al interesado). Nace vigente (VIG) y solo puede pasar a anulada (ANU) o
 *   sustituida (SUS); los registros nunca se borran. Una sustituta apunta a la autorización que
 *   reemplazó mediante ID_AUTORIZACION_ANTERIOR.
 *   No se devuelve directamente desde los servicios REST: se expone mediante modelos de respuesta.
 *
 *   La columna generada ID_TRAMITE_VIGENTE no se mapea: la calcula MySQL a partir del estado y
 *   solo existe para garantizar, con su índice único, una única autorización vigente por
 *   expediente. La vigente se determina por ESTADO_AUTORIZACION.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Entity
@Table(name = "SIMT_AUTORIZACION_EXONET")
public class EntAutorizacionExonet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AUTORIZACION_EXONET")
    private Long vgn_IdAutorizacionExonet;

    // El expediente, el enlace con la autorización sustituida, quien registra y la fecha de
    // registro no cambian después de insertar: Hibernate los excluye de toda actualización.
    @Column(name = "ID_TRAMITE", nullable = false, updatable = false)
    private Integer vgn_IdTramite;

    @Column(name = "NUMERO_SOLICITUD_EXONET", length = 45)
    private String vgc_NumeroSolicitudExonet;

    @Column(name = "FECHA_ENVIO_EXONET")
    private LocalDate vgd_FechaEnvioExonet;

    @Column(name = "NUMERO_AUTORIZACION", length = 45)
    private String vgc_NumeroAutorizacion;

    @Column(name = "FECHA_AUTORIZACION")
    private LocalDate vgd_FechaAutorizacion;

    @Column(name = "FECHA_COMUNICACION")
    private LocalDate vgd_FechaComunicacion;

    @Column(name = "ID_DOCUMENTO")
    private Integer vgn_IdDocumento;

    @Column(name = "OBSERVACIONES", length = 1000)
    private String vgc_Observaciones;

    @Column(name = "ESTADO_AUTORIZACION", nullable = false, length = 3)
    private String vgc_EstadoAutorizacion;

    @Column(name = "ID_AUTORIZACION_ANTERIOR", updatable = false)
    private Long vgn_IdAutorizacionAnterior;

    // La columna es BIGINT; se mapea como Integer igual que EntUsuario y el usuario autenticado.
    @Column(name = "ID_USUARIO_REGISTRO", nullable = false, updatable = false)
    private Integer vgn_IdUsuarioRegistro;

    @Column(name = "FECHA_REGISTRO", nullable = false, updatable = false)
    private LocalDateTime vgd_FechaRegistro;

    // La columna es BIGINT; se mapea como Integer igual que EntUsuario y el usuario autenticado.
    @Column(name = "ID_USUARIO_CAMBIO_ESTADO")
    private Integer vgn_IdUsuarioCambioEstado;

    @Column(name = "FECHA_CAMBIO_ESTADO")
    private LocalDateTime vgd_FechaCambioEstado;

    @Column(name = "MOTIVO_CAMBIO_ESTADO", length = 500)
    private String vgc_MotivoCambioEstado;

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Asigna la fecha de registro cuando no viene indicada. El estado lo asigna la
     *   lógica de negocio, que es la dueña del ciclo vigente / anulada / sustituida.
     */
    @PrePersist
    public void AsignarFechaRegistro() {
        if (this.vgd_FechaRegistro == null) {
            this.vgd_FechaRegistro = LocalDateTime.now();
        }
    }

    public Long getIdAutorizacionExonet() {
        return this.vgn_IdAutorizacionExonet;
    }

    public void setIdAutorizacionExonet(Long pvn_IdAutorizacionExonet) {
        this.vgn_IdAutorizacionExonet = pvn_IdAutorizacionExonet;
    }

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public void setIdTramite(Integer pvn_IdTramite) {
        this.vgn_IdTramite = pvn_IdTramite;
    }

    public String getNumeroSolicitudExonet() {
        return this.vgc_NumeroSolicitudExonet;
    }

    public void setNumeroSolicitudExonet(String pvc_NumeroSolicitudExonet) {
        this.vgc_NumeroSolicitudExonet = pvc_NumeroSolicitudExonet;
    }

    public LocalDate getFechaEnvioExonet() {
        return this.vgd_FechaEnvioExonet;
    }

    public void setFechaEnvioExonet(LocalDate pvd_FechaEnvioExonet) {
        this.vgd_FechaEnvioExonet = pvd_FechaEnvioExonet;
    }

    public String getNumeroAutorizacion() {
        return this.vgc_NumeroAutorizacion;
    }

    public void setNumeroAutorizacion(String pvc_NumeroAutorizacion) {
        this.vgc_NumeroAutorizacion = pvc_NumeroAutorizacion;
    }

    public LocalDate getFechaAutorizacion() {
        return this.vgd_FechaAutorizacion;
    }

    public void setFechaAutorizacion(LocalDate pvd_FechaAutorizacion) {
        this.vgd_FechaAutorizacion = pvd_FechaAutorizacion;
    }

    public LocalDate getFechaComunicacion() {
        return this.vgd_FechaComunicacion;
    }

    public void setFechaComunicacion(LocalDate pvd_FechaComunicacion) {
        this.vgd_FechaComunicacion = pvd_FechaComunicacion;
    }

    public Integer getIdDocumento() {
        return this.vgn_IdDocumento;
    }

    public void setIdDocumento(Integer pvn_IdDocumento) {
        this.vgn_IdDocumento = pvn_IdDocumento;
    }

    public String getObservaciones() {
        return this.vgc_Observaciones;
    }

    public void setObservaciones(String pvc_Observaciones) {
        this.vgc_Observaciones = pvc_Observaciones;
    }

    public String getEstadoAutorizacion() {
        return this.vgc_EstadoAutorizacion;
    }

    public void setEstadoAutorizacion(String pvc_EstadoAutorizacion) {
        this.vgc_EstadoAutorizacion = pvc_EstadoAutorizacion;
    }

    public Long getIdAutorizacionAnterior() {
        return this.vgn_IdAutorizacionAnterior;
    }

    public void setIdAutorizacionAnterior(Long pvn_IdAutorizacionAnterior) {
        this.vgn_IdAutorizacionAnterior = pvn_IdAutorizacionAnterior;
    }

    public Integer getIdUsuarioRegistro() {
        return this.vgn_IdUsuarioRegistro;
    }

    public void setIdUsuarioRegistro(Integer pvn_IdUsuarioRegistro) {
        this.vgn_IdUsuarioRegistro = pvn_IdUsuarioRegistro;
    }

    public LocalDateTime getFechaRegistro() {
        return this.vgd_FechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime pvd_FechaRegistro) {
        this.vgd_FechaRegistro = pvd_FechaRegistro;
    }

    public Integer getIdUsuarioCambioEstado() {
        return this.vgn_IdUsuarioCambioEstado;
    }

    public void setIdUsuarioCambioEstado(Integer pvn_IdUsuarioCambioEstado) {
        this.vgn_IdUsuarioCambioEstado = pvn_IdUsuarioCambioEstado;
    }

    public LocalDateTime getFechaCambioEstado() {
        return this.vgd_FechaCambioEstado;
    }

    public void setFechaCambioEstado(LocalDateTime pvd_FechaCambioEstado) {
        this.vgd_FechaCambioEstado = pvd_FechaCambioEstado;
    }

    public String getMotivoCambioEstado() {
        return this.vgc_MotivoCambioEstado;
    }

    public void setMotivoCambioEstado(String pvc_MotivoCambioEstado) {
        this.vgc_MotivoCambioEstado = pvc_MotivoCambioEstado;
    }
}