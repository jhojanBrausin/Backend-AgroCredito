package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarEstadoCreditoDTO {
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
        regexp = "activo|pagado|vencido|cancelado|en mora",
        message = "El estado debe ser: activo, pagado, vencido, en mora o cancelado"
    )
    private String estado;
    
    @Size(max = 300, message = "Las observaciones no pueden exceder 300 caracteres")
    private String observaciones;
}
