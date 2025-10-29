package com.AgroCredito.Dto.Request;

import java.util.List;

import lombok.Data;

@Data
public class SubirEvidenciaCultivoDTO {
    
    private String idCredito;
    
    private String tipoCultivo;
    
    private List<ImagenCultivoDTO> imagenes;
}

