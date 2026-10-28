/**
 * Autor: Equipo de Desarrollo SIM
 * Fecha: 25/10/2026
 * Descripción: Confirmación antes de pasar un expediente a un estado que lo cierra (H-09 del Sprint 2
 *   y EH5-03 del Sprint 1). Un expediente cerrado no se puede reabrir, así que la persona usuaria
 *   confirma el cambio sabiendo que no se puede deshacer. La usan las donaciones y los desalmacenajes;
 *   los trámites temporales tienen su propia ventana, que menciona el DUA de cierre.
 *
 * @param {object} propiedades
 * @param {string} propiedades.referencia referencia interna del expediente
 * @param {string} propiedades.estado estado al que pasará
 * @param {Function} propiedades.alConfirmar aplica el cambio
 * @param {Function} propiedades.alCancelar cierra la ventana sin cambiar nada
 * @param {boolean} propiedades.guardando deshabilita el botón mientras se guarda
 */
function Cmp_SIM_ConfirmarCierre({ referencia, estado, alConfirmar, alCancelar, guardando = false }) {
  return (
    <>
      <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true"
           aria-labelledby="ttlConfirmarCierre">
        <div className="modal-dialog">
          <div className="modal-content">
            <div className="modal-header">
              <h2 className="modal-title h5" id="ttlConfirmarCierre">
                <i className="bi bi-exclamation-triangle text-warning me-2" aria-hidden="true"></i>
                Cerrar el expediente
              </h2>
              <button type="button" className="btn-close" aria-label="Cerrar" onClick={alCancelar}></button>
            </div>
            <div className="modal-body">
              <p>
                El expediente <strong className="sim-referencia">{referencia}</strong> pasará a{" "}
                <strong>{estado}</strong> y quedará cerrado.
              </p>
              <p className="mb-0">
                Esta acción no se puede deshacer: un expediente cerrado no se puede reabrir. Su información
                y su historial se conservan para consulta.
              </p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={alCancelar}>Cancelar</button>
              <button type="button" className="btn btn-danger" id="btnConfirmarCierre"
                      onClick={alConfirmar} disabled={guardando}>
                Sí, cerrar el expediente
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="modal-backdrop fade show"></div>
    </>
  );
}

export default Cmp_SIM_ConfirmarCierre;
