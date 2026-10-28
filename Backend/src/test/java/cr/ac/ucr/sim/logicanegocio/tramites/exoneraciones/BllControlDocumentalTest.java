package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBitacora;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloControlDocumentalRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloRevisionDocumentoRespuesta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Pruebas de las reglas del control de documentos obligatorios (HU-021).
 */
@ExtendWith(MockitoExtension.class)
class BllControlDocumentalTest {

    private static final Integer ID_TRAMITE = 10;
    private static final Integer ID_TIPO_TRAMITE = 5;
    private static final Integer ID_USUARIO = 1;
    private static final Integer ID_ESTADO_REVISION = 50;
    private static final Integer ID_ESTADO_APROBACION = 51;

    @Mock
    private BllRevisionDocumento vgo_BllRevisionDocumento;

    @Mock
    private DalDocumento vgo_DalDocumento;

    @Mock
    private DalTramite vgo_DalTramite;

    @Mock
    private DalExoneracion vgo_DalExoneracion;

    @Mock
    private DalEstadoTramite vgo_DalEstadoTramite;

    @Mock
    private DalHistorialEstado vgo_DalHistorialEstado;

    @Mock
    private DalUsuario vgo_DalUsuario;

    @Mock
    private BllBitacora vgo_BllBitacora;

    @InjectMocks
    private BllControlDocumental vgo_BllControlDocumental;

    private EntTramite vgo_EnTramite;
    private EntEstadoTramite vgo_EnEstadoRevision;
    private EntEstadoTramite vgo_EnEstadoAprobacion;
    private List<ModeloRevisionDocumentoRespuesta> vgo_LsChecklist;
    private List<EntDocumento> vgo_LsDocumentos;

