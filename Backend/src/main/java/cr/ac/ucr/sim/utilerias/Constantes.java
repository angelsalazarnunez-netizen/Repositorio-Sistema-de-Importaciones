package cr.ac.ucr.sim.utilerias;

/**
 * Autor: Katherine Susana Valverde Fallas
 * Fecha: 10/09/2026
 * Descripción: Constantes de uso común del sistema, agrupadas según el estándar P05.
 */
public final class Constantes {

    // -----------------------------------------------------------------------
    // HU-004 Iniciar sesión.
    // -----------------------------------------------------------------------

    public static final int DURACION_SESION_HORAS = 8;
    public static final String ESTADO_VALIDACION_VALIDADO = "VAL";
    public static final String ESTADO_ROL_ASIGNADO = "ASI";
    public static final String MENSAJE_CREDENCIALES_INVALIDAS = "Credenciales inválidas.";
    public static final String MENSAJE_CORREO_NO_VALIDADO =
            "Debe validar su correo institucional antes de continuar.";
    public static final String MENSAJE_USUARIO_SIN_ROL =
            "Su cuenta no tiene un rol asignado.";
    public static final String MENSAJE_INICIO_SESION_EXITOSO = "Inicio de sesión exitoso.";

    // -----------------------------------------------------------------------
    // HU-031 Registrar trámite de desalmacenamiento especial.
    // -----------------------------------------------------------------------

    public static final String CODIGO_TIPO_TRAMITE_DESALMACENAJE = "DESALM_ESPECIAL";
    public static final String EXPRESION_REFERENCIA_DESALMACENAJE = "^DE-[0-9]{4}-[0-9]{4}$";
    public static final int LONGITUD_MINIMA_JUSTIFICACION = 20;
    public static final int LONGITUD_MAXIMA_JUSTIFICACION = 1000;
    public static final int LONGITUD_MAXIMA_ORIGEN_DEPENDENCIA = 120;
    public static final String CODIGO_TIPO_DOCUMENTO_RESPALDO = "RESPALDO";
    public static final String CODIGO_TIPO_DOCUMENTO_PACKING_LIST = "PACKING_LIST";
    public static final String CODIGO_TIPO_DOCUMENTO_FACTURA = "FACTURA";
    public static final String CODIGO_TIPO_DOCUMENTO_EMBARQUE = "EMBARQUE";
    public static final String ESTADO_DOCUMENTO_ACTIVO = "ACT";
    public static final String ESTADO_DOCUMENTO_INACTIVO = "INA";
    public static final String JUSTIFICACION_REGISTRO_INICIAL =
            "Registro inicial del expediente.";
    public static final String MENSAJE_DATOS_INCOMPLETOS =
            "Debe completar todos los datos del trámite.";
    public static final String MENSAJE_REFERENCIA_INVALIDA =
            "La referencia interna debe tener el formato DE-AAAA-NNNN, que identifica los trámites "
                    + "de desalmacenaje especial.";
    public static final String MENSAJE_REFERENCIA_DUPLICADA =
            "Ya existe un trámite registrado con esa referencia interna.";
    public static final String MENSAJE_ORIGEN_DEPENDENCIA_REQUERIDO =
            "Debe indicar el origen o la dependencia responsable del trámite.";
    public static final String MENSAJE_VALOR_ESTIMADO_INVALIDO =
            "El valor estimado de la mercancía debe ser mayor que cero.";
    public static final String MENSAJE_JUSTIFICACION_INVALIDA =
            "La justificación debe tener entre 20 y 1000 caracteres.";
    public static final String MENSAJE_RESPALDO_REQUERIDO =
            "Debe adjuntar al menos un documento de respaldo del trámite.";
    public static final String MENSAJE_MONEDA_INVALIDA =
            "La moneda indicada no está registrada.";
    public static final String MENSAJE_PAIS_INVALIDO =
            "El país de origen indicado no está registrado.";
    public static final String MENSAJE_USUARIO_RESPONSABLE_INVALIDO =
            "El usuario responsable del trámite no está registrado.";
    public static final String MENSAJE_TIPO_TRAMITE_NO_CONFIGURADO =
            "El tipo de trámite de desalmacenaje especial no está configurado en el catálogo.";
    public static final String MENSAJE_ESTADO_INICIAL_NO_CONFIGURADO =
            "El catálogo no define el estado inicial del trámite de desalmacenaje especial.";
    public static final String MENSAJE_TIPO_DOCUMENTO_NO_CONFIGURADO =
            "El catálogo no define los tipos de documento del expediente.";
    public static final String MENSAJE_EXPEDIENTE_NO_ENCONTRADO =
            "No existe un expediente de desalmacenaje especial con esa referencia interna.";
    public static final String MENSAJE_DOCUMENTO_NO_DISPONIBLE =
            "El documento solicitado no está disponible.";
    public static final String MENSAJE_DOCUMENTO_NO_ALMACENADO =
            "No fue posible guardar uno de los documentos adjuntos. Intente registrar el trámite de nuevo.";
    public static final String MENSAJE_SIN_DOCUMENTOS_ADJUNTOS =
            "Debe adjuntar al menos un documento para agregarlo al expediente.";
    public static final String MENSAJE_REGISTRO_DESALMACENAJE_EXITOSO =
            "El trámite de desalmacenaje especial se registró correctamente. "
                    + "El expediente queda disponible para su seguimiento.";

    // -----------------------------------------------------------------------
    // HU-024 Registrar donaciones.
    // Responsable: Josue Guevara Lostalo. Fecha: 10/09/2026.
    // -----------------------------------------------------------------------

    public static final String CODIGO_TIPO_TRAMITE_DONACION = "DONACION";
    public static final String TIPO_DONACION_NACIONAL = "NACIONAL";
    public static final String TIPO_DONACION_INTERNACIONAL = "INTERNACIONAL";
    public static final String TIPO_DONACION_FUNDACION_UCR = "FUNDACION_UCR";
    // Sigla de la Unidad para la donación por medio de la Fundación UCR. La donación normal usa
    // el prefijo del catálogo de tipos de trámite (D-).
    public static final String PREFIJO_REFERENCIA_DONACION_FUNDACION_UCR = "DF-";
    public static final String CODIGO_TIPO_DOCUMENTO_RESOLUCION_ACTA = "RESOLUCION_ACTA";
    public static final int LONGITUD_MAXIMA_NOMBRE_DONANTE = 150;
    public static final int LONGITUD_MAXIMA_REFERENCIA_FUNDACION_UCR = 80;
    public static final int LONGITUD_MAXIMA_OBSERVACIONES = 4000;
    public static final String MENSAJE_REFERENCIA_DONACION_INVALIDA =
            "La referencia interna debe iniciar con D- en las donaciones y con DF- en las donaciones "
                    + "por medio de la Fundación UCR, seguida del año y el consecutivo.";
    public static final String MENSAJE_TIPO_DONACION_INVALIDO =
            "El tipo de donación debe ser Nacional, Internacional o Fundación UCR.";
    public static final String MENSAJE_DONANTE_REQUERIDO =
            "Debe indicar el nombre de la persona o entidad donante.";
    public static final String MENSAJE_RESOLUCION_ACEPTACION_REQUERIDA =
            "Debe adjuntar la resolución o acta de aceptación de la donación.";
    public static final String MENSAJE_REFERENCIA_FUNDACION_UCR_NO_APLICA =
            "La referencia de la Fundación UCR solo aplica para las donaciones por medio de la Fundación UCR.";
    public static final String MENSAJE_REFERENCIA_FUNDACION_UCR_INVALIDA =
            "La referencia de la Fundación UCR no debe superar los 80 caracteres.";
    public static final String MENSAJE_OBSERVACIONES_INVALIDAS =
            "Las observaciones no deben superar los 4000 caracteres.";
    public static final String MENSAJE_TIPO_TRAMITE_DONACION_NO_CONFIGURADO =
            "El tipo de trámite de donación no está configurado en el catálogo.";
    public static final String MENSAJE_ESTADO_INICIAL_DONACION_NO_CONFIGURADO =
            "El catálogo no define el estado inicial del trámite de donación.";
    public static final String MENSAJE_REGISTRO_DONACION_EXITOSO =
            "La donación se registró correctamente. "
                    + "El expediente queda disponible para su seguimiento.";

