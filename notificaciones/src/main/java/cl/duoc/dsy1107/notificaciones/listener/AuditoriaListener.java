package cl.duoc.dsy1107.notificaciones.listener;

import cl.duoc.dsy1107.notificaciones.evento.EventoParser;
import cl.duoc.dsy1107.notificaciones.evento.SolicitudEvento;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Consumidor del caso 3: registra TODOS los eventos (routing key solicitud.*).
 * Solo deja constancia en el log, no avisa a nadie.
 */
@Component
public class AuditoriaListener {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaListener.class);

    private final EventoParser parser;

    public AuditoriaListener(EventoParser parser) {
        this.parser = parser;
    }

    // El nombre viene del application.yaml (app.rabbit.auditoria.queue).
    @RabbitListener(queues = "${app.rabbit.auditoria.queue}")
    public void recibir(String mensaje,
                        Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws IOException {
        try {
            SolicitudEvento evento = parser.parsear(mensaje);

            log.info("[AUDITORIA] Evento '{}' registrado: id={}, solicitud #{}, estado={}, solicitante={}",
                    routingKey, evento.messageId(), evento.solicitudId(), evento.estado(),
                    evento.solicitanteEmail());

            canal.basicAck(deliveryTag, false);
            log.info("[AUDITORIA] ACK enviado (deliveryTag={})", deliveryTag);

        } catch (Exception e) {
            log.error("[AUDITORIA] Evento inválido, va a la DLQ: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);
            log.warn("[AUDITORIA] NACK enviado sin reencolar (deliveryTag={})", deliveryTag);
        }
    }
}