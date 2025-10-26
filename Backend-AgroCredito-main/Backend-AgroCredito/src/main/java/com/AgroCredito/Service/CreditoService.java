package com.AgroCredito.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.AgroCredito.Dto.Request.AprobarSolicitudDTO;
import com.AgroCredito.Dto.Request.CambiarEstadoCreditoDTO;
import com.AgroCredito.Dto.Request.EvaluarSolicitudDTO;
import com.AgroCredito.Dto.Request.RechazarSolicitudDTO;
import com.AgroCredito.Model.Credito;
import com.AgroCredito.Model.Solicitudes_Credito;
import com.AgroCredito.Model.Solicitudes_Credito.Evaluacion;
import com.AgroCredito.Model.Usuario;
import com.AgroCredito.Repository.CreditoRepository;
import com.AgroCredito.Repository.SolicitudesCreditoRepository;
import com.AgroCredito.Repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CreditoService {

    @Autowired
    private CreditoRepository creditoRepository;

    @Autowired
    private SolicitudesCreditoRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ============================================
    // MÉTODOS DE ADMINISTRACIÓN - SOLICITUDES
    // ============================================

    /**
     * Listar todas las solicitudes con filtros
     */
    public List<Solicitudes_Credito> listarSolicitudesConFiltros(String estado, String departamento, LocalDateTime fechaDesde) {
        List<Solicitudes_Credito> solicitudes = solicitudRepository.findAll();

        // Aplicar filtros
        if (estado != null && !estado.isEmpty()) {
            solicitudes = solicitudes.stream()
                    .filter(s -> s.getEstado().equals(estado))
                    .collect(Collectors.toList());
        }

        if (departamento != null && !departamento.isEmpty()) {
            solicitudes = solicitudes.stream()
                    .filter(s -> s.getSolicitante().getUbicacion().getDepartamento().equals(departamento))
                    .collect(Collectors.toList());
        }

        if (fechaDesde != null) {
            solicitudes = solicitudes.stream()
                    .filter(s -> s.getFechaSolicitud().isAfter(fechaDesde)) // <-- ¡Comparación directa!
                    .collect(Collectors.toList());
        }

        return solicitudes;
    }

    /**
     * Listar solicitudes pendientes de revisión
     */
    public List<Solicitudes_Credito> listarSolicitudesPendientes() {
        return solicitudRepository.findByEstado("en revisión");
    }

    /**
     * Evaluar solicitud (agregar puntaje y observaciones)
     */
    public Solicitudes_Credito evaluarSolicitud(String idSolicitud, EvaluarSolicitudDTO dto, String correoAdmin) {
        
        Solicitudes_Credito solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden evaluar solicitudes en revisión");
        }

        // Obtener datos del administrador
        Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // Crear evaluación
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setIdAdministrador(admin.getId());
        evaluacion.setNombreAdministrador(admin.getNombres());
        evaluacion.setFechaRevision(LocalDateTime.now());
        evaluacion.setObservaciones(dto.getObservaciones());
        evaluacion.setPuntaje(dto.getPuntaje());

        solicitud.setEvaluacion(evaluacion);

        return solicitudRepository.save(solicitud);
    }

    /**
     * Aprobar solicitud y crear crédito
     */
    public Credito aprobarSolicitud(String idSolicitud, AprobarSolicitudDTO dto, String correoAdmin) {
        
        Solicitudes_Credito solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden aprobar solicitudes en revisión");
        }

        // Obtener datos del administrador
        Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // Obtener datos del usuario solicitante
        Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Cambiar estado de la solicitud a "aprobado"
        solicitud.setEstado("aprobado");
        solicitudRepository.save(solicitud);

        // Crear el crédito
        Credito credito = new Credito();
        credito.setIdSolicitud(idSolicitud);
        credito.setIdUsuario(solicitud.getIdUsuario());

        // Usuario embedido
        Credito.UsuarioEmbedido usuarioEmbedido = new Credito.UsuarioEmbedido();
        usuarioEmbedido.setNombres(usuario.getNombres());
        usuarioEmbedido.setIdentificacion(usuario.getIdentificacion());
        usuarioEmbedido.setTelefono(usuario.getTelefono());
        usuarioEmbedido.setCorreo(usuario.getCorreo());
        usuarioEmbedido.setRol(usuario.getRol());
        usuarioEmbedido.setActividadEconomica(usuario.getActividadEconomica());
        usuarioEmbedido.setIngresosAprox(usuario.getIngresosAprox());

        Credito.UbicacionPrincipal ubicacion = new Credito.UbicacionPrincipal();
        ubicacion.setDepartamento(usuario.getUbicacion().getDepartamento());
        ubicacion.setMunicipio(usuario.getUbicacion().getMunicipio());
        ubicacion.setVereda(usuario.getUbicacion().getVereda());
        usuarioEmbedido.setUbicacionPrincipal(ubicacion);
        usuarioEmbedido.setHistorialCrediticioResumen(usuario.getEstadisticas().getHistorialCrediticio());

        credito.setUsuarioEmbedido(usuarioEmbedido);

        // Solicitud embedida
        Credito.SolicitudEmbedida solicitudEmbedida = new Credito.SolicitudEmbedida();
        solicitudEmbedida.setFechaSolicitud(Date.from(solicitud.getFechaSolicitud().atZone(ZoneId.systemDefault()).toInstant())); // Ya es Date
        solicitudEmbedida.setMontoSolicitado(solicitud.getMontoSolicitado());
        solicitudEmbedida.setDestinoCredito(solicitud.getDestinoCredito());
        solicitudEmbedida.setPlazoMesesSolicitado(solicitud.getPlazoMeses());
        solicitudEmbedida.setGarantia(solicitud.getGarantia());
        
        if (solicitud.getEvaluacion() != null) {
            solicitudEmbedida.setPuntajeEvaluacion(solicitud.getEvaluacion().getPuntaje());
        }

        if (solicitud.getProyectoProductivo() != null) {
            Credito.ProyectoProductivoResumen proyectoResumen = new Credito.ProyectoProductivoResumen();
            proyectoResumen.setNombre(solicitud.getProyectoProductivo().getNombre());
            proyectoResumen.setDescripcion(solicitud.getProyectoProductivo().getDescripcion());
            proyectoResumen.setDuracionMeses(solicitud.getProyectoProductivo().getDuracionMeses());
            solicitudEmbedida.setProyectoProductivoResumen(proyectoResumen);
        }

        credito.setSolicitudEmbedida(solicitudEmbedida);

        // Aprobador
        Credito.Aprobador aprobador = new Credito.Aprobador();
        aprobador.setId(admin.getId());
        aprobador.setNombres(admin.getNombres());
        aprobador.setRol(admin.getRol());
        credito.setAprobador(aprobador);

        // Datos del crédito
        credito.setMontoAprobado(dto.getMontoAprobado());
        credito.setInteresMensual(dto.getInteresMensual());
        credito.setPlazoMeses(dto.getPlazoMeses());

        // Calcular cuota mensual (fórmula simple) y convertir a Long
        double tasaMensual = dto.getInteresMensual() / 100;
        double cuotaMensual = (dto.getMontoAprobado() * tasaMensual) / 
                (1 - Math.pow(1 + tasaMensual, -dto.getPlazoMeses()));
        credito.setCuotaMensual(Math.round(cuotaMensual)); // Long

        credito.setFechaAprobacion(LocalDateTime.now());
        
        // Calcular fecha de vencimiento como Date
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.MONTH, dto.getPlazoMeses());
        credito.setFechaVencimiento(calendar.getTime());
        
        credito.setEstado("activo");
        credito.setSaldoPendiente(dto.getMontoAprobado());
        credito.setTotalPagado(0.0);

        // Métricas iniciales
        Credito.Metricas metricas = new Credito.Metricas();
        credito.setMetricas(metricas);

        // Guardar crédito
        Credito creditoGuardado = creditoRepository.save(credito);

        // Actualizar estadísticas del usuario
        actualizarEstadisticasUsuario(usuario, true);

        return creditoGuardado;
    }

    /**
     * Rechazar solicitud
     */
    public Solicitudes_Credito rechazarSolicitud(String idSolicitud, RechazarSolicitudDTO dto, String correoAdmin) {
        
        Solicitudes_Credito solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes en revisión");
        }

        // Obtener datos del administrador
        Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // Actualizar evaluación con motivo de rechazo
        Evaluacion evaluacion = solicitud.getEvaluacion();
        if (evaluacion == null) {
            evaluacion = new Evaluacion();
            evaluacion.setIdAdministrador(admin.getId());
            evaluacion.setNombreAdministrador(admin.getNombres());
            evaluacion.setFechaRevision(LocalDateTime.now());
        }
        evaluacion.setObservaciones(dto.getMotivo());
        evaluacion.setPuntaje(0);

        solicitud.setEvaluacion(evaluacion);
        solicitud.setEstado("rechazado");

        return solicitudRepository.save(solicitud);
    }

    // ============================================
    // MÉTODOS DE CONSULTA - CRÉDITOS
    // ============================================

    /**
     * Listar créditos del usuario autenticado
     */
    public List<Credito> listarCreditosUsuario(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return creditoRepository.findByIdUsuario(usuario.getId());
    }

    /**
     * Obtener detalle de un crédito específico
     */
    public Credito obtenerDetalleCredito(String idCredito, String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

        if (!credito.getIdUsuario().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para ver este crédito");
        }

        return credito;
    }

    /**
     * Obtener solo créditos activos
     */
    public List<Credito> obtenerCreditosActivos(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return creditoRepository.findByIdUsuarioAndEstado(usuario.getId(), "activo");
    }

    // ============================================
    // MÉTODOS ADMINISTRATIVOS - CRÉDITOS
    // ============================================

    /**
     * Listar todos los créditos (admin)
     */
    public List<Credito> listarTodosCreditos(String estado, String idUsuario) {
        if (estado != null && !estado.isEmpty()) {
            return creditoRepository.findByEstado(estado);
        }
        
        if (idUsuario != null && !idUsuario.isEmpty()) {
            return creditoRepository.findByIdUsuario(idUsuario);
        }

        return creditoRepository.findAllByOrderByFechaAprobacionDesc();
    }

    /**
     * Cambiar estado del crédito (admin)
     */
    public Credito cambiarEstadoCredito(String idCredito, CambiarEstadoCreditoDTO dto) {
        
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));

        credito.setEstado(dto.getEstado());

        return creditoRepository.save(credito);
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    /**
     * Actualizar estadísticas del usuario
     */
    private void actualizarEstadisticasUsuario(Usuario usuario, boolean esAprobacion) {
        Usuario.Estadisticas stats = usuario.getEstadisticas();
        if (stats == null) {
            stats = new Usuario.Estadisticas();
        }

        if (esAprobacion) {
            stats.setTotalCreditosAprobados(stats.getTotalCreditosAprobados() + 1);
            stats.setTotalCreditosActivos(stats.getTotalCreditosActivos() + 1);
        }

        usuario.setEstadisticas(stats);
        usuarioRepository.save(usuario);
    }
}