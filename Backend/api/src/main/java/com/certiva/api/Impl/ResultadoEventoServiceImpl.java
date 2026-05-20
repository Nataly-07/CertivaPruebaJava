package com.certiva.api.Impl;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.certiva.api.DTO.CrearResultadoEventoDTO;
import com.certiva.api.DTO.ResultadoEventoDTO;
import com.certiva.api.Entity.Evento;
import com.certiva.api.Entity.ResultadoEvento;
import com.certiva.api.Entity.Usuario;
import com.certiva.api.Repository.EventoRepository;
import com.certiva.api.Repository.ResultadoEventoRepository;
import com.certiva.api.Repository.UsuarioRepository;
import com.certiva.api.Service.ResultadoEventoService;

@Service
public class ResultadoEventoServiceImpl implements ResultadoEventoService {

    private final ResultadoEventoRepository _resultadoEventoRepository;
    private final EventoRepository _eventoRepository;
    private final UsuarioRepository _usuarioRepository;
    private final ModelMapper _modelMapper;

    public ResultadoEventoServiceImpl(ResultadoEventoRepository resultadoEventoRepository,
                                      EventoRepository eventoRepository,
                                      UsuarioRepository usuarioRepository,
                                      ModelMapper modelMapper) {
        this._resultadoEventoRepository = resultadoEventoRepository;
        this._eventoRepository = eventoRepository;
        this._usuarioRepository = usuarioRepository;
        this._modelMapper = modelMapper;
    }

    @Override
    public CrearResultadoEventoDTO crearResultadoEvento(CrearResultadoEventoDTO resultadoDTO) {
        ResultadoEvento resultado = new ResultadoEvento();
        resultado.setPuntaje(resultadoDTO.getPuntaje());
        resultado.setPosicion(null);
        resultado.setEsGanador(false);

        Evento evento = _eventoRepository.findById(resultadoDTO.getIdEvento())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
        resultado.setEvento(evento);

        Usuario usuario = _usuarioRepository.findById(resultadoDTO.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        resultado.setUsuario(usuario);

        resultado = _resultadoEventoRepository.save(resultado);
        return _modelMapper.map(resultado, CrearResultadoEventoDTO.class);
    }

    @Override
    public List<ResultadoEventoDTO> listarResultadoEventos() {
        List<ResultadoEvento> listado = _resultadoEventoRepository.findAll();
        return listado.stream()
                .map(r -> {
                    ResultadoEventoDTO dto = _modelMapper.map(r, ResultadoEventoDTO.class);
                    dto.setIdEvento(r.getEvento().getIdEvento());
                    dto.setIdUsuario(r.getUsuario().getIdUsuario());
                    return dto;
                })
                .toList();
    }

    @Override
    public ResultadoEventoDTO actualizarResultadoEvento(ResultadoEventoDTO resultadoDTO) {
        ResultadoEvento resultado = _resultadoEventoRepository.findById(resultadoDTO.getId())
                .orElseThrow(() -> new RuntimeException("Resultado de evento no encontrado"));

        resultado.setPuntaje(resultadoDTO.getPuntaje());
        resultado.setPosicion(resultadoDTO.getPosicion());
        resultado.setEsGanador(resultadoDTO.getEsGanador());

        if (resultadoDTO.getIdEvento() != null) {
            Evento evento = _eventoRepository.findById(resultadoDTO.getIdEvento())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
            resultado.setEvento(evento);
        }

        if (resultadoDTO.getIdUsuario() != null) {
            Usuario usuario = _usuarioRepository.findById(resultadoDTO.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            resultado.setUsuario(usuario);
        }

        resultado = _resultadoEventoRepository.save(resultado);
        ResultadoEventoDTO dto = _modelMapper.map(resultado, ResultadoEventoDTO.class);
        dto.setIdEvento(resultado.getEvento().getIdEvento());
        dto.setIdUsuario(resultado.getUsuario().getIdUsuario());
        return dto;
    }

    @Override
    public String inactivarResultadoEvento(Long id) {
        ResultadoEvento resultado = _resultadoEventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de evento no encontrado"));
        _resultadoEventoRepository.delete(resultado);
        return "Resultado de evento inactivado correctamente";
    }

    @Override
    public boolean borrarResultadoEvento(Long id) {
        ResultadoEvento resultado = _resultadoEventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado de evento no encontrado"));
        _resultadoEventoRepository.delete(resultado);
        return true;
    }
}
