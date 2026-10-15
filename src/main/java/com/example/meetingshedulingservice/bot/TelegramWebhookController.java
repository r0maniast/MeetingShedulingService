package com.example.meetingshedulingservice.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {
    private final BotUpdateService botUpdateService;
    private final TelegramApiClient telegramApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TelegramWebhookController(BotUpdateService botUpdateService, TelegramApiClient telegramApiClient) {
        this.botUpdateService = botUpdateService;
        this.telegramApiClient = telegramApiClient;
    }

    @PostMapping("/updates")
    public ResponseEntity<Void> receive(@RequestBody String payload) {
        try {
            TelegramUpdate update = objectMapper.readValue(payload, TelegramUpdate.class);
            botUpdateService.handle(update)
                    .ifPresent(reply -> telegramApiClient.sendMessage(update.message().chat().id(), reply));
            return ResponseEntity.ok().build();
        } catch (JsonProcessingException exception) {
            return ResponseEntity.badRequest().build();
        }
    }
}
