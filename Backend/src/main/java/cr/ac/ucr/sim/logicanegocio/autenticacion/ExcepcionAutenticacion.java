package cr.ac.ucr.sim.logicanegocio.autenticacion;

import org.springframework.http.HttpStatus;

public class ExcepcionAutenticacion extends RuntimeException {

    private final HttpStatus vgo_EstadoHttp;

    public ExcepcionAutenticacion(HttpStatus pvo_EstadoHttp, String pvc_Mensaje) {
        super(pvc_Mensaje);
        this.vgo_EstadoHttp = pvo_EstadoHttp;
    }

    public HttpStatus getEstadoHttp() {
        return this.vgo_EstadoHttp;
    }
}
