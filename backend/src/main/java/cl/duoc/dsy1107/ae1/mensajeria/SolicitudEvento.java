package cl.duoc.dsy1107.ae1.mensajeria;

import cl.duoc.dsy1107.ae1.solicitudes.Solicitud;

import java.time.Instant;

/** Lo que viaja en el mensaje. El messageId identifica el evento (sirve para detectar duplicados). */
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

    public static SolicitudEvento de(String tipo, Solicitud s, Instant ahora) {
        return new SolicitudEvento(
                "solicitud-" + s.getId() + "-" + tipo,
                tipo,
                s.getId(),
                s.getSolicitanteEmail(),
                s.getFechaInicio().toString(),
                s.getFechaFin().toString(),
                s.getEstado(),
                s.getComentarioAprobador(),
                ahora.toString());
    }
}