package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalBitacora;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalObservacionExpediente;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntObservacionExpediente;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 04/10/2026
 * Descripción: Reglas de negocio de las observaciones y aclaraciones de un expediente de
 *   exoneración (HU-020).
 *   - Una observación nace como borrador: solo su autor la consulta, la modifica, la elimina
 *     o la confirma. Los borradores de otros usuarios no se muestran.
 *   - Al confirmarse pasa al historial compartido del expediente con su autor y su fecha de
 *     confirmación, queda anotada en la bitácora y ya no puede modificarse ni eliminarse.
 *   - Un borrador eliminado se marca inactivo: ninguna observación se borra físicamente.
 *   Toda operación que cambia un borrador obtiene antes su fila con bloqueo pesimista, de modo
 *   que dos solicitudes simultáneas sobre el mismo borrador no se pisen entre sí.
 *   La identidad del usuario la entrega el servicio REST desde la sesión autenticada.
 *
 * Historial de cambios:
 *   04/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Service
public class BllObservacionExpediente {

    private final DalObservacionExpediente vgo_DalObservacionExpediente;
    private final DalExoneracion vgo_DalExoneracion;
    private final DalUsuario vgo_DalUsuario;
    private final DalBitacora vgo_DalBitacora;

    public BllObservacionExpediente(
            DalObservacionExpediente pvo_DalObservacionExpediente,
            DalExoneracion pvo_DalExoneracion,
            DalUsuario pvo_DalUsuario,
            DalBitacora pvo_DalBitacora) {
        this.vgo_DalObservacionExpediente = pvo_DalObservacionExpediente;
        this.vgo_DalExoneracion = pvo_DalExoneracion;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_DalBitacora = pvo_DalBitacora;
    }

