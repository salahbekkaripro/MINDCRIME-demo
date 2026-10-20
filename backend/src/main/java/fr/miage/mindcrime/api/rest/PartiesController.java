package fr.miage.mindcrime.api.rest;
import fr.miage.mindcrime.application.ServiceParties;
import fr.miage.mindcrime.infrastructure.configuration.NotificationsParties;
import fr.miage.mindcrime.moteur.modele.Partie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parties")
public class PartiesController {
    public record Creation(@NotNull Partie.Mode mode) { }
    public record Temoignage(@NotBlank String lieuId, @Min(1140) @Max(1200) int minute) { }
    public record Accusation(@NotBlank String suspectId) { }
    private final ServiceParties service;
    private final NotificationsParties notifications;
    public PartiesController(ServiceParties service, NotificationsParties notifications) {
        this.service = service; this.notifications = notifications;
    }
    private VuePartie vue(Partie partie) { return VuePartie.depuis(partie, service.analyser(partie)); }
    private VuePartie modifiee(Partie partie) { notifications.notifier(partie.id()); return vue(partie); }
    @PostMapping public ResponseEntity<VuePartie> creer(@Valid @RequestBody Creation creation) {
        var partie = service.creer(creation.mode());
        return ResponseEntity.created(URI.create("/api/v1/parties/" + partie.id())).body(vue(partie));
    }
    @GetMapping public List<VuePartie> recentes() { return service.recentes().stream().map(this::vue).toList(); }
    @GetMapping("/{id}") public VuePartie lire(@PathVariable String id) { return vue(service.lire(id)); }
    @PostMapping("/{id}/interrogatoires/{suspectId}") public VuePartie interroger(@PathVariable String id, @PathVariable String suspectId) {
        return modifiee(service.interroger(id, suspectId));
    }
    @PutMapping("/{id}/temoignages/{suspectId}") public VuePartie temoigner(@PathVariable String id,
            @PathVariable String suspectId, @Valid @RequestBody Temoignage temoignage) {
        return modifiee(service.temoigner(id, suspectId, new Partie.Presence(temoignage.lieuId(), temoignage.minute())));
    }
    @PostMapping("/{id}/accusations") public VuePartie accuser(@PathVariable String id, @Valid @RequestBody Accusation accusation) {
        return modifiee(service.accuser(id, accusation.suspectId()));
    }
    @PostMapping("/{id}/conclusion") public VuePartie conclure(@PathVariable String id) { return modifiee(service.conclure(id)); }
}
