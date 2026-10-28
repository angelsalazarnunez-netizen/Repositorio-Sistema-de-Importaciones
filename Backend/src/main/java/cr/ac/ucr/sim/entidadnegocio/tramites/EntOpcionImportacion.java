package cr.ac.ucr.sim.entidadnegocio.tramites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SIMM_OPCION_IMPORTACION")
public class EntOpcionImportacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_OPCION")
    private Integer vgn_IdOpcion;

    @Column(name = "TIPO_OPCION", nullable = false, length = 20)
    private String vgc_TipoOpcion;

    @Column(name = "VALOR", nullable = false, length = 160)
    private String vgc_Valor;

    @Column(name = "ESTADO", nullable = false, length = 3)
    private String vgc_Estado = "ACT";

    protected EntOpcionImportacion() {
    }

    public EntOpcionImportacion(String pvc_TipoOpcion, String pvc_Valor) {
        this.vgc_TipoOpcion = pvc_TipoOpcion;
        this.vgc_Valor = pvc_Valor;
    }
}
