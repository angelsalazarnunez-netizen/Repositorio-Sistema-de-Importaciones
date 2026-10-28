package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDesalmacenajeEspecial;
import cr.ac.ucr.sim.accesodatos.tramites.DalDonacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalPedidoImportacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioReferencia;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCostosTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDesalmacenajeEspecial;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaCambioReferencia;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 23/09/2026
 * Descripción: Reglas de negocio comunes a cualquier expediente. Por ahora atiende el cambio de
 *   referencia interna: la Unidad de Importaciones corrige la referencia cuando se digitó mal y,
 *   cuando la sigla nueva pertenece a otro tipo de trámite, el expediente se reclasifica sin
 *   perder su documentación ni su bitácora. Es la mejora que pidió la persona usuaria para pasar,
 *   por ejemplo, una donación (D-) a un desalmacenaje especial (DE-).
 *
 * Historial de cambios:
 *   23/09/2026 Katherine Susana Valverde Fallas: creación de la clase.
 */
@Service
public class BllTramite {

    private final DalTramite vgo_DalTramite;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalDesalmacenajeEspecial vgo_DalDesalmacenajeEspecial;
    private final DalExoneracion vgo_DalExoneracion;
    private final DalDonacion vgo_DalDonacion;
    private final DalPedidoImportacion vgo_DalPedidoImportacion;
    private final DalUsuario vgo_DalUsuario;
    private final BllBitacora vgo_BllBitacora;