    // -----------------------------------------------------------------------
    // HU-027 Alertas de vencimiento de trámites temporales.
    // Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
    // Descripción: la alerta se anticipa 45 días (mes y medio) a pedido de la Unidad de
    //   Importaciones, que necesita ese margen para gestionar la prórroga ante la aduana.
    // -----------------------------------------------------------------------

    public static final int DIAS_ANTICIPACION_ALERTA_TEMPORAL = 45;
    public static final String MENSAJE_CONTRATO_TEMPORAL_REQUERIDO =
            "Debe indicar el número de contrato cuando el plazo se expresa en años.";
    public static final String MENSAJE_AGENTES_TEMPORAL_INVALIDOS =
            "Seleccione agentes de aduanas con una cuenta validada y un rol asignado.";
    public static final String MENSAJE_PLAZO_TEMPORAL_INVALIDO =
            "El plazo del trámite temporal debe ser positivo y su vencimiento posterior a hoy.";
    public static final String MENSAJE_ALERTA_TEMPORAL_NO_ENCONTRADA =
            "No existe una alerta de vencimiento pendiente para este trámite.";

    // -----------------------------------------------------------------------
    // HU-025 Registrar importaciones temporales.
    // Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
    // -----------------------------------------------------------------------

    public static final String CODIGO_TIPO_TRAMITE_IMPORTACION_TEMPORAL = "IMPORT_TEMPORAL";
    public static final String REGIMEN_IMPORTACION_TEMPORAL = "Importación temporal";
    public static final String ENTIDAD_BITACORA_TRAMITE_TEMPORAL = "SIMT_TRAMITE_TEMPORAL";
    public static final String ACCION_BITACORA_PRORROGA = "PRORROGA";
    public static final String ESTADO_MERCANCIA_NUEVA = "NUEVA";
    public static final String ESTADO_MERCANCIA_USADA = "USADA";
    public static final String ESTADO_MERCANCIA_REACONDICIONADA = "REACONDICIONADA";
    // Estados del catálogo con reglas propias.
    public static final String ESTADO_TEMPORAL_EN_GESTION = "En gestión aduanera";
    public static final String ESTADO_TEMPORAL_RETENIDO = "Retenido en aduana";
    public static final String ESTADO_TEMPORAL_ACTIVO = "Activo en el país";
    public static final String ESTADO_TEMPORAL_VENCIDO = "Vencido";
    public static final String ESTADO_TEMPORAL_EN_REEXPORTACION = "En reexportación";
    public static final String ESTADO_TEMPORAL_FINALIZADO = "Finalizado";
    // Alcances de la lista de importaciones temporales.
    public static final String ALCANCE_TEMPORAL_ACTIVOS = "ACTIVOS";
    public static final String ALCANCE_TEMPORAL_CERRADOS = "CERRADOS";
    public static final String ALCANCE_TEMPORAL_TODOS = "TODOS";
    // Indicador de vencimiento que se muestra en la lista y en el expediente.
    public static final String VENCIMIENTO_SIN_FECHA = "SIN_FECHA";
    public static final String VENCIMIENTO_EN_PLAZO = "EN_PLAZO";
    public static final String VENCIMIENTO_PROXIMO = "PROXIMO";
    public static final String VENCIMIENTO_VENCIDO = "VENCIDO";
    // Tipos de documento del expediente temporal.
    public static final String CODIGO_TIPO_DOCUMENTO_OFICIO_SOLICITUD = "OFICIO_SOLICITUD";
    public static final String CODIGO_TIPO_DOCUMENTO_OFICIO_ADUANA = "OFICIO_ADUANA";
    public static final String CODIGO_TIPO_DOCUMENTO_RESOLUCION_AUTORIZACION = "RESOL_AUTORIZACION";
    public static final String CODIGO_TIPO_DOCUMENTO_DUA_IMPORTACION_TEMPORAL = "DUA_IMPORT_TEMPORAL";
    public static final String CODIGO_TIPO_DOCUMENTO_OFICIO_PRORROGA = "OFICIO_PRORROGA";
    public static final String CODIGO_TIPO_DOCUMENTO_AUTORIZACION_PRORROGA = "AUTORIZ_PRORROGA";
    public static final String CODIGO_TIPO_DOCUMENTO_DUA_REEXPORTACION = "DUA_REEXPORTACION";
    public static final String CODIGO_TIPO_DOCUMENTO_OFICIO_CANCELACION = "OFICIO_CANCELACION";
    public static final int LONGITUD_MAXIMA_UNIDAD_SOLICITANTE = 120;
    public static final int LONGITUD_MAXIMA_DESCRIPCION_MERCANCIA = 255;
    public static final int LONGITUD_MAXIMA_REFERENCIA_ADUANA = 120;
    public static final int LONGITUD_MAXIMA_NUMERO_ADUANA = 80;
    public static final int LONGITUD_MAXIMA_ADUANA_CONTROL = 60;
    public static final String MENSAJE_TIPO_TRAMITE_TEMPORAL_NO_CONFIGURADO =
            "El tipo de trámite de importación temporal no está configurado en el catálogo.";
    public static final String MENSAJE_ESTADO_INICIAL_TEMPORAL_NO_CONFIGURADO =
            "El catálogo no define el estado inicial de la importación temporal.";
    public static final String MENSAJE_REFERENCIA_TEMPORAL_INVALIDA =
            "La referencia interna debe iniciar con IT-, que identifica las importaciones temporales, "
                    + "y tener como máximo 45 caracteres.";
    public static final String MENSAJE_UNIDAD_SOLICITANTE_REQUERIDA =
            "Debe indicar la unidad solicitante, con un máximo de 120 caracteres.";
    public static final String MENSAJE_OFICIO_SOLICITUD_REQUERIDO =
            "Debe indicar el oficio del usuario solicitante, con un máximo de 80 caracteres.";
    public static final String MENSAJE_FECHA_RECEPCION_INVALIDA =
            "Debe indicar la fecha de recepción del oficio, que no puede ser posterior a hoy.";
    public static final String MENSAJE_DESCRIPCION_MERCANCIA_REQUERIDA =
            "Debe describir la mercancía, con un máximo de 255 caracteres.";
    public static final String MENSAJE_ESTADO_MERCANCIA_INVALIDO =
            "El estado de la mercancía debe ser Nueva, Usada o Reacondicionada.";
    public static final String MENSAJE_VALOR_EQUIPO_INVALIDO =
            "El valor declarado del equipo debe ser mayor que cero.";
    public static final String MENSAJE_ADUANA_CONTROL_INVALIDA =
            "La aduana de control no debe superar los 60 caracteres.";
    public static final String MENSAJE_DATO_ADUANA_INVALIDO =
            "Los oficios, números de gestión, resoluciones y DUA no deben superar los 120 caracteres; "
                    + "los números de gestión y de DUA, los 80.";
    public static final String MENSAJE_FECHA_DUA_INVALIDA =
            "La fecha de un DUA no puede ser posterior a hoy.";
    public static final String MENSAJE_EXPEDIENTE_TEMPORAL_NO_ENCONTRADO =
            "No existe un expediente de importación temporal con esa referencia interna.";
    public static final String MENSAJE_EXPEDIENTE_TEMPORAL_CERRADO =
            "El expediente está cerrado y no admite cambios. Su información y su historial se "
                    + "conservan para consulta.";
    public static final String MENSAJE_SOLO_DUA_CIERRE_EDITABLE =
            "El expediente está cerrado: solo se pueden corregir el número, la fecha y el documento "
                    + "del DUA de cierre.";
    public static final String MENSAJE_ESTADO_TEMPORAL_INVALIDO =
            "El estado indicado no está definido para las importaciones temporales.";
    public static final String MENSAJE_VENCIMIENTO_REQUERIDO_ACTIVO =
            "Para pasar el expediente a Activo en el país registre primero la fecha de vencimiento "
                    + "que autorizó la aduana.";
    public static final String MENSAJE_VENCIMIENTO_NO_SE_PUEDE_BORRAR =
            "La fecha de vencimiento no se puede borrar mientras el equipo esté en el país.";
    public static final String MENSAJE_REQUISITOS_FINALIZACION =
            "Para finalizar el expediente registre el DUA de cancelación y la cancelación del "
                    + "régimen, con su número o con su documento.";
    public static final String MENSAJE_DATOS_TEMPORAL_SIN_CAMBIO =
            "No se indicó ningún cambio en los datos del expediente.";
    public static final String MENSAJE_PRORROGA_SIN_DATOS =
            "Indique al menos un dato de la prórroga: el motivo, el oficio, el número de gestión, la "
                    + "autorización o el nuevo vencimiento.";
    public static final int LONGITUD_MAXIMA_MOTIVO_PRORROGA = 500;
    public static final String MENSAJE_MOTIVO_PRORROGA_INVALIDO =
            "El motivo de la prórroga no debe superar los 500 caracteres.";
    public static final String MENSAJE_PRORROGA_FECHA_INVALIDA =
            "El nuevo vencimiento debe ser posterior al vencimiento actual del expediente.";
    public static final String MENSAJE_PRORROGA_SIN_GESTION_INICIAL =
            "El expediente no tiene un número de gestión inicial para repetirlo en la prórroga.";
    public static final String MENSAJE_PRORROGA_SIN_VENCIMIENTO =
            "Registre primero la fecha de vencimiento del expediente para poder prorrogarla.";
    public static final String MENSAJE_PRORROGA_REQUISITOS_INCOMPLETOS =
            "Para registrar la prórroga indique motivo, oficio de solicitud, número de gestión, "
                    + "número de autorización y confirme el vencimiento actual.";
    public static final String MENSAJE_PRORROGA_DOCUMENTOS_REQUERIDOS =
            "Adjunte el oficio de solicitud enviado a la aduana y la resolución emitida por la aduana.";
    public static final String MENSAJE_PRORROGA_VENCIMIENTO_CAMBIADO =
            "El vencimiento del trámite cambió. Recargue el expediente antes de registrar la prórroga.";
    public static final String MENSAJE_PRORROGA_MAXIMA_EXPORTACION =
            "Una exportación temporal solo admite una prórroga.";
    public static final String MENSAJE_PRORROGA_CONTRATO_REQUERIDO =
            "Indique el número de contrato si el bien permanecerá en el país por años.";
    public static final String MENSAJE_TIPO_DOCUMENTO_TEMPORAL_INVALIDO =
            "El tipo de documento indicado no corresponde a una importación temporal.";
    public static final String MENSAJE_ALCANCE_TEMPORAL_INVALIDO =
            "Seleccione qué expedientes mostrar: activos, cerrados o todos.";
    public static final String MENSAJE_REGISTRO_TEMPORAL_EXITOSO =
            "La importación temporal se registró correctamente. "
                    + "El expediente queda disponible para su seguimiento.";
    public static final String MENSAJE_REGISTRO_TEMPORAL_SIN_VENCIMIENTO =
            "La importación temporal se registró correctamente. La cuenta regresiva iniciará cuando "
                    + "registre la fecha de vencimiento que autorice la aduana.";
    public static final String MENSAJE_VENCIMIENTO_ANTERIOR_AL_DUA =
            "La fecha de vencimiento debe ser posterior a la fecha del DUA que inició el régimen.";
    public static final String MENSAJE_TRAMITE_TEMPORAL_NO_ENCONTRADO =
            "No existe un trámite temporal con esa referencia interna.";

