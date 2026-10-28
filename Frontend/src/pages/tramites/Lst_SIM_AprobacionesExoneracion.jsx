import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import { obtenerAprobacionesPendientes } from "../../servicios/Srv_SIM_Tramites.js";

/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 05/10/2026
 * Descripción: Bandeja de solicitudes de exoneración pendientes de aprobación (HU-022). Muestra
 *   los expedientes que la técnica envió a aprobación con la documentación completa, del más
 *   antiguo al más reciente, y lleva a la pantalla donde la jefatura registra su decisión.
 *
 * Historial de cambios:
 *   05/10/2026 Equipo de Desarrollo SIM: creación de la pantalla.
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: PU-04: al abrir una solicitud se indica que se viene de la bandeja, para poder
 *     regresar a ella.
 */

const NOMBRES_TIPO = {
  COMPRA_INSTITUCIONAL: "Compra institucional",
  DONACION: "Donación",
  CLIENTE_EXTERNO: "Cliente externo con contrato",
  VEHICULOS: "Vehículos",
  MOVIM_TRASPASO: "Movimiento de traspaso",
};

const AYUDA = {
  titulo: "Ayuda: aprobaciones de exoneración",
  introduccion:
    "Esta bandeja reúne las solicitudes de exoneración que esperan la decisión de la jefatura.",
  pasos: [
    "Revise la lista: las solicitudes más antiguas aparecen primero.",
    "Presione Revisar para ver el resumen del expediente y registrar la decisión.",
  ],
  dudas: [
    {
      pregunta: "¿Por qué no aparece una solicitud?",
      respuesta:
        "Solo aparecen las que la técnica envió a aprobación desde el control documental, con toda "
        + "la documentación obligatoria recibida.",
    },
  ],
};

function formatearFechaHora(valor) {
  return valor
    ? new Date(valor).toLocaleString("es-CR", {
      day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit",
    })
    : "—";
}

function Lst_SIM_AprobacionesExoneracion() {
  const [solicitudes, setSolicitudes] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const usuario = obtenerSesion()?.nombreCompleto || "Jefatura";

  useEffect(() => {
    let vigente = true;
    obtenerAprobacionesPendientes()
      .then((respuesta) => vigente && setSolicitudes(respuesta ?? []))
      .catch((problema) => vigente && setError(problema.message))
      .finally(() => vigente && setCargando(false));
    return () => {
      vigente = false;
    };
  }, []);

  return (
    <Cmp_SIM_Marco usuario={usuario} ayuda={AYUDA}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to="/menu">Trámites</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Aprobaciones de exoneración</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <h1 className="h4 mb-1">Aprobaciones de exoneración</h1>
        <p className="small text-muted mb-0">Solicitudes pendientes de la decisión de la jefatura.</p>
      </section>

      {cargando && <p className="text-muted">Cargando las solicitudes...</p>}
      {error && <div className="alert alert-danger" role="alert">{error}</div>}

      {!cargando && !error && (
        <div className="card sim-card" id="seccionAprobacionesPendientes">
          <div className="card-header d-flex justify-content-between align-items-center">
            <span><i className="bi bi-patch-check me-2" aria-hidden="true"></i>Pendientes de aprobación</span>
            <span className="badge text-bg-light text-dark">{solicitudes.length}</span>
          </div>
          <div className="card-body">
            {solicitudes.length === 0 ? (
              <p className="text-muted small mb-0">No hay solicitudes pendientes de aprobación.</p>
            ) : (
              <div className="table-responsive">
                <table className="table align-middle mb-0">
                  <thead>
                    <tr>
                      <th scope="col">Referencia</th>
                      <th scope="col">Tipo de exoneración</th>
                      <th scope="col">Contratación</th>
                      <th scope="col">Enviada por</th>
                      <th scope="col">Fecha de envío</th>
                      <th scope="col"><span className="visually-hidden">Acciones</span></th>
                    </tr>
                  </thead>
                  <tbody>
                    {solicitudes.map((solicitud) => (
                      <tr key={solicitud.idTramite}>
                        <td className="sim-referencia">{solicitud.referenciaInterna}</td>
                        <td>{NOMBRES_TIPO[solicitud.tipoExoneracion] ?? solicitud.tipoExoneracion}</td>
                        <td>{solicitud.numeroContratacion}</td>
                        <td>{solicitud.enviadoPor ?? "—"}</td>
                        <td>{formatearFechaHora(solicitud.fechaEnvio)}</td>
                        <td className="text-end">
                          <Link className="btn btn-sim btn-sm"
                                to={`/tramites/exoneraciones/${encodeURIComponent(solicitud.referenciaInterna)}/aprobacion`}
                                state={{ idTramite: solicitud.idTramite, desdeAprobaciones: true }}>
                            Revisar
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}
    </Cmp_SIM_Marco>
  );
}

export default Lst_SIM_AprobacionesExoneracion;
