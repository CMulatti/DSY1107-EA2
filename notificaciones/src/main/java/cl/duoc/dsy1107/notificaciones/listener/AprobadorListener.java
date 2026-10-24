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
 * Consumidor del caso 1: avisa al aprobador que llegó una solicitud nueva.
 * El "correo" es simulado: solo se escribe en el log.
 */
@Component
public class AprobadorListener {

    private static final Logger log = LoggerFactory.getLogger(AprobadorListener.class);

    private final EventoParser parser;

    public AprobadorListener(EventoParser parser) {
        this.parser = parser;
    }

    // El nombre viene del application.yaml (app.rabbit.aprobador.queue).
    @RabbitListener(queues = "${app.rabbit.aprobador.queue}")
    public void recibir(String mensaje,
                        Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            SolicitudEvento evento = parser.parsear(mensaje);
            log.info("[APROBADOR] Evento recibido: {}", evento.messageId());

            log.info("[APROBADOR] Correo (simulado) al aprobador: nueva solicitud #{} de {}, del {} al {}",
                    evento.solicitudId(), evento.solicitanteEmail(), evento.fechaInicio(), evento.fechaFin());

            canal.basicAck(deliveryTag, false);
            log.info("[APROBADOR] ACK enviado (deliveryTag={})", deliveryTag);

        } catch (Exception e) {
            log.error("[APROBADOR] Mensaje inválido o con fallo, va a la DLQ: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);
            log.warn("[APROBADOR] NACK enviado sin reencolar (deliveryTag={})", deliveryTag);
        }
    }
}
