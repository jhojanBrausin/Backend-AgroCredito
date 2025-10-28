package com.AgroCredito.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.AgroCredito.Dto.Request.AprobarSolicitudDTO;
import com.AgroCredito.Dto.Request.CambiarEstadoCreditoDTO;
import com.AgroCredito.Dto.Request.EvaluarSolicitudDTO;
import com.AgroCredito.Dto.Request.PlanPago;
import com.AgroCredito.Dto.Request.RechazarSolicitudDTO;
import com.AgroCredito.Dto.Request.RegistrarPagoDTO;
import com.AgroCredito.Dto.Request.SubirComprobanteDTO;
import com.AgroCredito.Dto.Request.SubirEvidenciaCultivoDTO;
import com.AgroCredito.Model.Credito;
import com.AgroCredito.Model.Credito.ComprobanteFile;
import com.AgroCredito.Model.Credito.EvidenciaCultivo;
import com.AgroCredito.Model.Credito.HistorialPago;
import com.AgroCredito.Model.Credito.ImagenCultivo;
import com.AgroCredito.Model.Solicitudes_Credito;
import com.AgroCredito.Model.Solicitudes_Credito.Evaluacion;
import com.AgroCredito.Model.Usuario;
import com.AgroCredito.Repository.CreditoRepository;
import com.AgroCredito.Repository.SolicitudesCreditoRepository;
import com.AgroCredito.Repository.UsuarioRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreditoService {

    @Autowired
    private CreditoRepository creditoRepository;

    @Autowired
    private SolicitudesCreditoRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private GridFSService gridFSService;
    

    public List<Solicitudes_Credito> listarSolicitudesConFiltros(String estado, String departamento, LocalDateTime fechaDesde) {
        
        List<Solicitudes_Credito> solicitudes = solicitudRepository.findAll();
        
        if (estado != null && !estado.isEmpty()) {
            solicitudes = solicitudes.stream().filter(s -> s.getEstado().equalsIgnoreCase(estado)).collect(Collectors.toList());
        }
        
        return solicitudes;
    }

    public List<Solicitudes_Credito> listarSolicitudesPendientes() {
        
        return solicitudRepository.findByEstado("en revisión");
    }

    public Solicitudes_Credito evaluarSolicitud(String idSolicitud, EvaluarSolicitudDTO dto, String correoAdmin) {
        Solicitudes_Credito solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + idSolicitud));
        
        Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                .orElseThrow(() -> new RuntimeException("Evaluador no encontrado con correo: " + correoAdmin));
                
        Evaluacion nuevaEvaluacion = new Evaluacion();
        nuevaEvaluacion.setPuntaje(dto.getPuntaje());
        nuevaEvaluacion.setObservaciones(dto.getObservaciones());
        nuevaEvaluacion.setFechaRevision(LocalDateTime.now());
        nuevaEvaluacion.setIdAdministrador(admin.getId()); 

        solicitud.setEvaluacion(nuevaEvaluacion);
                
        return solicitudRepository.save(solicitud);
    }

        public Credito aprobarSolicitud(String idSolicitud, AprobarSolicitudDTO dto, String correoAdmin) {
            
            Solicitudes_Credito solicitud = solicitudRepository.findById(idSolicitud)
                    .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

            if (!"en revisión".equals(solicitud.getEstado())) {
                throw new RuntimeException("Solo se pueden aprobar solicitudes en revisión");
            }

            Usuario admin = usuarioRepository.findByCorreo(correoAdmin)
                    .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
            Usuario usuario = usuarioRepository.findById(solicitud.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));


            solicitud.setEstado("aprobado");
            solicitudRepository.save(solicitud);


            Credito credito = new Credito();
            credito.setIdSolicitud(idSolicitud);
            credito.setIdUsuario(solicitud.getIdUsuario());


            Credito.UsuarioEmbedido usuarioEmbedido = new Credito.UsuarioEmbedido();
            usuarioEmbedido.setNombres(usuario.getNombres());
            usuarioEmbedido.setIdentificacion(usuario.getIdentificacion());
            usuarioEmbedido.setTelefono(usuario.getTelefono());
            usuarioEmbedido.setCorreo(usuario.getCorreo());
            usuarioEmbedido.setRol(usuario.getRol());


            Usuario.Ubicacion ubicacionUsuario = usuario.getUbicacion();
            if (ubicacionUsuario != null) {
                Credito.UbicacionPrincipal ubicacionPrincipal = new Credito.UbicacionPrincipal();
                ubicacionPrincipal.setDepartamento(ubicacionUsuario.getDepartamento());
                ubicacionPrincipal.setMunicipio(ubicacionUsuario.getMunicipio());
                ubicacionPrincipal.setVereda(ubicacionUsuario.getVereda());

                if (ubicacionUsuario.getCoordenadas() != null) {

                	Credito.UbicacionPrincipal.Coordenadas coordenadasCredito = new Credito.UbicacionPrincipal.Coordenadas();
                    coordenadasCredito.setLat(ubicacionUsuario.getCoordenadas().getLat());
                    coordenadasCredito.setLng(ubicacionUsuario.getCoordenadas().getLng());
                    ubicacionPrincipal.setCoordenadas(coordenadasCredito);
                }
                usuarioEmbedido.setUbicacionPrincipal(ubicacionPrincipal);
            }

            
            usuarioEmbedido.setActividadEconomica(usuario.getActividadEconomica());
            usuarioEmbedido.setIngresosAprox(usuario.getIngresosAprox());


            usuarioEmbedido.setHistorialCrediticioResumen(usuario.getEstadisticas() != null ? usuario.getEstadisticas().getHistorialCrediticio() : null); 
            
            credito.setUsuarioEmbedido(usuarioEmbedido);

            Credito.SolicitudEmbedida solicitudEmbedida = new Credito.SolicitudEmbedida();

            Date fechaSolicitudDate = Date.from(solicitud.getFechaSolicitud().atZone(ZoneId.systemDefault()).toInstant());
            solicitudEmbedida.setFechaSolicitud(fechaSolicitudDate); 
            solicitudEmbedida.setMontoSolicitado(solicitud.getMontoSolicitado());
            solicitudEmbedida.setDestinoCredito(solicitud.getDestinoCredito());
            solicitudEmbedida.setPlazoMesesSolicitado(solicitud.getPlazoMeses());
            solicitudEmbedida.setGarantia(solicitud.getGarantia());
            solicitudEmbedida.setPuntajeEvaluacion(solicitud.getEvaluacion() != null ? solicitud.getEvaluacion().getPuntaje() : null); // Uso getEvaluacion().getPuntaje()
            
            Solicitudes_Credito.ProyectoProductivo proyectoSolicitud = solicitud.getProyectoProductivo();
            if (proyectoSolicitud != null) {
                Credito.ProyectoProductivoResumen proyectoCredito = new Credito.ProyectoProductivoResumen();
                proyectoCredito.setNombre(proyectoSolicitud.getNombre());
                proyectoCredito.setDescripcion(proyectoSolicitud.getDescripcion());
                proyectoCredito.setCostoEstimado(proyectoSolicitud.getCostoEstimado());
                proyectoCredito.setIngresosEstimados(proyectoSolicitud.getIngresosEstimados());
                proyectoCredito.setImpactoComunitario(proyectoSolicitud.getImpactoComunitario());
                proyectoCredito.setDuracionMeses(proyectoSolicitud.getDuracionMeses());
                
                List<Credito.ProyectoProductivoResumen.ImagenReferencia> imagenesMapeadas = new ArrayList<>();
                for (Solicitudes_Credito.ProyectoProductivo.ImagenReferencia imgSol : proyectoSolicitud.getImagenes()) {
                    Credito.ProyectoProductivoResumen.ImagenReferencia imgCred = new Credito.ProyectoProductivoResumen.ImagenReferencia();
                    imgCred.setFileId(imgSol.getFileId());
                    imgCred.setFilename(imgSol.getFilename());
                    imgCred.setContentType(imgSol.getContentType());
                    imgCred.setDescripcion(imgSol.getDescripcion());
                    imagenesMapeadas.add(imgCred);
                }
                proyectoCredito.setImagenes(imagenesMapeadas); 
                
                solicitudEmbedida.setProyectoProductivoResumen(proyectoCredito);
            }
            
            credito.setSolicitudEmbedida(solicitudEmbedida);

            Credito.Aprobador aprobador = new Credito.Aprobador();
            aprobador.setId(admin.getId());
            aprobador.setNombres(admin.getNombres());
            aprobador.setRol(admin.getRol());
            credito.setAprobador(aprobador);

            credito.setMontoAprobado(dto.getMontoAprobado());
            credito.setInteresMensual(dto.getInteresMensual());
            credito.setPlazoMeses(dto.getPlazoMeses());

            double tasaMensual = dto.getInteresMensual() / 100.0;
            double montoAprobado = dto.getMontoAprobado();
            int plazoMeses = dto.getPlazoMeses();

            double cuotaMensualCalc = (tasaMensual == 0) ? (montoAprobado / plazoMeses) : 
                (montoAprobado * tasaMensual) / (1 - Math.pow(1 + tasaMensual, -plazoMeses));
            
            credito.setCuotaMensual(Math.round(cuotaMensualCalc)); 

            LocalDateTime fechaAprobacion = LocalDateTime.now();
            credito.setFechaAprobacion(fechaAprobacion);
            
            LocalDateTime vencimientoLocal = fechaAprobacion.plusMonths(plazoMeses);
            Date fechaVencimientoDate = Date.from(vencimientoLocal.atZone(ZoneId.systemDefault()).toInstant());
            credito.setFechaVencimiento(fechaVencimientoDate);
            
            credito.setEstado("activo");
            credito.setSaldoPendiente(montoAprobado);
            credito.setTotalPagado(0.0);
            
            credito.setMetricas(new Credito.Metricas());
            credito.setHistorialPagos(new ArrayList<>());
            credito.setEvidenciasCultivo(new ArrayList<>());

            Credito creditoGuardado = creditoRepository.save(credito);

            return creditoGuardado;
        }

    public Solicitudes_Credito rechazarSolicitud(String idSolicitud, RechazarSolicitudDTO dto, String correoAdmin) {
        Solicitudes_Credito solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        if (!"en revisión".equals(solicitud.getEstado())) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes en revisión");
        }

        solicitud.setEstado("rechazada");
        solicitud.getEvaluacion().setObservaciones(dto.getMotivo());
        
        return solicitudRepository.save(solicitud);
    }

   
    public List<Credito> listarCreditosUsuario(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return creditoRepository.findByIdUsuario(usuario.getId());
    }


    public Credito obtenerDetalleCredito(String idCredito, String correoUsuario) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado con ID: " + idCredito));

        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!credito.getIdUsuario().equals(usuario.getId())) {
            throw new RuntimeException("Acceso denegado. Este crédito no te pertenece.");
        }
        return credito;
    }

    public List<Credito> listarCreditosActivosUsuario(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return creditoRepository.findByIdUsuarioAndEstado(usuario.getId(), "activo");
    }

 
    public List<PlanPago> generarPlanPagos(String idCredito) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
        
        double saldoInicial = credito.getMontoAprobado();
        double tasaMensual = credito.getInteresMensual() / 100.0;
        int plazoMeses = credito.getPlazoMeses();
        LocalDateTime fechaBase = credito.getFechaAprobacion().plusMonths(1).withDayOfMonth(5);
        
        double cuotaMensualDouble = (saldoInicial * tasaMensual) / 
                (1 - Math.pow(1 + tasaMensual, -plazoMeses));
        
        List<PlanPago> planPagos = new ArrayList<>();
        double saldoPendiente = saldoInicial;
        double cuotaFija = cuotaMensualDouble;

        for (int i = 1; i <= plazoMeses; i++) {
            double interes = saldoPendiente * tasaMensual;
            double capital = cuotaFija - interes;

            if (i == plazoMeses) {
                capital = saldoPendiente;
                cuotaFija = capital + interes;
                saldoPendiente = 0.0;
            } else {
                saldoPendiente -= capital;
            }
            
            PlanPago cuota = new PlanPago();
            cuota.setId(String.valueOf(i));
            cuota.setNumeroCuota(i);
            cuota.setFechaVencimiento(fechaBase.plusMonths(i - 1));
            cuota.setMontoCuota(Math.round(cuotaFija) / 1.0);
            cuota.setInteres(interes);
            cuota.setCapital(capital);
            cuota.setSaldoPendiente(Math.max(0.0, saldoPendiente));
            cuota.setEstado("PENDIENTE");

            planPagos.add(cuota);
        }
        
        return planPagos;
    }


    public List<Credito> listarCreditosAdmin(String estado, String idUsuario) {
        if (estado != null && !estado.isEmpty()) {
            return creditoRepository.findByEstado(estado);
        }
        if (idUsuario != null && !idUsuario.isEmpty()) {
            return creditoRepository.findByIdUsuario(idUsuario);
        }
        return creditoRepository.findAll();
    }
    

    public Credito cambiarEstadoCredito(String idCredito, CambiarEstadoCreditoDTO dto) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado con ID: " + idCredito));
                
        credito.setEstado(dto.getEstado());
        

        return creditoRepository.save(credito);
    }
    

    public Credito registrarPagoConComprobante(
            RegistrarPagoDTO dto, 
            MultipartFile comprobante, 
            String correoUsuario) throws IOException { 
    	
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Credito credito = creditoRepository.findById(dto.getIdCredito())
            .orElseThrow(() -> new RuntimeException("Crédito no encontrado."));

        if (!credito.getIdUsuario().equals(usuario.getId())) {
            throw new RuntimeException("No puedes registrar pagos para este crédito.");
        }
        if (!"activo".equals(credito.getEstado())) {
            throw new RuntimeException("El crédito no está activo para registrar pagos.");
        }
        
        // 1. Obtener el DTO del servicio de archivos
        SubirComprobanteDTO dtoDesdeGridFS = gridFSService.subirComprobante(comprobante);

        // 2. CONVERTIR el DTO a la clase de modelo anidada que HistorialPago espera
        ComprobanteFile comprobanteModel = new ComprobanteFile(); // 👈 Instancia la clase anidada (Credito.ComprobanteFile)
        comprobanteModel.setFileId(dtoDesdeGridFS.getFileId()); 
        comprobanteModel.setFilename(dtoDesdeGridFS.getFilename());
        comprobanteModel.setContentType(dtoDesdeGridFS.getContentType());
        
        // 3. Crear HistorialPago
        HistorialPago nuevoPago = new HistorialPago();
        
        nuevoPago.setId(UUID.randomUUID().toString());
        nuevoPago.setMonto(dto.getMonto());
        nuevoPago.setFechaPago(new Date()); 
        nuevoPago.setMetodoPago(dto.getMetodoPago());
        nuevoPago.setEstado("pendiente");
        
        // 4. Asignación CORREGIDA: Asignamos el objeto del modelo (ComprobanteFile)
        nuevoPago.setComprobanteFile(comprobanteModel); 

        if (credito.getHistorialPagos() == null) {
            credito.setHistorialPagos(new ArrayList<>());
        }
        credito.getHistorialPagos().add(nuevoPago);
        
        // 5. Guardar y retornar
        return creditoRepository.save(credito);
    }
   
    public List<HistorialPago> obtenerHistorialPagos(String idCredito, String correoUsuario) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado con ID: " + idCredito));
                
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (!credito.getIdUsuario().equals(usuario.getId())) {
            throw new RuntimeException("Acceso denegado. Este historial de pagos no te pertenece.");
        }
        
        return credito.getHistorialPagos();
    }
    

    public HistorialPago obtenerDetallePago(String idCredito, String idPago, String correoUsuario) {
         Credito credito = obtenerDetalleCredito(idCredito, correoUsuario); // Reutiliza la validación de pertenencia

        return credito.getHistorialPagos().stream()
            .filter(p -> idPago.equals(p.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + idPago));
    }
    
    public Credito actualizarComprobante(
            String idCredito, 
            String idPago, 
            MultipartFile comprobante, 
            String correoUsuario) throws IOException { // Añadir el archivo y el email

        // 1. Validar Usuario y Crédito
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
                
        if (!credito.getIdUsuario().equals(usuario.getId())) {
            throw new RuntimeException("No puedes actualizar pagos para este crédito.");
        }

            HistorialPago pago = credito.getHistorialPagos().stream()
            .filter(p -> idPago.equals(p.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Pago no encontrado."));

        SubirComprobanteDTO dtoDesdeGridFS = gridFSService.subirComprobante(comprobante);

        ComprobanteFile nuevoComprobante = new ComprobanteFile(); 
        nuevoComprobante.setFileId(dtoDesdeGridFS.getFileId());
        nuevoComprobante.setFilename(dtoDesdeGridFS.getFilename());
        nuevoComprobante.setContentType(dtoDesdeGridFS.getContentType());

        pago.setComprobanteFile(nuevoComprobante);
        
        if ("rechazado".equals(pago.getEstado())) {
            pago.setEstado("pendiente");
        }
        
        return creditoRepository.save(credito);
    }
    
    public Credito confirmarPago(String idCredito, String idPago) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
        
        HistorialPago pago = credito.getHistorialPagos().stream()
            .filter(p -> idPago.equals(p.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        if (!"pendiente".equals(pago.getEstado())) {
            throw new RuntimeException("El pago ya fue procesado.");
        }
        
        double saldoAnterior = credito.getSaldoPendiente();
        double tasaMensual = credito.getInteresMensual() / 100.0;
        double montoPagado = pago.getMonto();
        
        double interesCuota = saldoAnterior * tasaMensual;
        double capitalCuota = montoPagado - interesCuota;
        
        if (capitalCuota < 0) {
            capitalCuota = 0; // El pago no cubrió el interés, todo es interés
        }
        
        pago.setEstado("confirmado");
        pago.setInteresPagado(interesCuota); 
        pago.setCapitalPagado(capitalCuota); 
        pago.setSaldoRestante(saldoAnterior - capitalCuota); 
        
        credito.setSaldoPendiente(credito.getSaldoPendiente() - capitalCuota);
        credito.setTotalPagado(credito.getTotalPagado() + montoPagado);
        
        credito.getMetricas().setPagosRealizados(credito.getMetricas().getPagosRealizados() + 1);
        
        if (credito.getSaldoPendiente() <= 0.01) {
            credito.setEstado("pagado");
        }
        
        return creditoRepository.save(credito);
    }
    
    public Credito rechazarPago(String idCredito, String idPago, String motivoRechazo) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
        
        HistorialPago pago = credito.getHistorialPagos().stream()
            .filter(p -> idPago.equals(p.getId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        if (!"pendiente".equals(pago.getEstado())) {
            throw new RuntimeException("El pago ya fue procesado.");
        }
        
        pago.setEstado("rechazado");
        
        return creditoRepository.save(credito);
    }


    public Map<String, Object> generarReporteSolicitudes(String estado, LocalDateTime fechaDesde) {
        // En un entorno real, esto sería una consulta de agregación directa al DB
        long totalSolicitudes = solicitudRepository.count();
        long solicitudesAprobadas = solicitudRepository.findByEstado("aprobado").size();
        long solicitudesRechazadas = solicitudRepository.findByEstado("rechazada").size();
        long solicitudesPendientes = solicitudRepository.findByEstado("en revisión").size();

        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalSolicitudes", totalSolicitudes);
        reporte.put("aprobadas", solicitudesAprobadas);
        reporte.put("rechazadas", solicitudesRechazadas);
        reporte.put("pendientes", solicitudesPendientes);
        reporte.put("fechaGeneracion", LocalDateTime.now());
        reporte.put("filtroEstado", estado != null ? estado : "TODOS");
        
        return reporte;
    }
    
 
    public Map<String, Object> generarReporteUsuarios() {
        long totalUsuarios = usuarioRepository.count();
        // Simular conteo de usuarios con créditos activos
        long usuariosConCreditosActivos = creditoRepository.findAll().stream()
            .filter(c -> "activo".equals(c.getEstado()))
            .map(Credito::getIdUsuario)
            .distinct()
            .count();
            
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("totalUsuariosRegistrados", totalUsuarios);
        reporte.put("usuariosConCreditosActivos", usuariosConCreditosActivos);
        reporte.put("fechaGeneracion", LocalDateTime.now());

        return reporte;
    }
    
    public Map<String, Object> obtenerEstadisticasGenerales() {
        long totalCreditos = creditoRepository.count();
        long creditosActivos = creditoRepository.findByEstado("activo").size();
        double saldoTotalPendiente = creditoRepository.findAll().stream()
            .mapToDouble(Credito::getSaldoPendiente)
            .sum();
            
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalCreditosEmitidos", totalCreditos);
        estadisticas.put("creditosActivos", creditosActivos);
        estadisticas.put("creditosEnMora", creditoRepository.findByEstado("en mora").size());
        estadisticas.put("montoTotalAprobado", creditoRepository.findAll().stream().mapToDouble(Credito::getMontoAprobado).sum());
        estadisticas.put("saldoTotalPendiente", saldoTotalPendiente);
        
        return estadisticas;
    }
    public Credito subirEvidenciaCultivo(SubirEvidenciaCultivoDTO dto) {
        Credito credito = creditoRepository.findById(dto.getIdCredito())
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
        
        if (!"activo".equals(credito.getEstado())) {
            throw new RuntimeException("Solo se pueden subir evidencias a créditos activos.");
        }

        EvidenciaCultivo evidencia = credito.getEvidenciasCultivo().stream()
            .filter(e -> e.getTipoCultivo().equalsIgnoreCase(dto.getTipoCultivo()))
            .findFirst()
            .orElseGet(() -> {
                EvidenciaCultivo nuevaEvidencia = new EvidenciaCultivo();
                nuevaEvidencia.setTipoCultivo(dto.getTipoCultivo());
                nuevaEvidencia.setImagenes(new ArrayList<>());
                credito.getEvidenciasCultivo().add(nuevaEvidencia);
                return nuevaEvidencia;
            });
            
        List<ImagenCultivo> nuevasImagenes = dto.getImagenes().stream()
            .map(imgDto -> {
                ImagenCultivo imgModel = new ImagenCultivo();
                imgModel.setFileId(imgDto.getFileId());
                imgModel.setFilename(imgDto.getFilename());
                imgModel.setContentType(imgDto.getContentType());
                imgModel.setDescripcion(imgDto.getDescripcion());
                imgModel.setFecha(new Date()); 
                return imgModel;
            })
            .collect(Collectors.toList());

        evidencia.getImagenes().addAll(nuevasImagenes);
        
        return creditoRepository.save(credito);
    }
    
    public List<EvidenciaCultivo> listarEvidenciasCultivo(String idCredito) {
        Credito credito = creditoRepository.findById(idCredito)
                .orElseThrow(() -> new RuntimeException("Crédito no encontrado"));
                
        return credito.getEvidenciasCultivo();
    }
}