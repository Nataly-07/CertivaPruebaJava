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
import com.certiva.api.DTO.EventoAsistenciaEnVivoDTO;
import com.certiva.api.DTO.AsistenciaManualRequestDTO;
import com.certiva.api.DTO.CheckInRespuestaDTO;
import com.certiva.api.DTO.EventoContenidoAcademicoDTO;
import com.certiva.api.DTO.GuardarEventoContenidoAcademicoDTO;
import com.certiva.api.DTO.GuardarRevisionEvaluacionesDTO;
import com.certiva.api.DTO.ProfesorParticipanteDTO;
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
                                              LocalDateTime hasta,
                                              com.certiva.api.enums.EstadoOperativoEvento estadoOperativo);

    EventoDTO reasignarStaff(Long idEvento, com.certiva.api.DTO.ReasignarStaffDTO dto);

    EventoDTO obtenerPorId(Long idEvento);

    EventoDTO actualizarEvento(EventoDTO eventoDTO);

    String inactivarEvento(Long idEvento);

    void borrarEventoLogico(Long idEvento);

    EventoCupoVerificacionDTO verificarCupo(Long idEvento);

    List<CampoFormularioDTO> listarCamposPorEvento(Long idEvento);

    EventoPublicoDTO obtenerPublicoPorCodigoDifusion(String codigoDifusion);

    EventoPublicoDTO obtenerPublicoPorId(Long idEvento);

    List<EventoCatalogoPublicoDTO> listarCatalogoPublico();

    ProfesorPanelDTO obtenerPanelProfesor();

    EventoRevisionPanelDTO obtenerRevisionCierre(Long idEvento);

    EventoAsistenciaEnVivoDTO obtenerAsistenciaEnVivo(Long idEvento);

    EventoRevisionPanelDTO guardarEvaluacionesRevision(Long idEvento, GuardarRevisionEvaluacionesDTO dto);

    EventoContenidoAcademicoDTO obtenerContenidoAcademico(Long idEvento);

    EventoContenidoAcademicoDTO guardarContenidoAcademico(Long idEvento, GuardarEventoContenidoAcademicoDTO dto);

    List<ProfesorParticipanteDTO> listarParticipantesAsignados(Long idEvento);

    CheckInRespuestaDTO registrarAsistenciaManual(AsistenciaManualRequestDTO dto);

    void cancelarEvento(Long idEvento);
}
