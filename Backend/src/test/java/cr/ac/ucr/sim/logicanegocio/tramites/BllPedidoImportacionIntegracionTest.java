package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.entidadnegocio.tramites.EntDetallePedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntNotificacionPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaPedidoImportacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResumenPedidoImportacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hu006;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false"
})
class BllPedidoImportacionIntegracionTest {

    @Autowired
    private JdbcTemplate vgo_JdbcTemplate;

    @Autowired
    private BllPedidoImportacion vgo_BllPedidoImportacion;

    @BeforeEach
    void PrepararEsquema() {
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMM_OPCION_IMPORTACION");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMT_PEDIDO_PROV");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMH_ESTADO_TRAMITE");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMT_TRAMITE");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMM_ESTADO_TRAMITE");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMM_TIPO_TRAMITE");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMM_MONEDA");
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMM_TIPO_TRAMITE (
                    ID_TIPO_TRAMITE INT PRIMARY KEY,
                    CODIGO VARCHAR(15) NOT NULL,
                    NOMBRE VARCHAR(60) NOT NULL,
                    PREFIJO_REFERENCIA VARCHAR(10) NOT NULL,
                    REQUIERE_SICOP BOOLEAN NOT NULL,
                    REQUIERE_ORDEN_COMPRA BOOLEAN NOT NULL,
                    ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMM_ESTADO_TRAMITE (
                    ID_ESTADO_TRAMITE INT AUTO_INCREMENT PRIMARY KEY,
                    ID_TIPO_TRAMITE INT NOT NULL,
                    NOMBRE VARCHAR(50) NOT NULL,
                    ES_INICIAL BOOLEAN NOT NULL,
                    ES_FINAL BOOLEAN NOT NULL,
                    ORDEN SMALLINT NOT NULL,
                    ESTADO CHAR(3) NOT NULL,
                    CATEGORIA_ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMM_MONEDA (
                    ID_MONEDA INT PRIMARY KEY,
                    CODIGO_ISO CHAR(3) NOT NULL,
                    NOMBRE VARCHAR(50) NOT NULL,
                    SIMBOLO VARCHAR(5) NOT NULL,
                    ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMT_TRAMITE (
                    ID_TRAMITE INT AUTO_INCREMENT PRIMARY KEY,
                    REFERENCIA_INTERNA VARCHAR(45) NOT NULL UNIQUE,
                    ID_TIPO_TRAMITE INT NOT NULL,
                    ID_ESTADO_TRAMITE INT NOT NULL,
                    ID_USUARIO_RESPONSABLE INT NOT NULL,
                    ID_PAIS_ORIGEN INT,
                    ID_MONEDA INT,
                    VALOR_ESTIMADO DECIMAL(13,2),
                    DESCRIPCION VARCHAR(100),
                    REGIMEN_ADUANERO VARCHAR(60),
                    ADUANA_CONTROL VARCHAR(60),
                    TIPO_PARTIDO_RESPONSABLE VARCHAR(25),
                    PARTIDO_RESPONSABLE VARCHAR(120),
                    IMPUESTOS_PAGADOS DECIMAL(13,2),
                    IMPUESTOS_EXONERADOS DECIMAL(13,2),
                    MONTO_BODEGAJE DECIMAL(13,2),
                    OBSERVACIONES VARCHAR(4000),
                    FECHA_CREACION TIMESTAMP NOT NULL,
                    FECHA_CIERRE DATE
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMH_ESTADO_TRAMITE (
                    ID_HISTORIAL INT AUTO_INCREMENT PRIMARY KEY,
                    ID_TRAMITE INT NOT NULL,
                    ID_ESTADO_ANTERIOR INT,
                    ID_ESTADO_NUEVO INT NOT NULL,
                    ID_USUARIO INT NOT NULL,
                    JUSTIFICACION VARCHAR(255),
                    FECHA_HORA TIMESTAMP NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMM_OPCION_IMPORTACION (
                    ID_OPCION INT AUTO_INCREMENT PRIMARY KEY,
                    TIPO_OPCION VARCHAR(20) NOT NULL,
                    VALOR VARCHAR(160) NOT NULL,
                    ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMT_PEDIDO_PROV (
                    ID_TRAMITE INT PRIMARY KEY,
                    CANTIDAD_SOLICITADA DECIMAL(13,3),
                    FECHA_RECIBIDO_EXPEDIENTE DATE NOT NULL,
                    UNIDAD_SOLICITANTE VARCHAR(120) NOT NULL,
                    DESCRIPCION_SOLICITUD TEXT NOT NULL,
                    NUMERO_CONTRATACION VARCHAR(100) NOT NULL,
                    NUMERO_PEDIDO VARCHAR(20) NOT NULL,
                    COMPANIA_ADJUDICADA VARCHAR(160) NOT NULL,
                    NUMERO_ORDEN_COMPRA VARCHAR(20) NOT NULL,
                    INCOTERM VARCHAR(10),
                    PRESUPUESTO_ORDEN_CRC DECIMAL(15,2),
                    MONTO_ORDEN DECIMAL(18,2),
                    MONEDA_ORDEN VARCHAR(30) NOT NULL,
                    SALDO_DISPONIBLE DECIMAL(18,2),
                    FECHA_RECIBIDO_SP_OAF DATE,
                    FECHA_RECIBIDO_COPIA_CHEQUE_UI DATE,
                    FECHA_TRANSFERENCIA DATE,
                    FECHA_ESTIMADA_DESPACHO DATE,
                    FECHA_INGRESO_PAIS DATE,
                    FECHA_DESALMACENAJE DATE,
                    FACTURA VARCHAR(100),
                    PARTIDA SMALLINT,
                    PARTIDA_2 SMALLINT,
                    MONTO_CANCELADO_CRC DECIMAL(15,2),
                    SOBRANTE_FALTANTE_CRC DECIMAL(15,2),
                    RECLAMO VARCHAR(2000)
                )
                """);
        this.vgo_JdbcTemplate.update(
                "INSERT INTO SIMM_TIPO_TRAMITE VALUES (7, 'PEDIDO_PROV', 'Pedido a proveedor', 'PP-', TRUE, TRUE, 'ACT')");
        this.vgo_JdbcTemplate.update(
                "INSERT INTO SIMM_ESTADO_TRAMITE VALUES (13, 7, 'En trámite', FALSE, FALSE, 1, 'ACT', 'ACT')");
        this.vgo_JdbcTemplate.update(
                "INSERT INTO SIMM_MONEDA VALUES (1, 'CRC', 'Colón', '₡', 'ACT')");
    }

    @Test
    void FlujoCompleto_RegistraFiltraActualizaYNotificaLaSolicitud() {
        int ano = Year.now().getValue();
        String referencia = ano + "-001";
        EntRegistroPedidoImportacion registro =
                this.CrearRegistro(ano, referencia, null, "En trámite", null, "USD");

        EntRespuestaPedidoImportacion creada = this.vgo_BllPedidoImportacion.InsertarRegistro(
                registro,
                23,
                "Importador de prueba");
        assertEquals(referencia, creada.referenciaInterna());
        this.VerificarValorEstimadoCRC(referencia);

        List<EntResumenPedidoImportacion> lista = this.vgo_BllPedidoImportacion.ObtenerLista(23);
        assertEquals(1, lista.size());
        assertEquals("En trámite", lista.get(0).estado());
        assertEquals("Unidad A", lista.get(0).unidadSolicitante());

        EntDetallePedidoImportacion detalle = this.vgo_BllPedidoImportacion.ObtenerRegistro(
                referencia,
                23,
                "Importador de prueba");
        assertEquals("En trámite", detalle.estado());
        assertEquals(new BigDecimal("12.500"), detalle.solicitud().cantidadSolicitada());
        assertEquals(2, detalle.solicitud().cantidadPartidas());
        assertEquals(5, detalle.solicitud().partida());
        assertEquals(2, detalle.solicitud().partida2());

        List<EntNotificacionPedidoImportacion> notificaciones =
                this.vgo_BllPedidoImportacion.ObtenerNotificaciones(23);
        assertEquals(1, notificaciones.size());
        assertEquals(3, notificaciones.get(0).diasCalendarioPago());

        EntRegistroPedidoImportacion actualizado =
                this.CrearRegistro(ano, referencia, new BigDecimal("900"), "En aduana", null, "USD");
        EntRespuestaPedidoImportacion respuesta = this.vgo_BllPedidoImportacion.ModificarRegistro(
                referencia,
                actualizado,
                23,
                "Importador de prueba");

        assertEquals("En aduana", respuesta.estado());
        this.VerificarValorEstimadoCRC(referencia);
        assertEquals(0, this.vgo_BllPedidoImportacion.ObtenerNotificaciones(23).size());
        assertEquals(2, this.vgo_JdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM SIMH_ESTADO_TRAMITE",
                Integer.class));
    }

    @Test
    void InsertarRegistro_SinCantidadSolicitada_PermiteGuardarYConsultar() {
        int ano = Year.now().getValue();
        String referencia = ano + "-002";
        EntRegistroPedidoImportacion registro = this.CrearRegistro(
                null,
                ano,
                referencia,
                null,
                "En trámite",
                new BigDecimal("1200.00"),
                "CRC");

        this.vgo_BllPedidoImportacion.InsertarRegistro(registro, 23, "Importador de prueba");

        EntDetallePedidoImportacion detalle = this.vgo_BllPedidoImportacion.ObtenerRegistro(
                referencia,
                23,
                "Importador de prueba");
        assertNull(detalle.solicitud().cantidadSolicitada());
    }

    private EntRegistroPedidoImportacion CrearRegistro(
            int pvn_Ano,
            String pvc_Referencia,
            BigDecimal pvn_MontoCancelado,
            String pvc_Estado) {
        return this.CrearRegistro(
                pvn_Ano,
                pvc_Referencia,
                pvn_MontoCancelado,
                pvc_Estado,
                new BigDecimal("1200.00"),
                "CRC");
    }

    private EntRegistroPedidoImportacion CrearRegistro(
            int pvn_Ano,
            String pvc_Referencia,
            BigDecimal pvn_MontoCancelado,
            String pvc_Estado,
            BigDecimal pvn_MontoOrden,
            String pvc_MonedaOrden) {
        return this.CrearRegistro(
                new BigDecimal("12.500"),
                pvn_Ano,
                pvc_Referencia,
                pvn_MontoCancelado,
                pvc_Estado,
                pvn_MontoOrden,
                pvc_MonedaOrden);
    }

    private EntRegistroPedidoImportacion CrearRegistro(
            BigDecimal pvn_CantidadSolicitada,
            int pvn_Ano,
            String pvc_Referencia,
            BigDecimal pvn_MontoCancelado,
            String pvc_Estado,
            BigDecimal pvn_MontoOrden,
            String pvc_MonedaOrden) {
        return new EntRegistroPedidoImportacion(
                pvc_Referencia,
                pvn_CantidadSolicitada,
                LocalDate.now(),
                "Unidad A",
                "Descripción de prueba para el pedido.",
                pvn_Ano + "-CFT-000002 UADQ-INT",
                "001-" + pvn_Ano,
                "Compañía A",
                pvn_Ano + "-001",
                "FOB",
                new BigDecimal("50000000.00"),
                pvn_MontoOrden,
                pvc_MonedaOrden,
                null,
                null,
                null,
                LocalDate.now().minusDays(3),
                null,
                null,
                null,
                "F-001",
                2,
                5,
                2,
                pvn_MontoCancelado,
                null,
                pvc_Estado,
                null);
    }

    private void VerificarValorEstimadoCRC(String pvc_Referencia) {
        assertEquals(1, this.vgo_JdbcTemplate.queryForObject(
                "SELECT ID_MONEDA FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = ?",
                Integer.class,
                pvc_Referencia));
        BigDecimal vlo_ValorEstimado = this.vgo_JdbcTemplate.queryForObject(
                "SELECT VALOR_ESTIMADO FROM SIMT_TRAMITE WHERE REFERENCIA_INTERNA = ?",
                BigDecimal.class,
                pvc_Referencia);
        assertEquals(0, vlo_ValorEstimado.compareTo(new BigDecimal("50000000.00")));
    }
}
