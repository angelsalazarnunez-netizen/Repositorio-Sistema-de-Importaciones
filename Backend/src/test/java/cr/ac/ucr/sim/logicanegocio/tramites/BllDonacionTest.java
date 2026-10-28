package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalDonacion;
import cr.ac.ucr.sim.accesodatos.tramites.DalEstadoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalHistorialEstado;
import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalPais;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntCambioEstadoDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntHistorialEstado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntRegistroDonacion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Angel Salazar Nunez
 * Fecha: 21/09/2026
 * Descripción: Pruebas de las reglas de negocio de la gestión de donaciones (HU-044).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se integran a main: la clase pasa a llamarse BllDonacionTest y las pruebas usan
 *     las consultas de estados y los mensajes que comparte el seguimiento de la HU-032.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se agregan las pruebas de la eliminación de documentos del expediente.
 *
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 03/10/2026
 *   Descripción: se agregan las pruebas de las siglas D- y DF- y del tipo Fundación UCR, que
 *     reemplaza a FUNDEVI.
 */
@ExtendWith(MockitoExtension.class)
class BllDonacionTest {

    private static final Integer ID_TIPO_DONACION = 4;
    private static final Integer ID_TRAMITE = 100;
    private static final Integer ID_USUARIO = 7;
    private static final String REFERENCIA = "D-2026-0001";

    @Mock private DalTramite vgo_DalTramite;
    @Mock private DalDonacion vgo_DalDonacion;
    @Mock private DalDocumento vgo_DalDocumento;
    @Mock private DalHistorialEstado vgo_DalHistorialEstado;
    @Mock private DalTipoTramite vgo_DalTipoTramite;
    @Mock private DalEstadoTramite vgo_DalEstadoTramite;
    @Mock private DalMoneda vgo_DalMoneda;
    @Mock private DalPais vgo_DalPais;
    @Mock private DalTipoDocumento vgo_DalTipoDocumento;
    @Mock private DalUsuario vgo_DalUsuario;
    @Mock private AlmacenamientoDocumentos vgo_AlmacenamientoDocumentos;
    @Mock private BllBitacora vgo_BllBitacora;
    @Mock private BllDocumento vgo_BllDocumento;

