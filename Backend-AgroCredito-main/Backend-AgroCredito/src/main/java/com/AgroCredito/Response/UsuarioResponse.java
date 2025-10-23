package com.AgroCredito.Response;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String nombres;
    private String tipoIdentificacion;
    private String identificacion;
    private Date fechaNacimiento;
    private String correo;
    private String telefono;
    private String rol;
    private UbicacionResponse ubicacion;
    private String actividadEconomica;
    private Double ingresosAprox;
    private LocalDateTime fechaRegistro;
    private EstadisticasResponse estadisticas;
    
    @Data
    @Builder
    public static class UbicacionResponse {
        private String departamento;
        private String municipio;
        private String vereda;
        private CoordenadasResponse coordenadas;
        
        @Data
        @Builder
        public static class CoordenadasResponse {
            private Double lat;
            private Double lng;
        }
    }
    
    @Data
    @Builder
    public static class EstadisticasResponse {
        private Integer totalCreditosSolicitados;
        private Integer totalCreditosAprobados;
        private Integer totalCreditosActivos;
        private String historialCrediticio;
    }
}
