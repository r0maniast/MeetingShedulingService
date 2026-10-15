package com.example.meetingshedulingservice.schedule;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface AvailabilityRepository {
    void join(long chatId, Participant participant);

    void leave(long chatId, long telegramUserId);

    boolean isParticipant(long chatId, long telegramUserId);

    Collection<Participant> participants(long chatId);

    void markFree(long chatId, long telegramUserId, LocalDate date);

    void markBusy(long chatId, long telegramUserId, LocalDate date);

    Set<LocalDate> freeDates(long chatId, long telegramUserId);

    Optional<GameSession> saveSessionIfAbsent(GameSession session);
}
