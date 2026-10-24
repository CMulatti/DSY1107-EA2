package cl.duoc.dsy1107.notificaciones.evento;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Lo que viaja en el mensaje. Es la copia, del lado consumidor, del evento que publica el backend. */
@JsonIgnoreProperties(ignoreUnknown = true)   // si el backend agrega campos nuevos, no se rompe
public record SolicitudEvento(
        String messageId,
        String tipo,
        Long solicitudId,
        String solicitanteEmail,
        String fechaInicio,
        String fechaFin,
        String estado,
        String comentarioAprobador,
        String ocurridoEn) {
}

