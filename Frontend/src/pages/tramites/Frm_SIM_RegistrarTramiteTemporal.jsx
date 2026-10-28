import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import Cmp_SIM_Marco from "../../componentes/Cmp_SIM_Marco.jsx";
import Cmp_SIM_Documentos from "../../componentes/Cmp_SIM_Documentos.jsx";
import { Cmp_SIM_AyudaCampo } from "../../componentes/Cmp_SIM_Ayuda.jsx";
import { fechaHoyLocal } from "../../componentes/Cmp_SIM_CuentaRegresiva.jsx";
import {
  MESES_PLAZO_ESTANDAR_EXPORTACION,
  NOMBRES_ESTADO_MERCANCIA,
  TIPOS_TRAMITE_TEMPORAL,
  capitalizar,
  crearAyudaRegistro,
  sumarMeses,
} from "../../componentes/Cmp_SIM_TramitesTemporales.js";
import { obtenerSesion } from "../../servicios/Srv_SIM_Autenticacion.js";
import {
  obtenerOpcionesTramiteTemporal,
  registrarTramiteTemporal,
} from "../../servicios/Srv_SIM_Tramites.js";
import { useVistaAlAviso } from "../../componentes/Cmp_SIM_Aviso.js";

/**
 * Autor: Josue Guevara Lostalo
 * Fecha: 04/10/2026
 * Descripción: Registro de una importación temporal (HU-025). Solicita los datos de la solicitud
 *   de la unidad y, si ya se tienen, los de la gestión ante la aduana y el vencimiento autorizado.
 *   Crea el expediente en estado Por llegar; los datos de la aduana se completan después desde el
 *   expediente. Ningún documento es obligatorio al registrar.
 *
 * Historial de cambios:
 *   Responsable: Josue Guevara Lostalo
 *   Fecha: 04/10/2026
 *   Descripción: registra también la exportación temporal (HU-026), que pide además la garantía,
 *     el peso, el país de destino y el antecedente, el oficio de notificación al usuario y la
 *     notificación del DUA de salida, y propone el vencimiento estándar (fecha del DUA más 6
 *     meses). El tipo llega por la ruta; antes se llamaba Frm_SIM_RegistrarImportacionTemporal.
 *
 * @param {object} props
 * @param {string} props.tipo importacion o exportacion
 *
 *   Responsable: Equipo de Desarrollo SIM
 *   Fecha: 25/10/2026
 *   Descripción: descripción y leyenda de obligatorios (PU-08); el aviso lleva la vista hasta el
 *     mensaje (H-10).
 */

const LONGITUD_MAXIMA_OBSERVACIONES = 4000;

function crearFormularioVacio(configuracion) {
  return {
    referenciaInterna: "",
    unidadSolicitante: "",
    oficioSolicitud: "",
    fechaRecepcion: "",
    descripcionMercancia: "",
    estadoMercancia: "",
    moneda: configuracion.monedaPorDefecto,
    valorEquipo: "",
    aduanaControl: "",
    numeroFactura: "",
    oficioAduana: "",
    numeroGestion: "",
    resolucionAutorizacion: "",
    numeroDuaInicio: "",
    fechaDuaInicio: "",
    fechaVencimiento: "",
    observaciones: "",
    agentesResponsables: [],
    // Solo los registra la exportación temporal; vacíos no viajan al servidor.
    antecedente: "",
    enGarantia: "",
    pesoKg: "",
    idPaisDestino: "",
    oficioNotificacionUsuario: "",
    notificacionDuaAduana: "",
  };
}