    // -----------------------------------------------------------------------
    // Consultas.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene el historial de observaciones confirmadas de un expediente, de la más
     *   antigua a la más reciente. El historial es compartido: lo ve cualquier usuario.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @return observaciones confirmadas del expediente
     */
    @Transactional(readOnly = true)
    public List<EntObservacionExpediente> ObtenerHistorial(Integer pvn_IdTramite) {
        this.ValidarExoneracion(pvn_IdTramite);
        return this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ObtenerHistorialPorTramite(
                pvn_IdTramite,
                Constantes.ESTADO_OBSERVACION_CONFIRMADA,
                Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene los borradores vigentes que el usuario redactó en un expediente. Los
     *   borradores de otros usuarios y los eliminados no se incluyen.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdUsuario usuario autenticado
     * @return borradores propios del usuario en el expediente
     */
    @Transactional(readOnly = true)
    public List<EntObservacionExpediente> ObtenerBorradoresPropios(Integer pvn_IdTramite, Integer pvn_IdUsuario) {
        this.ValidarExoneracion(pvn_IdTramite);
        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        return this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ObtenerPorTramiteYAutor(
                pvn_IdTramite,
                vln_IdUsuario,
                Constantes.ESTADO_OBSERVACION_BORRADOR,
                Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Resuelve el nombre del responsable de cada observación. Consulta una sola vez
     *   a cada autor distinto, aunque haya redactado varias observaciones.
     *
     * @param pvo_LsObservaciones observaciones cuyos autores se resuelven
     * @return nombre completo por identificador de autor
     */
    @Transactional(readOnly = true)
    public Map<Integer, String> ObtenerNombresResponsables(Collection<EntObservacionExpediente> pvo_LsObservaciones) {
        Map<Integer, String> vlo_MpNombres = new HashMap<>();
        if (pvo_LsObservaciones == null) {
            return vlo_MpNombres;
        }
        for (EntObservacionExpediente vlo_EnObservacion : pvo_LsObservaciones) {
            Integer vln_IdAutor = vlo_EnObservacion.getIdUsuarioAutor();
            if (vln_IdAutor != null && !vlo_MpNombres.containsKey(vln_IdAutor)) {
                EntUsuario vlo_EnUsuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(vln_IdAutor);
                String vlc_Nombre = vlo_EnUsuario == null || vlo_EnUsuario.getNombreCompleto() == null
                        ? Constantes.TEXTO_SIN_DATO
                        : vlo_EnUsuario.getNombreCompleto();
                vlo_MpNombres.put(vln_IdAutor, vlc_Nombre);
            }
        }
        return vlo_MpNombres;
    }

    // -----------------------------------------------------------------------
    // Operaciones.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Registra una observación en un expediente. Por defecto queda como borrador;
     *   si se pide confirmarla, se registra directamente en el historial y en la bitácora, dentro
     *   de la misma transacción.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdUsuario usuario autenticado, que queda como autor
     * @param pvc_Motivo motivo de la observación, opcional
     * @param pvc_Observacion texto de la observación
     * @param pvb_Confirmar true para confirmarla al registrarla
     * @return observación registrada
     */
    @Transactional
    public EntObservacionExpediente CrearObservacion(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuario,
            String pvc_Motivo,
            String pvc_Observacion,
            boolean pvb_Confirmar) {
        this.ValidarExoneracion(pvn_IdTramite);
        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        String vlc_Motivo = this.NormalizarMotivo(pvc_Motivo);
        String vlc_Observacion = this.NormalizarObservacion(pvc_Observacion);
        LocalDateTime vld_Ahora = LocalDateTime.now();

        EntObservacionExpediente vlo_EnObservacion = new EntObservacionExpediente();
        vlo_EnObservacion.setIdTramite(pvn_IdTramite);
        vlo_EnObservacion.setIdUsuarioAutor(vln_IdUsuario);
        vlo_EnObservacion.setMotivo(vlc_Motivo);
        vlo_EnObservacion.setObservacion(vlc_Observacion);
        vlo_EnObservacion.setFechaCreacion(vld_Ahora);
        vlo_EnObservacion.setEstado(Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO);
        if (pvb_Confirmar) {
            vlo_EnObservacion.setEstadoObservacion(Constantes.ESTADO_OBSERVACION_CONFIRMADA);
            vlo_EnObservacion.setFechaConfirmacion(vld_Ahora);
        } else {
            vlo_EnObservacion.setEstadoObservacion(Constantes.ESTADO_OBSERVACION_BORRADOR);
        }
        this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_InsertarRegistro(vlo_EnObservacion);

        if (pvb_Confirmar) {
            this.RegistrarConfirmacionEnBitacora(vlo_EnObservacion);
        }
        return vlo_EnObservacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Modifica el motivo y el texto de un borrador propio. El expediente, el autor
     *   y la fecha de creación no cambian.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdObservacion identificador del borrador
     * @param pvn_IdUsuario usuario autenticado
     * @param pvc_Motivo motivo nuevo, opcional
     * @param pvc_Observacion texto nuevo de la observación
     * @return borrador modificado
     */
    @Transactional
    public EntObservacionExpediente ModificarBorrador(
            Integer pvn_IdTramite,
            Long pvn_IdObservacion,
            Integer pvn_IdUsuario,
            String pvc_Motivo,
            String pvc_Observacion) {
        this.ValidarExoneracion(pvn_IdTramite);
        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        String vlc_Motivo = this.NormalizarMotivo(pvc_Motivo);
        String vlc_Observacion = this.NormalizarObservacion(pvc_Observacion);

        EntObservacionExpediente vlo_EnBorrador =
                this.ObtenerBorradorPropioBloqueado(pvn_IdTramite, pvn_IdObservacion, vln_IdUsuario);
        vlo_EnBorrador.setMotivo(vlc_Motivo);
        vlo_EnBorrador.setObservacion(vlc_Observacion);
        vlo_EnBorrador.setFechaModificacion(LocalDateTime.now());
        this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ModificarRegistro(vlo_EnBorrador);
        return vlo_EnBorrador;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Elimina un borrador propio mediante baja lógica: se marca inactivo y deja de
     *   mostrarse, pero el registro se conserva. FECHA_MODIFICACION guarda cuándo se eliminó.
     *   No se anota en bitácora porque el borrador nunca formó parte del expediente.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdObservacion identificador del borrador
     * @param pvn_IdUsuario usuario autenticado
     */
    @Transactional
    public void EliminarBorrador(Integer pvn_IdTramite, Long pvn_IdObservacion, Integer pvn_IdUsuario) {
        this.ValidarExoneracion(pvn_IdTramite);
        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);

        EntObservacionExpediente vlo_EnBorrador =
                this.ObtenerBorradorPropioBloqueado(pvn_IdTramite, pvn_IdObservacion, vln_IdUsuario);
        vlo_EnBorrador.setEstado(Constantes.ESTADO_REGISTRO_OBSERVACION_INACTIVO);
        vlo_EnBorrador.setFechaModificacion(LocalDateTime.now());
        this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ModificarRegistro(vlo_EnBorrador);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Confirma un borrador propio: pasa al historial compartido del expediente con
     *   la fecha y hora del servidor, y la confirmación queda anotada en la bitácora en la misma
     *   transacción. A partir de ese momento la observación es inmutable.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdObservacion identificador del borrador
     * @param pvn_IdUsuario usuario autenticado
     * @return observación confirmada
     */
    @Transactional
    public EntObservacionExpediente ConfirmarBorrador(
            Integer pvn_IdTramite,
            Long pvn_IdObservacion,
            Integer pvn_IdUsuario) {
        this.ValidarExoneracion(pvn_IdTramite);
        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);

        EntObservacionExpediente vlo_EnBorrador =
                this.ObtenerBorradorPropioBloqueado(pvn_IdTramite, pvn_IdObservacion, vln_IdUsuario);
        vlo_EnBorrador.setEstadoObservacion(Constantes.ESTADO_OBSERVACION_CONFIRMADA);
        vlo_EnBorrador.setFechaConfirmacion(LocalDateTime.now());
        this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ModificarRegistro(vlo_EnBorrador);

        this.RegistrarConfirmacionEnBitacora(vlo_EnBorrador);
        return vlo_EnBorrador;
    }

    // -----------------------------------------------------------------------
    // Reglas privadas.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Obtiene con bloqueo pesimista un borrador vigente del usuario en el
     *   expediente. Las validaciones se hacen sobre la fila ya bloqueada, de modo que ninguna otra
     *   transacción pueda confirmarlo o eliminarlo entre la validación y la modificación.
     *   - Si no existe, es de otro expediente o fue eliminado: no está disponible.
     *   - Si ya fue confirmado: es inmutable (el historial es público, así que se informa).
     *   - Si es de otro usuario: no está disponible, sin revelar que existe.
     *
     * @param pvn_IdTramite expediente indicado en la solicitud
     * @param pvn_IdObservacion identificador del borrador
     * @param pvn_IdUsuario usuario autenticado ya validado
     * @return borrador bloqueado, listo para modificarse
     */
    private EntObservacionExpediente ObtenerBorradorPropioBloqueado(
            Integer pvn_IdTramite,
            Long pvn_IdObservacion,
            Integer pvn_IdUsuario) {
        if (pvn_IdObservacion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_OBSERVACION_REQUERIDO);
        }
        EntObservacionExpediente vlo_EnObservacion =
                this.vgo_DalObservacionExpediente.SIMT_OBSERVACION_EXPED_ObtenerRegistroParaModificar(pvn_IdObservacion);

        boolean vlb_EsVigenteDelExpediente = vlo_EnObservacion != null
                && pvn_IdTramite.equals(vlo_EnObservacion.getIdTramite())
                && Constantes.ESTADO_REGISTRO_OBSERVACION_ACTIVO.equals(vlo_EnObservacion.getEstado());
        if (!vlb_EsVigenteDelExpediente) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE);
        }
        if (!Constantes.ESTADO_OBSERVACION_BORRADOR.equals(vlo_EnObservacion.getEstadoObservacion())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACION_CONFIRMADA_INMUTABLE);
        }
        if (!pvn_IdUsuario.equals(vlo_EnObservacion.getIdUsuarioAutor())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_BORRADOR_NO_DISPONIBLE);
        }
        return vlo_EnObservacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Anota en SIML_BITACORA la confirmación de una observación, con su autor, la
     *   misma fecha y hora de la confirmación y el texto confirmado. Inserta directamente con
     *   DalBitacora porque BllBitacora.RegistrarAccion (HU-018) no compila en su estado actual;
     *   el registro sigue el mismo esquema que esa clase.
     *
     * @param pvo_EnObservacion observación recién confirmada
     */
    private void RegistrarConfirmacionEnBitacora(EntObservacionExpediente pvo_EnObservacion) {
        EntBitacora vlo_EnBitacora = new EntBitacora();
        vlo_EnBitacora.setIdTramite(pvo_EnObservacion.getIdTramite());
        // EntBitacora declara el usuario como Long: es la única conversión indispensable.
        vlo_EnBitacora.setIdUsuario(Long.valueOf(pvo_EnObservacion.getIdUsuarioAutor()));
        vlo_EnBitacora.setAccion(Constantes.ACCION_BITACORA_OBSERVACION_CONFIRMADA);
        vlo_EnBitacora.setEntidadAfectada(Constantes.ENTIDAD_BITACORA_OBSERVACION);
        // SIML_BITACORA.ID_REGISTRO_AFECTADO es INT; la llave de la observación es BIGINT.
        vlo_EnBitacora.setIdRegistroAfectado(Math.toIntExact(pvo_EnObservacion.getIdObservacion()));
        vlo_EnBitacora.setDatosAnteriores(null);
        vlo_EnBitacora.setDatosNuevos(this.DescribirObservacion(pvo_EnObservacion));
        vlo_EnBitacora.setEstado(Constantes.ESTADO_BITACORA_ACTIVO);
        vlo_EnBitacora.setFechaHora(pvo_EnObservacion.getFechaConfirmacion());
        this.vgo_DalBitacora.SIML_BITACORA_InsertarRegistro(vlo_EnBitacora);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Describe la observación tal como se mostrará en la bitácora del expediente.
     *
     * @param pvo_EnObservacion observación confirmada
     * @return motivo, si lo tiene, seguido del texto de la observación
     */
    private String DescribirObservacion(EntObservacionExpediente pvo_EnObservacion) {
        return pvo_EnObservacion.getMotivo() == null
                ? pvo_EnObservacion.getObservacion()
                : Constantes.PREFIJO_BITACORA_MOTIVO_OBSERVACION + pvo_EnObservacion.getMotivo()
                        + ". " + pvo_EnObservacion.getObservacion();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Comprueba que el expediente exista y sea una solicitud de exoneración.
     *
     * @param pvn_IdTramite identificador del expediente
     */
    private void ValidarExoneracion(Integer pvn_IdTramite) {
        if (pvn_IdTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO);
        }
        if (this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvn_IdTramite) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA);
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Comprueba que el usuario autenticado esté registrado.
     *
     * @param pvn_IdUsuario identificador del usuario autenticado
     * @return identificador validado
     */
    private Integer ValidarUsuario(Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_OBSERVACION_INVALIDO);
        }
        return pvn_IdUsuario;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Valida el motivo opcional. Un motivo vacío se guarda como nulo.
     *
     * @param pvc_Motivo motivo recibido
     * @return motivo sin espacios sobrantes, o null
     */
    private String NormalizarMotivo(String pvc_Motivo) {
        if (pvc_Motivo == null || pvc_Motivo.isBlank()) {
            return null;
        }
        String vlc_Motivo = pvc_Motivo.trim();
        if (vlc_Motivo.length() > Constantes.LONGITUD_MAXIMA_MOTIVO_OBSERVACION) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_MOTIVO_OBSERVACION_INVALIDO);
        }
        return vlc_Motivo;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripción: Valida el texto obligatorio de la observación.
     *
     * @param pvc_Observacion texto recibido
     * @return texto sin espacios sobrantes
     */
    private String NormalizarObservacion(String pvc_Observacion) {
        String vlc_Observacion = pvc_Observacion == null ? "" : pvc_Observacion.trim();
        if (vlc_Observacion.isEmpty()
                || vlc_Observacion.length() > Constantes.LONGITUD_MAXIMA_OBSERVACION_EXPEDIENTE) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACION_INVALIDA);
        }
        return vlc_Observacion;
    }
}
