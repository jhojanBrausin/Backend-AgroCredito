package com.AgroCredito.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.AgroCredito.Model.Credito;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditoRepository extends MongoRepository<Credito, String> {
    
    List<Credito> findByIdUsuario(String idUsuario);
    
    List<Credito> findByEstado(String estado);
    
    List<Credito> findByIdUsuarioAndEstado(String idUsuario, String estado);
    
    Optional<Credito> findByIdSolicitud(String idSolicitud);
}
