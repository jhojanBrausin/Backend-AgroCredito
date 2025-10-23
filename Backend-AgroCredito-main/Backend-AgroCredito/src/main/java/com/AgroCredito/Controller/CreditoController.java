package com.AgroCredito.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.AgroCredito.Model.Credito;
import com.AgroCredito.Service.CreditoService;

@RestController
@RequestMapping("/api/creditos")
@CrossOrigin(origins = "*")
public class CreditoController {
    
    @Autowired
    private CreditoService creditoService;
    
    // Aquí irán los endpoints
}
