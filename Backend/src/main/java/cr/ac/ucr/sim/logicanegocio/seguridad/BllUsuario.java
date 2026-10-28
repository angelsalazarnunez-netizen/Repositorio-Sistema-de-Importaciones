package cr.ac.ucr.sim.logicanegocio.seguridad;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionAutenticacion;
import cr.ac.ucr.sim.logicanegocio.correo.CorreoService;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import cr.ac.ucr.sim.utilerias.modelo.seguridad.ModeloRegistroUsuarioSolicitud;
import cr.ac.ucr.sim.utilerias.modelo.seguridad.ModeloRegistroUsuarioRespuesta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

/**
 * Descripcion: administra la logica de negocio para
 * el registro y validacion de usuarios del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main. Usa EntUsuario y DalUsuario de main
 *     (autenticacion): el nombre y los apellidos se guardan juntos en NOMBRE_COMPLETO y la
 *     cuenta nueva queda con ESTADO_ROL pendiente. La validación ya no registra fecha porque
 *     SIMF_USUARIO no tiene esa columna. Si el correo de validación no se puede enviar, se deja
 *     constancia en el log y el registro continúa. Los errores usan ExcepcionAutenticacion.
 */
@Service
public class BllUsuario {

    private final DalUsuario vgo_DalUsuario;
    private final PasswordEncoder vgo_PasswordEncoder;
    private final CorreoService vgo_CorreoService;
    private final boolean vgb_MostrarEnlaceVerificacion;
    private final String vgc_DominioFrontend;

    public BllUsuario(
            DalUsuario vlo_DalUsuario,
            PasswordEncoder vlo_PasswordEncoder,
            CorreoService vlo_CorreoService,
            @Value("${app.mostrar-enlace-verificacion:false}")
            boolean vlb_MostrarEnlaceVerificacion,
            @Value("${app.dominio-frontend:http://localhost:5175}")
            String vlc_DominioFrontend) {

        this.vgo_DalUsuario =
                vlo_DalUsuario;

        this.vgo_PasswordEncoder =
                vlo_PasswordEncoder;

        this.vgo_CorreoService =
                vlo_CorreoService;

        this.vgb_MostrarEnlaceVerificacion =
                vlb_MostrarEnlaceVerificacion;

        this.vgc_DominioFrontend =
                vlc_DominioFrontend;
    }

