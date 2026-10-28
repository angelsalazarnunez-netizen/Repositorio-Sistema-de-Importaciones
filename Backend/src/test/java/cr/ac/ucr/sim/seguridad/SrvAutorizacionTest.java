package cr.ac.ucr.sim.seguridad;

import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.Permisos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Pruebas de las reglas de acceso que dependen del tipo del trámite (HU-003).
 */
class SrvAutorizacionTest {

    private static final int ID_TIPO_EXONERACION = 1;
    private static final int ID_TIPO_DONACION = 2;
    private static final int ID_TRAMITE_EXONERACION = 10;

    private DalTramite vgo_DalTramite;
    private DalTipoTramite vgo_DalTipoTramite;
    private DalDocumento vgo_DalDocumento;
    private SrvAutorizacion vgo_Servicio;

    @BeforeEach
    void Preparar() {
        this.vgo_DalTramite = mock(DalTramite.class);
        this.vgo_DalTipoTramite = mock(DalTipoTramite.class);
        this.vgo_DalDocumento = mock(DalDocumento.class);
        this.vgo_Servicio = new SrvAutorizacion(this.vgo_DalTramite, this.vgo_DalTipoTramite, this.vgo_DalDocumento);

        EntTipoTramite vlo_Exoneracion = Tipo(ID_TIPO_EXONERACION, Constantes.CODIGO_TIPO_TRAMITE_EXONERACION);
        EntTipoTramite vlo_Donacion = Tipo(ID_TIPO_DONACION, Constantes.CODIGO_TIPO_TRAMITE_DONACION);
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerLista()).thenReturn(List.of(vlo_Exoneracion, vlo_Donacion));
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(ID_TIPO_EXONERACION)).thenReturn(vlo_Exoneracion);
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerRegistro(ID_TIPO_DONACION)).thenReturn(vlo_Donacion);

        EntTramite vlo_Tramite = new EntTramite();
        vlo_Tramite.setIdTramite(ID_TRAMITE_EXONERACION);
        vlo_Tramite.setReferenciaInterna("EXO-001");
        vlo_Tramite.setIdTipoTramite(ID_TIPO_EXONERACION);
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerPorReferenciaInterna("EXO-001")).thenReturn(vlo_Tramite);
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(ID_TRAMITE_EXONERACION)).thenReturn(vlo_Tramite);

        EntDocumento vlo_Documento = new EntDocumento();
        vlo_Documento.setIdDocumento(100);
        vlo_Documento.setIdTramite(ID_TRAMITE_EXONERACION);
        when(this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(100)).thenReturn(vlo_Documento);
    }

    @AfterEach
    void Limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void SinSesionNoConsultaTramites() {
        assertFalse(this.vgo_Servicio.TieneConsultaDeTramites());
    }

    @Test
    void ConsultaSoloLosTiposDeSusPermisos() {
        Sesion(Permisos.DONACION_CONSULTAR);
        assertTrue(this.vgo_Servicio.TieneConsultaDeTramites());
        assertEquals(Set.of(Constantes.CODIGO_TIPO_TRAMITE_DONACION),
                this.vgo_Servicio.CodigosTipoTramiteConsultables());
        assertEquals(List.of(ID_TIPO_DONACION), this.vgo_Servicio.RestringirTiposTramite(null));
        assertEquals(List.of(ID_TIPO_DONACION),
                this.vgo_Servicio.RestringirTiposTramite(List.of(ID_TIPO_EXONERACION, ID_TIPO_DONACION)));
        assertThrows(ExcepcionAutenticacion.class,
                () -> this.vgo_Servicio.RestringirTiposTramite(List.of(ID_TIPO_EXONERACION)));
    }

    @Test
    void ReportesConsultaTodosLosTipos() {
        Sesion(Permisos.REPORTE_CONSULTAR);
        assertTrue(this.vgo_Servicio.TieneConsultaDeTramites());
        assertNull(this.vgo_Servicio.CodigosTipoTramiteConsultables());
        assertEquals(List.of(), this.vgo_Servicio.RestringirTiposTramite(null));
        assertEquals(List.of(ID_TIPO_EXONERACION), this.vgo_Servicio.RestringirTiposTramite(List.of(ID_TIPO_EXONERACION)));
    }

    @Test
    void ModificarExigeElPermisoDelTipo() {
        Sesion(Permisos.EXONERACION_CONSULTAR, Permisos.DONACION_MODIFICAR);
        assertFalse(this.vgo_Servicio.PuedeModificarTramite("EXO-001"));

        Sesion(Permisos.EXONERACION_MODIFICAR);
        assertTrue(this.vgo_Servicio.PuedeModificarTramite("EXO-001"));
    }

    @Test
    void ReportesNoPermiteModificar() {
        Sesion(Permisos.REPORTE_CONSULTAR);
        assertFalse(this.vgo_Servicio.PuedeModificarTramite("EXO-001"));
    }

    @Test
    void TramiteODocumentoInexistenteLoResuelveLaLogicaDeNegocio() {
        Sesion(Permisos.DONACION_CONSULTAR);
        assertTrue(this.vgo_Servicio.PuedeModificarTramite("NO-EXISTE"));
        assertTrue(this.vgo_Servicio.PuedeConsultarDocumento(999));
    }

    @Test
    void DescargarDocumentoExigeConsultarSuTipo() {
        Sesion(Permisos.DONACION_CONSULTAR);
        assertFalse(this.vgo_Servicio.PuedeConsultarDocumento(100));

        Sesion(Permisos.EXONERACION_CONSULTAR);
        assertTrue(this.vgo_Servicio.PuedeConsultarDocumento(100));
    }

    private static EntTipoTramite Tipo(int pvn_Id, String pvc_Codigo) {
        EntTipoTramite vlo_Tipo = new EntTipoTramite();
        vlo_Tipo.setIdTipoTramite(pvn_Id);
        vlo_Tipo.setCodigo(pvc_Codigo);
        return vlo_Tipo;
    }

    private static void Sesion(String... pvo_Permisos) {
        UsernamePasswordAuthenticationToken vlo_Autenticacion = new UsernamePasswordAuthenticationToken(
                "usuario", null,
                Arrays.stream(pvo_Permisos).map(SimpleGrantedAuthority::new).toList());
        SecurityContextHolder.getContext().setAuthentication(vlo_Autenticacion);
    }
}
