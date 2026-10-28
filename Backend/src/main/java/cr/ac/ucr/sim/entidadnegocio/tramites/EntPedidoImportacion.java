package cr.ac.ucr.sim.entidadnegocio.tramites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos específicos de la solicitud de importación, asociados al expediente común.
 */
@Entity
@Table(name = "SIMT_PEDIDO_PROV")
public class EntPedidoImportacion {

    @Id
    @Column(name = "ID_TRAMITE")
    private Integer vgn_IdTramite;

    @Column(name = "CANTIDAD_SOLICITADA", precision = 13, scale = 3)
    private BigDecimal vgn_CantidadSolicitada;

    @Column(name = "FECHA_RECIBIDO_EXPEDIENTE", nullable = false)
    private LocalDate vgd_FechaRecibidoExpediente;

    @Column(name = "UNIDAD_SOLICITANTE", nullable = false, length = 120)
    private String vgc_UnidadSolicitante;

    @Column(name = "DESCRIPCION_SOLICITUD", nullable = false, columnDefinition = "TEXT")
    private String vgc_Descripcion;

    @Column(name = "NUMERO_CONTRATACION", nullable = false, length = 100)
    private String vgc_NumeroContratacion;

    @Column(name = "NUMERO_PEDIDO", nullable = false, length = 20)
    private String vgc_NumeroPedido;

    @Column(name = "COMPANIA_ADJUDICADA", nullable = false, length = 160)
    private String vgc_CompaniaAdjudicada;

    @Column(name = "NUMERO_ORDEN_COMPRA", nullable = false, length = 20)
    private String vgc_NumeroOrdenCompra;

    @Column(name = "INCOTERM", length = 10)
    private String vgc_Incoterm;

    @Column(name = "PRESUPUESTO_ORDEN_CRC", precision = 15, scale = 2)
    private BigDecimal vgn_PresupuestoOrdenCRC;

    @Column(name = "MONTO_ORDEN", precision = 18, scale = 2)
    private BigDecimal vgn_MontoOrden;

    @Column(name = "MONEDA_ORDEN", nullable = false, length = 30)
    private String vgc_MonedaOrden;

    @Column(name = "SALDO_DISPONIBLE", precision = 18, scale = 2)
    private BigDecimal vgn_SaldoDisponible;

    @Column(name = "FECHA_RECIBIDO_SP_OAF")
    private LocalDate vgd_FechaRecibidoSpOaf;

    @Column(name = "FECHA_RECIBIDO_COPIA_CHEQUE_UI")
    private LocalDate vgd_FechaRecibidoCopiaChequeUi;

    @Column(name = "FECHA_TRANSFERENCIA")
    private LocalDate vgd_FechaTransferencia;

    @Column(name = "FECHA_ESTIMADA_DESPACHO")
    private LocalDate vgd_FechaEstimadaDespacho;

    @Column(name = "FECHA_INGRESO_PAIS")
    private LocalDate vgd_FechaIngresoPais;

    @Column(name = "FECHA_DESALMACENAJE")
    private LocalDate vgd_FechaDesalmacenaje;

    @Column(name = "FACTURA", length = 100)
    private String vgc_Factura;

    @Column(name = "PARTIDA")
    private Short vgn_Partida;

    @Column(name = "PARTIDA_2")
    private Short vgn_Partida2;

    @Column(name = "MONTO_CANCELADO_CRC", precision = 15, scale = 2)
    private BigDecimal vgn_MontoCanceladoCRC;

    @Column(name = "SOBRANTE_FALTANTE_CRC", precision = 15, scale = 2)
    private BigDecimal vgn_SobranteFaltanteCRC;

    @Column(name = "RECLAMO", length = 2000)
    private String vgc_Reclamo;

    protected EntPedidoImportacion() {
    }

