package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.autenticacion.DalUsuario;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.DalExoneracion;
import cr.ac.ucr.sim.accesodatos.tramites.DalTramite;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalAutorizacionExonet;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntTramite;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntAutorizacionExonet;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.logicanegocio.tramites.BllBitacora;
import cr.ac.ucr.sim.logicanegocio.tramites.BllDocumento;
import cr.ac.ucr.sim.utilerias.Constantes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Reglas de negocio de las solicitudes / autorizaciones EXONET de un expediente de
 *   exoneración (HU-023).
 *   - Un expediente tiene como máximo una autorización vigente (VIG). Las anuladas (ANU) y las
 *     sustituidas (SUS) se conservan en el historial y ya no cambian.
 *   - Registrar crea una vigente cuando no existe ninguna. Se puede registrar solo la solicitud
 *     y completar después los datos de la autorización que sigan vacíos.
 *   - Completar solo llena datos vacíos de la vigente: un dato ya informado nunca se sobrescribe;
 *     para cambiarlo se registra una sustituta.
 *   - Sustituir marca la vigente como SUS (con flush) y después crea la nueva vigente enlazada
 *     mediante ID_AUTORIZACION_ANTERIOR, en una sola transacción.
 *   - Toda escritura bloquea primero el expediente y después, si existe, la autorización, siempre
 *     en ese orden, para serializar las operaciones concurrentes sobre un mismo expediente.
 *   - No cambia el estado general del trámite ni sincroniza los números EXONET antiguos de
 *     SIMT_EXONERACION (HU-018): esos solo se leen como precarga mientras no exista historial.
 *   La identidad del usuario la entrega el servicio REST desde la sesión autenticada.
 *
 * Deuda técnica:
 *   - SIML_BITACORA.ID_REGISTRO_AFECTADO es INT y la llave de la autorización es BIGINT: se
 *     convierte con Math.toIntExact, igual que en la HU-020.
 *   - Los números EXONET de SIMT_EXONERACION (HU-018) conviven con esta tabla sin sincronizarse.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la clase.
 */
@Service
public class BllAutorizacionExonet {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern(Constantes.FORMATO_FECHA_BITACORA_AUTORIZACION);

    private final DalAutorizacionExonet vgo_DalAutorizacionExonet;
    private final DalExoneracion vgo_DalExoneracion;
    private final DalTramite vgo_DalTramite;
    private final DalDocumento vgo_DalDocumento;
    private final DalUsuario vgo_DalUsuario;
    private final BllDocumento vgo_BllDocumento;
    private final BllBitacora vgo_BllBitacora;

    public BllAutorizacionExonet(
            DalAutorizacionExonet pvo_DalAutorizacionExonet,
            DalExoneracion pvo_DalExoneracion,
            DalTramite pvo_DalTramite,
            DalDocumento pvo_DalDocumento,
            DalUsuario pvo_DalUsuario,
            BllDocumento pvo_BllDocumento,
            BllBitacora pvo_BllBitacora) {
        this.vgo_DalAutorizacionExonet = pvo_DalAutorizacionExonet;
        this.vgo_DalExoneracion = pvo_DalExoneracion;
        this.vgo_DalTramite = pvo_DalTramite;
        this.vgo_DalDocumento = pvo_DalDocumento;
        this.vgo_DalUsuario = pvo_DalUsuario;
        this.vgo_BllDocumento = pvo_BllDocumento;
        this.vgo_BllBitacora = pvo_BllBitacora;
    }

