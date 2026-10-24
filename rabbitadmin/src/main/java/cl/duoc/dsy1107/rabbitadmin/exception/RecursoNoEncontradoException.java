package cl.duoc.dsy1107.rabbitadmin.exception;

/** La cola no existe (se traduce a 404). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}