package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RechazarPagoAdminDTO {
	
	 @NotBlank(message = "El ID del crédito es obligatorio para rechazar el pago.")
	    private String idCredito;

	    @NotBlank(message = "El motivo de rechazo es obligatorio.")
	    @Size(min = 10, max = 500, message = "El motivo de rechazo debe tener entre 10 y 500 caracteres.")
	    private String motivoRechazo;
}
