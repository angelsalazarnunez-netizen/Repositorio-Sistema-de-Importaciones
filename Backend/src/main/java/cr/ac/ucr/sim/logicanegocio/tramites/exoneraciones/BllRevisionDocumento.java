package cr.ac.ucr.sim.logicanegocio.tramites.exoneraciones;

import cr.ac.ucr.sim.accesodatos.tramites.DalBitacora;
import cr.ac.ucr.sim.accesodatos.tramites.DalDocumento;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalRequisitoDocumental;
import cr.ac.ucr.sim.accesodatos.tramites.exoneraciones.DalRevisionDocumento;
import cr.ac.ucr.sim.entidadnegocio.autenticacion.EntUsuario;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntBitacora;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntDocumento;
import cr.ac.ucr.sim.entidadnegocio.tramites.EntExoneracion;

import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRequisitoDocumental;
import cr.ac.ucr.sim.entidadnegocio.tramites.exoneraciones.EntRevisionDocumento;
import cr.ac.ucr.sim.logicanegocio.autenticacion.ExcepcionNegocio;
import cr.ac.ucr.sim.utilerias.Constantes;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloHistorialRevisionRespuesta;
import cr.ac.ucr.sim.utilerias.modelo.exoneraciones.ModeloRevisionDocumentoRespuesta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 03/10/2026
 * Descripcion: administra la revision documental
 * de los expedientes de exoneracion.
 */
@Service
public class BllRevisionDocumento {

    private final DalRevisionDocumento vgo_DalRevisionDocumento;
    private final DalRequisitoDocumental vgo_DalRequisitoDocumental;
    private final DalDocumento vgo_DalDocumento;
    private final DalBitacora vgo_DalBitacora;

    @PersistenceContext
    private EntityManager vgo_EntityManager;

