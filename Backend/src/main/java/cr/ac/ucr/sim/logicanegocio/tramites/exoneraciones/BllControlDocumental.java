package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBitacora;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloControlDocumentalRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloRequisitoControlRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloRevisionDocumentoRespuesta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Control de los documentos obligatorios de la solicitud de exoneración (HU-021).
 *   Reglas de negocio:
 *   - Los documentos exigidos salen de la matriz SIMT_REQUISITO_DOCUMENTAL según el tipo de
 *     trámite y la modalidad de exoneración; la regla no se codifica en Java.
 *   - Un documento obligatorio se cumple cuando está adjunto y su revisión vigente (HU-019) es
 *     RECIBIDO. Sin adjuntar, sin revisar o rechazado, queda en la lista de pendientes.
 *   - El expediente solo continúa a aprobación con toda la documentación obligatoria cumplida:
 *     pasa al estado "Pendiente de aprobación" y el cambio queda en el historial de estados y en
 *     la bitácora con su fecha, su hora y el usuario responsable.
 *   - Solo se envía a aprobación desde "Pendiente revisión documental" o, cuando la jefatura pidió
 *     cambios (HU-022), desde "Pendiente de modificación".
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: con los estados de la decisión de la HU-022, el envío a
 *     aprobación se limita a los estados de revisión y de modificación; un expediente aprobado ya
 *     no puede volver a enviarse.
 */
@Service
public class BllControlDocumental {

    private final BllRevisionDocumento vgo_BllRevisionDocumento;
    private final DalDocumento vgo_DalDocumento;
    private final DalTramite vgo_DalTramite;
    private final DalExoneracion vgo_DalExoneracion;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalUsuario vgo_DalUsuario;
    private final BllBitacora vgo_BllBitacora;

