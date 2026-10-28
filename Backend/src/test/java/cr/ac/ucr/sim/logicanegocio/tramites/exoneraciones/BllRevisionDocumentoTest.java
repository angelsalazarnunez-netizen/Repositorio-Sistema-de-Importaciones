package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.tramites.DalBitacora;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalRequisitoDocumental;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalRevisionDocumento;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRequisitoDocumental;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRevisionDocumento;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloHistorialRevisionRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloRevisionDocumentoRespuesta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 09/10/2026
 * Descripción: Pruebas de las reglas de la revisión documental de los expedientes de exoneración
 *   (HU-019): checklist según la matriz de requisitos, registro de revisiones que conservan el
 *   historial, validaciones y anotación en la bitácora.
 */
@ExtendWith(MockitoExtension.class)
class BllRevisionDocumentoTest {

    private static final Integer ID_TRAMITE = 10;
    private static final Integer ID_OTRO_TRAMITE = 99;
    private static final Integer ID_TIPO_TRAMITE = 5;
    private static final Integer ID_DOCUMENTO = 100;
    private static final Integer ID_DOCUMENTO_EMBARQUE = 101;
    private static final Long ID_REVISOR = 1L;
    private static final Integer ID_REVISION_NUEVA = 80;
    private static final LocalDateTime FECHA_REVISION = LocalDateTime.of(2026, 10, 2, 14, 0);

    @Mock private DalRevisionDocumento vgo_DalRevisionDocumento;
    @Mock private DalRequisitoDocumental vgo_DalRequisitoDocumental;
    @Mock private DalDocumento vgo_DalDocumento;
    @Mock private DalBitacora vgo_DalBitacora;
    @Mock private EntityManager vgo_EntityManager;
    @Mock private TypedQuery<Integer> vgo_ConsultaTipoTramite;

    private BllRevisionDocumento vgo_BllRevisionDocumento;
    private EntDocumento vgo_EnDocumento;
    private EntUsuario vgo_EnRevisor;

    @BeforeEach
    void Preparar() {
        this.vgo_BllRevisionDocumento = new BllRevisionDocumento(
                this.vgo_DalRevisionDocumento, this.vgo_DalRequisitoDocumental,
                this.vgo_DalDocumento, this.vgo_DalBitacora);
        ReflectionTestUtils.setField(this.vgo_BllRevisionDocumento, "vgo_EntityManager", this.vgo_EntityManager);

        EntExoneracion vlo_EnExoneracion = new EntExoneracion();
        vlo_EnExoneracion.setIdTramite(ID_TRAMITE);
        vlo_EnExoneracion.setTipoExoneracion(Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL);
        lenient().when(this.vgo_EntityManager.find(EntExoneracion.class, ID_TRAMITE)).thenReturn(vlo_EnExoneracion);

        this.vgo_EnDocumento = this.Documento(ID_DOCUMENTO, 1, "factura.pdf");
        lenient().when(this.vgo_EntityManager.find(EntDocumento.class, ID_DOCUMENTO)).thenReturn(this.vgo_EnDocumento);

        this.vgo_EnRevisor = new EntUsuario();
        this.vgo_EnRevisor.setIdUsuario(1);
        this.vgo_EnRevisor.setNombreCompleto("K. Valverde Fallas");
        lenient().when(this.vgo_EntityManager.find(EntUsuario.class, ID_REVISOR)).thenReturn(this.vgo_EnRevisor);

        lenient().when(this.vgo_EntityManager.createQuery(anyString(), eq(Integer.class)))
                .thenReturn(this.vgo_ConsultaTipoTramite);
        lenient().when(this.vgo_ConsultaTipoTramite.getResultList()).thenReturn(List.of(ID_TIPO_TRAMITE));

        lenient().when(this.vgo_DalRevisionDocumento.ObtenerRevisionActivaPorDocumento(any()))
                .thenReturn(Optional.empty());

        lenient().when(this.vgo_DalRevisionDocumento.save(any(EntRevisionDocumento.class))).thenAnswer(vlo_Invocacion -> {
            EntRevisionDocumento vlo_EnRevision = vlo_Invocacion.getArgument(0);
            if (vlo_EnRevision.getIdRevisionDocumento() == null) {
                vlo_EnRevision.setIdRevisionDocumento(ID_REVISION_NUEVA);
            }
            return vlo_EnRevision;
        });
    }


