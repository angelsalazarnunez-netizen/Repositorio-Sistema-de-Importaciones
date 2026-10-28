package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoDocumento;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoDocumento;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 22/09/2026
 * Descripción: Pruebas de la documentación del expediente que es común a todos los trámites:
 *   el retiro de un documento y su clasificación (HU-045).
 */
@ExtendWith(MockitoExtension.class)
class BllDocumentoTest {

    private static final Integer ID_TRAMITE = 100;
    private static final Integer ID_DOCUMENTO = 55;
    private static final Integer ID_USUARIO = 1;

    @Mock
    private DalDocumento vgo_DalDocumento;

    @Mock
    private DalTipoDocumento vgo_DalTipoDocumento;

    @Mock
    private DalUsuario vgo_DalUsuario;

    @Mock
    private BllBitacora vgo_BllBitacora;

    @InjectMocks
    private BllDocumento vgo_BllDocumento;

    @BeforeEach
    void PrepararPrueba() {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(ID_USUARIO);
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(vlo_EnUsuario);
    }

    @Test
    void EliminarDocumento_VigenteDelExpediente_DebeMarcarloInactivoYAnotarloEnLaBitacora() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        this.vgo_BllDocumento.EliminarDocumento(ID_TRAMITE, ID_DOCUMENTO, ID_USUARIO);

