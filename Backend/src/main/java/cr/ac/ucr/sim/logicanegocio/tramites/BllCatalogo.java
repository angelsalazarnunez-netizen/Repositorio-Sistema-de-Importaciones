package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 26/09/2026
 * Descripción: Entrega los catálogos que necesitan las pantallas: los tipos de trámite, con su
 *   sigla, y los tipos de documento con los que se clasifica la documentación del expediente.
 *   Así las pantallas dejan de repetir los códigos y siguen al catálogo cuando este cambia.
 *
 * Historial de cambios:
 *   26/09/2026 Katherine Susana Valverde Fallas: creación de la clase.
 */
@Service
public class BllCatalogo {

    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalTipoDocumento vgo_DalTipoDocumento;

    public BllCatalogo(DalTipoTramite pvo_DalTipoTramite, DalTipoDocumento pvo_DalTipoDocumento) {
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalTipoDocumento = pvo_DalTipoDocumento;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Obtiene los tipos de trámite vigentes del catálogo.
     *
     * @return tipos de trámite activos, ordenados por nombre
     */
    @Transactional(readOnly = true)
    public List<EntTipoTramite> ObtenerTiposTramite() {
        return this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerLista();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Obtiene los tipos de documento vigentes del catálogo.
     *
     * @return tipos de documento activos, ordenados por nombre
     */
    @Transactional(readOnly = true)
    public List<EntTipoDocumento> ObtenerTiposDocumento() {
        return this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerLista();
    }
}
