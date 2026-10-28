package cr.ac.ucr.sim.logicanegocio.tramites;

import cr.ac.ucr.sim.accesodatos.tramites.DalMoneda;
import cr.ac.ucr.sim.accesodatos.tramites.DalTipoTramite;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntArchivoExportado;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntColumnaBusqueda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntFiltroBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntListadoTramites;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionCatalogo;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntOpcionesBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntResultadoBusquedaTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.reportes.tramites.Rpt_SIM_ListadoTramites;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Pruebas de las reglas de negocio de la búsqueda y exportación de trámites (HU-034).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BllBusquedaTramiteTest {

    private static final byte[] CONTENIDO_PDF = {37, 80, 68, 70};

    @Mock
    private DalTramite vgo_DalTramite;

    @Mock
    private DalTipoTramite vgo_DalTipoTramite;

    @Mock
    private DalMoneda vgo_DalMoneda;

    private Rpt_SIM_ListadoTramites vgo_ReportePdf;
    private Rpt_SIM_ListadoTramites vgo_ReporteExcel;
    private BllBusquedaTramite vgo_BllBusquedaTramite;

    @BeforeEach
    void PrepararPrueba() {
        this.vgo_ReportePdf = this.CrearReporte(Constantes.FORMATO_EXPORTACION_PDF, "PDF", "pdf", "application/pdf");
        this.vgo_ReporteExcel = this.CrearReporte(
                Constantes.FORMATO_EXPORTACION_EXCEL, "Excel", "xlsx", "application/vnd.ms-excel");
        Rpt_SIM_ListadoTramites vlo_ReporteWord = this.CrearReporte(
                Constantes.FORMATO_EXPORTACION_WORD, "Word", "docx", "application/msword");
        // Se registran en desorden: las opciones deben ofrecerlos siempre como Excel, PDF y Word.
        this.vgo_BllBusquedaTramite = new BllBusquedaTramite(
                this.vgo_DalTramite,
                this.vgo_DalTipoTramite,
                this.vgo_DalMoneda,
                List.of(this.vgo_ReportePdf, vlo_ReporteWord, this.vgo_ReporteExcel));
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerListaBusqueda(any())).thenAnswer(vlo_Invocacion -> new ArrayList<>(List.of(
                this.CrearResultado("DE-2026-0001", Constantes.CATEGORIA_ESTADO_TRAMITE_ACTIVO, null),
                this.CrearResultado("EX-2026-0001", Constantes.CATEGORIA_ESTADO_PENDIENTE_INGRESO,
                        Constantes.TIPO_EXONERACION_DONACION))));
        when(this.vgo_DalTipoTramite.SIMM_TIPO_TRAMITE_ObtenerLista(Constantes.ESTADO_CATALOGO_ACTIVO))
                .thenReturn(List.of(this.CrearTipoTramite(1, "Desalmacenaje especial"),
                        this.CrearTipoTramite(5, "Solicitud de exoneración")));
        when(this.vgo_DalMoneda.SIMM_MONEDA_ObtenerActivas())
                .thenReturn(List.of(this.CrearMoneda(2, "USD")));
    }

    @Test
    void BuscarTramites_SinCriterios_ConsultaSinRestricciones() {
        List<EntResultadoBusquedaTramite> vlo_LsResultados = this.vgo_BllBusquedaTramite.BuscarTramites(null);

        EntFiltroBusquedaTramite vlo_Filtro = this.CapturarFiltroConsultado();
        assertEquals(2, vlo_LsResultados.size());
        assertNull(vlo_Filtro.getFechaDesde());
        assertNull(vlo_Filtro.getReferenciaInterna());
        assertTrue(vlo_Filtro.getIdTiposTramite().isEmpty());
        assertTrue(vlo_Filtro.getCategoriasEstado().isEmpty());
    }

    @Test
    void BuscarTramites_CriteriosConEspaciosYRepetidos_LosNormaliza() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setReferenciaInterna("  ex-2026  ");
        vlo_Filtro.setIdTiposTramite(Arrays.asList(1, null, 1, 5));
        vlo_Filtro.setCategoriasEstado(Arrays.asList(" pen", "PEN", "", null, "fin"));
        vlo_Filtro.setTiposExoneracion(List.of("donacion "));
        vlo_Filtro.setRegimenesAduaneros(List.of(" Importación definitiva ", "  "));
        vlo_Filtro.setAduanasControl(null);

        this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro);

        EntFiltroBusquedaTramite vlo_Consultado = this.CapturarFiltroConsultado();
        assertEquals("ex-2026", vlo_Consultado.getReferenciaInterna());
        assertEquals(List.of(1, 5), vlo_Consultado.getIdTiposTramite());
        assertEquals(List.of("PEN", "FIN"), vlo_Consultado.getCategoriasEstado());
        assertEquals(List.of("DONACION"), vlo_Consultado.getTiposExoneracion());
        assertEquals(List.of("Importación definitiva"), vlo_Consultado.getRegimenesAduaneros());
        assertTrue(vlo_Consultado.getAduanasControl().isEmpty());
    }

    @Test
    void BuscarTramites_ReferenciaEnBlanco_NoFiltraPorReferencia() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setReferenciaInterna("   ");

        this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro);

        assertNull(this.CapturarFiltroConsultado().getReferenciaInterna());
    }

    @Test
    void BuscarTramites_CompletaNombresDeEstadoYTipoExoneracion() {
        List<EntResultadoBusquedaTramite> vlo_LsResultados =
                this.vgo_BllBusquedaTramite.BuscarTramites(new EntFiltroBusquedaTramite());

        assertEquals(Constantes.NOMBRE_CATEGORIA_TRAMITE_ACTIVO, vlo_LsResultados.get(0).getCategoriaEstadoNombre());
        assertNull(vlo_LsResultados.get(0).getTipoExoneracionNombre());
        assertEquals(Constantes.NOMBRE_CATEGORIA_PENDIENTE_INGRESO, vlo_LsResultados.get(1).getCategoriaEstadoNombre());
        assertEquals(Constantes.NOMBRE_TIPO_EXONERACION_DONACION, vlo_LsResultados.get(1).getTipoExoneracionNombre());
    }

    @Test
    void BuscarTramites_FechaInicialPosteriorALaFinal_RechazaBusqueda() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setFechaDesde(LocalDate.of(2026, 9, 10));
        vlo_Filtro.setFechaHasta(LocalDate.of(2026, 9, 1));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro));

        assertEquals(Constantes.MENSAJE_RANGO_FECHAS_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ObtenerListaBusqueda(any());
    }

    @Test
    void BuscarTramites_MismaFechaInicialYFinal_EsValida() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setFechaDesde(LocalDate.of(2026, 9, 10));
        vlo_Filtro.setFechaHasta(LocalDate.of(2026, 9, 10));

        this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro);

        assertEquals(LocalDate.of(2026, 9, 10), this.CapturarFiltroConsultado().getFechaHasta());
    }

    @Test
    void BuscarTramites_CategoriaDeEstadoDesconocida_RechazaBusqueda() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setCategoriasEstado(List.of("PEN", "CERRADO"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro));

        assertEquals(Constantes.MENSAJE_CATEGORIA_ESTADO_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void BuscarTramites_TipoExoneracionDesconocido_RechazaBusqueda() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setTiposExoneracion(List.of("OTRO"));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro));

        assertEquals(Constantes.MENSAJE_TIPO_EXONERACION_INVALIDO, vlo_Excepcion.getMessage());
    }

    @Test
    void BuscarTramites_ReferenciaMayorAlMaximo_RechazaBusqueda() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setReferenciaInterna("X".repeat(Constantes.LONGITUD_MAXIMA_REFERENCIA_INTERNA + 1));

        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBusquedaTramite.BuscarTramites(vlo_Filtro));

        assertEquals(Constantes.MENSAJE_REFERENCIA_BUSQUEDA_INVALIDA, vlo_Excepcion.getMessage());
    }

    @Test
    void ObtenerOpciones_IncluyeCatalogosEstadosColumnasYFormatos() {
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegimenesAduaneros()).thenReturn(List.of("Importación definitiva"));
        when(this.vgo_DalTramite.SIMT_TRAMITE_ObtenerAduanasControl()).thenReturn(List.of("Aduana Santamaría"));

        EntOpcionesBusquedaTramite vlo_Opciones = this.vgo_BllBusquedaTramite.ObtenerOpciones();

        assertEquals(2, vlo_Opciones.getTiposTramite().size());
        assertEquals(List.of(Constantes.NOMBRE_CATEGORIA_PENDIENTE_INGRESO, Constantes.NOMBRE_CATEGORIA_TRAMITE_ACTIVO,
                        Constantes.NOMBRE_CATEGORIA_TRAMITE_FINALIZADO),
                vlo_Opciones.getCategoriasEstado().stream().map(EntOpcionCatalogo::getNombre).toList());
        assertEquals(3, vlo_Opciones.getTiposExoneracion().size());
        assertEquals(List.of("Importación definitiva"), vlo_Opciones.getRegimenesAduaneros());
        assertEquals(List.of("Aduana Santamaría"), vlo_Opciones.getAduanasControl());
        assertEquals(List.of("EXCEL", "PDF", "WORD"),
                vlo_Opciones.getFormatosExportacion().stream().map(EntOpcionCatalogo::getCodigo).toList());
        List<String> vlo_LsNoDisponibles = vlo_Opciones.getColumnas().stream()
                .filter(vlo_Columna -> !vlo_Columna.isDisponible())
                .map(EntColumnaBusqueda::getCodigo)
                .toList();
        assertEquals(List.of("fechaEnvio", "fechaDesalmacenaje", "impuestosPagados", "impuestosExonerados",
                "costosPermisos", "totalCif"), vlo_LsNoDisponibles);
    }

    @Test
    void ExportarTramites_FormatoValido_GeneraConLaMismaBusquedaYCriterios() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setIdTiposTramite(List.of(5));
        vlo_Filtro.setCategoriasEstado(List.of("pen"));
        vlo_Filtro.setIdMonedas(List.of(2));
        vlo_Filtro.setFechaDesde(LocalDate.of(2026, 9, 1));

        EntArchivoExportado vlo_Archivo = this.vgo_BllBusquedaTramite.ExportarTramites(vlo_Filtro, " pdf ");

        ArgumentCaptor<EntListadoTramites> vlo_Captor = ArgumentCaptor.forClass(EntListadoTramites.class);
        verify(this.vgo_ReportePdf).Generar(vlo_Captor.capture());
        EntListadoTramites vlo_Listado = vlo_Captor.getValue();
        assertArrayEquals(CONTENIDO_PDF, vlo_Archivo.getContenido());
        assertEquals("application/pdf", vlo_Archivo.getTipoContenido());
        assertTrue(vlo_Archivo.getNombreArchivo().matches("listado-tramites-\\d{8}-\\d{4}\\.pdf"));
        assertEquals(2, vlo_Listado.getResultados().size());
        assertEquals(Constantes.NOMBRE_CATEGORIA_TRAMITE_ACTIVO,
                vlo_Listado.getResultados().get(0).getCategoriaEstadoNombre());
        assertEquals(15, vlo_Listado.getColumnas().size());
        assertEquals(List.of(
                "Periodo: 01/09/2026 al sin fin",
                "Tipo de contratación: Solicitud de exoneración",
                "Estado: Pendiente de ingreso",
                "Moneda: USD"), vlo_Listado.getCriteriosAplicados());
        assertEquals(List.of("PEN"), this.CapturarFiltroConsultado().getCategoriasEstado());
    }

    @Test
    void ExportarTramites_SinCriterios_LoIndicaEnElArchivo() {
        this.vgo_BllBusquedaTramite.ExportarTramites(new EntFiltroBusquedaTramite(), "PDF");

        ArgumentCaptor<EntListadoTramites> vlo_Captor = ArgumentCaptor.forClass(EntListadoTramites.class);
        verify(this.vgo_ReportePdf).Generar(vlo_Captor.capture());
        assertEquals(List.of("Criterios: todos los trámites"), vlo_Captor.getValue().getCriteriosAplicados());
    }

    @Test
    void ExportarTramites_FormatoDesconocido_RechazaExportacion() {
        ExcepcionNegocio vlo_Excepcion = assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBusquedaTramite.ExportarTramites(new EntFiltroBusquedaTramite(), "CSV"));

        assertEquals(Constantes.MENSAJE_FORMATO_EXPORTACION_INVALIDO, vlo_Excepcion.getMessage());
        verify(this.vgo_DalTramite, never()).SIMT_TRAMITE_ObtenerListaBusqueda(any());
    }

    @Test
    void ExportarTramites_CriteriosInvalidos_NoGeneraArchivo() {
        EntFiltroBusquedaTramite vlo_Filtro = new EntFiltroBusquedaTramite();
        vlo_Filtro.setCategoriasEstado(List.of("XYZ"));

        assertThrows(ExcepcionNegocio.class,
                () -> this.vgo_BllBusquedaTramite.ExportarTramites(vlo_Filtro, Constantes.FORMATO_EXPORTACION_PDF));

        verify(this.vgo_ReportePdf, never()).Generar(any());
    }

    @Test
    void ExportarTramites_CadaFormato_UsaSuPropioReporte() {
        EntArchivoExportado vlo_Archivo =
                this.vgo_BllBusquedaTramite.ExportarTramites(new EntFiltroBusquedaTramite(), "excel");

        assertEquals("application/vnd.ms-excel", vlo_Archivo.getTipoContenido());
        assertTrue(vlo_Archivo.getNombreArchivo().endsWith(".xlsx"));
        verify(this.vgo_ReporteExcel).Generar(any());
        verify(this.vgo_ReportePdf, never()).Generar(any());
    }

    private EntFiltroBusquedaTramite CapturarFiltroConsultado() {
        ArgumentCaptor<EntFiltroBusquedaTramite> vlo_Captor = ArgumentCaptor.forClass(EntFiltroBusquedaTramite.class);
        verify(this.vgo_DalTramite).SIMT_TRAMITE_ObtenerListaBusqueda(vlo_Captor.capture());
        return vlo_Captor.getValue();
    }

    private Rpt_SIM_ListadoTramites CrearReporte(String pvc_Formato, String pvc_Nombre, String pvc_Extension,
                                                 String pvc_TipoContenido) {
        Rpt_SIM_ListadoTramites vlo_Reporte = mock(Rpt_SIM_ListadoTramites.class);
        when(vlo_Reporte.getFormato()).thenReturn(pvc_Formato);
        when(vlo_Reporte.getNombreFormato()).thenReturn(pvc_Nombre);
        when(vlo_Reporte.getExtension()).thenReturn(pvc_Extension);
        when(vlo_Reporte.getTipoContenido()).thenReturn(pvc_TipoContenido);
        if (Constantes.FORMATO_EXPORTACION_PDF.equals(pvc_Formato)) {
            when(vlo_Reporte.Generar(any())).thenReturn(CONTENIDO_PDF);
        }
        return vlo_Reporte;
    }

    private EntResultadoBusquedaTramite CrearResultado(String pvc_Referencia, String pvc_Categoria,
                                                       String pvc_TipoExoneracion) {
        return new EntResultadoBusquedaTramite(1, pvc_Referencia, LocalDateTime.of(2026, 9, 2, 8, 0),
                "Desalmacenaje especial", "Importación definitiva", "Analista de Importaciones", pvc_Categoria,
                "Pendiente", new BigDecimal("100.00"), "USD", pvc_TipoExoneracion);
    }

    private EntTipoTramite CrearTipoTramite(Integer pvn_Id, String pvc_Nombre) {
        EntTipoTramite vlo_Tipo = new EntTipoTramite();
        vlo_Tipo.setIdTipoTramite(pvn_Id);
        vlo_Tipo.setNombre(pvc_Nombre);
        return vlo_Tipo;
    }

    private EntMoneda CrearMoneda(Integer pvn_Id, String pvc_Codigo) {
        EntMoneda vlo_Moneda = new EntMoneda();
        vlo_Moneda.setIdMoneda(pvn_Id);
        vlo_Moneda.setCodigoIso(pvc_Codigo);
        return vlo_Moneda;
    }
}
