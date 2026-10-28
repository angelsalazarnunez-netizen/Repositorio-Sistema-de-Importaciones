package cr.ac.ucr.sim.logicanegocio.seguridad;

import cr.ac.ucr.sim.accesodatos.seguridad.DalPermiso;
import cr.ac.ucr.sim.entidadnegocio.seguridad.EntPermiso;
import cr.ac.ucr.sim.utilerias.ConstantesSeguridad;
import cr.ac.ucr.sim.utilerias.Enumerados;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Descripcion: Lógica de negocio
 * para la consulta de permisos del sistema.
 *
 * Historial de cambios:
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 12/10/2026
 *   Descripción: integración con main: usa ConstantesSeguridad.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: los permisos activos se devuelven ordenados por módulo de acceso (HU-003) y por
 *     código.
 */
@Service
public class BllPermiso {

    private final DalPermiso vgo_DalPermiso;

    public BllPermiso(
            DalPermiso vlo_DalPermiso
    ) {
        this.vgo_DalPermiso = vlo_DalPermiso;
    }

    /**
     * Descripcion: obtiene los permisos activos
     * disponibles para asociar a roles.
     * @return lista de permisos activos.
     */
    @Transactional(readOnly = true)
    public List<EntPermiso> ListarPermisosActivos() {

        return vgo_DalPermiso.findAll()
                .stream()
                .filter(vlo_EnPermiso ->
                        ConstantesSeguridad.ESTADO_ACTIVO.equals(
                                vlo_EnPermiso.getEstado()
                        )
                )
                .sorted(Comparator
                        .comparingInt(BllPermiso::OrdenModulo)
                        .thenComparing(
                                EntPermiso::getCodigo,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ))
                .toList();
    }

    /**
     * Descripcion: posicion del modulo del permiso en el orden de la HU-003. Los permisos sin
     * modulo reconocido quedan al final.
     * @param vlo_EnPermiso permiso.
     * @return orden del modulo.
     */
    private static int OrdenModulo(EntPermiso vlo_EnPermiso) {

        Enumerados.ModuloPermiso vlo_Modulo =
                Enumerados.ModuloPermiso.DesdeCodigo(
                        vlo_EnPermiso.getModulo()
                );

        return vlo_Modulo != null
                ? vlo_Modulo.getOrden()
                : Integer.MAX_VALUE;
    }
}