    @Test
    void ObtenerChecklistRevision_ArmaUnElementoPorRequisitoConSuEstadoActual() {
        EntDocumento vlo_EnEmbarque = this.Documento(ID_DOCUMENTO_EMBARQUE, 2, "embarque.pdf");
        // Los requisitos se arman antes del when(): Requisito() también usa when() y anidarlos
        // deja el stubbing sin terminar (UnfinishedStubbingException).
        List<EntRequisitoDocumental> vlo_LsRequisitos = List.of(
                this.Requisito(1, "FACT", "Factura o documento equivalente", true),
                this.Requisito(2, "EMB", "Documento de embarque", true),
                this.Requisito(3, "EXONET", "EXONET", false));
        when(this.vgo_DalRequisitoDocumental.ObtenerRequisitosAplicables(
                ID_TIPO_TRAMITE, Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL))
                .thenReturn(vlo_LsRequisitos);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerActivosPorTramite(ID_TRAMITE))
                .thenReturn(List.of(this.vgo_EnDocumento, vlo_EnEmbarque));
        lenient().when(this.vgo_DalRevisionDocumento.ObtenerRevisionActivaPorDocumento(this.vgo_EnDocumento))
                .thenReturn(Optional.of(this.Revision(70, "RECIBIDO", "Conforme", "ACT")));

        List<ModeloRevisionDocumentoRespuesta> vlo_LsChecklist =
                this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(ID_TRAMITE);

        assertEquals(3, vlo_LsChecklist.size());

        ModeloRevisionDocumentoRespuesta vlo_Revisado = vlo_LsChecklist.get(0);
        assertEquals(1, vlo_Revisado.getIdTipoDocumento());
        assertEquals("FACT", vlo_Revisado.getCodigoTipoDocumento());
        assertEquals("Factura o documento equivalente", vlo_Revisado.getNombreTipoDocumento());
        assertTrue(vlo_Revisado.getObligatorio());
        assertEquals(ID_DOCUMENTO, vlo_Revisado.getIdDocumento());
        assertEquals("factura.pdf", vlo_Revisado.getNombreArchivo());
        assertEquals("RECIBIDO", vlo_Revisado.getEstadoRevision());
        assertEquals(70, vlo_Revisado.getIdRevisionDocumento());
        assertEquals("Conforme", vlo_Revisado.getObservacion());
        assertEquals(FECHA_REVISION, vlo_Revisado.getFechaRevision());

        ModeloRevisionDocumentoRespuesta vlo_SinRevisar = vlo_LsChecklist.get(1);
        assertEquals(ID_DOCUMENTO_EMBARQUE, vlo_SinRevisar.getIdDocumento());
        assertEquals("PENDIENTE", vlo_SinRevisar.getEstadoRevision());
        assertNull(vlo_SinRevisar.getIdRevisionDocumento());

