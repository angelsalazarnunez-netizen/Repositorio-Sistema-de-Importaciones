package cr.ac.ucr.sim.entidadnegocio.seguridad;

import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Representa un rol registrado dentro del Sistema de Importaciones.
 */
@Entity
@Table(
        name = "SIMM_ROL",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "AK_SIMM_ROL",
                        columnNames = "NOMBRE"
                )
        }
)
public class EntRol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROL")
    private Long vgn_IdRol;

    @Column(name = "NOMBRE", nullable = false, length = 50)
    private String vgc_Nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String vgc_Descripcion;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado;

    @Column(name = "TIME_STAMP", nullable = false)
    private LocalDateTime vgd_TimeStamp;

    public EntRol() {
    }

    /**
     * asigna valores iniciales antes
     * de registrar un rol.
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
     * actualiza el timestamp del rol antes de modificar
     */
    @PreUpdate
    protected void AntesDeActualizar() {
        vgd_TimeStamp = LocalDateTime.now();
    }

    public Long getIdRol() {
        return vgn_IdRol;
    }

    public void setIdRol(Long vln_IdRol) {
        this.vgn_IdRol = vln_IdRol;
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