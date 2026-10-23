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
 * Consumidor del caso 2: avisa al solicitante que su solicitud fue decidida.
 * El "correo" es simulado: solo se escribe en el log.
 */
@Component
public class SolicitanteListener {

    private static final Logger log = LoggerFactory.getLogger(SolicitanteListener.class);

    // El nombre viene del application.yaml (app.rabbit.solicitante.queue).
    @RabbitListener(queues = "${app.rabbit.solicitante.queue}")
    public void recibir(String mensaje,
                        Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[SOLICITANTE] Mensaje recibido: {}", mensaje);

            // SOLO PARA PRUEBAS: simula un fallo para ver cómo llega a la DLQ.
            if (mensaje.contains("FALLAR")) {
                throw new IllegalStateException("Fallo simulado al enviar el correo");
            }

            log.info("[SOLICITANTE] Correo (simulado) enviado al solicitante");
            canal.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[SOLICITANTE] Falló el procesamiento, el mensaje va a la DLQ: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);
        }
    }
}