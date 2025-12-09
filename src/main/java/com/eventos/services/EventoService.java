package com.eventos.services;

import com.eventos.exceptions.ValidationException;
import com.eventos.models.EstadoEvento;
import com.eventos.models.Evento;
import com.eventos.repositories.EventoRepository;
import com.eventos.utils.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio para gestionar eventos.
 * Encapsula validaciones y operaciones de alto nivel antes de tocar el repositorio.
 */
public class EventoService {

    private final EventoRepository eventoRepository;

    public EventoService() {
        this.eventoRepository = new EventoRepository();
    }

    public Evento crear(Evento evento) {
        validarEvento(evento, true);
        return eventoRepository.save(evento);
    }

    public Evento actualizar(Evento evento) {
        if (evento.getId() == null) {
            throw new ValidationException("El evento debe tener ID para actualizar");
        }
        validarEvento(evento, false);
        return eventoRepository.update(evento);
    }

    public boolean eliminar(Long id) {
        if (id == null) {
            throw new ValidationException("ID de evento requerido");
        }
        return eventoRepository.delete(id);
    }

    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    public Optional<Evento> buscarPorId(Long id) {
        return eventoRepository.findById(id);
    }

    public List<Evento> buscarPorNombre(String texto) {
        return (texto == null || texto.isBlank())
            ? listarTodos()
            : eventoRepository.searchByNombre(texto.trim());
    }

    public List<Evento> filtrarPorEstado(EstadoEvento estado) {
        if (estado == null) {
            return listarTodos();
        }
        return eventoRepository.findByEstado(estado.name());
    }

    public List<Evento> proximos() {
        return eventoRepository.findProximosEventos();
    }

    private void validarEvento(Evento evento, boolean esNuevo) {
        if (evento == null) {
            throw new ValidationException("El evento no puede ser nulo");
        }

        Validator.validarNoVacio(evento.getNombre(), "Nombre del evento");
        if (evento.getTipoEvento() == null) {
            throw new ValidationException("Debe seleccionar un tipo de evento");
        }
        if (evento.getSede() == null) {
            throw new ValidationException("Debe seleccionar una sede");
        }

        if (evento.getFechaInicio() == null || evento.getFechaFin() == null) {
            throw new ValidationException("Las fechas de inicio y fin son obligatorias");
        }

        LocalDateTime inicio = evento.getFechaInicio();
        LocalDateTime fin = evento.getFechaFin();
        if (inicio.isAfter(fin)) {
            throw new ValidationException("La fecha de inicio debe ser anterior a la de fin");
        }

        if (evento.getAforoMaximo() == null || evento.getAforoMaximo() <= 0) {
            throw new ValidationException("El aforo máximo debe ser mayor que 0");
        }

        if (!esNuevo && evento.getAforoActual() != null && evento.getAforoActual() > evento.getAforoMaximo()) {
            throw new ValidationException("El aforo actual no puede superar el aforo máximo");
        }

        // Estado por defecto
        if (evento.getEstado() == null) {
            evento.setEstado(EstadoEvento.PLANIFICADO);
        }
    }
}
