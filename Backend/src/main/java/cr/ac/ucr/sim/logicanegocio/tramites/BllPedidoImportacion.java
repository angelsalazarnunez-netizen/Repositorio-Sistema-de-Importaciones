package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPedidoImportacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCatalogosPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDetallePedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntNotificacionPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTransferenciaPedidoImportacion;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reglas del registro de solicitudes de importación (HU-006).
 */
@Service
public class BllPedidoImportacion {

    private static final String OPCION_UNIDAD = "UNIDAD";
    private static final String OPCION_COMPANIA = "COMPANIA";
    private static final String OPCION_INCOTERM = "INCOTERM";
    private static final String OPCION_MONEDA = "MONEDA";
    private static final long DIAS_ALERTA_PAGO = 3;

    private final DalTramite vgo_DalTramite;
    private final DalPedidoImportacion vgo_DalPedidoImportacion;
    private final DalTipoTramite vgo_DalTipoTramite;
    private final DalEstadoTramite vgo_DalEstadoTramite;
    private final DalHistorialEstado vgo_DalHistorialEstado;
    private final DalMoneda vgo_DalMoneda;

    public BllPedidoImportacion(
            DalTramite pvo_DalTramite,
            DalPedidoImportacion pvo_DalPedidoImportacion,
            DalTipoTramite pvo_DalTipoTramite,
            DalEstadoTramite pvo_DalEstadoTramite,
            DalHistorialEstado pvo_DalHistorialEstado,
            DalMoneda pvo_DalMoneda) {
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalPedidoImportacion = pvo_DalPedidoImportacion;
        this.vgo_DalTipoTramite = pvo_DalTipoTramite;
        this.vgo_DalEstadoTramite = pvo_DalEstadoTramite;
        this.vgo_DalHistorialEstado = pvo_DalHistorialEstado;
        this.vgo_DalMoneda = pvo_DalMoneda;
    }

    @Transactional
    public EntRespuestaPedidoImportacion InsertarRegistro(
            EntRegistroPedidoImportacion pvo_Registro,
            Integer pvn_IdUsuario,
            String pvc_NombreUsuario) {
        if (pvo_Registro == null || pvn_IdUsuario == null || pvn_IdUsuario <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }

        int vln_AnoActual = Year.now().getValue();
        String vlc_Referencia = this.Normalizar(pvo_Registro.numeroSolicitud());
        String vlc_Estado = this.Normalizar(pvo_Registro.estado());
        this.ValidarNumeracion(pvo_Registro, vln_AnoActual, vlc_Referencia);
        this.ValidarPartidas(pvo_Registro);

        if (this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna(vlc_Referencia)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_PEDIDO_IMPORTACION_DUPLICADA);
        }

