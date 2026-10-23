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
 * Consumidor del caso 1: avisa al aprobador que llegó una solicitud nueva.
 * El "correo" es simulado: solo se escribe en el log.
 */
@Component
public class AprobadorListener {

    private static final Logger log = LoggerFactory.getLogger(AprobadorListener.class);

    // El nombre de la cola viene del application.yaml, no se escribe a mano.
    @RabbitListener(queues = "${app.rabbit.aprobador.queue}")  //this annotation tells Spring AMQP to register a listener for the specified RabbitMQ queue. Spring sees the annotation and sets up a listener container and a RabbitMQ consumer to receive messages from the configured queue.
    public void recibir(String mensaje,
                        Channel canal,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            log.info("[APROBADOR] Mensaje recibido: {}", mensaje);

            // SOLO PARA PRUEBAS: simula un fallo para ver cómo llega a la DLQ.
            if (mensaje.contains("FALLAR")) {
                throw new IllegalStateException("Fallo simulado al enviar el correo");
            }

            log.info("[APROBADOR] Correo (simulado) enviado al aprobador");
            canal.basicAck(deliveryTag, false);          // ACK: terminé, bórralo

        } catch (Exception e) {
            log.error("[APROBADOR] Falló el procesamiento, el mensaje va a la DLQ: {}", e.getMessage());
            canal.basicNack(deliveryTag, false, false);  // NACK sin reencolar: va al DLX
        }
    }
}