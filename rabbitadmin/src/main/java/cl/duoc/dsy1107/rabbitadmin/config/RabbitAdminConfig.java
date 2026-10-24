package cl.duoc.dsy1107.rabbitadmin.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Crea el RabbitAdmin: la pieza de Spring que declara y borra colas,
 * exchanges y bindings en RabbitMQ mientras la aplicación está corriendo.
 * Solo configuración: aquí no hay lógica.
 */
@Configuration
public class RabbitAdminConfig {

    // ConnectionFactory es el de org.springframework.amqp.rabbit.connection
    // (el del tutorial, amqp.core, no existe).
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}