        EntTipoTramite vlo_EnTipo = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION);
        if (vlo_EnTipo == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_CONFIGURADO);
        }
        EntEstadoTramite vlo_EnEstado = this.ObtenerEstado(vlo_EnTipo.getIdTipoTramite(), vlc_Estado);
        EntValorEstimado vlo_EnValorEstimado = this.ObtenerValorEstimado(pvo_Registro);

        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setReferenciaInterna(vlc_Referencia);
        vlo_EnTramite.setIdTipoTramite(vlo_EnTipo.getIdTipoTramite());
        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstado.getIdEstadoTramite());
        vlo_EnTramite.setIdUsuarioResponsable(pvn_IdUsuario);
        vlo_EnTramite.setIdMoneda(this.ObtenerIdMoneda(vlo_EnValorEstimado.codigoMoneda()));
        vlo_EnTramite.setValorEstimado(vlo_EnValorEstimado.valor());
        vlo_EnTramite.setDescripcion(pvo_Registro.descripcion().trim().substring(
                0, Math.min(100, pvo_Registro.descripcion().trim().length())));
        vlo_EnTramite.setObservaciones(pvo_Registro.descripcion().trim());
        this.vgo_DalTramite.SIMT_TRAMITE_InsertarRegistro(vlo_EnTramite);

        this.vgo_DalPedidoImportacion.SIMT_PEDIDO_PROV_InsertarRegistro(
                new EntPedidoImportacion(vlo_EnTramite.getIdTramite(), pvo_Registro));
        this.AbrirHistorialEstado(vlo_EnTramite, vlo_EnEstado, pvn_IdUsuario);
        this.GuardarOpciones(pvo_Registro);

        return new EntRespuestaPedidoImportacion(
                Constantes.MENSAJE_PEDIDO_IMPORTACION_EXITOSO,
                vlo_EnTramite.getIdTramite(),
                vlo_EnTramite.getReferenciaInterna(),
                vlo_EnEstado.getNombre(),
                pvc_NombreUsuario,
                vlo_EnTramite.getFechaCreacion());
    }

    @Transactional(readOnly = true)
    public List<EntResumenPedidoImportacion> ObtenerLista(Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || pvn_IdUsuario <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO);
        }
        return this.vgo_DalPedidoImportacion.SIMT_PEDIDO_PROV_ObtenerListaPorUsuario(pvn_IdUsuario);
    }

    @Transactional(readOnly = true)
    public EntDetallePedidoImportacion ObtenerRegistro(
            String pvc_Referencia,
            Integer pvn_IdUsuario,
            String pvc_NombreUsuario) {
        EntTramite vlo_EnTramite = this.ObtenerTramitePropio(pvc_Referencia, pvn_IdUsuario);
        EntPedidoImportacion vlo_EnPedido = this.vgo_DalPedidoImportacion
                .SIMT_PEDIDO_PROV_ObtenerPorReferenciaYUsuario(vlo_EnTramite.getReferenciaInterna(), pvn_IdUsuario);
        if (vlo_EnPedido == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO);
        }
        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerRegistro(vlo_EnTramite.getIdEstadoTramite());
        if (vlo_EnEstado == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO);
        }
        return new EntDetallePedidoImportacion(
                vlo_EnTramite.getIdTramite(),
                vlo_EnTramite.getReferenciaInterna(),
                vlo_EnEstado.getNombre(),
                pvc_NombreUsuario,
                vlo_EnPedido.ObtenerRegistro(vlo_EnTramite.getReferenciaInterna(), vlo_EnEstado.getNombre()));
    }

    @Transactional
    public EntRespuestaPedidoImportacion ModificarRegistro(
            String pvc_Referencia,
            EntRegistroPedidoImportacion pvo_Registro,
            Integer pvn_IdUsuario,
            String pvc_NombreUsuario) {
        if (pvo_Registro == null || pvn_IdUsuario == null || pvn_IdUsuario <= 0) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_DATOS_INCOMPLETOS);
        }
        String vlc_Referencia = this.NormalizarMayusculas(pvc_Referencia);
        if (!vlc_Referencia.equals(this.NormalizarMayusculas(pvo_Registro.numeroSolicitud()))
                || !vlc_Referencia.matches("\\d{4}-\\d{3}")) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_PEDIDO_IMPORTACION_INVALIDA);
        }
        int vln_AnoReferencia = Integer.parseInt(vlc_Referencia.substring(0, 4));
        this.ValidarNumeracion(pvo_Registro, vln_AnoReferencia, vlc_Referencia);
        this.ValidarPartidas(pvo_Registro);

        EntTramite vlo_EnTramite = this.ObtenerTramitePropio(vlc_Referencia, pvn_IdUsuario);
        EntPedidoImportacion vlo_EnPedido = this.vgo_DalPedidoImportacion
                .SIMT_PEDIDO_PROV_ObtenerPorReferenciaYUsuario(vlc_Referencia, pvn_IdUsuario);
        if (vlo_EnPedido == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO);
        }
        EntEstadoTramite vlo_EnEstadoAnterior = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerRegistro(vlo_EnTramite.getIdEstadoTramite());
        EntTipoTramite vlo_EnTipo = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerRegistro(vlo_EnTramite.getIdTipoTramite());
        if (vlo_EnEstadoAnterior == null || vlo_EnTipo == null
                || !Constantes.CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION.equals(vlo_EnTipo.getCodigo())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO);
        }

        EntEstadoTramite vlo_EnEstadoNuevo = this.ObtenerEstado(
                vlo_EnTipo.getIdTipoTramite(),
                this.Normalizar(pvo_Registro.estado()));
        EntValorEstimado vlo_EnValorEstimado = this.ObtenerValorEstimado(pvo_Registro);
        vlo_EnTramite.setIdEstadoTramite(vlo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnTramite.setIdMoneda(this.ObtenerIdMoneda(vlo_EnValorEstimado.codigoMoneda()));
        vlo_EnTramite.setValorEstimado(vlo_EnValorEstimado.valor());
        String vlc_Descripcion = pvo_Registro.descripcion().trim();
        vlo_EnTramite.setDescripcion(vlc_Descripcion.substring(0, Math.min(100, vlc_Descripcion.length())));
        vlo_EnTramite.setObservaciones(vlc_Descripcion);

        this.vgo_DalTramite.SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);
        this.vgo_DalPedidoImportacion.SIMT_PEDIDO_PROV_ModificarRegistro(
                new EntPedidoImportacion(vlo_EnTramite.getIdTramite(), pvo_Registro));
        if (!vlo_EnEstadoAnterior.getIdEstadoTramite().equals(vlo_EnEstadoNuevo.getIdEstadoTramite())) {
            this.AnotarCambioEstado(
                    vlo_EnTramite,
                    vlo_EnEstadoAnterior,
                    vlo_EnEstadoNuevo,
                    pvn_IdUsuario);
        }
        this.GuardarOpciones(pvo_Registro);

        return new EntRespuestaPedidoImportacion(
                Constantes.MENSAJE_PEDIDO_IMPORTACION_EXITOSO,
                vlo_EnTramite.getIdTramite(),
                vlo_EnTramite.getReferenciaInterna(),
                vlo_EnEstadoNuevo.getNombre(),
                pvc_NombreUsuario,
                vlo_EnTramite.getFechaCreacion());
    }

    @Transactional(readOnly = true)
    public EntCatalogosPedidoImportacion ObtenerCatalogos() {
        Set<String> vlo_Monedas = new LinkedHashSet<>(List.of("CRC", "USD", "EUR", "PEN"));
        for (EntMoneda vlo_EnMoneda : this.vgo_DalMoneda.SIMM_MONEDA_ObtenerActivas()) {
            vlo_Monedas.add(vlo_EnMoneda.getCodigoIso());
        }
        vlo_Monedas.addAll(this.vgo_DalPedidoImportacion.SIMM_OPCION_IMPORTACION_ObtenerPorTipo(OPCION_MONEDA));

        return new EntCatalogosPedidoImportacion(
                this.vgo_DalPedidoImportacion.SIMM_OPCION_IMPORTACION_ObtenerPorTipo(OPCION_UNIDAD),
                this.vgo_DalPedidoImportacion.SIMM_OPCION_IMPORTACION_ObtenerPorTipo(OPCION_COMPANIA),
                this.vgo_DalPedidoImportacion.SIMM_OPCION_IMPORTACION_ObtenerPorTipo(OPCION_INCOTERM),
                vlo_Monedas.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                this.ObtenerNombresEstados());
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 18/10/2026
     * Descripción: Obtiene los nombres de los estados activos del pedido, en el orden del
     *   catálogo. El primero es el estado inicial ("Pendiente de pago", según la Unidad).
     *
     * @return nombres de los estados activos, o una lista vacía si el tipo no está configurado
     */
    private List<String> ObtenerNombresEstados() {
        EntTipoTramite vlo_EnTipo = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION);
        if (vlo_EnTipo == null) {
            return List.of();
        }
        List<EntEstadoTramite> vlo_LsEstados = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(vlo_EnTipo.getIdTipoTramite(), "ACT");
        return vlo_LsEstados == null
                ? List.of()
                : vlo_LsEstados.stream().map(EntEstadoTramite::getNombre).toList();
    }

    @Transactional(readOnly = true)
    public List<EntNotificacionPedidoImportacion> ObtenerNotificaciones(Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || pvn_IdUsuario <= 0) {
            return List.of();
        }

        LocalDate vlo_FechaHoy = LocalDate.now();
        LocalDate vlo_FechaLimite = vlo_FechaHoy.minusDays(DIAS_ALERTA_PAGO);
        List<EntNotificacionPedidoImportacion> vlo_Notificaciones = new ArrayList<>();
        for (EntTransferenciaPedidoImportacion vlo_Transferencia : this.vgo_DalPedidoImportacion
                .SIMT_PEDIDO_PROV_ObtenerTransferenciasAnterioresA(pvn_IdUsuario, vlo_FechaLimite)) {
            long vln_DiasTranscurridos = ChronoUnit.DAYS.between(
                    vlo_Transferencia.fechaTransferencia(),
                    vlo_FechaHoy);
            vlo_Notificaciones.add(new EntNotificacionPedidoImportacion(
                    vlo_Transferencia.referenciaInterna(),
                    vlo_Transferencia.fechaTransferencia(),
                    vln_DiasTranscurridos,
                    "La transferencia tiene " + vln_DiasTranscurridos
                            + " días calendario. Revise si el pago ya fue realizado."));
        }
        return vlo_Notificaciones;
    }

    private EntEstadoTramite ObtenerEstado(Integer pvn_IdTipoTramite, String pvc_Nombre) {
        if (pvc_Nombre == null || pvc_Nombre.isBlank()) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ESTADO_PEDIDO_IMPORTACION_REQUERIDO);
        }

        EntEstadoTramite vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerPorNombre(pvn_IdTipoTramite, pvc_Nombre, "ACT");
        if (vlo_EnEstado != null) {
            return vlo_EnEstado;
        }

        vlo_EnEstado = this.vgo_DalEstadoTramite
                .SIMM_ESTADO_TRAMITE_ObtenerPorNombreCualquiera(pvn_IdTipoTramite, pvc_Nombre);
        if (vlo_EnEstado != null) {
            vlo_EnEstado.setEstado("ACT");
            this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ModificarRegistro(vlo_EnEstado);
            return vlo_EnEstado;
        }

        EntEstadoTramite vlo_NuevoEstado = new EntEstadoTramite();
        vlo_NuevoEstado.setIdTipoTramite(pvn_IdTipoTramite);
        vlo_NuevoEstado.setNombre(pvc_Nombre);
        vlo_NuevoEstado.setEsInicial(false);
        vlo_NuevoEstado.setEsFinal(false);
        vlo_NuevoEstado.setOrden((short) 1);
        vlo_NuevoEstado.setEstado("ACT");
        this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_InsertarRegistro(vlo_NuevoEstado);
        return vlo_NuevoEstado;
    }

    private EntMoneda ObtenerMoneda(String pvc_Moneda) {
        String vlc_Moneda = this.NormalizarMayusculas(pvc_Moneda);
        if (vlc_Moneda == null || vlc_Moneda.length() != 3) {
            return null;
        }
        return this.vgo_DalMoneda.SIMM_MONEDA_ObtenerPorCodigoIso(vlc_Moneda);
    }

    private Integer ObtenerIdMoneda(String pvc_Moneda) {
        EntMoneda vlo_EnMoneda = this.ObtenerMoneda(pvc_Moneda);
        return vlo_EnMoneda == null ? null : vlo_EnMoneda.getIdMoneda();
    }

    private EntValorEstimado ObtenerValorEstimado(EntRegistroPedidoImportacion pvo_Registro) {
        if (this.EsValorEstimable(pvo_Registro.montoOrden())) {
            return new EntValorEstimado(
                    pvo_Registro.montoOrden(),
                    this.NormalizarMayusculas(pvo_Registro.monedaOrden()));
        }
        if (this.EsValorEstimable(pvo_Registro.presupuestoOrdenCRC())) {
            return new EntValorEstimado(pvo_Registro.presupuestoOrdenCRC(), "CRC");
        }
        return new EntValorEstimado(null, null);
    }

    private boolean EsValorEstimable(BigDecimal pvo_Valor) {
        return pvo_Valor != null && pvo_Valor.signum() > 0 && pvo_Valor.precision() <= 13;
    }

    private void GuardarOpciones(EntRegistroPedidoImportacion pvo_Registro) {
        this.GuardarOpcion(OPCION_UNIDAD, pvo_Registro.unidadSolicitante());
        this.GuardarOpcion(OPCION_COMPANIA, pvo_Registro.companiaAdjudicada());
        this.GuardarOpcion(OPCION_INCOTERM, pvo_Registro.incoterm());
        this.GuardarOpcion(OPCION_MONEDA, pvo_Registro.monedaOrden());
    }

    private void GuardarOpcion(String pvc_Tipo, String pvc_Valor) {
        if (pvc_Valor != null && !pvc_Valor.isBlank()) {
            this.vgo_DalPedidoImportacion.SIMM_OPCION_IMPORTACION_GuardarSiNoExiste(
                    pvc_Tipo,
                    pvc_Valor.trim());
        }
    }

    private void AbrirHistorialEstado(
            EntTramite pvo_EnTramite,
            EntEstadoTramite pvo_EnEstado,
            Integer pvn_IdUsuario) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstado.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion("Registro inicial de la solicitud de importación.");
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    private void AnotarCambioEstado(
            EntTramite pvo_EnTramite,
            EntEstadoTramite pvo_EnEstadoAnterior,
            EntEstadoTramite pvo_EnEstadoNuevo,
            Integer pvn_IdUsuario) {
        EntHistorialEstado vlo_EnHistorial = new EntHistorialEstado();
        vlo_EnHistorial.setIdTramite(pvo_EnTramite.getIdTramite());
        vlo_EnHistorial.setIdEstadoAnterior(pvo_EnEstadoAnterior.getIdEstadoTramite());
        vlo_EnHistorial.setIdEstadoNuevo(pvo_EnEstadoNuevo.getIdEstadoTramite());
        vlo_EnHistorial.setIdUsuario(pvn_IdUsuario);
        vlo_EnHistorial.setJustificacion("Actualización de la solicitud de importación.");
        this.vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_EnHistorial);
    }

    private EntTramite ObtenerTramitePropio(String pvc_Referencia, Integer pvn_IdUsuario) {
        EntTramite vlo_EnTramite = this.vgo_DalTramite
                .SIMT_TRAMITE_ObtenerPorReferenciaInterna(this.NormalizarMayusculas(pvc_Referencia));
        if (vlo_EnTramite == null
                || !pvn_IdUsuario.equals(vlo_EnTramite.getIdUsuarioResponsable())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO);
        }
        EntTipoTramite vlo_EnTipo = this.vgo_DalTipoTramite
                .SIMM_TIPO_TRAMITE_ObtenerRegistro(vlo_EnTramite.getIdTipoTramite());
        if (vlo_EnTipo == null
                || !Constantes.CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION.equals(vlo_EnTipo.getCodigo())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO);
        }
        return vlo_EnTramite;
    }

    private void ValidarNumeracion(
            EntRegistroPedidoImportacion pvo_Registro,
            int pvn_AnoActual,
            String pvc_Referencia) {
        String vlc_Ano = Integer.toString(pvn_AnoActual);
        if (pvc_Referencia == null || !pvc_Referencia.matches(vlc_Ano + "-\\d{3}")) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_REFERENCIA_PEDIDO_IMPORTACION_INVALIDA);
        }
        String vlc_Contratacion = this.Normalizar(pvo_Registro.numeroContratacion());
        String vlc_Pedido = this.Normalizar(pvo_Registro.numeroPedido());
        String vlc_OrdenCompra = this.Normalizar(pvo_Registro.numeroOrdenCompra());
        if (vlc_Contratacion == null || !vlc_Contratacion.matches(vlc_Ano + "-CFT-\\d{6} UADQ-INT")
                || vlc_Pedido == null || !vlc_Pedido.matches("\\d{3}-" + vlc_Ano)
                || vlc_OrdenCompra == null || !vlc_OrdenCompra.matches(vlc_Ano + "-\\d{3}")) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_NUMERACION_PEDIDO_IMPORTACION_INVALIDA);
        }
    }

    private void ValidarPartidas(EntRegistroPedidoImportacion pvo_Registro) {
        Integer vln_Cantidad = pvo_Registro.cantidadPartidas();
        Integer vln_Partida = pvo_Registro.partida();
        Integer vln_Partida2 = pvo_Registro.partida2();
        if ((vln_Cantidad == null || (vln_Cantidad != 1 && vln_Cantidad != 2))
                || !this.EsPartidaValida(vln_Partida)
                || (vln_Cantidad == 1 && vln_Partida2 != null)
                || (vln_Cantidad == 2 && !this.EsPartidaValida(vln_Partida2))) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_PARTIDA_PEDIDO_IMPORTACION_INVALIDA);
        }
    }

    private boolean EsPartidaValida(Integer pvn_Partida) {
        return pvn_Partida != null && (pvn_Partida == 1 || pvn_Partida == 2 || pvn_Partida == 5);
    }

    private String Normalizar(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim();
    }

    private String NormalizarMayusculas(String pvc_Texto) {
        return pvc_Texto == null ? null : pvc_Texto.trim().toUpperCase(Locale.ROOT);
    }

    private record EntValorEstimado(BigDecimal valor, String codigoMoneda) {
    }
}