    public BllRevisionDocumento(
            DalRevisionDocumento vlo_DalRevisionDocumento,
            DalRequisitoDocumental vlo_DalRequisitoDocumental,
            DalDocumento vlo_DalDocumento,
            DalBitacora vlo_DalBitacora
    ) {

        this.vgo_DalRevisionDocumento =
                vlo_DalRevisionDocumento;

        this.vgo_DalRequisitoDocumental =
                vlo_DalRequisitoDocumental;

        this.vgo_DalDocumento =
                vlo_DalDocumento;

        this.vgo_DalBitacora =
                vlo_DalBitacora;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: construye el checklist documental
     * aplicable a una solicitud de exoneracion.
     *
     * @param vln_IdTramite identificador del tramite.
     * @return documentos requeridos con su estado actual.
     */
    @Transactional(readOnly = true)
    public List<ModeloRevisionDocumentoRespuesta>
    ObtenerChecklistRevision(
            Integer vln_IdTramite
    ) {

        EntExoneracion vlo_EnExoneracion =
                ObtenerExoneracion(
                        vln_IdTramite
                );

        Integer vln_IdTipoTramite =
                ObtenerIdTipoTramiteExoneracion();

        List<EntRequisitoDocumental> vll_Requisitos =
                vgo_DalRequisitoDocumental
                        .ObtenerRequisitosAplicables(
                                vln_IdTipoTramite,
                                vlo_EnExoneracion.getTipoExoneracion()
                        );

        List<EntDocumento> vll_Documentos =
                vgo_DalDocumento
                        .SIMF_DOCUMENTO_ObtenerActivosPorTramite(
                                vln_IdTramite
                        );

        List<ModeloRevisionDocumentoRespuesta> vll_Respuesta =
                new ArrayList<>();

        for (EntRequisitoDocumental vlo_EnRequisito : vll_Requisitos) {

            EntDocumento vlo_EnDocumento =
                    BuscarDocumentoPorTipo(
                            vll_Documentos,
                            vlo_EnRequisito
                                    .getTipoDocumento()
                                    .getIdTipoDocumento()
                    );

            ModeloRevisionDocumentoRespuesta vlo_Modelo =
                    ConstruirElementoChecklist(
                            vlo_EnRequisito,
                            vlo_EnDocumento
                    );

            vll_Respuesta.add(
                    vlo_Modelo
            );
        }

        return vll_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: registra una nueva revision sobre
     * una version activa de un documento.
     *
     * La revision anterior se marca como inactiva
     * para conservar el historial.
     *
     * @param vln_IdDocumento documento revisado.
     * @param vln_IdUsuarioRevisor usuario responsable.
     * @param vlc_EstadoRevision resultado de revision.
     * @param vlc_Observacion observacion de la revision.
     * @return revision registrada.
     */
    @Transactional
    public EntRevisionDocumento RegistrarRevision(
            Integer vln_IdTramite,
            Integer vln_IdDocumento,
            Long vln_IdUsuarioRevisor,
            String vlc_EstadoRevision,
            String vlc_Observacion
    ){

        ValidarDatosRevision(
                vln_IdDocumento,
                vln_IdUsuarioRevisor,
                vlc_EstadoRevision,
                vlc_Observacion
        );

        EntDocumento vlo_EnDocumento =
                ObtenerDocumento(
                        vln_IdDocumento
                );

        if (!vln_IdTramite.equals(
                vlo_EnDocumento.getIdTramite()
        )) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_DOCUMENTO_INCORRECTO_EXONERACION
            );
        }

        if (!"ACT".equals(
                vlo_EnDocumento.getEstadoDocumento()
        )) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_DOCUMENTO_REVISION_NO_ACTIVO
            );
        }

        EntUsuario vlo_EnUsuario =
                ObtenerUsuario(
                        vln_IdUsuarioRevisor
                );

        Optional<EntRevisionDocumento> vlo_RevisionAnterior =
                vgo_DalRevisionDocumento
                        .ObtenerRevisionActivaPorDocumento(
                                vlo_EnDocumento
                        );

        if (vlo_RevisionAnterior.isPresent()) {

            EntRevisionDocumento vlo_EnAnterior =
                    vlo_RevisionAnterior.get();

            vlo_EnAnterior.setEstado(
                    "INA"
            );

            vgo_DalRevisionDocumento.save(
                    vlo_EnAnterior
            );
        }

        EntRevisionDocumento vlo_EnRevision =
                new EntRevisionDocumento();

        vlo_EnRevision.setDocumento(
                vlo_EnDocumento
        );

        vlo_EnRevision.setUsuarioRevisor(
                vlo_EnUsuario
        );

        vlo_EnRevision.setEstadoRevision(
                vlc_EstadoRevision
                        .trim()
                        .toUpperCase()
        );

        vlo_EnRevision.setObservacion(
                NormalizarObservacion(
                        vlc_Observacion
                )
        );

        vlo_EnRevision.setFechaHora(
                LocalDateTime.now()
        );

        vlo_EnRevision.setEstado(
                "ACT"
        );

        EntRevisionDocumento vlo_EnRevisionGuardada =
                vgo_DalRevisionDocumento.save(
                        vlo_EnRevision
                );

        RegistrarBitacoraRevision(
                vlo_EnDocumento,
                vlo_EnRevisionGuardada,
                vln_IdUsuarioRevisor,
                vlo_RevisionAnterior.orElse(null)
        );

        return vlo_EnRevisionGuardada;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: registra en la bitacora una revision
     * realizada sobre un documento del expediente.
     *
     * @param vlo_EnDocumento documento revisado.
     * @param vlo_EnRevision nueva revision registrada.
     * @param vln_IdUsuarioRevisor usuario responsable.
     * @param vlo_EnRevisionAnterior revision previa, si existia.
     */
    private void RegistrarBitacoraRevision(
            EntDocumento vlo_EnDocumento,
            EntRevisionDocumento vlo_EnRevision,
            Long vln_IdUsuarioRevisor,
            EntRevisionDocumento vlo_EnRevisionAnterior
    ) {

        EntBitacora vlo_EnBitacora =
                new EntBitacora();

        vlo_EnBitacora.setIdTramite(
                vlo_EnDocumento.getIdTramite()
        );

        vlo_EnBitacora.setIdUsuario(
                vln_IdUsuarioRevisor
        );

        vlo_EnBitacora.setAccion(
                "DOCUMENTO_REVISADO"
        );

        vlo_EnBitacora.setEntidadAfectada(
                "SIMT_REVISION_DOCUMENTO"
        );

        vlo_EnBitacora.setIdRegistroAfectado(
                vlo_EnRevision.getIdRevisionDocumento()
        );

        if (vlo_EnRevisionAnterior != null) {

            vlo_EnBitacora.setDatosAnteriores(
                    ConstruirDatosRevision(
                            vlo_EnRevisionAnterior
                    )
            );
        }

        vlo_EnBitacora.setDatosNuevos(
                ConstruirDatosRevision(
                        vlo_EnRevision
                )
        );

        vlo_EnBitacora.setEstado(
                "ACT"
        );

        vgo_DalBitacora
                .SIML_BITACORA_InsertarRegistro(
                        vlo_EnBitacora
                );
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: construye una representacion textual
     * de una revision para efectos de auditoria.
     *
     * @param vlo_EnRevision revision documental.
     * @return informacion resumida de la revision.
     */
    private String ConstruirDatosRevision(
            EntRevisionDocumento vlo_EnRevision
    ) {

        String vlc_Observacion =
                vlo_EnRevision.getObservacion() == null
                        ? ""
                        : vlo_EnRevision.getObservacion();

        return "estadoRevision="
                + vlo_EnRevision.getEstadoRevision()
                + "; observacion="
                + vlc_Observacion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 04/10/2026
     * Descripcion: obtiene el historial de revisiones
     * de un documento perteneciente a una exoneracion.
     *
     * @param vln_IdTramite identificador del tramite.
     * @param vln_IdDocumento identificador del documento.
     * @return historial de revisiones del documento.
     */
    @Transactional(readOnly = true)
    public List<ModeloHistorialRevisionRespuesta>
    ObtenerHistorial(
            Integer vln_IdTramite,
            Integer vln_IdDocumento
    ) {

        ObtenerExoneracion(
                vln_IdTramite
        );

        EntDocumento vlo_EnDocumento =
                ObtenerDocumento(
                        vln_IdDocumento
                );

        if (!vln_IdTramite.equals(
                vlo_EnDocumento.getIdTramite()
        )) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_DOCUMENTO_INCORRECTO_EXONERACION
            );
        }

        List<EntRevisionDocumento> vll_Historial =
                vgo_DalRevisionDocumento
                        .ObtenerHistorialPorDocumento(
                                vlo_EnDocumento
                        );

        List<ModeloHistorialRevisionRespuesta> vll_Respuesta =
                new ArrayList<>();

        for (EntRevisionDocumento vlo_EnRevision : vll_Historial) {

            ModeloHistorialRevisionRespuesta vlo_Modelo =
                    new ModeloHistorialRevisionRespuesta();

            vlo_Modelo.setIdRevisionDocumento(
                    vlo_EnRevision.getIdRevisionDocumento()
            );

            vlo_Modelo.setIdDocumento(
                    vlo_EnDocumento.getIdDocumento()
            );

            vlo_Modelo.setEstadoRevision(
                    vlo_EnRevision.getEstadoRevision()
            );

            vlo_Modelo.setObservacion(
                    vlo_EnRevision.getObservacion()
            );

            vlo_Modelo.setFechaRevision(
                    vlo_EnRevision.getFechaHora()
            );

            vlo_Modelo.setEstadoRegistro(
                    vlo_EnRevision.getEstado()
            );

            vlo_Modelo.setIdUsuarioRevisor(
                    vlo_EnRevision
                            .getUsuarioRevisor()
                            .getIdUsuario().longValue()
            );

            vlo_Modelo.setNombreUsuarioRevisor(
                    vlo_EnRevision
                            .getUsuarioRevisor()
                            .getNombreCompleto()
            );

            vll_Respuesta.add(
                    vlo_Modelo
            );
        }

        return vll_Respuesta;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: construye un elemento del checklist
     * a partir del requisito, documento y revision actual.
     *
     * @param vlo_EnRequisito requisito documental.
     * @param vlo_EnDocumento documento activo encontrado.
     * @return elemento del checklist.
     */
    private ModeloRevisionDocumentoRespuesta
    ConstruirElementoChecklist(
            EntRequisitoDocumental vlo_EnRequisito,
            EntDocumento vlo_EnDocumento
    ) {

        ModeloRevisionDocumentoRespuesta vlo_Modelo =
                new ModeloRevisionDocumentoRespuesta();

        vlo_Modelo.setIdTipoDocumento(
                vlo_EnRequisito
                        .getTipoDocumento()
                        .getIdTipoDocumento()
        );

        vlo_Modelo.setCodigoTipoDocumento(
                vlo_EnRequisito
                        .getTipoDocumento()
                        .getCodigo()
        );

        vlo_Modelo.setNombreTipoDocumento(
                vlo_EnRequisito
                        .getTipoDocumento()
                        .getNombre()
        );

        vlo_Modelo.setObligatorio(
                vlo_EnRequisito
                        .getObligatorio()
        );

        if (vlo_EnDocumento == null) {

            vlo_Modelo.setEstadoRevision(
                    "PENDIENTE"
            );

            return vlo_Modelo;
        }

        vlo_Modelo.setIdDocumento(
                vlo_EnDocumento.getIdDocumento()
        );

        vlo_Modelo.setNombreArchivo(
                vlo_EnDocumento.getNombreArchivo()
        );

        vlo_Modelo.setVersion(
                vlo_EnDocumento.getVersion()
        );

        Optional<EntRevisionDocumento> vlo_Revision =
                vgo_DalRevisionDocumento
                        .ObtenerRevisionActivaPorDocumento(
                                vlo_EnDocumento
                        );

        if (vlo_Revision.isEmpty()) {

            vlo_Modelo.setEstadoRevision(
                    "PENDIENTE"
            );

            return vlo_Modelo;
        }

        EntRevisionDocumento vlo_EnRevision =
                vlo_Revision.get();

        vlo_Modelo.setIdRevisionDocumento(
                vlo_EnRevision
                        .getIdRevisionDocumento()
        );

        vlo_Modelo.setEstadoRevision(
                vlo_EnRevision
                        .getEstadoRevision()
        );

        vlo_Modelo.setObservacion(
                vlo_EnRevision
                        .getObservacion()
        );

        vlo_Modelo.setFechaRevision(
                vlo_EnRevision
                        .getFechaHora()
        );

        return vlo_Modelo;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: busca un documento activo según
     * su tipo documental.
     *
     * @param vll_Documentos documentos activos.
     * @param vln_IdTipoDocumento tipo buscado.
     * @return documento encontrado o null.
     */
    private EntDocumento BuscarDocumentoPorTipo(
            List<EntDocumento> vll_Documentos,
            Integer vln_IdTipoDocumento
    ) {

        return vll_Documentos
                .stream()
                .filter(vlo_EnDocumento ->
                        vln_IdTipoDocumento.equals(
                                vlo_EnDocumento
                                        .getIdTipoDocumento()
                        )
                )
                .findFirst()
                .orElse(null);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: obtiene una solicitud de exoneracion.
     *
     * @param vln_IdTramite identificador del tramite.
     * @return exoneracion encontrada.
     */
    private EntExoneracion ObtenerExoneracion(
            Integer vln_IdTramite
    ) {

        if (vln_IdTramite == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_ID_TRAMITE_REQUERIDO
            );
        }

        EntExoneracion vlo_EnExoneracion =
                vgo_EntityManager.find(
                        EntExoneracion.class,
                        vln_IdTramite
                );

        if (vlo_EnExoneracion == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_EXONERACION_ID_NO_ENCONTRADA
            );
        }

        return vlo_EnExoneracion;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: obtiene el identificador del tipo
     * de tramite EXONERACION desde el catalogo.
     *
     * @return identificador del tipo de tramite.
     */
    private Integer ObtenerIdTipoTramiteExoneracion() {

        List<Integer> vll_Resultados =
                vgo_EntityManager
                        .createQuery(
                                """
                                SELECT t.vgn_IdTipoTramite
                                FROM EntTipoTramite t
                                WHERE t.vgc_Codigo = 'EXONERACION'
                                  AND t.vgc_Estado = 'ACT'
                                """,
                                Integer.class
                        )
                        .getResultList();

        if (vll_Resultados.isEmpty()) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_TIPO_TRAMITE_EXONERACION_NO_CONFIGURADO
            );
        }

        return vll_Resultados.get(0);
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: obtiene un documento existente.
     *
     * @param vln_IdDocumento identificador del documento.
     * @return documento encontrado.
     */
    private EntDocumento ObtenerDocumento(
            Integer vln_IdDocumento
    ) {

        if (vln_IdDocumento == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_ID_DOCUMENTO_REQUERIDO
            );
        }

        EntDocumento vlo_EnDocumento =
                vgo_EntityManager.find(
                        EntDocumento.class,
                        vln_IdDocumento
                );

        if (vlo_EnDocumento == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_DOCUMENTO_NO_ENCONTRADO
            );
        }

        return vlo_EnDocumento;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: obtiene el usuario responsable
     * de una revision.
     *
     * @param vln_IdUsuario identificador del usuario.
     * @return usuario encontrado.
     */
    private EntUsuario ObtenerUsuario(
            Long vln_IdUsuario
    ) {

        if (vln_IdUsuario == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_USUARIO_REVISOR_REQUERIDO
            );
        }

        EntUsuario vlo_EnUsuario =
                vgo_EntityManager.find(
                        EntUsuario.class,
                        vln_IdUsuario
                );

        if (vlo_EnUsuario == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_USUARIO_REVISOR_NO_ENCONTRADO
            );
        }

        return vlo_EnUsuario;
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: valida los datos de una revision.
     *
     * @param vln_IdDocumento documento revisado.
     * @param vln_IdUsuarioRevisor usuario revisor.
     * @param vlc_EstadoRevision estado seleccionado.
     * @param vlc_Observacion observacion registrada.
     */
    private void ValidarDatosRevision(
            Integer vln_IdDocumento,
            Long vln_IdUsuarioRevisor,
            String vlc_EstadoRevision,
            String vlc_Observacion
    ) {

        if (vln_IdDocumento == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_DOCUMENTO_REQUERIDO
            );
        }

        if (vln_IdUsuarioRevisor == null) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_USUARIO_REVISOR_REQUERIDO
            );
        }

        if (vlc_EstadoRevision == null
                || vlc_EstadoRevision.isBlank()) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_ESTADO_REVISION_REQUERIDO
            );
        }

        String vlc_EstadoNormalizado =
                vlc_EstadoRevision
                        .trim()
                        .toUpperCase();

        if (!vlc_EstadoNormalizado.equals("RECIBIDO")
                && !vlc_EstadoNormalizado.equals("PENDIENTE")
                && !vlc_EstadoNormalizado.equals("RECHAZADO")) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_ESTADO_REVISION_INVALIDO
            );
        }

        if (vlc_Observacion != null
                && vlc_Observacion.length() > 1000) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_OBSERVACION_REVISION_INVALIDA
            );
        }

        if ("RECHAZADO".equals(vlc_EstadoNormalizado)
                && (vlc_Observacion == null
                || vlc_Observacion.isBlank())) {

            throw new ExcepcionNegocio(
                    Constantes.MENSAJE_OBSERVACION_RECHAZO_REQUERIDA
            );
        }
    }

    /**
     * Autor: Equipo de Desarrollo SIM
     * Fecha: 03/10/2026
     * Descripcion: normaliza una observacion opcional.
     *
     * @param vlc_Observacion texto recibido.
     * @return texto limpio o null.
     */
    private String NormalizarObservacion(
            String vlc_Observacion
    ) {

        if (vlc_Observacion == null
                || vlc_Observacion.isBlank()) {

            return null;
        }

        return vlc_Observacion.trim();
    }
}