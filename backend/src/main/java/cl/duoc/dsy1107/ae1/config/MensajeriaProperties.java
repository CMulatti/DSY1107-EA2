package cl.duoc.dsy1107.ae1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Los nombres de la mensajería, leídos desde application.yaml (app.rabbit). */
@ConfigurationProperties(prefix = "app.rabbit")
public record MensajeriaProperties(
        boolean enabled,
        String exchangeEvents,
        String routingKeyCreada,
        String routingKeyDecidida) {
}

