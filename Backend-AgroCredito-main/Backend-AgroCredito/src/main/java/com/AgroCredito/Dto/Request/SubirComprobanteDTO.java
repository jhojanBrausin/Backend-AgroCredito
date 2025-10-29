package com.AgroCredito.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubirComprobanteDTO {

    // Asume que fileId es el identificador del archivo en un servicio de almacenamiento (ej. S3, Firebase Storage)
    @NotBlank(message = "El ID del archivo de comprobante es obligatorio.")
    private String fileId;

    @NotBlank(message = "El nombre del archivo es obligatorio.")
    @Size(max = 255, message = "El nombre del archivo no puede superar los 255 caracteres.")
    private String filename;

    @NotBlank(message = "El tipo de contenido (mime type) es obligatorio.")
    private String contentType;

}
