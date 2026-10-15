package com.example.meetingshedulingservice.bot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramProperties(String token, String apiUrl) {
}
