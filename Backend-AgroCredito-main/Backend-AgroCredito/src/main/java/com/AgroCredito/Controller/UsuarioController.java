package com.AgroCredito.Controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.AgroCredito.Dto.Request.ActualizarPerfilRequest;
import com.AgroCredito.Dto.Request.CambiarPasswordRequest;
import com.AgroCredito.Response.ApiResponse;
import com.AgroCredito.Response.UsuarioResponse;
import com.AgroCredito.Service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {
    
    private final UsuarioService usuarioService;
    
    /**
     * GET /api/usuarios/perfil - Obtener perfil del usuario autenticado
     */
    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPerfil(Authentication authentication) {
        try {
            String correo = authentication.getName();
            UsuarioResponse usuario = usuarioService.obtenerPerfil(correo);
            return ResponseEntity.ok(
                ApiResponse.success("Perfil obtenido exitosamente", usuario)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * PUT /api/usuarios/perfil - Actualizar información del perfil
     */
    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody ActualizarPerfilRequest request) {
        try {
            String correo = authentication.getName();
            UsuarioResponse usuario = usuarioService.actualizarPerfil(correo, request);
            return ResponseEntity.ok(
                ApiResponse.success("Perfil actualizado exitosamente", usuario)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * PUT /api/auth/cambiar-password - Cambiar contraseña
     */
    @PutMapping("/cambiar-password")
    public ResponseEntity<ApiResponse<String>> cambiarPassword(
            Authentication authentication,
            @Valid @RequestBody CambiarPasswordRequest request) {
        try {
            String correo = authentication.getName();
            usuarioService.cambiarPassword(correo, request);
            return ResponseEntity.ok(
                ApiResponse.success("Contraseña cambiada exitosamente", null)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * GET /api/usuarios/:id - Obtener información de un usuario (admin)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerUsuario(@PathVariable String id) {
        try {
            UsuarioResponse usuario = usuarioService.obtenerUsuarioPorId(id);
            return ResponseEntity.ok(
                ApiResponse.success("Usuario obtenido exitosamente", usuario)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * GET /api/usuarios - Listar todos los usuarios (admin)
     */
    @GetMapping
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listarUsuarios() {
        try {
            List<UsuarioResponse> usuarios = usuarioService.listarSoloUsuarios();
            return ResponseEntity.ok(
                ApiResponse.success("Usuarios obtenidos exitosamente", usuarios)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
    
    /**
     * DELETE /api/usuarios/:id - Eliminar usuario (admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('administrador')")
    public ResponseEntity<ApiResponse<String>> eliminarUsuario(@PathVariable String id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.ok(
                ApiResponse.success("Usuario eliminado exitosamente", null)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        }
    }
}
