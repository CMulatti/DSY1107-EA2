package cl.duoc.dsy1107.rabbitadmin.dto;

/** Lo que devuelve GET /queues/{name}. */
public record QueueInfoResponse(String name, long messages, int consumers) {
}
