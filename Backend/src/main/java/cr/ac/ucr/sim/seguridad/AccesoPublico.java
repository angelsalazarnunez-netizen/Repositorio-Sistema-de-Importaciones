package cr.ac.ucr.sim.seguridad;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Marca un servicio REST que se usa sin sesión (inicio de sesión, registro y validación
 *   de cuenta). Es solo una marca para la prueba CoberturaPermisosTest: el acceso público lo da
 *   CfgSeguridad.RUTAS_PUBLICAS, y la prueba verifica que ambos coincidan. Todo servicio que no
 *   tenga esta marca debe declarar su regla con @PreAuthorize.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccesoPublico {
}
