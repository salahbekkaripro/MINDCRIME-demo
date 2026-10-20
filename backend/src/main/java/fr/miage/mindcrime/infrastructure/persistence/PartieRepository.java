package fr.miage.mindcrime.infrastructure.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartieRepository extends JpaRepository<PartieEntity, String> {
    List<PartieEntity> findTop20ByOrderByCreatedAtDesc();
}
