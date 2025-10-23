package com.AgroCredito.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    private UsuarioSnapshot usuario;
    private Aprobador aprobador;
    
    @Field("monto_aprobado")
    private Double montoAprobado;
    
    @Field("interes_mensual")
    private Double interesMensual;
    
    @Field("plazo_meses")
    private Integer plazoMeses;
    
    @Field("cuota_mensual")
    private Double cuotaMensual;
    
    @Field("fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
    
    @Field("fecha_vencimiento")
    private LocalDateTime fechaVencimiento;
    
    private String estado; 
    
    @Field("saldo_pendiente")
    private Double saldoPendiente;
    
    @Field("total_pagado")
    private Double totalPagado;
    
    @Field("historial_pagos")
    private List<Pago> historialPagos = new ArrayList<>();
    
    @Field("evidencias_cultivo")
    private List<EvidenciaCultivo> evidenciasCultivo = new ArrayList<>();
    
    private Metricas metricas;
    
    
    @Data
    public static class UsuarioSnapshot {
        private String nombres;
        private String identificacion;
        private String telefono;
    }
    
    
    @Data
    public static class Aprobador {
        private String id;
        private String nombres;
        private String rol;
    }
    
    
    @Data
    public static class Pago {
        @Id
        private String id;
        
        @Field("fecha_pago")
        private LocalDateTime fechaPago;
        
        private Double monto;
        
        @Field("metodo_pago")
        private String metodoPago;
        
        @Field("comprobante_file")
        private ComprobanteReferencia comprobanteFile;
        
        private String estado; 
        
        @Field("interes_pagado")
        private Double interesPagado;
        
        @Field("capital_pagado")
        private Double capitalPagado;
        
        @Field("saldo_restante")
        private Double saldoRestante;
        
        @Data
        public static class ComprobanteReferencia {
            @Field("file_id")
            private String fileId;
            
            private String filename;
            
            @Field("contentType")
            private String contentType;
        }
    }
    
    
    @Data
    public static class EvidenciaCultivo {
        @Field("tipo_cultivo")
        private String tipoCultivo;
        
        private List<ImagenEvidencia> imagenes = new ArrayList<>();
        
        @Data
        public static class ImagenEvidencia {
            @Field("file_id")
            private String fileId;
            
            private String filename;
            
            @Field("contentType")
            private String contentType;
            
            private String descripcion;
            private LocalDateTime fecha;
        }
    }
    
    
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
