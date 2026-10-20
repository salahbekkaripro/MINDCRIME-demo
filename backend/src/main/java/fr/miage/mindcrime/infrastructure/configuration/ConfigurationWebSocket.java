package fr.miage.mindcrime.infrastructure.configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class ConfigurationWebSocket implements WebSocketConfigurer {
    private final NotificationsParties notifications;
    public ConfigurationWebSocket(NotificationsParties notifications) { this.notifications = notifications; }
    @Override public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notifications, "/ws/parties/*")
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
    }
}
