package fr.miage.mindcrime.infrastructure.configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Canal local de notification : aucune donnée métier ou solution dans les messages. */
@Component
public class NotificationsParties extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private String id(WebSocketSession session) {
        String path = session.getUri().getPath(); return path.substring(path.lastIndexOf('/') + 1);
    }
    @Override public void afterConnectionEstablished(WebSocketSession session) {
        sessions.computeIfAbsent(id(session), ignored -> ConcurrentHashMap.newKeySet()).add(session);
    }
    @Override public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var groupe = sessions.get(id(session));
        if (groupe != null) { groupe.remove(session); if (groupe.isEmpty()) { sessions.remove(id(session), groupe); } }
    }
    public void notifier(String partieId) {
        for (var session : sessions.getOrDefault(partieId, Set.of())) {
            try { synchronized (session) { if (session.isOpen()) { session.sendMessage(new TextMessage("refresh")); } } }
            catch (Exception ignored) {
                var groupe = sessions.get(partieId);
                if (groupe != null) { groupe.remove(session); }
            }
        }
    }
}
