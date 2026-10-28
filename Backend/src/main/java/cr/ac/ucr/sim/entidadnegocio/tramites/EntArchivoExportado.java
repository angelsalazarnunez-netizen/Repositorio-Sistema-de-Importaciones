package cr.ac.ucr.sim.entidadnegocio.tramites;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Archivo generado por la exportación del listado de trámites (HU-034). No representa
 *   una tabla.
 */
public class EntArchivoExportado {

    private final String vgc_NombreArchivo;
    private final String vgc_TipoContenido;
    private final byte[] vgo_Contenido;

    public EntArchivoExportado(String pvc_NombreArchivo, String pvc_TipoContenido, byte[] pvo_Contenido) {
        this.vgc_NombreArchivo = pvc_NombreArchivo;
        this.vgc_TipoContenido = pvc_TipoContenido;
        this.vgo_Contenido = pvo_Contenido;
    }

    public String getNombreArchivo() {
        return this.vgc_NombreArchivo;
    }

    public String getTipoContenido() {
        return this.vgc_TipoContenido;
    }

    public byte[] getContenido() {
        return this.vgo_Contenido;
    }
}
