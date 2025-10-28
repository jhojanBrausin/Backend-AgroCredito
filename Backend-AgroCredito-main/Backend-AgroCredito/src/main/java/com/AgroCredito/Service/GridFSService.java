package com.AgroCredito.Service;

import com.AgroCredito.Dto.Request.SubirComprobanteDTO;
import com.AgroCredito.Model.Credito;
import com.AgroCredito.Model.Solicitudes_Credito;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class GridFSService {
    
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;
    
    /**
     * Guarda un archivo en GridFS
     * @param file Archivo a guardar
     * @return ID del archivo guardado en GridFS
     */
    public String guardarArchivo(MultipartFile file) throws IOException {
        ObjectId fileId = gridFsTemplate.store(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        );
        return fileId.toString();
    }
    
    /**
     * Guarda un archivo con metadata personalizada
     */
    public String guardarArchivoConMetadata(MultipartFile file, String descripcion) throws IOException {
        ObjectId fileId = gridFsTemplate.store(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType(),
            descripcion
        );
        return fileId.toString();
    }
    
    /**
     * Obtiene un archivo de GridFS por su ID
     */
    public GridFSFile obtenerArchivo(String fileId) {
        return gridFsTemplate.findOne(
            new Query(Criteria.where("_id").is(new ObjectId(fileId)))
        );
    }
    
    /**
     * Obtiene el InputStream de un archivo
     */
    public InputStream obtenerArchivoStream(String fileId) throws IOException {
        GridFSFile file = obtenerArchivo(fileId);
        if (file == null) {
            throw new IOException("Archivo no encontrado: " + fileId);
        }
        return gridFsOperations.getResource(file).getInputStream();
    }
    
    /**
     * Elimina un archivo de GridFS
     */
    public void eliminarArchivo(String fileId) {
        gridFsTemplate.delete(
            new Query(Criteria.where("_id").is(new ObjectId(fileId)))
        );
    }
    
    /**
     * Verifica si un archivo existe
     */
    public boolean archivoExiste(String fileId) {
        return obtenerArchivo(fileId) != null;
    }
    
public SubirComprobanteDTO subirComprobante(MultipartFile file) throws IOException {
        
	ObjectId fileId = gridFsTemplate.store(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        );

        // 🚨 Crear y retornar SubirComprobanteDTO
        SubirComprobanteDTO comprobanteMetadata = new SubirComprobanteDTO();
        comprobanteMetadata.setFileId(fileId.toString());
        comprobanteMetadata.setFilename(file.getOriginalFilename());
        comprobanteMetadata.setContentType(file.getContentType());
        
        return comprobanteMetadata;
    }
}