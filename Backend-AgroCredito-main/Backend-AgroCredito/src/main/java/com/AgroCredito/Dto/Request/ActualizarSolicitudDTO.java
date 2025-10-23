package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarSolicitudDTO {
    
    @Min(value = 100000, message = "El monto mínimo es $100,000")
    @Max(value = 50000000, message = "El monto máximo es $50,000,000")
    private Double montoSolicitado;
    
    @Size(min = 10, max = 500, message = "El destino debe tener entre 10 y 500 caracteres")
    private String destinoCredito;
    
    @Min(value = 1, message = "El plazo mínimo es 1 mes")
    @Max(value = 60, message = "El plazo máximo es 60 meses")
    private Integer plazoMeses;
    
    @Size(min = 5, max = 300, message = "La garantía debe tener entre 5 y 300 caracteres")
    private String garantia;
}
