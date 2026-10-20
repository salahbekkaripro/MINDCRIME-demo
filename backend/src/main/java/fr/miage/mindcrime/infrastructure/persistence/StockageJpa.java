package fr.miage.mindcrime.infrastructure.persistence;
import fr.miage.mindcrime.application.port.StockageParties;
import fr.miage.mindcrime.moteur.modele.Partie;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import java.util.function.UnaryOperator;

@Repository
@Transactional
public class StockageJpa implements StockageParties {
    private final PartieRepository repository;
    private final JsonMapper mapper;
    public StockageJpa(PartieRepository repository, JsonMapper mapper) {
        this.repository = repository; this.mapper = mapper;
    }
    public Partie creer(Partie partie) {
        repository.save(new PartieEntity(partie.id(), mapper.writeValueAsString(partie))); return partie;
    }
    @Transactional(readOnly = true)
    public Optional<Partie> lire(String id) { return repository.findById(id).map(this::decoder); }
    public Partie modifier(String id, UnaryOperator<Partie> modification) {
        var entity = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Partie introuvable"));
        var updated = modification.apply(decoder(entity));
        entity.setPayload(mapper.writeValueAsString(updated));
        repository.saveAndFlush(entity);
        return updated;
    }
    @Transactional(readOnly = true)
    public List<Partie> recentes() { return repository.findTop20ByOrderByCreatedAtDesc().stream().map(this::decoder).toList(); }
    private Partie decoder(PartieEntity entity) { return mapper.readValue(entity.getPayload(), Partie.class); }
}
