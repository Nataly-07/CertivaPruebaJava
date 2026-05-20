package com.certiva.api.Service;

import java.util.List;

import com.certiva.api.DTO.Rol.CrearRolDTO;
import com.certiva.api.DTO.Rol.RolDTO;
import com.certiva.api.DTO.RolResumenDTO;

public interface RolService {

    RolDTO crearRol(CrearRolDTO rol);

    List<RolDTO> listarRoles();

    List<RolDTO> listarRolesActivos();

    /** Roles permitidos en formularios de administración (orden fijo). */
    List<RolResumenDTO> listarRolesParaAdministracion();

    /** Solo {@code ROLE_ESTUDIANTE} para el registro público. */
    List<RolResumenDTO> listarRolesParaRegistroEstudiante();

    RolDTO actualizarRol(Long idRol, CrearRolDTO rol);

    String inactivarRol(Long idRol);
}
