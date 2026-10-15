package com.example.meetingshedulingservice.bot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUser(long id, String first_name, String username) {
}
