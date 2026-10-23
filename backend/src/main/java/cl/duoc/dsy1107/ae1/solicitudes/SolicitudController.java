package cl.duoc.dsy1107.ae1.solicitudes;

import cl.duoc.dsy1107.ae1.mensajeria.SolicitudEventPublisher;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService servicio;

    private final SolicitudEventPublisher publicador;

    public SolicitudController(SolicitudService servicio, SolicitudEventPublisher publicador) {
        this.servicio = servicio;
        this.publicador = publicador;
    }

    public record SolicitudNueva(
            @NotNull(message = "la fecha de inicio es obligatoria")
            @FutureOrPresent(message = "la fecha de inicio no puede ser en el pasado")
            LocalDate fechaInicio,

            @NotNull(message = "la fecha de fin es obligatoria")
            LocalDate fechaFin,

            @NotBlank(message = "el motivo es obligatorio")
            @Size(max = 500, message = "el motivo no puede pasar de 500 caracteres")
            String motivo) {
    }

    public record Decision(
            @NotBlank(message = "el estado es obligatorio")
            String estado,

            @NotBlank(message = "el comentario es obligatorio")
            @Size(max = 500)
            String comentario) {
    }

    public record SolicitudVista(
            Long id,
            String solicitanteEmail,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String motivo,
            String estado,
            String comentarioAprobador,
            Instant creadoEn) {

        static SolicitudVista de(Solicitud s) {
            return new SolicitudVista(
                    s.getId(), s.getSolicitanteEmail(), s.getFechaInicio(), s.getFechaFin(),
                    s.getMotivo(), s.getEstado(), s.getComentarioAprobador(), s.getCreadoEn());
        }
    }

    private JwtClaims claims(String authorization) {
        return JwtClaims.desdeHeader(authorization);
    }

    /** Solicitante ve las suyas; aprobador ve todas. */
    @GetMapping
    public List<SolicitudVista> listar(@RequestHeader("Authorization") String authorization) {
        return servicio.listar(claims(authorization)).stream().map(SolicitudVista::de).toList();
    }

    @GetMapping("/{id}")
    public SolicitudVista obtener(@PathVariable long id, @RequestHeader("Authorization") String authorization) {
        return SolicitudVista.de(servicio.obtener(id, claims(authorization)));
    }

    @PostMapping
    public ResponseEntity<SolicitudVista> crear(
            @Valid @RequestBody SolicitudNueva datos,
            @RequestHeader("Authorization") String authorization) {
        Solicitud creada = servicio.crear(claims(authorization), datos.fechaInicio(), datos.fechaFin(), datos.motivo());
        publicador.solicitudCreada(creada);
        SolicitudVista vista = SolicitudVista.de(creada);
        return ResponseEntity.created(URI.create("/solicitudes/" + vista.id())).body(vista);
    }

    @PutMapping("/{id}")
    public SolicitudVista actualizar(
            @PathVariable long id,
            @Valid @RequestBody SolicitudNueva datos,
            @RequestHeader("Authorization") String authorization) {
        Solicitud actualizada = servicio.actualizar(
                id, claims(authorization), datos.fechaInicio(), datos.fechaFin(), datos.motivo());
        return SolicitudVista.de(actualizada);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id, @RequestHeader("Authorization") String authorization) {
        servicio.eliminar(id, claims(authorization));
    }

    /** Solo aprobadores llegan aquí: lo garantiza authorization_scopes en la ruta. */
    @PutMapping("/{id}/decision")
    public SolicitudVista decidir(@PathVariable long id, @Valid @RequestBody Decision decision) {
        Solicitud decidida = servicio.decidir(id, decision.estado(), decision.comentario());
        publicador.solicitudDecidida(decidida);
        return SolicitudVista.de(decidida);
    }
}