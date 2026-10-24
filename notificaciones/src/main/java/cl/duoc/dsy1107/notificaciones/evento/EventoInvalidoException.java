package cl.duoc.dsy1107.notificaciones.evento;

/** El mensaje no se puede procesar: está vacío, no es JSON, o le faltan campos. */
public class EventoInvalidoException extends RuntimeException {
    public EventoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
