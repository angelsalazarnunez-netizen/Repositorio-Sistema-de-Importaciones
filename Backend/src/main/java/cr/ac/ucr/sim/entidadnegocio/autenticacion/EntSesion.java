package cr.ac.ucr.sim.entidadnegocio.autenticacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "SIT_SESION")
public class EntSesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SESION")
    private Long vgn_IdSesion;

    @Column(name = "ID_USUARIO", nullable = false)
    private Integer vgn_IdUsuario;

    @Column(name = "TOKEN_HASH", nullable = false, unique = true, length = 255)
    private String vgc_TokenHash;

    @Column(name = "FECHA_CREACION", nullable = false)
    private LocalDateTime vgd_FechaCreacion;

    @Column(name = "FECHA_EXPIRACION", nullable = false)
    private LocalDateTime vgd_FechaExpiracion;

    @Column(name = "ES_ACTIVA", nullable = false)
    private Boolean vgb_EsActiva;

    @PrePersist
    public void AntesDeGuardar() {
        if (this.vgd_FechaCreacion == null) {
            this.vgd_FechaCreacion = LocalDateTime.now();
        }
        if (this.vgb_EsActiva == null) {
            this.vgb_EsActiva = true;
        }
    }

    public Long getIdSesion() {
        return this.vgn_IdSesion;
    }

    public Integer getIdUsuario() {
        return this.vgn_IdUsuario;
    }

    public void setIdUsuario(Integer pvn_IdUsuario) {
        this.vgn_IdUsuario = pvn_IdUsuario;
    }

    public String getTokenHash() {
        return this.vgc_TokenHash;
    }

    public void setTokenHash(String pvc_TokenHash) {
        this.vgc_TokenHash = pvc_TokenHash;
    }

    public LocalDateTime getFechaExpiracion() {
        return this.vgd_FechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime pvd_FechaExpiracion) {
        this.vgd_FechaExpiracion = pvd_FechaExpiracion;
    }

    public Boolean getEsActiva() {
        return this.vgb_EsActiva;
    }

    public void setEsActiva(Boolean pvb_EsActiva) {
        this.vgb_EsActiva = pvb_EsActiva;
    }
}