        ModeloRevisionDocumentoRespuesta vlo_SinAdjuntar = vlo_LsChecklist.get(2);
        assertFalse(vlo_SinAdjuntar.getObligatorio());
        assertNull(vlo_SinAdjuntar.getIdDocumento());
        assertNull(vlo_SinAdjuntar.getNombreArchivo());
        assertEquals("PENDIENTE", vlo_SinAdjuntar.getEstadoRevision());
    }

    @Test
    void ObtenerChecklistRevision_SinRequisitosConfigurados_DevuelveListaVacia() {
        when(this.vgo_DalRequisitoDocumental.ObtenerRequisitosAplicables(
                ID_TIPO_TRAMITE, Constantes.TIPO_EXONERACION_COMPRA_INSTITUCIONAL)).thenReturn(List.of());
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerActivosPorTramite(ID_TRAMITE))
                .thenReturn(List.of(this.vgo_EnDocumento));

        assertTrue(this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(ID_TRAMITE).isEmpty());
    }

    @Test
    void ObtenerChecklistRevision_TipoTramiteNoConfigurado_Rechaza() {
        when(this.vgo_ConsultaTipoTramite.getResultList()).thenReturn(List.of());

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(ID_TRAMITE));

        assertEquals(Constantes.MENSAJE_TIPO_TRAMITE_EXONERACION_NO_CONFIGURADO, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerChecklistRevision_TramiteQueNoEsExoneracion_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(ID_OTRO_TRAMITE));

        assertEquals(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerChecklistRevision_SinTramite_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllRevisionDocumento.ObtenerChecklistRevision(null));

        assertEquals(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO, vlo_Excepcion.getMessage());
    }


    @Test
    void RegistrarRevision_PrimeraRevision_GuardaActivaYRegistraBitacoraSinDatosAnteriores() {
        EntRevisionDocumento vlo_EnRevision = this.vgo_BllRevisionDocumento.RegistrarRevision(
                ID_TRAMITE, ID_DOCUMENTO, ID_REVISOR, " recibido ", "   ");

        assertEquals(ID_REVISION_NUEVA, vlo_EnRevision.getIdRevisionDocumento());
        assertSame(this.vgo_EnDocumento, vlo_EnRevision.getDocumento());
        assertSame(this.vgo_EnRevisor, vlo_EnRevision.getUsuarioRevisor());
        assertEquals("RECIBIDO", vlo_EnRevision.getEstadoRevision());
        assertNull(vlo_EnRevision.getObservacion());
        assertNotNull(vlo_EnRevision.getFechaHora());
        assertEquals("ACT", vlo_EnRevision.getEstado());
        verify(this.vgo_DalRevisionDocumento, times(1)).save(any(EntRevisionDocumento.class));

        ArgumentCaptor<EntBitacora> vlo_Captor = ArgumentCaptor.forClass(EntBitacora.class);
        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(vlo_Captor.capture());
        EntBitacora vlo_EnBitacora = vlo_Captor.getValue();
        assertEquals(ID_TRAMITE, vlo_EnBitacora.getIdTramite());
        assertEquals(ID_REVISOR, vlo_EnBitacora.getIdUsuario());
        assertEquals("DOCUMENTO_REVISADO", vlo_EnBitacora.getAccion());
        assertEquals("SIMT_REVISION_DOCUMENTO", vlo_EnBitacora.getEntidadAfectada());
        assertEquals(ID_REVISION_NUEVA, vlo_EnBitacora.getIdRegistroAfectado());
        assertNull(vlo_EnBitacora.getDatosAnteriores());
        assertEquals("estadoRevision=RECIBIDO; observacion=", vlo_EnBitacora.getDatosNuevos());
        assertEquals("ACT", vlo_EnBitacora.getEstado());
    }

    @Test
    void RegistrarRevision_ConRevisionAnterior_LaInactivaYConservaElHistorial() {
        EntRevisionDocumento vlo_EnAnterior = this.Revision(70, "RECHAZADO", "Ilegible", "ACT");
        when(this.vgo_DalRevisionDocumento.ObtenerRevisionActivaPorDocumento(this.vgo_EnDocumento))
                .thenReturn(Optional.of(vlo_EnAnterior));

        this.vgo_BllRevisionDocumento.RegistrarRevision(
                ID_TRAMITE, ID_DOCUMENTO, ID_REVISOR, "RECIBIDO", "  Se recibió la versión legible.  ");

        ArgumentCaptor<EntRevisionDocumento> vlo_CaptorRevision = ArgumentCaptor.forClass(EntRevisionDocumento.class);
        verify(this.vgo_DalRevisionDocumento, times(2)).save(vlo_CaptorRevision.capture());
        assertSame(vlo_EnAnterior, vlo_CaptorRevision.getAllValues().get(0));
        assertEquals("INA", vlo_EnAnterior.getEstado());
        assertEquals("RECHAZADO", vlo_EnAnterior.getEstadoRevision());
        assertEquals("Se recibió la versión legible.", vlo_CaptorRevision.getAllValues().get(1).getObservacion());

        ArgumentCaptor<EntBitacora> vlo_CaptorBitacora = ArgumentCaptor.forClass(EntBitacora.class);
        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(vlo_CaptorBitacora.capture());
        assertEquals("estadoRevision=RECHAZADO; observacion=Ilegible",
                vlo_CaptorBitacora.getValue().getDatosAnteriores());
        assertEquals("estadoRevision=RECIBIDO; observacion=Se recibió la versión legible.",
                vlo_CaptorBitacora.getValue().getDatosNuevos());
    }

    @Test
    void RegistrarRevision_RechazoConObservacion_LoAcepta() {
        EntRevisionDocumento vlo_EnRevision = this.vgo_BllRevisionDocumento.RegistrarRevision(
                ID_TRAMITE, ID_DOCUMENTO, ID_REVISOR, "rechazado", "El documento está incompleto.");

        assertEquals("RECHAZADO", vlo_EnRevision.getEstadoRevision());
        assertEquals("El documento está incompleto.", vlo_EnRevision.getObservacion());
    }

    @Test
    void RegistrarRevision_RechazoSinObservacion_Rechaza() {
        this.AfirmarRevisionRechazada(ID_DOCUMENTO, ID_REVISOR, "RECHAZADO", "  ",
                Constantes.MENSAJE_OBSERVACION_RECHAZO_REQUERIDA);
    }

    @Test
    void RegistrarRevision_EstadoDesconocido_Rechaza() {
        this.AfirmarRevisionRechazada(ID_DOCUMENTO, ID_REVISOR, "APROBADO", null,
                Constantes.MENSAJE_ESTADO_REVISION_INVALIDO);
    }

    @Test
    void RegistrarRevision_EstadoEnBlanco_Rechaza() {
        this.AfirmarRevisionRechazada(ID_DOCUMENTO, ID_REVISOR, "   ", null,
                Constantes.MENSAJE_ESTADO_REVISION_REQUERIDO);
    }

    @Test
    void RegistrarRevision_ObservacionDe1001Caracteres_Rechaza() {
        this.AfirmarRevisionRechazada(ID_DOCUMENTO, ID_REVISOR, "RECIBIDO", "a".repeat(1001),
                Constantes.MENSAJE_OBSERVACION_REVISION_INVALIDA);
    }

    @Test
    void RegistrarRevision_ObservacionDe1000Caracteres_LaAcepta() {
        this.vgo_BllRevisionDocumento.RegistrarRevision(
                ID_TRAMITE, ID_DOCUMENTO, ID_REVISOR, "RECIBIDO", "a".repeat(1000));

        verify(this.vgo_DalRevisionDocumento).save(any(EntRevisionDocumento.class));
    }

    @Test
    void RegistrarRevision_SinDocumento_Rechaza() {
        this.AfirmarRevisionRechazada(null, ID_REVISOR, "RECIBIDO", null, Constantes.MENSAJE_DOCUMENTO_REQUERIDO);
    }

    @Test
    void RegistrarRevision_SinRevisor_Rechaza() {
        this.AfirmarRevisionRechazada(ID_DOCUMENTO, null, "RECIBIDO", null,
                Constantes.MENSAJE_USUARIO_REVISOR_REQUERIDO);
    }

    @Test
    void RegistrarRevision_DocumentoInexistente_Rechaza() {
        this.AfirmarRevisionRechazada(999, ID_REVISOR, "RECIBIDO", null, Constantes.MENSAJE_DOCUMENTO_NO_ENCONTRADO);
    }

    @Test
    void RegistrarRevision_DocumentoDeOtroExpediente_Rechaza() {
        this.vgo_EnDocumento.setIdTramite(ID_OTRO_TRAMITE);

        this.AfirmarRevisionRechazada(ID_DOCUMENTO, ID_REVISOR, "RECIBIDO", null,
                Constantes.MENSAJE_DOCUMENTO_INCORRECTO_EXONERACION);
    }

    @Test
    void RegistrarRevision_DocumentoNoActivo_Rechaza() {
        this.vgo_EnDocumento.setEstadoDocumento("INA");

        this.AfirmarRevisionRechazada(ID_DOCUMENTO, ID_REVISOR, "RECIBIDO", null,
                Constantes.MENSAJE_DOCUMENTO_REVISION_NO_ACTIVO);
    }

    @Test
    void RegistrarRevision_RevisorInexistente_Rechaza() {
        this.AfirmarRevisionRechazada(ID_DOCUMENTO, 999L, "RECIBIDO", null,
                Constantes.MENSAJE_USUARIO_REVISOR_NO_ENCONTRADO);
    }


    @Test
    void ObtenerHistorial_ConvierteTodasLasRevisionesDelDocumento() {
        when(this.vgo_DalRevisionDocumento.ObtenerHistorialPorDocumento(this.vgo_EnDocumento))
                .thenReturn(List.of(
                        this.Revision(70, "RECHAZADO", "Ilegible", "INA"),
                        this.Revision(80, "RECIBIDO", null, "ACT")));

        List<ModeloHistorialRevisionRespuesta> vlo_LsHistorial =
                this.vgo_BllRevisionDocumento.ObtenerHistorial(ID_TRAMITE, ID_DOCUMENTO);

        assertEquals(2, vlo_LsHistorial.size());
        ModeloHistorialRevisionRespuesta vlo_Primera = vlo_LsHistorial.get(0);
        assertEquals(70, vlo_Primera.getIdRevisionDocumento());
        assertEquals(ID_DOCUMENTO, vlo_Primera.getIdDocumento());
        assertEquals("RECHAZADO", vlo_Primera.getEstadoRevision());
        assertEquals("Ilegible", vlo_Primera.getObservacion());
        assertEquals(FECHA_REVISION, vlo_Primera.getFechaRevision());
        assertEquals("INA", vlo_Primera.getEstadoRegistro());
        assertEquals(ID_REVISOR, vlo_Primera.getIdUsuarioRevisor());
        assertEquals("K. Valverde Fallas", vlo_Primera.getNombreUsuarioRevisor());
        assertEquals("ACT", vlo_LsHistorial.get(1).getEstadoRegistro());
    }

    @Test
    void ObtenerHistorial_DocumentoDeOtroExpediente_Rechaza() {
        this.vgo_EnDocumento.setIdTramite(ID_OTRO_TRAMITE);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllRevisionDocumento.ObtenerHistorial(ID_TRAMITE, ID_DOCUMENTO));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_INCORRECTO_EXONERACION, vlo_Excepcion.getMessage());
        verify(this.vgo_DalRevisionDocumento, never()).ObtenerHistorialPorDocumento(any());
    }

    @Test
    void ObtenerHistorial_TramiteQueNoEsExoneracion_Rechaza() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllRevisionDocumento.ObtenerHistorial(ID_OTRO_TRAMITE, ID_DOCUMENTO));

        assertEquals(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA, vlo_Excepcion.getMessage());
    }


    private void AfirmarRevisionRechazada(
            Integer pvn_IdDocumento, Long pvn_IdRevisor, String pvc_Estado, String pvc_Observacion, String pvc_Mensaje) {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllRevisionDocumento.RegistrarRevision(
                        ID_TRAMITE, pvn_IdDocumento, pvn_IdRevisor, pvc_Estado, pvc_Observacion));
        assertEquals(pvc_Mensaje, vlo_Excepcion.getMessage());
        verify(this.vgo_DalRevisionDocumento, never()).save(any(EntRevisionDocumento.class));
        verify(this.vgo_DalBitacora, never()).SIML_BITACORA_InsertarRegistro(any());
    }


    private EntRequisitoDocumental Requisito(Integer pvn_IdTipo, String pvc_Codigo, String pvc_Nombre,
                                             boolean pvb_Obligatorio) {
        EntRequisitoDocumental vlo_EnRequisito = mock(EntRequisitoDocumental.class, RETURNS_DEEP_STUBS);
        when(vlo_EnRequisito.getTipoDocumento().getIdTipoDocumento()).thenReturn(pvn_IdTipo);
        when(vlo_EnRequisito.getTipoDocumento().getCodigo()).thenReturn(pvc_Codigo);
        when(vlo_EnRequisito.getTipoDocumento().getNombre()).thenReturn(pvc_Nombre);
        when(vlo_EnRequisito.getObligatorio()).thenReturn(pvb_Obligatorio);
        return vlo_EnRequisito;
    }

    private EntDocumento Documento(Integer pvn_Id, Integer pvn_IdTipoDocumento, String pvc_Nombre) {
        EntDocumento vlo_EnDocumento = new EntDocumento();
        vlo_EnDocumento.setIdDocumento(pvn_Id);
        vlo_EnDocumento.setIdTramite(ID_TRAMITE);
        vlo_EnDocumento.setIdTipoDocumento(pvn_IdTipoDocumento);
        vlo_EnDocumento.setNombreArchivo(pvc_Nombre);
        vlo_EnDocumento.setEstadoDocumento("ACT");
        return vlo_EnDocumento;
    }

    private EntRevisionDocumento Revision(Integer pvn_Id, String pvc_EstadoRevision, String pvc_Observacion,
                                          String pvc_Estado) {
        EntRevisionDocumento vlo_EnRevision = new EntRevisionDocumento();
        vlo_EnRevision.setIdRevisionDocumento(pvn_Id);
        vlo_EnRevision.setDocumento(this.vgo_EnDocumento);
        vlo_EnRevision.setUsuarioRevisor(this.vgo_EnRevisor);
        vlo_EnRevision.setEstadoRevision(pvc_EstadoRevision);
        vlo_EnRevision.setObservacion(pvc_Observacion);
        vlo_EnRevision.setFechaHora(FECHA_REVISION);
        vlo_EnRevision.setEstado(pvc_Estado);
        return vlo_EnRevision;
    }
}
