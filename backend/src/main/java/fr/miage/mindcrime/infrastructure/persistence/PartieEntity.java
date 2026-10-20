package fr.miage.mindcrime.infrastructure.persistence;
import jakarta.persistence.*;

@Entity
@Table(name = "parties_demo")
public class PartieEntity {
    @Id private String id;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(name = "created_at", nullable = false) private long createdAt;
    @Version private Long version;
    protected PartieEntity() { }
    public PartieEntity(String id, String payload) {
        this.id = id; this.payload = payload; this.createdAt = System.currentTimeMillis();
    }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
