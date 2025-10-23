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
@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    
    private String nombres;
    private String identificacion;
    private String correo;
    private String password;
    private String telefono;
    private String rol;
    private Date fecha_nacimiento;
    private String tipoIdentificacion;
    
    private Ubicacion ubicacion;
    
    @Field("actividad_economica")
    private String actividadEconomica;
    
    @Field("ingresos_aprox")
    private Double ingresosAprox;
    
    @Field("fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Field("referencias_comunitarias")
    private List<ReferenciaComunitaria> referenciaComunitarias = new ArrayList<>();
    
    private List<Notificacion> notificaciones = new ArrayList<>();
    
    private Estadisticas estadisticas;
    
    @Data
    public static class ArchivoReferencia {
        @Field("file_id")
        private String fileId; 
        
        private String filename;
        
        @Field("contentType")
        private String contentType;
    }
    
    
    @Data
    public static class Ubicacion {
        private String departamento;
        private String municipio;
        private String vereda;
        private Coordenadas coordenadas;
        
        @Data
        public static class Coordenadas {
            private Double lat;
            private Double lng;
        }
    }
    
    
    @Data
    public static class ReferenciaComunitaria {
        private String nombre;
        
        @Field("rol_comunitario")
        private String rolComunitario;
        
        private String telefono;
        private String concepto;
        
        @Field("fecha_registro")
        private LocalDateTime fechaRegistro;
    }
    
    @Data
    public static class Notificacion {
        private String titulo;
        private String mensaje;
        private String tipo; 
        
        @Field("fecha_envio")
        private LocalDateTime fechaEnvio;
        
        private Boolean leido = false;
    }
    
   
    @Data
    public static class Estadisticas {
        @Field("total_creditos_solicitados")
        private Integer totalCreditosSolicitados = 0;
        
        @Field("total_creditos_aprobados")
        private Integer totalCreditosAprobados = 0;
        
        @Field("total_creditos_activos")
        private Integer totalCreditosActivos = 0;
        
        @Field("historial_crediticio")
        private String historialCrediticio; 
    }
}
