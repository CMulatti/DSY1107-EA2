package cl.duoc.dsy1107.notificaciones.evento;

import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Convierte el texto del mensaje en un SolicitudEvento y comprueba que esté completo. */
@Component
public class EventoParser {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    public SolicitudEvento parsear(String json) {
        if (json == null || json.isBlank()) {
            throw new EventoInvalidoException("El mensaje está vacío");
        }

        SolicitudEvento evento;
        try {
            evento = mapper.readValue(json, SolicitudEvento.class);
        } catch (Exception e) {
            throw new EventoInvalidoException("El mensaje no es un JSON válido");
        }

        if (evento == null
                || evento.messageId() == null
                || evento.tipo() == null
                || evento.solicitudId() == null
                || evento.solicitanteEmail() == null
                || evento.estado() == null) {
            throw new EventoInvalidoException("Al evento le faltan campos obligatorios");
        }
        return evento;
    }
}

