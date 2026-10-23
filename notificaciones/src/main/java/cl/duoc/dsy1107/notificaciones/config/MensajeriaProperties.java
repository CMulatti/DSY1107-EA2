package cl.duoc.dsy1107.notificaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Los nombres de la mensajería, leídos desde application.yaml (sección app.rabbit).
 * Ninguna otra clase escribe un nombre de cola o exchange a mano: lo piden aquí.
 */
@ConfigurationProperties(prefix = "app.rabbit")
public record MensajeriaProperties(
        String exchangeEvents,
        String exchangeDlx,
        int dlqTtlMs,
        Cola aprobador,
        Cola solicitante,
        Cola auditoria) {

    /** Una cola, la routing key que la alimenta, y su cola de mensajes fallidos. */
    public record Cola(String queue, String routingKey, String dlq, String dlqRoutingKey) {
    }
}