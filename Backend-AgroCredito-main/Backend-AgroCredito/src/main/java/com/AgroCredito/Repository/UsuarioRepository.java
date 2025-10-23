package com.AgroCredito.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.AgroCredito.Model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    Optional<Usuario> findByCorreo(String correo);
    
    Optional<Usuario> findByIdentificacion(String identificacion);
    
    boolean existsByCorreo(String correo);
    
    boolean existsByIdentificacion(String identificacion);
    
    List<Usuario> findByRol(String rol);
}
