package cr.ac.ucr.sim.accesodatos.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumentoExpediente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Acceso a datos de SIMF_DOCUMENTO. No implementa reglas de negocio.
 */
@Repository
public class DalDocumento {

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Inserta un documento de respaldo del expediente.
     *
     * @param pvo_EntDocumento entidad a persistir
     */
    public void SIMF_DOCUMENTO_InsertarRegistro(EntDocumento pvo_EntDocumento) {
        this.vgo_EntityManager.persist(pvo_EntDocumento);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Actualiza un documento del expediente.
     *
     * @param pvo_EntDocumento entidad a actualizar
     */
    public void SIMF_DOCUMENTO_ModificarRegistro(EntDocumento pvo_EntDocumento) {
        this.vgo_EntityManager.merge(pvo_EntDocumento);
        this.vgo_EntityManager.flush();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene un documento por su identificador.
     *
     * @param pvn_IdDocumento identificador del documento
     * @return documento encontrado o null
     */
    public EntDocumento SIMF_DOCUMENTO_ObtenerRegistro(Integer pvn_IdDocumento) {
        return this.vgo_EntityManager.find(EntDocumento.class, pvn_IdDocumento);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene la documentación de un expediente con el nombre del tipo de documento
     *   ya resuelto, para la pantalla de seguimiento.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return documentos del expediente, del más antiguo al más reciente
     */
    public List<EntDocumentoExpediente> SIMF_DOCUMENTO_ObtenerListaExpediente(Integer pvn_IdTramite) {
        String vlc_Jpql = "SELECT NEW cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumentoExpediente("
                + "d.vgn_IdDocumento, d.vgc_NombreArchivo, td.vgc_Nombre, d.vgn_Version, "
                + "d.vgc_EstadoDocumento, d.vgd_FechaCarga) "
                + "FROM EntDocumento d, EntTipoDocumento td "
                + "WHERE td.vgn_IdTipoDocumento = d.vgn_IdTipoDocumento "
                + "AND d.vgn_IdTramite = :pvn_IdTramite "
                + "ORDER BY d.vgn_IdDocumento";
        TypedQuery<EntDocumentoExpediente> vlo_Consulta =
                this.vgo_EntityManager.createQuery(vlc_Jpql, EntDocumentoExpediente.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los códigos de los tipos de documento que un expediente tiene vigentes,
     *   para determinar qué documentación obligatoria sigue pendiente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvc_EstadoDocumento estado que identifica los documentos vigentes
     * @return códigos de tipo de documento, sin repetir
     */
    public List<String> SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(Integer pvn_IdTramite, String pvc_EstadoDocumento) {
        String vlc_Jpql = "SELECT DISTINCT td.vgc_Codigo "
                + "FROM EntDocumento d, EntTipoDocumento td "
                + "WHERE td.vgn_IdTipoDocumento = d.vgn_IdTipoDocumento "
                + "AND d.vgn_IdTramite = :pvn_IdTramite "
                + "AND d.vgc_EstadoDocumento = :pvc_EstadoDocumento";
        TypedQuery<String> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, String.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvc_EstadoDocumento", pvc_EstadoDocumento);
        return vlo_Consulta.getResultList();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 10/09/2026
     * Descripción: Obtiene la versión vigente de un documento del expediente que comparte tipo y
     *   nombre de archivo, para encadenar la nueva versión con la anterior.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdTipoDocumento identificador del tipo de documento
     * @param pvc_NombreArchivo nombre del archivo depurado
     * @param pvc_EstadoDocumento estado que identifica la versión vigente
     * @return documento vigente o null si es la primera versión
     */
    public EntDocumento SIMF_DOCUMENTO_ObtenerVersionVigente(
            Integer pvn_IdTramite,
            Integer pvn_IdTipoDocumento,
            String pvc_NombreArchivo,
            String pvc_EstadoDocumento) {
        String vlc_Jpql = "SELECT d FROM EntDocumento d "
                + "WHERE d.vgn_IdTramite = :pvn_IdTramite "
                + "AND d.vgn_IdTipoDocumento = :pvn_IdTipoDocumento "
                + "AND d.vgc_NombreArchivo = :pvc_NombreArchivo "
                + "AND d.vgc_EstadoDocumento = :pvc_EstadoDocumento "
                + "ORDER BY d.vgn_Version DESC";
        TypedQuery<EntDocumento> vlo_Consulta = this.vgo_EntityManager.createQuery(vlc_Jpql, EntDocumento.class);
        vlo_Consulta.setParameter("pvn_IdTramite", pvn_IdTramite);
        vlo_Consulta.setParameter("pvn_IdTipoDocumento", pvn_IdTipoDocumento);
        vlo_Consulta.setParameter("pvc_NombreArchivo", pvc_NombreArchivo);
        vlo_Consulta.setParameter("pvc_EstadoDocumento", pvc_EstadoDocumento);
        vlo_Consulta.setMaxResults(1);
        try {
            return vlo_Consulta.getSingleResult();
        } catch (NoResultException vlo_Excepcion) {
            return null;
        }
    }

    /**
     * Descripcion: obtiene los documentos activos
     * asociados a un tramite.
     *
     * @return documentos activos ordenados del mas reciente al mas antiguo.
     */
    public List<EntDocumento> SIMF_DOCUMENTO_ObtenerActivosPorTramite(
            Integer pvn_IdTramite
    ) {
        String vlc_Jpql =
                "SELECT d "
                        + "FROM EntDocumento d "
                        + "WHERE d.vgn_IdTramite = :pvn_IdTramite "
                        + "AND d.vgc_EstadoDocumento = 'ACT' "
                        + "ORDER BY d.vgd_FechaCarga DESC";

        TypedQuery<EntDocumento> vlo_Consulta =
                this.vgo_EntityManager.createQuery(
                        vlc_Jpql,
                        EntDocumento.class
                );

        vlo_Consulta.setParameter(
                "pvn_IdTramite",
                pvn_IdTramite
        );

        return vlo_Consulta.getResultList();
    }
}
