package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AprobarSolicitudDTO {
    
    @NotNull(message = "El monto aprobado es obligatorio")
    @Min(value = 100000, message = "El monto mínimo es $100,000")
    private Double montoAprobado;
    
    @NotNull(message = "El interés mensual es obligatorio")
    @DecimalMin(value = "0.5", message = "El interés mensual mínimo es 0.5%")
    @DecimalMax(value = "5.0", message = "El interés mensual máximo es 5.0%")
    private Double interesMensual;
    
    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es 1 mes")
    @Max(value = 60, message = "El plazo máximo es 60 meses")
    private Integer plazoMeses;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}

