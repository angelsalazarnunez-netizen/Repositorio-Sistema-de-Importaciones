package cr.ac.ucr.sim.seguridad;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 19/10/2026
 * Descripción: Verifica que ningún servicio REST quede sin regla de acceso (HU-003). Revisa las
 *   clases con @RestController sin levantar la aplicación ni la base de datos:
 *   - Todo servicio tiene @PreAuthorize o @AccesoPublico, y no ambos.
 *   - Los servicios @AccesoPublico son exactamente las rutas de CfgSeguridad.RUTAS_PUBLICAS.
 *   - Las variables #nombre de una regla existen como parámetros del servicio (si no, la regla
 *     recibiría null).
 *   - Todas las reglas de ReglasAcceso son expresiones válidas.
 *   Si esta prueba falla al agregar un servicio, agregue su @PreAuthorize con una regla de
 *   ReglasAcceso.
 */
class CoberturaPermisosTest {

    private static final String PAQUETE_BASE = "cr.ac.ucr.sim";
    private static final Pattern VARIABLE_SPEL = Pattern.compile("#([A-Za-z_][A-Za-z_0-9]*)");

    private static final List<Servicio> SERVICIOS = new ArrayList<>();

    private record Servicio(Class<?> clase, Method metodo, List<String> rutas) {
        String Nombre() {
            return clase.getSimpleName() + "." + metodo.getName();
        }
    }

    @BeforeAll
    static void BuscarServicios() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider vlo_Buscador =
                new ClassPathScanningCandidateComponentProvider(false);
        vlo_Buscador.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        for (BeanDefinition vlo_Definicion : vlo_Buscador.findCandidateComponents(PAQUETE_BASE)) {
            Class<?> vlo_Clase = Class.forName(vlo_Definicion.getBeanClassName());
            RequestMapping vlo_MapeoClase = AnnotatedElementUtils.findMergedAnnotation(vlo_Clase, RequestMapping.class);
            String[] vlo_Bases = vlo_MapeoClase == null || vlo_MapeoClase.path().length == 0
                    ? new String[] {""}
                    : vlo_MapeoClase.path();
            for (Method vlo_Metodo : vlo_Clase.getDeclaredMethods()) {
                RequestMapping vlo_Mapeo = AnnotatedElementUtils.findMergedAnnotation(vlo_Metodo, RequestMapping.class);
                if (vlo_Mapeo == null) {
                    continue;
                }
                String[] vlo_Rutas = vlo_Mapeo.path().length == 0 ? new String[] {""} : vlo_Mapeo.path();
                List<String> vlo_RutasCompletas = new ArrayList<>();
                for (String vlc_Base : vlo_Bases) {
                    for (String vlc_Ruta : vlo_Rutas) {
                        vlo_RutasCompletas.add(vlc_Base + vlc_Ruta);
                    }
                }
                SERVICIOS.add(new Servicio(vlo_Clase, vlo_Metodo, vlo_RutasCompletas));
            }
        }
    }

    @Test
    void SeEncuentranServicios() {
        assertFalse(SERVICIOS.isEmpty(), "No se encontraron servicios REST en " + PAQUETE_BASE);
    }

    @Test
    void TodoServicioDeclaraSuRegla() {
        List<String> vlo_SinRegla = new ArrayList<>();
        List<String> vlo_ConAmbas = new ArrayList<>();
        for (Servicio vlo_Servicio : SERVICIOS) {
            boolean vlb_Protegido = ReglaDe(vlo_Servicio) != null;
            boolean vlb_Publico = vlo_Servicio.metodo().isAnnotationPresent(AccesoPublico.class);
            if (!vlb_Protegido && !vlb_Publico) {
                vlo_SinRegla.add(vlo_Servicio.Nombre() + " " + vlo_Servicio.rutas());
            }
            if (vlb_Protegido && vlb_Publico) {
                vlo_ConAmbas.add(vlo_Servicio.Nombre());
            }
        }
        assertTrue(vlo_SinRegla.isEmpty(),
                "Servicios sin @PreAuthorize ni @AccesoPublico: " + vlo_SinRegla);
        assertTrue(vlo_ConAmbas.isEmpty(),
                "Servicios con @PreAuthorize y @AccesoPublico a la vez: " + vlo_ConAmbas);
    }

    @Test
    void LosServiciosPublicosCoincidenConLasRutasPublicas() {
        Set<String> vlo_RutasMarcadas = new HashSet<>();
        for (Servicio vlo_Servicio : SERVICIOS) {
            if (vlo_Servicio.metodo().isAnnotationPresent(AccesoPublico.class)) {
                vlo_RutasMarcadas.addAll(vlo_Servicio.rutas());
            }
        }
        assertEquals(Set.copyOf(Arrays.asList(CfgSeguridad.RUTAS_PUBLICAS)), vlo_RutasMarcadas,
                "CfgSeguridad.RUTAS_PUBLICAS debe tener exactamente las rutas marcadas con @AccesoPublico");
    }

    @Test
    void LasVariablesDeLaReglaExistenEnElServicio() {
        List<String> vlo_Errores = new ArrayList<>();
        for (Servicio vlo_Servicio : SERVICIOS) {
            String vlc_Regla = ReglaDe(vlo_Servicio);
            if (vlc_Regla == null) {
                continue;
            }
            Set<String> vlo_Parametros = new HashSet<>();
            for (Parameter vlo_Parametro : vlo_Servicio.metodo().getParameters()) {
                vlo_Parametros.add(vlo_Parametro.getName());
            }
            Matcher vlo_Variable = VARIABLE_SPEL.matcher(vlc_Regla);
            while (vlo_Variable.find()) {
                if (!vlo_Parametros.contains(vlo_Variable.group(1))) {
                    vlo_Errores.add(vlo_Servicio.Nombre() + ": #" + vlo_Variable.group(1)
                            + " no es un parámetro " + vlo_Parametros);
                }
            }
        }
        assertTrue(vlo_Errores.isEmpty(), "Reglas con variables inexistentes: " + vlo_Errores);
    }

    @Test
    void LasReglasSonExpresionesValidas() throws IllegalAccessException {
        SpelExpressionParser vlo_Analizador = new SpelExpressionParser();
        for (Field vlo_Campo : ReglasAcceso.class.getDeclaredFields()) {
            if (Modifier.isPublic(vlo_Campo.getModifiers()) && vlo_Campo.getType() == String.class) {
                String vlc_Regla = (String) vlo_Campo.get(null);
                vlo_Analizador.parseExpression(vlc_Regla);
            }
        }
    }

    private static String ReglaDe(Servicio pvo_Servicio) {
        PreAuthorize vlo_Regla = AnnotatedElementUtils.findMergedAnnotation(pvo_Servicio.metodo(), PreAuthorize.class);
        if (vlo_Regla == null) {
            vlo_Regla = AnnotatedElementUtils.findMergedAnnotation(pvo_Servicio.clase(), PreAuthorize.class);
        }
        return vlo_Regla == null ? null : vlo_Regla.value();
    }
}
