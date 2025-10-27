package com.AgroCredito.Controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.AgroCredito.Dto.Request.AprobarSolicitudDTO;
import com.AgroCredito.Dto.Request.CambiarEstadoCreditoDTO;
import com.AgroCredito.Dto.Request.EvaluarSolicitudDTO;
import com.AgroCredito.Dto.Request.PlanPago;
import com.AgroCredito.Dto.Request.RechazarSolicitudDTO;
import com.AgroCredito.Dto.Request.RegistrarPagoDTO;
import com.AgroCredito.Model.Credito;
import com.AgroCredito.Model.Credito.HistorialPago;
import com.AgroCredito.Model.Solicitudes_Credito;
import com.AgroCredito.Service.CreditoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api") // Base corregida a /api para permitir rutas absolutas como /api/admin/...
@CrossOrigin(origins = "*") 
public class CreditoController {

    @Autowired
    private CreditoService creditoService;

    // Se asume el uso de UserDetails para obtener el nombre de usuario (correo)

    // ============================================
    // 3. EVALUACIÓN Y APROBACIÓN (Administradores)
    // Rutas: /api/admin/solicitudes...
    // ============================================

    /**
     * GET /api/admin/solicitudes - Listar todas las solicitudes con filtros
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/solicitudes")
    public ResponseEntity<List<Solicitudes_Credito>> listarSolicitudesConFiltros(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde) {
        
        List<Solicitudes_Credito> solicitudes = creditoService.listarSolicitudesConFiltros(estado, departamento, fechaDesde);
        return ResponseEntity.ok(solicitudes);
    }

    /**
     * GET /api/admin/solicitudes/pendientes - Solicitudes pendientes de revisión
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/solicitudes/pendientes")
    public ResponseEntity<List<Solicitudes_Credito>> listarSolicitudesPendientes() {
        return ResponseEntity.ok(creditoService.listarSolicitudesPendientes());
    }

    /**
     * PUT /api/admin/solicitudes/{id}/evaluar - Evaluar solicitud
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/solicitudes/{id}/evaluar")
    public ResponseEntity<Solicitudes_Credito> evaluarSolicitud(
            @PathVariable("id") String idSolicitud,
            @Valid @RequestBody EvaluarSolicitudDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Solicitudes_Credito solicitudActualizada = creditoService.evaluarSolicitud(
            idSolicitud, dto, userDetails.getUsername()
        );
        return ResponseEntity.ok(solicitudActualizada);
    }

    /**
     * PUT /api/admin/solicitudes/{id}/aprobar - Aprobar solicitud y crear crédito
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/solicitudes/{id}/aprobar")
    public ResponseEntity<Credito> aprobarSolicitud(
            @PathVariable("id") String idSolicitud,
            @Valid @RequestBody AprobarSolicitudDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Credito nuevoCredito = creditoService.aprobarSolicitud(
            idSolicitud, dto, userDetails.getUsername()
        );
        return new ResponseEntity<>(nuevoCredito, HttpStatus.CREATED);
    }

    /**
     * PUT /api/admin/solicitudes/{id}/rechazar - Rechazar solicitud
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/solicitudes/{id}/rechazar")
    public ResponseEntity<Solicitudes_Credito> rechazarSolicitud(
            @PathVariable("id") String idSolicitud,
            @Valid @RequestBody RechazarSolicitudDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Solicitudes_Credito solicitudRechazada = creditoService.rechazarSolicitud(
            idSolicitud, dto, userDetails.getUsername()
        );
        return ResponseEntity.ok(solicitudRechazada);
    }
    
    // ============================================
    // REPORTES (Administradores)
    // ============================================

    /**
     * GET /api/admin/reportes/solicitudes - Reporte de solicitudes por estado/fecha
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reportes/solicitudes")
    public ResponseEntity<?> reporteSolicitudes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde) {
        // Asumiendo que el servicio tiene un método para generar el reporte
        return ResponseEntity.ok(creditoService.generarReporteSolicitudes(estado, fechaDesde));
    }
    
    /**
     * GET /api/admin/reportes/usuarios - Reporte de usuarios registrados
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/reportes/usuarios")
    public ResponseEntity<?> reporteUsuarios() {
        return ResponseEntity.ok(creditoService.generarReporteUsuarios());
    }
    
    /**
     * GET /api/admin/estadisticas - Dashboard con estadísticas generales
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/estadisticas")
    public ResponseEntity<?> dashboardEstadisticas() {
        return ResponseEntity.ok(creditoService.obtenerEstadisticasGenerales());
    }

    // ============================================
    // 4. CRÉDITOS APROBADOS (Usuario y Admin)
    // Rutas: /api/creditos... y /api/admin/creditos...
    // ============================================

    // CONSULTA DE CRÉDITOS (Usuario)
    
    /**
     * GET /api/creditos - Listar créditos del usuario autenticado
     */
    @GetMapping("/creditos") 
    public ResponseEntity<List<Credito>> listarCreditosUsuario(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<Credito> creditos = creditoService.listarCreditosUsuario(userDetails.getUsername());
        return ResponseEntity.ok(creditos);
    }

