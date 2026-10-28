package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Opciones de las pantallas de importación temporal (HU-025): sugerencias para los
 *   campos de texto, catálogos y estados del trámite.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: atiende también la exportación temporal (HU-026) y ofrece los países de destino.
 *
 * @param unidadesSolicitantes unidades ya registradas, como sugerencia
 * @param aduanasControl aduanas ya registradas en los trámites, como sugerencia
 * @param agentes agentes de aduanas disponibles
 * @param monedas monedas activas
 * @param estados estados del catálogo del tipo de trámite temporal
 * @param paises países activos, para elegir el destino de la exportación
 */
public record EntOpcionesTramiteTemporal(
        List<String> unidadesSolicitantes,
        List<String> aduanasControl,
        List<EntAgenteTemporal> agentes,
        List<EntMoneda> monedas,
        List<EntEstadoTramite> estados,
        List<EntPais> paises) {
}
