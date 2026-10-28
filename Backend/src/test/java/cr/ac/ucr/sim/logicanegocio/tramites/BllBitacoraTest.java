package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalBitacora;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntComentarioBitacora;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 15/09/2026
 * Descripción: Pruebas de las reglas de la bitácora del expediente (HU-018): registro de
 *   comentarios y eliminación de los registrados por error sin perder la auditoría.
 */
@ExtendWith(MockitoExtension.class)
class BllBitacoraTest {

    private static final Integer ID_TRAMITE = 200;
    private static final Integer ID_USUARIO = 1;

    @Mock
    private DalBitacora vgo_DalBitacora;

    @Mock
    private DalUsuario vgo_DalUsuario;

    @InjectMocks
    private BllBitacora vgo_BllBitacora;

    @BeforeEach
    void PrepararPrueba() {
        EntUsuario vlo_EnUsuario = new EntUsuario();
        vlo_EnUsuario.setIdUsuario(ID_USUARIO);
        lenient().when(this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(ID_USUARIO)).thenReturn(vlo_EnUsuario);
        lenient().when(this.vgo_DalBitacora.SIML_BITACORA_ObtenerPorTramite(any(), anyString())).thenReturn(List.of());
    }

    @Test
    void InsertarComentario_ConTextoValido_DebeRegistrarloComoComentarioVigente() {
        this.vgo_BllBitacora.InsertarComentario(ID_TRAMITE, this.CrearComentario("  Se solicitó el S1 al proveedor.  "));

        ArgumentCaptor<EntBitacora> vlo_Captor = ArgumentCaptor.forClass(EntBitacora.class);
        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(vlo_Captor.capture());
        EntBitacora vlo_EnBitacora = vlo_Captor.getValue();

        assertEquals(Constantes.ACCION_BITACORA_COMENTARIO, vlo_EnBitacora.getAccion());
        assertEquals("Se solicitó el S1 al proveedor.", vlo_EnBitacora.getDatosNuevos());
        assertEquals(Constantes.ESTADO_BITACORA_ACTIVO, vlo_EnBitacora.getEstado());
        assertEquals(ID_TRAMITE, vlo_EnBitacora.getIdTramite());
    }

    @Test
    void InsertarComentario_De1000Caracteres_DebeAceptarlo() {
        this.vgo_BllBitacora.InsertarComentario(ID_TRAMITE, this.CrearComentario("a".repeat(1000)));

        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(any());
    }

    @Test
    void InsertarComentario_De1001Caracteres_DebeLanzarExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBitacora.InsertarComentario(ID_TRAMITE, this.CrearComentario("a".repeat(1001))));

        assertEquals(Constantes.MENSAJE_COMENTARIO_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalBitacora, never()).SIML_BITACORA_InsertarRegistro(any());
    }

    @Test
    void InsertarComentario_EnBlanco_DebeLanzarExcepcion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBitacora.InsertarComentario(ID_TRAMITE, this.CrearComentario("   ")));

        assertEquals(Constantes.MENSAJE_COMENTARIO_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void EliminarComentario_Vigente_DebeMarcarloInactivoYRegistrarLaEliminacionConElTextoOriginal() {
        EntBitacora vlo_EnComentario = this.CrearRegistro(10, ID_TRAMITE, Constantes.ACCION_BITACORA_COMENTARIO);
        when(this.vgo_DalBitacora.SIML_BITACORA_ObtenerRegistro(10)).thenReturn(vlo_EnComentario);

        this.vgo_BllBitacora.EliminarComentario(ID_TRAMITE, 10, ID_USUARIO);

        assertEquals(Constantes.ESTADO_BITACORA_INACTIVO, vlo_EnComentario.getEstado());
        verify(this.vgo_DalBitacora).SIML_BITACORA_ModificarRegistro(vlo_EnComentario);

        ArgumentCaptor<EntBitacora> vlo_Captor = ArgumentCaptor.forClass(EntBitacora.class);
        verify(this.vgo_DalBitacora).SIML_BITACORA_InsertarRegistro(vlo_Captor.capture());
        EntBitacora vlo_EnEliminacion = vlo_Captor.getValue();
        assertEquals(Constantes.ACCION_BITACORA_COMENTARIO_ELIMINADO, vlo_EnEliminacion.getAccion());
        assertEquals(10, vlo_EnEliminacion.getIdRegistroAfectado());
        assertEquals("Texto registrado por error", vlo_EnEliminacion.getDatosAnteriores());
        assertNull(vlo_EnEliminacion.getDatosNuevos());
    }

    @Test
    void EliminarComentario_DeUnaAccionDelSistema_DebeLanzarExcepcion() {
        when(this.vgo_DalBitacora.SIML_BITACORA_ObtenerRegistro(11))
                .thenReturn(this.CrearRegistro(11, ID_TRAMITE, Constantes.ACCION_BITACORA_REGISTRO));

        this.AfirmarComentarioNoEncontrado(11);
    }

    @Test
    void EliminarComentario_DeOtroExpediente_DebeLanzarExcepcion() {
        when(this.vgo_DalBitacora.SIML_BITACORA_ObtenerRegistro(12))
                .thenReturn(this.CrearRegistro(12, 999, Constantes.ACCION_BITACORA_COMENTARIO));

        this.AfirmarComentarioNoEncontrado(12);
    }

    @Test
    void EliminarComentario_YaEliminado_DebeLanzarExcepcion() {
        EntBitacora vlo_EnComentario = this.CrearRegistro(13, ID_TRAMITE, Constantes.ACCION_BITACORA_COMENTARIO);
        vlo_EnComentario.setEstado(Constantes.ESTADO_BITACORA_INACTIVO);
        when(this.vgo_DalBitacora.SIML_BITACORA_ObtenerRegistro(13)).thenReturn(vlo_EnComentario);

        this.AfirmarComentarioNoEncontrado(13);
    }

    private void AfirmarComentarioNoEncontrado(Integer pvn_IdBitacora) {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBitacora.EliminarComentario(ID_TRAMITE, pvn_IdBitacora, ID_USUARIO));
        assertEquals(Constantes.MENSAJE_COMENTARIO_NO_ENCONTRADO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalBitacora, never()).SIML_BITACORA_ModificarRegistro(any());
    }

    private EntComentarioBitacora CrearComentario(String pvc_Texto) {
        EntComentarioBitacora vlo_Comentario = new EntComentarioBitacora();
        vlo_Comentario.setComentario(pvc_Texto);
        vlo_Comentario.setIdUsuario(ID_USUARIO);
        return vlo_Comentario;
    }

    private EntBitacora CrearRegistro(Integer pvn_Id, Integer pvn_IdTramite, String pvc_Accion) {
        EntBitacora vlo_EnBitacora = new EntBitacora();
        vlo_EnBitacora.setIdBitacora(pvn_Id);
        vlo_EnBitacora.setIdTramite(pvn_IdTramite);
        vlo_EnBitacora.setAccion(pvc_Accion);
        vlo_EnBitacora.setDatosNuevos("Texto registrado por error");
        vlo_EnBitacora.setEstado(Constantes.ESTADO_BITACORA_ACTIVO);
        return vlo_EnBitacora;
    }
}
