import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_ConPermiso from "../../componentes/seguridad/Cmp_SIM_ConPermiso.jsx";
import { PERMISOS } from "../../utilerias/Permisos.js";
import { formatearFechaLocal } from "../../componentes/Cmp_SIM_CuentaRegresiva.jsx";
import { obtenerTipoTemporalPorNombre } from "../../componentes/Cmp_SIM_TramitesTemporales.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  marcarAlertaTemporalAtendida,
  obtenerAlertasTemporales,
} from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Camila Pastor Castro
 * Fecha: 03/10/2026
 * Descripción: Alertas de vencimiento de los trámites temporales (HU-027).
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la pantalla queda como bandeja de alertas del usuario de la sesión. El registro de
 *     los trámites y la extensión del plazo pasan al expediente de importación temporal (HU-025),
 *     al que se llega desde cada alerta.
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: la bandeja reúne las alertas de las importaciones y de las exportaciones
 *     temporales (HU-026): cada alerta indica su tipo y lleva al expediente que le corresponde.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 19/10/2026
 *   Descripción: restricciones por rol (HU-003): «Marcar atendida» solo se muestra con
 *     ADUANERO_TEMPORAL_MODIFICAR.
 */

const AYUDA = {
  titulo: "Ayuda: alertas de vencimiento",
  introduccion:
    "El sistema genera una alerta cuando a un trámite temporal le faltan 45 días o menos para vencer. "
    + "La reciben el responsable del expediente y los agentes de aduanas asignados, aquí y por correo.",
  pasos: [
    "Revise las alertas pendientes: están ordenadas de la más reciente a la más antigua.",
    "Presione Ver expediente para gestionar el retorno del equipo o registrar una prórroga.",
    "Cuando ya no requiera acción, presione Marcar atendida.",
  ],
  dudas: [
    {
      pregunta: "¿Qué pasa con la alerta si registro una prórroga?",
      respuesta:
        "Queda atendida automáticamente, porque se refiere al vencimiento anterior. Si el nuevo "
        + "vencimiento también se acerca, el sistema genera otra alerta.",
    },
    {
      pregunta: "¿Qué significa el estado del correo?",
      respuesta:
        "Enviado indica que el correo salió. Pendiente indica que todavía no se pudo enviar; el sistema "
        + "lo reintenta en la próxima ejecución diaria.",
    },
  ],
};

function Frm_SIM_AlertasTemporales() {
  const [alertas, setAlertas] = useState([]);
  const [soloPendientes, setSoloPendientes] = useState(true);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(true);

  const recargar = useCallback(async () => {
    setAlertas(await obtenerAlertasTemporales());
  }, []);

  useEffect(() => {
    recargar()
      .catch((problema) => setError(`No fue posible cargar las alertas. ${problema.message}`))
      .finally(() => setCargando(false));
  }, [recargar]);

  const visibles = useMemo(
    () => (soloPendientes ? alertas.filter((alerta) => alerta.estado === "PENDIENTE") : alertas),
    [alertas, soloPendientes]
  );

  async function atender(alerta) {
    setError("");
    setMensaje("");
    try {
      await marcarAlertaTemporalAtendida(alerta.idAlerta);
      setMensaje(`La alerta de ${alerta.referenciaInterna} quedó atendida.`);
      await recargar();
    } catch (problema) {
      setError(`No fue posible atender la alerta. ${problema.message}`);
    }
  }

  return (
    <Cmp_SIM_Marco usuario={obtenerSesion()?.nombreCompleto || "Usuario de la sesión"} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/menu">Menú</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Alertas de vencimiento</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <h1 className="h4 mb-1">Alertas de vencimiento</h1>
        <p className="small text-muted mb-0">
          Trámites temporales que vencen en los próximos 45 días o que ya vencieron.
        </p>
      </section>

      {mensaje && <div className="alert alert-success" role="status">{mensaje}</div>}
      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      <section className="card sim-card shadow-sm">
        <div className="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span><i className="bi bi-bell me-2" aria-hidden="true"></i>Bandeja de alertas</span>
          <div className="form-check form-switch mb-0">
            <input className="form-check-input" type="checkbox" role="switch" id="chkSoloPendientes"
                   checked={soloPendientes} onChange={(evento) => setSoloPendientes(evento.target.checked)} />
            <label className="form-check-label small" htmlFor="chkSoloPendientes">Solo pendientes</label>
          </div>
        </div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <caption className="visually-hidden">Alertas de vencimiento de trámites temporales</caption>
              <thead>
                <tr>
                  <th scope="col">Referencia</th>
                  <th scope="col">Unidad solicitante</th>
                  <th scope="col">Mercancía</th>
                  <th scope="col">Vencimiento</th>
                  <th scope="col">Estado / correo</th>
                  <th scope="col"><span className="visually-hidden">Acciones</span></th>
                </tr>
              </thead>
              <tbody>
                {cargando && <tr><td colSpan={6} className="text-muted small">Cargando alertas...</td></tr>}
                {!cargando && visibles.length === 0 && (
                  <tr>
                    <td colSpan={6} className="text-muted small">
                      {soloPendientes ? "No tiene alertas pendientes." : "Todavía no hay alertas de vencimiento."}
                    </td>
                  </tr>
                )}
                {visibles.map((alerta) => (
                  <tr key={alerta.idAlerta}>
                    <td>
                      <span className="sim-referencia">{alerta.referenciaInterna}</span>
                      <span className="d-block small text-muted">{alerta.tipoTramite}</span>
                    </td>
                    <td>{alerta.unidadSolicitante}</td>
                    <td>{alerta.descripcionMercancia}</td>
                    <td className="text-nowrap">{formatearFechaLocal(alerta.fechaVencimiento)}</td>
                    <td>
                      <span className={`badge ${alerta.estado === "PENDIENTE" ? "text-bg-warning" : "text-bg-success"}`}>
                        {alerta.estado === "PENDIENTE" ? "Pendiente" : "Atendida"}
                      </span>
                      <span className="d-block small text-muted">
                        Correo: {alerta.estadoCorreo === "ENVIADO" ? "enviado" : "pendiente"}
                      </span>
                      {alerta.indicacionVencimiento && (
                        <span className="d-block small text-danger">{alerta.indicacionVencimiento}</span>
                      )}
                    </td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <Link className="btn btn-outline-secondary"
                              to={`${obtenerTipoTemporalPorNombre(alerta.tipoTramite).ruta}/${encodeURIComponent(alerta.referenciaInterna)}`}>
                          Ver expediente
                        </Link>
                        {alerta.estado === "PENDIENTE" && (
                          <Cmp_SIM_ConPermiso permisos={[PERMISOS.ADUANERO_TEMPORAL_MODIFICAR]}>
                            <button type="button" className="btn btn-outline-primary" onClick={() => atender(alerta)}>
                              Marcar atendida
                            </button>
                          </Cmp_SIM_ConPermiso>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </Cmp_SIM_Marco>
  );
}

export default Frm_SIM_AlertasTemporales;
