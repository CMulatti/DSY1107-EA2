package cl.duoc.dsy1107.rabbitadmin.service;

import cl.duoc.dsy1107.rabbitadmin.dto.BindingRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.ExchangeRequest;
import cl.duoc.dsy1107.rabbitadmin.dto.QueueInfoResponse;
import cl.duoc.dsy1107.rabbitadmin.dto.QueueRequest;
import cl.duoc.dsy1107.rabbitadmin.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

/**
 * Toda la lógica de RabbitMQ: crear, consultar y borrar colas, exchanges y
 * bindings. El controlador solo llama a estos métodos.
 *
 * Aquí no se atrapan errores: si RabbitMQ rechaza algo, la excepción sube y
 * ManejadorDeErrores la convierte en un código HTTP.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // ===== COLAS =====

    /** Crea una cola quorum y durable (la regla de la clase: todas son quorum). */
    public void crearCola(QueueRequest req) {
        QueueBuilder builder = QueueBuilder.durable(req.name()).quorum();
        if (req.ttlMs() != null) {
            builder.withArgument("x-message-ttl", req.ttlMs());
        }
        rabbitAdmin.declareQueue(builder.build());
        log.info("[ADMIN] Cola declarada: {}", req.name());
    }

    public QueueInfoResponse obtenerCola(String name) {
        QueueInformation info = rabbitAdmin.getQueueInfo(name);
        if (info == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        return new QueueInfoResponse(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }


    public void eliminarCola(String name) {
        if (rabbitAdmin.getQueueInfo(name) == null) {
            throw new RecursoNoEncontradoException("La cola '" + name + "' no existe");
        }
        rabbitAdmin.deleteQueue(name);
        log.info("[ADMIN] Cola eliminada: {}", name);
    }

    // ===== EXCHANGES =====

    /** Crea un exchange durable del tipo pedido (el DTO ya comprobó que es válido). */
    public void crearExchange(ExchangeRequest req) {
        Exchange exchange;
        if (req.type().equals("direct")) {
            exchange = new DirectExchange(req.name(), true, false);
        } else if (req.type().equals("topic")) {
            exchange = new TopicExchange(req.name(), true, false);
        } else {
            exchange = new FanoutExchange(req.name(), true, false);
        }
        rabbitAdmin.declareExchange(exchange);
        log.info("[ADMIN] Exchange declarado: {} ({})", req.name(), req.type());
    }

    /** Borrar un exchange que no existe no da error en RabbitMQ. */
    public void eliminarExchange(String name) {
        rabbitAdmin.deleteExchange(name);
        log.info("[ADMIN] Exchange eliminado: {}", name);
    }

    // ===== BINDINGS =====

    public void crearBinding(BindingRequest req) {
        rabbitAdmin.declareBinding(aBinding(req));
        log.info("[ADMIN] Binding creado: {} <- {} ({})", req.queue(), req.exchange(), req.routingKey());
    }

    public void eliminarBinding(BindingRequest req) {
        rabbitAdmin.removeBinding(aBinding(req));
        log.info("[ADMIN] Binding eliminado: {} <- {} ({})", req.queue(), req.exchange(), req.routingKey());
    }

    /** Arma el objeto Binding que RabbitAdmin entiende. */
    private Binding aBinding(BindingRequest req) {
        return new Binding(req.queue(), Binding.DestinationType.QUEUE,
                req.exchange(), req.routingKey(), null);
    }
}