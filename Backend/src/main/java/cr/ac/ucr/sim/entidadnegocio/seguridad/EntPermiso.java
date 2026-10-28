package cr.ac.ucr.sim.entidadnegocio.seguridad;

import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Descripcion: representa los permisos, asociados a funcionalidades dentro
 * del Sistema de Importaciones.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: se agrega el módulo de acceso al que pertenece el permiso (columna MODULO,
 *     script 16-Modulos-Permisos-Roles-Base.sql; valores en Enumerados.ModuloPermiso).
 */
@Entity
@Table(
        name = "SIMM_PERMISO",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "AK_SIMM_PERMISO",
                        columnNames = "CODIGO"
                )
        }
)
public class EntPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERMISO")
    private Long vgn_IdPermiso;

    @Column(name = "CODIGO", nullable = false, length = 60)
    private String vgc_Codigo;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String vgc_Nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String vgc_Descripcion;

    @Column(name = "MODULO", length = 30)
    private String vgc_Modulo;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @Column(name = "TIME_STAMP", nullable = false)
    private LocalDateTime vgd_TimeStamp;

    public EntPermiso() {
    }

    /**
     * Descripcion: asigna valores iniciales antes
     * de almacenar un permiso.
     */
    @PrePersist
    protected void AntesDeGuardar() {
        if (vgc_Estado == null || vgc_Estado.isBlank()) {
            vgc_Estado = ConstantesSeguridad.ESTADO_ACTIVO;
        }

        if (vgd_TimeStamp == null) {
            vgd_TimeStamp = LocalDateTime.now();
        }
    }

    /**
     * Descripcion: actualiza el timestamp del permiso antes
     * de modificar.
     */
    @PreUpdate
    protected void AntesDeActualizar() {
        vgd_TimeStamp = LocalDateTime.now();
    }

    public Long getIdPermiso() {
        return vgn_IdPermiso;
    }

    public void setIdPermiso(Long vln_IdPermiso) {
        this.vgn_IdPermiso = vln_IdPermiso;
    }

    public String getCodigo() {
        return vgc_Codigo;
    }

    public void setCodigo(String vlc_Codigo) {
        this.vgc_Codigo = vlc_Codigo;
    }

    public String getNombre() {
        return vgc_Nombre;
    }

    public void setNombre(String vlc_Nombre) {
        this.vgc_Nombre = vlc_Nombre;
    }

    public String getDescripcion() {
        return vgc_Descripcion;
    }

    public void setDescripcion(String vlc_Descripcion) {
        this.vgc_Descripcion = vlc_Descripcion;
    }

    public String getModulo() {
        return vgc_Modulo;
    }

    public void setModulo(String vlc_Modulo) {
        this.vgc_Modulo = vlc_Modulo;
    }

    public String getEstado() {
        return vgc_Estado;
    }

    public void setEstado(String vlc_Estado) {
        this.vgc_Estado = vlc_Estado;
    }

    public LocalDateTime getTimeStamp() {
        return vgd_TimeStamp;
    }

    public void setTimeStamp(LocalDateTime vld_TimeStamp) {
        this.vgd_TimeStamp = vld_TimeStamp;
    }
}