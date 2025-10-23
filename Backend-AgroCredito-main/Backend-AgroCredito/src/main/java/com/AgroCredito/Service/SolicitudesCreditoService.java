package com.AgroCredito.Service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgroCredito.Dto.Request.ActualizarSolicitudDTO;
import com.AgroCredito.Dto.Request.CrearSolicitudDTO;
import com.AgroCredito.Dto.Request.ProyectoProductivoDTO;
import com.AgroCredito.Model.Solicitudes_Credito;
import com.AgroCredito.Model.Solicitudes_Credito.ProyectoProductivo;
import com.AgroCredito.Model.Solicitudes_Credito.Solicitante;
import com.AgroCredito.Model.Solicitudes_Credito.ProyectoProductivo.ImagenReferencia;
import com.AgroCredito.Model.Solicitudes_Credito.Solicitante.UbicacionBasica;
import com.AgroCredito.Model.Usuario;
import com.AgroCredito.Model.Usuario.ReferenciaComunitaria;
import com.AgroCredito.Model.Usuario.Estadisticas;
import com.AgroCredito.Repository.SolicitudesCreditoRepository;
import com.AgroCredito.Repository.UsuarioRepository;
import com.mongodb.client.gridfs.model.GridFSFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudesCreditoService { // Usando el nombre de clase de tu último fragmento

    @Autowired
    private SolicitudesCreditoRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    // ----------------------------------------------------------------------
    // Métodos Principales (Corregidos para usar Correo -> ID)
    // ----------------------------------------------------------------------

    /**
     * Crear nueva solicitud de crédito.
     * @param correoUsuario Es el correo del usuario autenticado (getName()).
     */
    public Solicitudes_Credito crearSolicitud(CrearSolicitudDTO dto, String correoUsuario, MultipartFile imagen) throws IOException {
        
        // 1. Obtener Usuario por correo y verificar existencia.
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));

        String idUsuario = usuario.getId();

        // 2. Verificar si el usuario ya tiene solicitudes activas
        if (solicitudRepository.existsSolicitudesActivas(idUsuario)) {
            throw new RuntimeException("Ya tienes una solicitud activa. No puedes crear otra hasta que se resuelva.");
        }

        // 3. Mapear y crear la solicitud
        Solicitudes_Credito solicitud = new Solicitudes_Credito();
        solicitud.setIdUsuario(idUsuario);
        
        // Información del solicitante (copiada del usuario)
        Solicitante solicitante = new Solicitante();
        solicitante.setNombres(usuario.getNombres());
        solicitante.setIdentificacion(usuario.getIdentificacion());
        solicitante.setTelefono(usuario.getTelefono());
        
        // Mapear ubicación
        UbicacionBasica ubicacionSolicitante = new Solicitudes_Credito.Solicitante.UbicacionBasica();
        // Asumiendo que usuario.getUbicacion() existe y tiene los getters
        if (usuario.getUbicacion() != null) {
            ubicacionSolicitante.setDepartamento(usuario.getUbicacion().getDepartamento());
            ubicacionSolicitante.setMunicipio(usuario.getUbicacion().getMunicipio());
            ubicacionSolicitante.setVereda(usuario.getUbicacion().getVereda());
        }
        solicitante.setUbicacion(ubicacionSolicitante);
        solicitud.setSolicitante(solicitante);

        // Datos del crédito
        solicitud.setMontoSolicitado(dto.getMontoSolicitado());
        solicitud.setDestinoCredito(dto.getDestinoCredito());
        solicitud.setPlazoMeses(dto.getPlazoMeses());
        solicitud.setGarantia(dto.getGarantia());
        solicitud.setEstado("en revisión");
        solicitud.setFechaSolicitud(LocalDateTime.now());

        // Referencias
        List<ReferenciaComunitaria> referencias = usuario.getReferenciaComunitarias();
        solicitud.setReferenciasVerificadas(referencias != null && !referencias.isEmpty());

        // Proyecto productivo
        if (dto.getProyectoProductivo() != null) {
            ProyectoProductivo proyecto = new ProyectoProductivo();
            proyecto.setNombre(dto.getProyectoProductivo().getNombre());
            proyecto.setDescripcion(dto.getProyectoProductivo().getDescripcion());
            proyecto.setCostoEstimado(dto.getProyectoProductivo().getCostoEstimado());
            proyecto.setIngresosEstimados(dto.getProyectoProductivo().getIngresosEstimados());
            proyecto.setImpactoComunitario(dto.getProyectoProductivo().getImpactoComunitario());
            proyecto.setDuracionMeses(dto.getProyectoProductivo().getDuracionMeses());
            
            // Subir imagen inicial
            if (imagen != null && !imagen.isEmpty()) {
                List<ImagenReferencia> imagenes = new ArrayList<>();
                ImagenReferencia imagenProyecto = subirImagen(imagen, dto.getProyectoProductivo().getDescripcionImagen());
                imagenes.add(imagenProyecto);
                proyecto.setImagenes(imagenes);
            } else {
                proyecto.setImagenes(new ArrayList<>());
            }
            
            solicitud.setProyectoProductivo(proyecto);
        }

        // 4. Guardar y actualizar estadísticas
        Solicitudes_Credito solicitudGuardada = solicitudRepository.save(solicitud);
        actualizarEstadisticasUsuario(usuario);

        return solicitudGuardada;
    }

    /**
     * Agregar más imágenes a una solicitud existente.
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public Solicitudes_Credito agregarImagenProyecto(String idSolicitud, String correoUsuario, 
                                                   MultipartFile file, String descripcion) throws IOException {
        
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        // 2. Buscar y validar la solicitud
        Solicitudes_Credito solicitud = solicitudRepository.findByIdAndIdUsuario(idSolicitud, idUsuario)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada o no pertenece al usuario."));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden agregar imágenes a solicitudes en revisión");
        }

        if (solicitud.getProyectoProductivo() == null) {
            throw new RuntimeException("La solicitud no tiene un proyecto productivo asociado.");
        }

        // 3. Subir imagen
        ImagenReferencia nuevaImagen = subirImagen(file, descripcion);

        // 4. Agregar a la lista de imágenes
        List<ImagenReferencia> imagenes = solicitud.getProyectoProductivo().getImagenes();
        if (imagenes == null) {
            imagenes = new ArrayList<>();
        }
        imagenes.add(nuevaImagen);
        solicitud.getProyectoProductivo().setImagenes(imagenes);

        return solicitudRepository.save(solicitud);
    }

    /**
     * Eliminar una imagen del proyecto.
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public Solicitudes_Credito eliminarImagenProyecto(String idSolicitud, String correoUsuario, String fileId) {
        
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        // 2. Buscar y validar la solicitud
        Solicitudes_Credito solicitud = solicitudRepository.findByIdAndIdUsuario(idSolicitud, idUsuario)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada o no pertenece al usuario."));

        if (solicitud.getProyectoProductivo() == null || 
            solicitud.getProyectoProductivo().getImagenes() == null) {
            throw new RuntimeException("No hay imágenes para eliminar en el proyecto.");
        }

        // 3. Eliminar de GridFS (convertir String a ObjectId)
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(fileId))));

        // 4. Eliminar de la lista y guardar
        List<ImagenReferencia> imagenes = solicitud.getProyectoProductivo().getImagenes()
                .stream()
                .filter(img -> !img.getFileId().equals(fileId))
                .collect(Collectors.toList());
        
        solicitud.getProyectoProductivo().setImagenes(imagenes);

        return solicitudRepository.save(solicitud);
    }
    
    // ----------------------------------------------------------------------
    // Métodos de Consulta y Actualización
    // ----------------------------------------------------------------------

    /**
     * Obtener solicitudes del usuario.
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public List<Solicitudes_Credito> obtenerSolicitudesUsuario(String correoUsuario) {
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        return solicitudRepository.findByIdUsuario(idUsuario);
    }

    /**
     * Obtener detalle de una solicitud.
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public Solicitudes_Credito obtenerDetalleSolicitud(String idSolicitud, String correoUsuario) {
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        // 2. Buscar solicitud
        return solicitudRepository.findByIdAndIdUsuario(idSolicitud, idUsuario)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada o no pertenece al usuario."));
    }

    /**
     * Actualizar solicitud (solo en revisión).
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public Solicitudes_Credito actualizarSolicitud(String idSolicitud, String correoUsuario, 
                                                ActualizarSolicitudDTO dto) {
        
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        // 2. Buscar y validar la solicitud
        Solicitudes_Credito solicitud = solicitudRepository.findByIdAndIdUsuario(idSolicitud, idUsuario)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada o no pertenece al usuario."));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden actualizar solicitudes en revisión");
        }

        // 3. Actualizar campos permitidos
        if (dto.getMontoSolicitado() != null) {
            solicitud.setMontoSolicitado(dto.getMontoSolicitado());
        }
        if (dto.getDestinoCredito() != null) {
            solicitud.setDestinoCredito(dto.getDestinoCredito());
        }
        if (dto.getPlazoMeses() != null) {
            solicitud.setPlazoMeses(dto.getPlazoMeses());
        }
        if (dto.getGarantia() != null) {
            solicitud.setGarantia(dto.getGarantia());
        }

        return solicitudRepository.save(solicitud);
    }
    
    /**
     * Actualizar proyecto productivo.
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public Solicitudes_Credito actualizarProyectoProductivo(String idSolicitud, String correoUsuario, 
                                                         ProyectoProductivoDTO dto) {
        
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        // 2. Buscar y validar la solicitud
        Solicitudes_Credito solicitud = solicitudRepository.findByIdAndIdUsuario(idSolicitud, idUsuario)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada o no pertenece al usuario."));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden actualizar solicitudes en revisión");
        }

        // 3. Crear o actualizar proyecto productivo
        ProyectoProductivo proyecto = solicitud.getProyectoProductivo();
        if (proyecto == null) {
            proyecto = new ProyectoProductivo();
            // Asegurar que la lista de imágenes no es nula si se crea el proyecto
            proyecto.setImagenes(new ArrayList<>());
        }
        
        proyecto.setNombre(dto.getNombre());
        proyecto.setDescripcion(dto.getDescripcion());
        proyecto.setCostoEstimado(dto.getCostoEstimado());
        proyecto.setIngresosEstimados(dto.getIngresosEstimados());
        proyecto.setImpactoComunitario(dto.getImpactoComunitario());
        proyecto.setDuracionMeses(dto.getDuracionMeses());
        
        solicitud.setProyectoProductivo(proyecto);
        
        return solicitudRepository.save(solicitud);
    }

    /**
     * Cancelar solicitud.
     * @param correoUsuario Es el correo del usuario autenticado.
     */
    public void cancelarSolicitud(String idSolicitud, String correoUsuario) {
        // 1. Obtener Usuario por correo y su ID
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));
        String idUsuario = usuario.getId();
        
        // 2. Buscar y validar la solicitud
        Solicitudes_Credito solicitud = solicitudRepository.findByIdAndIdUsuario(idSolicitud, idUsuario)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada o no pertenece al usuario."));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden cancelar solicitudes en revisión");
        }

        solicitud.setEstado("cancelado");
        solicitudRepository.save(solicitud);
    }
    
    // ----------------------------------------------------------------------
    // Métodos Auxiliares
    // ----------------------------------------------------------------------

    /**
     * Subir imagen a GridFS
     */
    private ImagenReferencia subirImagen(MultipartFile file, String descripcion) throws IOException {
        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        // Guardar en GridFS y obtener ObjectId
        ObjectId fileIdObject = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                contentType
        );

        // Crear objeto de imagen
        ImagenReferencia imagen = new ImagenReferencia();
        imagen.setFileId(fileIdObject.toString());
        imagen.setFilename(file.getOriginalFilename());
        imagen.setContentType(contentType);
        imagen.setDescripcion(descripcion != null ? descripcion : "Evidencia del proyecto");

        return imagen;
    }

    /**
     * Actualizar estadísticas del usuario
     */
    private void actualizarEstadisticasUsuario(Usuario usuario) {
        Estadisticas stats = usuario.getEstadisticas();
        if (stats == null) {
            stats = new Estadisticas();
            stats.setTotalCreditosSolicitados(0); // Inicializar a 0 si es nulo
        }
        
        stats.setTotalCreditosSolicitados(stats.getTotalCreditosSolicitados() + 1);
        usuario.setEstadisticas(stats);
        usuarioRepository.save(usuario);
    }

    /**
     * Obtener archivo de GridFS por String ID
     */
    public GridFSFile obtenerArchivo(String fileId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
    }
}