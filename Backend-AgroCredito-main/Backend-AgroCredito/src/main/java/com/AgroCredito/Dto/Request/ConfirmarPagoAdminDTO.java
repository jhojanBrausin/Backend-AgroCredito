package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmarPagoAdminDTO {

    @NotBlank(message = "El ID del crédito es obligatorio para confirmar el pago.")
    private String idCredito;

}