    // -----------------------------------------------------------------------
    // HU-026 Registrar exportaciones temporales.
    // Responsable: Josue Guevara Lostalo. Fecha: 04/10/2026.
    // Descripción: la exportación temporal reutiliza las reglas de la importación temporal; aquí
    //   están solo sus datos y sus textos propios.
    // -----------------------------------------------------------------------

    public static final String CODIGO_TIPO_TRAMITE_EXPORTACION_TEMPORAL = "EXPORT_TEMPORAL";
    public static final String REGIMEN_EXPORTACION_TEMPORAL = "Exportación temporal";
    // Estados del catálogo con reglas propias de la exportación.
    public static final String ESTADO_TEMPORAL_FUERA_DEL_PAIS = "Fuera del país";
    public static final String ESTADO_TEMPORAL_EN_REIMPORTACION = "En reimportación";
    // Tipos de documento propios de la exportación temporal.
    public static final String CODIGO_TIPO_DOCUMENTO_OFICIO_NOTIFICACION_USUARIO = "OFICIO_NOTIF_USUARIO";
    public static final String CODIGO_TIPO_DOCUMENTO_DUA_EXPORTACION_TEMPORAL = "DUA_EXPORT_TEMPORAL";
    public static final String CODIGO_TIPO_DOCUMENTO_SOLICITUD_REINGRESO = "SOLICITUD_REINGRESO";
    public static final String CODIGO_TIPO_DOCUMENTO_DUA_REIMPORTACION = "DUA_REIMPORTACION";
    // Plazo estándar del régimen: la Unidad fija el vencimiento 6 meses después del DUA de salida.
    public static final String UNIDAD_PLAZO_MESES = "MESES";
    public static final int MESES_PLAZO_ESTANDAR_EXPORTACION = 6;
    public static final int LONGITUD_MAXIMA_ANTECEDENTE = 120;
    public static final int LONGITUD_MAXIMA_DOCUMENTO_EMBARQUE = 80;
    public static final long PESO_MAXIMO_KG = 99_999_999L; // DECIMAL(10,2): 8 dígitos enteros
    public static final String MENSAJE_TIPO_TRAMITE_EXPORTACION_TEMPORAL_NO_CONFIGURADO =
            "El tipo de trámite de exportación temporal no está configurado en el catálogo.";
    public static final String MENSAJE_ESTADO_INICIAL_EXPORTACION_TEMPORAL_NO_CONFIGURADO =
            "El catálogo no define el estado inicial de la exportación temporal.";
    public static final String MENSAJE_REFERENCIA_EXPORTACION_TEMPORAL_INVALIDA =
            "La referencia interna debe iniciar con EXP-T-, que identifica las exportaciones temporales, "
                    + "y tener como máximo 45 caracteres.";
    public static final String MENSAJE_EXPEDIENTE_EXPORTACION_TEMPORAL_NO_ENCONTRADO =
            "No existe un expediente de exportación temporal con esa referencia interna.";
    public static final String MENSAJE_ESTADO_EXPORTACION_TEMPORAL_INVALIDO =
            "El estado indicado no está definido para las exportaciones temporales.";
    public static final String MENSAJE_VENCIMIENTO_REQUERIDO_FUERA_DEL_PAIS =
            "Para pasar el expediente a Fuera del país registre primero la fecha de vencimiento del "
                    + "régimen que autorizó la aduana.";
    public static final String MENSAJE_VENCIMIENTO_NO_SE_PUEDE_BORRAR_EXPORTACION =
            "La fecha de vencimiento no se puede borrar mientras el equipo esté fuera del país.";
    public static final String MENSAJE_REQUISITOS_FINALIZACION_EXPORTACION =
            "Para finalizar el expediente registre el DUA de reingreso y la cancelación del régimen, "
                    + "con su número o con su documento. Si el equipo no regresa, use Salida definitiva.";
    public static final String MENSAJE_TIPO_DOCUMENTO_EXPORTACION_TEMPORAL_INVALIDO =
            "El tipo de documento indicado no corresponde a una exportación temporal.";
    public static final String MENSAJE_REGISTRO_EXPORTACION_TEMPORAL_EXITOSO =
            "La exportación temporal se registró correctamente. "
                    + "El expediente queda disponible para su seguimiento.";
    public static final String MENSAJE_REGISTRO_EXPORTACION_TEMPORAL_SIN_VENCIMIENTO =
            "La exportación temporal se registró correctamente. La cuenta regresiva iniciará cuando "
                    + "registre la fecha de vencimiento del régimen.";
    public static final String MENSAJE_GARANTIA_REQUERIDA =
            "Indique si el equipo está en periodo de garantía.";
    public static final String MENSAJE_PESO_INVALIDO =
            "Indique el peso del equipo en kilogramos, mayor que cero.";
    public static final String MENSAJE_PAIS_DESTINO_INVALIDO =
            "Seleccione el país de destino del equipo entre los países registrados.";
    public static final String MENSAJE_ANTECEDENTE_INVALIDO =
            "El antecedente no debe superar los 120 caracteres.";
    public static final String MENSAJE_DATO_REIMPORTACION_INVALIDO =
            "La solicitud de reingreso no debe superar los 120 caracteres ni el documento de embarque "
                    + "los 80.";
    public static final String MENSAJE_FECHA_REIMPORTACION_INVALIDA =
            "Las fechas de reingreso y de desalmacenaje no pueden ser posteriores a hoy.";
    public static final java.math.BigDecimal MONTO_MAXIMO_IMPUESTOS_REIMPORTACION =
            new java.math.BigDecimal("9999999999999.99"); // DECIMAL(15,2)
    public static final String MENSAJE_IMPUESTOS_REIMPORTACION_INVALIDOS =
            "El monto de impuestos de la reimportación debe ser un número positivo en colones.";

