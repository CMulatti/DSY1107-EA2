package cl.duoc.dsy1107.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara la topología de RabbitMQ: exchanges, colas y bindings.
 * Solo configuración: aquí no hay lógica de negocio.
 *
 * Ruta de un mensaje normal:
 *   backend -> solicitudes.events (topic) --routing key--> cola de trabajo
 *
 * Ruta de un mensaje fallido (nack sin reencolar):
 *   cola de trabajo -> solicitudes.dlx (direct) --dlq-routing-key--> su DLQ
 */
@Configuration
public class RabbitMQConfig {

    private final MensajeriaProperties props;

    public RabbitMQConfig(MensajeriaProperties props) {
        this.props = props;           //The constructor receives the MensajeriaProperties object that Spring has created and populated from our YAML.
    }

    // ========== EXCHANGES ==========

    /** Recibe los eventos del backend y los reparte según la routing key. */
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(props.exchangeEvents(), true, false);
    }

    /** Recibe los mensajes fallidos y los manda a la DLQ que corresponda. */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.exchangeDlx(), true, false);
    }

    // ========== CASO 1: aviso al aprobador (solicitud.creada) ==========

    @Bean
    public Queue aprobadorQueue() {
        return colaDeTrabajo(props.aprobador());
    }

    @Bean
    public Queue aprobadorDlq() {
        return colaDlq(props.aprobador());
    }

    @Bean
    public Binding aprobadorBinding() {
        return BindingBuilder.bind(aprobadorQueue())
                .to(eventsExchange())
                .with(props.aprobador().routingKey());
    }

    @Bean
    public Binding aprobadorDlqBinding() {
        return BindingBuilder.bind(aprobadorDlq())
                .to(deadLetterExchange())
                .with(props.aprobador().dlqRoutingKey());
    }

    // ========== CASO 2: aviso al solicitante (solicitud.decidida) ==========

    @Bean
    public Queue solicitanteQueue() {
        return colaDeTrabajo(props.solicitante());
    }

    @Bean
    public Queue solicitanteDlq() {
        return colaDlq(props.solicitante());
    }

    @Bean
    public Binding solicitanteBinding() {
        return BindingBuilder.bind(solicitanteQueue())
                .to(eventsExchange())
                .with(props.solicitante().routingKey());
    }

    @Bean
    public Binding solicitanteDlqBinding() {
        return BindingBuilder.bind(solicitanteDlq())
                .to(deadLetterExchange())
                .with(props.solicitante().dlqRoutingKey());
    }

    // ========== CASO 3: auditoría de todos los eventos (solicitud.*) ==========

    @Bean
    public Queue auditoriaQueue() {
        return colaDeTrabajo(props.auditoria());
    }

    @Bean
    public Queue auditoriaDlq() {
        return colaDlq(props.auditoria());
    }

    @Bean
    public Binding auditoriaBinding() {
        return BindingBuilder.bind(auditoriaQueue())
                .to(eventsExchange())
                .with(props.auditoria().routingKey());
    }

    @Bean
    public Binding auditoriaDlqBinding() {
        return BindingBuilder.bind(auditoriaDlq())
                .to(deadLetterExchange())
                .with(props.auditoria().dlqRoutingKey());
    }

    // ========== AYUDAS PRIVADAS ==========

    /**
     * Cola de trabajo: quorum (se replica en los tres nodos) y sin TTL.
     * Si un mensaje falla, va al DLX con la routing key de SU DLQ.
     */
    private Queue colaDeTrabajo(MensajeriaProperties.Cola cola) {
        return QueueBuilder.durable(cola.queue())
                .quorum()
                .deadLetterExchange(props.exchangeDlx())
                .deadLetterRoutingKey(cola.dlqRoutingKey())
                .build();
    }

    /** DLQ: también quorum, con TTL para que los fallidos se limpien solos. */
    private Queue colaDlq(MensajeriaProperties.Cola cola) {
        return QueueBuilder.durable(cola.dlq())
                .quorum()
                .ttl(props.dlqTtlMs())
                .build();
    }
}