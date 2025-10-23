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
    
    // Buscar solicitudes por ID de usuario
    List<Solicitudes_Credito> findByIdUsuario(String idUsuario);
    
    // Buscar solicitudes por estado
    List<Solicitudes_Credito> findByEstado(String estado);
    
    // Buscar solicitudes por usuario y estado
    List<Solicitudes_Credito> findByIdUsuarioAndEstado(String idUsuario, String estado);
    
    // Verificar si un usuario tiene solicitudes activas
    @Query(value = "{'id_usuario': ?0, 'estado': {$in: ['en revisión', 'aprobado']}}", exists = true)
    boolean existsSolicitudesActivas(String idUsuario);
    
    // Contar solicitudes por usuario
    long countByIdUsuario(String idUsuario);
    
    // Buscar por ID y usuario (para validar pertenencia)
    Optional<Solicitudes_Credito> findByIdAndIdUsuario(String id, String idUsuario);
}