    // -----------------------------------------------------------------------
    // HU-018 Registrar solicitud de exoneración.
    // Responsable: Katherine Susana Valverde Fallas. Fecha: 15/09/2026.
    // -----------------------------------------------------------------------

    public static final String CODIGO_TIPO_TRAMITE_EXONERACION = "EXONERACION";
    public static final String TIPO_EXONERACION_COMPRA_INSTITUCIONAL = "COMPRA_INSTITUCIONAL";
    public static final String TIPO_EXONERACION_DONACION = "DONACION";
    public static final String TIPO_EXONERACION_CLIENTE_EXTERNO = "CLIENTE_EXTERNO";
    public static final String TIPO_EXONERACION_VEHICULOS = "VEHICULOS";
    public static final String TIPO_EXONERACION_MOVIMIENTO_TRASPASO = "MOVIM_TRASPASO";
    public static final String CODIGO_TIPO_DOCUMENTO_FORMULARIO_S1 = "FORMULARIO_S1";
    public static final String CODIGO_TIPO_DOCUMENTO_EXONET = "EXONET";
    public static final String CODIGO_TIPO_DOCUMENTO_PERMISO_REGULATORIO = "PERMISO_REGULATORIO";
    public static final String CODIGO_TIPO_DOCUMENTO_TRADUCCION_FACTURA = "TRADUCCION_FACTURA";
    // Documentos que la Unidad pidió poder agregar al expediente de exoneración (revisión del
    // Sprint 2): el expediente de SICOP para la jefatura y, después de la aprobación, el DUA de
    // aplicación, el comprobante de impuestos y cualquier otro respaldo.
    public static final String CODIGO_TIPO_DOCUMENTO_EXPEDIENTE_SICOP = "EXPEDIENTE_SICOP";
    public static final String CODIGO_TIPO_DOCUMENTO_DUA_APLICACION = "DUA_APLICACION";
    public static final String CODIGO_TIPO_DOCUMENTO_COMPROBANTE_IMPUESTOS = "COMPROB_IMPUESTOS";
    public static final String CODIGO_TIPO_DOCUMENTO_OTROS = "OTROS_DOCUMENTOS";
    public static final int LONGITUD_MAXIMA_DATO_CONTRACTUAL = 45;
    public static final int LONGITUD_MAXIMA_REFERENCIA_INTERNA = 45;
    public static final int LONGITUD_MAXIMA_COMENTARIO_BITACORA = 1000;
    public static final String ACCION_BITACORA_REGISTRO = "REGISTRO";
    public static final String ACCION_BITACORA_DOCUMENTO_AGREGADO = "DOCUMENTO_AGREGADO";
    public static final String ACCION_BITACORA_COMENTARIO = "COMENTARIO";
    public static final String ACCION_BITACORA_REFERENCIA_MODIFICADA = "REFERENCIA_MODIFICADA";

    // -----------------------------------------------------------------------
    // HU-045 Gestionar documentación de desalmacenamiento especial.
    // Responsable: Katherine Susana Valverde Fallas. Fecha: 25/09/2026.
    // -----------------------------------------------------------------------

    public static final String ACCION_BITACORA_DOCUMENTO_CLASIFICADO = "DOCUMENTO_CLASIFICADO";
    public static final String ACCION_BITACORA_ESTADO_DOCUMENTAL = "ESTADO_DOCUMENTAL";
    public static final String ESTADO_DOCUMENTAL_COMPLETO = "COMPLETO";
    public static final String ESTADO_DOCUMENTAL_INCOMPLETO = "INCOMPLETO";
    public static final int LONGITUD_MAXIMA_OBSERVACIONES_DOCUMENTALES = 1000;
    public static final String MENSAJE_CLASIFICACION_INVALIDA =
            "Seleccione una clasificación del catálogo de tipos de documento.";
    public static final String MENSAJE_CLASIFICACION_SIN_CAMBIO =
            "El documento ya tiene esa clasificación.";
    public static final String MENSAJE_ESTADO_DOCUMENTAL_INVALIDO =
            "Indique si el expediente está documentalmente completo o incompleto.";
    public static final String MENSAJE_OBSERVACIONES_DOCUMENTALES_INVALIDAS =
            "Las observaciones documentales no pueden pasar de 1000 caracteres.";
    public static final String MENSAJE_REFERENCIA_SIN_CAMBIO =
            "La referencia nueva es igual a la actual. Digite una referencia distinta.";
    public static final String MENSAJE_SIGLA_NO_RECONOCIDA =
            "La referencia debe iniciar con la sigla de un tipo de trámite del catálogo, "
                    + "por ejemplo DE- para un desalmacenaje especial o EXO- para una exoneración.";
    public static final String MENSAJE_REFERENCIA_NUEVA_INVALIDA =
            "Después de la sigla debe digitar el año y el consecutivo, "
                    + "sin pasar de 45 caracteres en total.";
    public static final String MENSAJE_TIPO_DESTINO_NO_SOPORTADO =
            "El sistema reclasifica el expediente hacia desalmacenaje especial o exoneración. "
                    + "Los demás tipos se registran desde su propia pantalla.";
    public static final String MENSAJE_DEPENDENCIA_DESTINO_REQUERIDA =
            "Para pasar el expediente a desalmacenaje especial debe indicar la dependencia de origen.";
    public static final String MENSAJE_ESTADO_INICIAL_DESTINO_NO_CONFIGURADO =
            "El catálogo no define el estado inicial del tipo de trámite al que se quiere pasar "
                    + "el expediente.";
    public static final String MENSAJE_REFERENCIA_MODIFICADA =
            "La referencia del expediente se actualizó y el cambio quedó anotado en la bitácora.";
    public static final String ACCION_BITACORA_COMENTARIO_ELIMINADO = "COMENTARIO_ELIMINADO";
    public static final String ENTIDAD_BITACORA_EXONERACION = "SIMT_EXONERACION";
    public static final String ENTIDAD_BITACORA_DOCUMENTO = "SIMF_DOCUMENTO";
    public static final String ENTIDAD_BITACORA_COMENTARIO = "SIML_BITACORA";
    public static final String ESTADO_BITACORA_ACTIVO = "ACT";
    public static final String ESTADO_BITACORA_INACTIVO = "INA";
    public static final String MENSAJE_REFERENCIA_EXONERACION_INVALIDA =
            "La referencia interna debe iniciar con EXO-, que identifica las solicitudes de exoneración, "
                    + "y tener como máximo 45 caracteres.";
    public static final String MENSAJE_TIPO_EXONERACION_INVALIDO =
            "Seleccione un tipo de exoneración válido.";
    public static final String MENSAJE_NUMERO_CONTRATACION_REQUERIDO =
            "Debe indicar el número de contratación, con un máximo de 45 caracteres.";
    public static final String MENSAJE_ORDEN_COMPRA_GECO_INVALIDA =
            "La orden de compra GECO es opcional. Si la indica, debe tener un máximo de 45 caracteres.";
    public static final String MENSAJE_NUMERO_EXONET_INVALIDO =
            "El número de solicitud EXONET es opcional. Si lo indica, debe tener un máximo de 45 caracteres.";
    public static final String MENSAJE_AUTORIZACION_EXONET_INVALIDA =
            "El número de autorización EXONET es opcional. Si lo indica, debe tener un máximo de 45 caracteres.";
    public static final String MENSAJE_CONTRATO_SICOP_INVALIDO =
            "El contrato SICOP es opcional, porque hay pedidos que no se tramitan por SICOP. "
                    + "Si lo indica, debe tener un máximo de 45 caracteres.";
    public static final String MENSAJE_TIPO_TRAMITE_EXONERACION_NO_CONFIGURADO =
            "El tipo de trámite de exoneración no está configurado en el catálogo.";
    public static final String MENSAJE_ESTADO_INICIAL_EXONERACION_NO_CONFIGURADO =
            "El catálogo no define el estado inicial de la solicitud de exoneración.";
    public static final String MENSAJE_EXONERACION_NO_ENCONTRADA =
            "No existe una solicitud de exoneración con esa referencia interna.";
    public static final String MENSAJE_COMENTARIO_INVALIDO =
            "Escriba el comentario, con un máximo de 1000 caracteres.";
    public static final String MENSAJE_COMENTARIO_NO_ENCONTRADO =
            "El comentario indicado no existe o ya fue eliminado.";
    public static final String MENSAJE_REGISTRO_EXONERACION_EXITOSO =
            "La solicitud de exoneración se registró correctamente. "
                    + "El expediente queda disponible para su seguimiento.";
    public static final String MENSAJE_REGISTRO_EXONERACION_CON_PENDIENTES =
            "La solicitud de exoneración se registró con documentación obligatoria pendiente. "
                    + "Adjúntela al expediente para continuar con la revisión documental.";

