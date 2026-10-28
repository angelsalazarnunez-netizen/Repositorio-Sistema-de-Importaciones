package cr.ac.ucr.sim.entidadnegocio.tramites;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 16/09/2026
 * Descripción: Criterios de la búsqueda de trámites (HU-034). No representa una tabla. Todos los
 *   criterios son opcionales y se combinan entre sí; dentro de una lista basta con que el trámite
 *   coincida con uno de los valores seleccionados.
 *   El filtro por proveedor se incorporará cuando exista el módulo de compra exterior, que es el
 *   dueño de ese dato.
 */
public class EntFiltroBusquedaTramite {

    private LocalDate vgd_FechaDesde;
    private LocalDate vgd_FechaHasta;
    private String vgc_ReferenciaInterna;
    private List<Integer> vgo_LsIdTiposTramite = new ArrayList<>();
    private List<String> vgo_LsCategoriasEstado = new ArrayList<>();
    private List<Integer> vgo_LsIdMonedas = new ArrayList<>();
    private List<String> vgo_LsRegimenesAduaneros = new ArrayList<>();
    private List<String> vgo_LsTiposExoneracion = new ArrayList<>();
    private List<String> vgo_LsAduanasControl = new ArrayList<>();

    public LocalDate getFechaDesde() {
        return this.vgd_FechaDesde;
    }

    public void setFechaDesde(LocalDate pvd_FechaDesde) {
        this.vgd_FechaDesde = pvd_FechaDesde;
    }

    public LocalDate getFechaHasta() {
        return this.vgd_FechaHasta;
    }

    public void setFechaHasta(LocalDate pvd_FechaHasta) {
        this.vgd_FechaHasta = pvd_FechaHasta;
    }

    public String getReferenciaInterna() {
        return this.vgc_ReferenciaInterna;
    }

    public void setReferenciaInterna(String pvc_ReferenciaInterna) {
        this.vgc_ReferenciaInterna = pvc_ReferenciaInterna;
    }

    public List<Integer> getIdTiposTramite() {
        return this.vgo_LsIdTiposTramite;
    }

    public void setIdTiposTramite(List<Integer> pvo_LsIdTiposTramite) {
        this.vgo_LsIdTiposTramite = pvo_LsIdTiposTramite == null ? new ArrayList<>() : pvo_LsIdTiposTramite;
    }

    public List<String> getCategoriasEstado() {
        return this.vgo_LsCategoriasEstado;
    }

    public void setCategoriasEstado(List<String> pvo_LsCategoriasEstado) {
        this.vgo_LsCategoriasEstado = pvo_LsCategoriasEstado == null ? new ArrayList<>() : pvo_LsCategoriasEstado;
    }

    public List<Integer> getIdMonedas() {
        return this.vgo_LsIdMonedas;
    }

    public void setIdMonedas(List<Integer> pvo_LsIdMonedas) {
        this.vgo_LsIdMonedas = pvo_LsIdMonedas == null ? new ArrayList<>() : pvo_LsIdMonedas;
    }

    public List<String> getRegimenesAduaneros() {
        return this.vgo_LsRegimenesAduaneros;
    }

    public void setRegimenesAduaneros(List<String> pvo_LsRegimenesAduaneros) {
        this.vgo_LsRegimenesAduaneros =
                pvo_LsRegimenesAduaneros == null ? new ArrayList<>() : pvo_LsRegimenesAduaneros;
    }

    public List<String> getTiposExoneracion() {
        return this.vgo_LsTiposExoneracion;
    }

    public void setTiposExoneracion(List<String> pvo_LsTiposExoneracion) {
        this.vgo_LsTiposExoneracion = pvo_LsTiposExoneracion == null ? new ArrayList<>() : pvo_LsTiposExoneracion;
    }

    public List<String> getAduanasControl() {
        return this.vgo_LsAduanasControl;
    }

    public void setAduanasControl(List<String> pvo_LsAduanasControl) {
        this.vgo_LsAduanasControl = pvo_LsAduanasControl == null ? new ArrayList<>() : pvo_LsAduanasControl;
    }
}
