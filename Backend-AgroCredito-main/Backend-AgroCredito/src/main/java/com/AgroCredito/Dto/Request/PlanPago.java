package com.AgroCredito.Dto.Request;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanPago {

    private String id; // Puede ser el número de cuota
    
    @Field("numero_cuota")
    private Integer numeroCuota;
    
    @Field("fecha_vencimiento")
    private LocalDateTime fechaVencimiento;
    
    @Field("monto_cuota")
    private Double montoCuota; // Usamos Double para precisión en cálculos
    
    private Double capital;
    
    private Double interes;
    
    @Field("saldo_pendiente")
    private Double saldoPendiente;
    
    private String estado; // Por ejemplo: "PENDIENTE", "PAGADO", "VENCIDO"
    
    // Campo opcional para hacer referencia al pago real si ya se realizó
    @Field("id_pago_realizado")
    private String idPagoRealizado; 
}