    // -----------------------------------------------------------------------
    // HU-006 Crear solicitud de importación.
    // -----------------------------------------------------------------------

    public static final String CODIGO_TIPO_TRAMITE_PEDIDO_IMPORTACION = "PEDIDO_PROV";
    public static final String MENSAJE_PEDIDO_IMPORTACION_NO_CONFIGURADO =
            "El tipo de trámite de pedido a proveedor no está configurado en el catálogo.";
    public static final String MENSAJE_PEDIDO_IMPORTACION_NO_ENCONTRADO =
            "No existe una solicitud de importación con esa referencia.";
    public static final String MENSAJE_REFERENCIA_PEDIDO_IMPORTACION_INVALIDA =
            "El número de solicitud debe usar el año actual y el formato AAAA-NNN.";
    public static final String MENSAJE_NUMERACION_PEDIDO_IMPORTACION_INVALIDA =
            "Revise los números de contratación, pedido y orden de compra para el año actual.";
    public static final String MENSAJE_REFERENCIA_PEDIDO_IMPORTACION_DUPLICADA =
            "Ya existe una solicitud con ese número.";
    public static final String MENSAJE_ESTADO_PEDIDO_IMPORTACION_REQUERIDO =
            "Debe indicar el estado actual de la solicitud.";
    public static final String MENSAJE_PARTIDA_PEDIDO_IMPORTACION_INVALIDA =
            "Seleccione una o dos partidas; cada partida debe ser 1, 2 o 5.";
    public static final String MENSAJE_PEDIDO_IMPORTACION_EXITOSO =
            "La solicitud de importación se registró correctamente.";

    // -----------------------------------------------------------------------
    // HU-032 Gestionar desalmacenaje especial.
    // Responsable: Katherine Susana Valverde Fallas. Fecha: 15/09/2026.
    // -----------------------------------------------------------------------

    public static final String ESTADO_CATALOGO_ACTIVO = "ACT";
    public static final int LONGITUD_MINIMA_JUSTIFICACION_CAMBIO = 10;
    public static final int LONGITUD_MAXIMA_JUSTIFICACION_CAMBIO = 255;
    public static final String MENSAJE_ESTADO_NUEVO_INVALIDO =
            "Seleccione un estado válido para este tipo de trámite.";
    public static final String MENSAJE_JUSTIFICACION_CAMBIO_REQUERIDA =
            "Debe escribir la justificación del cambio de estado, entre 10 y 255 caracteres.";
    public static final String MENSAJE_ESTADO_SIN_CAMBIO =
            "El expediente ya se encuentra en ese estado.";
    public static final String ACCION_BITACORA_CAMBIO_ESTADO = "CAMBIO_ESTADO";
    public static final String ACCION_BITACORA_DOCUMENTO_ELIMINADO = "DOCUMENTO_ELIMINADO";
    public static final String ACCION_BITACORA_DATOS_MODIFICADOS = "DATOS_MODIFICADOS";
    public static final String MENSAJE_DOCUMENTO_NO_PERTENECE_AL_EXPEDIENTE =
            "El documento indicado no pertenece a este expediente.";
    public static final String MENSAJE_DOCUMENTO_YA_ELIMINADO =
            "El documento indicado ya fue eliminado del expediente.";
    public static final String MENSAJE_DATOS_CONTRACTUALES_SIN_CAMBIO =
            "No se indicó ningún cambio en los datos de la solicitud.";
    public static final String ENTIDAD_BITACORA_TRAMITE = "SIMT_TRAMITE";

    // -----------------------------------------------------------------------
    // HU-034 Buscar y filtrar datos.
    // Responsable: Josue Guevara Lostalo. Fecha: 16/09/2026.
    // -----------------------------------------------------------------------

    public static final String CATEGORIA_ESTADO_PENDIENTE_INGRESO = "PEN";
    public static final String CATEGORIA_ESTADO_TRAMITE_ACTIVO = "ACT";
    public static final String CATEGORIA_ESTADO_TRAMITE_FINALIZADO = "FIN";
    public static final String NOMBRE_CATEGORIA_PENDIENTE_INGRESO = "Pendiente de ingreso";
    public static final String NOMBRE_CATEGORIA_TRAMITE_ACTIVO = "Trámite activo";
    public static final String NOMBRE_CATEGORIA_TRAMITE_FINALIZADO = "Trámite finalizado";
    public static final String NOMBRE_TIPO_EXONERACION_COMPRA_INSTITUCIONAL = "Compra institucional";
    public static final String NOMBRE_TIPO_EXONERACION_DONACION = "Donación";
    public static final String NOMBRE_TIPO_EXONERACION_CLIENTE_EXTERNO = "Cliente externo con contrato";
    public static final String FORMATO_EXPORTACION_EXCEL = "EXCEL";
    public static final String FORMATO_EXPORTACION_PDF = "PDF";
    public static final String FORMATO_EXPORTACION_WORD = "WORD";
    public static final String TEXTO_NO_REGISTRADO_AUN = "No registrado aún";
    public static final String TEXTO_SIN_DATO = "—";
    public static final String TITULO_LISTADO_TRAMITES = "Listado de trámites";
    public static final String NOMBRE_ARCHIVO_LISTADO_TRAMITES = "listado-tramites";
    public static final String MENSAJE_RANGO_FECHAS_INVALIDO =
            "La fecha inicial del periodo no puede ser posterior a la fecha final.";
    public static final String MENSAJE_REFERENCIA_BUSQUEDA_INVALIDA =
            "El identificador del trámite no debe superar los 45 caracteres.";
    public static final String MENSAJE_CATEGORIA_ESTADO_INVALIDA =
            "Seleccione un estado válido: pendiente de ingreso, trámite activo o trámite finalizado.";
    public static final String MENSAJE_FORMATO_EXPORTACION_INVALIDO =
            "Seleccione un formato de exportación válido: Excel, PDF o Word.";
    public static final String MENSAJE_LISTADO_NO_GENERADO =
            "No fue posible generar el archivo del listado. Intente exportarlo de nuevo.";