    /**
     * GET /api/creditos/{id} - Obtener detalle de un crédito específico
     */
    @GetMapping("/creditos/{id}")
    public ResponseEntity<Credito> obtenerDetalleCredito(
            @PathVariable("id") String idCredito,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Credito credito = creditoService.obtenerDetalleCredito(idCredito, userDetails.getUsername());
        return ResponseEntity.ok(credito);
    }

    /**
     * GET /api/creditos/activos - Obtener solo créditos activos
     */
    @GetMapping("/creditos/activos")
    public ResponseEntity<List<Credito>> obtenerCreditosActivos(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<Credito> activos = creditoService.listarCreditosActivosUsuario(userDetails.getUsername());
        return ResponseEntity.ok(activos);
    }
    
    /**
     * GET /api/creditos/:id/plan-pagos - Ver plan de pagos del crédito
     */
    @GetMapping("/creditos/{id}/plan-pagos")
    public ResponseEntity<List<PlanPago>> generarPlanPagos(@PathVariable("id") String idCredito) {
        return ResponseEntity.ok(creditoService.generarPlanPagos(idCredito));
    }

    // GESTIÓN ADMINISTRATIVA
    
    /**
     * GET /api/admin/creditos - Listar todos los créditos (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/creditos")
    public ResponseEntity<List<Credito>> listarTodosCreditos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario) { // usuario es idUsuario
        
        List<Credito> creditos = creditoService.listarCreditosAdmin(estado, usuario);
        return ResponseEntity.ok(creditos);
    }

    /**
     * PUT /api/admin/creditos/{id}/estado - Cambiar estado del crédito (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/creditos/{id}/estado")
    public ResponseEntity<Credito> cambiarEstadoCredito(
            @PathVariable("id") String idCredito,
            @Valid @RequestBody CambiarEstadoCreditoDTO dto) {
        
        Credito creditoActualizado = creditoService.cambiarEstadoCredito(idCredito, dto);
        return ResponseEntity.ok(creditoActualizado);
    }
    
    // ============================================
    // 5. PAGOS
    // Rutas: /api/pagos... y /api/admin/pagos...
    // ============================================

    // REGISTRO Y CONSULTA DE PAGOS (Usuario)
    
    /**
     * POST /api/pagos - Registrar un nuevo pago (inicialmente pendiente)
     */
    @PostMapping("/pagos")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Credito> registrarPago(
            @Valid @RequestBody RegistrarPagoDTO dto, 
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Credito credito = creditoService.registrarPago(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(credito);
    }
    
    /**
     * GET /api/pagos/:idCredito - Obtener historial de pagos de un crédito
     */
    @GetMapping("/pagos/{idCredito}")
    public ResponseEntity<List<HistorialPago>> obtenerHistorialPagos(
            @PathVariable("idCredito") String idCredito, 
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Se asume que el servicio valida que el crédito sea del usuario autenticado
        return ResponseEntity.ok(creditoService.obtenerHistorialPagos(idCredito, userDetails.getUsername()));
    }

    /**
     * GET /api/pagos/:idCredito/:idPago - Detalle de un pago específico
     */
    @GetMapping("/pagos/{idCredito}/{idPago}")
    public ResponseEntity<HistorialPago> obtenerDetallePago(
            @PathVariable("idCredito") String idCredito,
            @PathVariable("idPago") String idPago,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Se asume que el servicio valida que el crédito sea del usuario autenticado
        return ResponseEntity.ok(creditoService.obtenerDetallePago(idCredito, idPago, userDetails.getUsername()));
    }

    /**
     * PUT /api/pagos/:idCredito/:idPago/comprobante - Actualizar comprobante de pago
     * NOTA: El path debe incluir idCredito porque el pago está incrustado en Credito.
     */
    @PutMapping("/pagos/{idCredito}/{idPago}/comprobante")
    public ResponseEntity<Credito> actualizarComprobante(
            @PathVariable("idCredito") String idCredito,
            @PathVariable("idPago") String idPago,
            @Valid @RequestBody SubirComprobanteDTO dto) {
        
        // La validación de pertenencia se delega al servicio o se realiza con PreAuthorize
        Credito credito = creditoService.actualizarComprobante(idCredito, idPago, dto);
        return ResponseEntity.ok(credito);
    }

    // CONFIRMACIÓN ADMINISTRATIVA DE PAGOS
    
    /**
     * PUT /api/admin/pagos/{idPago}/confirmar - Confirmar pago (admin)
     * NOTA: idCredito se requiere en el body para buscar el pago incrustado.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/pagos/{idPago}/confirmar")
    public ResponseEntity<Credito> confirmarPago(
            @PathVariable("idPago") String idPago,
            @Valid @RequestBody ConfirmarPagoAdminDTO dto) {
        
        Credito credito = creditoService.confirmarPago(dto.getIdCredito(), idPago);
        return ResponseEntity.ok(credito);
    }

    /**
     * PUT /api/admin/pagos/{idPago}/rechazar - Rechazar pago (admin)
     * NOTA: idCredito y motivo se requieren en el body.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/pagos/{idPago}/rechazar")
    public ResponseEntity<Credito> rechazarPago(
            @PathVariable("idPago") String idPago,
            @Valid @RequestBody RechazarPagoAdminDTO dto) {
        
        Credito credito = creditoService.rechazarPago(dto.getIdCredito(), idPago, dto.getMotivoRechazo());
        return ResponseEntity.ok(credito);
    }
}
