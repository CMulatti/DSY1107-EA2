package cl.duoc.dsy1107.notificaciones.listener;

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

    // El nombre viene del application.yaml (app.rabbit.auditoria.queue).
    @RabbitListener(queues = "${app.rabbit.auditoria.queue}")
    public void recibir(String mensaje,
                        Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) throws IOException {
        try {
            log.info("[AUDITORIA] Evento '{}' registrado: {}", routingKey, mensaje);

            // SOLO PARA PRUEBAS. Usa otra palabra que los otros dos para que
            // cada DLQ se pueda probar por separado.
            if (mensaje.contains("ROMPER")) {
                throw new IllegalStateException("Fallo simulado al registrar el evento");
            }

            canal.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[AUDITORIA] Falló el registro, el mensaje va a la DLQ: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);
        }
    }
}