package cl.duoc.dsy1107.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /exchanges. Siempre se crea durable. */
public record ExchangeRequest(

        @NotBlank(message = "el nombre del exchange es obligatorio")
        @Size(max = 255, message = "el nombre no puede pasar de 255 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$",
                message = "el nombre solo admite letras, números, punto, guion y guion bajo")
        String name,

        @NotBlank(message = "el tipo es obligatorio")
        @Pattern(regexp = "^(direct|topic|fanout)$",
                message = "el tipo debe ser direct, topic o fanout")
        String type) {
}

