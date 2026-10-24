package cl.duoc.dsy1107.notificaciones.listener;

import cl.duoc.dsy1107.notificaciones.evento.EventoInvalidoException;
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
 * Consumidor del caso 2: avisa al solicitante que su solicitud fue decidida.
 * El "correo" es simulado: solo se escribe en el log.
 */
@Component
public class SolicitanteListener {

    private static final Logger log = LoggerFactory.getLogger(SolicitanteListener.class);

    private final EventoParser parser;

    public SolicitanteListener(EventoParser parser) {
        this.parser = parser;
    }

    // El nombre viene del application.yaml (app.rabbit.solicitante.queue).
    @RabbitListener(queues = "${app.rabbit.solicitante.queue}")
    public void recibir(String mensaje,
                        Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            SolicitudEvento evento = parser.parsear(mensaje);
            log.info("[SOLICITANTE] Evento recibido: {}", evento.messageId());

            if (!"APROBADA".equals(evento.estado()) && !"RECHAZADA".equals(evento.estado())) {
                throw new EventoInvalidoException("Una solicitud decidida debe estar APROBADA o RECHAZADA");
            }

            String comentario = evento.comentarioAprobador() == null
                    ? "(sin comentario)" : evento.comentarioAprobador();

            log.info("[SOLICITANTE] Correo (simulado) a {}: tu solicitud #{} fue {}. Comentario del aprobador: {}",
                    evento.solicitanteEmail(), evento.solicitudId(), evento.estado(), comentario);

            canal.basicAck(deliveryTag, false);
            log.info("[SOLICITANTE] ACK enviado (deliveryTag={})", deliveryTag);

        } catch (Exception e) {
            log.error("[SOLICITANTE] Mensaje inválido o con fallo, va a la DLQ: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);
            log.warn("[SOLICITANTE] NACK enviado sin reencolar (deliveryTag={})", deliveryTag);
        }
    }
}