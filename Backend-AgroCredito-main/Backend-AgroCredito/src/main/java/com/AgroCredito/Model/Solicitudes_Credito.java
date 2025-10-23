package com.AgroCredito.Model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "solicitudes_credito")
public class Solicitudes_Credito {
    
    @Id
    private String id;
    
    @Field("id_usuario")
    private String idUsuario;
    
    private Solicitante solicitante;
    
    @Field("monto_solicitado")
    private Double montoSolicitado;
    
    @Field("destino_credito")
    private String destinoCredito;
    
    @Field("plazo_meses")
    private Integer plazoMeses;
    
    private String garantia;
    
    @Field("referencias_verificadas")
    private Boolean referenciasVerificadas;
    
    private String estado;
    
    @Field("fecha_solicitud")
    private LocalDateTime fechaSolicitud;
    
    private Evaluacion evaluacion;
    
    @Field("proyecto_productivo")
    private ProyectoProductivo proyectoProductivo;
    
 
    @Data
    public static class Solicitante {
        private String nombres;
        private String identificacion;
        private String telefono;
        private UbicacionBasica ubicacion;
        
        @Data
        public static class UbicacionBasica {
            private String departamento;
            private String municipio;
            private String vereda;
        }
    }
    
   
    @Data
    public static class Evaluacion {
        @Field("id_administrador")
        private String idAdministrador;
        
        @Field("nombre_administrador")
        private String nombreAdministrador;
        
        @Field("fecha_revision")
        private LocalDateTime fechaRevision;
        
        private String observaciones;
        private Integer puntaje;
    }
    
   
    @Data
    public static class ProyectoProductivo {
        private String nombre;
        private String descripcion;
        
        @Field("costo_estimado")
        private Double costoEstimado;
        
        @Field("ingresos_estimados")
        private Double ingresosEstimados;
        
        @Field("impacto_comunitario")
        private String impactoComunitario;
        
        @Field("duracion_meses")
        private Integer duracionMeses;
        
        private List<ImagenReferencia> imagenes = new ArrayList<>();
        
        @Data
        public static class ImagenReferencia {
            @Field("file_id")
            private String fileId;
            
            private String filename;
            
            @Field("contentType")
            private String contentType;
            
            private String descripcion;
        }
    }
}
