package com.certiva.api.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.certiva.api.DTO.CampoFormularioDTO;
import com.certiva.api.DTO.CrearEventoDTO;
import com.certiva.api.DTO.EventoCupoVerificacionDTO;
import com.certiva.api.DTO.EventoDTO;
import com.certiva.api.DTO.EventoFilaAdminDTO;
import com.certiva.api.DTO.EventoCatalogoPublicoDTO;
import com.certiva.api.DTO.EventoPublicoDTO;
import com.certiva.api.DTO.EventoResumenTipoDTO;
import com.certiva.api.DTO.EventoRevisionPanelDTO;
import com.certiva.api.DTO.ProfesorPanelDTO;
import org.springframework.web.multipart.MultipartFile;

import com.certiva.api.enums.ModalidadEvento;
import com.certiva.api.enums.TipoEventoEnum;

public interface EventoService {

    EventoDTO crearEvento(CrearEventoDTO eventoDTO, MultipartFile imagen, MultipartFile pensum);

    List<EventoDTO> listarEventos(Boolean soloActivos,
                                  ModalidadEvento modalidad,
                                  TipoEventoEnum tipo,
                                  LocalDateTime desde,
                                  LocalDateTime hasta);

    List<EventoResumenTipoDTO> listarResumenTipos(Boolean soloActivos, ModalidadEvento modalidad);

    List<EventoFilaAdminDTO> listarVistaAdmin(Boolean soloActivos,
                                              ModalidadEvento modalidad,
                                              TipoEventoEnum tipo,
                                              LocalDateTime desde,
                                              LocalDateTime hasta);

    EventoDTO obtenerPorId(Long idEvento);

    EventoDTO actualizarEvento(EventoDTO eventoDTO);

    String inactivarEvento(Long idEvento);

    void borrarEventoLogico(Long idEvento);

    EventoCupoVerificacionDTO verificarCupo(Long idEvento);

    List<CampoFormularioDTO> listarCamposPorEvento(Long idEvento);

    EventoPublicoDTO obtenerPublicoPorCodigoDifusion(String codigoDifusion);

    List<EventoCatalogoPublicoDTO> listarCatalogoPublico();

    ProfesorPanelDTO obtenerPanelProfesor();

    EventoRevisionPanelDTO obtenerRevisionCierre(Long idEvento);

    void cancelarEvento(Long idEvento);
}
