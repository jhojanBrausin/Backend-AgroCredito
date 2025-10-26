package com.AgroCredito.Repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.AgroCredito.Model.Solicitudes_Credito;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudesCreditoRepository extends MongoRepository<Solicitudes_Credito, String> {
    
    
	List<Solicitudes_Credito> findByIdUsuario(String idUsuario);
    
    
    List<Solicitudes_Credito> findByEstado(String estado);
    
    
    List<Solicitudes_Credito> findByIdUsuarioAndEstado(String idUsuario, String estado);
    
    
    @Query(value = "{'id_usuario': ?0, 'estado': {$in: ['en revisión', 'aprobado']}}", exists = true)
    boolean existsSolicitudesActivas(String idUsuario);
   
        long countByIdUsuario(String idUsuario);
    
    
        Optional<Solicitudes_Credito> findByIdAndIdUsuario(String id, String idUsuario);
}
