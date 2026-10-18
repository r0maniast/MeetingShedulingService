package com.example.meetingshedulingservice.schedule;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Service
public class SchedulingService {
    private static final String SESSION_TITLE = "Игровая сессия";
    private final AvailabilityRepository repository;

    public SchedulingService(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public synchronized String join(long chatId, long userId, String displayName) {
        repository.join(chatId, new Participant(userId, displayName));
        return displayName + ", вы участвуете в планировании. Укажите дату: /free ГГГГ-ММ-ДД";
    }

    public synchronized String leave(long chatId, long userId) {
        repository.leave(chatId, userId);
        return "Вы исключены из планирования в этом чате.";
    }

    public synchronized String markFree(long chatId, long userId, LocalDate date) {
        if (!repository.isParticipant(chatId, userId)) {
            return "Сначала присоединитесь: /join";
        }
        repository.markFree(chatId, userId, date);
        Optional<GameSession> session = createSessionIfEveryoneIsFree(chatId, date);
        return session.map(value -> "Все свободны! Создано событие «" + value.title() + "» на " + value.date() + ".")
                .orElse("Дата " + date + " отмечена как свободная.");
    }

    public synchronized String markBusy(long chatId, long userId, LocalDate date) {
        if (!repository.isParticipant(chatId, userId)) {
            return "Сначала присоединитесь: /join";
        }
        repository.markBusy(chatId, userId, date);
        return "Дата " + date + " больше не отмечена как свободная. Игровая сессия на эту дату удалена, если была запланирована.";
    }

    public synchronized String schedule(long chatId) {
        Collection<Participant> participants = repository.participants(chatId);
        if (participants.isEmpty()) {
            return "Пока никто не участвует. Напишите /join.";
        }
        Set<LocalDate> commonDates = null;
        for (Participant participant : participants) {
            Set<LocalDate> dates = repository.freeDates(chatId, participant.telegramUserId());
            commonDates = commonDates == null ? dates : intersection(commonDates, dates);
        }
        if (commonDates == null || commonDates.isEmpty()) {
            return "Общих свободных дат пока нет.";
        }
        Set<LocalDate> availableToEveryone = commonDates;
        LocalDate nearest = availableToEveryone.stream().filter(date -> !date.isBefore(LocalDate.now())).min(LocalDate::compareTo)
                .orElseGet(() -> availableToEveryone.stream().min(LocalDate::compareTo).orElseThrow());
        Optional<GameSession> session = createSessionIfEveryoneIsFree(chatId, nearest);
        return session.map(value -> "Создано событие «" + value.title() + "» на " + value.date() + ".")
                .orElse("Ближайшая общая свободная дата: " + nearest + ". Событие уже создано.");
    }

    public synchronized String sessions(long chatId) {
        Collection<GameSession> sessions = repository.sessionsAfter(chatId, LocalDate.now());
        if (sessions.isEmpty()) {
            return "Будущих игровых сессий пока нет.";
        }
        return "Запланированные игровые сессии:\n" + sessions.stream()
                .map(session -> session.date() + " — " + session.title())
                .collect(java.util.stream.Collectors.joining("\n"));
    }

    private Optional<GameSession> createSessionIfEveryoneIsFree(long chatId, LocalDate date) {
        Collection<Participant> participants = repository.participants(chatId);
        if (participants.isEmpty() || participants.stream()
                .anyMatch(participant -> !repository.freeDates(chatId, participant.telegramUserId()).contains(date))) {
            return Optional.empty();
        }
        return repository.saveSessionIfAbsent(new GameSession(chatId, date, SESSION_TITLE));
    }

    private Set<LocalDate> intersection(Set<LocalDate> first, Set<LocalDate> second) {
        Set<LocalDate> result = new java.util.HashSet<>(first);
        result.retainAll(second);
        return result;
    }
}
