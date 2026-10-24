package cl.duoc.dsy1107.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST y DELETE /bindings: une una cola a un exchange con una routing key. */
public record BindingRequest(

        @NotBlank(message = "el nombre de la cola es obligatorio")
        String queue,

        @NotBlank(message = "el nombre del exchange es obligatorio")
        String exchange,

        // Puede ir vacía: un exchange fanout ignora la routing key.
        @NotNull(message = "la routingKey es obligatoria (puede ser vacía)")
        @Size(max = 255, message = "la routingKey no puede pasar de 255 caracteres")
        String routingKey) {
}