    // -----------------------------------------------------------------------
    // HU-044 Gestionar donaciones.
    // Responsable: Josue Guevara Lostalo. Fecha: 15/09/2026.
    // -----------------------------------------------------------------------

    public static final String ENTIDAD_BITACORA_DONACION = "SIMT_DONACION";

    public static final String MENSAJE_EXPEDIENTE_DONACION_NO_ENCONTRADO =
            "No existe un expediente de donación con esa referencia interna.";
    public static final String MENSAJE_ESTADO_DONACION_INVALIDO =
            "El estado indicado no está definido para los trámites de donación.";
    public static final String MENSAJE_EXPEDIENTE_DONACION_CERRADO =
            "El expediente está cerrado y no admite cambios. Su información y su historial se "
                    + "conservan para consulta.";
    public static final String MENSAJE_JUSTIFICACION_ESTADO_INVALIDA =
            "Debe indicar la justificación del cambio de estado, sin superar los 255 caracteres.";
    public static final String MENSAJE_ESTADO_ACTUAL_NO_DISPONIBLE =
            "El expediente tiene un estado que ya no está definido en el catálogo.";

    private Constantes() {
    }

    // -----------------------------------------------------------------------
    // HU-036 Generar reporte de consultas internas.
    // Responsable: Katherine Susana Valverde Fallas. Fecha: 25/09/2026.
    // -----------------------------------------------------------------------

    public static final String AGRUPACION_TIPO_TRAMITE = "TIPO_TRAMITE";
    public static final String AGRUPACION_REGIMEN_ADUANERO = "REGIMEN_ADUANERO";
    public static final String AGRUPACION_DEPENDENCIA = "DEPENDENCIA";
    public static final String AGRUPACION_PARTIDO_RESPONSABLE = "PARTIDO_RESPONSABLE";
    public static final String AGRUPACION_TIPO_EXONERACION = "TIPO_EXONERACION";
    public static final String AGRUPACION_MES = "MES";
    public static final String MONEDA_NO_DECLARADA = "Sin moneda";
    public static final String DATO_NO_REGISTRADO = "Sin registrar";
    public static final String ACCION_BITACORA_COSTOS_MODIFICADOS = "COSTOS_MODIFICADOS";
    public static final int LONGITUD_MAXIMA_PARTIDO_RESPONSABLE = 120;
    public static final String MENSAJE_AGRUPACION_INVALIDA =
            "Seleccione un criterio de agrupación válido: tipo de trámite, régimen aduanero, "
                    + "dependencia, tipo de exoneración o mes.";
    public static final String MENSAJE_RANGO_DE_FECHAS_INVALIDO =
            "La fecha final del rango no puede ser anterior a la inicial.";
    public static final String TIPO_PARTIDO_UNIDAD_IMPORTACIONES = "UNIDAD_IMPORTACIONES";
    public static final String TIPO_PARTIDO_SOLICITANTE = "PARTIDO_SOLICITANTE";
    public static final String TIPO_PARTIDO_ORDEN_COMPRA = "ORDEN_COMPRA";
    public static final String NOMBRE_UNIDAD_IMPORTACIONES = "Unidad de Importaciones";
    public static final String MENSAJE_TIPO_PARTIDO_INVALIDO =
            "Indique a quién se le carga el costo: la Unidad de Importaciones, el partido "
                    + "solicitante o una orden de compra.";
    public static final String MENSAJE_DETALLE_PARTIDO_REQUERIDO =
            "Indique el nombre del partido solicitante o el número de la orden de compra.";
    public static final String MENSAJE_PARTIDO_RESPONSABLE_INVALIDO =
            "El partido responsable del costo no puede pasar de 120 caracteres.";
    public static final String MENSAJE_MONTO_NO_PUEDE_SER_NEGATIVO =
            "Los impuestos y el bodegaje no pueden ser montos negativos.";
    public static final String MENSAJE_COSTOS_SIN_CAMBIO =
            "Los datos de costo del expediente no cambiaron.";
    public static final String MENSAJE_COSTOS_MODIFICADOS =
            "Los datos de costo se guardaron y el cambio quedó anotado en la bitácora.";
    public static final String MENSAJE_DATOS_DESALMACENAJE_SIN_CAMBIO =
            "Los datos del expediente no cambiaron.";
    public static final String MENSAJE_DATOS_DESALMACENAJE_MODIFICADOS =
            "Los datos del expediente se corrigieron y el cambio quedó anotado en la bitácora.";
    public static final String MENSAJE_REPORTE_NO_GENERADO =
            "No fue posible generar el archivo del reporte. Intente de nuevo.";
    public static final String NOMBRE_ARCHIVO_REPORTE = "reporte-consultas-internas";
    public static final String ENCABEZADO_UNIVERSIDAD = "Universidad de Costa Rica";
    public static final String ENCABEZADO_OFICINA = "Oficina de Suministros";
    public static final String ENCABEZADO_UNIDAD = "Unidad de Importaciones";
    public static final String RUTA_LOGO_INSTITUCIONAL = "reportes/logo-ucr.png";
    public static final String NOTA_REPORTE_MONEDAS =
            "Los montos se totalizan por moneda: el sistema no registra tipo de cambio, "
                    + "así que los colones y los dólares no se suman entre sí.";
    public static final String NOTA_REPORTE_MONTOS_PENDIENTES =
            "Los impuestos y el bodegaje se totalizan solo con lo que los expedientes tengan "
                    + "registrado: un trámite en proceso aún no los declara.";


    // -----------------------------------------------------------------------
    // HU-19 Revisar documentacion de exoneracion
    public static final String MENSAJE_DOCUMENTO_REVISION_NO_ACTIVO =
            "Solo se puede revisar la versión activa de un documento.";

    public static final String MENSAJE_ID_TRAMITE_REQUERIDO =
            "El identificador del trámite es obligatorio.";

    public static final String MENSAJE_EXONERACION_ID_NO_ENCONTRADA =
            "No existe una solicitud de exoneración con el identificador indicado.";

    public static final String MENSAJE_ID_DOCUMENTO_REQUERIDO =
            "El identificador del documento es obligatorio.";

    public static final String MENSAJE_DOCUMENTO_NO_ENCONTRADO =
            "No existe el documento indicado.";

    public static final String MENSAJE_USUARIO_REVISOR_REQUERIDO =
            "El usuario revisor es obligatorio.";

    public static final String MENSAJE_USUARIO_REVISOR_NO_ENCONTRADO =
            "No existe el usuario revisor indicado.";

    public static final String MENSAJE_DOCUMENTO_REQUERIDO =
            "El documento es obligatorio.";

    public static final String MENSAJE_ESTADO_REVISION_REQUERIDO =
            "El estado de revisión es obligatorio.";

    public static final String MENSAJE_ESTADO_REVISION_INVALIDO =
            "El estado de revisión indicado no es válido.";

    public static final String MENSAJE_OBSERVACION_REVISION_INVALIDA =
            "La observación no puede superar los 1000 caracteres.";

    public static final String MENSAJE_OBSERVACION_RECHAZO_REQUERIDA =
            "Debe indicar la observación que justifica el rechazo del documento.";

