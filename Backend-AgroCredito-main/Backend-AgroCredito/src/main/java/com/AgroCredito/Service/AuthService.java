package com.AgroCredito.Service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.AgroCredito.Dto.Request.LoginRequest;
import com.AgroCredito.Dto.Request.RegistroRequest;
import com.AgroCredito.Model.Usuario;
import com.AgroCredito.Repository.UsuarioRepository;
import com.AgroCredito.Response.AuthResponse;
import com.AgroCredito.Response.UsuarioResponse;
import com.AgroCredito.Security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthResponse registrar(RegistroRequest request) {
        
        
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        
        
        if (usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new RuntimeException("La identificación ya está registrada");
        }
        
        
        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setTipoIdentificacion(request.getTipoIdentificacion());
        usuario.setIdentificacion(request.getIdentificacion());
        usuario.setFecha_nacimiento(request.getFechaNacimiento());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefono(request.getTelefono());
        usuario.setRol("usuario");
        usuario.setActividadEconomica(request.getActividadEconomica());
        usuario.setIngresosAprox(request.getIngresosAprox());
        usuario.setFechaRegistro(LocalDateTime.now());
        
        
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
        
        
        Usuario.Estadisticas estadisticas = new Usuario.Estadisticas();
        estadisticas.setTotalCreditosSolicitados(0);
        estadisticas.setTotalCreditosAprobados(0);
        estadisticas.setTotalCreditosActivos(0);
        estadisticas.setHistorialCrediticio("bueno");
        usuario.setEstadisticas(estadisticas);
        
        // Guardar usuario
        usuario = usuarioRepository.save(usuario);
        
        // Generar tokens
        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getCorreo());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .usuario(convertirAUsuarioResponse(usuario))
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        
        // Buscar usuario por correo
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        
        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // Generar tokens
        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        String refreshToken = jwtUtil.generarRefreshToken(usuario.getCorreo());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tipo("Bearer")
                .usuario(convertirAUsuarioResponse(usuario))
                .build();
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        
        if (!jwtUtil.validarToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }
        
        String correo = jwtUtil.getCorreoFromToken(refreshToken);
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        String nuevoToken = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        String nuevoRefreshToken = jwtUtil.generarRefreshToken(usuario.getCorreo());
        
        return AuthResponse.builder()
                .token(nuevoToken)
                .refreshToken(nuevoRefreshToken)
                .tipo("Bearer")
                .usuario(convertirAUsuarioResponse(usuario))
                .build();
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
