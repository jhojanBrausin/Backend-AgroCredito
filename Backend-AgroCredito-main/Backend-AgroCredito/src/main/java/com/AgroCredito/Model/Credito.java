package com.AgroCredito.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "creditos")
public class Credito {
    
    @Id
    private String id;
    
    @Field("id_solicitud")
    private String idSolicitud;
    
    @Field("id_usuario")
    private String idUsuario;
    
    @Field("usuario_embedido")
    private UsuarioEmbedido usuarioEmbedido;
    
    @Field("solicitud_embedida")
    private SolicitudEmbedida solicitudEmbedida;
    
    private Aprobador aprobador;
    
    @Field("monto_aprobado")
    private Double montoAprobado;
    
    @Field("interes_mensual")
    private Double interesMensual;
    
    @Field("plazo_meses")
    private Integer plazoMeses;
    
    @Field("cuota_mensual")
    private Long cuotaMensual;
    
    @Field("fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
    
    @Field("fecha_vencimiento")
    private Date fechaVencimiento;
    
    private String estado; // "activo", "pagado", "vencido", "cancelado"
    
    @Field("saldo_pendiente")
    private Double saldoPendiente;
    
    @Field("total_pagado")
    private Double totalPagado;
    
    @Field("historial_pagos")
    private List<HistorialPago> historialPagos = new ArrayList<>();
    
    @Field("evidencias_cultivo")
    private List<EvidenciaCultivo> evidenciasCultivo = new ArrayList<>();
    
    private Metricas metricas;
    
    // ==========================================
    // CLASES INTERNAS
    // ==========================================
    
    /**
     * Usuario Embedido
     */
    @Data
    public static class UsuarioEmbedido {
        private String nombres;
        private String identificacion;
        private String telefono;
        private String correo;
        private String rol;
        
        @Field("ubicacion_principal")
        private UbicacionPrincipal ubicacionPrincipal;
        
        @Field("actividad_economica")
        private String actividadEconomica;
        
        @Field("ingresos_aprox")
        private Double ingresosAprox;
        
        @Field("historial_crediticio_resumen")
        private String historialCrediticioResumen;
    }
    
    /**
     * Ubicación Principal
     */
    @Data
    public static class UbicacionPrincipal {
        private String departamento;
        private String municipio;
        private String vereda;
    }
    
    /**
     * Solicitud Embedida
     */
    @Data
    public static class SolicitudEmbedida {
        @Field("fecha_solicitud")
        private Date fechaSolicitud;
        
        @Field("monto_solicitado")
        private Double montoSolicitado;
        
        @Field("destino_credito")
        private String destinoCredito;
        
        @Field("plazo_meses_solicitado")
        private Integer plazoMesesSolicitado;
        
        private String garantia;
        
        @Field("puntaje_evaluacion")
        private Integer puntajeEvaluacion;
        
        @Field("proyecto_productivo_resumen")
        private ProyectoProductivoResumen proyectoProductivoResumen;
    }
    
    /**
     * Resumen del Proyecto Productivo
     */
    @Data
    public static class ProyectoProductivoResumen {
        private String nombre;
        private String descripcion;
        
        @Field("duracion_meses")
        private Integer duracionMeses;
    }
    
    /**
     * Aprobador del Crédito
     */
    @Data
    public static class Aprobador {
        private String id;
        private String nombres;
        private String rol;
    }
    
    /**
     * Historial de Pago
     */
    @Data
    public static class HistorialPago {
        @Id
        private String id;
        
        @Field("fecha_pago")
        private Date fechaPago;
        
        private Double monto;
        
        @Field("metodo_pago")
        private String metodoPago;
        
        @Field("comprobante_file")
        private ComprobanteFile comprobanteFile;
        
        private String estado; // "pendiente", "confirmado", "rechazado"
        
        @Field("interes_pagado")
        private Double interesPagado;
        
        @Field("capital_pagado")
        private Double capitalPagado;
        
        @Field("saldo_restante")
        private Double saldoRestante;
    }
    
    /**
     * Comprobante de Pago
     */
    @Data
    public static class ComprobanteFile {
        @Field("file_id")
        private String fileId;
        
        private String filename;
        
        @Field("contentType")
        private String contentType;
    }
    
    /**
     * Evidencias del Cultivo
     */
    @Data
    public static class EvidenciaCultivo {
        @Field("tipo_cultivo")
        private String tipoCultivo;
        
        private List<ImagenCultivo> imagenes = new ArrayList<>();
    }
    
    /**
     * Imagen del Cultivo
     */
    @Data
    public static class ImagenCultivo {
        @Field("file_id")
        private String fileId;
        
        private String filename;
        
        @Field("contentType")
        private String contentType;
        
        private String descripcion;
        private Date fecha;
    }
    
    /**
     * Métricas del Crédito
     */
    @Data
    public static class Metricas {
        @Field("pagos_realizados")
        private Integer pagosRealizados = 0;
        
        @Field("pagos_atrasados")
        private Integer pagosAtrasados = 0;
        
        @Field("dias_mora_actual")
        private Integer diasMoraActual = 0;
        
        @Field("cumplimiento_porcentaje")
        private Integer cumplimientoPorcentaje = 100;
    }
}