package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechazarSolicitudDTO {
    
    @NotBlank(message = "El motivo del rechazo es obligatorio")
    @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
    private String motivo;
}