    @BeforeEach
    void Preparar() {
        this.vgo_EnTramite = new EntTramite();
        this.vgo_EnTramite.setIdTramite(ID_TRAMITE);
        this.vgo_EnTramite.setIdTipoTramite(ID_TIPO_TRAMITE);
        this.vgo_EnTramite.setIdEstadoTramite(ID_ESTADO_REVISION);
        this.vgo_EnTramite.setReferenciaInterna("EXO-2026-0087");

        EntExoneracion vlo_EnExoneracion = new EntExoneracion();
        vlo_EnExoneracion.setIdTramite(ID_TRAMITE);
        vlo_EnExoneracion.setTipoExoneracion(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL);

        this.vgo_EnEstadoRevision = this.CrearEstado(ID_ESTADO_REVISION, "Pendiente revisión documental", false);
        this.vgo_EnEstadoAprobacion = this.CrearEstado(
                ID_ESTADO_APROBACION, Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION, false);

        this.vgo_LsChecklist = new ArrayList<>();
        this.vgo_LsDocumentos = new ArrayList<>();

        lenient().when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(ID_TRAMITE)).thenReturn(this.vgo_EnTramite);
        lenient().when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE))
                .thenReturn(vlo_EnExoneracion);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(ID_ESTADO_REVISION))
                .thenReturn(this.vgo_EnEstadoRevision);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(ID_ESTADO_APROBACION))
                .thenReturn(this.vgo_EnEstadoAprobacion);
        lenient().when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                        ID_TIPO_TRAMITE,
                        Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION,
                        Constantes.ESTADO_CATALOGO_ACTIVO))
                .thenReturn(this.vgo_EnEstadoAprobacion);
        lenient().when(this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(ID_TRAMITE))
                .thenReturn(this.vgo_LsChecklist);
        lenient().when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerActivosPorTramite(ID_TRAMITE))
                .thenReturn(this.vgo_LsDocumentos);
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(new EntUsuario());
    }

    @Test
    void ObtenerControlDocumental_ConFaltantes_ListaPendientesYNoConfirmaCumplimiento() {
        this.AgregarRequisito(1, "Factura o documento equivalente", true, 100, "RECIBIDO");
        this.AgregarRequisito(2, "Documento de embarque", true, 101, "RECIBIDO");
        this.AgregarRequisito(3, "Formulario S1", true, null, "PENDIENTE");
        this.AgregarRequisito(4, "Lista de empaque", true, 103, "RECHAZADO");
        this.AgregarRequisito(5, "EXONET", false, null, "PENDIENTE");

        ModeloControlDocumentalRespuesta vlo_Control =
                this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE);

        assertFalse(vlo_Control.getCumpleDocumentacion());
        assertFalse(vlo_Control.getPuedeContinuarAprobacion());
        assertEquals(Constantes.ESTADO_DOCUMENTAL_EXONERACION_INCOMPLETO, vlo_Control.getEstadoDocumental());
        assertEquals(4, vlo_Control.getTotalObligatorios());
        assertEquals(2, vlo_Control.getObligatoriosCumplidos());
        assertEquals(2, vlo_Control.getObligatoriosPendientes());
        assertEquals(50, vlo_Control.getPorcentajeCumplimiento());
        assertEquals(List.of(
                        "Formulario S1 (" + Constantes.MOTIVO_CONTROL_FALTANTE + ")",
                        "Lista de empaque (" + Constantes.MOTIVO_CONTROL_RECHAZADO + ")"),
                vlo_Control.getDocumentosPendientes());
        assertEquals(Constantes.ESTADO_CONTROL_FALTANTE, vlo_Control.getRequisitos().get(2).getEstadoControl());
        assertEquals(Constantes.ESTADO_CONTROL_RECHAZADO, vlo_Control.getRequisitos().get(3).getEstadoControl());
    }

    @Test
    void ObtenerControlDocumental_DocumentoAdjuntoSinRevisar_NoCuentaComoCumplido() {
        this.AgregarRequisito(1, "Factura o documento equivalente", true, 100, "PENDIENTE");

        ModeloControlDocumentalRespuesta vlo_Control =
                this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE);

        assertFalse(vlo_Control.getCumpleDocumentacion());
        assertEquals(Constantes.ESTADO_CONTROL_POR_REVISAR, vlo_Control.getRequisitos().get(0).getEstadoControl());
        assertEquals(List.of("Factura o documento equivalente (" + Constantes.MOTIVO_CONTROL_POR_REVISAR + ")"),
                vlo_Control.getDocumentosPendientes());
    }

    @Test
    void ObtenerControlDocumental_ObligatoriosRecibidos_ConfirmaCumplimientoAunqueFalteOpcional() {
        this.AgregarObligatoriosRecibidos();
        this.AgregarRequisito(5, "EXONET", false, null, "PENDIENTE");

        ModeloControlDocumentalRespuesta vlo_Control =
                this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE);

        assertTrue(vlo_Control.getCumpleDocumentacion());
        assertTrue(vlo_Control.getPuedeContinuarAprobacion());
        assertEquals(Constantes.ESTADO_DOCUMENTAL_EXONERACION_COMPLETO, vlo_Control.getEstadoDocumental());
        assertEquals(100, vlo_Control.getPorcentajeCumplimiento());
        assertTrue(vlo_Control.getDocumentosPendientes().isEmpty());
        assertEquals(LocalDateTime.of(2026, 9, 2, 10, 0), vlo_Control.getRequisitos().get(0).getFechaCarga());
    }

    @Test
    void ObtenerControlDocumental_SinRequisitosConfigurados_NoConfirmaCumplimiento() {
        ModeloControlDocumentalRespuesta vlo_Control =
                this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE);

        assertFalse(vlo_Control.getCumpleDocumentacion());
        assertEquals(0, vlo_Control.getPorcentajeCumplimiento());
    }

    @Test
    void ObtenerControlDocumental_TramiteQueNoEsExoneracion_Rechaza() {
        when(this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(ID_TRAMITE)).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE));

        assertEquals(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }

    @Test
    void ContinuarAprobacion_DocumentacionIncompleta_RechazaSinCambiarEstado() {
        this.AgregarRequisito(1, "Factura o documento equivalente", true, 100, "RECIBIDO");
        this.AgregarRequisito(3, "Formulario S1", true, null, "PENDIENTE");

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO));

        assertTrue(vlo_Excepcion.getMessage().startsWith(Constantes.MENSAJE_DOCUMENTACION_OBLIGATORIA_INCOMPLETA));
        assertTrue(vlo_Excepcion.getMessage().contains("Formulario S1"));
        assertEquals(ID_ESTADO_REVISION, this.vgo_EnTramite.getIdEstadoTramite());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
        verify(this.vgo_DalHistorialEstado, never()).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
        verify(this.vgo_BllBitacora, never())
                .RegistrarAccion(anyInt(), anyInt(), anyString(), anyString(), anyInt(), any(), any());
    }

    @Test
    void ContinuarAprobacion_SinRequisitosConfigurados_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_SIN_REQUISITOS_DOCUMENTALES, vlo_Excepcion.getMessage());
    }

    @Test
    void ContinuarAprobacion_DocumentacionCompleta_PasaAPendienteDeAprobacionYRegistraHistorial() {
        this.AgregarObligatoriosRecibidos();

        ModeloControlDocumentalRespuesta vlo_Control =
                this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO);

        assertEquals(ID_ESTADO_APROBACION, this.vgo_EnTramite.getIdEstadoTramite());
        verify(this.vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(this.vgo_EnTramite);

        ArgumentCaptor<EntHistorialEstado> vlo_Historial = ArgumentCaptor.forClass(EntHistorialEstado.class);
        verify(this.vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_Historial.capture());
        assertEquals(ID_ESTADO_REVISION, vlo_Historial.getValue().getIdEstadoAnterior());
        assertEquals(ID_ESTADO_APROBACION, vlo_Historial.getValue().getIdEstadoNuevo());
        assertEquals(ID_USUARIO, vlo_Historial.getValue().getIdUsuario());
        assertNull(vlo_Historial.getValue().getFechaHora()); // la asigna @PrePersist al guardarse

        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO), eq(Constantes.ACCION_BITACORA_ESTADO_DOCUMENTAL),
                eq(Constantes.ENTIDAD_BITACORA_EXONERACION), eq(ID_TRAMITE), any(), anyString());
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO), eq(Constantes.ACCION_BITACORA_CAMBIO_ESTADO),
                eq(Constantes.ENTIDAD_BITACORA_TRAMITE), eq(ID_TRAMITE), anyString(), anyString());

        assertEquals(Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION, vlo_Control.getEstadoExpediente());
        assertTrue(vlo_Control.getCumpleDocumentacion());
        assertFalse(vlo_Control.getPuedeContinuarAprobacion());
    }

    @Test
    void ContinuarAprobacion_ExpedienteYaEnAprobacion_Rechaza() {
        this.AgregarObligatoriosRecibidos();
        this.vgo_EnTramite.setIdEstadoTramite(ID_ESTADO_APROBACION);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_EXONERACION_YA_EN_APROBACION, vlo_Excepcion.getMessage());
    }

    @Test
    void ContinuarAprobacion_ExpedienteCerrado_Rechaza() {
        this.AgregarObligatoriosRecibidos();
        this.vgo_EnEstadoRevision.setEsFinal(true);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_EXONERACION_CERRADA, vlo_Excepcion.getMessage());
    }

    @Test
    void ContinuarAprobacion_ExoneracionYaAprobada_NoPermiteReenviar() {
        this.AgregarObligatoriosRecibidos();
        EntEstadoTramite vlo_EnAprobada =
                this.CrearEstado(60, Constantes.NOMBRE_ESTADO_EXONERACION_APROBADA, false);
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(60)).thenReturn(vlo_EnAprobada);
        this.vgo_EnTramite.setIdEstadoTramite(60);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_EXONERACION_NO_PERMITE_ENVIO_APROBACION, vlo_Excepcion.getMessage());
        assertFalse(this.vgo_BllControlDocumental.ObtenerControlDocumental(ID_TRAMITE).getPuedeContinuarAprobacion());
    }

    @Test
    void ContinuarAprobacion_DesdePendienteDeModificacion_Permite() {
        this.AgregarObligatoriosRecibidos();
        EntEstadoTramite vlo_EnModificacion =
                this.CrearEstado(61, Constantes.NOMBRE_ESTADO_PENDIENTE_MODIFICACION_EXONERACION, false);
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(61)).thenReturn(vlo_EnModificacion);
        this.vgo_EnTramite.setIdEstadoTramite(61);

        this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO);

        assertEquals(ID_ESTADO_APROBACION, this.vgo_EnTramite.getIdEstadoTramite());
    }

    @Test
    void ContinuarAprobacion_EstadoNoConfigurado_Rechaza() {
        this.AgregarObligatoriosRecibidos();
        when(this.vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                ID_TIPO_TRAMITE,
                Constantes.NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION,
                Constantes.ESTADO_CATALOGO_ACTIVO)).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_ESTADO_PENDIENTE_APROBACION_NO_CONFIGURADO, vlo_Excepcion.getMessage());
    }

    @Test
    void ContinuarAprobacion_SinUsuario_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllControlDocumental.ContinuarAprobacion(ID_TRAMITE, null));

        assertEquals(Constantes.MENSAJE_USUARIO_CONTROL_DOCUMENTAL_INVALIDO, vlo_Excepcion.getMessage());
    }

    private EntEstadoTramite CrearEstado(Integer pvn_IdEstado, String pvc_Nombre, boolean pvb_EsFinal) {
        EntEstadoTramite vlo_EnEstado = new EntEstadoTramite();
        vlo_EnEstado.setIdEstadoTramite(pvn_IdEstado);
        vlo_EnEstado.setIdTipoTramite(ID_TIPO_TRAMITE);
        vlo_EnEstado.setNombre(pvc_Nombre);
        vlo_EnEstado.setEsFinal(pvb_EsFinal);
        return vlo_EnEstado;
    }

    private void AgregarObligatoriosRecibidos() {
        this.AgregarRequisito(1, "Factura o documento equivalente", true, 100, "RECIBIDO");
        this.AgregarRequisito(2, "Documento de embarque", true, 101, "RECIBIDO");
        this.AgregarRequisito(3, "Formulario S1", true, 102, "RECIBIDO");
        this.AgregarRequisito(4, "Lista de empaque", true, 103, "RECIBIDO");
    }

    private void AgregarRequisito(
            Integer pvn_IdTipoDocumento,
            String pvc_Nombre,
            boolean pvb_Obligatorio,
            Integer pvn_IdDocumento,
            String pvc_EstadoRevision) {
        ModeloRevisionDocumentoRespuesta vlo_Elemento = new ModeloRevisionDocumentoRespuesta();
        vlo_Elemento.setIdTipoDocumento(pvn_IdTipoDocumento);
        vlo_Elemento.setNombreTipoDocumento(pvc_Nombre);
        vlo_Elemento.setObligatorio(pvb_Obligatorio);
        vlo_Elemento.setIdDocumento(pvn_IdDocumento);
        vlo_Elemento.setEstadoRevision(pvc_EstadoRevision);
        this.vgo_LsChecklist.add(vlo_Elemento);

        if (pvn_IdDocumento != null) {
            vlo_Elemento.setNombreArchivo("documento_" + pvn_IdDocumento + ".pdf");
            EntDocumento vlo_EnDocumento = new EntDocumento();
            vlo_EnDocumento.setIdDocumento(pvn_IdDocumento);
            vlo_EnDocumento.setIdTramite(ID_TRAMITE);
            vlo_EnDocumento.setFechaCarga(LocalDateTime.of(2026, 9, 2, 10, 0));
            this.vgo_LsDocumentos.add(vlo_EnDocumento);
        }
    }
}
