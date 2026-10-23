package cl.duoc.dsy1107.ae1.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara el exchange al que el backend publica. Solo configuración.
 * Debe ser idéntico al de notificaciones (topic, durable): si difiere,
 * RabbitMQ responde PRECONDITION_FAILED. Declararlo aquí evita depender
 * de que notificaciones haya arrancado primero.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange eventsExchange(MensajeriaProperties props) {
        return new TopicExchange(props.exchangeEvents(), true, false);
    }
}

