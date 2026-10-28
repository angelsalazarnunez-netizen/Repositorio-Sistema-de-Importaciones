package cr.ac.ucr.sim.logicanegocio.autenticacion;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Excepción de reglas de negocio. La capa de lógica de negocio la lanza cuando
 *   una regla no se cumple y el manejador de excepciones la traduce a una respuesta HTTP.
 */
public class ExcepcionNegocio extends RuntimeException {

    public ExcepcionNegocio(String pvc_Mensaje) {
        super(pvc_Mensaje);
    }
}
