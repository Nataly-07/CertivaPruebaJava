package com.certiva.api.Config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.certiva.api.Entity.Rol;
import com.certiva.api.Entity.TipoDocumento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Repository.RolRepository;
import com.certiva.api.Repository.TipoDocumentoRepository;
import com.certiva.api.Repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_CORREO = "admin@certiva.com";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository,
                           RolRepository rolRepository,
                           TipoDocumentoRepository tipoDocumentoRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol rolAdmin = ensureRol("ROLE_ADMIN", "Administrador del sistema");
        ensureRol("ROLE_ESTUDIANTE", "Estudiante");
        ensureRol("ROLE_MONITOR", "Monitor de eventos");
        ensureRol("ROLE_PROFESOR", "Profesor");

        TipoDocumento tipoDoc = tipoDocumentoRepository.findByNombre("Documento de Identidad")
                .orElseGet(() -> {
                    TipoDocumento td = new TipoDocumento();
                    td.setNombre("Documento de Identidad");
                    td.setTipoDocumento("CC");
                    return tipoDocumentoRepository.save(td);
                });

        if (!usuarioRepository.existsByCorreo(ADMIN_CORREO)) {
            Usuario admin = new Usuario();
            admin.setNombres("Administrador");
            admin.setApellidos("Sistema");
            admin.setCorreo(ADMIN_CORREO);
            admin.setNumeroDocumento("10010010000");
            admin.setContraseña(passwordEncoder.encode("Admin123"));
            admin.setEstado(true);
            admin.setFechaRegistro(LocalDateTime.now());
            admin.setTipoDocumento(tipoDoc);
            admin.getRoles().add(rolAdmin);
            usuarioRepository.save(admin);
        }
    }

    private Rol ensureRol(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre(nombre);
                    r.setDescripcion(descripcion);
                    r.setActivo(true);
                    return rolRepository.save(r);
                });
    }
}