    // -----------------------------------------------------------------------
    // Consultas.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene el historial completo de autorizaciones del expediente, en todos sus
     *   estados, de la más antigua a la más reciente.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @return autorizaciones del expediente
     */
    @Transactional(readOnly = true)
    public List<EntAutorizacionExonet> ObtenerHistorial(Integer pvn_IdTramite) {
        this.ValidarExoneracion(pvn_IdTramite);
        return this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerHistorialPorTramite(pvn_IdTramite);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene la autorización vigente del expediente, si existe.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @return autorización vigente o null
     */
    @Transactional(readOnly = true)
    public EntAutorizacionExonet ObtenerVigente(Integer pvn_IdTramite) {
        this.ValidarExoneracion(pvn_IdTramite);
        return this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerVigentePorTramite(
                pvn_IdTramite, Constantes.ESTADO_AUTORIZACION_VIGENTE);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene los números EXONET registrados en la HU-018 para ofrecerlos como
     *   precarga del primer registro (K-6). Solo los devuelve si el expediente todavía no tiene
     *   ninguna autorización en SIMT_AUTORIZACION_EXONET y si al menos uno de los dos números
     *   existe. El resultado es de solo lectura: la HU-023 no modifica SIMT_EXONERACION.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @return datos de exoneración con los números antiguos, o null si no aplica la precarga
     */
    @Transactional(readOnly = true)
    public EntExoneracion ObtenerPrecargaHistorica(Integer pvn_IdTramite) {
        EntExoneracion vlo_EnExoneracion = this.ValidarExoneracion(pvn_IdTramite);
        if (this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ContarPorTramite(pvn_IdTramite) > 0) {
            return null;
        }
        boolean vlb_TieneNumeros = this.NormalizarOpcional(vlo_EnExoneracion.getNumeroExonet()) != null
                || this.NormalizarOpcional(vlo_EnExoneracion.getNumeroAutorizacionExonet()) != null;
        return vlb_TieneNumeros ? vlo_EnExoneracion : null;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Resuelve el nombre de quienes registraron o cambiaron de estado cada
     *   autorización. Consulta una sola vez a cada usuario distinto.
     *
     * @param pvo_LsAutorizaciones autorizaciones cuyos usuarios se resuelven
     * @return nombre completo por identificador de usuario
     */
    @Transactional(readOnly = true)
    public Map<Integer, String> ObtenerNombresUsuarios(Collection<EntAutorizacionExonet> pvo_LsAutorizaciones) {
        Map<Integer, String> vlo_MpNombres = new HashMap<>();
        if (pvo_LsAutorizaciones == null) {
            return vlo_MpNombres;
        }
        for (EntAutorizacionExonet vlo_EnAutorizacion : pvo_LsAutorizaciones) {
            this.AgregarNombreUsuario(vlo_MpNombres, vlo_EnAutorizacion.getIdUsuarioRegistro());
            this.AgregarNombreUsuario(vlo_MpNombres, vlo_EnAutorizacion.getIdUsuarioCambioEstado());
        }
        return vlo_MpNombres;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Obtiene los documentos asociados a las autorizaciones. Consulta una sola vez
     *   cada documento distinto. Incluye documentos inactivos: forman parte del historial.
     *
     * @param pvo_LsAutorizaciones autorizaciones cuyos documentos se resuelven
     * @return documento por identificador
     */
    @Transactional(readOnly = true)
    public Map<Integer, EntDocumento> ObtenerDocumentos(Collection<EntAutorizacionExonet> pvo_LsAutorizaciones) {
        Map<Integer, EntDocumento> vlo_MpDocumentos = new HashMap<>();
        if (pvo_LsAutorizaciones == null) {
            return vlo_MpDocumentos;
        }
        for (EntAutorizacionExonet vlo_EnAutorizacion : pvo_LsAutorizaciones) {
            Integer vln_IdDocumento = vlo_EnAutorizacion.getIdDocumento();
            if (vln_IdDocumento != null && !vlo_MpDocumentos.containsKey(vln_IdDocumento)) {
                EntDocumento vlo_EnDocumento = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(vln_IdDocumento);
                if (vlo_EnDocumento != null) {
                    vlo_MpDocumentos.put(vln_IdDocumento, vlo_EnDocumento);
                }
            }
        }
        return vlo_MpDocumentos;
    }

    // -----------------------------------------------------------------------
    // Operaciones.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Registra una autorización vigente en un expediente que no tiene ninguna.
     *   Bloquea el expediente antes de comprobar que no exista otra vigente.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdUsuario usuario autenticado, que queda como responsable del registro
     * @param pvc_NumeroSolicitudExonet número de solicitud EXONET, opcional
     * @param pvd_FechaEnvioExonet fecha de envío a EXONET, opcional
     * @param pvc_NumeroAutorizacion número de autorización, opcional
     * @param pvd_FechaAutorizacion fecha de autorización, opcional
     * @param pvd_FechaComunicacion fecha de comunicación al interesado, opcional
     * @param pvc_Observaciones observaciones, opcionales
     * @param pvo_Archivo documento de respaldo, opcional
     * @return autorización registrada
     */
    @Transactional
    public EntAutorizacionExonet RegistrarAutorizacion(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuario,
            String pvc_NumeroSolicitudExonet,
            LocalDate pvd_FechaEnvioExonet,
            String pvc_NumeroAutorizacion,
            LocalDate pvd_FechaAutorizacion,
            LocalDate pvd_FechaComunicacion,
            String pvc_Observaciones,
            MultipartFile pvo_Archivo) {

        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        this.BloquearExpediente(pvn_IdTramite);

        if (this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerVigentePorTramite(
                pvn_IdTramite, Constantes.ESTADO_AUTORIZACION_VIGENTE) != null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_AUTORIZACION_VIGENTE_EXISTENTE);
        }

        EntAutorizacionExonet vlo_EnNueva = this.CrearVigente(
                pvn_IdTramite, vln_IdUsuario, LocalDateTime.now().withNano(0), null,
                pvc_NumeroSolicitudExonet, pvd_FechaEnvioExonet, pvc_NumeroAutorizacion,
                pvd_FechaAutorizacion, pvd_FechaComunicacion, pvc_Observaciones);
        this.ValidarDatos(vlo_EnNueva);

        this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_InsertarRegistro(vlo_EnNueva);
        this.AdjuntarDocumento(vlo_EnNueva, vln_IdUsuario, pvo_Archivo);

        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vln_IdUsuario,
                Constantes.ACCION_BITACORA_AUTORIZACION_REGISTRADA,
                Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET,
                this.IdBitacora(vlo_EnNueva),
                null,
                this.DescribirAutorizacion(vlo_EnNueva));
        return vlo_EnNueva;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Completa datos vacíos de la autorización vigente. Un dato ya informado no se
     *   sobrescribe: si se envía igual se ignora y si se envía distinto se rechaza la operación.
     *   El estado resultante se valida completo antes de guardar.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdAutorizacion autorización vigente que se completa
     * @param pvn_IdUsuario usuario autenticado
     * @param pvc_NumeroSolicitudExonet número de solicitud EXONET, si estaba vacío
     * @param pvd_FechaEnvioExonet fecha de envío a EXONET, si estaba vacía
     * @param pvc_NumeroAutorizacion número de autorización, si estaba vacío
     * @param pvd_FechaAutorizacion fecha de autorización, si estaba vacía
     * @param pvd_FechaComunicacion fecha de comunicación, si estaba vacía
     * @param pvc_Observaciones observaciones, si estaban vacías
     * @param pvo_Archivo documento de respaldo, si la autorización no tenía
     * @return autorización completada
     */
    @Transactional
    public EntAutorizacionExonet CompletarAutorizacion(
            Integer pvn_IdTramite,
            Long pvn_IdAutorizacion,
            Integer pvn_IdUsuario,
            String pvc_NumeroSolicitudExonet,
            LocalDate pvd_FechaEnvioExonet,
            String pvc_NumeroAutorizacion,
            LocalDate pvd_FechaAutorizacion,
            LocalDate pvd_FechaComunicacion,
            String pvc_Observaciones,
            MultipartFile pvo_Archivo) {

        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        this.BloquearExpediente(pvn_IdTramite);
        EntAutorizacionExonet vlo_EnVigente = this.ObtenerVigenteBloqueada(pvn_IdTramite, pvn_IdAutorizacion);

        // Se trabaja sobre una copia para no tocar la entidad administrada antes de validar.
        EntAutorizacionExonet vlo_EnPropuesta = this.CopiarDatos(vlo_EnVigente);
        List<String> vlo_LsAnteriores = new ArrayList<>();
        List<String> vlo_LsNuevos = new ArrayList<>();

        String vlc_NumeroSolicitud = this.NormalizarIdentificadorExonet(pvc_NumeroSolicitudExonet);
        if (this.DebeCompletar(Constantes.CAMPO_AUTORIZACION_NUMERO_SOLICITUD,
                vlo_EnVigente.getNumeroSolicitudExonet(), vlc_NumeroSolicitud)) {
            vlo_EnPropuesta.setNumeroSolicitudExonet(vlc_NumeroSolicitud);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_NUMERO_SOLICITUD, vlc_NumeroSolicitud);
        }
        if (this.DebeCompletar(Constantes.CAMPO_AUTORIZACION_FECHA_ENVIO,
                vlo_EnVigente.getFechaEnvioExonet(), pvd_FechaEnvioExonet)) {
            vlo_EnPropuesta.setFechaEnvioExonet(pvd_FechaEnvioExonet);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_FECHA_ENVIO, this.DescribirFecha(pvd_FechaEnvioExonet));
        }
        String vlc_NumeroAutorizacion = this.NormalizarIdentificadorExonet(pvc_NumeroAutorizacion);
        if (this.DebeCompletar(Constantes.CAMPO_AUTORIZACION_NUMERO_AUTORIZACION,
                vlo_EnVigente.getNumeroAutorizacion(), vlc_NumeroAutorizacion)) {
            vlo_EnPropuesta.setNumeroAutorizacion(vlc_NumeroAutorizacion);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_NUMERO_AUTORIZACION, vlc_NumeroAutorizacion);
        }
        if (this.DebeCompletar(Constantes.CAMPO_AUTORIZACION_FECHA_AUTORIZACION,
                vlo_EnVigente.getFechaAutorizacion(), pvd_FechaAutorizacion)) {
            vlo_EnPropuesta.setFechaAutorizacion(pvd_FechaAutorizacion);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_FECHA_AUTORIZACION, this.DescribirFecha(pvd_FechaAutorizacion));
        }
        if (this.DebeCompletar(Constantes.CAMPO_AUTORIZACION_FECHA_COMUNICACION,
                vlo_EnVigente.getFechaComunicacion(), pvd_FechaComunicacion)) {
            vlo_EnPropuesta.setFechaComunicacion(pvd_FechaComunicacion);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_FECHA_COMUNICACION, this.DescribirFecha(pvd_FechaComunicacion));
        }
        String vlc_Observaciones = this.NormalizarOpcional(pvc_Observaciones);
        if (this.DebeCompletar(Constantes.CAMPO_AUTORIZACION_OBSERVACIONES,
                vlo_EnVigente.getObservaciones(), vlc_Observaciones)) {
            vlo_EnPropuesta.setObservaciones(vlc_Observaciones);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_OBSERVACIONES, vlc_Observaciones);
        }
        boolean vlb_TraeArchivo = this.TraeArchivo(pvo_Archivo);
        if (vlb_TraeArchivo && vlo_EnVigente.getIdDocumento() != null) {
            throw new ExcepcionNegocio(String.format(
                    Constantes.MENSAJE_DATO_AUTORIZACION_YA_REGISTRADO, Constantes.CAMPO_AUTORIZACION_DOCUMENTO));
        }

        boolean vlb_TraeDatos = vlc_NumeroSolicitud != null
                || pvd_FechaEnvioExonet != null
                || vlc_NumeroAutorizacion != null
                || pvd_FechaAutorizacion != null
                || pvd_FechaComunicacion != null
                || vlc_Observaciones != null;

        if (vlo_LsNuevos.isEmpty() && !vlb_TraeArchivo) {
            if (vlb_TraeDatos) {
                // Idempotencia: si el cliente reenvía exactamente los mismos datos,
                // la operación se considera correcta y no genera escritura ni bitácora.
                this.ValidarDatos(vlo_EnPropuesta);
                return vlo_EnVigente;
            }
            throw new ExcepcionNegocio(Constantes.MENSAJE_AUTORIZACION_SIN_DATOS_PENDIENTES);
        }
        this.ValidarDatos(vlo_EnPropuesta);

        vlo_EnVigente.setNumeroSolicitudExonet(vlo_EnPropuesta.getNumeroSolicitudExonet());
        vlo_EnVigente.setFechaEnvioExonet(vlo_EnPropuesta.getFechaEnvioExonet());
        vlo_EnVigente.setNumeroAutorizacion(vlo_EnPropuesta.getNumeroAutorizacion());
        vlo_EnVigente.setFechaAutorizacion(vlo_EnPropuesta.getFechaAutorizacion());
        vlo_EnVigente.setFechaComunicacion(vlo_EnPropuesta.getFechaComunicacion());
        vlo_EnVigente.setObservaciones(vlo_EnPropuesta.getObservaciones());
        this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ModificarRegistro(vlo_EnVigente);

        if (vlb_TraeArchivo) {
            this.AdjuntarDocumento(vlo_EnVigente, vln_IdUsuario, pvo_Archivo);
            this.AnotarCambio(vlo_LsAnteriores, vlo_LsNuevos,
                    Constantes.CAMPO_AUTORIZACION_DOCUMENTO, this.DescribirDocumento(vlo_EnVigente.getIdDocumento()));
        }

        String vlc_Prefijo = Constantes.PREFIJO_BITACORA_AUTORIZACION + vlo_EnVigente.getIdAutorizacionExonet() + ". ";
        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vln_IdUsuario,
                Constantes.ACCION_BITACORA_AUTORIZACION_COMPLETADA,
                Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET,
                this.IdBitacora(vlo_EnVigente),
                vlc_Prefijo + String.join(" ", vlo_LsAnteriores),
                vlc_Prefijo + String.join(" ", vlo_LsNuevos));
        return vlo_EnVigente;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Anula la autorización vigente sin borrarla: conserva sus datos y registra
     *   quién la anuló, cuándo y por qué. El expediente queda sin autorización vigente.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdAutorizacion autorización vigente que se anula
     * @param pvn_IdUsuario usuario autenticado
     * @param pvc_Motivo motivo de la anulación, obligatorio
     * @return autorización anulada
     */
    @Transactional
    public EntAutorizacionExonet AnularAutorizacion(
            Integer pvn_IdTramite,
            Long pvn_IdAutorizacion,
            Integer pvn_IdUsuario,
            String pvc_Motivo) {

        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        String vlc_Motivo = this.ValidarMotivo(pvc_Motivo);
        this.BloquearExpediente(pvn_IdTramite);
        EntAutorizacionExonet vlo_EnVigente = this.ObtenerVigenteBloqueada(pvn_IdTramite, pvn_IdAutorizacion);

        String vlc_DatosAnteriores = this.DescribirAutorizacion(vlo_EnVigente);
        this.MarcarCambioEstado(vlo_EnVigente, Constantes.ESTADO_AUTORIZACION_ANULADA,
                vln_IdUsuario, LocalDateTime.now().withNano(0), vlc_Motivo);
        this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ModificarRegistro(vlo_EnVigente);

        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vln_IdUsuario,
                Constantes.ACCION_BITACORA_AUTORIZACION_ANULADA,
                Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET,
                this.IdBitacora(vlo_EnVigente),
                vlc_DatosAnteriores,
                Constantes.PREFIJO_BITACORA_AUTORIZACION + vlo_EnVigente.getIdAutorizacionExonet() + ". "
                        + Constantes.NOMBRE_ESTADO_AUTORIZACION_ANULADA + ". "
                        + Constantes.PREFIJO_BITACORA_MOTIVO_AUTORIZACION + vlc_Motivo);
        return vlo_EnVigente;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Sustituye la autorización vigente por una nueva, conservando ambas. Orden
     *   obligatorio dentro de la transacción, impuesto por el índice único de vigente:
     *   1. bloquear el expediente; 2. bloquear la vigente y validar que siga VIG y pertenezca al
     *   expediente; 3. pasar la anterior a SUS; 4. guardarla con flush; 5. crear la nueva VIG con
     *   ID_AUTORIZACION_ANTERIOR; 6. adjuntar el documento; 7. registrar la bitácora.
     *   Los datos de la nueva se validan antes del paso 3. Si algo falla, se deshace todo y los
     *   archivos escritos se eliminan.
     *
     * @param pvn_IdTramite identificador del expediente de exoneración
     * @param pvn_IdAutorizacion autorización vigente que se sustituye
     * @param pvn_IdUsuario usuario autenticado
     * @param pvc_Motivo motivo de la sustitución, obligatorio
     * @param pvc_NumeroSolicitudExonet número de solicitud EXONET de la nueva, opcional
     * @param pvd_FechaEnvioExonet fecha de envío de la nueva, opcional
     * @param pvc_NumeroAutorizacion número de autorización de la nueva, opcional
     * @param pvd_FechaAutorizacion fecha de autorización de la nueva, opcional
     * @param pvd_FechaComunicacion fecha de comunicación de la nueva, opcional
     * @param pvc_Observaciones observaciones de la nueva, opcionales
     * @param pvo_Archivo documento de respaldo de la nueva, opcional
     * @return nueva autorización vigente
     */
    @Transactional
    public EntAutorizacionExonet SustituirAutorizacion(
            Integer pvn_IdTramite,
            Long pvn_IdAutorizacion,
            Integer pvn_IdUsuario,
            String pvc_Motivo,
            String pvc_NumeroSolicitudExonet,
            LocalDate pvd_FechaEnvioExonet,
            String pvc_NumeroAutorizacion,
            LocalDate pvd_FechaAutorizacion,
            LocalDate pvd_FechaComunicacion,
            String pvc_Observaciones,
            MultipartFile pvo_Archivo) {

        Integer vln_IdUsuario = this.ValidarUsuario(pvn_IdUsuario);
        String vlc_Motivo = this.ValidarMotivo(pvc_Motivo);

        // 1 y 2. Bloqueos en el orden de la HU: expediente y después autorización.
        this.BloquearExpediente(pvn_IdTramite);
        EntAutorizacionExonet vlo_EnAnterior = this.ObtenerVigenteBloqueada(pvn_IdTramite, pvn_IdAutorizacion);

        // La misma fecha marca el cambio de la anterior y el registro de la nueva.
        LocalDateTime vld_Ahora = LocalDateTime.now().withNano(0);
        EntAutorizacionExonet vlo_EnNueva = this.CrearVigente(
                pvn_IdTramite, vln_IdUsuario, vld_Ahora, vlo_EnAnterior.getIdAutorizacionExonet(),
                pvc_NumeroSolicitudExonet, pvd_FechaEnvioExonet, pvc_NumeroAutorizacion,
                pvd_FechaAutorizacion, pvd_FechaComunicacion, pvc_Observaciones);
        this.ValidarDatos(vlo_EnNueva);
        String vlc_DatosAnteriores = this.DescribirAutorizacion(vlo_EnAnterior);

        // 3 y 4. La anterior pasa a SUS y el DAL hace flush antes de insertar la nueva vigente.
        this.MarcarCambioEstado(vlo_EnAnterior, Constantes.ESTADO_AUTORIZACION_SUSTITUIDA,
                vln_IdUsuario, vld_Ahora, vlc_Motivo);
        this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ModificarRegistro(vlo_EnAnterior);

        // 5. Nueva vigente enlazada con la anterior.
        this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_InsertarRegistro(vlo_EnNueva);

        // 6. Documento de la nueva, si se adjuntó.
        this.AdjuntarDocumento(vlo_EnNueva, vln_IdUsuario, pvo_Archivo);

        // 7. Bitácora: un movimiento por cada registro afectado.
        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vln_IdUsuario,
                Constantes.ACCION_BITACORA_AUTORIZACION_SUSTITUIDA,
                Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET,
                this.IdBitacora(vlo_EnAnterior),
                vlc_DatosAnteriores,
                Constantes.PREFIJO_BITACORA_AUTORIZACION + vlo_EnAnterior.getIdAutorizacionExonet() + ". "
                        + Constantes.NOMBRE_ESTADO_AUTORIZACION_SUSTITUIDA + " por "
                        + Constantes.PREFIJO_BITACORA_AUTORIZACION + vlo_EnNueva.getIdAutorizacionExonet() + ". "
                        + Constantes.PREFIJO_BITACORA_MOTIVO_AUTORIZACION + vlc_Motivo);
        this.vgo_BllBitacora.RegistrarAccion(
                pvn_IdTramite,
                vln_IdUsuario,
                Constantes.ACCION_BITACORA_AUTORIZACION_REGISTRADA,
                Constantes.ENTIDAD_BITACORA_AUTORIZACION_EXONET,
                this.IdBitacora(vlo_EnNueva),
                null,
                this.DescribirAutorizacion(vlo_EnNueva) + " Sustituye a "
                        + Constantes.PREFIJO_BITACORA_AUTORIZACION + vlo_EnAnterior.getIdAutorizacionExonet() + ".");
        return vlo_EnNueva;
    }

    // -----------------------------------------------------------------------
    // Bloqueos y validaciones.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Bloquea el expediente de exoneración. Es siempre el primer bloqueo de toda
     *   escritura de la HU-023.
     *
     * @param pvn_IdTramite identificador del expediente
     */
    private void BloquearExpediente(Integer pvn_IdTramite) {
        if (pvn_IdTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO);
        }
        if (this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_BloquearExpediente(pvn_IdTramite) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA);
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Bloquea la autorización indicada y comprueba que pertenezca al expediente y
     *   que siga vigente. Debe invocarse después de bloquear el expediente.
     *
     * @param pvn_IdTramite identificador del expediente
     * @param pvn_IdAutorizacion identificador de la autorización
     * @return autorización vigente bloqueada
     */
    private EntAutorizacionExonet ObtenerVigenteBloqueada(Integer pvn_IdTramite, Long pvn_IdAutorizacion) {
        if (pvn_IdAutorizacion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_AUTORIZACION_REQUERIDO);
        }
        EntAutorizacionExonet vlo_EnAutorizacion =
                this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ObtenerRegistroParaModificar(pvn_IdAutorizacion);
        if (vlo_EnAutorizacion == null || !pvn_IdTramite.equals(vlo_EnAutorizacion.getIdTramite())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_AUTORIZACION_NO_DISPONIBLE);
        }
        if (!Constantes.ESTADO_AUTORIZACION_VIGENTE.equals(vlo_EnAutorizacion.getEstadoAutorizacion())) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_AUTORIZACION_NO_VIGENTE);
        }
        return vlo_EnAutorizacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Comprueba, sin bloquear, que el expediente exista y sea una exoneración.
     *
     * @param pvn_IdTramite identificador del expediente
     * @return datos de exoneración del expediente
     */
    private EntExoneracion ValidarExoneracion(Integer pvn_IdTramite) {
        if (pvn_IdTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ID_TRAMITE_REQUERIDO);
        }
        EntExoneracion vlo_EnExoneracion = this.vgo_DalExoneracion.SIMT_EXONERACION_ObtenerRegistro(pvn_IdTramite);
        if (vlo_EnExoneracion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA);
        }
        return vlo_EnExoneracion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Comprueba que el usuario autenticado esté registrado.
     *
     * @param pvn_IdUsuario identificador del usuario autenticado
     * @return identificador validado
     */
    private Integer ValidarUsuario(Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario) == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_USUARIO_AUTORIZACION_INVALIDO);
        }
        return pvn_IdUsuario;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Normaliza y valida el motivo de una anulación o sustitución. Es obligatorio:
     *   un texto vacío, "Nulo" o "No aplica" no se acepta como motivo.
     *
     * @param pvc_Motivo motivo recibido
     * @return motivo normalizado
     */
    private String ValidarMotivo(String pvc_Motivo) {
        String vlc_Motivo = this.NormalizarOpcional(pvc_Motivo);
        if (vlc_Motivo == null || vlc_Motivo.length() > Constantes.LONGITUD_MAXIMA_MOTIVO_CAMBIO_AUTORIZACION) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_MOTIVO_CAMBIO_AUTORIZACION_INVALIDO);
        }
        return vlc_Motivo;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Valida el estado completo de una autorización antes de guardarla:
     *   longitudes, al menos un identificador EXONET, fechas no futuras, número y fecha de
     *   autorización juntos, comunicación solo con autorización y orden de los hitos.
     *
     * @param pvo_EnAutorizacion autorización con los datos que se guardarían
     */
    private void ValidarDatos(EntAutorizacionExonet pvo_EnAutorizacion) {
        String vlc_NumeroSolicitud = pvo_EnAutorizacion.getNumeroSolicitudExonet();
        String vlc_NumeroAutorizacion = pvo_EnAutorizacion.getNumeroAutorizacion();
        LocalDate vld_FechaEnvio = pvo_EnAutorizacion.getFechaEnvioExonet();
        LocalDate vld_FechaAutorizacion = pvo_EnAutorizacion.getFechaAutorizacion();
        LocalDate vld_FechaComunicacion = pvo_EnAutorizacion.getFechaComunicacion();

        if (vlc_NumeroSolicitud != null && vlc_NumeroSolicitud.length() > Constantes.LONGITUD_MAXIMA_NUMERO_EXONET) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_NUMERO_SOLICITUD_EXONET_LONGITUD);
        }
        if (vlc_NumeroAutorizacion != null
                && vlc_NumeroAutorizacion.length() > Constantes.LONGITUD_MAXIMA_NUMERO_EXONET) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_NUMERO_AUTORIZACION_LONGITUD);
        }
        if (pvo_EnAutorizacion.getObservaciones() != null
                && pvo_EnAutorizacion.getObservaciones().length()
                > Constantes.LONGITUD_MAXIMA_OBSERVACIONES_AUTORIZACION) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_OBSERVACIONES_AUTORIZACION_LONGITUD);
        }
        if (vlc_NumeroSolicitud == null && vlc_NumeroAutorizacion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_IDENTIFICADOR_EXONET_REQUERIDO);
        }
        if (vld_FechaEnvio != null && vlc_NumeroSolicitud == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_FECHA_ENVIO_SIN_SOLICITUD_EXONET);
        }

        LocalDate vld_Hoy = LocalDate.now();
        if (this.EsFutura(vld_FechaEnvio, vld_Hoy) || this.EsFutura(vld_FechaAutorizacion, vld_Hoy)
                || this.EsFutura(vld_FechaComunicacion, vld_Hoy)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_FECHA_AUTORIZACION_FUTURA);
        }
        if ((vlc_NumeroAutorizacion == null) != (vld_FechaAutorizacion == null)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_AUTORIZACION_SIN_FECHA_O_NUMERO);
        }
        if (vld_FechaComunicacion != null && vlc_NumeroAutorizacion == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_FECHA_COMUNICACION_SIN_AUTORIZACION);
        }
        if (this.EsPosterior(vld_FechaEnvio, vld_FechaAutorizacion)
                || this.EsPosterior(vld_FechaAutorizacion, vld_FechaComunicacion)
                || this.EsPosterior(vld_FechaEnvio, vld_FechaComunicacion)) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_ORDEN_FECHAS_AUTORIZACION);
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Decide si un dato de la vigente se completa. Un valor no enviado se ignora;
     *   un valor igual al ya registrado se ignora; un valor distinto de uno ya registrado se
     *   rechaza, porque completar nunca sobrescribe.
     *
     * @param pvc_Campo nombre visible del dato, para el mensaje
     * @param pvo_Actual valor registrado
     * @param pvo_Nuevo valor recibido, ya normalizado
     * @return true si el dato estaba vacío y se debe completar
     */
    private boolean DebeCompletar(String pvc_Campo, Object pvo_Actual, Object pvo_Nuevo) {
        if (pvo_Nuevo == null || Objects.equals(pvo_Actual, pvo_Nuevo)) {
            return false;
        }
        if (pvo_Actual != null) {
            throw new ExcepcionNegocio(String.format(Constantes.MENSAJE_DATO_AUTORIZACION_YA_REGISTRADO, pvc_Campo));
        }
        return true;
    }

    private boolean EsFutura(LocalDate pvd_Fecha, LocalDate pvd_Hoy) {
        return pvd_Fecha != null && pvd_Fecha.isAfter(pvd_Hoy);
    }

    private boolean EsPosterior(LocalDate pvd_Primera, LocalDate pvd_Segunda) {
        return pvd_Primera != null && pvd_Segunda != null && pvd_Primera.isAfter(pvd_Segunda);
    }

    // -----------------------------------------------------------------------
    // Construcción, documento y descripción.
    // -----------------------------------------------------------------------

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Arma una autorización vigente nueva con sus datos ya normalizados.
     *
     * @return autorización sin persistir
     */
    private EntAutorizacionExonet CrearVigente(
            Integer pvn_IdTramite,
            Integer pvn_IdUsuario,
            LocalDateTime pvd_FechaRegistro,
            Long pvn_IdAutorizacionAnterior,
            String pvc_NumeroSolicitudExonet,
            LocalDate pvd_FechaEnvioExonet,
            String pvc_NumeroAutorizacion,
            LocalDate pvd_FechaAutorizacion,
            LocalDate pvd_FechaComunicacion,
            String pvc_Observaciones) {
        EntAutorizacionExonet vlo_EnAutorizacion = new EntAutorizacionExonet();
        vlo_EnAutorizacion.setIdTramite(pvn_IdTramite);
        vlo_EnAutorizacion.setNumeroSolicitudExonet(this.NormalizarIdentificadorExonet(pvc_NumeroSolicitudExonet));
        vlo_EnAutorizacion.setFechaEnvioExonet(pvd_FechaEnvioExonet);
        vlo_EnAutorizacion.setNumeroAutorizacion(this.NormalizarIdentificadorExonet(pvc_NumeroAutorizacion));
        vlo_EnAutorizacion.setFechaAutorizacion(pvd_FechaAutorizacion);
        vlo_EnAutorizacion.setFechaComunicacion(pvd_FechaComunicacion);
        vlo_EnAutorizacion.setObservaciones(this.NormalizarOpcional(pvc_Observaciones));
        vlo_EnAutorizacion.setEstadoAutorizacion(Constantes.ESTADO_AUTORIZACION_VIGENTE);
        vlo_EnAutorizacion.setIdAutorizacionAnterior(pvn_IdAutorizacionAnterior);
        vlo_EnAutorizacion.setIdUsuarioRegistro(pvn_IdUsuario);
        vlo_EnAutorizacion.setFechaRegistro(pvd_FechaRegistro);
        return vlo_EnAutorizacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Copia en una entidad no administrada los datos de negocio de una autorización,
     *   para validar una propuesta sin modificar la original.
     *
     * @param pvo_EnOrigen autorización original
     * @return copia sin persistir
     */
    private EntAutorizacionExonet CopiarDatos(EntAutorizacionExonet pvo_EnOrigen) {
        EntAutorizacionExonet vlo_EnCopia = new EntAutorizacionExonet();
        vlo_EnCopia.setNumeroSolicitudExonet(pvo_EnOrigen.getNumeroSolicitudExonet());
        vlo_EnCopia.setFechaEnvioExonet(pvo_EnOrigen.getFechaEnvioExonet());
        vlo_EnCopia.setNumeroAutorizacion(pvo_EnOrigen.getNumeroAutorizacion());
        vlo_EnCopia.setFechaAutorizacion(pvo_EnOrigen.getFechaAutorizacion());
        vlo_EnCopia.setFechaComunicacion(pvo_EnOrigen.getFechaComunicacion());
        vlo_EnCopia.setObservaciones(pvo_EnOrigen.getObservaciones());
        return vlo_EnCopia;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Registra quién, cuándo y por qué una vigente pasó a anulada o sustituida.
     */
    private void MarcarCambioEstado(
            EntAutorizacionExonet pvo_EnAutorizacion,
            String pvc_NuevoEstado,
            Integer pvn_IdUsuario,
            LocalDateTime pvd_Fecha,
            String pvc_Motivo) {
        pvo_EnAutorizacion.setEstadoAutorizacion(pvc_NuevoEstado);
        pvo_EnAutorizacion.setIdUsuarioCambioEstado(pvn_IdUsuario);
        pvo_EnAutorizacion.setFechaCambioEstado(pvd_Fecha);
        pvo_EnAutorizacion.setMotivoCambioEstado(pvc_Motivo);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Guarda el documento de respaldo con el mecanismo común de BllDocumento, lo
     *   asocia a la autorización ya persistida y programa el borrado del archivo si la
     *   transacción termina en rollback. Si no se adjuntó archivo, no hace nada.
     *
     * @param pvo_EnAutorizacion autorización ya persistida
     * @param pvn_IdUsuario usuario que adjunta el documento
     * @param pvo_Archivo archivo recibido, opcional
     */
    private void AdjuntarDocumento(EntAutorizacionExonet pvo_EnAutorizacion, Integer pvn_IdUsuario, MultipartFile pvo_Archivo) {
        if (!this.TraeArchivo(pvo_Archivo)) {
            return;
        }
        EntTramite vlo_EnTramite = this.vgo_DalTramite.SIMT_TRAMITE_ObtenerRegistro(pvo_EnAutorizacion.getIdTramite());
        if (vlo_EnTramite == null) {
            throw new ExcepcionNegocio(Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA);
        }
        List<String> vlo_LsRutasEscritas = new ArrayList<>();
        this.vgo_BllDocumento.RegistrarLimpiezaSiFallaLaTransaccion(vlo_LsRutasEscritas);
        EntDocumento vlo_EnDocumento = this.vgo_BllDocumento.GuardarDocumento(
                vlo_EnTramite,
                pvn_IdUsuario,
                pvo_Archivo,
                Constantes.CODIGO_TIPO_DOCUMENTO_AUTORIZACION_EXONET,
                vlo_LsRutasEscritas);
        pvo_EnAutorizacion.setIdDocumento(vlo_EnDocumento.getIdDocumento());
        this.vgo_DalAutorizacionExonet.SIMT_AUTORIZACION_EXONET_ModificarRegistro(pvo_EnAutorizacion);
    }

    private boolean TraeArchivo(MultipartFile pvo_Archivo) {
        return pvo_Archivo != null && !pvo_Archivo.isEmpty();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Normaliza un texto opcional (K-12): recorta los espacios externos y convierte
     *   en NULL el texto vacío y los literales "Nulo" y "No aplica", sin distinguir mayúsculas.
     *
     * @param pvc_Texto texto recibido
     * @return texto normalizado o null
     */
    private String NormalizarOpcional(String pvc_Texto) {
        if (pvc_Texto == null) {
            return null;
        }
        String vlc_Texto = pvc_Texto.trim();
        if (vlc_Texto.isEmpty()
                || Constantes.VALOR_SIN_DATO_NULO.equalsIgnoreCase(vlc_Texto)
                || Constantes.VALOR_SIN_DATO_NO_APLICA.equalsIgnoreCase(vlc_Texto)) {
            return null;
        }
        return vlc_Texto;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Normaliza un identificador EXONET como texto opcional y, cuando existe,
     *   lo convierte a mayúsculas para que diferencias de capitalización no se interpreten
     *   como cambios de negocio.
     *
     * @param pvc_Texto identificador recibido
     * @return identificador normalizado en mayúsculas o null
     */
    private String NormalizarIdentificadorExonet(String pvc_Texto) {
        String vlc_Texto = this.NormalizarOpcional(pvc_Texto);
        return vlc_Texto == null ? null : vlc_Texto.toUpperCase(Locale.ROOT);
    }

    private void AgregarNombreUsuario(Map<Integer, String> pvo_MpNombres, Integer pvn_IdUsuario) {
        if (pvn_IdUsuario == null || pvo_MpNombres.containsKey(pvn_IdUsuario)) {
            return;
        }
        EntUsuario vlo_EnUsuario = this.vgo_DalUsuario.SIMF_USUARIO_ObtenerRegistro(pvn_IdUsuario);
        pvo_MpNombres.put(pvn_IdUsuario,
                vlo_EnUsuario == null || vlo_EnUsuario.getNombreCompleto() == null
                        ? Constantes.TEXTO_SIN_DATO
                        : vlo_EnUsuario.getNombreCompleto());
    }

    private void AnotarCambio(List<String> pvo_LsAnteriores, List<String> pvo_LsNuevos, String pvc_Campo, String pvc_Valor) {
        pvo_LsAnteriores.add(pvc_Campo + ": " + Constantes.TEXTO_SIN_DATO + ".");
        pvo_LsNuevos.add(pvc_Campo + ": " + pvc_Valor + ".");
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: Describe una autorización tal como queda en la bitácora del expediente.
     *
     * @param pvo_EnAutorizacion autorización descrita
     * @return descripción legible
     */
    private String DescribirAutorizacion(EntAutorizacionExonet pvo_EnAutorizacion) {
        StringBuilder vlo_Texto = new StringBuilder()
                .append(Constantes.PREFIJO_BITACORA_AUTORIZACION).append(pvo_EnAutorizacion.getIdAutorizacionExonet())
                .append(". ").append(Constantes.CAMPO_AUTORIZACION_NUMERO_SOLICITUD).append(": ")
                .append(this.DescribirTexto(pvo_EnAutorizacion.getNumeroSolicitudExonet()))
                .append(". ").append(Constantes.CAMPO_AUTORIZACION_FECHA_ENVIO).append(": ")
                .append(this.DescribirFecha(pvo_EnAutorizacion.getFechaEnvioExonet()))
                .append(". ").append(Constantes.CAMPO_AUTORIZACION_NUMERO_AUTORIZACION).append(": ")
                .append(this.DescribirTexto(pvo_EnAutorizacion.getNumeroAutorizacion()))
                .append(". ").append(Constantes.CAMPO_AUTORIZACION_FECHA_AUTORIZACION).append(": ")
                .append(this.DescribirFecha(pvo_EnAutorizacion.getFechaAutorizacion()))
                .append(". ").append(Constantes.CAMPO_AUTORIZACION_FECHA_COMUNICACION).append(": ")
                .append(this.DescribirFecha(pvo_EnAutorizacion.getFechaComunicacion()))
                .append(". ").append(Constantes.CAMPO_AUTORIZACION_DOCUMENTO).append(": ")
                .append(this.DescribirDocumento(pvo_EnAutorizacion.getIdDocumento()))
                .append(".");
        if (pvo_EnAutorizacion.getObservaciones() != null) {
            vlo_Texto.append(" ").append(Constantes.CAMPO_AUTORIZACION_OBSERVACIONES).append(": ")
                    .append(pvo_EnAutorizacion.getObservaciones()).append(".");
        }
        return vlo_Texto.toString();
    }

    private String DescribirTexto(String pvc_Texto) {
        return pvc_Texto == null ? Constantes.TEXTO_SIN_DATO : pvc_Texto;
    }

    private String DescribirFecha(LocalDate pvd_Fecha) {
        return pvd_Fecha == null ? Constantes.TEXTO_SIN_DATO : pvd_Fecha.format(FORMATO_FECHA);
    }

    private String DescribirDocumento(Integer pvn_IdDocumento) {
        if (pvn_IdDocumento == null) {
            return Constantes.TEXTO_SIN_DATO;
        }
        EntDocumento vlo_EnDocumento = this.vgo_DalDocumento.SIMF_DOCUMENTO_ObtenerRegistro(pvn_IdDocumento);
        return vlo_EnDocumento == null ? Constantes.TEXTO_SIN_DATO : vlo_EnDocumento.getNombreArchivo();
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 05/10/2026
     * Descripción: SIML_BITACORA.ID_REGISTRO_AFECTADO es INT y la llave de la autorización es
     *   BIGINT; se convierte igual que en la HU-020 (deuda técnica). Falla si supera el rango.
     */
    private Integer IdBitacora(EntAutorizacionExonet pvo_EnAutorizacion) {
        return Math.toIntExact(pvo_EnAutorizacion.getIdAutorizacionExonet());
    }
}
