package com.certiva.api.Config;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.certiva.api.Entity.Usuario;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Service.AuditoriaService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Registra POST/PUT/PATCH/DELETE con usuario (si hay JWT), IP (X-Forwarded-For) y entidad afectada (heurística por ruta).
 */
@Component
public class HttpAuditFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_AUDITADOS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    public HttpAuditFilter(AuditoriaService auditoriaService, UsuarioRepository usuarioRepository) {
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return true;
        }
        if ("/api/usuarios/login".equals(path)) {
            return true;
        }
        return !METODOS_AUDITADOS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);

        try {
            String method = request.getMethod();
            if (!METODOS_AUDITADOS.contains(method)) {
                return;
            }
            String path = request.getRequestURI();
            if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
                return;
            }
            if ("/api/usuarios/login".equals(path)) {
                return;
            }

            String ip = resolverIp(request);
            Usuario usuario = resolverUsuarioAutenticado();
            String accion = method + " " + simplificarRuta(path);
            String entidad = inferirEntidad(path);
            String detalle = "status=" + response.getStatus();

            auditoriaService.registrarAuditoriaHttp(accion, entidad, detalle, ip, usuario);
        } catch (Exception ignored) {
            // No bloquear la respuesta por fallos de auditoría
        }
    }

    private static String resolverIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Usuario resolverUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof String correo)) {
            return null;
        }
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    /**
     * Reduce IDs numéricos en la ruta para agrupar eventos de auditoría sin exponer datos sensibles en la acción.
     */
    private static String simplificarRuta(String uri) {
        if (uri == null) {
            return "";
        }
        return uri.replaceAll("/\\d+", "/{id}");
    }

    private static String inferirEntidad(String uri) {
        if (uri == null || uri.length() < 5) {
            return "";
        }
        String[] partes = uri.split("/");
        if (partes.length >= 3 && "api".equals(partes[1])) {
            return partes[2];
        }
        return "";
    }
}
