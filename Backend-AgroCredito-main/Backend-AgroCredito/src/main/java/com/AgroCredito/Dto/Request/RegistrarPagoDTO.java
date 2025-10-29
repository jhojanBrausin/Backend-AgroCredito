package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarPagoDTO {
    
    @NotBlank(message = "El ID del crédito es obligatorio")
    private String idCredito;
    
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1000, message = "El monto mínimo es $1,000")
    private Double monto;
    
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(
        regexp = "transferencia|efectivo|pse|tarjeta",
        message = "El método debe ser: transferencia, efectivo, pse o tarjeta"
    )
    private String metodoPago;
    
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
    
 
}
