package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPais;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntAgenteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDatosTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExpedienteTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExtensionPlazoTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFilaTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialProrrogaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntIndicadorVencimiento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionesTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPais;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntProrrogaTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenTramiteTemporal;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramiteTemporal;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.Enumerados.TipoTramiteTemporal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Reglas de registro y de extensión del plazo de los trámites temporales (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende el expediente de la HU-025 Registrar importaciones temporales. El
 *     registro crea un expediente de SIMT_TRAMITE con la sigla IT-, su historial de estados, su
 *     bitácora y su documentación; cualquier usuario puede registrar y los agentes son opcionales.
 *     Se agregan la lista de trámites activos con su cuenta regresiva, el expediente, el cambio
 *     rápido de estado, los datos de aduana que se completan después, la prórroga y la
 *     documentación. Las alertas pasan a BllAlertaTemporal; la extensión del plazo por cantidad y
 *     unidad, con la regla del contrato, se conserva.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende también la HU-026 Registrar exportaciones temporales. Cada operación
 *     recibe el tipo de trámite temporal, que define la sigla, los estados con reglas propias, los
 *     DUA de inicio y de cierre, los documentos admitidos y los mensajes. La exportación registra
 *     además la garantía, el peso, el país de destino, las notificaciones y los datos de la
 *     reimportación; su vencimiento estándar es la fecha del DUA de exportación más 6 meses.
 *     El vencimiento debe ser posterior a la fecha del DUA de inicio en ambos tipos.
 */
@Service
public class BllTramiteTemporal {

    private static final String UNIDAD_DIAS = "DIAS";
    private static final String UNIDAD_MESES = "MESES";
    private static final String UNIDAD_ANOS = "ANOS";
    private static final int MAXIMO_DIAS_PLAZO = 3650;
    private static final int MAXIMO_MESES_PLAZO = 120;
    private static final int MAXIMO_ANOS_PLAZO = 99;

    private static final Set<String> ESTADOS_MERCANCIA = Set.of(
            Constantes.ESTADO_MERCANCIA_NUEVA,
            Constantes.ESTADO_MERCANCIA_USADA,
            Constantes.ESTADO_MERCANCIA_REACONDICIONADA);

    private final DalTramite vgo_DalTramite;
    private final DalTramiteTemporal vgo_DalTramiteTemporal;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalMoneda vgo_DalMoneda;
    private final DalPais vgo_DalPais;
    private final DalDocumento vgo_DalDocumento;
    private final DalUsuario vgo_DalUsuario;
    private final AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;
    private final BllDocumento vgo_BllDocumento;
    private final BllBitacora vgo_BllBitacora;
    private final BllAlertaTemporal vgo_BllAlertaTemporal;

    public BllTramiteTemporal(
            DalTramite pvo_DalTramite,
            DalTramiteTemporal pvo_DalTramiteTemporal,
            DalTipoTramite pvo_DalTipoTramite,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalMoneda pvo_DalMoneda,
            DalPais pvo_DalPais,
            DalDocumento pvo_DalDocumento,
            DalUsuario pvo_DalUsuario,
            AlmacenamientoDocumentos pvo_AlmacenamientoDocumentos,
            BllDocumento pvo_BllDocumento,
            BllBitacora pvo_BllBitacora,
            BllAlertaTemporal pvo_BllAlertaTemporal) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalTramiteTemporal = pvo_DalTramiteTemporal;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalMoneda = pvo_DalMoneda;
        this.vgo_DalPais = pvo_DalPais;
        this.vgo_DalDocumento = pvo_DalDocumento;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_AlmacenamientoDocumentos = pvo_AlmacenamientoDocumentos;
        this.vgo_BllDocumento = pvo_BllDocumento;
        this.vgo_BllBitacora = pvo_BllBitacora;
        this.vgo_BllAlertaTemporal = pvo_BllAlertaTemporal;
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Registra un trámite temporal.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: crea el expediente de la importación temporal (HU-025) con el estado inicial
     *     del catálogo, su historial, sus agentes opcionales, su documentación y su bitácora. Los
     *     datos de la aduana y el vencimiento son opcionales porque llegan después del registro.
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: registra también la exportación temporal (HU-026), que exige la garantía, el
     *     peso y el país de destino.
     *
     * @param pvo_Tipo tipo de trámite temporal que se registra
     * @param pvo_Registro datos del formulario
     * @param pvo_MpDocumentos archivos agrupados por código de tipo de documento
     * @param pvn_IdUsuario usuario de la sesión, que queda como responsable del expediente
     * @return confirmación del registro
     */
    @Transactional
    public EntRespuestaTramiteTemporal InsertarRegistro(
            TipoTramiteTemporal pvo_Tipo,
            EntRegistroTramiteTemporal pvo_Registro,
            Map<String, List<MultipartFile>> pvo_MpDocumentos,
            Integer pvn_IdUsuario) {

        if (pvo_Registro == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        String vlc_Referencia = this.NormalizarCodigo(pvo_Registro.referenciaInterna());
        this.ValidarReferencia(pvo_Tipo, vlc_Referencia, vlo_EnTipoTramite);

        String vlc_Unidad = this.Normalizar(pvo_Registro.unidadSolicitante());
        String vlc_Oficio = this.Normalizar(pvo_Registro.oficioSolicitud());
        String vlc_Descripcion = this.Normalizar(pvo_Registro.descripcionMercancia());
        String vlc_EstadoMercancia = this.NormalizarCodigo(pvo_Registro.estadoMercancia());
        String vlc_Aduana = this.Normalizar(pvo_Registro.aduanaControl());
        String vlc_Observaciones = this.Normalizar(pvo_Registro.observaciones());

        this.ValidarRequerido(vlc_Unidad, Constantes.LONGITUD_MAXIMA_UNIDAD_SOLICITANTE,
                Constantes.MENSAJE_UNIDAD_SOLICITANTE_REQUERIDA);
        this.ValidarRequerido(vlc_Oficio, Constantes.LONGITUD_MAXIMA_NUMERO_ADUANA,
                Constantes.MENSAJE_OFICIO_SOLICITUD_REQUERIDO);
        if (pvo_Registro.fechaRecepcion() == null
                || pvo_Registro.fechaRecepcion().isAfter(this.vgo_BllAlertaTemporal.Hoy())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_FECHA_RECEPCION_INVALIDA);
        }
        this.ValidarRequerido(vlc_Descripcion, Constantes.LONGITUD_MAXIMA_DESCRIPCION_MERCANCIA,
                Constantes.MENSAJE_DESCRIPCION_MERCANCIA_REQUERIDA);
        if (!ESTADOS_MERCANCIA.contains(vlc_EstadoMercancia)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_MERCANCIA_INVALIDO);
        }
        if (pvo_Registro.valorEquipo() == null || pvo_Registro.valorEquipo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_VALOR_EQUIPO_INVALIDO);
        }
        this.ValidarLongitud(vlc_Aduana, Constantes.LONGITUD_MAXIMA_ADUANA_CONTROL,
                Constantes.MENSAJE_ADUANA_CONTROL_INVALIDA);
        this.ValidarLongitud(vlc_Observaciones, Constantes.LONGITUD_MAXIMA_OBSERVACIONES,
                Constantes.MENSAJE_OBSERVACIONES_INVALIDAS);

        EntTramiteTemporal vlo_EnTemporal = new EntTramiteTemporal();
        vlo_EnTemporal.setUnidadSolicitante(vlc_Unidad);
        vlo_EnTemporal.setOficioSolicitud(vlc_Oficio);
        vlo_EnTemporal.setFechaRecepcion(pvo_Registro.fechaRecepcion());
        vlo_EnTemporal.setEstadoMercancia(vlc_EstadoMercancia);
        vlo_EnTemporal.setNumeroFactura(this.Normalizar(pvo_Registro.numeroFactura()));
        vlo_EnTemporal.setOficioAduana(this.Normalizar(pvo_Registro.oficioAduana()));
        vlo_EnTemporal.setNumeroGestion(this.Normalizar(pvo_Registro.numeroGestion()));
        vlo_EnTemporal.setResolucionAutorizacion(this.Normalizar(pvo_Registro.resolucionAutorizacion()));
        vlo_EnTemporal.setNumeroDuaInicio(this.Normalizar(pvo_Registro.numeroDuaInicio()));
        vlo_EnTemporal.setFechaDuaInicio(pvo_Registro.fechaDuaInicio());
        vlo_EnTemporal.setFechaVencimiento(pvo_Registro.fechaVencimiento());
        if (this.EsExportacion(pvo_Tipo)) {
            this.AsignarDatosDeSalida(vlo_EnTemporal, pvo_Registro);
        }
        this.ValidarDatosAduana(vlo_EnTemporal);
        this.AsignarPlazoEstandar(pvo_Tipo, vlo_EnTemporal);
        this.ValidarTiposDocumento(pvo_Tipo, pvo_MpDocumentos, false);

        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        Set<Integer> vlo_LsAgentes = this.ValidarAgentes(pvo_Registro.agentesResponsables());
        EntMoneda vlo_EnMoneda = this.ObtenerMoneda(pvo_Registro.moneda());
        EntEstadoTramite vlo_EnEstadoInicial = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(vlo_EnTipoTramite.getIdTipoTramite());
        if (vlo_EnEstadoInicial == null) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeEstadoInicialNoConfigurado());
        }

        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setReferenciaInterna(vlc_Referencia);
        vlo_EnTramite.setIdTipoTramite(vlo_EnTipoTramite.getIdTipoTramite());
        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoInicial.getIdEstadoTramite());
        vlo_EnTramite.setIdUsuarioResponsable(vlo_EnUsuario.getIdUsuario());
        vlo_EnTramite.setIdMoneda(vlo_EnMoneda.getIdMoneda());
        vlo_EnTramite.setValorEstimado(pvo_Registro.valorEquipo());
        vlo_EnTramite.setDescripcion(vlc_Descripcion);
        vlo_EnTramite.setRegimenAduanero(pvo_Tipo.getRegimen());
        vlo_EnTramite.setAduanaControl(vlc_Aduana);
        vlo_EnTramite.setObservaciones(vlc_Observaciones);
        this.vgo_DalTramite.SIMT_TRAMITE_InsertarRegistro(vlo_EnTramite);

        vlo_EnTemporal.setIdTramite(vlo_EnTramite.getIdTramite());
        this.vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_InsertarRegistro(vlo_EnTemporal);
        for (Integer vln_IdAgente : vlo_LsAgentes) {
            this.vgo_DalTramiteTemporal.SIMT_TEMPORAL_AGENTE_Insertar(vlo_EnTramite.getIdTramite(), vln_IdAgente);
        }

        this.RegistrarMovimientoEstado(vlo_EnTramite, null, vlo_EnEstadoInicial,
                vlo_EnUsuario.getIdUsuario(), Constantes.JUSTIFICACION_REGISTRO_INICIAL);

        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.vgo_BllDocumento.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);
        List<EntDocumento> vlo_LsDocumentos = this.GuardarDocumentos(
                vlo_EnTramite, vlo_EnUsuario.getIdUsuario(), pvo_MpDocumentos, vlo_LsRutasEscritas);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_REGISTRO,
                Constantes.ENTIDAD_BITACORA_TRAMITE_TEMPORAL,
                vlo_EnTramite.getIdTramite(),
                null,
                pvo_Tipo.getRegimen() + " solicitada por " + vlc_Unidad + " con el oficio " + vlc_Oficio
                        + ". Estado inicial: " + vlo_EnEstadoInicial.getNombre()
                        + ". Documentos asociados: " + vlo_LsDocumentos.size() + ".");

        return new EntRespuestaTramiteTemporal(
                vlo_EnTemporal.getFechaVencimiento() == null
                        ? pvo_Tipo.getMensajeRegistroSinVencimiento()
                        : pvo_Tipo.getMensajeRegistroExitoso(),
                vlo_EnTramite.getReferenciaInterna(),
                vlo_EnEstadoInicial.getNombre(),
                vlo_LsDocumentos.size(),
                vlo_EnTramite.getFechaCreacion());
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Obtiene la lista de trámites temporales.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: lista las importaciones temporales según su alcance (HU-025). La vista de
     *     trámites activos muestra los expedientes abiertos, por llegar o en gestión, ordenados por
     *     la cercanía de su vencimiento; los que aún no tienen vencimiento van al final.
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: lista también las exportaciones temporales (HU-026), con su país de destino.
     *
     * @param pvo_Tipo tipo de trámite temporal que se lista
     * @param pvc_Alcance ACTIVOS, CERRADOS o TODOS; vacío equivale a ACTIVOS
     * @return expedientes con su cuenta regresiva
     */
    @Transactional(readOnly = true)
    public List<EntResumenTramiteTemporal> ObtenerLista(TipoTramiteTemporal pvo_Tipo, String pvc_Alcance) {
        String vlc_Alcance = this.NormalizarCodigo(pvc_Alcance);
        if (vlc_Alcance == null) {
            vlc_Alcance = Constantes.ALCANCE_TEMPORAL_ACTIVOS;
        }
        if (!Set.of(Constantes.ALCANCE_TEMPORAL_ACTIVOS, Constantes.ALCANCE_TEMPORAL_CERRADOS,
                Constantes.ALCANCE_TEMPORAL_TODOS).contains(vlc_Alcance)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ALCANCE_TEMPORAL_INVALIDO);
        }

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        LocalDate vld_Hoy = this.vgo_BllAlertaTemporal.Hoy();
        String vlc_AlcanceFinal = vlc_Alcance;
        List<EntResumenTramiteTemporal> vlo_LsResumen = this.vgo_DalTramiteTemporal
                .SIMT_TRAMITE_TEMPORAL_ObtenerLista(vlo_EnTipoTramite.getIdTipoTramite()).stream()
                .filter(pvo_Fila -> this.PerteneceAlAlcance(pvo_Fila.estado(), vlc_AlcanceFinal))
                .map(pvo_Fila -> this.ArmarResumen(pvo_Fila, vld_Hoy))
                .toList();

        if (!Constantes.ALCANCE_TEMPORAL_ACTIVOS.equals(vlc_Alcance)) {
            return vlo_LsResumen;
        }
        return vlo_LsResumen.stream()
                .sorted(Comparator.comparing(
                        (EntResumenTramiteTemporal pvo_Resumen) -> pvo_Resumen.vencimiento().diasRestantes(),
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene las opciones de las pantallas de importación temporal: sugerencias de
     *   unidades y aduanas, agentes disponibles, monedas y estados del catálogo.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026), que además ofrece los países
     *     de destino.
     *
     * @param pvo_Tipo tipo de trámite temporal de las pantallas
     * @return opciones de las pantallas
     */
    @Transactional(readOnly = true)
    public EntOpcionesTramiteTemporal ObtenerOpciones(TipoTramiteTemporal pvo_Tipo) {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        return new EntOpcionesTramiteTemporal(
                this.vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ObtenerUnidadesSolicitantes(),
                this.vgo_DalTramite.SIMT_TRAMITE_ObtenerAduanasControl(),
                this.vgo_DalTramiteTemporal.SIMF_USUARIO_ObtenerAgentesResponsables(),
                this.vgo_DalMoneda.SIMM_MONEDA_ObtenerActivas(),
                this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(
                        vlo_EnTipoTramite.getIdTipoTramite(), Constantes.ESTADO_CATALOGO_ACTIVO),
                this.EsExportacion(pvo_Tipo) ? this.vgo_DalPais.SIMM_PAIS_ObtenerActivos() : List.of());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene los agentes de aduanas que se pueden asignar a cualquier trámite temporal.
     *
     * @return agentes disponibles
     */
    @Transactional(readOnly = true)
    public List<EntAgenteTemporal> ObtenerAgentes() {
        return this.vgo_DalTramiteTemporal.SIMF_USUARIO_ObtenerAgentesResponsables();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene el expediente completo de una importación temporal.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvc_ReferenciaInterna referencia del expediente
     * @return expediente con su cuenta regresiva, pendientes, documentación e historial
     */
    @Transactional(readOnly = true)
    public EntExpedienteTramiteTemporal ObtenerExpediente(TipoTramiteTemporal pvo_Tipo, String pvc_ReferenciaInterna) {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvo_Tipo, pvc_ReferenciaInterna, vlo_EnTipoTramite);
        return this.ArmarExpediente(pvo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Cambia el estado del expediente sin editar el resto de sus datos. Pasar a
     *   Activo en el país exige la fecha de vencimiento, y finalizar exige el DUA de reexportación
     *   y la cancelación del régimen. Al cerrarse, las alertas pendientes quedan atendidas.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026): pasar a Fuera del país exige
     *     la fecha de vencimiento y finalizar exige el DUA de reingreso y la cancelación del régimen.
     *     Salida definitiva y Anulado cierran el expediente solo con la justificación.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Cambio nuevo estado y su justificación
     * @param pvn_IdUsuario usuario de la sesión
     * @return expediente actualizado
     */
    @Transactional
    public EntExpedienteTramiteTemporal ActualizarEstado(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_ReferenciaInterna,
            EntCambioEstado pvo_Cambio,
            Integer pvn_IdUsuario) {

        if (pvo_Cambio == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvo_Tipo, pvc_ReferenciaInterna, vlo_EnTipoTramite);
        EntTramiteTemporal vlo_EnTemporal = this.ObtenerTemporal(pvo_Tipo, vlo_EnTramite);

        String vlc_Justificacion = this.Normalizar(pvo_Cambio.getJustificacion());
        if (vlc_Justificacion == null
                || vlc_Justificacion.length() > Constantes.LONGITUD_MAXIMA_JUSTIFICACION_CAMBIO) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_ESTADO_INVALIDA);
        }
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        EntEstadoTramite vlo_EnEstadoActual = this.ObtenerEstadoActual(vlo_EnTramite);
        this.ValidarExpedienteAbierto(vlo_EnEstadoActual);

        String vlc_NuevoEstado = this.Normalizar(pvo_Cambio.getNuevoEstado());
        EntEstadoTramite vlo_EnEstadoNuevo = vlc_NuevoEstado == null
                ? null
                : this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                        vlo_EnTipoTramite.getIdTipoTramite(), vlc_NuevoEstado, Constantes.ESTADO_CATALOGO_ACTIVO);
        if (vlo_EnEstadoNuevo == null) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeEstadoInvalido());
        }
        if (vlo_EnEstadoNuevo.getIdEstadoTramite().equals(vlo_EnEstadoActual.getIdEstadoTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_SIN_CAMBIO);
        }
        if (pvo_Tipo.getEstadoEquipoEnDestino().equals(vlo_EnEstadoNuevo.getNombre())
                && vlo_EnTemporal.getFechaVencimiento() == null) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeVencimientoRequerido());
        }
        if (Constantes.ESTADO_TEMPORAL_FINALIZADO.equals(vlo_EnEstadoNuevo.getNombre())
                && !this.CumpleRequisitosFinalizacion(
                        pvo_Tipo, vlo_EnTemporal, this.ObtenerCodigosDocumentos(vlo_EnTramite))) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeRequisitosFinalizacion());
        }

        this.RegistrarMovimientoEstado(vlo_EnTramite, vlo_EnEstadoActual.getIdEstadoTramite(),
                vlo_EnEstadoNuevo, vlo_EnUsuario.getIdUsuario(), vlc_Justificacion);
        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoNuevo.getIdEstadoTramite());
        if (Boolean.TRUE.equals(vlo_EnEstadoNuevo.getEsFinal())) {
            vlo_EnTramite.setFechaCierre(this.vgo_BllAlertaTemporal.Hoy());
            this.vgo_BllAlertaTemporal.AtenderPendientes(vlo_EnTramite.getIdTramite());
        }
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_CAMBIO_ESTADO,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                "Estado: " + vlo_EnEstadoActual.getNombre() + ".",
                "Estado: " + vlo_EnEstadoNuevo.getNombre() + ". Justificación: " + vlc_Justificacion);

        return this.ArmarExpediente(pvo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Actualiza los datos que se completan conforme avanza el trámite ante la aduana.
     *   Un expediente cerrado solo admite corregir el DUA de cierre. Si cambia el vencimiento, las
     *   alertas del vencimiento anterior quedan atendidas. El cambio queda en la bitácora.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026), que además actualiza las
     *     notificaciones y los datos de la reimportación.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Datos datos completos del expediente
     * @param pvn_IdUsuario usuario de la sesión
     * @return expediente actualizado
     */
    @Transactional
    public EntExpedienteTramiteTemporal ModificarDatos(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_ReferenciaInterna,
            EntDatosTramiteTemporal pvo_Datos,
            Integer pvn_IdUsuario) {

        if (pvo_Datos == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvo_Tipo, pvc_ReferenciaInterna, vlo_EnTipoTramite);
        EntTramiteTemporal vlo_EnTemporal = this.ObtenerTemporal(pvo_Tipo, vlo_EnTramite);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        EntEstadoTramite vlo_EnEstado = this.ObtenerEstadoActual(vlo_EnTramite);

        EntTramiteTemporal vlo_EnNuevo = new EntTramiteTemporal();
        vlo_EnNuevo.setNumeroFactura(this.Normalizar(pvo_Datos.numeroFactura()));
        vlo_EnNuevo.setOficioAduana(this.Normalizar(pvo_Datos.oficioAduana()));
        vlo_EnNuevo.setNumeroGestion(this.Normalizar(pvo_Datos.numeroGestion()));
        vlo_EnNuevo.setResolucionAutorizacion(this.Normalizar(pvo_Datos.resolucionAutorizacion()));
        vlo_EnNuevo.setNumeroDuaInicio(this.Normalizar(pvo_Datos.numeroDuaInicio()));
        vlo_EnNuevo.setFechaDuaInicio(pvo_Datos.fechaDuaInicio());
        vlo_EnNuevo.setFechaVencimiento(pvo_Datos.fechaVencimiento());
        vlo_EnNuevo.setNumeroDuaCierre(this.Normalizar(pvo_Datos.numeroDuaCierre()));
        vlo_EnNuevo.setFechaDuaCierre(pvo_Datos.fechaDuaCierre());
        vlo_EnNuevo.setCancelacionRegimen(this.Normalizar(pvo_Datos.cancelacionRegimen()));
        vlo_EnNuevo.setFechaCancelacion(pvo_Datos.fechaCancelacion());
        if (this.EsExportacion(pvo_Tipo)) {
            vlo_EnNuevo.setOficioNotificacionUsuario(this.Normalizar(pvo_Datos.oficioNotificacionUsuario()));
            vlo_EnNuevo.setNotificacionDuaAduana(this.Normalizar(pvo_Datos.notificacionDuaAduana()));
            vlo_EnNuevo.setSolicitudIngreso(this.Normalizar(pvo_Datos.solicitudIngreso()));
            vlo_EnNuevo.setFechaReingreso(pvo_Datos.fechaReingreso());
            vlo_EnNuevo.setDocumentoEmbarque(this.Normalizar(pvo_Datos.documentoEmbarque()));
            vlo_EnNuevo.setFechaDesalmacenaje(pvo_Datos.fechaDesalmacenaje());
            vlo_EnNuevo.setPagaImpuestosReimportacion(pvo_Datos.pagaImpuestosReimportacion());
            // Sin impuestos (garantía) no se guarda monto, para que el dato no quede contradictorio.
            vlo_EnNuevo.setMontoImpuestosReimportacion(
                    Boolean.TRUE.equals(pvo_Datos.pagaImpuestosReimportacion())
                            ? pvo_Datos.montoImpuestosReimportacion()
                            : null);
        }
        this.ValidarDatosAduana(vlo_EnNuevo);
        String vlc_Aduana = this.Normalizar(pvo_Datos.aduanaControl());
        String vlc_Observaciones = this.Normalizar(pvo_Datos.observaciones());
        this.ValidarLongitud(vlc_Aduana, Constantes.LONGITUD_MAXIMA_ADUANA_CONTROL,
                Constantes.MENSAJE_ADUANA_CONTROL_INVALIDA);
        this.ValidarLongitud(vlc_Observaciones, Constantes.LONGITUD_MAXIMA_OBSERVACIONES,
                Constantes.MENSAJE_OBSERVACIONES_INVALIDAS);

        String vlc_DuaInicio = pvo_Tipo.getNombreDuaInicio();
        String vlc_DuaCierre = pvo_Tipo.getNombreDuaCierre();
        List<String> vlo_LsAnteriores = new ArrayList<>();
        List<String> vlo_LsNuevos = new ArrayList<>();
        this.CompararDato("Aduana de control", vlo_EnTramite.getAduanaControl(), vlc_Aduana,
                vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Factura del exportador", vlo_EnTemporal.getNumeroFactura(),
                vlo_EnNuevo.getNumeroFactura(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Oficio enviado a la aduana", vlo_EnTemporal.getOficioAduana(),
                vlo_EnNuevo.getOficioAduana(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Número de gestión", vlo_EnTemporal.getNumeroGestion(),
                vlo_EnNuevo.getNumeroGestion(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Resolución de autorización", vlo_EnTemporal.getResolucionAutorizacion(),
                vlo_EnNuevo.getResolucionAutorizacion(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Oficio de notificación al usuario", vlo_EnTemporal.getOficioNotificacionUsuario(),
                vlo_EnNuevo.getOficioNotificacionUsuario(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato(vlc_DuaInicio, vlo_EnTemporal.getNumeroDuaInicio(),
                vlo_EnNuevo.getNumeroDuaInicio(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Fecha del " + vlc_DuaInicio, vlo_EnTemporal.getFechaDuaInicio(),
                vlo_EnNuevo.getFechaDuaInicio(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Notificación del DUA de salida a la aduana", vlo_EnTemporal.getNotificacionDuaAduana(),
                vlo_EnNuevo.getNotificacionDuaAduana(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Fecha de vencimiento", vlo_EnTemporal.getFechaVencimiento(),
                vlo_EnNuevo.getFechaVencimiento(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Solicitud de reingreso", vlo_EnTemporal.getSolicitudIngreso(),
                vlo_EnNuevo.getSolicitudIngreso(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Fecha de reingreso al país", vlo_EnTemporal.getFechaReingreso(),
                vlo_EnNuevo.getFechaReingreso(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Documento de embarque", vlo_EnTemporal.getDocumentoEmbarque(),
                vlo_EnNuevo.getDocumentoEmbarque(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Fecha de desalmacenaje", vlo_EnTemporal.getFechaDesalmacenaje(),
                vlo_EnNuevo.getFechaDesalmacenaje(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Reimportación paga impuestos",
                this.DescribirSiNo(vlo_EnTemporal.getPagaImpuestosReimportacion()),
                this.DescribirSiNo(vlo_EnNuevo.getPagaImpuestosReimportacion()), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Impuestos de la reimportación (₡)",
                this.SinCerosDecimales(vlo_EnTemporal.getMontoImpuestosReimportacion()),
                this.SinCerosDecimales(vlo_EnNuevo.getMontoImpuestosReimportacion()), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Cancelación del régimen", vlo_EnTemporal.getCancelacionRegimen(),
                vlo_EnNuevo.getCancelacionRegimen(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Fecha de cancelación", vlo_EnTemporal.getFechaCancelacion(),
                vlo_EnNuevo.getFechaCancelacion(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Observaciones", vlo_EnTramite.getObservaciones(), vlc_Observaciones,
                vlo_LsAnteriores, vlo_LsNuevos);
        boolean vlb_CambiaFueraDelCierre = !vlo_LsNuevos.isEmpty();
        this.CompararDato(vlc_DuaCierre, vlo_EnTemporal.getNumeroDuaCierre(),
                vlo_EnNuevo.getNumeroDuaCierre(), vlo_LsAnteriores, vlo_LsNuevos);
        this.CompararDato("Fecha del " + vlc_DuaCierre, vlo_EnTemporal.getFechaDuaCierre(),
                vlo_EnNuevo.getFechaDuaCierre(), vlo_LsAnteriores, vlo_LsNuevos);

        if (vlo_LsNuevos.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_TEMPORAL_SIN_CAMBIO);
        }
        if (Boolean.TRUE.equals(vlo_EnEstado.getEsFinal()) && vlb_CambiaFueraDelCierre) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SOLO_DUA_CIERRE_EDITABLE);
        }
        if (vlo_EnNuevo.getFechaVencimiento() == null
                && this.EstadosEquipoEnDestino(pvo_Tipo).contains(vlo_EnEstado.getNombre())) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeVencimientoNoSeBorra());
        }

        boolean vlb_CambiaVencimiento = !Objects.equals(
                vlo_EnTemporal.getFechaVencimiento(), vlo_EnNuevo.getFechaVencimiento());
        vlo_EnTemporal.setNumeroFactura(vlo_EnNuevo.getNumeroFactura());
        vlo_EnTemporal.setOficioAduana(vlo_EnNuevo.getOficioAduana());
        vlo_EnTemporal.setNumeroGestion(vlo_EnNuevo.getNumeroGestion());
        vlo_EnTemporal.setResolucionAutorizacion(vlo_EnNuevo.getResolucionAutorizacion());
        vlo_EnTemporal.setOficioNotificacionUsuario(vlo_EnNuevo.getOficioNotificacionUsuario());
        vlo_EnTemporal.setNumeroDuaInicio(vlo_EnNuevo.getNumeroDuaInicio());
        vlo_EnTemporal.setFechaDuaInicio(vlo_EnNuevo.getFechaDuaInicio());
        vlo_EnTemporal.setNotificacionDuaAduana(vlo_EnNuevo.getNotificacionDuaAduana());
        vlo_EnTemporal.setFechaVencimiento(vlo_EnNuevo.getFechaVencimiento());
        vlo_EnTemporal.setSolicitudIngreso(vlo_EnNuevo.getSolicitudIngreso());
        vlo_EnTemporal.setFechaReingreso(vlo_EnNuevo.getFechaReingreso());
        vlo_EnTemporal.setDocumentoEmbarque(vlo_EnNuevo.getDocumentoEmbarque());
        vlo_EnTemporal.setNumeroDuaCierre(vlo_EnNuevo.getNumeroDuaCierre());
        vlo_EnTemporal.setFechaDuaCierre(vlo_EnNuevo.getFechaDuaCierre());
        vlo_EnTemporal.setFechaDesalmacenaje(vlo_EnNuevo.getFechaDesalmacenaje());
        vlo_EnTemporal.setPagaImpuestosReimportacion(vlo_EnNuevo.getPagaImpuestosReimportacion());
        vlo_EnTemporal.setMontoImpuestosReimportacion(vlo_EnNuevo.getMontoImpuestosReimportacion());
        vlo_EnTemporal.setCancelacionRegimen(vlo_EnNuevo.getCancelacionRegimen());
        vlo_EnTemporal.setFechaCancelacion(vlo_EnNuevo.getFechaCancelacion());
        this.AsignarPlazoEstandar(pvo_Tipo, vlo_EnTemporal);
        this.vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ModificarRegistro(vlo_EnTemporal);

        vlo_EnTramite.setAduanaControl(vlc_Aduana);
        vlo_EnTramite.setObservaciones(vlc_Observaciones);
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        if (vlb_CambiaVencimiento) {
            this.vgo_BllAlertaTemporal.AtenderPendientes(vlo_EnTramite.getIdTramite());
        }

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DATOS_MODIFICADOS,
                Constantes.ENTIDAD_BITACORA_TRAMITE_TEMPORAL,
                vlo_EnTramite.getIdTramite(),
                String.join(" ", vlo_LsAnteriores),
                String.join(" ", vlo_LsNuevos));

        return this.ArmarExpediente(pvo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Registra la prórroga del plazo. Los datos se pueden registrar conforme llegan:
     *   un dato vacío conserva el valor ya registrado. Con la casilla de mismo número de gestión
     *   se repite el número de la autorización inicial. Si llega el nuevo vencimiento, reemplaza
     *   al vigente y las alertas del vencimiento anterior quedan atendidas.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: se registra también el motivo de la prórroga.
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026). Un nuevo vencimiento deja
     *   de ser el plazo estándar de 6 meses (HU-026).
     *   Responsable: Equipo SIM. Fecha: 05/10/2026.
     *   Descripción: la prórroga requiere su documentación, autorización y registro de historial
     *     (HU-028), y siempre extiende seis meses el vencimiento confirmado.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Prorroga datos de la prórroga
     * @param pvn_IdUsuario usuario de la sesión
     * @return expediente actualizado
     */
    @Transactional
    public EntExpedienteTramiteTemporal RegistrarProrroga(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_ReferenciaInterna,
            EntProrrogaTemporal pvo_Prorroga,
            MultipartFile pvo_OficioSolicitud,
            MultipartFile pvo_Resolucion,
            Integer pvn_IdUsuario) {

        if (pvo_Prorroga == null
                || pvo_OficioSolicitud == null || pvo_OficioSolicitud.isEmpty()
                || pvo_Resolucion == null || pvo_Resolucion.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_DOCUMENTOS_REQUERIDOS);
        }
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvo_Tipo, pvc_ReferenciaInterna, vlo_EnTipoTramite);
        vlo_EnTramite = this.vgo_DalTramite
                .SIMT_TRAMITE_ObtenerRegistroParaModificar(vlo_EnTramite.getIdTramite());
        if (vlo_EnTramite == null) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeExpedienteNoEncontrado());
        }
        EntTramiteTemporal vlo_EnTemporal = this.ObtenerTemporal(pvo_Tipo, vlo_EnTramite);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        this.ValidarExpedienteAbierto(this.ObtenerEstadoActual(vlo_EnTramite));

        String vlc_Motivo = this.Normalizar(pvo_Prorroga.motivoProrroga());
        String vlc_Oficio = this.Normalizar(pvo_Prorroga.oficioProrroga());
        String vlc_Gestion = this.Normalizar(pvo_Prorroga.numeroGestionProrroga());
        String vlc_Autorizacion = this.Normalizar(pvo_Prorroga.autorizacionProrroga());
        String vlc_Contrato = this.Normalizar(pvo_Prorroga.numeroContrato());
        if (Boolean.TRUE.equals(pvo_Prorroga.mismoNumeroGestion())) {
            vlc_Gestion = this.Normalizar(vlo_EnTemporal.getNumeroGestion());
            if (vlc_Gestion == null) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_SIN_GESTION_INICIAL);
            }
        }
        this.ValidarLongitud(vlc_Motivo, Constantes.LONGITUD_MAXIMA_MOTIVO_PRORROGA,
                Constantes.MENSAJE_MOTIVO_PRORROGA_INVALIDO);
        this.ValidarLongitud(vlc_Oficio, Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA,
                Constantes.MENSAJE_DATO_ADUANA_INVALIDO);
        this.ValidarLongitud(vlc_Gestion, Constantes.LONGITUD_MAXIMA_NUMERO_ADUANA,
                Constantes.MENSAJE_DATO_ADUANA_INVALIDO);
        this.ValidarLongitud(vlc_Autorizacion, Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA,
                Constantes.MENSAJE_DATO_ADUANA_INVALIDO);

        if (vlc_Motivo == null || vlc_Oficio == null || vlc_Gestion == null || vlc_Autorizacion == null
                || pvo_Prorroga.fechaVencimientoActual() == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_REQUISITOS_INCOMPLETOS);
        }
        if (vlc_Contrato != null) {
            this.ValidarLongitud(vlc_Contrato, 45,
                    Constantes.MENSAJE_DATO_ADUANA_INVALIDO);
        }
        if (Boolean.TRUE.equals(pvo_Prorroga.permanecePorAnos()) && vlc_Contrato == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_CONTRATO_REQUERIDO);
        }
        LocalDate vld_VencimientoAnterior = vlo_EnTemporal.getFechaVencimiento();
        if (vld_VencimientoAnterior == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_SIN_VENCIMIENTO);
        }
        if (!vld_VencimientoAnterior.equals(pvo_Prorroga.fechaVencimientoActual())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_VENCIMIENTO_CAMBIADO);
        }
        if (this.EsExportacion(pvo_Tipo)
                && (this.vgo_DalTramiteTemporal.SIMT_HISTORIAL_PRORROGA_ContarPorTramite(
                            vlo_EnTramite.getIdTramite()) > 0
                    || vlo_EnTemporal.getMotivoProrroga() != null
                    || vlo_EnTemporal.getOficioProrroga() != null
                    || vlo_EnTemporal.getNumeroGestionProrroga() != null
                    || vlo_EnTemporal.getAutorizacionProrroga() != null)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_MAXIMA_EXPORTACION);
        }

        LocalDate vld_NuevoVencimiento = vld_VencimientoAnterior.plusMonths(6);
        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.vgo_BllDocumento.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);
        EntDocumento vlo_EnOficio = this.vgo_BllDocumento.GuardarDocumento(
                vlo_EnTramite, vlo_EnUsuario.getIdUsuario(), pvo_OficioSolicitud,
                Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_PRORROGA, vlo_LsRutasEscritas);
        EntDocumento vlo_EnResolucion = this.vgo_BllDocumento.GuardarDocumento(
                vlo_EnTramite, vlo_EnUsuario.getIdUsuario(), pvo_Resolucion,
                Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_PRORROGA, vlo_LsRutasEscritas);

        EntHistorialProrrogaTemporal vlo_EnHistorial = new EntHistorialProrrogaTemporal();
        vlo_EnHistorial.setIdTramite(vlo_EnTramite.getIdTramite());
        vlo_EnHistorial.setFechaVencimientoAnterior(vld_VencimientoAnterior);
        vlo_EnHistorial.setFechaVencimientoNueva(vld_NuevoVencimiento);
        vlo_EnHistorial.setMotivo(vlc_Motivo);
        vlo_EnHistorial.setOficioSolicitud(vlc_Oficio);
        vlo_EnHistorial.setNumeroGestion(vlc_Gestion);
        vlo_EnHistorial.setNumeroAutorizacion(vlc_Autorizacion);
        vlo_EnHistorial.setNumeroContrato(vlc_Contrato);
        vlo_EnHistorial.setIdDocumentoOficio(vlo_EnOficio.getIdDocumento());
        vlo_EnHistorial.setIdDocumentoResolucion(vlo_EnResolucion.getIdDocumento());
        vlo_EnHistorial.setIdUsuarioRegistro(vlo_EnUsuario.getIdUsuario());
        this.vgo_DalTramiteTemporal.SIMT_HISTORIAL_PRORROGA_Insertar(vlo_EnHistorial);

        vlo_EnTemporal.setMotivoProrroga(vlc_Motivo);
        vlo_EnTemporal.setOficioProrroga(vlc_Oficio);
        vlo_EnTemporal.setNumeroGestionProrroga(vlc_Gestion);
        vlo_EnTemporal.setAutorizacionProrroga(vlc_Autorizacion);
        vlo_EnTemporal.setFechaVencimiento(vld_NuevoVencimiento);
        this.AsignarPlazoEstandar(pvo_Tipo, vlo_EnTemporal);
        if (vlc_Contrato != null) {
            vlo_EnTemporal.setNumeroContrato(vlc_Contrato);
        }
        this.vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ModificarRegistro(vlo_EnTemporal);
        this.vgo_BllAlertaTemporal.AtenderPendientes(vlo_EnTramite.getIdTramite());

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_PRORROGA,
                Constantes.ENTIDAD_BITACORA_TRAMITE_TEMPORAL,
                vlo_EnTramite.getIdTramite(),
                "Vencimiento anterior: " + vld_VencimientoAnterior + ".",
                "Motivo: " + vlc_Motivo + ". Gestión: " + vlc_Gestion + ". Autorización: "
                        + vlc_Autorizacion + ". Vencimiento actualizado a " + vld_NuevoVencimiento
                        + ". Oficio y resolución asociados.");

        return this.ArmarExpediente(pvo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Extiende el plazo vigente en la cantidad y la unidad indicadas. Un plazo en años
     *   exige el número de contrato. Las alertas del vencimiento anterior quedan atendidas.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: busca el expediente por su referencia interna y lo puede extender cualquier
     *     usuario, no solo un agente asignado. Exige un expediente abierto con vencimiento y deja la
     *     extensión en la bitácora.
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende cualquier trámite temporal: el tipo se toma del expediente (HU-026).
     *
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_Extension cantidad, unidad y contrato de la extensión
     * @param pvn_IdUsuario usuario de la sesión
     * @return expediente actualizado
     */
    @Transactional
    public EntExpedienteTramiteTemporal ExtenderPlazo(
            String pvc_ReferenciaInterna,
            EntExtensionPlazoTemporal pvo_Extension,
            Integer pvn_IdUsuario) {

        if (pvo_Extension == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PLAZO_TEMPORAL_INVALIDO);
        }
        String vlc_Referencia = this.NormalizarCodigo(pvc_ReferenciaInterna);
        EntTramite vlo_EnTramite = vlc_Referencia == null
                ? null
                : this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(vlc_Referencia);
        TipoTramiteTemporal vlo_Tipo = this.ObtenerTipoDelTramite(vlo_EnTramite);
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(vlo_Tipo);
        EntTramiteTemporal vlo_EnTemporal = this.ObtenerTemporal(vlo_Tipo, vlo_EnTramite);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        this.ValidarExpedienteAbierto(this.ObtenerEstadoActual(vlo_EnTramite));
        if (vlo_EnTemporal.getFechaVencimiento() == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PRORROGA_SIN_VENCIMIENTO);
        }

        String vlc_Unidad = this.NormalizarCodigo(pvo_Extension.unidadPlazo());
        LocalDate vld_VencimientoAnterior = vlo_EnTemporal.getFechaVencimiento();
        LocalDate vld_NuevoVencimiento = this.CalcularVencimiento(
                vld_VencimientoAnterior, vlc_Unidad, pvo_Extension.cantidadPlazo(), pvo_Extension.numeroContrato());
        if (!vld_NuevoVencimiento.isAfter(this.vgo_BllAlertaTemporal.Hoy())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PLAZO_TEMPORAL_INVALIDO);
        }

        this.vgo_BllAlertaTemporal.AtenderPendientes(vlo_EnTramite.getIdTramite());
        vlo_EnTemporal.setFechaVencimiento(vld_NuevoVencimiento);
        vlo_EnTemporal.setUnidadExtension(vlc_Unidad);
        vlo_EnTemporal.setCantidadExtension(pvo_Extension.cantidadPlazo());
        if (pvo_Extension.numeroContrato() != null && !pvo_Extension.numeroContrato().isBlank()) {
            vlo_EnTemporal.setNumeroContrato(pvo_Extension.numeroContrato().trim());
        }
        this.vgo_DalTramiteTemporal.SIMT_TRAMITE_TEMPORAL_ModificarRegistro(vlo_EnTemporal);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_PRORROGA,
                Constantes.ENTIDAD_BITACORA_TRAMITE_TEMPORAL,
                vlo_EnTramite.getIdTramite(),
                "Fecha de vencimiento: " + vld_VencimientoAnterior + ".",
                "Fecha de vencimiento: " + vld_NuevoVencimiento + ". Extensión: "
                        + pvo_Extension.cantidadPlazo() + " " + vlc_Unidad + ".");

        return this.ArmarExpediente(vlo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Agrega documentación al expediente. Si el archivo repite tipo y nombre, se
     *   guarda como la versión siguiente. Un expediente cerrado solo admite el DUA de cierre.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026), con sus propios documentos.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_MpDocumentos archivos agrupados por código de tipo de documento
     * @param pvn_IdUsuario usuario de la sesión
     * @return expediente actualizado
     */
    @Transactional
    public EntExpedienteTramiteTemporal InsertarDocumentos(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_ReferenciaInterna,
            Map<String, List<MultipartFile>> pvo_MpDocumentos,
            Integer pvn_IdUsuario) {

        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvo_Tipo, pvc_ReferenciaInterna, vlo_EnTipoTramite);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvn_IdUsuario);
        boolean vlb_Cerrado = Boolean.TRUE.equals(this.ObtenerEstadoActual(vlo_EnTramite).getEsFinal());
        this.ValidarTiposDocumento(pvo_Tipo, pvo_MpDocumentos, vlb_Cerrado);

        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.vgo_BllDocumento.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);
        List<EntDocumento> vlo_LsDocumentos = this.GuardarDocumentos(
                vlo_EnTramite, vlo_EnUsuario.getIdUsuario(), pvo_MpDocumentos, vlo_LsRutasEscritas);
        if (vlo_LsDocumentos.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SIN_DOCUMENTOS_ADJUNTOS);
        }

        List<String> vlo_LsNombres = vlo_LsDocumentos.stream()
                .map(pvo_Documento -> pvo_Documento.getNombreArchivo() + " (versión " + pvo_Documento.getVersion() + ")")
                .toList();
        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_DOCUMENTO_AGREGADO,
                Constantes.ENTIDAD_BITACORA_DOCUMENTO,
                vlo_LsDocumentos.size() == 1 ? vlo_LsDocumentos.get(0).getIdDocumento() : null,
                null,
                "Documentos agregados: " + String.join(", ", vlo_LsNombres) + ".");

        return this.ArmarExpediente(pvo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Retira del expediente abierto un documento cargado por error, con la regla
     *   común de BllDocumento: el archivo se conserva inactivo y la eliminación queda en la bitácora.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvn_IdDocumento documento que se retira
     * @param pvn_IdUsuario usuario de la sesión
     * @return expediente actualizado
     */
    @Transactional
    public EntExpedienteTramiteTemporal EliminarDocumento(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_ReferenciaInterna,
            Integer pvn_IdDocumento,
            Integer pvn_IdUsuario) {
        EntTipoTramite vlo_EnTipoTramite = this.ObtenerTipoTramite(pvo_Tipo);
        EntTramite vlo_EnTramite = this.ObtenerTramite(pvo_Tipo, pvc_ReferenciaInterna, vlo_EnTipoTramite);
        this.ValidarExpedienteAbierto(this.ObtenerEstadoActual(vlo_EnTramite));
        this.vgo_BllDocumento.EliminarDocumento(vlo_EnTramite.getIdTramite(), pvn_IdDocumento, pvn_IdUsuario);
        return this.ArmarExpediente(pvo_Tipo, vlo_EnTramite, vlo_EnTipoTramite);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene un documento de una importación temporal para su descarga.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026): el documento debe pertenecer
     *     a un expediente del tipo indicado.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvn_IdDocumento identificador del documento
     * @return documento encontrado
     */
    @Transactional(readOnly = true)
    public EntDocumento ObtenerDocumento(TipoTramiteTemporal pvo_Tipo, Integer pvn_IdDocumento) {
        EntDocumento vlo_EnDocumento = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(pvn_IdDocumento);
        if (vlo_EnDocumento == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE);
        }
        EntTramite vlo_EnTramite = this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(vlo_EnDocumento.getIdTramite());
        if (vlo_EnTramite == null
                || !this.ObtenerTipoTramite(pvo_Tipo).getIdTipoTramite().equals(vlo_EnTramite.getIdTipoTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE);
        }
        return vlo_EnDocumento;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Lee el contenido de un archivo del expediente.
     *
     * @param pvo_EnDocumento documento ya validado
     * @return contenido del archivo
     */
    public byte[] ObtenerContenidoDocumento(EntDocumento pvo_EnDocumento) {
        return this.vgo_AlmacenamientoDocumentos.LeerArchivo(pvo_EnDocumento.getRutaArchivo());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Reúne el expediente común, los datos temporales, la cuenta regresiva, los
     *   pendientes, los agentes, la documentación y el historial.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvo_EnTramite expediente común
     * @param pvo_EnTipoTramite tipo de trámite
     * @return expediente completo
     */
    private EntExpedienteTramiteTemporal ArmarExpediente(
            TipoTramiteTemporal pvo_Tipo,
            EntTramite pvo_EnTramite,
            EntTipoTramite pvo_EnTipoTramite) {
        EntTramiteTemporal vlo_EnTemporal = this.ObtenerTemporal(pvo_Tipo, pvo_EnTramite);
        EntEstadoTramite vlo_EnEstado = this.ObtenerEstadoActual(pvo_EnTramite);
        EntMoneda vlo_EnMoneda = this.vgo_DalMoneda.SIMM_MONEDA_ObtenerRegistro(pvo_EnTramite.getIdMoneda());
        EntPais vlo_EnPais = this.vgo_DalPais.SIMM_PAIS_ObtenerRegistro(vlo_EnTemporal.getIdPaisDestino());
        EntUsuario vlo_EnResponsable = this.vgo_DalUsuario
                .SIMF_USUARIO_ObtenerRegistro(pvo_EnTramite.getIdUsuarioResponsable());
        boolean vlb_Pausado = Constantes.ESTADO_TEMPORAL_RETENIDO.equals(vlo_EnEstado.getNombre());
        List<String> vlo_LsPendientes = vlb_Pausado
                ? List.of()
                : this.CalcularPendientes(pvo_Tipo, vlo_EnTemporal, vlo_EnEstado,
                        this.ObtenerCodigosDocumentos(pvo_EnTramite));

        return new EntExpedienteTramiteTemporal(
                pvo_EnTramite.getReferenciaInterna(),
                pvo_EnTipoTramite.getNombre(),
                vlo_EnEstado.getNombre(),
                vlo_EnEstado.getEsFinal(),
                vlo_EnResponsable == null ? null : vlo_EnResponsable.getNombreCompleto(),
                pvo_EnTramite.getFechaCreacion(),
                pvo_EnTramite.getFechaCierre(),
                pvo_EnTramite.getDescripcion(),
                pvo_EnTramite.getValorEstimado(),
                vlo_EnMoneda == null ? null : vlo_EnMoneda.getCodigoIso(),
                pvo_EnTramite.getAduanaControl(),
                vlo_EnPais == null ? null : vlo_EnPais.getNombre(),
                pvo_EnTramite.getObservaciones(),
                vlo_EnTemporal,
                this.CalcularIndicador(vlo_EnTemporal, vlo_EnEstado, this.vgo_BllAlertaTemporal.Hoy()),
                vlb_Pausado,
                vlo_LsPendientes,
                this.vgo_DalTramiteTemporal.SIMT_TEMPORAL_AGENTE_ObtenerLista(pvo_EnTramite.getIdTramite()),
                this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerListaExpediente(pvo_EnTramite.getIdTramite()),
                this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerPorTramite(pvo_EnTramite.getIdTramite()),
                this.vgo_DalTramiteTemporal
                        .SIMT_HISTORIAL_PRORROGA_ObtenerPorTramite(pvo_EnTramite.getIdTramite()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Arma la fila de la lista con la cuenta regresiva del expediente.
     *
     * @param pvo_Fila expediente consultado
     * @param pvd_Hoy fecha de la consulta
     * @return fila de la lista
     */
    private EntResumenTramiteTemporal ArmarResumen(EntFilaTramiteTemporal pvo_Fila, LocalDate pvd_Hoy) {
        return new EntResumenTramiteTemporal(
                pvo_Fila.tramite().getReferenciaInterna(),
                pvo_Fila.estado().getNombre(),
                pvo_Fila.estado().getEsFinal(),
                pvo_Fila.temporal().getUnidadSolicitante(),
                pvo_Fila.tramite().getDescripcion(),
                pvo_Fila.tramite().getValorEstimado(),
                pvo_Fila.moneda(),
                pvo_Fila.temporal().getFechaRecepcion(),
                pvo_Fila.paisDestino(),
                this.CalcularIndicador(pvo_Fila.temporal(), pvo_Fila.estado(), pvd_Hoy));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Calcula la cuenta regresiva del vencimiento. Un expediente cerrado no muestra
     *   nivel porque ya no requiere seguimiento. Retenido en aduana no detiene la cuenta: el
     *   vencimiento legal sigue corriendo.
     *
     * @param pvo_EnTemporal datos temporales
     * @param pvo_EnEstado estado actual
     * @param pvd_Hoy fecha de la consulta
     * @return indicador de vencimiento
     */
    private EntIndicadorVencimiento CalcularIndicador(
            EntTramiteTemporal pvo_EnTemporal,
            EntEstadoTramite pvo_EnEstado,
            LocalDate pvd_Hoy) {
        LocalDate vld_Vencimiento = pvo_EnTemporal.getFechaVencimiento();
        boolean vlb_Prorrogado = pvo_EnTemporal.getOficioProrroga() != null
                || pvo_EnTemporal.getAutorizacionProrroga() != null
                || pvo_EnTemporal.getCantidadExtension() != null;
        if (Boolean.TRUE.equals(pvo_EnEstado.getEsFinal())) {
            return new EntIndicadorVencimiento(vld_Vencimiento, null, null, vlb_Prorrogado, "");
        }
        String vlc_Indicacion = this.vgo_BllAlertaTemporal.ObtenerIndicacion(
                pvo_EnTemporal.getUnidadPlazo(),
                pvo_EnTemporal.getCantidadPlazo(),
                pvo_EnTemporal.getCantidadExtension(),
                vld_Vencimiento,
                pvd_Hoy);
        if (vld_Vencimiento == null) {
            return new EntIndicadorVencimiento(null, null, Constantes.VENCIMIENTO_SIN_FECHA, vlb_Prorrogado, vlc_Indicacion);
        }
        long vln_Dias = ChronoUnit.DAYS.between(pvd_Hoy, vld_Vencimiento);
        String vlc_Nivel = vln_Dias < 0
                ? Constantes.VENCIMIENTO_VENCIDO
                : vln_Dias <= Constantes.DIAS_ANTICIPACION_ALERTA_TEMPORAL
                        ? Constantes.VENCIMIENTO_PROXIMO
                        : Constantes.VENCIMIENTO_EN_PLAZO;
        return new EntIndicadorVencimiento(vld_Vencimiento, vln_Dias, vlc_Nivel, vlb_Prorrogado, vlc_Indicacion);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Calcula los datos que el expediente aún debe registrar según su etapa. Son
     *   avisos operativos: no impiden cambiar de estado. Un dato cuenta como registrado si tiene su
     *   referencia o su documento.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: atiende también la exportación temporal (HU-026). Con el equipo fuera del país
     *     pide además el oficio de notificación al usuario y la notificación del DUA de salida a la
     *     aduana, y en la reimportación, la solicitud de reingreso y el documento de embarque.
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvo_EnTemporal datos temporales
     * @param pvo_EnEstado estado actual
     * @param pvo_LsCodigos códigos de los documentos vigentes del expediente
     * @return descripciones de los datos pendientes
     */
    private List<String> CalcularPendientes(
            TipoTramiteTemporal pvo_Tipo,
            EntTramiteTemporal pvo_EnTemporal,
            EntEstadoTramite pvo_EnEstado,
            Set<String> pvo_LsCodigos) {
        List<String> vlo_LsPendientes = new ArrayList<>();
        String vlc_Estado = pvo_EnEstado.getNombre();
        boolean vlb_Exportacion = this.EsExportacion(pvo_Tipo);
        Set<String> vlo_LsEstadosEnDestino = this.EstadosEquipoEnDestino(pvo_Tipo);
        if ((Constantes.ESTADO_TEMPORAL_EN_GESTION.equals(vlc_Estado) || vlo_LsEstadosEnDestino.contains(vlc_Estado))
                && !this.EstaRegistrado(pvo_EnTemporal.getOficioAduana(), pvo_LsCodigos,
                        Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_ADUANA)) {
            vlo_LsPendientes.add("Oficio enviado a la aduana");
        }
        if (vlo_LsEstadosEnDestino.contains(vlc_Estado)) {
            if (pvo_EnTemporal.getNumeroGestion() == null) {
                vlo_LsPendientes.add("Número de gestión asignado por la aduana");
            }
            if (!this.EstaRegistrado(pvo_EnTemporal.getResolucionAutorizacion(), pvo_LsCodigos,
                    Constantes.CODIGO_TIPO_DOCUMENTO_RESOLUCION_AUTORIZACION)) {
                vlo_LsPendientes.add("Resolución de autorización");
            }
            if (vlb_Exportacion && !this.EstaRegistrado(pvo_EnTemporal.getOficioNotificacionUsuario(),
                    pvo_LsCodigos, Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_NOTIFICACION_USUARIO)) {
                vlo_LsPendientes.add("Oficio de notificación al usuario");
            }
            if (!this.EstaRegistrado(pvo_EnTemporal.getNumeroDuaInicio(), pvo_LsCodigos,
                    pvo_Tipo.getCodigoDuaInicio())) {
                vlo_LsPendientes.add(pvo_Tipo.getNombreDuaInicio());
            }
            if (vlb_Exportacion && pvo_EnTemporal.getNotificacionDuaAduana() == null) {
                vlo_LsPendientes.add("Notificación del DUA de salida a la aduana");
            }
        }
        if (pvo_Tipo.getEstadoRetorno().equals(vlc_Estado)) {
            if (vlb_Exportacion && !this.EstaRegistrado(pvo_EnTemporal.getSolicitudIngreso(), pvo_LsCodigos,
                    Constantes.CODIGO_TIPO_DOCUMENTO_SOLICITUD_REINGRESO)) {
                vlo_LsPendientes.add("Solicitud de reingreso del usuario");
            }
            if (vlb_Exportacion && !this.EstaRegistrado(pvo_EnTemporal.getDocumentoEmbarque(), pvo_LsCodigos,
                    Constantes.CODIGO_TIPO_DOCUMENTO_EMBARQUE)) {
                vlo_LsPendientes.add("Documento de embarque");
            }
            if (!this.EstaRegistrado(pvo_EnTemporal.getNumeroDuaCierre(), pvo_LsCodigos,
                    pvo_Tipo.getCodigoDuaCierre())) {
                vlo_LsPendientes.add(pvo_Tipo.getNombreDuaCierre());
            }
        }
        return vlo_LsPendientes;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba que el expediente tenga lo que exige su cierre: el DUA de
     *   reexportación y la cancelación del régimen, con su referencia o con su documento.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: el DUA de cierre depende del tipo: reexportación o reingreso (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvo_EnTemporal datos temporales
     * @param pvo_LsCodigos códigos de los documentos vigentes del expediente
     * @return true si el expediente se puede finalizar
     */
    private boolean CumpleRequisitosFinalizacion(
            TipoTramiteTemporal pvo_Tipo,
            EntTramiteTemporal pvo_EnTemporal,
            Set<String> pvo_LsCodigos) {
        boolean vlb_TieneCancelacion = pvo_EnTemporal.getCancelacionRegimen() != null
                || pvo_EnTemporal.getFechaCancelacion() != null
                || pvo_LsCodigos.contains(Constantes.CODIGO_TIPO_DOCUMENTO_OFICIO_CANCELACION);
        return vlb_TieneCancelacion
                && this.EstaRegistrado(pvo_EnTemporal.getNumeroDuaCierre(), pvo_LsCodigos,
                        pvo_Tipo.getCodigoDuaCierre());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Indica si un dato del expediente está registrado por su referencia o por su
     *   documento.
     *
     * @param pvc_Referencia referencia registrada
     * @param pvo_LsCodigos códigos de los documentos vigentes
     * @param pvc_CodigoDocumento código del documento que respalda el dato
     * @return true si el dato está registrado
     */
    private boolean EstaRegistrado(String pvc_Referencia, Set<String> pvo_LsCodigos, String pvc_CodigoDocumento) {
        return pvc_Referencia != null || pvo_LsCodigos.contains(pvc_CodigoDocumento);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Estados en los que el equipo ya está en su destino temporal o en camino de
     *   regreso: el expediente necesita su fecha de vencimiento.
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @return nombres de los estados
     */
    private Set<String> EstadosEquipoEnDestino(TipoTramiteTemporal pvo_Tipo) {
        return Set.of(
                pvo_Tipo.getEstadoEquipoEnDestino(),
                Constantes.ESTADO_TEMPORAL_VENCIDO,
                pvo_Tipo.getEstadoRetorno());
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene los códigos de tipo de los documentos vigentes del expediente.
     *
     * @param pvo_EnTramite expediente común
     * @return códigos de tipo de documento
     */
    private Set<String> ObtenerCodigosDocumentos(EntTramite pvo_EnTramite) {
        return new HashSet<>(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerCodigosTipoVigentes(
                pvo_EnTramite.getIdTramite(), Constantes.ESTADO_DOCUMENTO_ACTIVO));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Guarda los archivos recibidos con el tipo de documento con el que vienen
     *   agrupados. Los campos de archivo enviados vacíos se ignoran.
     *
     * @param pvo_EnTramite expediente ya persistido
     * @param pvn_IdUsuario usuario que adjunta los documentos
     * @param pvo_MpDocumentos archivos agrupados por código de tipo de documento
     * @param pvo_LsRutasEscritas rutas escritas durante la transacción
     * @return documentos registrados
     */
    private List<EntDocumento> GuardarDocumentos(
            EntTramite pvo_EnTramite,
            Integer pvn_IdUsuario,
            Map<String, List<MultipartFile>> pvo_MpDocumentos,
            List<String> pvo_LsRutasEscritas) {
        List<EntDocumento> vlo_LsDocumentos = new ArrayList<>();
        if (pvo_MpDocumentos == null) {
            return vlo_LsDocumentos;
        }
        for (Map.Entry<String, List<MultipartFile>> vlo_Grupo : pvo_MpDocumentos.entrySet()) {
            for (MultipartFile vlo_Archivo : vlo_Grupo.getValue()) {
                if (vlo_Archivo != null && !vlo_Archivo.isEmpty()) {
                    vlo_LsDocumentos.add(this.vgo_BllDocumento.GuardarDocumento(
                            pvo_EnTramite, pvn_IdUsuario, vlo_Archivo,
                            this.NormalizarCodigo(vlo_Grupo.getKey()), pvo_LsRutasEscritas));
                }
            }
        }
        return vlo_LsDocumentos;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba que los documentos vengan con un tipo admitido por la importación
     *   temporal. Si el expediente está cerrado solo se admite el DUA de cierre.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: los documentos admitidos y el DUA de cierre dependen del tipo (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal del expediente
     * @param pvo_MpDocumentos archivos agrupados por código de tipo de documento
     * @param pvb_SoloDuaCierre true si el expediente está cerrado
     */
    private void ValidarTiposDocumento(
            TipoTramiteTemporal pvo_Tipo,
            Map<String, List<MultipartFile>> pvo_MpDocumentos,
            boolean pvb_SoloDuaCierre) {
        if (pvo_MpDocumentos == null) {
            return;
        }
        for (Map.Entry<String, List<MultipartFile>> vlo_Grupo : pvo_MpDocumentos.entrySet()) {
            boolean vlb_TieneArchivos = vlo_Grupo.getValue() != null && vlo_Grupo.getValue().stream()
                    .anyMatch(pvo_Archivo -> pvo_Archivo != null && !pvo_Archivo.isEmpty());
            if (!vlb_TieneArchivos) {
                continue;
            }
            String vlc_Codigo = this.NormalizarCodigo(vlo_Grupo.getKey());
            if (!pvo_Tipo.getTiposDocumento().contains(vlc_Codigo)) {
                throw new ExcepcionNegocio(pvo_Tipo.getMensajeTipoDocumentoInvalido());
            }
            if (pvb_SoloDuaCierre && !pvo_Tipo.getCodigoDuaCierre().equals(vlc_Codigo)) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_SOLO_DUA_CIERRE_EDITABLE);
            }
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba la longitud de los datos de aduana y que las fechas de los DUA y de la
     *   cancelación no sean posteriores a hoy.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: el vencimiento debe ser posterior a la fecha del DUA de inicio, y se validan
     *     los datos de la exportación temporal (HU-026): notificaciones y reimportación.
     *
     * @param pvo_EnTemporal datos temporales normalizados
     */
    private void ValidarDatosAduana(EntTramiteTemporal pvo_EnTemporal) {
        String vlc_Mensaje = Constantes.MENSAJE_DATO_ADUANA_INVALIDO;
        this.ValidarLongitud(pvo_EnTemporal.getNumeroFactura(), Constantes.LONGITUD_MAXIMA_NUMERO_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getOficioAduana(), Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getNumeroGestion(), Constantes.LONGITUD_MAXIMA_NUMERO_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getResolucionAutorizacion(),
                Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getOficioNotificacionUsuario(),
                Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getNumeroDuaInicio(), Constantes.LONGITUD_MAXIMA_NUMERO_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getNotificacionDuaAduana(),
                Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getNumeroDuaCierre(), Constantes.LONGITUD_MAXIMA_NUMERO_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getCancelacionRegimen(),
                Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA, vlc_Mensaje);
        this.ValidarLongitud(pvo_EnTemporal.getSolicitudIngreso(), Constantes.LONGITUD_MAXIMA_REFERENCIA_ADUANA,
                Constantes.MENSAJE_DATO_REIMPORTACION_INVALIDO);
        this.ValidarLongitud(pvo_EnTemporal.getDocumentoEmbarque(), Constantes.LONGITUD_MAXIMA_DOCUMENTO_EMBARQUE,
                Constantes.MENSAJE_DATO_REIMPORTACION_INVALIDO);
        LocalDate vld_Hoy = this.vgo_BllAlertaTemporal.Hoy();
        for (LocalDate vld_Fecha : new LocalDate[] {
                pvo_EnTemporal.getFechaDuaInicio(),
                pvo_EnTemporal.getFechaDuaCierre(),
                pvo_EnTemporal.getFechaCancelacion()}) {
            if (vld_Fecha != null && vld_Fecha.isAfter(vld_Hoy)) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_FECHA_DUA_INVALIDA);
            }
        }
        for (LocalDate vld_Fecha : new LocalDate[] {
                pvo_EnTemporal.getFechaReingreso(),
                pvo_EnTemporal.getFechaDesalmacenaje()}) {
            if (vld_Fecha != null && vld_Fecha.isAfter(vld_Hoy)) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_FECHA_REIMPORTACION_INVALIDA);
            }
        }
        if (pvo_EnTemporal.getFechaDuaInicio() != null && pvo_EnTemporal.getFechaVencimiento() != null
                && !pvo_EnTemporal.getFechaVencimiento().isAfter(pvo_EnTemporal.getFechaDuaInicio())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_VENCIMIENTO_ANTERIOR_AL_DUA);
        }
        BigDecimal vln_Impuestos = pvo_EnTemporal.getMontoImpuestosReimportacion();
        if (vln_Impuestos != null && (vln_Impuestos.signum() < 0
                || vln_Impuestos.compareTo(Constantes.MONTO_MAXIMO_IMPUESTOS_REIMPORTACION) > 0)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_IMPUESTOS_REIMPORTACION_INVALIDOS);
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 18/10/2026
     * Descripción: Describe un dato Sí/No para la bitácora.
     *
     * @param pvb_Valor valor lógico, o null si no se ha registrado
     * @return "Sí", "No" o null
     */
    private String DescribirSiNo(Boolean pvb_Valor) {
        return pvb_Valor == null ? null : (pvb_Valor ? "Sí" : "No");
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 18/10/2026
     * Descripción: Normaliza un monto para compararlo sin que 100 y 100.00 cuenten como cambio.
     *
     * @param pvn_Monto monto a normalizar
     * @return monto sin ceros decimales sobrantes, o null
     */
    private String SinCerosDecimales(BigDecimal pvn_Monto) {
        return pvn_Monto == null ? null : pvn_Monto.stripTrailingZeros().toPlainString();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Valida y asigna los datos que solo registra la exportación temporal (HU-026):
     *   la garantía, el peso y el país de destino son obligatorios; el antecedente y las
     *   notificaciones son opcionales.
     *
     * @param pvo_EnTemporal datos temporales que se registran
     * @param pvo_Registro datos del formulario
     */
    private void AsignarDatosDeSalida(EntTramiteTemporal pvo_EnTemporal, EntRegistroTramiteTemporal pvo_Registro) {
        String vlc_Antecedente = this.Normalizar(pvo_Registro.antecedente());
        this.ValidarLongitud(vlc_Antecedente, Constantes.LONGITUD_MAXIMA_ANTECEDENTE,
                Constantes.MENSAJE_ANTECEDENTE_INVALIDO);
        if (pvo_Registro.enGarantia() == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_GARANTIA_REQUERIDA);
        }
        BigDecimal vln_Peso = pvo_Registro.pesoKg();
        if (vln_Peso == null || vln_Peso.compareTo(BigDecimal.ZERO) <= 0
                || vln_Peso.compareTo(BigDecimal.valueOf(Constantes.PESO_MAXIMO_KG)) > 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PESO_INVALIDO);
        }
        EntPais vlo_EnPais = this.vgo_DalPais.SIMM_PAIS_ObtenerRegistro(pvo_Registro.idPaisDestino());
        if (vlo_EnPais == null || !Constantes.ESTADO_CATALOGO_ACTIVO.equals(vlo_EnPais.getEstado())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PAIS_DESTINO_INVALIDO);
        }

        pvo_EnTemporal.setAntecedente(vlc_Antecedente);
        pvo_EnTemporal.setEnGarantia(pvo_Registro.enGarantia());
        pvo_EnTemporal.setPesoKg(vln_Peso);
        pvo_EnTemporal.setIdPaisDestino(vlo_EnPais.getIdPais());
        pvo_EnTemporal.setOficioNotificacionUsuario(this.Normalizar(pvo_Registro.oficioNotificacionUsuario()));
        pvo_EnTemporal.setNotificacionDuaAduana(this.Normalizar(pvo_Registro.notificacionDuaAduana()));
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Registra el plazo estándar de la exportación temporal (HU-026): si el
     *   vencimiento es exactamente la fecha del DUA de exportación más 6 meses, el plazo queda en
     *   6 meses y la alerta de BllAlertaTemporal recomienda la declaración de reimportación al
     *   vencer. Si el vencimiento es otro (por ejemplo, tras una prórroga), el plazo se limpia. La
     *   importación temporal no registra plazo.
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @param pvo_EnTemporal datos temporales ya validados
     */
    private void AsignarPlazoEstandar(TipoTramiteTemporal pvo_Tipo, EntTramiteTemporal pvo_EnTemporal) {
        if (!this.EsExportacion(pvo_Tipo)) {
            return;
        }
        LocalDate vld_DuaInicio = pvo_EnTemporal.getFechaDuaInicio();
        boolean vlb_Estandar = vld_DuaInicio != null
                && vld_DuaInicio.plusMonths(Constantes.MESES_PLAZO_ESTANDAR_EXPORTACION)
                        .equals(pvo_EnTemporal.getFechaVencimiento());
        pvo_EnTemporal.setUnidadPlazo(vlb_Estandar ? Constantes.UNIDAD_PLAZO_MESES : null);
        pvo_EnTemporal.setCantidadPlazo(vlb_Estandar ? Constantes.MESES_PLAZO_ESTANDAR_EXPORTACION : null);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba que la referencia lleve la sigla del tipo, no supere los 45
     *   caracteres y no la use otro trámite. El formato del consecutivo no se restringe.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: la sigla y el mensaje dependen del tipo: IT- o EXP-T- (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @param pvc_Referencia referencia normalizada
     * @param pvo_EnTipoTramite tipo de trámite que define la sigla
     */
    private void ValidarReferencia(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_Referencia,
            EntTipoTramite pvo_EnTipoTramite) {
        String vlc_Prefijo = pvo_EnTipoTramite.getPrefijoReferencia();
        if (pvc_Referencia == null
                || !pvc_Referencia.startsWith(vlc_Prefijo)
                || pvc_Referencia.length() == vlc_Prefijo.length()
                || pvc_Referencia.length() > Constantes.LONGITUD_MAXIMA_REFERENCIA_INTERNA) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeReferenciaInvalida());
        }
        if (this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(pvc_Referencia)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DUPLICADA);
        }
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Comprueba que los agentes indicados tengan una cuenta validada con rol de
     *   agente de aduanas.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: los agentes son opcionales y el usuario que registra ya no tiene que ser
     *     agente: el responsable del expediente recibe las alertas de todos modos.
     *
     * @param pvo_LsAgentes agentes indicados en el formulario
     * @return agentes sin repetir
     */
    private Set<Integer> ValidarAgentes(List<Integer> pvo_LsAgentes) {
        Set<Integer> vlo_LsAgentes = new LinkedHashSet<>();
        if (pvo_LsAgentes == null || pvo_LsAgentes.isEmpty()) {
            return vlo_LsAgentes;
        }
        vlo_LsAgentes.addAll(pvo_LsAgentes);
        List<Integer> vlo_LsDisponibles = this.vgo_DalTramiteTemporal.SIMF_USUARIO_ObtenerAgentesResponsables()
                .stream()
                .map(EntAgenteTemporal::idUsuario)
                .toList();
        if (vlo_LsAgentes.contains(null) || !vlo_LsDisponibles.containsAll(vlo_LsAgentes)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_AGENTES_TEMPORAL_INVALIDOS);
        }
        return vlo_LsAgentes;
    }

    /**
     * Autor: Camila Pastor Castro
     * Fecha: 03/10/2026
     * Descripción: Calcula un vencimiento sumando un plazo a una fecha. Un plazo en años exige el
     *   número de contrato.
     *
     * @param pvd_FechaBase fecha desde la que corre el plazo
     * @param pvc_Unidad DIAS, MESES o ANOS
     * @param pvn_Cantidad cantidad del plazo
     * @param pvc_Contrato número de contrato
     * @return vencimiento calculado
     */
    private LocalDate CalcularVencimiento(
            LocalDate pvd_FechaBase,
            String pvc_Unidad,
            Integer pvn_Cantidad,
            String pvc_Contrato) {
        if (pvd_FechaBase == null || pvn_Cantidad == null || pvn_Cantidad <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PLAZO_TEMPORAL_INVALIDO);
        }
        int vln_Maximo = switch (pvc_Unidad == null ? "" : pvc_Unidad) {
            case UNIDAD_DIAS -> MAXIMO_DIAS_PLAZO;
            case UNIDAD_MESES -> MAXIMO_MESES_PLAZO;
            case UNIDAD_ANOS -> MAXIMO_ANOS_PLAZO;
            default -> 0;
        };
        if (vln_Maximo == 0 || pvn_Cantidad > vln_Maximo
                || (pvc_Contrato != null && pvc_Contrato.length() > 45)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PLAZO_TEMPORAL_INVALIDO);
        }
        if (UNIDAD_ANOS.equals(pvc_Unidad) && (pvc_Contrato == null || pvc_Contrato.isBlank())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_CONTRATO_TEMPORAL_REQUERIDO);
        }
        return switch (pvc_Unidad) {
            case UNIDAD_DIAS -> pvd_FechaBase.plusDays(pvn_Cantidad);
            case UNIDAD_MESES -> pvd_FechaBase.plusMonths(pvn_Cantidad);
            default -> pvd_FechaBase.plusYears(pvn_Cantidad);
        };
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Agrega a la descripción de la bitácora un dato que cambió, con su valor anterior
     *   y su valor nuevo.
     *
     * @param pvc_Campo nombre del dato
     * @param pvo_Anterior valor anterior
     * @param pvo_Nuevo valor nuevo
     * @param pvo_LsAnteriores descripción de los valores anteriores
     * @param pvo_LsNuevos descripción de los valores nuevos
     */
    private void CompararDato(
            String pvc_Campo,
            Object pvo_Anterior,
            Object pvo_Nuevo,
            List<String> pvo_LsAnteriores,
            List<String> pvo_LsNuevos) {
        if (Objects.equals(pvo_Anterior, pvo_Nuevo)) {
            return;
        }
        pvo_LsAnteriores.add(pvc_Campo + ": " + (pvo_Anterior == null ? "sin registrar" : pvo_Anterior) + ".");
        pvo_LsNuevos.add(pvc_Campo + ": " + (pvo_Nuevo == null ? "sin registrar" : pvo_Nuevo) + ".");
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Agrega un movimiento al historial de estados del expediente.
     *
     * @param pvo_EnTramite expediente
     * @param pvn_IdEstadoAnterior estado que deja, nulo en el registro inicial
     * @param pvo_EnEstadoNuevo estado que toma
     * @param pvn_IdUsuario usuario responsable del movimiento
     * @param pvc_Justificacion justificación del movimiento
     */
    private void RegistrarMovimientoEstado(
            EntTramite pvo_EnTramite,
            Integer pvn_IdEstadoAnterior,
            EntEstadoTramite pvo_EnEstadoNuevo,
            Integer pvn_IdUsuario,
            String pvc_Justificacion) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(pvn_IdEstadoAnterior);
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion(pvc_Justificacion);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Indica si un estado pertenece al alcance pedido de la lista.
     *
     * @param pvo_EnEstado estado del expediente
     * @param pvc_Alcance ACTIVOS, CERRADOS o TODOS
     * @return true si el expediente se muestra
     */
    private boolean PerteneceAlAlcance(EntEstadoTramite pvo_EnEstado, String pvc_Alcance) {
        boolean vlb_Cerrado = Boolean.TRUE.equals(pvo_EnEstado.getEsFinal());
        return switch (pvc_Alcance) {
            case Constantes.ALCANCE_TEMPORAL_ACTIVOS -> !vlb_Cerrado;
            case Constantes.ALCANCE_TEMPORAL_CERRADOS -> vlb_Cerrado;
            default -> true;
        };
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Indica si el tipo es la exportación temporal, que registra datos propios de la
     *   salida del equipo y de su reimportación (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @return true si es la exportación temporal
     */
    private boolean EsExportacion(TipoTramiteTemporal pvo_Tipo) {
        return TipoTramiteTemporal.EXPORTACION.equals(pvo_Tipo);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene del catálogo el tipo de trámite de importación temporal.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: obtiene el tipo de trámite temporal indicado: importación o exportación (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @return tipo de trámite del catálogo
     */
    private EntTipoTramite ObtenerTipoTramite(TipoTramiteTemporal pvo_Tipo) {
        EntTipoTramite vlo_EnTipoTramite = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(pvo_Tipo.getCodigoTipoTramite());
        if (vlo_EnTipoTramite == null) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeTipoNoConfigurado());
        }
        return vlo_EnTipoTramite;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Identifica el tipo de trámite temporal de un expediente, para las operaciones
     *   que lo reciben solo por su referencia (HU-026).
     *
     * @param pvo_EnTramite expediente común, o null si la referencia no existe
     * @return tipo de trámite temporal del expediente
     */
    private TipoTramiteTemporal ObtenerTipoDelTramite(EntTramite pvo_EnTramite) {
        if (pvo_EnTramite != null) {
            for (TipoTramiteTemporal vlo_Tipo : TipoTramiteTemporal.values()) {
                EntTipoTramite vlo_EnTipoTramite = this.vgo_DalTipoTramite
                        .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(vlo_Tipo.getCodigoTipoTramite());
                if (vlo_EnTipoTramite != null
                        && vlo_EnTipoTramite.getIdTipoTramite().equals(pvo_EnTramite.getIdTipoTramite())) {
                    return vlo_Tipo;
                }
            }
        }
        throw new ExcepcionNegocio(Constantes.MENSAJE_TRAMITE_TEMPORAL_NO_ENCONTRADO);
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Recupera un expediente de importación temporal por su referencia. La referencia
     *   es única en todo el sistema, por eso se comprueba también el tipo de trámite.
     *
     * Historial de cambios:
     *   Responsable: Josue Guevara Lostalo
     *   Fecha: 04/10/2026
     *   Descripción: recupera el expediente del tipo indicado: importación o exportación (HU-026).
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @param pvc_ReferenciaInterna referencia del expediente
     * @param pvo_EnTipoTramite tipo de trámite del catálogo
     * @return expediente común
     */
    private EntTramite ObtenerTramite(
            TipoTramiteTemporal pvo_Tipo,
            String pvc_ReferenciaInterna,
            EntTipoTramite pvo_EnTipoTramite) {
        String vlc_Referencia = this.NormalizarCodigo(pvc_ReferenciaInterna);
        EntTramite vlo_EnTramite = vlc_Referencia == null
                ? null
                : this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(vlc_Referencia);
        if (vlo_EnTramite == null
                || !pvo_EnTipoTramite.getIdTipoTramite().equals(vlo_EnTramite.getIdTipoTramite())) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeExpedienteNoEncontrado());
        }
        return vlo_EnTramite;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene los datos temporales de un expediente.
     *
     * @param pvo_Tipo tipo de trámite temporal
     * @param pvo_EnTramite expediente común
     * @return datos temporales
     */
    private EntTramiteTemporal ObtenerTemporal(TipoTramiteTemporal pvo_Tipo, EntTramite pvo_EnTramite) {
        EntTramiteTemporal vlo_EnTemporal = this.vgo_DalTramiteTemporal
                .SIMT_TRAMITE_TEMPORAL_ObtenerRegistro(pvo_EnTramite.getIdTramite());
        if (vlo_EnTemporal == null) {
            throw new ExcepcionNegocio(pvo_Tipo.getMensajeExpedienteNoEncontrado());
        }
        return vlo_EnTemporal;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene el estado en el que se encuentra el expediente.
     *
     * @param pvo_EnTramite expediente común
     * @return estado actual
     */
    private EntEstadoTramite ObtenerEstadoActual(EntTramite pvo_EnTramite) {
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_EnTramite.getIdEstadoTramite());
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_ACTUAL_NO_DISPONIBLE);
        }
        return vlo_EnEstado;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba que el expediente no esté cerrado.
     *
     * @param pvo_EnEstado estado actual
     */
    private void ValidarExpedienteAbierto(EntEstadoTramite pvo_EnEstado) {
        if (Boolean.TRUE.equals(pvo_EnEstado.getEsFinal())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_TEMPORAL_CERRADO);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene el usuario de la sesión y comprueba que esté registrado.
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
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Obtiene la moneda del valor declarado.
     *
     * @param pvc_Moneda código ISO de la moneda
     * @return moneda registrada
     */
    private EntMoneda ObtenerMoneda(String pvc_Moneda) {
        String vlc_Codigo = this.NormalizarCodigo(pvc_Moneda);
        EntMoneda vlo_EnMoneda = vlc_Codigo == null ? null : this.vgo_DalMoneda.SIMM_MONEDA_ObtenerPorCodigoIso(vlc_Codigo);
        if (vlo_EnMoneda == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_MONEDA_INVALIDA);
        }
        return vlo_EnMoneda;
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba que un dato obligatorio venga indicado y no supere su longitud.
     *
     * @param pvc_Valor dato normalizado
     * @param pvn_Maximo longitud máxima
     * @param pvc_Mensaje mensaje si el dato no es válido
     */
    private void ValidarRequerido(String pvc_Valor, int pvn_Maximo, String pvc_Mensaje) {
        if (pvc_Valor == null || pvc_Valor.length() > pvn_Maximo) {
            throw new ExcepcionNegocio(pvc_Mensaje);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Comprueba que un dato opcional no supere su longitud.
     *
     * @param pvc_Valor dato normalizado
     * @param pvn_Maximo longitud máxima
     * @param pvc_Mensaje mensaje si el dato no es válido
     */
    private void ValidarLongitud(String pvc_Valor, int pvn_Maximo, String pvc_Mensaje) {
        if (pvc_Valor != null && pvc_Valor.length() > pvn_Maximo) {
            throw new ExcepcionNegocio(pvc_Mensaje);
        }
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Elimina los espacios sobrantes; un texto vacío se trata como no indicado.
     *
     * @param pvc_Texto texto recibido
     * @return texto normalizado o null
     */
    private String Normalizar(String pvc_Texto) {
        return pvc_Texto == null || pvc_Texto.isBlank() ? null : pvc_Texto.trim();
    }

    /**
     * Autor: Josue Guevara Lostalo
     * Fecha: 04/10/2026
     * Descripción: Normaliza un código a mayúsculas.
     *
     * @param pvc_Codigo código recibido
     * @return código normalizado o null
     */
    private String NormalizarCodigo(String pvc_Codigo) {
        String vlc_Codigo = this.Normalizar(pvc_Codigo);
        return vlc_Codigo == null ? null : vlc_Codigo.toUpperCase(Locale.ROOT);
    }
}
