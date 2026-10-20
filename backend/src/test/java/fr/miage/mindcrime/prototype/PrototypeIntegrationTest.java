package fr.miage.mindcrime.prototype;

import fr.miage.mindcrime.MindcrimeApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import tools.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Vrai serveur HTTP, migrations Flyway et JPA sur H2 ; pas de contrôleur doublé. */
class PrototypeIntegrationTest {
    private final JsonMapper json = JsonMapper.builder().build();
    private final HttpClient http = HttpClient.newHttpClient();
    private String base;
    private HttpResponse<String> appel(String path, String method, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(base + path)).header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build(), HttpResponse.BodyHandlers.ofString());
    }
    @SuppressWarnings("unchecked") private Map<String,Object> objet(HttpResponse<String> response) {
        return json.readValue(response.body(), Map.class);
    }
    @SuppressWarnings("unchecked") private Map<String,Object> analyse(Map<String,Object> partie) {
        return (Map<String,Object>) partie.get("analyse");
    }
    private ServletWebServerApplicationContext demarrer(String database) {
        return (ServletWebServerApplicationContext) SpringApplication.run(MindcrimeApplication.class,
                "--server.port=0", "--server.address=127.0.0.1", "--spring.datasource.url="+database,
                "--spring.datasource.username=sa", "--spring.datasource.password=", "--spring.jpa.hibernate.ddl-auto=validate");
    }
    @Test void deuxModesPersistanceErreursEtAbsenceDeSpoilers() throws Exception {
        String db="jdbc:h2:mem:prototype_"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1";
        String id;
        try(var context=demarrer(db)) {
            base="http://127.0.0.1:"+context.getWebServer().getPort()+"/api/v1/parties";
            var creation=appel("","POST","{\"mode\":\"ENQUETEUR\"}");
            assertEquals(201,creation.statusCode()); assertNotNull(creation.headers().firstValue("location").orElse(null));
            var partie=objet(creation);id=(String)partie.get("id");
            assertFalse(creation.body().contains("coupableId"));assertFalse(creation.body().contains("observations"));
            assertEquals(4,((List<?>)analyse(partie).get("possibles")).size());
            assertEquals(400,appel("","POST","{}").statusCode());
            assertEquals(404,appel("/inconnu","GET",null).statusCode());
            var premature=objet(appel("/"+id+"/accusations","POST","{\"suspectId\":\"s1\"}"));
            assertEquals("EN_COURS",premature.get("etat"));
            for(int i=1;i<=4;i++) {
                var response=appel("/"+id+"/interrogatoires/s"+i,"POST",null);
                assertEquals(200,response.statusCode());partie=objet(response);
            }
            var possibles=(List<?>)analyse(partie).get("possibles");assertEquals(1,possibles.size());
            var solution=objet(appel("/"+id+"/accusations","POST",json.writeValueAsString(Map.of("suspectId",possibles.getFirst()))));
            assertEquals("RESOLUE",solution.get("etat"));
            assertEquals(409,appel("/"+id+"/interrogatoires/s1","POST",null).statusCode());
            var alibi=objet(appel("","POST","{\"mode\":\"ALIBI\"}"));
            String autre=(String)alibi.get("id");
            assertEquals(409,appel("/"+autre+"/conclusion","POST",null).statusCode());
            assertEquals(409,appel("/"+autre+"/interrogatoires/s1","POST",null).statusCode());
            assertEquals(400,appel("/"+autre+"/temoignages/s1","PUT","{\"lieuId\":\"hall\",\"minute\":1300}").statusCode());
            for(int i=1;i<=4;i++) {
                var reponse=appel("/"+autre+"/temoignages/s"+i,"PUT",json.writeValueAsString(Map.of("lieuId",alibi.get("lieuCrime"),"minute",1190)));
                assertEquals(200,reponse.statusCode());
            }
            var conclusion=objet(appel("/"+autre+"/conclusion","POST",null));
            assertEquals("TERMINEE",conclusion.get("etat"));
            assertEquals(4,((List<?>)analyse(conclusion).get("possibles")).size());
            assertEquals(Boolean.TRUE,analyse(conclusion).get("conditionnelle"));
            assertFalse(appel("","GET",null).body().contains("coupableId"));
        }
        // Un second contexte relit réellement les données via Flyway/JPA.
        try(var context=demarrer(db)) {
            base="http://127.0.0.1:"+context.getWebServer().getPort()+"/api/v1/parties";
            assertEquals("RESOLUE",objet(appel("/"+id,"GET",null)).get("etat"));
        }
    }
}
