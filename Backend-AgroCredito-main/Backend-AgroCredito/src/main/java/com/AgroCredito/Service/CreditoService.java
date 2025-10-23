package com.AgroCredito.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.AgroCredito.Model.Credito;
import com.AgroCredito.Model.Usuario;
import com.AgroCredito.Repository.CreditoRepository;
import com.AgroCredito.Repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CreditoService {
    
    @Autowired
    private CreditoRepository creditoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public List<Credito> obtenerTodosLosCreditos() {
        return creditoRepository.findAll();
    }
    
    public Optional<Credito> obtenerCreditoPorId(String id) {
        return creditoRepository.findById(id);
    }
    
    public List<Credito> obtenerCreditosPorUsuario(String idUsuario) {
        return creditoRepository.findByIdUsuario(idUsuario);
    }
    
    public List<Credito> obtenerCreditosPorEstado(String estado) {
        return creditoRepository.findByEstado(estado);
    }
    
    public Optional<Credito> obtenerCreditoPorSolicitud(String idSolicitud) {
        return creditoRepository.findByIdSolicitud(idSolicitud);
    }
    
    public Credito crearCredito(Credito credito) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(credito.getIdUsuario());
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        
        credito.setFechaAprobacion(LocalDateTime.now());
        credito.setFechaVencimiento(LocalDateTime.now().plusMonths(credito.getPlazoMeses()));
        credito.setEstado("activo");
        credito.setSaldoPendiente(credito.getMontoAprobado());
        credito.setTotalPagado(0.0);
        
        if (credito.getMetricas() == null) {
            credito.setMetricas(new Credito.Metricas());
        }
        
        double interesMensualDecimal = credito.getInteresMensual() / 100;
        double totalConIntereses = credito.getMontoAprobado() * (1 + (interesMensualDecimal * credito.getPlazoMeses()));
        credito.setCuotaMensual(totalConIntereses / credito.getPlazoMeses());
        
        Credito creditoGuardado = creditoRepository.save(credito);
        
        Usuario usuario = usuarioOpt.get();
        if (usuario.getEstadisticas() == null) {
            usuario.setEstadisticas(new Usuario.Estadisticas());
        }
        usuario.getEstadisticas().setTotalCreditosAprobados(
            usuario.getEstadisticas().getTotalCreditosAprobados() + 1
        );
        usuario.getEstadisticas().setTotalCreditosActivos(
            usuario.getEstadisticas().getTotalCreditosActivos() + 1
        );
        usuarioRepository.save(usuario);
        
        return creditoGuardado;
    }
    
    public Credito actualizarCredito(String id, Credito credito) {
        Optional<Credito> creditoExistente = creditoRepository.findById(id);
        if (creditoExistente.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }
        credito.setId(id);
        credito.setFechaAprobacion(creditoExistente.get().getFechaAprobacion());
        return creditoRepository.save(credito);
    }
    
    public Credito registrarPago(String id, Credito.Pago pago) {
        Optional<Credito> creditoOpt = creditoRepository.findById(id);
        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }
        
        Credito credito = creditoOpt.get();
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado("completado");
        
        double interesMensualDecimal = credito.getInteresMensual() / 100;
        double interesPago = credito.getSaldoPendiente() * interesMensualDecimal;
        double capitalPago = pago.getMonto() - interesPago;
        
        pago.setInteresPagado(interesPago);
        pago.setCapitalPagado(capitalPago > 0 ? capitalPago : 0);
        
        double nuevoSaldo = credito.getSaldoPendiente() - pago.getCapitalPagado();
        pago.setSaldoRestante(nuevoSaldo > 0 ? nuevoSaldo : 0);
        credito.setSaldoPendiente(pago.getSaldoRestante());
        credito.setTotalPagado(credito.getTotalPagado() + pago.getMonto());
        
        credito.getHistorialPagos().add(pago);
        
        credito.getMetricas().setPagosRealizados(
            credito.getMetricas().getPagosRealizados() + 1
        );
        
        if (credito.getSaldoPendiente() <= 0) {
            credito.setEstado("completado");
        }
        
        return creditoRepository.save(credito);
    }
    
    public Credito agregarEvidenciaCultivo(String id, Credito.EvidenciaCultivo evidencia) {
        Optional<Credito> creditoOpt = creditoRepository.findById(id);
        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }
        
        Credito credito = creditoOpt.get();
        credito.getEvidenciasCultivo().add(evidencia);
        return creditoRepository.save(credito);
    }
    
    public Credito cambiarEstadoCredito(String id, String nuevoEstado) {
        Optional<Credito> creditoOpt = creditoRepository.findById(id);
        if (creditoOpt.isEmpty()) {
            throw new RuntimeException("Crédito no encontrado");
        }
        
        Credito credito = creditoOpt.get();
        String estadoAnterior = credito.getEstado();
        credito.setEstado(nuevoEstado);
        
        if (estadoAnterior.equals("activo") && !nuevoEstado.equals("activo")) {
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(credito.getIdUsuario());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                if (usuario.getEstadisticas() != null) {
                    usuario.getEstadisticas().setTotalCreditosActivos(
                        Math.max(0, usuario.getEstadisticas().getTotalCreditosActivos() - 1)
                    );
                    usuarioRepository.save(usuario);
                }
            }
        }
        
        return creditoRepository.save(credito);
    }
    
    public void eliminarCredito(String id) {
        if (!creditoRepository.existsById(id)) {
            throw new RuntimeException("Crédito no encontrado");
        }
        creditoRepository.deleteById(id);
    }
}