function Frm_SIM_RegistrarTramiteTemporal({ tipo }) {
  const configuracion = TIPOS_TRAMITE_TEMPORAL[tipo];
  const esExportacion = configuracion.esExportacion;
  const documentosRegistro = configuracion.documentos
    .filter((documento) => documento.alRegistrar)
    .map((documento) => ({ ...documento, obligatorio: false }));
  const archivosVacios = Object.fromEntries(documentosRegistro.map((documento) => [documento.codigo, []]));

  const [formulario, setFormulario] = useState(() => crearFormularioVacio(configuracion));
  const [archivos, setArchivos] = useState(archivosVacios);
  // Cambia al limpiar para que los campos de archivo se vuelvan a crear vacíos.
  const [versionArchivos, setVersionArchivos] = useState(0);
  // Último vencimiento que propuso la pantalla: si la persona no lo cambió, se actualiza con el DUA.
  const [vencimientoSugerido, setVencimientoSugerido] = useState("");
  const [opciones, setOpciones] = useState(null);
  const [alerta, setAlerta] = useState(null);
  const refAviso = useVistaAlAviso(alerta);
  const [faltantes, setFaltantes] = useState([]);
  const [respuesta, setRespuesta] = useState(null);
  const [enviando, setEnviando] = useState(false);

  const usuarioActual = obtenerSesion()?.nombreCompleto || "Usuario de la sesión";
  const hoy = fechaHoyLocal();

  useEffect(() => {
    obtenerOpcionesTramiteTemporal(configuracion.servicio)
      .then(setOpciones)
      .catch((problema) => setAlerta({
        tipo: "warning",
        texto: `No fue posible cargar las sugerencias del formulario. ${problema.message}`,
      }));
  }, [configuracion.servicio]);

  function cambiar(campo, valor) {
    setFormulario((anterior) => ({ ...anterior, [campo]: valor }));
  }

  /** En la exportación, la fecha del DUA propone el vencimiento estándar si no se escribió otro. */
  function cambiarFechaDua(valor) {
    if (!esExportacion) {
      cambiar("fechaDuaInicio", valor);
      return;
    }
    const sugerido = sumarMeses(valor, MESES_PLAZO_ESTANDAR_EXPORTACION);
    setFormulario((anterior) => ({
      ...anterior,
      fechaDuaInicio: valor,
      fechaVencimiento: !anterior.fechaVencimiento || anterior.fechaVencimiento === vencimientoSugerido
        ? sugerido
        : anterior.fechaVencimiento,
    }));
    setVencimientoSugerido(sugerido);
  }

  function alternarAgente(idUsuario, marcado) {
    setFormulario((anterior) => ({
      ...anterior,
      agentesResponsables: marcado
        ? [...anterior.agentesResponsables, idUsuario]
        : anterior.agentesResponsables.filter((id) => id !== idUsuario),
    }));
  }

  /** Revisa lo obligatorio y devuelve, en lenguaje claro, lo que falta. */
  function revisarDatos() {
    const pendientes = [];
    if (!formulario.referenciaInterna.trim()) {
      pendientes.push(`la referencia, después de la sigla ${configuracion.sigla}`);
    }
    if (!formulario.unidadSolicitante.trim()) {
      pendientes.push("la unidad solicitante");
    }
    if (!formulario.oficioSolicitud.trim()) {
      pendientes.push("el oficio del usuario solicitante");
    }
    if (!formulario.fechaRecepcion) {
      pendientes.push("la fecha de recepción del oficio");
    }
    if (!formulario.descripcionMercancia.trim()) {
      pendientes.push("la descripción de la mercancía");
    }
    if (!formulario.estadoMercancia) {
      pendientes.push("el estado de la mercancía");
    }
    if (!formulario.valorEquipo || Number(formulario.valorEquipo) <= 0) {
      pendientes.push("el valor declarado del equipo, mayor que cero");
    }
    if (esExportacion) {
      if (formulario.enGarantia === "") {
        pendientes.push("si el equipo está en periodo de garantía");
      }
      if (!formulario.pesoKg || Number(formulario.pesoKg) <= 0) {
        pendientes.push("el peso del equipo en kilogramos, mayor que cero");
      }
      if (!formulario.idPaisDestino) {
        pendientes.push("el país de destino");
      }
    }
    if (formulario.fechaDuaInicio && formulario.fechaVencimiento
        && formulario.fechaVencimiento <= formulario.fechaDuaInicio) {
      pendientes.push(`una fecha de vencimiento posterior a la fecha del ${configuracion.duaInicio.nombre}`);
    }
    return pendientes;
  }

  function limpiar() {
    setFormulario(crearFormularioVacio(configuracion));
    setArchivos(archivosVacios);
    setVersionArchivos((version) => version + 1);
    setVencimientoSugerido("");
    setFaltantes([]);
    setAlerta(null);
  }

  async function registrar(evento) {
    evento.preventDefault();
    setAlerta(null);

    const pendientes = revisarDatos();
    setFaltantes(pendientes);
    if (pendientes.length > 0) {
      setAlerta({ tipo: "danger", texto: "Falta completar la información que se indica abajo." });
      return;
    }

    setEnviando(true);
    try {
      const resultado = await registrarTramiteTemporal(
        configuracion.servicio,
        { ...formulario, referenciaInterna: configuracion.sigla + formulario.referenciaInterna.trim() },
        archivos
      );
      setRespuesta(resultado);
      limpiar();
    } catch (problema) {
      setAlerta({ tipo: "danger", texto: `No fue posible registrar la ${configuracion.singular}. ${problema.message}` });
    } finally {
      setEnviando(false);
    }
  }

  const agentes = opciones?.agentes ?? [];
  const nombreDuaInicio = configuracion.duaInicio.nombre;

  return (
    <Cmp_SIM_Marco usuario={usuarioActual} ayuda={crearAyudaRegistro(configuracion)}>
      <nav aria-label="Ruta de navegación">
        <ol className="breadcrumb small">
          <li className="breadcrumb-item"><Link to={configuracion.ruta}>{configuracion.titulo}</Link></li>
          <li className="breadcrumb-item active" aria-current="page">Nuevo trámite</li>
        </ol>
      </nav>

      <section className="sim-encabezado p-3 mb-4 shadow-sm">
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2">
          <h1 className="h4 mb-0">Registrar {configuracion.singular}</h1>
          <div className="text-md-end">
            <span className="d-block text-muted small">Estado inicial</span>
            <span className="badge text-bg-warning fs-6">{configuracion.estadoInicial}</span>
          </div>
        </div>
        <p className="sim-descripcion-pantalla mt-2" id="lblDescripcionPantalla">
          Registre el trámite con sus datos aduaneros, sus fechas y su documentación. El sistema calcula el vencimiento y avisa cuando se acerca.
        </p>
        {/* PU-08: leyenda de los campos obligatorios. */}
        <p className="small text-muted mb-0 mt-1" id="lblLeyendaObligatorios">
          Los campos marcados con <span className="text-danger fw-bold">*</span> son obligatorios.
        </p>
      </section>

      {alerta && (
        <div className={`alert alert-${alerta.tipo}`} role="alert" tabIndex={-1} ref={refAviso}>
          {alerta.texto}
          {faltantes.length > 0 && (
            <ul className="mb-0 mt-2">
              {faltantes.map((falta) => <li key={falta}>Indique {falta}.</li>)}
            </ul>
          )}
        </div>
      )}

      {/* Sugerencias de los campos de texto: valores ya registrados en otros trámites. */}
      <datalist id="lstUnidadesSolicitantes">
        {(opciones?.unidadesSolicitantes ?? []).map((unidad) => <option value={unidad} key={unidad} />)}
      </datalist>
      <datalist id="lstAduanasControl">
        {(opciones?.aduanasControl ?? []).map((aduana) => <option value={aduana} key={aduana} />)}
      </datalist>

      <form onSubmit={registrar} noValidate>
        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-file-earmark-text me-2" aria-hidden="true"></i>
            Solicitud de la unidad
          </div>
          <div className="card-body">
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtReferenciaInterna">
                  Referencia
                  <Cmp_SIM_AyudaCampo titulo="la referencia">
                    La pantalla agrega la sigla <strong>{configuracion.sigla}</strong>. Escriba el consecutivo y el
                    año, por ejemplo 001-26. La referencia no se puede repetir en otro trámite.
                  </Cmp_SIM_AyudaCampo>
                </label>
                <div className="input-group">
                  <span className="input-group-text sim-referencia">{configuracion.sigla}</span>
                  <input type="text" className="form-control sim-referencia" id="txtReferenciaInterna"
                         maxLength={45 - configuracion.sigla.length} placeholder="001-26"
                         value={formulario.referenciaInterna}
                         onChange={(evento) => cambiar("referenciaInterna", evento.target.value.toUpperCase())} />
                </div>
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtUnidadSolicitante">Unidad solicitante</label>
                <input type="text" className="form-control" id="txtUnidadSolicitante" maxLength={120}
                       list="lstUnidadesSolicitantes" placeholder={configuracion.ejemplos.unidad}
                       value={formulario.unidadSolicitante}
                       onChange={(evento) => cambiar("unidadSolicitante", evento.target.value)} />
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="txtOficioSolicitud">
                  Oficio del usuario solicitante
                </label>
                <input type="text" className="form-control" id="txtOficioSolicitud" maxLength={80}
                       placeholder={configuracion.ejemplos.oficio}
                       value={formulario.oficioSolicitud}
                       onChange={(evento) => cambiar("oficioSolicitud", evento.target.value)} />
              </div>

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="dtpFechaRecepcion">
                  Fecha de recepción en la oficina
                </label>
                <input type="date" className="form-control" id="dtpFechaRecepcion" max={hoy}
                       value={formulario.fechaRecepcion}
                       onChange={(evento) => cambiar("fechaRecepcion", evento.target.value)} />
              </div>

              <div className="col-12">
                <label className="form-label sim-requerido" htmlFor="txtDescripcionMercancia">
                  Descripción de la mercancía
                </label>
                <input type="text" className="form-control" id="txtDescripcionMercancia" maxLength={255}
                       placeholder={configuracion.ejemplos.descripcion}
                       value={formulario.descripcionMercancia}
                       onChange={(evento) => cambiar("descripcionMercancia", evento.target.value)} />
              </div>

              {esExportacion && (
                <div className="col-md-6">
                  <label className="form-label" htmlFor="txtAntecedente">
                    Antecedente <span className="sim-opcional">(si aplica)</span>
                    <Cmp_SIM_AyudaCampo titulo="el antecedente">
                      Cómo adquirió la Universidad el equipo: su placa, el pedido, la donación normal o por medio
                      de la Fundación UCR, o la compra local.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control" id="txtAntecedente" maxLength={120}
                         placeholder="Por ejemplo, Placa UCR 270256 o P-005-2024"
                         value={formulario.antecedente}
                         onChange={(evento) => cambiar("antecedente", evento.target.value)} />
                </div>
              )}

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="cmbEstadoMercancia">
                  Estado de la mercancía
                </label>
                <select className="form-select" id="cmbEstadoMercancia" value={formulario.estadoMercancia}
                        onChange={(evento) => cambiar("estadoMercancia", evento.target.value)}>
                  <option value="">Seleccione el estado del equipo</option>
                  {Object.entries(NOMBRES_ESTADO_MERCANCIA).map(([valor, nombre]) => (
                    <option value={valor} key={valor}>{nombre}</option>
                  ))}
                </select>
              </div>

              {esExportacion && (
                <div className="col-md-6">
                  <span className="form-label d-block sim-requerido" id="lblEnGarantia">En periodo de garantía</span>
                  <div role="radiogroup" aria-labelledby="lblEnGarantia">
                    {[["true", "Sí"], ["false", "No"]].map(([valor, nombre]) => (
                      <div className="form-check form-check-inline" key={valor}>
                        <input className="form-check-input" type="radio" name="rdbEnGarantia"
                               id={`rdbEnGarantia${nombre}`} value={valor}
                               checked={formulario.enGarantia === valor}
                               onChange={(evento) => cambiar("enGarantia", evento.target.value)} />
                        <label className="form-check-label" htmlFor={`rdbEnGarantia${nombre}`}>{nombre}</label>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="col-md-6">
                <label className="form-label sim-requerido" htmlFor="numValorEquipo">Valor declarado del equipo</label>
                <div className="input-group">
                  <select className="form-select flex-grow-0 w-auto" id="cmbMoneda" aria-label="Moneda"
                          value={formulario.moneda} onChange={(evento) => cambiar("moneda", evento.target.value)}>
                    {(opciones?.monedas ?? [{ codigoIso: configuracion.monedaPorDefecto }]).map((moneda) => (
                      <option value={moneda.codigoIso} key={moneda.codigoIso}>{moneda.codigoIso}</option>
                    ))}
                  </select>
                  <input type="number" className="form-control" id="numValorEquipo" min="0" step="0.01"
                         placeholder="0.00" value={formulario.valorEquipo}
                         onChange={(evento) => cambiar("valorEquipo", evento.target.value)} />
                </div>
                {esExportacion && (
                  <div className="form-text">Si son varios equipos, indique el valor total.</div>
                )}
              </div>

              {esExportacion && (
                <>
                  <div className="col-md-6">
                    <label className="form-label sim-requerido" htmlFor="numPesoKg">Peso (kg)</label>
                    <input type="number" className="form-control" id="numPesoKg" min="0" step="0.01"
                           placeholder="0.00" value={formulario.pesoKg}
                           onChange={(evento) => cambiar("pesoKg", evento.target.value)} />
                  </div>
                  <div className="col-md-6">
                    <label className="form-label sim-requerido" htmlFor="cmbPaisDestino">País de destino</label>
                    <select className="form-select" id="cmbPaisDestino" value={formulario.idPaisDestino}
                            onChange={(evento) => cambiar("idPaisDestino", evento.target.value)}>
                      <option value="">Seleccione el país de destino</option>
                      {(opciones?.paises ?? []).map((pais) => (
                        <option value={pais.idPais} key={pais.idPais}>{pais.nombre}</option>
                      ))}
                    </select>
                  </div>
                </>
              )}

              <div className="col-md-6">
                <label className="form-label" htmlFor="txtNumeroFactura">
                  Número de factura del exportador <span className="sim-opcional">(si aplica)</span>
                </label>
                <input type="text" className="form-control" id="txtNumeroFactura" maxLength={80}
                       placeholder={esExportacion ? "Por ejemplo, 002-2025" : "SR si el equipo no trae factura"}
                       value={formulario.numeroFactura}
                       onChange={(evento) => cambiar("numeroFactura", evento.target.value)} />
              </div>

              <div className="col-md-6">
                <label className="form-label" htmlFor="txtAduanaControl">
                  Aduana de control <span className="sim-opcional">(si aplica)</span>
                </label>
                <input type="text" className="form-control" id="txtAduanaControl" maxLength={60}
                       list="lstAduanasControl" placeholder="Por ejemplo, Aduana Santamaría"
                       value={formulario.aduanaControl}
                       onChange={(evento) => cambiar("aduanaControl", evento.target.value)} />
              </div>

              <div className="col-md-6">
                <label className="form-label" htmlFor="txtUsuarioResponsable">Usuario responsable</label>
                <input type="text" className="form-control" id="txtUsuarioResponsable" value={usuarioActual} readOnly />
              </div>
            </div>
          </div>
        </div>

        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-building me-2" aria-hidden="true"></i>
            Gestión ante la aduana <span className="sim-opcional fw-normal">(si ya se tiene)</span>
          </div>
          <div className="card-body">
            <p className="small text-muted">
              Estos datos llegan conforme avanza el trámite. Si todavía no los tiene, los completa
              después desde el expediente.
            </p>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label" htmlFor="txtOficioAduana">Oficio enviado a la aduana</label>
                <input type="text" className="form-control" id="txtOficioAduana" maxLength={120}
                       placeholder="Oficio o correo con su fecha"
                       value={formulario.oficioAduana}
                       onChange={(evento) => cambiar("oficioAduana", evento.target.value)} />
              </div>
              <div className="col-md-6">
                <label className="form-label" htmlFor="txtNumeroGestion">Número de gestión</label>
                <input type="text" className="form-control" id="txtNumeroGestion" maxLength={80}
                       placeholder="Asignado por la aduana"
                       value={formulario.numeroGestion}
                       onChange={(evento) => cambiar("numeroGestion", evento.target.value)} />
              </div>
              <div className="col-md-6">
                <label className="form-label" htmlFor="txtResolucionAutorizacion">Resolución de autorización</label>
                <input type="text" className="form-control" id="txtResolucionAutorizacion" maxLength={120}
                       value={formulario.resolucionAutorizacion}
                       onChange={(evento) => cambiar("resolucionAutorizacion", evento.target.value)} />
              </div>
              {esExportacion && (
                <div className="col-md-6">
                  <label className="form-label" htmlFor="txtOficioNotificacionUsuario">
                    Oficio de notificación al usuario
                    <Cmp_SIM_AyudaCampo titulo="el oficio de notificación">
                      Oficio con el que la Unidad informa al usuario que la aduana autorizó la exportación temporal.
                    </Cmp_SIM_AyudaCampo>
                  </label>
                  <input type="text" className="form-control" id="txtOficioNotificacionUsuario" maxLength={120}
                         placeholder="Por ejemplo, OS-115-2025"
                         value={formulario.oficioNotificacionUsuario}
                         onChange={(evento) => cambiar("oficioNotificacionUsuario", evento.target.value)} />
                </div>
              )}
              <div className="col-md-6">
                <label className="form-label" htmlFor="txtNumeroDuaInicio">Número del {nombreDuaInicio}</label>
                <input type="text" className="form-control" id="txtNumeroDuaInicio" maxLength={80}
                       placeholder="Por ejemplo, 005-2025-122327"
                       value={formulario.numeroDuaInicio}
                       onChange={(evento) => cambiar("numeroDuaInicio", evento.target.value)} />
              </div>
              <div className="col-md-6">
                <label className="form-label" htmlFor="dtpFechaDuaInicio">Fecha del {nombreDuaInicio}</label>
                <input type="date" className="form-control" id="dtpFechaDuaInicio" max={hoy}
                       value={formulario.fechaDuaInicio}
                       onChange={(evento) => cambiarFechaDua(evento.target.value)} />
              </div>
              <div className="col-md-6">
                <label className="form-label" htmlFor="dtpFechaVencimiento">
                  {esExportacion ? "Fecha de vencimiento del régimen" : "Fecha de vencimiento autorizada"}
                  <Cmp_SIM_AyudaCampo titulo="la fecha de vencimiento">
                    La fija la aduana en la resolución de autorización. Con ella inicia la cuenta regresiva
                    y las alertas de vencimiento.
                    {esExportacion && " El plazo estándar es la fecha del DUA de exportación más 6 meses."}
                  </Cmp_SIM_AyudaCampo>
                </label>
                <input type="date" className="form-control" id="dtpFechaVencimiento"
                       min={formulario.fechaDuaInicio || undefined}
                       value={formulario.fechaVencimiento}
                       onChange={(evento) => cambiar("fechaVencimiento", evento.target.value)} />
                {esExportacion && formulario.fechaVencimiento && formulario.fechaVencimiento === vencimientoSugerido && (
                  <div className="form-text">Plazo estándar: fecha del DUA más 6 meses. Puede cambiarla.</div>
                )}
              </div>
              {esExportacion && (
                <div className="col-md-6">
                  <label className="form-label" htmlFor="txtNotificacionDuaAduana">
                    Notificación del DUA de salida a la aduana
                  </label>
                  <input type="text" className="form-control" id="txtNotificacionDuaAduana" maxLength={120}
                         placeholder="Por ejemplo, correo 17-03-2025"
                         value={formulario.notificacionDuaAduana}
                         onChange={(evento) => cambiar("notificacionDuaAduana", evento.target.value)} />
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-bell me-2" aria-hidden="true"></i>
            Alertas de vencimiento
          </div>
          <div className="card-body">
            <p className="small text-muted mb-2">
              Usted recibe las alertas como responsable del expediente. Marque los agentes de aduanas que
              también deben recibirlas <span className="sim-opcional">(opcional)</span>.
            </p>
            {agentes.length === 0 ? (
              <p className="small text-muted mb-0">No hay agentes de aduanas con una cuenta activa para asignar.</p>
            ) : (
              <div className="row g-2">
                {agentes.map((agente) => (
                  <div className="col-12 col-md-6 col-lg-4" key={agente.idUsuario}>
                    <div className="form-check">
                      <input className="form-check-input" type="checkbox" id={`chkAgente${agente.idUsuario}`}
                             checked={formulario.agentesResponsables.includes(agente.idUsuario)}
                             onChange={(evento) => alternarAgente(agente.idUsuario, evento.target.checked)} />
                      <label className="form-check-label" htmlFor={`chkAgente${agente.idUsuario}`}>
                        {agente.nombreCompleto}
                      </label>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="card sim-card shadow-sm mb-4">
          <div className="card-header">
            <i className="bi bi-paperclip me-2" aria-hidden="true"></i>
            Documentación del expediente <span className="sim-opcional fw-normal">(si aplica)</span>
          </div>
          <div className="card-body">
            <Cmp_SIM_Documentos key={versionArchivos} documentos={documentosRegistro}
                                archivos={archivos} alCambiar={setArchivos} />
            <div className="mt-3">
              <label className="form-label" htmlFor="txtObservaciones">
                Observaciones <span className="sim-opcional">(si aplica)</span>
              </label>
              <textarea className="form-control" id="txtObservaciones" rows={3}
                        maxLength={LONGITUD_MAXIMA_OBSERVACIONES}
                        placeholder="Notas de seguimiento del trámite."
                        value={formulario.observaciones}
                        onChange={(evento) => cambiar("observaciones", evento.target.value)} />
              <div className="form-text text-end">
                {formulario.observaciones.length}/{LONGITUD_MAXIMA_OBSERVACIONES}
              </div>
            </div>
          </div>
        </div>

        <div className="d-flex justify-content-end gap-2 mb-5">
          <Link className="btn btn-link" to={configuracion.ruta}>Cancelar</Link>
          <button type="button" className="btn btn-outline-secondary" id="btnLimpiarTramiteTemporal"
                  onClick={limpiar}>
            Limpiar
          </button>
          <button type="submit" className="btn btn-sim" id="btnRegistrarTramiteTemporal" disabled={enviando}>
            <i className="bi bi-save me-1" aria-hidden="true"></i>
            {enviando ? "Registrando..." : "Crear expediente"}
          </button>
        </div>
      </form>

      {respuesta && (
        <>
          <div className="modal fade show d-block" tabIndex="-1" role="dialog" aria-modal="true">
            <div className="modal-dialog">
              <div className="modal-content">
                <div className="modal-header">
                  <h2 className="modal-title h5">
                    <i className="bi bi-check-circle text-success me-2" aria-hidden="true"></i>
                    {capitalizar(configuracion.singular)} registrada
                  </h2>
                  <button type="button" className="btn-close" aria-label="Cerrar" onClick={() => setRespuesta(null)}></button>
                </div>
                <div className="modal-body">
                  <p>{respuesta.mensaje}</p>
                  <dl className="row mb-0">
                    <dt className="col-5">Referencia</dt>
                    <dd className="col-7 sim-referencia">{respuesta.referenciaInterna}</dd>
                    <dt className="col-5">Estado</dt>
                    <dd className="col-7"><span className="badge text-bg-warning">{respuesta.estado}</span></dd>
                    <dt className="col-5">Documentos adjuntos</dt>
                    <dd className="col-7">{respuesta.cantidadDocumentos} archivo(s)</dd>
                  </dl>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-outline-secondary" onClick={() => setRespuesta(null)}>
                    Registrar otra
                  </button>
                  <Link className="btn btn-sim"
                        to={`${configuracion.ruta}/${encodeURIComponent(respuesta.referenciaInterna)}`}>
                    Ver expediente
                  </Link>
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop fade show"></div>
        </>
      )}
    </Cmp_SIM_Marco>
  );
}

export default Frm_SIM_RegistrarTramiteTemporal;