    public static final String MENSAJE_DOCUMENTO_INCORRECTO_EXONERACION =
            "El documento indicado no pertenece al expediente de exoneración.";

    // -----------------------------------------------------------------------
    // HU-020 Registrar observaciones o aclaraciones del expediente.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 04/10/2026.
    // -----------------------------------------------------------------------

    public static final String ESTADO_OBSERVACION_BORRADOR = "BOR";
    public static final String ESTADO_OBSERVACION_CONFIRMADA = "CON";
    public static final String ESTADO_REGISTRO_OBSERVACION_ACTIVO = "ACT";
    public static final String ESTADO_REGISTRO_OBSERVACION_INACTIVO = "INA";
    public static final int LONGITUD_MAXIMA_OBSERVACION_EXPEDIENTE = 1000;
    public static final int LONGITUD_MAXIMA_MOTIVO_OBSERVACION = 100;
    public static final String ACCION_BITACORA_OBSERVACION_CONFIRMADA = "OBSERVACION_CONFIRMADA";
    public static final String ENTIDAD_BITACORA_OBSERVACION = "SIMT_OBSERVACION_EXPED";
    public static final String PREFIJO_BITACORA_MOTIVO_OBSERVACION = "Motivo: ";
    public static final String MENSAJE_OBSERVACION_INVALIDA =
            "Escriba la observación, con un máximo de 1000 caracteres.";
    public static final String MENSAJE_MOTIVO_OBSERVACION_INVALIDO =
            "El motivo es opcional. Si lo indica, debe tener un máximo de 100 caracteres.";
    public static final String MENSAJE_ID_OBSERVACION_REQUERIDO =
            "El identificador de la observación es obligatorio.";
    public static final String MENSAJE_BORRADOR_NO_DISPONIBLE =
            "El borrador indicado no existe en este expediente o no le pertenece.";
    public static final String MENSAJE_OBSERVACION_CONFIRMADA_INMUTABLE =
            "La observación ya fue confirmada y forma parte del historial del expediente: "
                    + "no puede modificarse ni eliminarse.";
    public static final String MENSAJE_USUARIO_OBSERVACION_INVALIDO =
            "No fue posible identificar al usuario que registra la observación. Inicie sesión de nuevo.";

    // -----------------------------------------------------------------------
    // HU-021 Controlar documentos obligatorios del trámite.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
    // -----------------------------------------------------------------------

    public static final String ESTADO_REVISION_RECIBIDO = "RECIBIDO";
    public static final String ESTADO_REVISION_RECHAZADO = "RECHAZADO";
    public static final String ESTADO_CONTROL_CUMPLIDO = "CUMPLIDO";
    public static final String ESTADO_CONTROL_FALTANTE = "FALTANTE";
    public static final String ESTADO_CONTROL_POR_REVISAR = "POR_REVISAR";
    public static final String ESTADO_CONTROL_RECHAZADO = "RECHAZADO";
    public static final String ESTADO_DOCUMENTAL_EXONERACION_COMPLETO = "COMPLETO";
    public static final String ESTADO_DOCUMENTAL_EXONERACION_INCOMPLETO = "INCOMPLETO";
    public static final String NOMBRE_ESTADO_PENDIENTE_APROBACION_EXONERACION = "Pendiente de aprobación";
    public static final String JUSTIFICACION_ENVIO_APROBACION_EXONERACION =
            "Documentación obligatoria completa: el expediente continúa a aprobación (HU-021).";
    public static final String MOTIVO_CONTROL_FALTANTE = "sin adjuntar";
    public static final String MOTIVO_CONTROL_POR_REVISAR = "adjunto, pendiente de revisión";
    public static final String MOTIVO_CONTROL_RECHAZADO = "rechazado en la revisión";
    public static final String MENSAJE_USUARIO_CONTROL_DOCUMENTAL_INVALIDO =
            "No fue posible identificar al usuario que continúa el trámite. Inicie sesión de nuevo.";
    public static final String MENSAJE_SIN_REQUISITOS_DOCUMENTALES =
            "No hay documentos obligatorios configurados para este tipo de exoneración: "
                    + "no es posible confirmar el cumplimiento documental.";
    public static final String MENSAJE_DOCUMENTACION_OBLIGATORIA_INCOMPLETA =
            "No es posible continuar a aprobación: la documentación obligatoria está incompleta. Pendientes: ";
    public static final String MENSAJE_ESTADO_PENDIENTE_APROBACION_NO_CONFIGURADO =
            "El estado \"Pendiente de aprobación\" de la exoneración no está configurado en el catálogo.";
    public static final String MENSAJE_EXONERACION_YA_EN_APROBACION =
            "El expediente ya fue enviado a aprobación.";
    public static final String MENSAJE_EXONERACION_CERRADA =
            "El expediente está cerrado: no puede continuar a aprobación.";

    // -----------------------------------------------------------------------
    // HU-022 Aprobar solicitud de exoneración.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
    // -----------------------------------------------------------------------

    // El permiso para decidir es Permisos.EXONERACION_APROBAR (única fuente de códigos de permiso).
    public static final String DECISION_EXONERACION_APROBAR = "APR";
    public static final String DECISION_EXONERACION_RECHAZAR = "REC";
    public static final String DECISION_EXONERACION_MODIFICAR = "MOD";
    public static final String NOMBRE_ESTADO_REVISION_DOCUMENTAL_EXONERACION = "Pendiente revisión documental";
    public static final String NOMBRE_ESTADO_EXONERACION_APROBADA = "Exoneración aprobada";
    public static final String NOMBRE_ESTADO_EXONERACION_RECHAZADA = "Exoneración rechazada";
    public static final String NOMBRE_ESTADO_PENDIENTE_MODIFICACION_EXONERACION = "Pendiente de modificación";
    public static final String ESTADO_ENVIO_NOTIFICACION_PENDIENTE = "PEN";
    public static final String ESTADO_ENVIO_NOTIFICACION_ENVIADA = "ENV";
    public static final String ESTADO_ENVIO_NOTIFICACION_ERROR = "ERR";
    public static final String ESTADO_REGISTRO_DECISION_ACTIVO = "ACT";
    public static final String ACCION_BITACORA_DECISION_EXONERACION = "DECISION_EXONERACION";
    public static final String ENTIDAD_BITACORA_DECISION_EXONERACION = "SIMT_DECISION_EXONERACION";
    public static final int LONGITUD_MAXIMA_JUSTIFICACION_DECISION = 1000;
    public static final String MENSAJE_USUARIO_NO_AUTORIZADO_APROBAR =
            "No cuenta con autorización para aprobar solicitudes de exoneración.";
    public static final String MENSAJE_USUARIO_DECISION_INVALIDO =
            "No fue posible identificar al usuario que registra la decisión. Inicie sesión de nuevo.";
    public static final String MENSAJE_DECISION_INVALIDA =
            "Seleccione una decisión válida: aprobar, rechazar o solicitar modificación.";
    public static final String MENSAJE_JUSTIFICACION_DECISION_REQUERIDA =
            "Escriba la justificación: es obligatoria al rechazar o al solicitar una modificación.";
    public static final String MENSAJE_JUSTIFICACION_DECISION_INVALIDA =
            "La justificación no puede superar los 1000 caracteres.";
    public static final String MENSAJE_EXONERACION_NO_PENDIENTE_APROBACION =
            "Solo se puede decidir sobre un expediente en estado \"Pendiente de aprobación\".";
    public static final String MENSAJE_EXONERACION_INCOMPLETA_NO_APROBABLE =
            "No es posible aprobar: la documentación obligatoria está incompleta. Pendientes: ";
    public static final String MENSAJE_ESTADO_DECISION_NO_CONFIGURADO =
            "El estado resultante de la decisión no está configurado en el catálogo de la exoneración.";
    public static final String MENSAJE_EXONERACION_NO_PERMITE_ENVIO_APROBACION =
            "El expediente no puede continuar a aprobación desde su estado actual.";

