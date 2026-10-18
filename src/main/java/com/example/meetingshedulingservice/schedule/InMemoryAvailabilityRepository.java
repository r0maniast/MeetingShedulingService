package com.example.meetingshedulingservice.schedule;

import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Temporary repository. All data disappears when the application restarts. */
@Repository
@ConditionalOnProperty(prefix = "app.database", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryAvailabilityRepository implements AvailabilityRepository {
    private final Map<Long, Map<Long, Participant>> participantsByChat = new HashMap<>();
    private final Map<Long, Map<Long, Set<LocalDate>>> datesByChat = new HashMap<>();
    private final Map<String, GameSession> sessions = new HashMap<>();

    @Override
    public synchronized void join(long chatId, Participant participant) {
        participantsByChat.computeIfAbsent(chatId, ignored -> new HashMap<>())
                .put(participant.telegramUserId(), participant);
    }

    @Override
    public synchronized void leave(long chatId, long telegramUserId) {
        Map<Long, Participant> participants = participantsByChat.get(chatId);
        if (participants != null) {
            participants.remove(telegramUserId);
        }
        Map<Long, Set<LocalDate>> dates = datesByChat.get(chatId);
        if (dates != null) {
            dates.remove(telegramUserId);
        }
    }

    @Override
    public synchronized boolean isParticipant(long chatId, long telegramUserId) {
        return participantsByChat.getOrDefault(chatId, Map.of()).containsKey(telegramUserId);
    }

    @Override
    public synchronized Collection<Participant> participants(long chatId) {
        return Set.copyOf(participantsByChat.getOrDefault(chatId, Map.of()).values());
    }

    @Override
    public synchronized void markFree(long chatId, long telegramUserId, LocalDate date) {
        datesByChat.computeIfAbsent(chatId, ignored -> new HashMap<>())
                .computeIfAbsent(telegramUserId, ignored -> new HashSet<>())
                .add(date);
    }

    @Override
    public synchronized void markBusy(long chatId, long telegramUserId, LocalDate date) {
        Map<Long, Set<LocalDate>> dates = datesByChat.get(chatId);
        if (dates != null && dates.get(telegramUserId) != null) {
            dates.get(telegramUserId).remove(date);
        }
        sessions.remove(chatId + ":" + date);
    }

    @Override
    public synchronized Set<LocalDate> freeDates(long chatId, long telegramUserId) {
        return Set.copyOf(datesByChat.getOrDefault(chatId, Map.of())
                .getOrDefault(telegramUserId, Set.of()));
    }

    @Override
    public synchronized Collection<GameSession> sessionsAfter(long chatId, LocalDate date) {
        return sessions.values().stream()
                .filter(session -> session.chatId() == chatId && session.date().isAfter(date))
                .sorted(java.util.Comparator.comparing(GameSession::date))
                .toList();
    }

    @Override
    public synchronized Optional<GameSession> saveSessionIfAbsent(GameSession session) {
        String key = session.chatId() + ":" + session.date();
        if (sessions.containsKey(key)) {
            return Optional.empty();
        }
        sessions.put(key, session);
        return Optional.of(session);
    }
}
