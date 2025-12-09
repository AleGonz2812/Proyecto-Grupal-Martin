package com.eventos.services;

import com.eventos.exceptions.ValidationException;
import com.eventos.models.*;
import com.eventos.utils.QRService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.eventos.config.HibernateUtil.getEntityManager;

/**
 * Servicio de negocio para procesar compras y generar entradas con QR.
 */
public class CompraService {

    private final QRService qrService;
    private final ObjectMapper objectMapper;

    public CompraService() {
        this.qrService = new QRService();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Procesa una compra completa: valida, crea compra, entradas, QR y actualiza aforo.
     */
    public Compra procesarCompra(Long usuarioId, Long eventoId, Long tipoEntradaId, int cantidad, String metodoPago) {
        if (usuarioId == null || eventoId == null || tipoEntradaId == null) {
            throw new ValidationException("Usuario, evento y tipo de entrada son obligatorios");
        }
        if (cantidad <= 0) {
            throw new ValidationException("La cantidad debe ser mayor que 0");
        }

        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Usuario usuario = em.find(Usuario.class, usuarioId);
            Evento evento = em.find(Evento.class, eventoId);
            TipoEntrada tipoEntrada = em.find(TipoEntrada.class, tipoEntradaId);

            if (usuario == null) throw new ValidationException("Usuario no encontrado");
            if (evento == null) throw new ValidationException("Evento no encontrado");
            if (tipoEntrada == null) throw new ValidationException("Tipo de entrada no encontrado");

            // Validaciones de negocio
            if (!evento.hayDisponibilidad() || evento.getAforoActual() + cantidad > evento.getAforoMaximo()) {
                throw new ValidationException("No hay aforo disponible para la cantidad solicitada");
            }
            if (evento.getEstado() == EstadoEvento.CANCELADO || evento.getEstado() == EstadoEvento.FINALIZADO) {
                throw new ValidationException("El evento no está disponible para comprar");
            }

            BigDecimal total = tipoEntrada.getPrecio().multiply(BigDecimal.valueOf(cantidad));

            Compra compra = new Compra();
            compra.setUsuario(usuario);
            compra.setTotal(total);
            compra.setMetodoPago(metodoPago);
            compra.setEstado(EstadoCompra.COMPLETADA);
            compra.setFechaCompra(LocalDateTime.now());
            compra.setCodigoConfirmacion(UUID.randomUUID().toString());

            em.persist(compra);

            for (int i = 0; i < cantidad; i++) {
                Entrada entrada = new Entrada();
                entrada.setTipoEntrada(tipoEntrada);
                entrada.setEvento(evento);
                entrada.setCompra(compra);
                
                // Generar número de entrada único
                String numeroEntrada = "ENT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
                entrada.setNumeroEntrada(numeroEntrada);

                String payload = buildPayload(evento, compra, usuario, tipoEntrada);
                String qrBase64 = qrService.generarQRBase64(payload, 250);
                entrada.setCodigoQR(qrBase64);

                em.persist(entrada);
                compra.addEntrada(entrada);
            }

            // Actualizar aforo
            evento.setAforoActual(evento.getAforoActual() + cantidad);
            em.merge(evento);

            // Confirmación JSON
            compra.setConfirmacionJSON(buildConfirmacionJson(compra, evento, usuario));
            em.merge(compra);

            tx.commit();
            return compra;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Error al procesar la compra", e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    private String buildPayload(Evento evento, Compra compra, Usuario usuario, TipoEntrada tipoEntrada) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("evento", evento.getNombre());
        payload.put("compra", compra.getCodigoConfirmacion());
        payload.put("usuario", usuario.getEmail());
        payload.put("tipoEntrada", tipoEntrada.getNombre());
        payload.put("fecha", compra.getFechaCompra());
        return payload.toString();
    }

    private String buildConfirmacionJson(Compra compra, Evento evento, Usuario usuario) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("compraId", compra.getId());
            data.put("codigoConfirmacion", compra.getCodigoConfirmacion());
            data.put("usuario", usuario.getEmail());
            data.put("evento", evento.getNombre());
            data.put("fechaEvento", evento.getFechaInicio());
            data.put("total", compra.getTotal());
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar JSON de confirmación", e);
        }
    }
}
