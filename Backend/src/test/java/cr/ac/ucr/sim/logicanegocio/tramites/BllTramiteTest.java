package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDesalmacenajeEspecial;
import cr.ac.ucr.sim.accesodatos.tramites.DalDonacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalPedidoImportacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioReferencia;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCostosTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDesalmacenajeEspecial;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRespuestaCambioReferencia;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 23/09/2026
 * Descripción: Pruebas del cambio de referencia de un expediente, incluida la reclasificación
 *   hacia otro tipo de trámite.
 */
@ExtendWith(MockitoExtension.class)
class BllTramiteTest {

    private static final Integer ID_TRAMITE = 300;
    private static final Integer ID_USUARIO = 1;
    private static final Integer ID_TIPO_DESALMACENAJE = 1;
    private static final Integer ID_TIPO_DONACION = 2;
    private static final Integer ID_TIPO_EXONERACION = 5;
    private static final Integer ID_ESTADO_INICIAL_DESALMACENAJE = 10;
    private static final String JUSTIFICACION = "La mercancía no es una donación, corresponde a un desalmacenaje.";

    @Mock
    private DalTramite vgo_DalTramite;

    @Mock
    private DalTipoTramite vgo_DalTipoTramite;

    @Mock
    private DalEstadoTramite vgo_DalEstadoTramite;

    @Mock
    private DalHistorialEstado vgo_DalHistorialEstado;

    @Mock
    private DalDesalmacenajeEspecial vgo_DalDesalmacenajeEspecial;

    @Mock
    private DalExoneracion vgo_DalExoneracion;

    @Mock
    private DalDonacion vgo_DalDonacion;

    @Mock
    private DalPedidoImportacion vgo_DalPedidoImportacion;

    @Mock
    private DalUsuario vgo_DalUsuario;

    @Mock
    private BllBitacora vgo_BllBitacora;

    @InjectMocks
    private BllTramite vgo_BllTramite;

