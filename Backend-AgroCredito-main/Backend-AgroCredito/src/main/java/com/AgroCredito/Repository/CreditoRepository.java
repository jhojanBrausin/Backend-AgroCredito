package com.AgroCredito.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.AgroCredito.Model.Credito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditoRepository extends MongoRepository<Credito, String> {
    
	
	List<Credito> findByIdUsuario(String idUsuario);
    
	
        List<Credito> findByEstado(String estado);
    
        
        List<Credito> findByIdUsuarioAndEstado(String idUsuario, String estado);
    
        
        Optional<Credito> findByIdSolicitud(String idSolicitud);
    
        
        @Query("{'fecha_aprobacion': {$gte: ?0, $lte: ?1}}")
    List<Credito> findByFechaAprobacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
        
        @Query("{'usuario_embedido.ubicacion_principal.departamento': ?0}")
    List<Credito> findByDepartamento(String departamento);
    
        
        long countByEstado(String estado);
    
        
        @Query(value = "{'id_usuario': ?0, 'estado': {$in: ['activo']}}", exists = true)
    boolean existeCreditoActivoParaUsuario(String idUsuario);
    
        
        List<Credito> findAllByOrderByFechaAprobacionDesc();
}
