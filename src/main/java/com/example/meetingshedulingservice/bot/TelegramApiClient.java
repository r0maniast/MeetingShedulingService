package com.example.meetingshedulingservice.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Component
public class TelegramApiClient {
    private static final Logger log = LoggerFactory.getLogger(TelegramApiClient.class);
    private final TelegramProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public TelegramApiClient(TelegramProperties properties) {
        this.properties = properties;
    }

    public void sendMessage(long chatId, String text) {
        if (properties.token() == null || properties.token().isBlank()) {
            log.warn("Telegram token is not set; response for chat {} was not sent", chatId);
            return;
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of("chat_id", chatId, "text", text));
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.apiUrl() + "/bot" + properties.token() + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                log.warn("Telegram sendMessage returned {}: {}", response.statusCode(), response.body());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Could not send a Telegram message", exception);
        } catch (IOException exception) {
            log.error("Could not send a Telegram message", exception);
        }
    }
}
