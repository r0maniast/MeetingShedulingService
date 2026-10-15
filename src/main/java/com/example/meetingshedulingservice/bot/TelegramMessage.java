package com.example.meetingshedulingservice.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessage(TelegramChat chat, TelegramUser from, String text) {
}