    /**
     * Descripcion: registra una nueva cuenta de usuario
     * y genera el token para validar el correo institucional.
     *
     * @param vlo_Registro datos del usuario.
     * @return resultado del registro.
     */
    @Transactional
    public ModeloRegistroUsuarioRespuesta InsertarRegistro(
            ModeloRegistroUsuarioSolicitud vlo_Registro) {

        ValidarDatosCompletos(
                vlo_Registro
        );

        String vlc_Nombre =
                vlo_Registro
                        .getNombre()
                        .trim();

        String vlc_PrimerApellido =
                vlo_Registro
                        .getPrimerApellido()
                        .trim();

        String vlc_SegundoApellido =
                vlo_Registro
                        .getSegundoApellido() == null
                        ? null
                        : vlo_Registro
                        .getSegundoApellido()
                        .trim();

        String vlc_NumeroCedula =
                NormalizarCedula(
                        vlo_Registro
                                .getNumeroCedula()
                );

        String vlc_CorreoInstitucional =
                vlo_Registro
                        .getCorreoInstitucional()
                        .trim()
                        .toLowerCase();

        String vlc_Contrasena =
                vlo_Registro
                        .getContrasena();

        ValidarCedula(
                vlc_NumeroCedula
        );

        ValidarEdadMinima(
                vlo_Registro.getFechaNacimiento()
        );

        ValidarCorreoInstitucional(
                vlc_CorreoInstitucional
        );

        ValidarPoliticaContrasena(
                vlc_Contrasena
        );

        if (vgo_DalUsuario
                .SIMF_USUARIO_ExistePorCorreoInstitucional(
                        vlc_CorreoInstitucional
                )) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.CONFLICT,
                    ConstantesSeguridad.MENSAJE_CORREO_DUPLICADO
            );
        }

        if (vgo_DalUsuario
                .SIMF_USUARIO_ExistePorNumeroCedula(
                        vlc_NumeroCedula
                )) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.CONFLICT,
                    ConstantesSeguridad.MENSAJE_CEDULA_DUPLICADA
            );
        }

        String vlc_TokenVerificacion =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        String vlc_EnlaceVerificacion =
                vgc_DominioFrontend
                        + "/verificar?token="
                        + vlc_TokenVerificacion;

        EntUsuario vlo_EnUsuario =
                new EntUsuario();

        String vlc_NombreCompleto =
                ConstruirNombreCompleto(
                        vlc_Nombre,
                        vlc_PrimerApellido,
                        vlc_SegundoApellido
                );

        vlo_EnUsuario.setNombreCompleto(
                vlc_NombreCompleto
        );

        vlo_EnUsuario.setNumeroCedula(
                vlc_NumeroCedula
        );

        vlo_EnUsuario.setFechaNacimiento(
                vlo_Registro
                        .getFechaNacimiento()
        );

        vlo_EnUsuario.setCorreoInstitucional(
                vlc_CorreoInstitucional
        );

        vlo_EnUsuario.setContrasenaHash(
                vgo_PasswordEncoder.encode(
                        vlc_Contrasena
                )
        );

        vlo_EnUsuario.setEstadoValidacion(
                ConstantesSeguridad.ESTADO_VALIDACION_PENDIENTE
        );

        vlo_EnUsuario.setEstadoRol(
                ConstantesSeguridad.ESTADO_ROL_PENDIENTE
        );

        vlo_EnUsuario.setTokenVerificacion(
                vlc_TokenVerificacion
        );

        vgo_DalUsuario.SIMF_USUARIO_InsertarRegistro(
                vlo_EnUsuario
        );

        vgo_CorreoService.EnviarCorreoValidacion(
                vlc_CorreoInstitucional,
                vlc_NombreCompleto,
                vlc_EnlaceVerificacion
        );

        return ConstruirRespuestaRegistro(
                vlo_EnUsuario,
                ConstantesSeguridad.MENSAJE_REGISTRO_EXITOSO,
                vlc_EnlaceVerificacion
        );
    }

    /**
     * Descripcion: valida el correo institucional
     * mediante el token recibido.
     *
     * @param vlc_Token token de validacion.
     * @return resultado de la validacion.
     */
    @Transactional
    public ModeloRegistroUsuarioRespuesta VerificarCorreoInstitucional(
            String vlc_Token) {

        if (vlc_Token == null
                || vlc_Token.isBlank()) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    ConstantesSeguridad.MENSAJE_TOKEN_INVALIDO
            );
        }

        EntUsuario vlo_EnUsuario =
                vgo_DalUsuario
                        .SIMF_USUARIO_ObtenerPorTokenVerificacion(
                                vlc_Token.trim()
                        );

        if (vlo_EnUsuario == null) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.NOT_FOUND,
                    ConstantesSeguridad.MENSAJE_TOKEN_INVALIDO
            );
        }

        if (ConstantesSeguridad.ESTADO_VALIDACION_VALIDADO
                .equals(
                        vlo_EnUsuario
                                .getEstadoValidacion()
                )) {

            return ConstruirRespuestaRegistro(
                    vlo_EnUsuario,
                    ConstantesSeguridad.MENSAJE_CORREO_VALIDADO,
                    null
            );
        }

        vlo_EnUsuario.setEstadoValidacion(
                ConstantesSeguridad.ESTADO_VALIDACION_VALIDADO
        );

        vgo_DalUsuario.SIMF_USUARIO_ModificarRegistro(
                vlo_EnUsuario
        );

        return ConstruirRespuestaRegistro(
                vlo_EnUsuario,
                ConstantesSeguridad.MENSAJE_CORREO_VALIDADO,
                null
        );
    }

    /**
     * Descripcion: impide que un usuario se asigne
     * roles a si mismo.
     *
     * @param vln_IdUsuario identificador del usuario.
     */
    public void RechazarAsignacionDeRolPorUsuario(
            Integer vln_IdUsuario) {

        throw new ExcepcionAutenticacion(
                    HttpStatus.FORBIDDEN,
                    ConstantesSeguridad.MENSAJE_ASIGNACION_ROL_NO_PERMITIDA
            );
    }

    /**
     * Descripcion: valida que el correo pertenezca
     * al dominio institucional de la UCR.
     *
     * @param vlc_CorreoInstitucional correo a validar.
     */
    public void ValidarCorreoInstitucional(
            String vlc_CorreoInstitucional) {

        boolean vlb_EsDominioValido =
                vlc_CorreoInstitucional != null
                        && vlc_CorreoInstitucional
                        .toLowerCase()
                        .endsWith(
                                ConstantesSeguridad.DOMINIO_CORREO_INSTITUCIONAL
                        );

        if (!vlb_EsDominioValido) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    ConstantesSeguridad.MENSAJE_CORREO_NO_INSTITUCIONAL
            );
        }
    }

    /**
     * Descripcion: valida la politica de seguridad
     * establecida para la contrasena.
     *
     * @param vlc_Contrasena contrasena en texto plano.
     */
    public void ValidarPoliticaContrasena(
            String vlc_Contrasena) {

        boolean vlb_CumpleLongitud =
                vlc_Contrasena != null
                        && vlc_Contrasena.length()
                        >= ConstantesSeguridad.LONGITUD_MINIMA_CONTRASENA;

        boolean vlb_TieneMayuscula =
                vlb_CumpleLongitud
                        && vlc_Contrasena.matches(
                        ConstantesSeguridad.EXPRESION_MAYUSCULA
                );

        boolean vlb_TieneNumero =
                vlb_CumpleLongitud
                        && vlc_Contrasena.matches(
                        ConstantesSeguridad.EXPRESION_NUMERO
                );

        boolean vlb_TieneEspecial =
                vlb_CumpleLongitud
                        && vlc_Contrasena.matches(
                        ConstantesSeguridad.EXPRESION_CARACTER_ESPECIAL
                );

        if (!vlb_CumpleLongitud
                || !vlb_TieneMayuscula
                || !vlb_TieneNumero
                || !vlb_TieneEspecial) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    ConstantesSeguridad.MENSAJE_CONTRASENA_INVALIDA
            );
        }
    }

    /**
     * Descripcion: valida los campos obligatorios
     * del registro.
     *
     * @param vlo_Registro datos recibidos.
     */
    private void ValidarDatosCompletos(
            ModeloRegistroUsuarioSolicitud vlo_Registro) {

        boolean vlb_DatosIncompletos =
                vlo_Registro == null
                        || EsVacio(
                        vlo_Registro.getNombre()
                )
                        || EsVacio(
                        vlo_Registro.getPrimerApellido()
                )
                        || EsVacio(
                        vlo_Registro.getNumeroCedula()
                )
                        || vlo_Registro.getFechaNacimiento() == null
                        || EsVacio(
                        vlo_Registro.getCorreoInstitucional()
                )
                        || EsVacio(
                        vlo_Registro.getContrasena()
                );

        if (vlb_DatosIncompletos) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    ConstantesSeguridad.MENSAJE_DATOS_REGISTRO_INCOMPLETOS
            );
        }
    }

    /**
     * Responsable: Equipo de Desarrollo SIM
     * Fecha: 25/10/2026
     * Descripcion: valida que la persona tenga la edad minima para registrarse
     * (comentario de las pruebas de usabilidad: no aceptar menores de 18 anos).
     *
     * @param vld_FechaNacimiento fecha de nacimiento indicada.
     */
    private void ValidarEdadMinima(
            LocalDate vld_FechaNacimiento) {

        LocalDate vld_Hoy = LocalDate.now();

        if (vld_FechaNacimiento.isAfter(vld_Hoy)
                || Period.between(vld_FechaNacimiento, vld_Hoy).getYears()
                < ConstantesSeguridad.EDAD_MINIMA_REGISTRO) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    ConstantesSeguridad.MENSAJE_EDAD_MINIMA
            );
        }
    }

    /**
     * Descripcion: valida el formato de la cedula.
     *
     * @param vlc_NumeroCedula numero de cedula.
     */
    private void ValidarCedula(
            String vlc_NumeroCedula) {

        if (!vlc_NumeroCedula.matches(
                ConstantesSeguridad.EXPRESION_CEDULA
        )) {

            throw new ExcepcionAutenticacion(
                    HttpStatus.BAD_REQUEST,
                    ConstantesSeguridad.MENSAJE_CEDULA_INVALIDA
            );
        }
    }

    /**
     * Descripcion: elimina caracteres no numericos
     * del numero de cedula.
     *
     * @param vlc_NumeroCedula numero de cedula.
     * @return cedula normalizada.
     */
    private String NormalizarCedula(
            String vlc_NumeroCedula) {

        return vlc_NumeroCedula
                .replaceAll(
                        "[^0-9]",
                        ""
                );
    }

    /**
     * Descripcion: determina si un texto está vacio.
     *
     * @param vlc_Valor valor a evaluar.
     * @return verdadero si no contiene texto.
     */
    private boolean EsVacio(
            String vlc_Valor) {

        return vlc_Valor == null
                || vlc_Valor.isBlank();
    }

    /**
     * Descripcion: construye el nombre completo
     * de un usuario a partir de su nombre y apellidos.
     *
     * @param vlc_Nombre nombre.
     * @param vlc_PrimerApellido primer apellido.
     * @param vlc_SegundoApellido segundo apellido opcional.
     * @return nombre completo.
     */
    private String ConstruirNombreCompleto(
            String vlc_Nombre,
            String vlc_PrimerApellido,
            String vlc_SegundoApellido) {

        StringBuilder vlo_NombreCompleto =
                new StringBuilder();

        vlo_NombreCompleto
                .append(vlc_Nombre)
                .append(" ")
                .append(vlc_PrimerApellido);

        if (vlc_SegundoApellido != null
                && !vlc_SegundoApellido.isBlank()) {

            vlo_NombreCompleto
                    .append(" ")
                    .append(vlc_SegundoApellido);
        }

        return vlo_NombreCompleto.toString();
    }

    /**
     * Descripcion: construye la respuesta utilizada
     * durante el registro y verificacion del usuario.
     *
     * @param vlo_EnUsuario usuario.
     * @param vlc_Mensaje mensaje de respuesta.
     * @param vlc_EnlaceVerificacion enlace opcional.
     * @return modelo de respuesta.
     */
    private ModeloRegistroUsuarioRespuesta ConstruirRespuestaRegistro(
            EntUsuario vlo_EnUsuario,
            String vlc_Mensaje,
            String vlc_EnlaceVerificacion) {

        ModeloRegistroUsuarioRespuesta vlo_Respuesta =
                new ModeloRegistroUsuarioRespuesta();

        vlo_Respuesta.setMensaje(
                vlc_Mensaje
        );

        vlo_Respuesta.setIdUsuario(
                vlo_EnUsuario.getIdUsuario()
        );

        vlo_Respuesta.setCorreoInstitucional(
                vlo_EnUsuario.getCorreoInstitucional()
        );

        vlo_Respuesta.setEstadoValidacion(
                vlo_EnUsuario.getEstadoValidacion()
        );


        vlo_Respuesta.setEstadoAsignacionRol(
                ConstantesSeguridad.ESTADO_ROL_ASIGNADO.equals(
                        vlo_EnUsuario.getEstadoRol()
                )
                        ? ConstantesSeguridad.ESTADO_ASIGNACION_ROL_ASIGNADO
                        : ConstantesSeguridad.ESTADO_ASIGNACION_ROL_PENDIENTE
        );

        if (vgb_MostrarEnlaceVerificacion
                && vlc_EnlaceVerificacion != null) {

            vlo_Respuesta.setEnlaceVerificacion(
                    vlc_EnlaceVerificacion
            );
        }

        return vlo_Respuesta;
    }
}