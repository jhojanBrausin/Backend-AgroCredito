package com.AgroCredito.Dto.Request;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class RegistroRequest {
    
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombres;
    
    @NotBlank(message = "El tipo de identificación es obligatorio")
    private String tipoIdentificacion; // "Cédula de ciudadanía", "Cédula de extranjería", "Pasaporte"
    
    @NotBlank(message = "La identificación es obligatoria")
    @Pattern(regexp = "^[0-9]{6,15}$", message = "La identificación debe contener entre 6 y 15 números")
    private String identificacion;
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    private Date fechaNacimiento;
    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String correo;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    private String telefono;
    
    @NotBlank(message = "La actividad económica es obligatoria")
    private String actividadEconomica;
    
    @NotNull(message = "Los ingresos aproximados son obligatorios")
    @Min(value = 0, message = "Los ingresos deben ser mayores o iguales a 0")
    private Double ingresosAprox;
    
    @NotNull(message = "La ubicación es obligatoria")
    private UbicacionRequest ubicacion;
    
    @Data
    public static class UbicacionRequest {
        @NotBlank(message = "El departamento es obligatorio")
        private String departamento;
        
        @NotBlank(message = "El municipio es obligatorio")
        private String municipio;
        
        @NotBlank(message = "La vereda es obligatoria")
        private String vereda;
        
        private CoordenadasRequest coordenadas;
        
        @Data
        public static class CoordenadasRequest {
            @NotNull(message = "La latitud es obligatoria")
            private Double lat;
            
            @NotNull(message = "La longitud es obligatoria")
            private Double lng;
        }
    }
}
