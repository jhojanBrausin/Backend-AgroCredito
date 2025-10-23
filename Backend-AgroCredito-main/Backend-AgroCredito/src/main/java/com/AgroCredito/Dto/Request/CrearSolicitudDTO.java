package com.AgroCredito.Dto.Request;

import jakarta.validation.Valid;
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
public class CrearSolicitudDTO {
    
    @NotNull(message = "El monto solicitado es obligatorio")
    @Min(value = 5000000, message = "El monto mínimo es $5'000,000")
    @Max(value = 100000000, message = "El monto máximo es $100'000,000")
    private Double montoSolicitado;
    
    @NotBlank(message = "El destino del crédito es obligatorio")
    @Size(min = 10, max = 500, message = "El destino debe tener entre 10 y 500 caracteres")
    private String destinoCredito;
    
    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es 1 mes")
    @Max(value = 60, message = "El plazo máximo es 60 meses")
    private Integer plazoMeses;
    
    @NotBlank(message = "La garantía es obligatoria")
    @Size(min = 5, max = 300, message = "La garantía debe tener entre 5 y 300 caracteres")
    private String garantia;
    
    // Proyecto productivo (opcional pero recomendado)
    @Valid
    private ProyectoProductivoDTO proyectoProductivo;
}
