package com.certiva.api.Service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.certiva.api.DTO.CrearUsuarioDTO;
import com.certiva.api.DTO.ImportacionCsvResultadoDTO;
import com.certiva.api.DTO.LoginDTO;
import com.certiva.api.DTO.LoginRespuestaDTO;
import com.certiva.api.DTO.UsuarioDTO;
import com.certiva.api.DTO.UsuarioStaffDTO;

public interface UsuarioService {

    CrearUsuarioDTO registrarUsuario(CrearUsuarioDTO usuarioDTO);

    CrearUsuarioDTO crearUsuarioAdministracion(CrearUsuarioDTO usuarioDTO);

    LoginRespuestaDTO login(LoginDTO loginDTO, String ip);

    UsuarioDTO obtenerUsuarioPorId(Long idUsuario);

    List<UsuarioDTO> listarUsuarios();

    UsuarioDTO actualizarUsuario(UsuarioDTO usuarioDTO);

    UsuarioDTO cambiarRolUsuario(Long idUsuario, Long idRol);

    String inactivarUsuario(Long idUsuario);

    ImportacionCsvResultadoDTO importarUsuariosDesdeCsv(MultipartFile archivo);

    List<UsuarioStaffDTO> listarStaffPorRol(String codigoRol, String busqueda);

    UsuarioDTO actualizarMiTelefono(String telefono);
}
