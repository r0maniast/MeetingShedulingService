package com.example.meetingshedulingservice.schedule;

import java.time.LocalDate;

public record GameSession(long chatId, LocalDate date, String title) {
}
