package com.AgroCredito.Controller;

import com.AgroCredito.Service.GridFSService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final GridFSService gridFSService;
    

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> subirArchivo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        
        try {
            String fileId = descripcion != null 
                ? gridFSService.guardarArchivoConMetadata(file, descripcion)
                : gridFSService.guardarArchivo(file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("file_id", fileId);
            response.put("filename", file.getOriginalFilename());
            response.put("contentType", file.getContentType());
            response.put("size", file.getSize());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al subir el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
 
    @GetMapping("/{fileId}")
    public ResponseEntity<InputStreamResource> descargarArchivo(@PathVariable String fileId) {
        try {
            GridFSFile file = gridFSService.obtenerArchivo(fileId);
            
            if (file == null) {
                return ResponseEntity.notFound().build();
            }
            
            InputStreamResource resource = new InputStreamResource(
                gridFSService.obtenerArchivoStream(fileId)
            );
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getMetadata().get("_contentType").toString()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + file.getFilename() + "\"")
                .body(resource);
                
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Map<String, Object>> eliminarArchivo(@PathVariable String fileId) {
        try {
            if (!gridFSService.archivoExiste(fileId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Archivo no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            gridFSService.eliminarArchivo(fileId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Archivo eliminado correctamente");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Error al eliminar el archivo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}