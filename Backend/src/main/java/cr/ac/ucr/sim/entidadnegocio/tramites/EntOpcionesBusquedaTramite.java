package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Valores que ofrece la pantalla de búsqueda de trámites en sus filtros, junto con las
 *   columnas del listado y los formatos de exportación disponibles (HU-034). No representa una tabla.
 */
public class EntOpcionesBusquedaTramite {

    private List<EntTipoTramite> vgo_LsTiposTramite;
    private List<EntOpcionCatalogo> vgo_LsCategoriasEstado;
    private List<EntMoneda> vgo_LsMonedas;
    private List<String> vgo_LsRegimenesAduaneros;
    private List<EntOpcionCatalogo> vgo_LsTiposExoneracion;
    private List<String> vgo_LsAduanasControl;
    private List<EntColumnaBusqueda> vgo_LsColumnas;
    private List<EntOpcionCatalogo> vgo_LsFormatosExportacion;

    @JsonProperty("tiposTramite")
    public List<EntTipoTramite> getTiposTramite() {
        return this.vgo_LsTiposTramite;
    }

    public void setTiposTramite(List<EntTipoTramite> pvo_LsTiposTramite) {
        this.vgo_LsTiposTramite = pvo_LsTiposTramite;
    }

    @JsonProperty("categoriasEstado")
    public List<EntOpcionCatalogo> getCategoriasEstado() {
        return this.vgo_LsCategoriasEstado;
    }

    public void setCategoriasEstado(List<EntOpcionCatalogo> pvo_LsCategoriasEstado) {
        this.vgo_LsCategoriasEstado = pvo_LsCategoriasEstado;
    }

    @JsonProperty("monedas")
    public List<EntMoneda> getMonedas() {
        return this.vgo_LsMonedas;
    }

    public void setMonedas(List<EntMoneda> pvo_LsMonedas) {
        this.vgo_LsMonedas = pvo_LsMonedas;
    }

    @JsonProperty("regimenesAduaneros")
    public List<String> getRegimenesAduaneros() {
        return this.vgo_LsRegimenesAduaneros;
    }

    public void setRegimenesAduaneros(List<String> pvo_LsRegimenesAduaneros) {
        this.vgo_LsRegimenesAduaneros = pvo_LsRegimenesAduaneros;
    }

    @JsonProperty("tiposExoneracion")
    public List<EntOpcionCatalogo> getTiposExoneracion() {
        return this.vgo_LsTiposExoneracion;
    }

    public void setTiposExoneracion(List<EntOpcionCatalogo> pvo_LsTiposExoneracion) {
        this.vgo_LsTiposExoneracion = pvo_LsTiposExoneracion;
    }

    @JsonProperty("aduanasControl")
    public List<String> getAduanasControl() {
        return this.vgo_LsAduanasControl;
    }

    public void setAduanasControl(List<String> pvo_LsAduanasControl) {
        this.vgo_LsAduanasControl = pvo_LsAduanasControl;
    }

    @JsonProperty("columnas")
    public List<EntColumnaBusqueda> getColumnas() {
        return this.vgo_LsColumnas;
    }

    public void setColumnas(List<EntColumnaBusqueda> pvo_LsColumnas) {
        this.vgo_LsColumnas = pvo_LsColumnas;
    }

    @JsonProperty("formatosExportacion")
    public List<EntOpcionCatalogo> getFormatosExportacion() {
        return this.vgo_LsFormatosExportacion;
    }

    public void setFormatosExportacion(List<EntOpcionCatalogo> pvo_LsFormatosExportacion) {
        this.vgo_LsFormatosExportacion = pvo_LsFormatosExportacion;
    }
}