    @BeforeEach
    void PrepararPrueba() {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(ID_USUARIO);
        vlo_EnUsuario.setNombreCompleto("Analista de Importaciones");
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(vlo_EnUsuario);

        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("D-2026-0007"))
                .thenReturn(this.CrearTramite(ID_TIPO_DONACION));
        lenient().when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(ID_TIPO_DONACION))
                .thenReturn(this.CrearTipo(ID_TIPO_DONACION, Constantes.CODIGO_TIPO_TRAMITE_DONACION,
                        "Donación", "D-"));
        lenient().when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorReferencia("DE-2026-0100"))
                .thenReturn(this.CrearTipo(ID_TIPO_DESALMACENAJE, Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE,
                        "Desalmacenaje especial", "DE-"));
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(ID_TIPO_DESALMACENAJE))
                .thenReturn(this.CrearEstadoInicial());
    }

    // ---------------------------------------------------------------- Cambio dentro del mismo tipo

    @Test
    void CambiarReferencia_ConOtroConsecutivoDelMismoTipo_DebeRenumerarSinReclasificar() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("DE-2026-0099"))
                .thenReturn(this.CrearTramite(ID_TIPO_DESALMACENAJE));
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(ID_TIPO_DESALMACENAJE))
                .thenReturn(this.CrearTipo(ID_TIPO_DESALMACENAJE, Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE,
                        "Desalmacenaje especial", "DE-"));

        EntRespuestaCambioReferencia vlo_Respuesta = this.vgo_BllTramite.CambiarReferencia(
                "DE-2026-0099", this.CrearCambio("DE-2026-0100"));

        assertEquals("DE-2026-0100", vlo_Respuesta.getReferenciaInterna());
        assertEquals("DE-2026-0099", vlo_Respuesta.getReferenciaAnterior());
        assertFalse(vlo_Respuesta.isReclasificado());
        verify(this.vgo_DalDesalmacenajeEspecial, never()).SIMT_DESALMACENAJE_ESPEC_EliminarRegistro(anyInt());
        verify(this.vgo_DalHistorialEstado, never()).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
    }

    // ---------------------------------------------------------------- Reclasificación

    @Test
    void CambiarReferencia_DeUnaDonacionAUnDesalmacenaje_DebeReclasificarElExpediente() {
        EntCambioReferencia vlo_Cambio = this.CrearCambio("DE-2026-0100");
        vlo_Cambio.setOrigenDependencia("Escuela de Química");

        EntRespuestaCambioReferencia vlo_Respuesta =
                this.vgo_BllTramite.CambiarReferencia("D-2026-0007", vlo_Cambio);

        assertTrue(vlo_Respuesta.isReclasificado());
        assertEquals("Desalmacenaje especial", vlo_Respuesta.getTipoTramite());
        assertEquals("Donación", vlo_Respuesta.getTipoTramiteAnterior());
        assertEquals("Pendiente", vlo_Respuesta.getEstado());
        verify(this.vgo_DalDonacion).SIMT_DONACION_EliminarRegistro(ID_TRAMITE);
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void CambiarReferencia_AUnDesalmacenaje_DebeGuardarLaDependenciaYLaJustificacion() {
        EntCambioReferencia vlo_Cambio = this.CrearCambio("DE-2026-0100");
        vlo_Cambio.setOrigenDependencia("  Escuela de Química  ");

        this.vgo_BllTramite.CambiarReferencia("D-2026-0007", vlo_Cambio);

        ArgumentCaptor<EntDesalmacenajeEspecial> vlo_Captor =
                ArgumentCaptor.forClass(EntDesalmacenajeEspecial.class);
        verify(this.vgo_DalDesalmacenajeEspecial).SIMT_DESALMACENAJE_ESPEC_InsertarRegistro(vlo_Captor.capture());

        assertEquals("Escuela de Química", vlo_Captor.getValue().getOrigenDependencia());
        assertEquals(JUSTIFICACION, vlo_Captor.getValue().getJustificacion());
        assertEquals(ID_TRAMITE, vlo_Captor.getValue().getIdTramite());
    }

    @Test
    void CambiarReferencia_AUnDesalmacenajeSinDependencia_DebeLanzarExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", this.CrearCambio("DE-2026-0100")));

        assertEquals(Constantes.MENSAJE_DEPENDENCIA_DESTINO_REQUERIDA, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
    }

    @Test
    void CambiarReferencia_AUnaExoneracion_DebeGuardarLosDatosContractuales() {
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorReferencia("EXO-2026-0300"))
                .thenReturn(this.CrearTipo(ID_TIPO_EXONERACION, Constantes.CODIGO_TIPO_TRAMITE_EXONERACION,
                        "Solicitud de exoneración", "EXO-"));
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerEstadoInicial(ID_TIPO_EXONERACION))
                .thenReturn(this.CrearEstadoInicial());

        EntCambioReferencia vlo_Cambio = this.CrearCambio("EXO-2026-0300");
        vlo_Cambio.setTipoExoneracion(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL);
        vlo_Cambio.setNumeroContratacion("2026LA-000123");
        vlo_Cambio.setContratoSicop("0432026000100045");

        this.vgo_BllTramite.CambiarReferencia("D-2026-0007", vlo_Cambio);

        ArgumentCaptor<EntExoneracion> vlo_Captor = ArgumentCaptor.forClass(EntExoneracion.class);
        verify(this.vgo_DalExoneracion).SIMT_EXONERACION_InsertarRegistro(vlo_Captor.capture());

        assertEquals(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL, vlo_Captor.getValue().getTipoExoneracion());
        assertEquals("2026LA-000123", vlo_Captor.getValue().getNumeroContratacion());
        assertEquals("0432026000100045", vlo_Captor.getValue().getContratoSicop());
    }

    @Test
    void CambiarReferencia_AUnTipoQueElSistemaNoReclasifica_DebeLanzarExcepcion() {
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorReferencia("PP-2026-0001"))
                .thenReturn(this.CrearTipo(9, "PEDIDO_PROV", "Pedido a proveedor", "PP-"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", this.CrearCambio("PP-2026-0001")));

        assertEquals(Constantes.MENSAJE_TIPO_DESTINO_NO_SOPORTADO, vlo_Excepcion.getMessage());
    }

    // ---------------------------------------------------------------- Validaciones

    @Test
    void CambiarReferencia_ConUnaSiglaQueNoEstaEnElCatalogo_DebeLanzarExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", this.CrearCambio("ZZ-2026-0001")));

        assertEquals(Constantes.MENSAJE_SIGLA_NO_RECONOCIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void CambiarReferencia_ConLaMismaReferencia_DebeLanzarExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", this.CrearCambio("D-2026-0007")));

        assertEquals(Constantes.MENSAJE_REFERENCIA_SIN_CAMBIO, vlo_Excepcion.getMessage());
    }

    @Test
    void CambiarReferencia_ConUnaReferenciaYaUsada_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ExistePorReferenciaInterna("DE-2026-0100")).thenReturn(true);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", this.CrearCambio("DE-2026-0100")));

        assertEquals(Constantes.MENSAJE_REFERENCIA_DUPLICADA, vlo_Excepcion.getMessage());
    }

    @Test
    void CambiarReferencia_ConUnaJustificacionMuyCorta_DebeLanzarExcepcion() {
        EntCambioReferencia vlo_Cambio = this.CrearCambio("DE-2026-0100");
        vlo_Cambio.setJustificacion("error");

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", vlo_Cambio));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void CambiarReferencia_ConSoloLaSigla_DebeLanzarExcepcion() {
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorReferencia("DE-"))
                .thenReturn(this.CrearTipo(ID_TIPO_DESALMACENAJE, Constantes.CODIGO_TIPO_TRAMITE_DESALMACENAJE,
                        "Desalmacenaje especial", "DE-"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("D-2026-0007", this.CrearCambio("DE-")));

        assertEquals(Constantes.MENSAJE_REFERENCIA_NUEVA_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void CambiarReferencia_DeUnExpedienteQueNoExiste_DebeLanzarExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.CambiarReferencia("DE-2026-9999", this.CrearCambio("DE-2026-0100")));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_NO_ENCONTRADO, vlo_Excepcion.getMessage());
    }

    // ---------------------------------------------------------------- Bitácora

    @Test
    void CambiarReferencia_ConDatosValidos_DebeAnotarElCambioEnLaBitacora() {
        EntCambioReferencia vlo_Cambio = this.CrearCambio("DE-2026-0100");
        vlo_Cambio.setOrigenDependencia("Escuela de Química");

        this.vgo_BllTramite.CambiarReferencia("D-2026-0007", vlo_Cambio);

        ArgumentCaptor<String> vlo_Anteriores = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> vlo_Nuevos = ArgumentCaptor.forClass(String.class);
        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_REFERENCIA_MODIFICADA),
                eq(Constantes.ENTIDAD_BITACORA_TRAMITE), eq(ID_TRAMITE),
                vlo_Anteriores.capture(), vlo_Nuevos.capture());

        assertTrue(vlo_Anteriores.getValue().contains("D-2026-0007"));
        assertTrue(vlo_Anteriores.getValue().contains("Donación"));
        assertTrue(vlo_Nuevos.getValue().contains("DE-2026-0100"));
        assertTrue(vlo_Nuevos.getValue().contains(JUSTIFICACION));
    }

    // ---------------------------------------------------------------- Datos de costo (HU-036)

    @Test
    void RegistrarCostos_ConLosMontosDelTramite_DebeGuardarlosYAnotarlosEnLaBitacora() {
        EntTramite vlo_EnTramite = this.CrearTramite(ID_TIPO_DESALMACENAJE);
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("DE-2026-0099"))
                .thenReturn(vlo_EnTramite);

        EntCostosTramite vlo_Costos = this.CrearCostos();
        EntTramite vlo_Resultado = this.vgo_BllTramite.RegistrarCostos("DE-2026-0099", vlo_Costos);

        assertEquals("Unidad de Importaciones", vlo_Resultado.getPartidoResponsable());
        assertEquals(new BigDecimal("9000.00"), vlo_Resultado.getImpuestosPagados());
        assertEquals(new BigDecimal("25000.00"), vlo_Resultado.getMontoBodegaje());
        verify(this.vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(vlo_EnTramite);
        verify(this.vgo_BllBitacora).RegistrarAccion(eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_COSTOS_MODIFICADOS),
                eq(Constantes.ENTIDAD_BITACORA_TRAMITE), eq(ID_TRAMITE), any(), any());
    }

    @Test
    void RegistrarCostos_ConUnMontoNegativo_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("DE-2026-0099"))
                .thenReturn(this.CrearTramite(ID_TIPO_DESALMACENAJE));
        EntCostosTramite vlo_Costos = this.CrearCostos();
        vlo_Costos.setMontoBodegaje(new BigDecimal("-1.00"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.RegistrarCostos("DE-2026-0099", vlo_Costos));

        assertEquals(Constantes.MENSAJE_MONTO_NO_PUEDE_SER_NEGATIVO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
    }

    @Test
    void RegistrarCostos_SinNingunCambio_DebeLanzarExcepcion() {
        EntTramite vlo_EnTramite = this.CrearTramite(ID_TIPO_DESALMACENAJE);
        vlo_EnTramite.setPartidoResponsable("Unidad de Importaciones");
        vlo_EnTramite.setImpuestosPagados(new BigDecimal("9000.00"));
        vlo_EnTramite.setImpuestosExonerados(new BigDecimal("1500.00"));
        vlo_EnTramite.setMontoBodegaje(new BigDecimal("25000.00"));
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("DE-2026-0099"))
                .thenReturn(vlo_EnTramite);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.RegistrarCostos("DE-2026-0099", this.CrearCostos()));

        assertEquals(Constantes.MENSAJE_COSTOS_SIN_CAMBIO, vlo_Excepcion.getMessage());
    }

    @Test
    void RegistrarCostos_ConUnPartidoDeMasDe120Caracteres_DebeLanzarExcepcion() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("DE-2026-0099"))
                .thenReturn(this.CrearTramite(ID_TIPO_DESALMACENAJE));
        EntCostosTramite vlo_Costos = this.CrearCostos();
        vlo_Costos.setTipoPartidoResponsable(Constantes.TIPO_PARTIDO_SOLICITANTE);
        vlo_Costos.setPartidoResponsable("a".repeat(121));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllTramite.RegistrarCostos("DE-2026-0099", vlo_Costos));

        assertEquals(Constantes.MENSAJE_PARTIDO_RESPONSABLE_INVALIDO, vlo_Excepcion.getMessage());
    }

    private EntCostosTramite CrearCostos() {
        EntCostosTramite vlo_Costos = new EntCostosTramite();
        vlo_Costos.setTipoPartidoResponsable(Constantes.TIPO_PARTIDO_UNIDAD_IMPORTACIONES);
        vlo_Costos.setImpuestosPagados(new BigDecimal("9000.00"));
        vlo_Costos.setImpuestosExonerados(new BigDecimal("1500.00"));
        vlo_Costos.setMontoBodegaje(new BigDecimal("25000.00"));
        vlo_Costos.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Costos;
    }

    // ---------------------------------------------------------------- Datos de apoyo

    private EntCambioReferencia CrearCambio(String pvc_ReferenciaNueva) {
        EntCambioReferencia vlo_Cambio = new EntCambioReferencia();
        vlo_Cambio.setReferenciaInterna(pvc_ReferenciaNueva);
        vlo_Cambio.setJustificacion(JUSTIFICACION);
        vlo_Cambio.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Cambio;
    }

    private EntTramite CrearTramite(Integer pvn_IdTipoTramite) {
        EntTramite vlo_EnTramite = new EntTramite();
        vlo_EnTramite.setIdTramite(ID_TRAMITE);
        vlo_EnTramite.setIdTipoTramite(pvn_IdTipoTramite);
        vlo_EnTramite.setIdEstadoTramite(99);
        vlo_EnTramite.setIdUsuarioResponsable(ID_USUARIO);
        vlo_EnTramite.setReferenciaInterna(
                pvn_IdTipoTramite.equals(ID_TIPO_DONACION) ? "D-2026-0007" : "DE-2026-0099");
        return vlo_EnTramite;
    }

    private EntTipoTramite CrearTipo(Integer pvn_IdTipoTramite, String pvc_Codigo, String pvc_Nombre,
            String pvc_Prefijo) {
        EntTipoTramite vlo_EnTipo = new EntTipoTramite();
        vlo_EnTipo.setIdTipoTramite(pvn_IdTipoTramite);
        vlo_EnTipo.setCodigo(pvc_Codigo);
        vlo_EnTipo.setNombre(pvc_Nombre);
        vlo_EnTipo.setPrefijoReferencia(pvc_Prefijo);
        return vlo_EnTipo;
    }

    private EntEstadoTramite CrearEstadoInicial() {
        EntEstadoTramite vlo_EnEstado = new EntEstadoTramite();
        vlo_EnEstado.setIdEstadoTramite(ID_ESTADO_INICIAL_DESALMACENAJE);
        vlo_EnEstado.setNombre("Pendiente");
        return vlo_EnEstado;
    }
}