    public BllControlDocumental(
            BllRevisionDocumento pvo_BllRevisionDocumento,
            DalDocumento pvo_DalDocumento,
            DalTramite pvo_DalTramite,
            DalExoneracion pvo_DalExoneracion,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalUsuario pvo_DalUsuario,
            BllBitacora pvo_BllBitacora) {
        this.vgo_BllRevisionDocumento = pvo_BllRevisionDocumento;
        this.vgo_DalDocumento = pvo_DalDocumento;
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalExoneracion = pvo_DalExoneracion;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_BllBitacora = pvo_BllBitacora;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Evalúa la documentación obligatoria del expediente: valida la existencia de
     *   cada documento exigido, lista los pendientes y confirma si hay cumplimiento documental.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @return estado documental del expediente con sus requisitos y pendientes
     */
    @Transactional(readOnly = true)
    public ModeloControlDocumentalRespuesta ObtenerControlDocumental(Integer pvn_IdTramite) {
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvn_IdTramite);
        return this.EvaluarControl(vlo_EnTramite, this.ObtenerEstado(vlo_EnTramite));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Continúa el expediente a aprobación. La documentación se vuelve a evaluar en el
     *   servidor: si falta algún documento obligatorio, se rechaza la acción indicando cuáles. Con
     *   la documentación completa, el expediente pasa a "Pendiente de aprobación"; el movimiento
     *   se agrega al historial de estados y la confirmación de cumplimiento y el cambio de estado
     *   quedan en la bitácora.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdUsuario usuario autenticado que continúa el trámite
     * @return estado documental del expediente ya en su nuevo estado
     */
    @Transactional
    public ModeloControlDocumentalRespuesta ContinuarAprobacion(Integer pvn_IdTramite, Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_CONTROL_DOCUMENTAL_INVALIDO);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvn_IdTramite);
        EntEstadoTramite vlo_EnEstadoActual = this.ObtenerEstado(vlo_EnTramite);
        if (vlo_EnEstadoActual != null && Boolean.TRUE.equals(vlo_EnEstadoActual.getEsFinal())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_CERRADA);
        }

        EntEstadoTramite vlo_EnEstadoAprobacion = this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                vlo_EnTramite.getIdTipoTramite(),
                Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION,
                Constantes.ESTADO_CATALOGO_ACTIVO);
        if (vlo_EnEstadoAprobacion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_PENDIENTE_APROBACION_NO_CONFIGURADO);
        }
        if (vlo_EnEstadoActual != null
                && vlo_EnEstadoAprobacion.getIdEstadoTramite().equals(vlo_EnEstadoActual.getIdEstadoTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_YA_EN_APROBACION);
        }
        if (!this.PermiteEnvioAprobacion(vlo_EnEstadoActual)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_NO_PERMITE_ENVIO_APROBACION);
        }

        ModeloControlDocumentalRespuesta vlo_Control = this.EvaluarControl(vlo_EnTramite, vlo_EnEstadoActual);
        if (vlo_Control.getTotalObligatorios() == 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SIN_REQUISITOS_DOCUMENTALES);
        }
        if (!Boolean.TRUE.equals(vlo_Control.getCumpleDocumentacion())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTACION_OBLIGATORIA_INCOMPLETA
                    + String.join("; ", vlo_Control.getDocumentosPendientes()) + ".");
        }

        Integer vln_IdEstadoAnterior = vlo_EnEstadoActual == null ? null : vlo_EnEstadoActual.getIdEstadoTramite();
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(vln_IdEstadoAnterior);
        vlo_EnHistorial.setIdEstadoNuevo(vlo_EnEstadoAprobacion.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion(Constantes.JUSTIFICACION_ENVIO_APROBACION_EXONERACION);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);

        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoAprobacion.getIdEstadoTramite());
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                pvn_IdUsuario,
                Constantes.ACCION_BITACORA_ESTADO_DOCUMENTAL,
                Constantes.ENTIDAD_BITACORA_EXONERACION,
                vlo_EnTramite.getIdTramite(),
                null,
                this.DescribirCumplimiento(vlo_Control));
        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                pvn_IdUsuario,
                Constantes.ACCION_BITACORA_CAMBIO_ESTADO,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                "Estado: " + (vlo_EnEstadoActual == null ? "sin definir" : vlo_EnEstadoActual.getNombre()) + ".",
                "Estado: " + vlo_EnEstadoAprobacion.getNombre() + ". Justificación: "
                        + Constantes.JUSTIFICACION_ENVIO_APROBACION_EXONERACION);

        return this.EvaluarControl(vlo_EnTramite, vlo_EnEstadoAprobacion);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Arma el control documental del expediente a partir del checklist de la
     *   revisión documental (HU-019), de modo que ambas pantallas evalúan los mismos documentos.
     *
     * @param pvo_EnTramite expediente de exoneración
     * @param pvo_EnEstado estado actual del expediente, puede ser nulo
     * @return control documental del expediente
     */
    private ModeloControlDocumentalRespuesta EvaluarControl(EntTramite pvo_EnTramite, EntEstadoTramite pvo_EnEstado) {
        EntExoneracion vlo_EnExoneracion =
                this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvo_EnTramite.getIdTramite());
        List<ModeloRevisionDocumentoRespuesta> vlo_LsChecklist =
                this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(pvo_EnTramite.getIdTramite());
        Map<Integer, EntDocumento> vlo_MpDocumentos = this.vgo_DalDocumento
                .SIMF_DOCUMENTO_ObtenerActivosPorTramite(pvo_EnTramite.getIdTramite())
                .stream()
                .collect(Collectors.toMap(EntDocumento::getIdDocumento, Function.identity(),
                        (vlo_EnPrimero, vlo_EnSegundo) -> vlo_EnPrimero));

        List<ModeloRequisitoControlRespuesta> vlo_LsRequisitos = new ArrayList<>();
        List<String> vlo_LsPendientes = new ArrayList<>();
        int vln_TotalObligatorios = 0;
        int vln_Cumplidos = 0;

        for (ModeloRevisionDocumentoRespuesta vlo_Elemento : vlo_LsChecklist) {
            ModeloRequisitoControlRespuesta vlo_Requisito = this.ConvertirRequisito(vlo_Elemento, vlo_MpDocumentos);
            vlo_LsRequisitos.add(vlo_Requisito);

            if (!Boolean.TRUE.equals(vlo_Requisito.getObligatorio())) {
                continue;
            }
            vln_TotalObligatorios++;
            if (Constantes.ESTADO_CONTROL_CUMPLIDO.equals(vlo_Requisito.getEstadoControl())) {
                vln_Cumplidos++;
            } else {
                vlo_LsPendientes.add(vlo_Requisito.getNombreTipoDocumento()
                        + " (" + this.DescribirMotivoPendiente(vlo_Requisito.getEstadoControl()) + ")");
            }
        }

        boolean vlb_Cumple = vln_TotalObligatorios > 0 && vln_Cumplidos == vln_TotalObligatorios;
        boolean vlb_PermiteEnvio = this.PermiteEnvioAprobacion(pvo_EnEstado);

        ModeloControlDocumentalRespuesta vlo_Respuesta = new ModeloControlDocumentalRespuesta();
        vlo_Respuesta.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_Respuesta.setReferenciaInterna(pvo_EnTramite.getReferenciaInterna());
        vlo_Respuesta.setTipoExoneracion(vlo_EnExoneracion == null ? null : vlo_EnExoneracion.getTipoExoneracion());
        vlo_Respuesta.setEstadoExpediente(pvo_EnEstado == null ? null : pvo_EnEstado.getNombre());
        vlo_Respuesta.setEstadoDocumental(vlb_Cumple
                ? Constantes.ESTADO_DOCUMENTAL_EXONERACION_COMPLETO
                : Constantes.ESTADO_DOCUMENTAL_EXONERACION_INCOMPLETO);
        vlo_Respuesta.setCumpleDocumentacion(vlb_Cumple);
        vlo_Respuesta.setPuedeContinuarAprobacion(vlb_Cumple && vlb_PermiteEnvio);
        vlo_Respuesta.setTotalObligatorios(vln_TotalObligatorios);
        vlo_Respuesta.setObligatoriosCumplidos(vln_Cumplidos);
        vlo_Respuesta.setObligatoriosPendientes(vln_TotalObligatorios - vln_Cumplidos);
        vlo_Respuesta.setPorcentajeCumplimiento(vln_TotalObligatorios == 0
                ? 0
                : (int) Math.floor(vln_Cumplidos * 100.0 / vln_TotalObligatorios));
        vlo_Respuesta.setRequisitos(vlo_LsRequisitos);
        vlo_Respuesta.setDocumentosPendientes(vlo_LsPendientes);
        return vlo_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Convierte un elemento del checklist en un requisito del control y determina
     *   su estado: sin archivo es FALTANTE; con archivo depende de su revisión vigente.
     *
     * @param pvo_Elemento elemento del checklist de revisión documental
     * @param pvo_MpDocumentos documentos activos del expediente por identificador
     * @return requisito del control documental
     */
    private ModeloRequisitoControlRespuesta ConvertirRequisito(
            ModeloRevisionDocumentoRespuesta pvo_Elemento,
            Map<Integer, EntDocumento> pvo_MpDocumentos) {
        ModeloRequisitoControlRespuesta vlo_Requisito = new ModeloRequisitoControlRespuesta();
        vlo_Requisito.setIdTipoDocumento(pvo_Elemento.getIdTipoDocumento());
        vlo_Requisito.setCodigoTipoDocumento(pvo_Elemento.getCodigoTipoDocumento());
        vlo_Requisito.setNombreTipoDocumento(pvo_Elemento.getNombreTipoDocumento());
        vlo_Requisito.setObligatorio(Boolean.TRUE.equals(pvo_Elemento.getObligatorio()));
        vlo_Requisito.setIdDocumento(pvo_Elemento.getIdDocumento());
        vlo_Requisito.setNombreArchivo(pvo_Elemento.getNombreArchivo());
        vlo_Requisito.setEstadoRevision(pvo_Elemento.getEstadoRevision());

        if (pvo_Elemento.getIdDocumento() == null) {
            vlo_Requisito.setEstadoControl(Constantes.ESTADO_CONTROL_FALTANTE);
            return vlo_Requisito;
        }

        EntDocumento vlo_EnDocumento = pvo_MpDocumentos.get(pvo_Elemento.getIdDocumento());
        vlo_Requisito.setFechaCarga(vlo_EnDocumento == null ? null : vlo_EnDocumento.getFechaCarga());

        if (Constantes.ESTADO_REVISION_RECIBIDO.equals(pvo_Elemento.getEstadoRevision())) {
            vlo_Requisito.setEstadoControl(Constantes.ESTADO_CONTROL_CUMPLIDO);
        } else if (Constantes.ESTADO_REVISION_RECHAZADO.equals(pvo_Elemento.getEstadoRevision())) {
            vlo_Requisito.setEstadoControl(Constantes.ESTADO_CONTROL_RECHAZADO);
        } else {
            vlo_Requisito.setEstadoControl(Constantes.ESTADO_CONTROL_POR_REVISAR);
        }
        return vlo_Requisito;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Recupera el trámite de una solicitud de exoneración por su identificador y
     *   comprueba que efectivamente sea una exoneración.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return trámite de la solicitud
     */
    private EntTramite ObtenerTramite(Integer pvn_IdTramite) {
        if (pvn_IdTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO);
        }
        EntTramite vlo_EnTramite = this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(pvn_IdTramite);
        if (vlo_EnTramite == null || this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvn_IdTramite) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA);
        }
        return vlo_EnTramite;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el estado actual del expediente.
     *
     * @param pvo_EnTramite expediente
     * @return estado actual o null si no está catalogado
     */
    private EntEstadoTramite ObtenerEstado(EntTramite pvo_EnTramite) {
        return this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Indica si el expediente puede enviarse a aprobación desde su estado actual:
     *   solo desde la revisión documental o desde una modificación pedida por la jefatura.
     *
     * @param pvo_EnEstado estado actual del expediente, puede ser nulo
     * @return verdadero si el estado admite el envío a aprobación
     */
    private boolean PermiteEnvioAprobacion(EntEstadoTramite pvo_EnEstado) {
        if (pvo_EnEstado == null) {
            return true;
        }
        return !Boolean.TRUE.equals(pvo_EnEstado.getEsFinal())
                && (Boolean.TRUE.equals(pvo_EnEstado.getEsInicial())
                || Constantes.NOMBRE_ESTADO_REVISION_DOCUMENTAL_EXONERACION.equalsIgnoreCase(pvo_EnEstado.getNombre())
                || Constantes.NOMBRE_ESTADO_PENDIENTE_MODIFICACION_EXONERACION.equalsIgnoreCase(pvo_EnEstado.getNombre()));
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Explica por qué un documento obligatorio sigue pendiente.
     *
     * @param pvc_EstadoControl estado de control del requisito
     * @return motivo legible para la persona usuaria
     */
    private String DescribirMotivoPendiente(String pvc_EstadoControl) {
        if (Constantes.ESTADO_CONTROL_RECHAZADO.equals(pvc_EstadoControl)) {
            return Constantes.MOTIVO_CONTROL_RECHAZADO;
        }
        if (Constantes.ESTADO_CONTROL_POR_REVISAR.equals(pvc_EstadoControl)) {
            return Constantes.MOTIVO_CONTROL_POR_REVISAR;
        }
        return Constantes.MOTIVO_CONTROL_FALTANTE;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Redacta la confirmación de cumplimiento documental para la bitácora.
     *
     * @param pvo_Control control documental evaluado
     * @return descripción legible al imprimirse la bitácora
     */
    private String DescribirCumplimiento(ModeloControlDocumentalRespuesta pvo_Control) {
        String vlc_Documentos = pvo_Control.getRequisitos().stream()
                .filter(vlo_Requisito -> Boolean.TRUE.equals(vlo_Requisito.getObligatorio()))
                .map(ModeloRequisitoControlRespuesta::getNombreTipoDocumento)
                .collect(Collectors.joining(", "));
        return "Cumplimiento documental confirmado: " + pvo_Control.getObligatoriosCumplidos() + " de "
                + pvo_Control.getTotalObligatorios() + " documentos obligatorios recibidos (" + vlc_Documentos + ").";
    }
}
