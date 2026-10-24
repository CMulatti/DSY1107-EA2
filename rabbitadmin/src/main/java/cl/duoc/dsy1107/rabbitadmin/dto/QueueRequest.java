package cl.duoc.dsy1107.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /queues. Siempre crea una cola quorum y durable. */
public record QueueRequest(

        @NotBlank(message = "el nombre de la cola es obligatorio")
        @Size(max = 255, message = "el nombre no puede pasar de 255 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$",
                message = "el nombre solo admite letras, números, punto, guion y guion bajo")
        String name,

        @Positive(message = "el ttlMs debe ser mayor que 0")
        Long ttlMs) {      // opcional: vida máxima de un mensaje, en milisegundos
}

