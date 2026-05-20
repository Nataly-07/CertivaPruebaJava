package com.certiva.api.Impl;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.Rol.CrearRolDTO;
import com.certiva.api.DTO.Rol.RolDTO;
import com.certiva.api.DTO.RolResumenDTO;
import com.certiva.api.Entity.Rol;
import com.certiva.api.Exception.RecursoDuplicadoException;
import com.certiva.api.Exception.RecursoNoEncontradoException;
import com.certiva.api.Repository.RolRepository;
import com.certiva.api.Service.RolService;

@Service
public class RolServiceImpl implements RolService {

    private static final List<String> ROLES_FORMULARIO_ADMIN = List.of(
            "ROLE_ADMIN",
            "ROLE_PROFESOR",
            "ROLE_MONITOR",
            "ROLE_ESTUDIANTE");

    private final RolRepository _rolRepository;
    private final ModelMapper _modelMapper;

    public RolServiceImpl(RolRepository rolRepository, ModelMapper modelMapper) {
        this._rolRepository = rolRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public RolDTO crearRol(CrearRolDTO rolDTO) {
        String nombreEstandar = estandarizarNombre(rolDTO.getNombre());

        if (_rolRepository.existsByNombre(nombreEstandar)) {
            throw new RecursoDuplicadoException("Ya existe un rol con el nombre: " + nombreEstandar);
        }

        Rol rol = new Rol();
        rol.setNombre(nombreEstandar);
        rol.setDescripcion(rolDTO.getDescripcion());
        rol.setActivo(true);

        rol = _rolRepository.save(rol);
        return _modelMapper.map(rol, RolDTO.class);
    }

    @Override
    public List<RolDTO> listarRoles() {
        return _rolRepository.findAll().stream()
                .map(rol -> _modelMapper.map(rol, RolDTO.class))
                .toList();
    }

    @Override
    public List<RolDTO> listarRolesActivos() {
        return _rolRepository.findByActivoTrue().stream()
                .map(rol -> _modelMapper.map(rol, RolDTO.class))
                .toList();
    }

    @Override
    public List<RolResumenDTO> listarRolesParaAdministracion() {
        return ROLES_FORMULARIO_ADMIN.stream()
                .map(_rolRepository::findByNombre)
                .flatMap(Optional::stream)
                .map(RolServiceImpl::toResumen)
                .toList();
    }

    @Override
    public List<RolResumenDTO> listarRolesParaRegistroEstudiante() {
        return _rolRepository.findByNombre("ROLE_ESTUDIANTE")
                .map(r -> List.of(toResumen(r)))
                .orElse(List.of());
    }

    private static RolResumenDTO toResumen(Rol rol) {
        String nombre = rol.getNombre();
        String codigo = nombre != null && nombre.startsWith("ROLE_") ? nombre.substring(5) : nombre;
        return new RolResumenDTO(rol.getIdRol(), nombre, codigo);
    }

    @Override
    public RolDTO actualizarRol(Long idRol, CrearRolDTO rolDTO) {
        Rol rol = _rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));

        String nombreEstandar = estandarizarNombre(rolDTO.getNombre());

        if (!rol.getNombre().equals(nombreEstandar) && _rolRepository.existsByNombre(nombreEstandar)) {
            throw new RecursoDuplicadoException("Ya existe un rol con el nombre: " + nombreEstandar);
        }

        rol.setNombre(nombreEstandar);
        rol.setDescripcion(rolDTO.getDescripcion());

        rol = _rolRepository.save(rol);
        return _modelMapper.map(rol, RolDTO.class);
    }

    @Override
    public String inactivarRol(Long idRol) {
        Rol rol = _rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));

        rol.setActivo(false);
        _rolRepository.save(rol);
        return "Rol inactivado correctamente";
    }

    private String estandarizarNombre(String nombre) {
        String upper = nombre.trim().toUpperCase().replace(" ", "_");
        if (!upper.startsWith("ROLE_")) {
            upper = "ROLE_" + upper;
        }
        return upper;
    }
}
