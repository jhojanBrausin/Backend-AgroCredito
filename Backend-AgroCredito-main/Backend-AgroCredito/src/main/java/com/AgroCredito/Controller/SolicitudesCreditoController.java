package com.AgroCredito.Controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.AgroCredito.Dto.Request.ActualizarSolicitudDTO;
import com.AgroCredito.Dto.Request.CrearSolicitudDTO;
import com.AgroCredito.Dto.Request.ProyectoProductivoDTO;
import com.AgroCredito.Model.Solicitudes_Credito;
import com.AgroCredito.Model.Solicitudes_Credito.ProyectoProductivo;
import com.AgroCredito.Service.SolicitudesCreditoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudesCreditoController {

    @Autowired
    private SolicitudesCreditoService solicitudService;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    /**
     * Crear nueva solicitud de crédito con proyecto productivo e imagen
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearSolicitud(
            @RequestPart(value = "solicitud") String solicitudJson,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            Authentication authentication) {

        try {
            System.out.println("=== DEBUG: Solicitud JSON recibido ===");
            System.out.println(solicitudJson);
            System.out.println("=== FIN DEBUG ===");

            // Parsear el JSON manualmente
            ObjectMapper objectMapper = new ObjectMapper();
            CrearSolicitudDTO dto;

            try {
                dto = objectMapper.readValue(solicitudJson, CrearSolicitudDTO.class);
            } catch (com.fasterxml.jackson.core.JsonParseException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "JSON inválido o incompleto",
                                "detalle", e.getMessage(),
                                "ayuda", "Verifica que el JSON esté completo y tenga todas las llaves cerradas { }"
                        ));
            }

            // Validar el DTO
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<CrearSolicitudDTO>> violations = validator.validate(dto);

            if (!violations.isEmpty()) {
                List<String> errors = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Errores de validación", "detalles", errors));
            }

            String idUsuario = authentication.getName();
            // Corrección: Tipo de retorno Solicitudes_Credito
            Solicitudes_Credito solicitud = solicitudService.crearSolicitud(dto, idUsuario, imagen); 

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Solicitud creada exitosamente");
            response.put("solicitud", solicitud);
            response.put("tieneImagen", imagen != null && !imagen.isEmpty());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al procesar la imagen",
                            "detalle", e.getMessage()
                    ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "tipo", e.getClass().getSimpleName()
                    ));
        }
    }

    /**
     * Agregar imagen adicional al proyecto
     */
    @PostMapping("/{id}/evidencias")
    public ResponseEntity<?> agregarImagenProyecto(
            @PathVariable String id,
            @RequestParam("imagen") MultipartFile imagen,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            Authentication authentication) {

        try {
            String idUsuario = authentication.getName();

            // Corrección: Tipo de retorno Solicitudes_Credito
            Solicitudes_Credito solicitud = solicitudService.agregarImagenProyecto(
                    id, idUsuario, imagen, descripcion);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Imagen agregada exitosamente",
                    // Corrección: usar camelCase para el getter del Modelo anidado
                    "totalImagenes", solicitud.getProyectoProductivo().getImagenes().size() 
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar la imagen: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Eliminar imagen del proyecto
     */
    @DeleteMapping("/{id}/evidencias/{idEvidencia}")
    public ResponseEntity<?> eliminarImagenProyecto(
            @PathVariable String id,
            @PathVariable String idEvidencia,
            Authentication authentication) {

        try {
            String idUsuario = authentication.getName();

            // El servicio retorna Solicitudes_Credito, pero aquí no se usa el retorno, solo se llama al método
            solicitudService.eliminarImagenProyecto(id, idUsuario, idEvidencia); 

            return ResponseEntity.ok(Map.of("mensaje", "Imagen eliminada exitosamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener lista de evidencias fotográficas
     */
    @GetMapping("/{id}/evidencias")
    public ResponseEntity<?> obtenerEvidencias(
            @PathVariable String id,
            Authentication authentication) {

        try {
            String idUsuario = authentication.getName();

            // Corrección: Tipo de retorno Solicitudes_Credito
            Solicitudes_Credito solicitud = solicitudService.obtenerDetalleSolicitud(id, idUsuario); 

            // Corrección: usar camelCase para el getter del Modelo
            if (solicitud.getProyectoProductivo() == null || 
                solicitud.getProyectoProductivo().getImagenes() == null) {
                return ResponseEntity.ok(Map.of(
                        "mensaje", "No hay evidencias fotográficas",
                        "imagenes", List.of()
                ));
            }

            return ResponseEntity.ok(Map.of(
                    // Corrección: usar camelCase para el getter del Modelo anidado
                    "imagenes", solicitud.getProyectoProductivo().getImagenes() 
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Descargar imagen específica
     */
    @GetMapping("/evidencias/descargar/{fileId}")
    public ResponseEntity<?> descargarImagen(@PathVariable String fileId) {
        try {
            // Se asume que el servicio usa el GridFSFile de Spring o Mongo
            GridFSFile gridFSFile = solicitudService.obtenerArchivo(fileId); 

            if (gridFSFile == null) {
                return ResponseEntity.notFound().build();
            }

            // Asegurar que GridFsResource esté importado (org.springframework.data.mongodb.gridfs.GridFsResource)
            GridFsResource resource = gridFsTemplate.getResource(gridFSFile); 

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(gridFSFile.getMetadata().getString("_contentType"))) // Usar getString() para metadatos
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + gridFSFile.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al descargar la imagen: " + e.getMessage()));
        }
    }

    /**
     * Listar solicitudes del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<?> listarSolicitudesUsuario(Authentication authentication) {
        try {
            String idUsuario = authentication.getName();
            // Corrección: Tipo de retorno List<Solicitudes_Credito>
            List<Solicitudes_Credito> solicitudes = solicitudService.obtenerSolicitudesUsuario(idUsuario); 

            return ResponseEntity.ok(Map.of(
                    "solicitudes", solicitudes,
                    "total", solicitudes.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtener detalle de una solicitud
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleSolicitud(
            @PathVariable String id,
            Authentication authentication) {

        try {
            String idUsuario = authentication.getName();

            // Corrección: Tipo de retorno Solicitudes_Credito
            Solicitudes_Credito solicitud = solicitudService.obtenerDetalleSolicitud(id, idUsuario); 
            return ResponseEntity.ok(solicitud);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar solicitud (solo si está en revisión)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarSolicitud(
            @PathVariable String id,
            @RequestBody @Valid ActualizarSolicitudDTO dto,
            Authentication authentication) {

        try {
            String idUsuario = authentication.getName();

            // Corrección: Tipo de retorno Solicitudes_Credito
            Solicitudes_Credito solicitud = solicitudService.actualizarSolicitud(id, idUsuario, dto); 

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Solicitud actualizada exitosamente",
                    "solicitud", solicitud
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancelar solicitud (solo si está en revisión)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarSolicitud(
            @PathVariable String id,
            Authentication authentication) {

        try {
            String idUsuario = authentication.getName();

            solicitudService.cancelarSolicitud(id, idUsuario);

            return ResponseEntity.ok(Map.of("mensaje", "Solicitud cancelada exitosamente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Actualizar proyecto productivo de una solicitud
     */
    @PutMapping("/{id}/proyecto")
    public ResponseEntity<?> actualizarProyectoProductivo(
            @PathVariable String id,
            @RequestBody String proyectoJson,
            Authentication authentication) {

        try {
            // Parsear el JSON manualmente
            ObjectMapper objectMapper = new ObjectMapper();
            ProyectoProductivoDTO dto = objectMapper.readValue(proyectoJson, ProyectoProductivoDTO.class);

            // Validar el DTO
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<ProyectoProductivoDTO>> violations = validator.validate(dto);

            if (!violations.isEmpty()) {
                List<String> errors = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Errores de validación", "detalles", errors));
            }

            String idUsuario = authentication.getName();

            // Corrección: Tipo de retorno Solicitudes_Credito
            Solicitudes_Credito actualizada = solicitudService.actualizarProyectoProductivo(id, idUsuario, dto); 

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Proyecto productivo actualizado exitosamente",
                    "solicitud", actualizada
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}