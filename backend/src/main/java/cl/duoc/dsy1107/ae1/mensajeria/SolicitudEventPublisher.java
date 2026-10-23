package cl.duoc.dsy1107.ae1.mensajeria;

import cl.duoc.dsy1107.ae1.config.MensajeriaProperties;
import cl.duoc.dsy1107.ae1.solicitudes.Solicitud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;

/**
 * Publica los eventos de solicitudes en RabbitMQ.
 * Nunca lanza excepciones: si RabbitMQ falla, se registra y la petición sigue.
 */
@Component
public class SolicitudEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SolicitudEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;
    private final MensajeriaProperties props;
    private final Clock reloj;

    public SolicitudEventPublisher(RabbitTemplate rabbitTemplate, ObjectMapper mapper,
                                   MensajeriaProperties props, Clock reloj) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
        this.props = props;
        this.reloj = reloj;
    }

    public void solicitudCreada(Solicitud solicitud) {
        publicar(props.routingKeyCreada(), "creada", solicitud);
    }

    public void solicitudDecidida(Solicitud solicitud) {
        publicar(props.routingKeyDecidida(), "decidida", solicitud);
    }

    private void publicar(String routingKey, String tipo, Solicitud solicitud) {
        if (!props.enabled()) {
            log.debug("[RABBIT] Mensajería desactivada, no se publica {}", routingKey);
            return;
        }
        try {
            SolicitudEvento evento = SolicitudEvento.de(tipo, solicitud, reloj.instant());
            String json = mapper.writeValueAsString(evento);

            rabbitTemplate.convertAndSend(props.exchangeEvents(), routingKey, json, mensaje -> {
                mensaje.getMessageProperties().setMessageId(evento.messageId());
                return mensaje;
            });
            log.info("[RABBIT] Evento publicado: {} ({})", routingKey, evento.messageId());

        } catch (Exception e) {
            log.error("[RABBIT] No se pudo publicar {}: {}", routingKey, e.getMessage());
        }
    }
}