    // -----------------------------------------------------------------------
    // HU-030 Consultar trámites activos.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
    // -----------------------------------------------------------------------

    public static final String GRUPO_PANEL_POR_LLEGAR = "POR_LLEGAR";
    public static final String GRUPO_PANEL_EN_PROCESO = "EN_PROCESO";
    public static final String GRUPO_PANEL_PERMISOS = "PERMISOS";
    public static final String GRUPO_PANEL_APROBACION = "APROBACION";
    public static final String GRUPO_PANEL_EXONERACION = "EXONERACION";
    public static final String GRUPO_PANEL_FUERA = "FUERA";
    public static final String NOMBRE_GRUPO_PANEL_POR_LLEGAR = "Por llegar";
    public static final String NOMBRE_GRUPO_PANEL_EN_PROCESO = "En proceso";
    public static final String NOMBRE_GRUPO_PANEL_PERMISOS = "Pendiente de permisos";
    public static final String NOMBRE_GRUPO_PANEL_APROBACION = "Pendiente de aprobación";
    public static final String NOMBRE_GRUPO_PANEL_EXONERACION = "Pendiente de exoneración";
    public static final String NOMBRE_GRUPO_PANEL_FUERA = "Fuera del panel";
    public static final String ALCANCE_PANEL_ACTIVOS = "ACTIVOS";
    public static final String ALCANCE_PANEL_HISTORICO = "HISTORICO";
    public static final int LIMITE_HISTORICO_PANEL = 500;
    public static final String MENSAJE_ALCANCE_PANEL_INVALIDO =
            "Seleccione una vista válida del panel: trámites activos o histórico.";
  
    // HU-023 Registrar resolución / autorización EXONET.
    // Responsable: Equipo de Desarrollo SIM. Fecha: 05/10/2026.
    // Los nombres visibles de los datos se concentran aquí para que el texto de interfaz
    // ("Número de autorización", provisional) pueda cambiar sin tocar el modelo.
    // -----------------------------------------------------------------------

    public static final String ESTADO_AUTORIZACION_VIGENTE = "VIG";
    public static final String ESTADO_AUTORIZACION_ANULADA = "ANU";
    public static final String ESTADO_AUTORIZACION_SUSTITUIDA = "SUS";
    public static final String CODIGO_TIPO_DOCUMENTO_AUTORIZACION_EXONET = "AUTORIZACION_EXONET";
    public static final String ACCION_BITACORA_AUTORIZACION_REGISTRADA = "AUTORIZACION_REGISTRADA";
    public static final String ACCION_BITACORA_AUTORIZACION_COMPLETADA = "AUTORIZACION_COMPLETADA";
    public static final String ACCION_BITACORA_AUTORIZACION_ANULADA = "AUTORIZACION_ANULADA";
    public static final String ACCION_BITACORA_AUTORIZACION_SUSTITUIDA = "AUTORIZACION_SUSTITUIDA";
    public static final String ENTIDAD_BITACORA_AUTORIZACION_EXONET = "SIMT_AUTORIZACION_EXONET";
    public static final int LONGITUD_MAXIMA_NUMERO_EXONET = 45;
    public static final int LONGITUD_MAXIMA_OBSERVACIONES_AUTORIZACION = 1000;
    public static final int LONGITUD_MAXIMA_MOTIVO_CAMBIO_AUTORIZACION = 500;
    // Literales históricos que representan ausencia de dato: se guardan como NULL (K-12).
    public static final String VALOR_SIN_DATO_NULO = "NULO";
    public static final String VALOR_SIN_DATO_NO_APLICA = "NO APLICA";
    public static final String FORMATO_FECHA_BITACORA_AUTORIZACION = "dd/MM/yyyy";

    public static final String NOMBRE_ESTADO_AUTORIZACION_VIGENTE = "Vigente";
    public static final String NOMBRE_ESTADO_AUTORIZACION_ANULADA = "Anulada";
    public static final String NOMBRE_ESTADO_AUTORIZACION_SUSTITUIDA = "Sustituida";
    public static final String CAMPO_AUTORIZACION_NUMERO_SOLICITUD = "Número de solicitud EXONET";
    public static final String CAMPO_AUTORIZACION_FECHA_ENVIO = "Fecha de envío a EXONET";
    public static final String CAMPO_AUTORIZACION_NUMERO_AUTORIZACION = "Número de autorización";
    public static final String CAMPO_AUTORIZACION_FECHA_AUTORIZACION = "Fecha de autorización";
    public static final String CAMPO_AUTORIZACION_FECHA_COMUNICACION = "Fecha de comunicación";
    public static final String CAMPO_AUTORIZACION_OBSERVACIONES = "Observaciones";
    public static final String CAMPO_AUTORIZACION_DOCUMENTO = "Documento";
    public static final String PREFIJO_BITACORA_AUTORIZACION = "Autorización EXONET #";
    public static final String PREFIJO_BITACORA_MOTIVO_AUTORIZACION = "Motivo: ";

    public static final String MENSAJE_ID_AUTORIZACION_REQUERIDO =
            "El identificador de la autorización EXONET es obligatorio.";
    public static final String MENSAJE_USUARIO_AUTORIZACION_INVALIDO =
            "No fue posible identificar al usuario que registra la autorización. Inicie sesión de nuevo.";
    public static final String MENSAJE_AUTORIZACION_NO_DISPONIBLE =
            "La autorización EXONET indicada no existe en este expediente.";
    public static final String MENSAJE_AUTORIZACION_NO_VIGENTE =
            "La autorización EXONET ya fue anulada o sustituida: forma parte del historial y no "
                    + "puede completarse, anularse ni sustituirse.";
    public static final String MENSAJE_AUTORIZACION_VIGENTE_EXISTENTE =
            "El expediente ya tiene una autorización EXONET vigente. Para cambiarla, sustitúyala o anúlela.";
    public static final String MENSAJE_IDENTIFICADOR_EXONET_REQUERIDO =
            "Indique el número de solicitud EXONET o el número de autorización.";
    public static final String MENSAJE_NUMERO_SOLICITUD_EXONET_LONGITUD =
            "El número de solicitud EXONET debe tener un máximo de 45 caracteres.";
    public static final String MENSAJE_NUMERO_AUTORIZACION_LONGITUD =
            "El número de autorización debe tener un máximo de 45 caracteres.";
    public static final String MENSAJE_OBSERVACIONES_AUTORIZACION_LONGITUD =
            "Las observaciones deben tener un máximo de 1000 caracteres.";
    public static final String MENSAJE_FECHA_AUTORIZACION_FUTURA =
            "Las fechas de envío, autorización y comunicación no pueden ser posteriores a hoy.";
    public static final String MENSAJE_AUTORIZACION_SIN_FECHA_O_NUMERO =
            "El número de autorización y la fecha de autorización deben indicarse juntos.";
    public static final String MENSAJE_FECHA_ENVIO_SIN_SOLICITUD_EXONET =
            "La fecha de envío a EXONET requiere que el número de solicitud EXONET esté registrado.";
    public static final String MENSAJE_FECHA_COMUNICACION_SIN_AUTORIZACION =
            "La fecha de comunicación requiere que la autorización ya esté registrada.";
    public static final String MENSAJE_ORDEN_FECHAS_AUTORIZACION =
            "Las fechas deben respetar el orden: envío a EXONET, autorización y comunicación.";
    public static final String MENSAJE_MOTIVO_CAMBIO_AUTORIZACION_INVALIDO =
            "Indique el motivo, con un máximo de 500 caracteres.";
    public static final String MENSAJE_DATO_AUTORIZACION_YA_REGISTRADO =
            "El dato «%s» ya está registrado y no puede sobrescribirse. Si debe cambiar, "
                    + "registre una autorización sustituta.";
    public static final String MENSAJE_AUTORIZACION_SIN_DATOS_PENDIENTES =
            "No se indicó ningún dato pendiente para completar.";
}