        assertEquals(Constantes.ESTADO_DOCUMENTO_INACTIVO, vlo_EnDocumento.getEstadoDocumento());
        verify(this.vgo_DalDocumento).SIMF_DOCUMENTO_ModificarRegistro(vlo_EnDocumento);
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DOCUMENTO_ELIMINADO),
                eq(Constantes.ENTIDAD_BITACORA_DOCUMENTO),
                eq(ID_DOCUMENTO), anyString(), anyString());
    }

    @Test
    void EliminarDocumento_DeOtroExpediente_DebeLanzarExcepcion() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setIdTramite(999);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.EliminarDocumento(ID_TRAMITE, ID_DOCUMENTO, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_NO_PERTENECE_AL_EXPEDIENTE, vlo_Excepcion.getMessage());
        verify(this.vgo_DalDocumento, never()).SIMF_DOCUMENTO_ModificarRegistro(any());
    }

    @Test
    void EliminarDocumento_Inexistente_DebeLanzarExcepcion() {
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.EliminarDocumento(ID_TRAMITE, ID_DOCUMENTO, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_NO_PERTENECE_AL_EXPEDIENTE, vlo_Excepcion.getMessage());
    }

    @Test
    void EliminarDocumento_YaRetirado_DebeLanzarExcepcion() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_INACTIVO);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.EliminarDocumento(ID_TRAMITE, ID_DOCUMENTO, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_YA_ELIMINADO, vlo_Excepcion.getMessage());
        verify(this.vgo_BllBitacora, never()).RegistrarAccion(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void EliminarDocumento_ConUsuarioInexistente_DebeLanzarExcepcion() {
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO))
                .thenReturn(this.CrearDocumentoVigente());
        when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(null);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.EliminarDocumento(ID_TRAMITE, ID_DOCUMENTO, ID_USUARIO));

        assertEquals(Constantes.MENSAJE_USUARIO_RESPONSABLE_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalDocumento, never()).SIMF_DOCUMENTO_ModificarRegistro(any());
    }

    // ---------------------------------------------------------------- Clasificación (HU-045)

    @Test
    void ClasificarDocumento_ConUnTipoDistinto_DebeGuardarloYAnotarloEnLaBitacora() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setIdTipoDocumento(3);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);
        when(this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo("FACTURA"))
                .thenReturn(this.CrearTipoDocumento(7, "FACTURA", "Factura o documento equivalente"));
        when(this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerRegistro(3))
                .thenReturn(this.CrearTipoDocumento(3, "EMBARQUE", "Documento de embarque"));

        this.vgo_BllDocumento.ClasificarDocumento(ID_TRAMITE, ID_DOCUMENTO, "FACTURA", ID_USUARIO);

        assertEquals(7, vlo_EnDocumento.getIdTipoDocumento());
        assertEquals(Constantes.ESTADO_DOCUMENTO_ACTIVO, vlo_EnDocumento.getEstadoDocumento());
        verify(this.vgo_DalDocumento).SIMF_DOCUMENTO_ModificarRegistro(vlo_EnDocumento);
        verify(this.vgo_BllBitacora).RegistrarAccion(
                eq(ID_TRAMITE), eq(ID_USUARIO),
                eq(Constantes.ACCION_BITACORA_DOCUMENTO_CLASIFICADO),
                eq(Constantes.ENTIDAD_BITACORA_DOCUMENTO),
                eq(ID_DOCUMENTO), anyString(), anyString());
    }

    @Test
    void ClasificarDocumento_ConUnTipoQueNoEstaEnElCatalogo_DebeLanzarExcepcion() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setIdTipoDocumento(3);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.ClasificarDocumento(
                        ID_TRAMITE, ID_DOCUMENTO, "NO_EXISTE", ID_USUARIO));

        assertEquals(Constantes.MENSAJE_CLASIFICACION_INVALIDA, vlo_Excepcion.getMessage());
        verify(this.vgo_DalDocumento, never()).SIMF_DOCUMENTO_ModificarRegistro(any());
    }

    @Test
    void ClasificarDocumento_ConLaMismaClasificacion_DebeLanzarExcepcion() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setIdTipoDocumento(7);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);
        when(this.vgo_DalTipoDocumento.SIMM_TIPO_DOCUMENTO_ObtenerPorCodigo("FACTURA"))
                .thenReturn(this.CrearTipoDocumento(7, "FACTURA", "Factura o documento equivalente"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.ClasificarDocumento(
                        ID_TRAMITE, ID_DOCUMENTO, "FACTURA", ID_USUARIO));

        assertEquals(Constantes.MENSAJE_CLASIFICACION_SIN_CAMBIO, vlo_Excepcion.getMessage());
    }

    @Test
    void ClasificarDocumento_DeOtroExpediente_DebeLanzarExcepcion() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setIdTramite(999);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.ClasificarDocumento(
                        ID_TRAMITE, ID_DOCUMENTO, "FACTURA", ID_USUARIO));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_NO_PERTENECE_AL_EXPEDIENTE, vlo_Excepcion.getMessage());
    }

    @Test
    void ClasificarDocumento_YaRetiradoDelExpediente_DebeLanzarExcepcion() {
        EntDocumento vlo_EnDocumento = this.CrearDocumentoVigente();
        vlo_EnDocumento.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_INACTIVO);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(ID_DOCUMENTO)).thenReturn(vlo_EnDocumento);

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllDocumento.ClasificarDocumento(
                        ID_TRAMITE, ID_DOCUMENTO, "FACTURA", ID_USUARIO));

        assertEquals(Constantes.MENSAJE_DOCUMENTO_YA_ELIMINADO, vlo_Excepcion.getMessage());
    }

    // ---------------------------------------------------------------- Datos de apoyo

    private EntTipoDocumento CrearTipoDocumento(Integer pvn_Id, String pvc_Codigo, String pvc_Nombre) {
        EntTipoDocumento vlo_EnTipo = new EntTipoDocumento();
        vlo_EnTipo.setIdTipoDocumento(pvn_Id);
        vlo_EnTipo.setCodigo(pvc_Codigo);
        vlo_EnTipo.setNombre(pvc_Nombre);
        return vlo_EnTipo;
    }

    private EntDocumento CrearDocumentoVigente() {
        EntDocumento vlo_EnDocumento = new EntDocumento();
        vlo_EnDocumento.setIdDocumento(ID_DOCUMENTO);
        vlo_EnDocumento.setIdTramite(ID_TRAMITE);
        vlo_EnDocumento.setNombreArchivo("acta-tardia.pdf");
        vlo_EnDocumento.setVersion((short) 1);
        vlo_EnDocumento.setEstadoDocumento(Constantes.ESTADO_DOCUMENTO_ACTIVO);
        return vlo_EnDocumento;
    }
}