    public BllTramite(
            DalTramite pvo_DalTramite,
            DalTipoTramite pvo_DalTipoTramite,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalDesalmacenajeEspecial pvo_DalDesalmacenajeEspecial,
            DalExoneracion pvo_DalExoneracion,
            DalDonacion pvo_DalDonacion,
            DalPedidoImportacion pvo_DalPedidoImportacion,
            DalUsuario pvo_DalUsuario,
            BllBitacora pvo_BllBitacora) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalDesalmacenajeEspecial = pvo_DalDesalmacenajeEspecial;
        this.vgo_DalExoneracion = pvo_DalExoneracion;
        this.vgo_DalDonacion = pvo_DalDonacion;
        this.vgo_DalPedidoImportacion = pvo_DalPedidoImportacion;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_BllBitacora = pvo_BllBitacora;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Cambia la referencia interna de un expediente. Si la sigla de la referencia
     *   nueva pertenece a otro tipo de trámite, el expediente se reclasifica: se retiran los datos
     *   propios del tipo anterior, se registran los del tipo nuevo y el expediente arranca en el
     *   estado inicial de ese tipo. La documentación y la bitácora se conservan, y el cambio queda
     *   anotado con su justificación.
     *
     * @param pvc_ReferenciaInterna referencia interna actual del expediente
     * @param pvo_Cambio referencia nueva, justificación y datos que exige el tipo destino
     * @return referencia anterior y nueva, tipo resultante y si hubo reclasificación
     */
    @Transactional
    public EntRespuestaCambioReferencia CambiarReferencia(
            String pvc_ReferenciaInterna,
            EntCambioReferencia pvo_Cambio) {

        if (pvo_Cambio == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvo_Cambio.getIdUsuarioResponsable());
        String vlc_Anterior = vlo_EnTramite.getReferenciaInterna();
        String vlc_Nueva = this.NormalizarMayusculas(pvo_Cambio.getReferenciaInterna());
        String vlc_Justificacion = this.NormalizarTexto(pvo_Cambio.getJustificacion());

        this.ValidarJustificacion(vlc_Justificacion);
        if (vlc_Nueva == null || vlc_Nueva.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_NUEVA_INVALIDA);
        }
        if (vlc_Nueva.equals(vlc_Anterior)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_SIN_CAMBIO);
        }
        if (this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(vlc_Nueva)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_DUPLICADA);
        }

        EntTipoTramite vlo_EnTipoDestino =
                this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorReferencia(vlc_Nueva);
        if (vlo_EnTipoDestino == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_SIGLA_NO_RECONOCIDA);
        }
        if (vlc_Nueva.length() <= vlo_EnTipoDestino.getPrefijoReferencia().length()
                || vlc_Nueva.length() > Constantes.LONGITUD_MAXIMA_REFERENCIA_INTERNA) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_NUEVA_INVALIDA);
        }

        EntTipoTramite vlo_EnTipoActual =
                this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(vlo_EnTramite.getIdTipoTramite());
        boolean vlb_Reclasifica = !vlo_EnTipoDestino.getIdTipoTramite().equals(vlo_EnTramite.getIdTipoTramite());
        EntEstadoTramite vlo_EnEstadoDestino = null;

        if (vlb_Reclasifica) {
            this.RetirarDatosPropios(vlo_EnTramite, vlo_EnTipoActual);
            this.RegistrarDatosPropios(vlo_EnTramite, vlo_EnTipoDestino, pvo_Cambio, vlc_Justificacion);
            vlo_EnEstadoDestino = this.vgo_DalEstadoTramite
                    .SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(vlo_EnTipoDestino.getIdTipoTramite());
            if (vlo_EnEstadoDestino == null) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_INICIAL_DESTINO_NO_CONFIGURADO);
            }
            vlo_EnTramite.setIdTipoTramite(vlo_EnTipoDestino.getIdTipoTramite());
            vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoDestino.getIdEstadoTramite());
        }

        vlo_EnTramite.setReferenciaInterna(vlc_Nueva);
        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        if (vlb_Reclasifica) {
            this.AnotarHistorialEstado(vlo_EnTramite, vlo_EnEstadoDestino, vlo_EnUsuario.getIdUsuario(),
                    vlc_Justificacion);
        }

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_REFERENCIA_MODIFICADA,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                this.DescribirTramite(vlc_Anterior, vlo_EnTipoActual),
                this.DescribirTramite(vlc_Nueva, vlo_EnTipoDestino) + " Justificación: " + vlc_Justificacion);

        EntRespuestaCambioReferencia vlo_Respuesta = new EntRespuestaCambioReferencia();
        vlo_Respuesta.setMensaje(Constantes.MENSAJE_REFERENCIA_MODIFICADA);
        vlo_Respuesta.setReferenciaAnterior(vlc_Anterior);
        vlo_Respuesta.setReferenciaInterna(vlc_Nueva);
        vlo_Respuesta.setTipoTramiteAnterior(vlo_EnTipoActual == null ? null : vlo_EnTipoActual.getNombre());
        vlo_Respuesta.setTipoTramite(vlo_EnTipoDestino.getNombre());
        vlo_Respuesta.setCodigoTipoTramite(vlo_EnTipoDestino.getCodigo());
        vlo_Respuesta.setEstado(vlo_EnEstadoDestino == null ? null : vlo_EnEstadoDestino.getNombre());
        vlo_Respuesta.setReclasificado(vlb_Reclasifica);
        return vlo_Respuesta;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Retira los datos propios del tipo que deja de corresponderle al expediente.
     *   Los tipos que no guardan datos propios no requieren ninguna acción.
     *
     * @param pvo_EnTramite expediente que se reclasifica
     * @param pvo_EnTipoActual tipo de trámite que tenía el expediente
     */
    private void RetirarDatosPropios(EntTramite pvo_EnTramite, EntTipoTramite pvo_EnTipoActual) {
        if (pvo_EnTipoActual == null) {
            return;
        }
        switch (pvo_EnTipoActual.getCodigo()) {
            case Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE -> this.vgo_DalDesalmacenajeEspecial
                    .SIMT_DESALMACENAJE_ESPEC_EliminarRegistro(pvo_EnTramite.getIdTramite());
            case Constantes.CODIGO_TIPO_TRAMITE_EXONERACION -> this.vgo_DalExoneracion
                    .SIMT_EXONERACION_EliminarRegistro(pvo_EnTramite.getIdTramite());
            case Constantes.CODIGO_TIPO_TRAMITE_DONACION -> this.vgo_DalDonacion
                    .SIMT_DONACION_EliminarRegistro(pvo_EnTramite.getIdTramite());
            case Constantes.CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION -> this.vgo_DalPedidoImportacion
                    .SIMT_PEDIDO_PROV_EliminarRegistro(pvo_EnTramite.getIdTramite());
            default -> { }
        }
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 23/09/2026
     * Descripción: Registra los datos propios que exige el tipo al que pasa el expediente. El
     *   sistema reclasifica hacia desalmacenaje especial o exoneración, que son los tipos que la
     *   Unidad corrige con más frecuencia.
     *
     * @param pvo_EnTramite expediente que se reclasifica
     * @param pvo_EnTipoDestino tipo de trámite al que pasa el expediente
     * @param pvo_Cambio datos digitados para el tipo destino
     * @param pvc_Justificacion justificación del cambio, que respalda el trámite reclasificado
     */
    private void RegistrarDatosPropios(
            EntTramite pvo_EnTramite,
            EntTipoTramite pvo_EnTipoDestino,
            EntCambioReferencia pvo_Cambio,
            String pvc_Justificacion) {

        if (Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE.equals(pvo_EnTipoDestino.getCodigo())) {
            String vlc_Dependencia = this.NormalizarTexto(pvo_Cambio.getOrigenDependencia());
            if (vlc_Dependencia == null || vlc_Dependencia.isEmpty()) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_DEPENDENCIA_DESTINO_REQUERIDA);
            }
            EntDesalmacenajeEspecial vlo_EnDesalmacenaje = new EntDesalmacenajeEspecial();
            vlo_EnDesalmacenaje.setIdTramite(pvo_EnTramite.getIdTramite());
            vlo_EnDesalmacenaje.setOrigenDependencia(vlc_Dependencia);
            vlo_EnDesalmacenaje.setJustificacion(pvc_Justificacion);
            this.vgo_DalDesalmacenajeEspecial.SIMT_DESALMACENAJE_ESPEC_InsertarRegistro(vlo_EnDesalmacenaje);
            return;
        }

        if (Constantes.CODIGO_TIPO_TRAMITE_EXONERACION.equals(pvo_EnTipoDestino.getCodigo())) {
            String vlc_TipoExoneracion = this.NormalizarMayusculas(pvo_Cambio.getTipoExoneracion());
            String vlc_NumeroContratacion = this.NormalizarTexto(pvo_Cambio.getNumeroContratacion());
            String vlc_ContratoSicop = this.NormalizarTexto(pvo_Cambio.getContratoSicop());
            String vlc_OrdenCompraGeco = this.NormalizarTexto(pvo_Cambio.getOrdenCompraGeco());

            if (!BllExoneracion.TIPOS_EXONERACION.contains(vlc_TipoExoneracion)) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_EXONERACION_INVALIDO);
            }
            if (vlc_NumeroContratacion == null || vlc_NumeroContratacion.isEmpty()) {
                throw new ExcepcionNegocio(Constantes.MENSAJE_NUMERO_CONTRATACION_REQUERIDO);
            }

            EntExoneracion vlo_EnExoneracion = new EntExoneracion();
            vlo_EnExoneracion.setIdTramite(pvo_EnTramite.getIdTramite());
            vlo_EnExoneracion.setTipoExoneracion(vlc_TipoExoneracion);
            vlo_EnExoneracion.setNumeroContratacion(vlc_NumeroContratacion);
            vlo_EnExoneracion.setContratoSicop(vlc_ContratoSicop);
            vlo_EnExoneracion.setOrdenCompraGeco(vlc_OrdenCompraGeco);
            this.vgo_DalExoneracion.SIMT_EXONERACION_InsertarRegistro(vlo_EnExoneracion);
            return;
        }

        throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_DESTINO_NO_SOPORTADO);
    }

    private void AnotarHistorialEstado(
            EntTramite pvo_EnTramite,
            EntEstadoTramite pvo_EnEstado,
            Integer pvn_IdUsuario,
            String pvc_Justificacion) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstado.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion(pvc_Justificacion);
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    private String DescribirTramite(String pvc_ReferenciaInterna, EntTipoTramite pvo_EnTipoTramite) {
        return "Referencia: " + pvc_ReferenciaInterna + ". Tipo de trámite: "
                + (pvo_EnTipoTramite == null ? "sin dato" : pvo_EnTipoTramite.getNombre()) + ".";
    }

    private EntTramite ObtenerTramite(String pvc_ReferenciaInterna) {
        EntTramite vlo_EnTramite = this.vgo_DalTramite
                .SIMT_TRAMITE_ObtenerPorReferenciaInterna(this.NormalizarMayusculas(pvc_ReferenciaInterna));
        if (vlo_EnTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO);
        }
        return vlo_EnTramite;
    }

    private EntUsuario ObtenerUsuario(Integer pvn_IdUsuario) {
        EntUsuario vlo_EnUsuario = pvn_IdUsuario == null
                ? null
                : this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario);
        if (vlo_EnUsuario == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        return vlo_EnUsuario;
    }

    private void ValidarJustificacion(String pvc_Justificacion) {
        boolean vlb_Valida = pvc_Justificacion != null
                && pvc_Justificacion.length() >= Constantes.LONGITUD_MINIMA_JUSTIFICACION_CAMBIO
                && pvc_Justificacion.length() <= Constantes.LONGITUD_MAXIMA_JUSTIFICACION_CAMBIO;
        if (!vlb_Valida) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA);
        }
    }

    private String NormalizarTexto(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim();
    }

    private String NormalizarMayusculas(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim().toUpperCase();
    }
    private String DescribirValor(String pvc_Dato) {
        return pvc_Dato == null || pvc_Dato.isEmpty() ? "sin dato" : pvc_Dato;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 26/09/2026
     * Descripción: Registra los datos de costo del expediente (HU-036): el partido al que se
     *   le carga el gasto, los impuestos pagados y exonerados y el monto de bodegaje. Son los
     *   montos que totaliza el reporte de consultas internas, por eso cada cambio queda anotado
     *   en la bitácora con su responsable y su fecha.
     *
     * @param pvc_ReferenciaInterna referencia interna del expediente
     * @param pvo_Costos datos de costo que se registran
     * @return expediente con sus datos de costo actualizados
     */
    @Transactional
    public EntTramite RegistrarCostos(String pvc_ReferenciaInterna, EntCostosTramite pvo_Costos) {
        if (pvo_Costos == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        EntTramite vlo_EnTramite = this.ObtenerTramite(pvc_ReferenciaInterna);
        EntUsuario vlo_EnUsuario = this.ObtenerUsuario(pvo_Costos.getIdUsuarioResponsable());

        String vlc_TipoPartido = this.NormalizarTexto(pvo_Costos.getTipoPartidoResponsable());
        String vlc_Partido = this.ArmarPartidoResponsable(vlc_TipoPartido,
                this.NormalizarTexto(pvo_Costos.getPartidoResponsable()));
        this.ValidarMonto(pvo_Costos.getImpuestosPagados());
        this.ValidarMonto(pvo_Costos.getImpuestosExonerados());
        this.ValidarMonto(pvo_Costos.getMontoBodegaje());

        List<String> vlo_LsAnteriores = new ArrayList<>();
        List<String> vlo_LsCambios = new ArrayList<>();

        if (vlc_Partido != null
                && !vlc_Partido.equals(this.DescribirTexto(vlo_EnTramite.getPartidoResponsable()))) {
            vlo_LsAnteriores.add("Partido responsable: "
                    + this.DescribirValor(vlo_EnTramite.getPartidoResponsable()));
            vlo_LsCambios.add("Partido responsable: " + this.DescribirValor(vlc_Partido));
            vlo_EnTramite.setPartidoResponsable(vlc_Partido.isEmpty() ? null : vlc_Partido);
            vlo_EnTramite.setTipoPartidoResponsable(
                    vlc_Partido.isEmpty() ? null : vlc_TipoPartido.toUpperCase());
        }

        if (this.CambiaMonto(pvo_Costos.getImpuestosPagados(), vlo_EnTramite.getImpuestosPagados())) {
            vlo_LsAnteriores.add("Impuestos pagados: "
                    + this.DescribirMonto(vlo_EnTramite.getImpuestosPagados()));
            vlo_LsCambios.add("Impuestos pagados: "
                    + this.DescribirMonto(pvo_Costos.getImpuestosPagados()));
            vlo_EnTramite.setImpuestosPagados(pvo_Costos.getImpuestosPagados());
        }

        if (this.CambiaMonto(pvo_Costos.getImpuestosExonerados(), vlo_EnTramite.getImpuestosExonerados())) {
            vlo_LsAnteriores.add("Impuestos exonerados: "
                    + this.DescribirMonto(vlo_EnTramite.getImpuestosExonerados()));
            vlo_LsCambios.add("Impuestos exonerados: "
                    + this.DescribirMonto(pvo_Costos.getImpuestosExonerados()));
            vlo_EnTramite.setImpuestosExonerados(pvo_Costos.getImpuestosExonerados());
        }

        if (this.CambiaMonto(pvo_Costos.getMontoBodegaje(), vlo_EnTramite.getMontoBodegaje())) {
            vlo_LsAnteriores.add("Bodegaje (₡): " + this.DescribirMonto(vlo_EnTramite.getMontoBodegaje()));
            vlo_LsCambios.add("Bodegaje (₡): " + this.DescribirMonto(pvo_Costos.getMontoBodegaje()));
            vlo_EnTramite.setMontoBodegaje(pvo_Costos.getMontoBodegaje());
        }

        if (vlo_LsCambios.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_COSTOS_SIN_CAMBIO);
        }

        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);

        this.vgo_BllBitacora.RegistrarAccion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnUsuario.getIdUsuario(),
                Constantes.ACCION_BITACORA_COSTOS_MODIFICADOS,
                Constantes.ENTIDAD_BITACORA_TRAMITE,
                vlo_EnTramite.getIdTramite(),
                String.join(". ", vlo_LsAnteriores) + ".",
                String.join(". ", vlo_LsCambios) + ".");

        return vlo_EnTramite;
    }

    private void ValidarMonto(BigDecimal pvn_Monto) {
        if (pvn_Monto != null && pvn_Monto.signum() < 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_MONTO_NO_PUEDE_SER_NEGATIVO);
        }
    }

    private boolean CambiaMonto(BigDecimal pvn_Nuevo, BigDecimal pvn_Actual) {
        return pvn_Nuevo != null && (pvn_Actual == null || pvn_Nuevo.compareTo(pvn_Actual) != 0);
    }

    private String DescribirMonto(BigDecimal pvn_Monto) {
        return pvn_Monto == null ? "sin dato" : pvn_Monto.toPlainString();
    }

    private String DescribirTexto(String pvc_Dato) {
        return pvc_Dato == null ? "" : pvc_Dato;
    }

    /**
     * Autor: Katherine Susana Valverde Fallas
     * Fecha: 05/10/2026
     * Descripción: Arma el nombre con el que se agrupa el partido responsabilizado por el
     *   costo (HU-036). Cuando el costo lo asume la propia Unidad de Importaciones el nombre
     *   es el de la Unidad; cuando lo asume el partido solicitante o una orden de compra, es
     *   el nombre o el número que indica la persona usuaria.
     *
     * @param pvc_Tipo tipo de partido responsable
     * @param pvc_Detalle nombre del solicitante o número de la orden de compra
     * @return nombre con el que queda registrado, o null si no se indicó nada
     */
    private String ArmarPartidoResponsable(String pvc_Tipo, String pvc_Detalle) {
        if (pvc_Tipo == null || pvc_Tipo.isEmpty()) {
            return pvc_Detalle != null && !pvc_Detalle.isEmpty() ? pvc_Detalle : null;
        }

        String vlc_Tipo = pvc_Tipo.toUpperCase();
        if (Constantes.TIPO_PARTIDO_UNIDAD_IMPORTACIONES.equals(vlc_Tipo)) {
            return Constantes.NOMBRE_UNIDAD_IMPORTACIONES;
        }
        if (!Constantes.TIPO_PARTIDO_SOLICITANTE.equals(vlc_Tipo)
                && !Constantes.TIPO_PARTIDO_ORDEN_COMPRA.equals(vlc_Tipo)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_TIPO_PARTIDO_INVALIDO);
        }
        if (pvc_Detalle == null || pvc_Detalle.isEmpty()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DETALLE_PARTIDO_REQUERIDO);
        }
        if (pvc_Detalle.length() > Constantes.LONGITUD_MAXIMA_PARTIDO_RESPONSABLE) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PARTIDO_RESPONSABLE_INVALIDO);
        }
        return Constantes.TIPO_PARTIDO_ORDEN_COMPRA.equals(vlc_Tipo)
                ? "Orden de compra " + pvc_Detalle
                : pvc_Detalle;
    }
}
