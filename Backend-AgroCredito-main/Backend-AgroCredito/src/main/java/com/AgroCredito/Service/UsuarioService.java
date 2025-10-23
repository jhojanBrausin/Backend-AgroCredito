package com.AgroCredito.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.AgroCredito.Dto.Request.ActualizarPerfilRequest;
import com.AgroCredito.Dto.Request.CambiarPasswordRequest;
import com.AgroCredito.Model.Usuario;
import com.AgroCredito.Repository.UsuarioRepository;
import com.AgroCredito.Response.UsuarioResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UsuarioResponse obtenerPerfil(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirAUsuarioResponse(usuario);
    }
    
    public UsuarioResponse actualizarPerfil(String correo, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (request.getNombres() != null) {
            usuario.setNombres(request.getNombres());
        }
        
        if (request.getTelefono() != null) {
            usuario.setTelefono(request.getTelefono());
        }
        
        if (request.getActividadEconomica() != null) {
            usuario.setActividadEconomica(request.getActividadEconomica());
        }
        
        if (request.getIngresosAprox() != null) {
            usuario.setIngresosAprox(request.getIngresosAprox());
        }
        
        if (request.getUbicacion() != null) {
            Usuario.Ubicacion ubicacion = new Usuario.Ubicacion();
            ubicacion.setDepartamento(request.getUbicacion().getDepartamento());
            ubicacion.setMunicipio(request.getUbicacion().getMunicipio());
            ubicacion.setVereda(request.getUbicacion().getVereda());
            
            if (request.getUbicacion().getCoordenadas() != null) {
                Usuario.Ubicacion.Coordenadas coordenadas = new Usuario.Ubicacion.Coordenadas();
                coordenadas.setLat(request.getUbicacion().getCoordenadas().getLat());
                coordenadas.setLng(request.getUbicacion().getCoordenadas().getLng());
                ubicacion.setCoordenadas(coordenadas);
            }
            
            usuario.setUbicacion(ubicacion);
        }
        
        usuario = usuarioRepository.save(usuario);
        return convertirAUsuarioResponse(usuario);
    }
    
    public void cambiarPassword(String correo, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Validar contraseña actual
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        
        // Validar que las contraseñas coincidan
        if (!request.getPasswordNueva().equals(request.getPasswordConfirmacion())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }
        
        usuario.setPassword(passwordEncoder.encode(request.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }
    
    public UsuarioResponse obtenerUsuarioPorId(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return convertirAUsuarioResponse(usuario);
    }
    
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAUsuarioResponse)
                .collect(Collectors.toList());
    }
    
    public List<UsuarioResponse> listarSoloUsuarios() {
        return usuarioRepository.findByRol("usuario").stream()
                .map(this::convertirAUsuarioResponse)
                .collect(Collectors.toList());
    }
    
    public void eliminarUsuario(String id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
    
    private UsuarioResponse convertirAUsuarioResponse(Usuario usuario) {
        
        UsuarioResponse.UbicacionResponse.CoordenadasResponse coordenadas = null;
        if (usuario.getUbicacion() != null && usuario.getUbicacion().getCoordenadas() != null) {
            coordenadas = UsuarioResponse.UbicacionResponse.CoordenadasResponse.builder()
                    .lat(usuario.getUbicacion().getCoordenadas().getLat())
                    .lng(usuario.getUbicacion().getCoordenadas().getLng())
                    .build();
        }
        
        UsuarioResponse.UbicacionResponse ubicacion = null;
        if (usuario.getUbicacion() != null) {
            ubicacion = UsuarioResponse.UbicacionResponse.builder()
                    .departamento(usuario.getUbicacion().getDepartamento())
                    .municipio(usuario.getUbicacion().getMunicipio())
                    .vereda(usuario.getUbicacion().getVereda())
                    .coordenadas(coordenadas)
                    .build();
        }
        
        UsuarioResponse.EstadisticasResponse estadisticas = null;
        if (usuario.getEstadisticas() != null) {
            estadisticas = UsuarioResponse.EstadisticasResponse.builder()
                    .totalCreditosSolicitados(usuario.getEstadisticas().getTotalCreditosSolicitados())
                    .totalCreditosAprobados(usuario.getEstadisticas().getTotalCreditosAprobados())
                    .totalCreditosActivos(usuario.getEstadisticas().getTotalCreditosActivos())
                    .historialCrediticio(usuario.getEstadisticas().getHistorialCrediticio())
                    .build();
        }
        
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .tipoIdentificacion(usuario.getTipoIdentificacion())
                .identificacion(usuario.getIdentificacion())
                .fechaNacimiento(usuario.getFecha_nacimiento())
                .correo(usuario.getCorreo())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .ubicacion(ubicacion)
                .actividadEconomica(usuario.getActividadEconomica())
                .ingresosAprox(usuario.getIngresosAprox())
                .fechaRegistro(usuario.getFechaRegistro())
                .estadisticas(estadisticas)
                .build();
    }
}