    private BllDonacion vgo_BllDonacion;

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_BllDonacion = new BllDonacion(vgo_DalTramite, vgo_DalDonacion, vgo_DalDocumento,
                vgo_DalHistorialEstado, vgo_DalTipoTramite, vgo_DalEstadoTramite, vgo_DalMoneda,
                vgo_DalPais, vgo_DalTipoDocumento, vgo_DalUsuario, vgo_AlmacenamientoDocumentos,
                vgo_BllBitacora, vgo_BllDocumento);
        lenient().when(vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerPorCodigo(Constantes.CODIGO_TIPO_TRAMITE_DONACION))
                .thenReturn(CrearTipoDonacion());
    }

    @Test
    void ObtenerListaExpedientes_ConTipoConfigurado_ConsultaDonacionesActivas() {
        this.vgo_BllDonacion.ObtenerListaExpedientes();

        verify(vgo_DalTramite).SIMT_TRAMITE_ObtenerListaDonaciones(
                ID_TIPO_DONACION, Constantes.ESTADO_DOCUMENTO_ACTIVO);
    }

    @Test
    void ObtenerEstadosDisponibles_ConTipoConfigurado_ConsultaElCatalogoDelTipoDonacion() {
        this.vgo_BllDonacion.ObtenerEstadosDisponibles();

        verify(vgo_DalEstadoTramite).SIMM_ESTADO_TRAMITE_ObtenerPorTipoTramite(
                ID_TIPO_DONACION, Constantes.ESTADO_CATALOGO_ACTIVO);
    }

    @Test
    void ActualizarEstado_ConDatosValidos_RegistraHistorialActualizaTramiteYBitacora() {
        EntTramite vlo_Tramite = CrearTramite(10);
        EntEstadoTramite vlo_EnRevision = CrearEstado(20, "En revisión", false);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(10)).thenReturn(CrearEstado(10, "Pendiente", false));
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                ID_TIPO_DONACION, "En revisión", Constantes.ESTADO_CATALOGO_ACTIVO))
                .thenReturn(vlo_EnRevision);
        when(vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(CrearUsuario());
        PrepararArmadoExpediente(vlo_Tramite, vlo_EnRevision);

        this.vgo_BllDonacion.ActualizarEstado(REFERENCIA, CrearCambio(" En revisión ", "  Documentación verificada. "));

        ArgumentCaptor<EntHistorialEstado> vlo_Captor = ArgumentCaptor.forClass(EntHistorialEstado.class);
        verify(vgo_DalHistorialEstado).SIMH_ESTADO_TRAMITE_InsertarRegistro(vlo_Captor.capture());
        assertEquals(10, vlo_Captor.getValue().getIdEstadoAnterior());
        assertEquals(20, vlo_Captor.getValue().getIdEstadoNuevo());
        assertEquals(ID_USUARIO, vlo_Captor.getValue().getIdUsuario());
        assertEquals("Documentación verificada.", vlo_Captor.getValue().getJustificacion());
        assertEquals(20, vlo_Tramite.getIdEstadoTramite());
        verify(vgo_DalTramite).SIMT_TRAMITE_ModificarRegistro(vlo_Tramite);
        verify(vgo_BllBitacora).RegistrarAccion(anyInt(), anyInt(),
                org.mockito.ArgumentMatchers.eq(Constantes.ACCION_BITACORA_CAMBIO_ESTADO),
                org.mockito.ArgumentMatchers.eq(Constantes.ENTIDAD_BITACORA_TRAMITE), anyInt(), anyString(), anyString());
    }

    @Test
    void ActualizarEstado_ConEstadoFinal_AsignaFechaDeCierre() {
        EntTramite vlo_Tramite = CrearTramite(10);
        EntEstadoTramite vlo_Finalizado = CrearEstado(30, "Finalizado", true);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(10)).thenReturn(CrearEstado(10, "Pendiente", false));
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                ID_TIPO_DONACION, "Finalizado", Constantes.ESTADO_CATALOGO_ACTIVO))
                .thenReturn(vlo_Finalizado);
        when(vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(CrearUsuario());
        PrepararArmadoExpediente(vlo_Tramite, vlo_Finalizado);

        this.vgo_BllDonacion.ActualizarEstado(REFERENCIA, CrearCambio("Finalizado", "Resolución aplicada."));

        assertNotNull(vlo_Tramite.getFechaCierre());
    }

    @Test
    void ActualizarEstado_SinJustificacion_LanzaExcepcionYNoModificaElTramite() {
        EntTramite vlo_Tramite = CrearTramite(10);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDonacion.ActualizarEstado(REFERENCIA, CrearCambio("En revisión", "  ")));

        assertEquals(Constantes.MENSAJE_JUSTIFICACION_ESTADO_INVALIDA, vlo_Excepcion.getMessage());
        verify(vgo_DalTramite, never()).SIMT_TRAMITE_ModificarRegistro(any());
    }

    @Test
    void ActualizarEstado_ConMismoEstado_LanzaExcepcionYNoCreaHistorial() {
        EntTramite vlo_Tramite = CrearTramite(10);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(10)).thenReturn(CrearEstado(10, "Pendiente", false));
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerPorNombre(
                ID_TIPO_DONACION, "Pendiente", Constantes.ESTADO_CATALOGO_ACTIVO))
                .thenReturn(CrearEstado(10, "Pendiente", false));
        when(vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(CrearUsuario());

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDonacion.ActualizarEstado(REFERENCIA, CrearCambio("Pendiente", "Sin cambios.")));

        assertEquals(Constantes.MENSAJE_ESTADO_SIN_CAMBIO, vlo_Excepcion.getMessage());
        verify(vgo_DalHistorialEstado, never()).SIMH_ESTADO_TRAMITE_InsertarRegistro(any());
    }

    @Test
    void ActualizarEstado_EnExpedienteCerrado_LanzaExcepcion() {
        EntTramite vlo_Tramite = CrearTramite(10);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(10)).thenReturn(CrearEstado(10, "Finalizado", true));
        when(vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(CrearUsuario());

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDonacion.ActualizarEstado(REFERENCIA, CrearCambio("En revisión", "Intento posterior.")));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_DONACION_CERRADO, vlo_Excepcion.getMessage());
    }

    @Test
    void EliminarDocumento_EnExpedienteAbierto_LoRetiraConLaReglaComun() {
        EntTramite vlo_Tramite = CrearTramite(10);
        EntEstadoTramite vlo_Pendiente = CrearEstado(10, "Pendiente", false);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(10)).thenReturn(vlo_Pendiente);
        PrepararArmadoExpediente(vlo_Tramite, vlo_Pendiente);

        this.vgo_BllDonacion.EliminarDocumento(REFERENCIA, 50, ID_USUARIO);

        verify(vgo_BllDocumento).EliminarDocumento(ID_TRAMITE, 50, ID_USUARIO);
    }

    @Test
    void EliminarDocumento_EnExpedienteCerrado_LanzaExcepcionYNoRetiraNada() {
        EntTramite vlo_Tramite = CrearTramite(30);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna(REFERENCIA)).thenReturn(vlo_Tramite);
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(30)).thenReturn(CrearEstado(30, "Finalizada", true));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDonacion.EliminarDocumento(REFERENCIA, 50, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_EXPEDIENTE_DONACION_CERRADO, vlo_Excepcion.getMessage());
        verify(vgo_BllDocumento, never()).EliminarDocumento(anyInt(), anyInt(), anyInt());
    }

    @Test
    void ObtenerDocumento_DeDonacionExistente_LoDevuelve() {
        EntDocumento vlo_Documento = new EntDocumento();
        vlo_Documento.setIdTramite(ID_TRAMITE);
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(50)).thenReturn(vlo_Documento);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(ID_TRAMITE)).thenReturn(CrearTramite(10));

        assertEquals(vlo_Documento, this.vgo_BllDonacion.ObtenerDocumento(50));
    }

    @Test
    void ObtenerDocumento_DeOtroTipoDeTramite_LanzaExcepcion() {
        EntDocumento vlo_Documento = new EntDocumento();
        vlo_Documento.setIdTramite(ID_TRAMITE);
        EntTramite vlo_TramiteAjeno = CrearTramite(10);
        vlo_TramiteAjeno.setIdTipoTramite(999);
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(50)).thenReturn(vlo_Documento);
        when(vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(ID_TRAMITE)).thenReturn(vlo_TramiteAjeno);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDonacion.ObtenerDocumento(50));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_NO_DISPONIBLE, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerContenidoDocumento_DelegadoAlAlmacenamiento_RetornaElArchivo() {
        EntDocumento vlo_Documento = new EntDocumento();
        vlo_Documento.setRutaArchivo("donaciones/100/acta.pdf");
        byte[] vlb_Contenido = {1, 2, 3};
        when(vgo_AlmacenamientoDocumentos.LeerArchivo("donaciones/100/acta.pdf")).thenReturn(vlb_Contenido);

        assertArrayEquals(vlb_Contenido, this.vgo_BllDonacion.ObtenerContenidoDocumento(vlo_Documento));
    }

    @Test
    void InsertarRegistro_FundacionUcrConSiglaD_LanzaExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.InsertarRegistro(CrearRegistro("D-2026-0001", Constantes.TIPO_DONACION_FUNDACION_UCR)));

        assertEquals(Constantes.MENSAJE_REFERENCIA_DONACION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_NacionalConSiglaDF_LanzaExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.InsertarRegistro(CrearRegistro("DF-2026-0001", Constantes.TIPO_DONACION_NACIONAL)));

        assertEquals(Constantes.MENSAJE_REFERENCIA_DONACION_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void InsertarRegistro_FundacionUcrConSiglaDF_AceptaLaReferencia() {
        // Sin donante, el registro se detiene en la validación siguiente a la de la referencia.
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.InsertarRegistro(CrearRegistro("df-2026-0001", Constantes.TIPO_DONACION_FUNDACION_UCR)));

        assertEquals(Constantes.MENSAJE_DONANTE_REQUERIDO, vlo_Excepcion.getMessage());
        verify(vgo_DalTramite).SIMT_TRAMITE_ExistePorReferenciaInterna("DF-2026-0001");
    }

    @Test
    void InsertarRegistro_InternacionalConSiglaD_AceptaLaReferencia() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.InsertarRegistro(CrearRegistro("D-2026-0001", Constantes.TIPO_DONACION_INTERNACIONAL)));

        assertEquals(Constantes.MENSAJE_DONANTE_REQUERIDO, vlo_Excepcion.getMessage());
        verify(vgo_DalTramite).SIMT_TRAMITE_ExistePorReferenciaInterna("D-2026-0001");
    }

    @Test
    void InsertarRegistro_ConTipoFundeviAnterior_LanzaExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.InsertarRegistro(CrearRegistro("DF-2026-0001", "FUNDEVI")));

        assertEquals(Constantes.MENSAJE_TIPO_DONACION_INVALIDO, vlo_Excepcion.getMessage());
    }

    private void InsertarRegistro(EntRegistroDonacion pvo_Registro) {
        this.vgo_BllDonacion.InsertarRegistro(pvo_Registro, null, List.of(), null, null, null);
    }

    private EntRegistroDonacion CrearRegistro(String pvc_Referencia, String pvc_TipoDonacion) {
        EntRegistroDonacion vlo_Registro = new EntRegistroDonacion();
        vlo_Registro.setReferenciaInterna(pvc_Referencia);
        vlo_Registro.setTipoDonacion(pvc_TipoDonacion);
        return vlo_Registro;
    }

    private void PrepararArmadoExpediente(EntTramite pvo_Tramite, EntEstadoTramite pvo_Estado) {
        when(vgo_DalEstadoTramite.SIMM_ESTADO_TRAMITE_ObtenerRegistro(pvo_Estado.getIdEstadoTramite())).thenReturn(pvo_Estado);
        when(vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerListaExpediente(ID_TRAMITE)).thenReturn(List.of());
        when(vgo_DalHistorialEstado.SIMH_ESTADO_TRAMITE_ObtenerPorTramite(ID_TRAMITE)).thenReturn(List.of());
    }

    private EntTipoTramite CrearTipoDonacion() {
        EntTipoTramite vlo_Tipo = new EntTipoTramite();
        vlo_Tipo.setIdTipoTramite(ID_TIPO_DONACION);
        vlo_Tipo.setNombre("Donación");
        vlo_Tipo.setPrefijoReferencia("D-");
        vlo_Tipo.setRequiereSicop(false);
        vlo_Tipo.setRequiereOrdenCompra(false);
        return vlo_Tipo;
    }

    private EntTramite CrearTramite(Integer pvn_IdEstado) {
        EntTramite vlo_Tramite = new EntTramite();
        vlo_Tramite.setIdTramite(ID_TRAMITE);
        vlo_Tramite.setReferenciaInterna(REFERENCIA);
        vlo_Tramite.setIdTipoTramite(ID_TIPO_DONACION);
        vlo_Tramite.setIdEstadoTramite(pvn_IdEstado);
        vlo_Tramite.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Tramite;
    }

    private EntEstadoTramite CrearEstado(Integer pvn_Id, String pvc_Nombre, boolean pvb_Final) {
        EntEstadoTramite vlo_Estado = new EntEstadoTramite();
        vlo_Estado.setIdEstadoTramite(pvn_Id);
        vlo_Estado.setNombre(pvc_Nombre);
        vlo_Estado.setEsFinal(pvb_Final);
        return vlo_Estado;
    }

    private EntUsuario CrearUsuario() {
        EntUsuario vlo_Usuario = new EntUsuario();
        vlo_Usuario.setIdUsuario(ID_USUARIO);
        vlo_Usuario.setNombreCompleto("Ana Responsable");
        return vlo_Usuario;
    }

    private EntCambioEstadoDonacion CrearCambio(String pvc_Estado, String pvc_Justificacion) {
        EntCambioEstadoDonacion vlo_Cambio = new EntCambioEstadoDonacion();
        vlo_Cambio.setNuevoEstado(pvc_Estado);
        vlo_Cambio.setJustificacion(pvc_Justificacion);
        vlo_Cambio.setIdUsuarioResponsable(ID_USUARIO);
        return vlo_Cambio;
    }
}
