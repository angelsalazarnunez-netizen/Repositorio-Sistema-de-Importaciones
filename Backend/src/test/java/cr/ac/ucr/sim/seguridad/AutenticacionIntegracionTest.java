package cr.ac.ucr.sim.seguridad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hu004;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
@Import(AutenticacionIntegracionTest.ConfiguracionEndpointPrueba.class)
class AutenticacionIntegracionTest {

    private static final String CORREO = "admin.sim@ucr.ac.cr";
    private static final String CONTRASENA = "Admin2026!";

    @Autowired
    private MockMvc vgo_MockMvc;

    @Autowired
    private JdbcTemplate vgo_JdbcTemplate;

    @Autowired
    private PasswordEncoder vgo_PasswordEncoder;

    @BeforeEach
    void PrepararBaseDeDatos() {
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIT_SESION");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMT_ROL_PERMISO");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMT_USUARIO_ROL");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMM_PERMISO");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMM_ROL");
        this.vgo_JdbcTemplate.execute("DROP TABLE IF EXISTS SIMF_USUARIO");
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMF_USUARIO (
                    ID_USUARIO INT AUTO_INCREMENT PRIMARY KEY,
                    NOMBRE_COMPLETO VARCHAR(150) NOT NULL,
                    NUMERO_CEDULA VARCHAR(20) NOT NULL,
                    FECHA_NACIMIENTO DATE NOT NULL,
                    CORREO_INSTITUCIONAL VARCHAR(120) NOT NULL UNIQUE,
                    CONTRASENA_HASH VARCHAR(255) NOT NULL,
                    ESTADO_VALIDACION CHAR(3) NOT NULL,
                    ESTADO_ROL CHAR(3) NOT NULL,
                    TOKEN_VERIFICACION VARCHAR(64),
                    TIME_STAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMM_ROL (
                    ID_ROL BIGINT AUTO_INCREMENT PRIMARY KEY,
                    NOMBRE VARCHAR(50) NOT NULL,
                    ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMM_PERMISO (
                    ID_PERMISO BIGINT AUTO_INCREMENT PRIMARY KEY,
                    CODIGO VARCHAR(60) NOT NULL,
                    ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMT_USUARIO_ROL (
                    ID_USUARIO_ROL BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ID_USUARIO INT NOT NULL,
                    ID_ROL BIGINT NOT NULL,
                    ESTADO CHAR(3) NOT NULL,
                    FECHA_FINALIZACION TIMESTAMP
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIMT_ROL_PERMISO (
                    ID_ROL_PERMISO BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ID_ROL BIGINT NOT NULL,
                    ID_PERMISO BIGINT NOT NULL,
                    ESTADO CHAR(3) NOT NULL
                )
                """);
        this.vgo_JdbcTemplate.execute("""
                CREATE TABLE SIT_SESION (
                    ID_SESION BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ID_USUARIO INT NOT NULL,
                    TOKEN_HASH VARCHAR(255) NOT NULL UNIQUE,
                    FECHA_CREACION TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FECHA_EXPIRACION TIMESTAMP NOT NULL,
                    ES_ACTIVA TINYINT NOT NULL DEFAULT 1
                )
                """);
        this.InsertarUsuario("VAL", "ASI");
        this.vgo_JdbcTemplate.update("INSERT INTO SIMM_ROL (NOMBRE, ESTADO) VALUES (?, 'ACT')", "OPERADOR");
        this.vgo_JdbcTemplate.update(
                "INSERT INTO SIMM_PERMISO (CODIGO, ESTADO) VALUES (?, 'ACT')",
                "IMPORTACION_CONSULTAR");
        this.vgo_JdbcTemplate.update(
                """
                        INSERT INTO SIMT_USUARIO_ROL (ID_USUARIO, ID_ROL, ESTADO, FECHA_FINALIZACION)
                        VALUES (1, 1, 'ACT', NULL)
                        """);
        this.vgo_JdbcTemplate.update(
                "INSERT INTO SIMT_ROL_PERMISO (ID_ROL, ID_PERMISO, ESTADO) VALUES (1, 1, 'ACT')");
    }

    @Test
    void IniciarSesion_ConCredencialesValidas_CreaSesionYPermiteAcceso() throws Exception {
        String vlc_Cuerpo = this.vgo_MockMvc.perform(post("/api/autenticacion/login")
                        .contentType(APPLICATION_JSON)
                        .content(this.CuerpoLogin(CORREO, CONTRASENA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesoConcedido").value(true))
                .andExpect(jsonPath("$.roles[0]").value("OPERADOR"))
                .andExpect(jsonPath("$.permisos[0]").value("IMPORTACION_CONSULTAR"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher vlo_Matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(vlc_Cuerpo);
        org.junit.jupiter.api.Assertions.assertTrue(vlo_Matcher.find());
        String vlc_Token = vlo_Matcher.group(1);

        this.vgo_MockMvc.perform(get("/api/test/protegido")
                        .header("Authorization", "Bearer " + vlc_Token))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(1,
                this.vgo_JdbcTemplate.queryForObject("SELECT COUNT(*) FROM SIT_SESION", Integer.class));
        org.junit.jupiter.api.Assertions.assertNotEquals(vlc_Token,
                this.vgo_JdbcTemplate.queryForObject(
                        "SELECT TOKEN_HASH FROM SIT_SESION", String.class));
    }

    @Test
    void IniciarSesion_CredencialesInvalidas_Retorna401() throws Exception {
        this.vgo_MockMvc.perform(post("/api/autenticacion/login")
                        .contentType(APPLICATION_JSON)
                        .content(this.CuerpoLogin(CORREO, "incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Credenciales inválidas."));
    }

    @Test
    void IniciarSesion_CorreoNoValidado_Retorna401() throws Exception {
        this.vgo_JdbcTemplate.update(
                "UPDATE SIMF_USUARIO SET ESTADO_VALIDACION = 'PEN' WHERE CORREO_INSTITUCIONAL = ?",
                CORREO);

        this.vgo_MockMvc.perform(post("/api/autenticacion/login")
                        .contentType(APPLICATION_JSON)
                        .content(this.CuerpoLogin(CORREO, CONTRASENA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void IniciarSesion_SinRolAsignado_Retorna403() throws Exception {
        this.vgo_JdbcTemplate.update(
                "UPDATE SIMF_USUARIO SET ESTADO_ROL = 'PEN' WHERE CORREO_INSTITUCIONAL = ?",
                CORREO);

        this.vgo_MockMvc.perform(post("/api/autenticacion/login")
                        .contentType(APPLICATION_JSON)
                        .content(this.CuerpoLogin(CORREO, CONTRASENA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void IniciarSesion_CuerpoInvalido_Retorna400ConDetalleDeCampo() throws Exception {
        this.vgo_MockMvc.perform(post("/api/autenticacion/login")
                        .contentType(APPLICATION_JSON)
                        .content(this.CuerpoLogin("no-es-correo", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correoInstitucional").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    void AccesoProtegido_SinToken_Retorna401() throws Exception {
        this.vgo_MockMvc.perform(get("/api/test/protegido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void AccesoProtegido_ConTokenInvalidoOExpirado_Retorna401() throws Exception {
        this.vgo_JdbcTemplate.update(
                """
                        INSERT INTO SIT_SESION
                            (ID_USUARIO, TOKEN_HASH, FECHA_EXPIRACION, ES_ACTIVA)
                        VALUES (?, ?, ?, 1)
                        """,
                1,
                TokenSesionUtil.CalcularHash("token-expirado"),
                Timestamp.valueOf(LocalDateTime.now().minusMinutes(1)));

        this.vgo_MockMvc.perform(get("/api/test/protegido")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
        this.vgo_MockMvc.perform(get("/api/test/protegido")
                        .header("Authorization", "Bearer token-expirado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void CerrarSesion_InvalidaElToken() throws Exception {
        String vlc_Cuerpo = this.vgo_MockMvc.perform(post("/api/autenticacion/login")
                        .contentType(APPLICATION_JSON)
                        .content(this.CuerpoLogin(CORREO, CONTRASENA)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher vlo_Matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(vlc_Cuerpo);
        org.junit.jupiter.api.Assertions.assertTrue(vlo_Matcher.find());
        String vlc_Token = vlo_Matcher.group(1);

        this.vgo_MockMvc.perform(post("/api/autenticacion/logout")
                        .header("Authorization", "Bearer " + vlc_Token))
                .andExpect(status().isNoContent());
        this.vgo_MockMvc.perform(get("/api/test/protegido")
                        .header("Authorization", "Bearer " + vlc_Token))
                .andExpect(status().isUnauthorized());
    }

    private void InsertarUsuario(String pvc_EstadoValidacion, String pvc_EstadoRol) {
        this.vgo_JdbcTemplate.update(
                """
                        INSERT INTO SIMF_USUARIO
                            (NOMBRE_COMPLETO, NUMERO_CEDULA, FECHA_NACIMIENTO,
                             CORREO_INSTITUCIONAL, CONTRASENA_HASH,
                             ESTADO_VALIDACION, ESTADO_ROL)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                "Administrador SIM",
                "102340567",
                java.sql.Date.valueOf("1990-01-01"),
                CORREO,
                this.vgo_PasswordEncoder.encode(CONTRASENA),
                pvc_EstadoValidacion,
                pvc_EstadoRol);
    }

    private String CuerpoLogin(String pvc_Correo, String pvc_Contrasena) throws Exception {
        return "{\"correoInstitucional\":\"" + pvc_Correo
                + "\",\"contrasena\":\"" + pvc_Contrasena + "\"}";
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionEndpointPrueba {

        @Bean
        EndpointProtegidoPrueba EndpointProtegidoPrueba() {
            return new EndpointProtegidoPrueba();
        }
    }

    @RestController
    static class EndpointProtegidoPrueba {

        @GetMapping("/api/test/protegido")
        String Protegido() {
            return "ok";
        }
    }
}
