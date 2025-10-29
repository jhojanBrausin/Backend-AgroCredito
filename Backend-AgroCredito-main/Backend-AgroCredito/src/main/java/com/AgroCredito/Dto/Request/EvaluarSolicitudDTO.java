package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluarSolicitudDTO {
    
    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 0, message = "El puntaje mínimo es 0")
    @Max(value = 100, message = "El puntaje máximo es 100")
    private Integer puntaje;
    
    @NotBlank(message = "Las observaciones son obligatorias")
    @Size(min = 10, max = 1000, message = "Las observaciones deben tener entre 10 y 1000 caracteres")
    private String observaciones;
}
