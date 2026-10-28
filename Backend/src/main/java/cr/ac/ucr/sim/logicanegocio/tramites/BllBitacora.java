package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntComentarioBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoBitacora;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Reglas de negocio de la bitácora de un expediente (HU-018). Registra toda acción
 *   realizada sobre un trámite y administra los comentarios. Un comentario registrado por error
 *   no se borra: se marca inactivo y su eliminación queda a su vez registrada.
 */
@Service
public class BllBitacora {

    private final DalBitacora vgo_DalBitacora;
    private final DalUsuario vgo_DalUsuario;

    public BllBitacora(DalBitacora pvo_DalBitacora, DalUsuario pvo_DalUsuario) {
        this.vgo_DalBitacora = pvo_DalBitacora;
        this.vgo_DalUsuario = pvo_DalUsuario;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Registra una acción sobre un expediente. Participa de la transacción de quien la
     *   invoca, de modo que la acción y su registro en bitácora se confirman o se deshacen juntos.
     *
     * @param pvn_IdTramite expediente sobre el que se actúa
     * @param pvn_IdUsuario usuario que realiza la acción
     * @param pvc_Accion código de la acción catalogada en SIMC_ACCION_BITACORA
     * @param pvc_EntidadAfectada tabla afectada por la acción
     * @param pvn_IdRegistroAfectado registro afectado, puede ser nulo
     * @param pvc_DatosAnteriores descripción del estado previo, puede ser nula
     * @param pvc_DatosNuevos descripción del resultado de la acción
     * @return registro de bitácora persistido
     */
    @Transactional
    public EntBitacora RegistrarAccion(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuario,
            String pvc_Accion,
            String pvc_EntidadAfectada,
            Integer pvn_IdRegistroAfectado,
            String pvc_DatosAnteriores,
            String pvc_DatosNuevos) {
        EntBitacora vlo_EnBitacora = new EntBitacora();
        vlo_EnBitacora.setIdTramite(pvn_IdTramite);
        vlo_EnBitacora.setIdUsuario(pvn_IdUsuario.longValue());
        vlo_EnBitacora.setAccion(pvc_Accion);
        vlo_EnBitacora.setEntidadAfectada(pvc_EntidadAfectada);
        vlo_EnBitacora.setIdRegistroAfectado(pvn_IdRegistroAfectado);
        vlo_EnBitacora.setDatosAnteriores(pvc_DatosAnteriores);
        vlo_EnBitacora.setDatosNuevos(pvc_DatosNuevos);
        vlo_EnBitacora.setEstado(Constantes.ESTADO_BITACORA_ACTIVO);
        this.vgo_DalBitacora.SIML_BITACORA_InsertarRegistro(vlo_EnBitacora);
        return vlo_EnBitacora;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene la bitácora vigente de un expediente, lista para mostrarse o imprimirse.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return registros vigentes del más antiguo al más reciente
     */
    @Transactional(readOnly = true)
    public List<EntMovimientoBitacora> ObtenerPorTramite(Integer pvn_IdTramite) {
        return this.vgo_DalBitacora.SIML_BITACORA_ObtenerPorTramite(
                pvn_IdTramite, Constantes.ESTADO_BITACORA_ACTIVO);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Agrega un comentario a la bitácora de un expediente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvo_Comentario texto del comentario y usuario que lo registra
     * @return bitácora vigente del expediente, con el comentario agregado
     */
    @Transactional
    public List<EntMovimientoBitacora> InsertarComentario(Integer pvn_IdTramite, EntComentarioBitacora pvo_Comentario) {
        String vlc_Comentario = pvo_Comentario == null || pvo_Comentario.getComentario() == null
                ? null
                : pvo_Comentario.getComentario().trim();
        if (vlc_Comentario == null
                || vlc_Comentario.isEmpty()
                || vlc_Comentario.length() > Constantes.LONGITUD_MAXIMA_COMENTARIO_BITACORA) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_COMENTARIO_INVALIDO);
        }
        Integer vln_IdUsuario = this.ValidarUsuario(pvo_Comentario.getIdUsuario());

        this.RegistrarAccion(pvn_IdTramite, vln_IdUsuario, Constantes.ACCION_BITACORA_COMENTARIO,
                Constantes.ENTIDAD_BITACORA_COMENTARIO, null, null, vlc_Comentario);
        return this.ObtenerPorTramite(pvn_IdTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Elimina un comentario registrado por error. El comentario se marca inactivo y la
     *   eliminación se registra con el texto original, de modo que la auditoría se conserva.
     *   Solo se admiten comentarios: las acciones del sistema no pueden eliminarse.
     *
     * @param pvn_IdTramite identificador del expediente al que pertenece el comentario
     * @param pvn_IdBitacora identificador del comentario
     * @param pvn_IdUsuario usuario que elimina el comentario
     * @return bitácora vigente del expediente, sin el comentario eliminado
     */
    @Transactional
    public List<EntMovimientoBitacora> EliminarComentario(
            Integer pvn_IdTramite,
            Integer pvn_IdBitacora,
            Integer pvn_IdUsuario) {
        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        EntBitacora vlo_EnComentario = this.vgo_DalBitacora.SIML_BITACORA_ObtenerRegistro(pvn_IdBitacora);

        boolean vlb_EsComentarioVigenteDelExpediente = vlo_EnComentario != null
                && vlo_EnComentario.getIdTramite().equals(pvn_IdTramite)
                && Constantes.ACCION_BITACORA_COMENTARIO.equals(vlo_EnComentario.getAccion())
                && Constantes.ESTADO_BITACORA_ACTIVO.equals(vlo_EnComentario.getEstado());
        if (!vlb_EsComentarioVigenteDelExpediente) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_COMENTARIO_NO_ENCONTRADO);
        }

        vlo_EnComentario.setEstado(Constantes.ESTADO_BITACORA_INACTIVO);
        this.vgo_DalBitacora.SIML_BITACORA_ModificarRegistro(vlo_EnComentario);

        this.RegistrarAccion(pvn_IdTramite, vln_IdUsuario, Constantes.ACCION_BITACORA_COMENTARIO_ELIMINADO,
                Constantes.ENTIDAD_BITACORA_COMENTARIO, vlo_EnComentario.getIdBitacora(),
                vlo_EnComentario.getDatosNuevos(), null);
        return this.ObtenerPorTramite(pvn_IdTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Comprueba que el usuario que actúa sobre la bitácora esté registrado.
     *
     * @param pvn_IdUsuario identificador del usuario
     * @return identificador validado
     */
    private Integer ValidarUsuario(Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        return pvn_IdUsuario;
    }
}
