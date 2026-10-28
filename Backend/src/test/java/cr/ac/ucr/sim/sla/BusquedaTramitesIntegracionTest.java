package cr.ac.ucr.sim.sla;

import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntEstadoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntMoneda;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTipoTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.utilerias.Constantes;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Pruebas de integración de la búsqueda de trámites (HU-034). Recorren el servicio
 *   REST, la lógica de negocio y la consulta JPQL contra una base de datos H2 en memoria, de modo que
 *   no dependen del MySQL de desarrollo. Cada prueba corre en una transacción que se revierte.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 30/09/2026
 *   Descripción: las pruebas corren con un usuario autenticado, porque la API exige sesión desde
 *     la HU-004, y el usuario de prueba declara los datos de cuenta que ahora son obligatorios.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sim_busqueda;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"})
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class BusquedaTramitesIntegracionTest {

    private static final String URL_BUSQUEDA = "/api/v1/tramites/busqueda";

    @Autowired
    private MockMvc vgo_MockMvc;

    @Autowired
    private EntityManager vgo_EntityManager;

    private Integer vgn_IdDesalmacenaje;
    private Integer vgn_IdExoneracion;
    private Integer vgn_IdUsd;
    private Integer vgn_IdCrc;

    @BeforeEach
    void PrepararDatos() {
        EntUsuario vlo_Usuario = new EntUsuario();
        vlo_Usuario.setNombreCompleto("Analista de Importaciones");
        vlo_Usuario.setCorreoInstitucional("analista.busqueda@ucr.ac.cr");
        vlo_Usuario.setContrasenaHash("no-utilizable");
        vlo_Usuario.setEstadoValidacion(Constantes.ESTADO_VALIDACION_VALIDADO);
        vlo_Usuario.setEstadoRol(Constantes.ESTADO_ROL_ASIGNADO);
        // Cédula y fecha de nacimiento son obligatorias desde la HU-001 Registrar cuenta de usuario.
        vlo_Usuario.setNumeroCedula("101110111");
        vlo_Usuario.setFechaNacimiento(LocalDate.of(1990, 1, 15));
        this.vgo_EntityManager.persist(vlo_Usuario);

        EntTipoTramite vlo_Desalmacenaje = this.CrearTipo("DESALM_ESPECIAL", "Desalmacenaje especial", "DE-");
        EntTipoTramite vlo_Exoneracion = this.CrearTipo("EXONERACION", "Solicitud de exoneración", "EXO-");
        this.vgn_IdDesalmacenaje = vlo_Desalmacenaje.getIdTipoTramite();
        this.vgn_IdExoneracion = vlo_Exoneracion.getIdTipoTramite();

        EntEstadoTramite vlo_Pendiente = this.CrearEstado(vlo_Desalmacenaje, "Pendiente", "PEN");
        EntEstadoTramite vlo_EnRevision = this.CrearEstado(vlo_Desalmacenaje, "En revisión", "ACT");
        EntEstadoTramite vlo_Desalmacenado = this.CrearEstado(vlo_Desalmacenaje, "Desalmacenado", "FIN");
        EntEstadoTramite vlo_PendienteRevision = this.CrearEstado(vlo_Exoneracion, "Pendiente revisión documental", "PEN");

        this.vgn_IdUsd = this.CrearMoneda("USD").getIdMoneda();
        this.vgn_IdCrc = this.CrearMoneda("CRC").getIdMoneda();
        this.CrearMoneda("EUR");

        Integer vln_IdUsuario = vlo_Usuario.getIdUsuario();
        this.CrearTramite("DE-2026-9001", vlo_Desalmacenaje, vlo_Pendiente, vln_IdUsuario, this.vgn_IdUsd,
                "1850.00", "Importación definitiva", "Aduana Santamaría", LocalDateTime.of(2026, 7, 8, 9, 15));
        this.CrearTramite("DE-2026-9002", vlo_Desalmacenaje, vlo_EnRevision, vln_IdUsuario, this.vgn_IdCrc,
                "425000.00", "Importación definitiva", "Aduana Central", LocalDateTime.of(2026, 8, 14, 14, 30));
        this.CrearTramite("DE-2026-9003", vlo_Desalmacenaje, vlo_Desalmacenado, vln_IdUsuario, this.vgn_IdUsd,
                "3200.00", "Importación temporal", "Aduana Santamaría", LocalDateTime.of(2026, 9, 2, 23, 59));
        EntTramite vlo_Ex1 = this.CrearTramite("EXO-2026-9001", vlo_Exoneracion, vlo_PendienteRevision, vln_IdUsuario,
                null, null, "Importación definitiva", "Aduana Santamaría", LocalDateTime.of(2026, 8, 20, 10, 45));
        EntTramite vlo_Ex2 = this.CrearTramite("EXO-2026-9002", vlo_Exoneracion, vlo_PendienteRevision, vln_IdUsuario,
                null, null, null, null, LocalDateTime.of(2026, 9, 5, 16, 20));
        this.CrearExoneracion(vlo_Ex1, "COMPRA_INSTITUCIONAL");
        this.CrearExoneracion(vlo_Ex2, "DONACION");
        this.vgo_EntityManager.flush();
    }

    @Test
    void Busqueda_SinCriterios_ListaTodosLosTiposDelMasRecienteAlMasAntiguo() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].referenciaInterna",
                        contains("EXO-2026-9002", "DE-2026-9003", "EXO-2026-9001", "DE-2026-9002", "DE-2026-9001")))
                .andExpect(jsonPath("$[0].tipoTramite").value("Solicitud de exoneración"))
                .andExpect(jsonPath("$[0].valorMercancia").value(nullValue()))
                .andExpect(jsonPath("$[0].moneda").value(nullValue()))
                .andExpect(jsonPath("$[0].tipoExoneracionNombre").value("Donación"))
                .andExpect(jsonPath("$[1].categoriaEstadoNombre").value("Trámite finalizado"))
                .andExpect(jsonPath("$[1].estadoTramite").value("Desalmacenado"))
                .andExpect(jsonPath("$[1].usuario").value("Analista de Importaciones"))
                .andExpect(jsonPath("$[1].tipoExoneracion").value(nullValue()));
    }

    @Test
    void Busqueda_VariosEstados_DevuelveLosDeCualquieraDeEllos() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("categoriasEstado", "PEN")
                        .param("categoriasEstado", "FIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna",
                        contains("EXO-2026-9002", "DE-2026-9003", "EXO-2026-9001", "DE-2026-9001")));
    }

    @Test
    void Busqueda_TipoDeContratacionYMoneda_CombinaAmbosCriterios() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("tiposTramite", String.valueOf(this.vgn_IdDesalmacenaje))
                        .param("monedas", String.valueOf(this.vgn_IdUsd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna", contains("DE-2026-9003", "DE-2026-9001")));
    }

    @Test
    void Busqueda_RangoDeFechas_IncluyeElDiaFinalCompleto() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("fechaDesde", "2026-08-14")
                        .param("fechaHasta", "2026-09-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna",
                        contains("DE-2026-9003", "EXO-2026-9001", "DE-2026-9002")));
    }

    @Test
    void Busqueda_IdDeTramiteParcial_NoDistingueMayusculas() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA).param("referenciaInterna", "exo-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna", contains("EXO-2026-9002", "EXO-2026-9001")));
    }

    @Test
    void Busqueda_IdConComodines_LosBuscaLiteralmente() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA).param("referenciaInterna", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA).param("referenciaInterna", "DE_2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void Busqueda_TipoDeExoneracion_SoloDevuelveEsasSolicitudes() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA).param("tiposExoneracion", "DONACION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna", contains("EXO-2026-9002")));
    }

    @Test
    void Busqueda_RegimenAduanaYEstado_CombinaLosTresCriterios() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("regimenesAduaneros", "Importación definitiva")
                        .param("aduanasControl", "Aduana Santamaría")
                        .param("categoriasEstado", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna", contains("EXO-2026-9001", "DE-2026-9001")));
    }

    @Test
    void Busqueda_TodosLosCriteriosALaVez_DevuelveLaCoincidenciaExacta() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("fechaDesde", "2026-07-01")
                        .param("fechaHasta", "2026-07-31")
                        .param("referenciaInterna", "9001")
                        .param("tiposTramite", String.valueOf(this.vgn_IdDesalmacenaje))
                        .param("categoriasEstado", "PEN")
                        .param("monedas", String.valueOf(this.vgn_IdUsd))
                        .param("regimenesAduaneros", "Importación definitiva")
                        .param("aduanasControl", "Aduana Santamaría"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].referenciaInterna", contains("DE-2026-9001")));
    }

    @Test
    void Busqueda_CriteriosSinCoincidencias_DevuelveListaVacia() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("tiposTramite", String.valueOf(this.vgn_IdExoneracion))
                        .param("monedas", String.valueOf(this.vgn_IdCrc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void Busqueda_RangoDeFechasInvertido_RespondeMensajeDeError() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA)
                        .param("fechaDesde", "2026-09-10")
                        .param("fechaHasta", "2026-09-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(Constantes.MENSAJE_RANGO_FECHAS_INVALIDO));
    }

    @Test
    void Busqueda_EstadoDesconocido_RespondeMensajeDeError() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA).param("categoriasEstado", "CERRADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(Constantes.MENSAJE_CATEGORIA_ESTADO_INVALIDA));
    }

    @Test
    void Opciones_DevuelveCatalogosValoresRegistradosColumnasYFormatos() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA + "/opciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tiposTramite[*].nombre",
                        contains("Desalmacenaje especial", "Solicitud de exoneración")))
                .andExpect(jsonPath("$.categoriasEstado[*].codigo", contains("PEN", "ACT", "FIN")))
                .andExpect(jsonPath("$.monedas[*].codigoIso", contains("CRC", "EUR", "USD")))
                .andExpect(jsonPath("$.regimenesAduaneros", contains("Importación definitiva", "Importación temporal")))
                .andExpect(jsonPath("$.aduanasControl", contains("Aduana Central", "Aduana Santamaría")))
                .andExpect(jsonPath("$.tiposExoneracion", hasSize(3)))
                .andExpect(jsonPath("$.columnas", hasSize(15)))
                .andExpect(jsonPath("$.columnas[?(@.disponible == false)].codigo", contains("fechaEnvio",
                        "fechaDesalmacenaje", "impuestosPagados", "impuestosExonerados", "costosPermisos", "totalCif")))
                .andExpect(jsonPath("$.formatosExportacion[*].codigo", contains("EXCEL", "PDF", "WORD")));
    }

    @Test
    void Exportar_Excel_ContieneLasMismasFilasQueLaBusqueda() throws Exception {
        MvcResult vlo_Resultado = this.vgo_MockMvc.perform(this.ConCriteriosDeExportacion(get(URL_BUSQUEDA + "/exportar/EXCEL")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", containsString("listado-tramites-")))
                .andExpect(header().string("Content-Disposition", containsString(".xlsx")))
                .andReturn();

        try (XSSFWorkbook vlo_Libro = new XSSFWorkbook(
                new ByteArrayInputStream(vlo_Resultado.getResponse().getContentAsByteArray()))) {
            Sheet vlo_Hoja = vlo_Libro.getSheetAt(0);
            // Título, fecha de generación, dos criterios, línea en blanco y encabezado ocupan las filas 0 a 5.
            assertEquals("Tipo de contratación: Desalmacenaje especial", vlo_Hoja.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Moneda: USD", vlo_Hoja.getRow(3).getCell(0).getStringCellValue());
            assertEquals("DE-2026-9003", vlo_Hoja.getRow(6).getCell(0).getStringCellValue());
            assertEquals("DE-2026-9001", vlo_Hoja.getRow(7).getCell(0).getStringCellValue());
            assertEquals(7, vlo_Hoja.getLastRowNum());
        }
    }

    @Test
    void Exportar_Pdf_ContieneSoloLosTramitesFiltrados() throws Exception {
        MvcResult vlo_Resultado = this.vgo_MockMvc.perform(this.ConCriteriosDeExportacion(get(URL_BUSQUEDA + "/exportar/pdf")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        PdfReader vlo_Lector = new PdfReader(vlo_Resultado.getResponse().getContentAsByteArray());
        String vlc_Texto = new PdfTextExtractor(vlo_Lector).getTextFromPage(1).replaceAll("\\s+", " ");
        vlo_Lector.close();
        assertTrue(vlc_Texto.contains("DE-2026-9001"));
        assertTrue(vlc_Texto.contains("DE-2026-9003"));
        assertFalse(vlc_Texto.contains("DE-2026-9002"));
        assertFalse(vlc_Texto.contains("EXO-2026-9001"));
        assertTrue(vlc_Texto.contains("2 trámite(s)"));
    }

    @Test
    void Exportar_Word_ContieneSoloLosTramitesFiltrados() throws Exception {
        MvcResult vlo_Resultado = this.vgo_MockMvc.perform(this.ConCriteriosDeExportacion(get(URL_BUSQUEDA + "/exportar/WORD")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andReturn();

        try (XWPFDocument vlo_Documento = new XWPFDocument(
                new ByteArrayInputStream(vlo_Resultado.getResponse().getContentAsByteArray()))) {
            XWPFTable vlo_Tabla = vlo_Documento.getTables().get(0);
            assertEquals(3, vlo_Tabla.getNumberOfRows());
            assertEquals("DE-2026-9003", vlo_Tabla.getRow(1).getCell(0).getText());
            assertEquals("DE-2026-9001", vlo_Tabla.getRow(2).getCell(0).getText());
            assertEquals(Constantes.TEXTO_NO_REGISTRADO_AUN, vlo_Tabla.getRow(1).getCell(14).getText());
        }
    }

    @Test
    void Exportar_FormatoNoAdmitido_RespondeMensajeDeError() throws Exception {
        this.vgo_MockMvc.perform(get(URL_BUSQUEDA + "/exportar/CSV"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(Constantes.MENSAJE_FORMATO_EXPORTACION_INVALIDO));
    }

    @Test
    void ServiciosExistentes_ListaDeDesalmacenajes_SigueRespondiendo() throws Exception {
        this.vgo_MockMvc.perform(get("/api/v1/tramites/desalmacenaje-especial"))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder ConCriteriosDeExportacion(MockHttpServletRequestBuilder pvo_Peticion) {
        return pvo_Peticion
                .param("tiposTramite", String.valueOf(this.vgn_IdDesalmacenaje))
                .param("monedas", String.valueOf(this.vgn_IdUsd));
    }

    private EntTipoTramite CrearTipo(String pvc_Codigo, String pvc_Nombre, String pvc_Prefijo) {
        EntTipoTramite vlo_Tipo = new EntTipoTramite();
        vlo_Tipo.setCodigo(pvc_Codigo);
        vlo_Tipo.setNombre(pvc_Nombre);
        vlo_Tipo.setPrefijoReferencia(pvc_Prefijo);
        vlo_Tipo.setRequiereSicop(false);
        vlo_Tipo.setRequiereOrdenCompra(false);
        vlo_Tipo.setEstado(Constantes.ESTADO_CATALOGO_ACTIVO);
        this.vgo_EntityManager.persist(vlo_Tipo);
        return vlo_Tipo;
    }

    private EntEstadoTramite CrearEstado(EntTipoTramite pvo_Tipo, String pvc_Nombre, String pvc_Categoria) {
        EntEstadoTramite vlo_Estado = new EntEstadoTramite();
        vlo_Estado.setIdTipoTramite(pvo_Tipo.getIdTipoTramite());
        vlo_Estado.setNombre(pvc_Nombre);
        vlo_Estado.setEsInicial("PEN".equals(pvc_Categoria));
        vlo_Estado.setEsFinal("FIN".equals(pvc_Categoria));
        vlo_Estado.setCategoriaEstado(pvc_Categoria);
        vlo_Estado.setOrden((short) 1);
        vlo_Estado.setEstado(Constantes.ESTADO_CATALOGO_ACTIVO);
        this.vgo_EntityManager.persist(vlo_Estado);
        return vlo_Estado;
    }

    private EntMoneda CrearMoneda(String pvc_Codigo) {
        EntMoneda vlo_Moneda = new EntMoneda();
        vlo_Moneda.setCodigoIso(pvc_Codigo);
        vlo_Moneda.setNombre(pvc_Codigo);
        vlo_Moneda.setSimbolo("$");
        vlo_Moneda.setEstado(Constantes.ESTADO_CATALOGO_ACTIVO);
        this.vgo_EntityManager.persist(vlo_Moneda);
        return vlo_Moneda;
    }

    private EntTramite CrearTramite(String pvc_Referencia, EntTipoTramite pvo_Tipo, EntEstadoTramite pvo_Estado,
                                    Integer pvn_IdUsuario, Integer pvn_IdMoneda, String pvc_Valor, String pvc_Regimen,
                                    String pvc_Aduana, LocalDateTime pvd_Fecha) {
        EntTramite vlo_Tramite = new EntTramite();
        vlo_Tramite.setReferenciaInterna(pvc_Referencia);
        vlo_Tramite.setIdTipoTramite(pvo_Tipo.getIdTipoTramite());
        vlo_Tramite.setIdEstadoTramite(pvo_Estado.getIdEstadoTramite());
        vlo_Tramite.setIdUsuarioResponsable(pvn_IdUsuario);
        vlo_Tramite.setIdMoneda(pvn_IdMoneda);
        vlo_Tramite.setValorEstimado(pvc_Valor == null ? null : new BigDecimal(pvc_Valor));
        vlo_Tramite.setRegimenAduanero(pvc_Regimen);
        vlo_Tramite.setAduanaControl(pvc_Aduana);
        vlo_Tramite.setFechaCreacion(pvd_Fecha);
        this.vgo_EntityManager.persist(vlo_Tramite);
        return vlo_Tramite;
    }

    private void CrearExoneracion(EntTramite pvo_Tramite, String pvc_Tipo) {
        EntExoneracion vlo_Exoneracion = new EntExoneracion();
        vlo_Exoneracion.setIdTramite(pvo_Tramite.getIdTramite());
        vlo_Exoneracion.setTipoExoneracion(pvc_Tipo);
        vlo_Exoneracion.setNumeroContratacion("2026LD-000045");
        vlo_Exoneracion.setOrdenCompraGeco("GECO-2026-1180");
        vlo_Exoneracion.setContratoSicop("SICOP-2026-0457");
        this.vgo_EntityManager.persist(vlo_Exoneracion);
    }
}