    public EntPedidoImportacion(Integer pvn_IdTramite, EntRegistroPedidoImportacion pvo_Registro) {
        this.vgn_IdTramite = pvn_IdTramite;
        this.vgn_CantidadSolicitada = pvo_Registro.cantidadSolicitada();
        this.vgd_FechaRecibidoExpediente = pvo_Registro.fechaRecibidoExpediente();
        this.vgc_UnidadSolicitante = pvo_Registro.unidadSolicitante().trim();
        this.vgc_Descripcion = pvo_Registro.descripcion().trim();
        this.vgc_NumeroContratacion = pvo_Registro.numeroContratacion().trim();
        this.vgc_NumeroPedido = pvo_Registro.numeroPedido().trim();
        this.vgc_CompaniaAdjudicada = pvo_Registro.companiaAdjudicada().trim();
        this.vgc_NumeroOrdenCompra = pvo_Registro.numeroOrdenCompra().trim();
        this.vgc_Incoterm = this.NormalizarOpcional(pvo_Registro.incoterm());
        this.vgn_PresupuestoOrdenCRC = pvo_Registro.presupuestoOrdenCRC();
        this.vgn_MontoOrden = pvo_Registro.montoOrden();
        this.vgc_MonedaOrden = pvo_Registro.monedaOrden().trim().toUpperCase();
        this.vgn_SaldoDisponible = pvo_Registro.saldoDisponible();
        this.vgd_FechaRecibidoSpOaf = pvo_Registro.fechaRecibidoSpOaf();
        this.vgd_FechaRecibidoCopiaChequeUi = pvo_Registro.fechaRecibidoCopiaChequeUi();
        this.vgd_FechaTransferencia = pvo_Registro.fechaTransferencia();
        this.vgd_FechaEstimadaDespacho = pvo_Registro.fechaEstimadaDespacho();
        this.vgd_FechaIngresoPais = pvo_Registro.fechaIngresoPais();
        this.vgd_FechaDesalmacenaje = pvo_Registro.fechaDesalmacenaje();
        this.vgc_Factura = this.NormalizarOpcional(pvo_Registro.factura());
        this.vgn_Partida = pvo_Registro.partida() == null ? null : pvo_Registro.partida().shortValue();
        this.vgn_Partida2 = pvo_Registro.partida2() == null ? null : pvo_Registro.partida2().shortValue();
        this.vgn_MontoCanceladoCRC = pvo_Registro.montoCanceladoCRC();
        this.vgn_SobranteFaltanteCRC = pvo_Registro.sobranteFaltanteCRC();
        this.vgc_Reclamo = this.NormalizarOpcional(pvo_Registro.reclamo());
    }

    public Integer getIdTramite() {
        return this.vgn_IdTramite;
    }

    public EntRegistroPedidoImportacion ObtenerRegistro(String pvc_Referencia, String pvc_Estado) {
        return new EntRegistroPedidoImportacion(
                pvc_Referencia,
                this.vgn_CantidadSolicitada,
                this.vgd_FechaRecibidoExpediente,
                this.vgc_UnidadSolicitante,
                this.vgc_Descripcion,
                this.vgc_NumeroContratacion,
                this.vgc_NumeroPedido,
                this.vgc_CompaniaAdjudicada,
                this.vgc_NumeroOrdenCompra,
                this.vgc_Incoterm,
                this.vgn_PresupuestoOrdenCRC,
                this.vgn_MontoOrden,
                this.vgc_MonedaOrden,
                this.vgn_SaldoDisponible,
                this.vgd_FechaRecibidoSpOaf,
                this.vgd_FechaRecibidoCopiaChequeUi,
                this.vgd_FechaTransferencia,
                this.vgd_FechaEstimadaDespacho,
                this.vgd_FechaIngresoPais,
                this.vgd_FechaDesalmacenaje,
                this.vgc_Factura,
                this.vgn_Partida2 == null ? 1 : 2,
                this.vgn_Partida == null ? null : this.vgn_Partida.intValue(),
                this.vgn_Partida2 == null ? null : this.vgn_Partida2.intValue(),
                this.vgn_MontoCanceladoCRC,
                this.vgn_SobranteFaltanteCRC,
                pvc_Estado,
                this.vgc_Reclamo);
    }

    private String NormalizarOpcional(String pvc_Valor) {
        return pvc_Valor == null || pvc_Valor.isBlank() ? null : pvc_Valor.trim();
    }
}
