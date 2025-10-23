package com.AgroCredito.Dto.Request;

import lombok.Data;

@Data
public class ActualizarPerfilRequest {
    
    private String nombres;
    private String telefono;
    private String actividadEconomica;
    private Double ingresosAprox;
    private RegistroRequest.UbicacionRequest ubicacion;
}