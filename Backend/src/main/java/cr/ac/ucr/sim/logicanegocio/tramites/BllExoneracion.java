package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntComentarioBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCorreccionExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracionDetalle;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMovimientoBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Reglas de negocio de la solicitud de exoneración (HU-018). Crea el expediente con
 *   una referencia interna única y el estado inicial "Pendiente revisión documental", permite
 *   registrarlo aunque falte documentación obligatoria advirtiendo lo pendiente, admite adjuntar
 *   esa documentación después y registra toda acción en la bitácora.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 18/10/2026
 *   Descripción: se quitan los permisos regulatorios de los documentos admitidos y se agregan el
 *     expediente SICOP, la autorización EXONET, el DUA de aplicación, el comprobante de impuestos
 *     y otros documentos (comentarios de la Unidad).
 */
@Service
public class BllExoneracion {

    /**
     * Documentos que la HU-018 exige para la revisión documental. Si faltan, la solicitud se
     * registra igual y se informan como pendientes.
     */
    public static final List<String> DOCUMENTOS_OBLIGATORIOS = List.of(
            Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE,
            Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA,
            Constantes.CODIGO_TIPO_DOCUMENTO_FORMULARIO_S1,
            Constantes.CODIGO_TIPO_DOCUMENTO_PACKING_LIST);

    /**
     * Documentos que admite el expediente de exoneración, obligatorios y opcionales. La Unidad
     * indicó que la exoneración no lleva permisos regulatorios, así que ese tipo ya no se admite;
     * en cambio se admiten el expediente de SICOP, la autorización EXONET, el DUA de aplicación,
     * el comprobante de impuestos y otros documentos de respaldo.
     */
    public static final Set<String> DOCUMENTOS_ADMITIDOS = Set.of(
            Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE,
            Constantes.CODIGO_TIPO_DOCUMENTO_FACTURA,
            Constantes.CODIGO_TIPO_DOCUMENTO_FORMULARIO_S1,
            Constantes.CODIGO_TIPO_DOCUMENTO_PACKING_LIST,
            Constantes.CODIGO_TIPO_DOCUMENTO_EXONET,
            Constantes.CODIGO_TIPO_DOCUMENTO_TRADUCCION_FACTURA,
            Constantes.CODIGO_TIPO_DOCUMENTO_EXPEDIENTE_SICOP,
            Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_EXONET,
            Constantes.CODIGO_TIPO_DOCUMENTO_DUA_APLICACION,
            Constantes.CODIGO_TIPO_DOCUMENTO_COMPROBANTE_IMPUESTOS,
            Constantes.CODIGO_TIPO_DOCUMENTO_OTROS);

    /**
     * Tipos de exoneración que admite la Unidad de Importaciones.
     */
    public static final Set<String> TIPOS_EXONERACION = Set.of(
            Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL,
            Constantes.TIPO_EXONERACION_DONACION,
            Constantes.TIPO_EXONERACION_CLIENTE_EXTERNO,
            Constantes.TIPO_EXONERACION_VEHICULOS,
            Constantes.TIPO_EXONERACION_MOVIMIENTO_TRASPASO);

    private final DalTramite vgo_DalTramite;
    private final DalExoneracion vgo_DalExoneracion;
    private final DalDocumento vgo_DalDocumento;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalTipoDocumento vgo_DalTipoDocumento;
    private final DalUsuario vgo_DalUsuario;
    private final AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;
    private final BllBitacora vgo_BllBitacora;
    private final BllDocumento vgo_BllDocumento;

