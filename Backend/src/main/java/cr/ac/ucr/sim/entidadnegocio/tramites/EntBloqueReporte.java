package cr.ac.ucr.sim.entidadnegocio.tramites;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 05/10/2026
 * Descripción: Desglose del reporte de consultas internas por un criterio de agrupación (HU-036).
 *   La persona agente puede pedir varios criterios a la vez, y cada uno se entrega como un bloque
 *   con su título y sus líneas, para que la pantalla y el archivo descargado muestren una tabla
 *   por criterio.
 */
public class EntBloqueReporte {

    private String vgc_Criterio;
    private String vgc_Titulo;
    private List<String> vgo_LsEncabezados = new ArrayList<>();
    private List<EntLineaReporte> vgo_LsLineas = new ArrayList<>();

    public EntBloqueReporte() {
    }

    public EntBloqueReporte(String pvc_Criterio, String pvc_Titulo, List<EntLineaReporte> pvo_LsLineas) {
        this.vgc_Criterio = pvc_Criterio;
        this.vgc_Titulo = pvc_Titulo;
        this.vgo_LsLineas = pvo_LsLineas;
    }

    @JsonProperty("criterio")
    public String getCriterio() {
        return this.vgc_Criterio;
    }

    public void setCriterio(String pvc_Criterio) {
        this.vgc_Criterio = pvc_Criterio;
    }

    @JsonProperty("titulo")
    public String getTitulo() {
        return this.vgc_Titulo;
    }

    public void setTitulo(String pvc_Titulo) {
        this.vgc_Titulo = pvc_Titulo;
    }

    @JsonProperty("lineas")
    public List<EntLineaReporte> getLineas() {
        return this.vgo_LsLineas;
    }

    public void setLineas(List<EntLineaReporte> pvo_LsLineas) {
        this.vgo_LsLineas = pvo_LsLineas;
    }

    /**
     * Nombres de las columnas de agrupación del bloque. Son varias cuando los criterios
     * se combinan en una sola tabla.
     */
    @JsonProperty("encabezados")
    public List<String> getEncabezados() {
        return this.vgo_LsEncabezados;
    }

    public void setEncabezados(List<String> pvo_LsEncabezados) {
        this.vgo_LsEncabezados = pvo_LsEncabezados;
    }
}
