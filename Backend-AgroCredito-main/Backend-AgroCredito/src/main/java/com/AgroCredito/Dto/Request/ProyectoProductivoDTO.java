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
public class ProyectoProductivoDTO {
    
    @NotBlank(message = "El nombre del proyecto es obligatorio")
    @Size(min = 5, max = 200, message = "El nombre debe tener entre 5 y 200 caracteres")
    private String nombre;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, max = 5000, message = "La descripción debe tener entre 20 y 5000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El costo estimado es obligatorio")
    @Min(value = 0, message = "El costo no puede ser negativo")
    private Double costoEstimado;
    
    @NotNull(message = "Los ingresos estimados son obligatorios")
    @Min(value = 0, message = "Los ingresos no pueden ser negativos")
    private Double ingresosEstimados;
    
    @NotBlank(message = "El impacto comunitario es obligatorio")
    @Size(min = 10, max = 5000, message = "El impacto debe tener entre 10 y 5000 caracteres")
    private String impactoComunitario;
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración mínima es 1 mes")
    @Max(value = 120, message = "La duración máxima es 120 meses")
    private Integer duracionMeses;
    
    // Descripción opcional para la imagen
    private String descripcionImagen;


}
