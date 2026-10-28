package cr.ac.ucr.sim.utilerias;

/**
 * Descripcion: contiene las constantes de uso comun
 * del modulo de seguridad: registro y validacion de cuentas (HU-001)
 * y administracion y asignacion de roles (HU-003).
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: se separan de Constantes al integrar la rama de roles con main. La constante
 *     MENSAJE_DATOS_INCOMPLETOS de la rama pasa a llamarse MENSAJE_DATOS_REGISTRO_INCOMPLETOS
 *     porque main ya define una con ese nombre para los trámites. Se agregan los estados de rol
 *     de SIMF_USUARIO (ESTADO_ROL) que usa el inicio de sesión de main.
 */
public final class ConstantesSeguridad {

    // estados de asignacion.
    public static final String ESTADO_ACTIVO = "ACT";
    public static final String ESTADO_INACTIVO = "INA";

    // validacion de correo
    public static final String ESTADO_VALIDACION_PENDIENTE = "PEN";
    public static final String ESTADO_VALIDACION_VALIDADO = "VAL";

    // estado de asignacion de rol del usuario (columna ESTADO_ROL de SIMF_USUARIO).
    public static final String ESTADO_ROL_PENDIENTE = "PEN";
    public static final String ESTADO_ROL_ASIGNADO = "ASI";

    // correo institucional.
    public static final String DOMINIO_CORREO_INSTITUCIONAL = "@ucr.ac.cr";

    // contraseña de usuario.
    public static final int LONGITUD_MINIMA_CONTRASENA = 8;
    public static final String EXPRESION_MAYUSCULA = ".*[A-Z].*";
    public static final String EXPRESION_NUMERO = ".*[0-9].*";
    public static final String EXPRESION_CARACTER_ESPECIAL =
            ".*[^A-Za-z0-9].*";

    // regustro de usuarios
    public static final String MENSAJE_CORREO_NO_INSTITUCIONAL =
            "Solo se aceptan correos del dominio institucional de la UCR.";

    public static final String MENSAJE_CONTRASENA_INVALIDA =
            "La contraseña no cumple las políticas de seguridad: mínimo 8 caracteres, "
                    + "una letra mayúscula, un número y un carácter especial.";

    public static final String MENSAJE_CORREO_DUPLICADO =
            "Ya existe una cuenta registrada con ese correo institucional.";

    public static final String MENSAJE_CEDULA_DUPLICADA =
            "Ya existe una cuenta registrada con ese número de cédula.";

    public static final String MENSAJE_DATOS_REGISTRO_INCOMPLETOS =
            "Debe completar todos los datos del registro.";

    public static final String MENSAJE_REGISTRO_EXITOSO =
            "Registro exitoso. Se envió un correo de validación. "
                    + "La cuenta queda pendiente de validación y de asignación de rol.";

    public static final String MENSAJE_TOKEN_INVALIDO =
            "El enlace de validación no es válido o ya fue utilizado.";

    public static final String MENSAJE_CORREO_VALIDADO =
            "El correo institucional fue validado. "
                    + "La cuenta permanece pendiente de asignación de rol.";

    public static final String MENSAJE_ASIGNACION_ROL_NO_PERMITIDA =
            "El usuario no puede asignarse roles. "
                    + "La jefatura realiza esa asignación.";

    public static final String ESTADO_ASIGNACION_ROL_PENDIENTE =
            "PENDIENTE";

    public static final String ESTADO_ASIGNACION_ROL_ASIGNADO =
            "ASIGNADO";

    public static final String MENSAJE_CEDULA_INVALIDA =
            "El número de cédula debe contener 9 dígitos.";

    public static final String EXPRESION_CEDULA = "^[0-9]{9}$";

    // edad minima para registrar una cuenta. Responsable: Equipo de Desarrollo SIM. Fecha: 25/10/2026.
    public static final int EDAD_MINIMA_REGISTRO = 18;

    public static final String MENSAJE_EDAD_MINIMA =
            "Debe ser mayor de 18 años para registrarse. Revise la fecha de nacimiento.";

    // restricciones por rol (HU-003). Responsable: Equipo de Desarrollo SIM. Fecha: 19/10/2026.
    public static final String MENSAJE_SESION_REQUERIDA =
            "Debe iniciar sesión para continuar.";

    public static final String MENSAJE_ACCESO_DENEGADO =
            "No cuenta con autorización para esta operación. "
                    + "Solicite a la jefatura el rol correspondiente.";

    public static final String MENSAJE_TIPOS_TRAMITE_NO_PERMITIDOS =
            "No cuenta con autorización para consultar los tipos de trámite seleccionados.";

    /**
     * Descripcion: impide la instanciacion de la clase
     * de constantes.
     */
    private ConstantesSeguridad() {
    }
}
