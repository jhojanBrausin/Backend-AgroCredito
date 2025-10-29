package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgregarEvidenciaCultivoDTO {
    
    @NotBlank(message = "El tipo de cultivo es obligatorio")
    @Size(min = 3, max = 100, message = "El tipo de cultivo debe tener entre 3 y 100 caracteres")
    private String tipoCultivo;
    
    @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
    private String descripcion;
}