    public BllExoneracion(
            DalTramite pvo_DalTramite,
            DalExoneracion pvo_DalExoneracion,
            DalDocumento pvo_DalDocumento,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalTipoTramite pvo_DalTipoTramite,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalTipoDocumento pvo_DalTipoDocumento,
            DalUsuario pvo_DalUsuario,
            AlmacenamientoDocumentos pvo_AlmacenamientoDocumentos,
            BllBitacora pvo_BllBitacora,
            BllDocumento pvo_BllDocumento) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalExoneracion = pvo_DalExoneracion;
        this.vgo_DalDocumento = pvo_DalDocumento;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalTipoDocumento = pvo_DalTipoDocumento;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_AlmacenamientoDocumentos = pvo_AlmacenamientoDocumentos;
        this.vgo_BllBitacora = pvo_BllBitacora;
        this.vgo_BllDocumento = pvo_BllDocumento;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Registra la solicitud de exoneración y crea su expediente. La documentación
     *   obligatoria faltante no impide el registro: se informa como pendiente en la respuesta y en
     *   la bitácora. El registro se anota en la bitácora dentro de la misma transacción.
     *
     * @param pvo_Registro datos del formulario de registro
     * @param pvo_MpDocumentos archivos recibidos, agrupados por código de tipo de documento
     * @return confirmación con la referencia interna, el estado inicial y lo pendiente
     */
    @Transactional
    public EntRespuestaExoneracion InsertarRegistro(
            EntRegistroExoneracion pvo_Registro,
            Map<String, List<MultipartFile>> pvo_MpDocumentos) {

        if (pvo_Registro == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteExoneracion();
        String vlc_ReferenciaInterna = this.NormalizarMayusculas(pvo_Registro.getReferenciaInterna());
        String vlc_TipoExoneracion = this.NormalizarMayusculas(pvo_Registro.getTipoExoneracion());
        String vlc_NumeroContratacion = this.NormalizarTexto(pvo_Registro.getNumeroContratacion());
        String vlc_OrdenCompraGeco = this.NormalizarTexto(pvo_Registro.getOrdenCompraGeco());
        String vlc_ContratoSicop = this.NormalizarTexto(pvo_Registro.getContratoSicop());
        String vlc_NumeroExonet = this.NormalizarTexto(pvo_Registro.getNumeroExonet());
        String vlc_AutorizacionExonet = this.NormalizarTexto(pvo_Registro.getNumeroAutorizacionExonet());
        String vlc_Observaciones = this.NormalizarTexto(pvo_Registro.getObservaciones());

        this.ValidarReferenciaInterna(vlc_ReferenciaInterna, vlo_EnTipoTramite);
        if (!TIPOS_EXONERACION.contains(vlc_TipoExoneracion)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_EXONERACION_INVALIDO);
        }
        this.ValidarDatoContractual(vlc_NumeroContratacion, Constantes.MENSAJE_NUMERO_CONTRATACION_REQUERIDO);
        this.ValidarDatoContractualOpcional(vlc_OrdenCompraGeco, Constantes.MENSAJE_ORDEN_COMPRA_GECO_INVALIDA);
        this.ValidarDatoContractualOpcional(vlc_ContratoSicop, Constantes.MENSAJE_CONTRATO_SICOP_INVALIDO);
        this.ValidarDatoContractualOpcional(vlc_NumeroExonet, Constantes.MENSAJE_NUMERO_EXONET_INVALIDO);
        this.ValidarDatoContractualOpcional(vlc_AutorizacionExonet, Constantes.MENSAJE_AUTORIZACION_EXONET_INVALIDA);
        if (vlc_Observaciones != null && vlc_Observaciones.length() > Constantes.LONGITUD_MAXIMA_OBSERVACIONES) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACIONES_INVALIDAS);
        }

        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvo_Registro.getIdUsuarioResponsable());
        EntEstadoTramite vlo_EnEstadoInicial = this.ObtenerEstadoInicial(vlo_EnTipoTramite.getIdTipoTramite());

        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setReferenciaInterna(vlc_ReferenciaInterna);
        vlo_EnTramite.setIdTipoTramite(vlo_EnTipoTramite.getIdTipoTramite());
        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoInicial.getIdEstadoTramite());
        vlo_EnTramite.setIdUsuarioResponsable(vlo_EnUsuario.getIdUsuario());
        vlo_EnTramite.setObservaciones(vlc_Observaciones == null || vlc_Observaciones.isEmpty() ? null : vlc_Observaciones);
        this.vgo_DalTramite.SIMT_TRAMITE_InsertarRegistro(vlo_EnTramite);

        EntExoneracion vlo_EnExoneracion = new EntExoneracion();
        vlo_EnExoneracion.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnExoneracion.setTipoExoneracion(vlc_TipoExoneracion);
        vlo_EnExoneracion.setNumeroContratacion(vlc_NumeroContratacion);
        vlo_EnExoneracion.setOrdenCompraGeco(vlc_OrdenCompraGeco);
        vlo_EnExoneracion.setContratoSicop(vlc_ContratoSicop);
        vlo_EnExoneracion.setNumeroExonet(vlc_NumeroExonet);
        vlo_EnExoneracion.setNumeroAutorizacionExonet(vlc_AutorizacionExonet);
        this.vgo_DalExoneracion.SIMT_EXONERACION_InsertarRegistro(vlo_EnExoneracion);

        this.AbrirHistorialEstado(vlo_EnTramite, vlo_EnEstadoInicial, vlo_EnUsuario.getIdUsuario());

        List<EntDocumento> vlo_LsDocumentos = this.RegistrarDocumentos(
                vlo_EnTramite, vlo_EnUsuario.getIdUsuario(), pvo_MpDocumentos);
        List<String> vlo_LsPendientes = this.ObtenerNombresPendientes(vlo_EnTramite.getIdTramite());

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_REGISTRO,
                Constantes.ENTIDAD_BITACORA_EXONERACION,
                vlo_EnTramite.getIdTramite(),
                null,
                this.DescribirRegistro(vlo_EnTramite, vlo_EnExoneracion, vlo_EnEstadoInicial,
                        vlo_LsDocumentos.size(), vlo_LsPendientes));

        EntRespuestaExoneracion vlo_Respuesta = new EntRespuestaExoneracion();
        vlo_Respuesta.setMensaje(vlo_LsPendientes.isEmpty()
                ? Constantes.MENSAJE_REGISTRO_EXONERACION_EXITOSO
                : Constantes.MENSAJE_REGISTRO_EXONERACION_CON_PENDIENTES);
        vlo_Respuesta.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_Respuesta.setReferenciaInterna(vlo_EnTramite.getReferenciaInterna());
        vlo_Respuesta.setTipoExoneracion(vlo_EnExoneracion.getTipoExoneracion());
        vlo_Respuesta.setEstado(vlo_EnEstadoInicial.getNombre());
        vlo_Respuesta.setUsuarioResponsable(vlo_EnUsuario.getNombreCompleto());
        vlo_Respuesta.setCantidadDocumentos(vlo_LsDocumentos.size());
        vlo_Respuesta.setDocumentosPendientes(vlo_LsPendientes);
        vlo_Respuesta.setFechaCreacion(vlo_EnTramite.getFechaCreacion());
        return vlo_Respuesta;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Adjunta documentación a una solicitud ya registrada, normalmente la que quedó
     *   pendiente, y registra la acción en la bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvn_IdUsuario usuario que adjunta la documentación
     * @param pvo_MpDocumentos archivos recibidos, agrupados por código de tipo de documento
     * @return expediente actualizado con su documentación, lo pendiente y su bitácora
     */
    @Transactional
    public EntExoneracionDetalle InsertarDocumentos(
            String pvc_ReferenciaInterna,
            Integer pvn_IdUsuario,
            Map<String, List<MultipartFile>> pvo_MpDocumentos) {

        EntTramite vlo_EnTramite = this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);

        List<EntDocumento> vlo_LsDocumentos = this.RegistrarDocumentos(
                vlo_EnTramite, vlo_EnUsuario.getIdUsuario(), pvo_MpDocumentos);
        if (vlo_LsDocumentos.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SIN_DOCUMENTOS_ADJUNTOS);
        }

        List<String> vlo_LsPendientes = this.ObtenerNombresPendientes(vlo_EnTramite.getIdTramite());
        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DOCUMENTO_AGREGADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                vlo_LsDocumentos.size() == 1 ? vlo_LsDocumentos.get(0).getIdDocumento() : null,
                null,
                this.DescribirDocumentosAgregados(vlo_LsDocumentos, vlo_LsPendientes));

        return this.ArmarDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene el expediente completo de una solicitud de exoneración.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @return expediente con su documentación, lo pendiente y su bitácora
     */
    @Transactional(readOnly = true)
    public EntExoneracionDetalle ObtenerExpediente(String pvc_ReferenciaInterna) {
        return this.ArmarDetalle(this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna));
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Corrige los datos contractuales de una solicitud ya registrada, tal como ocurre
     *   cuando los agentes revisan la documentación y verifican los números con el usuario. Solo
     *   cambia los datos indicados y anota en la bitácora el valor anterior y el nuevo.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvo_Correccion datos corregidos y usuario responsable del cambio
     * @return expediente actualizado con su documentación y su bitácora
     */
    @Transactional
    public EntExoneracionDetalle ModificarDatosContractuales(
            String pvc_ReferenciaInterna,
            EntCorreccionExoneracion pvo_Correccion) {

        if (pvo_Correccion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvo_Correccion.getIdUsuarioResponsable());
        EntExoneracion vlo_EnExoneracion =
                this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(vlo_EnTramite.getIdTramite());

        String vlc_TipoExoneracion = this.NormalizarMayusculas(pvo_Correccion.getTipoExoneracion());
        String vlc_NumeroContratacion = this.NormalizarTexto(pvo_Correccion.getNumeroContratacion());
        String vlc_OrdenCompraGeco = this.NormalizarTexto(pvo_Correccion.getOrdenCompraGeco());
        String vlc_ContratoSicop = this.NormalizarTexto(pvo_Correccion.getContratoSicop());
        String vlc_NumeroExonet = this.NormalizarTexto(pvo_Correccion.getNumeroExonet());
        String vlc_AutorizacionExonet = this.NormalizarTexto(pvo_Correccion.getNumeroAutorizacionExonet());
        String vlc_Observaciones = this.NormalizarTexto(pvo_Correccion.getObservaciones());

        List<String> vlo_LsCambios = new ArrayList<>();
        List<String> vlo_LsAnteriores = new ArrayList<>();

        if (vlc_TipoExoneracion != null && !vlc_TipoExoneracion.isEmpty()
                && !vlc_TipoExoneracion.equals(vlo_EnExoneracion.getTipoExoneracion())) {
            if (!TIPOS_EXONERACION.contains(vlc_TipoExoneracion)) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_EXONERACION_INVALIDO);
            }
            vlo_LsAnteriores.add("Tipo de exoneración: " + vlo_EnExoneracion.getTipoExoneracion());
            vlo_LsCambios.add("Tipo de exoneración: " + vlc_TipoExoneracion);
            vlo_EnExoneracion.setTipoExoneracion(vlc_TipoExoneracion);
        }

        if (vlc_NumeroContratacion != null
                && !vlc_NumeroContratacion.equals(vlo_EnExoneracion.getNumeroContratacion())) {
            this.ValidarDatoContractual(vlc_NumeroContratacion, Constantes.MENSAJE_NUMERO_CONTRATACION_REQUERIDO);
            vlo_LsAnteriores.add("Número de contratación: " + vlo_EnExoneracion.getNumeroContratacion());
            vlo_LsCambios.add("Número de contratación: " + vlc_NumeroContratacion);
            vlo_EnExoneracion.setNumeroContratacion(vlc_NumeroContratacion);
        }

        if (vlc_OrdenCompraGeco != null
                && !vlc_OrdenCompraGeco.equals(vlo_EnExoneracion.getOrdenCompraGeco())) {
            this.ValidarDatoContractualOpcional(vlc_OrdenCompraGeco, Constantes.MENSAJE_ORDEN_COMPRA_GECO_INVALIDA);
            vlo_LsAnteriores.add("Orden de compra GECO: "
                    + this.DescribirValor(vlo_EnExoneracion.getOrdenCompraGeco()));
            vlo_LsCambios.add("Orden de compra GECO: " + this.DescribirValor(vlc_OrdenCompraGeco));
            vlo_EnExoneracion.setOrdenCompraGeco(vlc_OrdenCompraGeco.isEmpty() ? null : vlc_OrdenCompraGeco);
        }

        if (vlc_ContratoSicop != null
                && !vlc_ContratoSicop.equals(vlo_EnExoneracion.getContratoSicop())) {
            this.ValidarDatoContractualOpcional(vlc_ContratoSicop, Constantes.MENSAJE_CONTRATO_SICOP_INVALIDO);
            vlo_LsAnteriores.add("Contrato SICOP: " + vlo_EnExoneracion.getContratoSicop());
            vlo_LsCambios.add("Contrato SICOP: " + vlc_ContratoSicop);
            vlo_EnExoneracion.setContratoSicop(vlc_ContratoSicop);
        }

        if (vlc_NumeroExonet != null
                && !vlc_NumeroExonet.equals(this.NormalizarTexto(vlo_EnExoneracion.getNumeroExonet()))) {
            this.ValidarDatoContractualOpcional(vlc_NumeroExonet, Constantes.MENSAJE_NUMERO_EXONET_INVALIDO);
            vlo_LsAnteriores.add("Número de solicitud EXONET: "
                    + this.DescribirValor(vlo_EnExoneracion.getNumeroExonet()));
            vlo_LsCambios.add("Número de solicitud EXONET: " + this.DescribirValor(vlc_NumeroExonet));
            vlo_EnExoneracion.setNumeroExonet(vlc_NumeroExonet.isEmpty() ? null : vlc_NumeroExonet);
        }

        if (vlc_AutorizacionExonet != null
                && !vlc_AutorizacionExonet.equals(
                        this.NormalizarTexto(vlo_EnExoneracion.getNumeroAutorizacionExonet()))) {
            this.ValidarDatoContractualOpcional(vlc_AutorizacionExonet,
                    Constantes.MENSAJE_AUTORIZACION_EXONET_INVALIDA);
            vlo_LsAnteriores.add("Número de autorización EXONET: "
                    + this.DescribirValor(vlo_EnExoneracion.getNumeroAutorizacionExonet()));
            vlo_LsCambios.add("Número de autorización EXONET: "
                    + this.DescribirValor(vlc_AutorizacionExonet));
            vlo_EnExoneracion.setNumeroAutorizacionExonet(
                    vlc_AutorizacionExonet.isEmpty() ? null : vlc_AutorizacionExonet);
        }

        if (vlc_Observaciones != null
                && !vlc_Observaciones.equals(vlo_EnTramite.getObservaciones())) {
            if (vlc_Observaciones.length() > Constantes.LONGITUD_MAXIMA_OBSERVACIONES) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACIONES_INVALIDAS);
            }
            vlo_LsAnteriores.add("Observaciones: " + (vlo_EnTramite.getObservaciones() == null
                    ? "sin observaciones" : vlo_EnTramite.getObservaciones()));
            vlo_LsCambios.add("Observaciones: " + vlc_Observaciones);
            vlo_EnTramite.setObservaciones(vlc_Observaciones);
            this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);
        }

        if (vlo_LsCambios.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_CONTRACTUALES_SIN_CAMBIO);
        }

        this.vgo_DalExoneracion.SIMT_EXONERACION_ModificarRegistro(vlo_EnExoneracion);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DATOS_MODIFICADOS,
                Constantes.ENTIDAD_BITACORA_EXONERACION,
                vlo_EnTramite.getIdTramite(),
                String.join(". ", vlo_LsAnteriores) + ".",
                String.join(". ", vlo_LsCambios) + ".");

        return this.ArmarDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Retira de la solicitud un documento cargado por error. El archivo se conserva
     *   como inactivo y la eliminación queda anotada en la bitácora.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuario usuario que realiza la acción
     * @return expediente actualizado con lo que queda pendiente
     */
    @Transactional
    public EntExoneracionDetalle EliminarDocumento(
            String pvc_ReferenciaInterna,
            Integer pvn_IdDocumento,
            Integer pvn_IdUsuario) {

        EntTramite vlo_EnTramite = this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna);
        this.vgo_BllDocumento.EliminarDocumento(
                vlo_EnTramite.getIdTramite(), pvn_IdDocumento, pvn_IdUsuario);
        return this.ArmarDetalle(vlo_EnTramite);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene la bitácora de una solicitud de exoneración.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @return registros vigentes de la bitácora
     */
    @Transactional(readOnly = true)
    public List<EntMovimientoBitacora> ObtenerBitacora(String pvc_ReferenciaInterna) {
        return this.vgo_BllBitacora.ObtenerPorTramite(
                this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna).getIdTramite());
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Agrega un comentario a la bitácora de una solicitud de exoneración.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvo_Comentario texto del comentario y usuario que lo registra
     * @return bitácora vigente de la solicitud
     */
    @Transactional
    public List<EntMovimientoBitacora> InsertarComentario(String pvc_ReferenciaInterna, EntComentarioBitacora pvo_Comentario) {
        return this.vgo_BllBitacora.InsertarComentario(
                this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna).getIdTramite(), pvo_Comentario);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Elimina un comentario registrado por error en la bitácora de una solicitud.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @param pvn_IdBitacora identificador del comentario
     * @param pvn_IdUsuario usuario que elimina el comentario
     * @return bitácora vigente de la solicitud
     */
    @Transactional
    public List<EntMovimientoBitacora> EliminarComentario(
            String pvc_ReferenciaInterna,
            Integer pvn_IdBitacora,
            Integer pvn_IdUsuario) {
        return this.vgo_BllBitacora.EliminarComentario(
                this.ObtenerTramiteExoneracion(pvc_ReferenciaInterna).getIdTramite(), pvn_IdBitacora, pvn_IdUsuario);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 25/10/2026
     * Descripción: Obtiene los expedientes de exoneración registrados, para la pantalla de entrada
     *   del módulo desde la que se abre cada expediente.
     *
     * @return expedientes del más reciente al más antiguo
     */
    @Transactional(readOnly = true)
    public List<EntResumenExoneracion> ObtenerListaExpedientes() {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramiteExoneracion();
        return this.vgo_DalTramite.SIMT_TRAMITE_ObtenerListaExoneraciones(
                vlo_EnTipoTramite.getIdTipoTramite(),
                Constantes.ESTADO_DOCUMENTO_ACTIVO);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene el tipo de trámite de exoneración y comprueba que el catálogo le exija
     *   contrato SICOP y orden de compra, que la HU-018 solicita como datos de la solicitud.
     *
     * @return tipo de trámite de exoneración
     */
    private EntTipoTramite ObtenerTipoTramiteExoneracion() {
        EntTipoTramite vlo_EnTipoTramite = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_EXONERACION);
        boolean vlb_Configurado = vlo_EnTipoTramite != null
                && Boolean.TRUE.equals(vlo_EnTipoTramite.getRequiereSicop());
        if (!vlb_Configurado) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_TRAMITE_EXONERACION_NO_CONFIGURADO);
        }
        return vlo_EnTipoTramite;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Comprueba que la referencia interna lleve el prefijo de la exoneración, que
     *   tenga un consecutivo después del prefijo, que respete la longitud de la columna y que no
     *   la haya utilizado ningún otro trámite del sistema.
     *
     * @param pvc_ReferenciaInterna referencia interna normalizada
     * @param pvo_EnTipoTramite tipo de trámite que define el prefijo
     */
    private void ValidarReferenciaInterna(String pvc_ReferenciaInterna, EntTipoTramite pvo_EnTipoTramite) {
        String vlc_Prefijo = pvo_EnTipoTramite.getPrefijoReferencia();
        boolean vlb_FormatoValido = pvc_ReferenciaInterna != null
                && pvc_ReferenciaInterna.startsWith(vlc_Prefijo)
                && pvc_ReferenciaInterna.length() > vlc_Prefijo.length()
                && pvc_ReferenciaInterna.length() <= Constantes.LONGITUD_MAXIMA_REFERENCIA_INTERNA;
        if (!vlb_FormatoValido) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_EXONERACION_INVALIDA);
        }
        if (this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(pvc_ReferenciaInterna)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DUPLICADA);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Comprueba que un dato contractual venga indicado y respete su longitud.
     *
     * @param pvc_Dato dato normalizado
     * @param pvc_Mensaje mensaje que se informa si el dato no es válido
     */
    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Valida un dato contractual que la persona usuaria puede dejar en blanco,
     *   como la orden de compra GECO o los números de EXONET. Solo revisa la longitud.
     *
     * @param pvc_Dato dato digitado, ya normalizado
     * @param pvc_Mensaje mensaje que se muestra cuando el dato excede la longitud permitida
     */
    private void ValidarDatoContractualOpcional(String pvc_Dato, String pvc_Mensaje) {
        if (pvc_Dato != null && pvc_Dato.length() > Constantes.LONGITUD_MAXIMA_DATO_CONTRACTUAL) {
            throw new ExcepcionNegocio(pvc_Mensaje);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 22/09/2026
     * Descripción: Describe un dato opcional en la bitácora, para que un campo vacío se lea
     *   como "sin dato" y no como un espacio en blanco.
     *
     * @param pvc_Dato dato almacenado o digitado
     * @return el dato o el texto "sin dato" cuando viene vacío
     */
    private String DescribirValor(String pvc_Dato) {
        return pvc_Dato == null || pvc_Dato.isEmpty() ? "sin dato" : pvc_Dato;
    }

    private void ValidarDatoContractual(String pvc_Dato, String pvc_Mensaje) {
        if (pvc_Dato == null || pvc_Dato.isEmpty() || pvc_Dato.length() > Constantes.LONGITUD_MAXIMA_DATO_CONTRACTUAL) {
            throw new ExcepcionNegocio(pvc_Mensaje);
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene un usuario registrado.
     *
     * @param pvn_IdUsuario identificador del usuario
     * @return usuario registrado
     */
    private EntUsuario ObtenerUsuario(Integer pvn_IdUsuario) {
        EntUsuario vlo_EnUsuario = pvn_IdUsuario == null
                ? null
                : this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario);
        if (vlo_EnUsuario == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        return vlo_EnUsuario;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene el estado con el que nace la solicitud, definido en el catálogo.
     *
     * @param pvn_IdTipoTramite identificador del tipo de trámite
     * @return estado inicial
     */
    private EntEstadoTramite ObtenerEstadoInicial(Integer pvn_IdTipoTramite) {
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(pvn_IdTipoTramite);
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_INICIAL_EXONERACION_NO_CONFIGURADO);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Recupera el trámite de una solicitud de exoneración por su referencia interna y
     *   comprueba que efectivamente sea una exoneración.
     *
     * @param pvc_ReferenciaInterna referencia interna de la solicitud
     * @return trámite de la solicitud
     */
    private EntTramite ObtenerTramiteExoneracion(String pvc_ReferenciaInterna) {
        String vlc_Referencia = this.NormalizarMayusculas(pvc_ReferenciaInterna);
        EntTramite vlo_EnTramite = vlc_Referencia == null || vlc_Referencia.isEmpty()
                ? null
                : this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(vlc_Referencia);
        if (vlo_EnTramite == null
                || this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(vlo_EnTramite.getIdTramite()) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_NO_ENCONTRADA);
        }
        return vlo_EnTramite;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Registra el primer movimiento del historial de estados, con estado anterior nulo.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvo_EnEstadoInicial estado con el que nace el expediente
     * @param pvn_IdUsuario usuario responsable del registro
     */
    private void AbrirHistorialEstado(EntTramite pvo_EnTramite, EntEstadoTramite pvo_EnEstadoInicial, Integer pvn_IdUsuario) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstadoInicial.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion(Constantes.JUSTIFICACION_REGISTRO_INICIAL);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Almacena los archivos recibidos y los registra en SIMF_DOCUMENTO. Los tipos que no
     *   admite el expediente de exoneración se descartan. Si la transacción no se confirma, los
     *   archivos escritos se borran para no dejar documentos en disco sin registro.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_MpDocumentos archivos agrupados por código de tipo de documento
     * @return documentos registrados
     */
    private List<EntDocumento> RegistrarDocumentos(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            Map<String, List<MultipartFile>> pvo_MpDocumentos) {

        List<EntDocumento> vlo_LsRegistrados = new ArrayList<>();
        if (pvo_MpDocumentos == null) {
            return vlo_LsRegistrados;
        }
        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);

        for (Map.Entry<String, List<MultipartFile>> vlo_EnTipo : pvo_MpDocumentos.entrySet()) {
            if (!DOCUMENTOS_ADMITIDOS.contains(vlo_EnTipo.getKey()) || vlo_EnTipo.getValue() == null) {
                continue;
            }
            for (MultipartFile vlo_Archivo : vlo_EnTipo.getValue()) {
                if (vlo_Archivo != null && !vlo_Archivo.isEmpty()) {
                    vlo_LsRegistrados.add(this.GuardarDocumento(
                            pvo_EnTramite, pvn_IdUsuarioCarga, vlo_Archivo, vlo_EnTipo.getKey(), vlo_LsRutasEscritas));
                }
            }
        }
        return vlo_LsRegistrados;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Guarda un archivo y persiste su registro documental. Si el expediente ya tiene un
     *   documento vigente del mismo tipo y nombre, el nuevo se registra como la versión siguiente y
     *   el anterior queda inactivo, sin perderse del expediente.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuarioCarga usuario que adjunta la documentación
     * @param pvo_Archivo archivo recibido
     * @param pvc_CodigoTipoDocumento código del tipo de documento
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     * @return documento registrado
     */
    private EntDocumento GuardarDocumento(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuarioCarga,
            MultipartFile pvo_Archivo,
            String pvc_CodigoTipoDocumento,
            List<String> pvo_LsRutasEscritas) {

        EntTipoDocumento vlo_EnTipoDocumento = this.ObtenerTipoDocumento(pvc_CodigoTipoDocumento);
        String vlc_NombreArchivo = this.vgo_AlmacenamientoDocumentos.DepurarNombreArchivo(pvo_Archivo.getOriginalFilename());
        EntDocumento vlo_EnVersionAnterior = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerVersionVigente(
                pvo_EnTramite.getIdTramite(), vlo_EnTipoDocumento.getIdTipoDocumento(),
                vlc_NombreArchivo, Constantes.ESTADO_DOCUMENTO_ACTIVO);

        String vlc_RutaArchivo = this.vgo_AlmacenamientoDocumentos
                .GuardarArchivo(pvo_Archivo, pvo_EnTramite.getReferenciaInterna());
        pvo_LsRutasEscritas.add(vlc_RutaArchivo);

        EntDocumento vlo_EnDocumento = new EntDocumento();
        vlo_EnDocumento.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnDocumento.setIdTipoDocumento(vlo_EnTipoDocumento.getIdTipoDocumento());
        vlo_EnDocumento.setIdUsuarioCarga(pvn_IdUsuarioCarga);
        vlo_EnDocumento.setNombreArchivo(vlc_NombreArchivo);
        vlo_EnDocumento.setRutaArchivo(vlc_RutaArchivo);
        vlo_EnDocumento.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_ACTIVO);
        if (vlo_EnVersionAnterior == null) {
            vlo_EnDocumento.setVersion((short) 1); // 1 = primera versión del documento
        } else {
            vlo_EnDocumento.setVersion((short) (vlo_EnVersionAnterior.getVersion() + 1));
            vlo_EnDocumento.setIdDocumentoAnterior(vlo_EnVersionAnterior.getIdDocumento());
            vlo_EnVersionAnterior.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_INACTIVO);
            this.vgo_DalDocumento.SIMF_DOCUMENTO_ModificarRegistro(vlo_EnVersionAnterior);
        }
        this.vgo_DalDocumento.SIMF_DOCUMENTO_InsertarRegistro(vlo_EnDocumento);
        return vlo_EnDocumento;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Programa el borrado de los archivos escritos si la transacción termina en rollback.
     *
     * @param pvo_LsRutasEscritas rutas relativas escritas durante la transacción
     */
    private void RegistrarLimpiezaSiFallaLaTransaccion(List<String> pvo_LsRutasEscritas) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int pvn_EstadoTransaccion) {
                if (pvn_EstadoTransaccion != TransactionSynchronization.STATUS_ROLLED_BACK) {
                    return;
                }
                for (String vlc_Ruta : pvo_LsRutasEscritas) {
                    vgo_AlmacenamientoDocumentos.EliminarArchivo(vlc_Ruta);
                }
            }
        });
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene los nombres de los documentos obligatorios que el expediente todavía no
     *   tiene vigentes. Se calcula en el servidor, sin confiar en lo que informe el formulario.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return nombres de los documentos obligatorios pendientes
     */
    private List<String> ObtenerNombresPendientes(Integer pvn_IdTramite) {
        List<String> vlo_LsPresentes = this.vgo_DalDocumento
                .SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(pvn_IdTramite, Constantes.ESTADO_DOCUMENTO_ACTIVO);
        List<String> vlo_LsPendientes = new ArrayList<>();
        for (String vlc_Codigo : DOCUMENTOS_OBLIGATORIOS) {
            if (!vlo_LsPresentes.contains(vlc_Codigo)) {
                vlo_LsPendientes.add(this.ObtenerTipoDocumento(vlc_Codigo).getNombre());
            }
        }
        return vlo_LsPendientes;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Obtiene un tipo de documento del catálogo.
     *
     * @param pvc_Codigo código del tipo de documento
     * @return tipo de documento
     */
    private EntTipoDocumento ObtenerTipoDocumento(String pvc_Codigo) {
        EntTipoDocumento vlo_EnTipo = this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo(pvc_Codigo);
        if (vlo_EnTipo == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_DOCUMENTO_NO_CONFIGURADO);
        }
        return vlo_EnTipo;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Reúne los datos, la documentación, lo pendiente y la bitácora de la solicitud.
     *
     * @param pvo_EnTramite trámite de la solicitud
     * @return expediente completo
     */
    private EntExoneracionDetalle ArmarDetalle(EntTramite pvo_EnTramite) {
        EntExoneracion vlo_EnExoneracion = this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvo_EnTramite.getIdTramite());
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
        EntUsuario vlo_EnUsuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvo_EnTramite.getIdUsuarioResponsable());

        EntExoneracionDetalle vlo_Detalle = new EntExoneracionDetalle();
        vlo_Detalle.setIdTramite(pvo_EnTramite.getIdTramite()); // HU-020
        vlo_Detalle.setReferenciaInterna(pvo_EnTramite.getReferenciaInterna());
        vlo_Detalle.setEstado(vlo_EnEstado == null ? null : vlo_EnEstado.getNombre());
        vlo_Detalle.setTipoExoneracion(vlo_EnExoneracion.getTipoExoneracion());
        vlo_Detalle.setNumeroContratacion(vlo_EnExoneracion.getNumeroContratacion());
        vlo_Detalle.setOrdenCompraGeco(vlo_EnExoneracion.getOrdenCompraGeco());
        vlo_Detalle.setContratoSicop(vlo_EnExoneracion.getContratoSicop());
        vlo_Detalle.setNumeroExonet(vlo_EnExoneracion.getNumeroExonet());
        vlo_Detalle.setNumeroAutorizacionExonet(vlo_EnExoneracion.getNumeroAutorizacionExonet());
        vlo_Detalle.setObservaciones(pvo_EnTramite.getObservaciones());
        vlo_Detalle.setTipoPartidoResponsable(pvo_EnTramite.getTipoPartidoResponsable());
        vlo_Detalle.setPartidoResponsable(pvo_EnTramite.getPartidoResponsable());
        vlo_Detalle.setImpuestosPagados(pvo_EnTramite.getImpuestosPagados());
        vlo_Detalle.setImpuestosExonerados(pvo_EnTramite.getImpuestosExonerados());
        vlo_Detalle.setMontoBodegaje(pvo_EnTramite.getMontoBodegaje());
        vlo_Detalle.setUsuarioResponsable(vlo_EnUsuario == null ? null : vlo_EnUsuario.getNombreCompleto());
        vlo_Detalle.setFechaCreacion(pvo_EnTramite.getFechaCreacion());
        vlo_Detalle.setDocumentos(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerListaExpediente(pvo_EnTramite.getIdTramite()));
        vlo_Detalle.setDocumentosPendientes(this.ObtenerNombresPendientes(pvo_EnTramite.getIdTramite()));
        vlo_Detalle.setBitacora(this.vgo_BllBitacora.ObtenerPorTramite(pvo_EnTramite.getIdTramite()));
        return vlo_Detalle;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Redacta el registro de bitácora del alta de la solicitud, legible al imprimirse.
     *
     * @param pvo_EnTramite trámite registrado
     * @param pvo_EnExoneracion datos de exoneración registrados
     * @param pvo_EnEstado estado inicial asignado
     * @param pvn_CantidadDocumentos documentos adjuntos
     * @param pvo_LsPendientes documentos obligatorios pendientes
     * @return descripción del registro
     */
    private String DescribirRegistro(
            EntTramite pvo_EnTramite,
            EntExoneracion pvo_EnExoneracion,
            EntEstadoTramite pvo_EnEstado,
            int pvn_CantidadDocumentos,
            List<String> pvo_LsPendientes) {
        StringBuilder vlo_Texto = new StringBuilder()
                .append("Solicitud ").append(pvo_EnTramite.getReferenciaInterna())
                .append(" registrada con tipo ").append(pvo_EnExoneracion.getTipoExoneracion())
                .append(" y estado inicial ").append(pvo_EnEstado.getNombre()).append(". ")
                .append("Documentos adjuntos: ").append(pvn_CantidadDocumentos).append(".");
        if (!pvo_LsPendientes.isEmpty()) {
            vlo_Texto.append(" Documentación obligatoria pendiente: ")
                    .append(String.join(", ", pvo_LsPendientes)).append(".");
        }
        return vlo_Texto.toString();
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 15/09/2026
     * Descripción: Redacta el registro de bitácora de la documentación agregada.
     *
     * @param pvo_LsDocumentos documentos agregados
     * @param pvo_LsPendientes documentos obligatorios que siguen pendientes
     * @return descripción de la acción
     */
    private String DescribirDocumentosAgregados(List<EntDocumento> pvo_LsDocumentos, List<String> pvo_LsPendientes) {
        List<String> vlo_LsArchivos = new ArrayList<>();
        for (EntDocumento vlo_EnDocumento : pvo_LsDocumentos) {
            vlo_LsArchivos.add(vlo_EnDocumento.getNombreArchivo() + " (versión " + vlo_EnDocumento.getVersion() + ")");
        }
        String vlc_Texto = "Documentación agregada: " + String.join(", ", vlo_LsArchivos) + ".";
        return pvo_LsPendientes.isEmpty()
                ? vlc_Texto + " La documentación obligatoria quedó completa."
                : vlc_Texto + " Sigue pendiente: " + String.join(", ", pvo_LsPendientes) + ".";
    }

    private String NormalizarTexto(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim();
    }

    private String NormalizarMayusculas(String pvc_Texto) {
        String vlc_Texto = this.NormalizarTexto(pvc_Texto);
        return vlc_Texto == null ? null : vlc_Texto.toUpperCase(Locale.ROOT);
    }
}
