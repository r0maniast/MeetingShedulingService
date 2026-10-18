package com.example.meetingshedulingservice.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingServiceTest {

    @Test
    void busyRemovesSessionAndAllowsRecreationWithoutAffectingOtherDatesOrChats() {
        InMemoryAvailabilityRepository repository = new InMemoryAvailabilityRepository();
        SchedulingService service = new SchedulingService(repository);
        LocalDate date = LocalDate.now().plusDays(2);
        service.join(100L, 1L, "Alice");
        service.join(100L, 2L, "Bob");
        service.markFree(100L, 1L, date);
        service.markFree(100L, 2L, date);
        GameSession otherDate = new GameSession(100L, date.plusDays(1), "Other date");
        GameSession otherChat = new GameSession(200L, date, "Other chat");
        repository.saveSessionIfAbsent(otherDate);
        repository.saveSessionIfAbsent(otherChat);

        service.markBusy(100L, 2L, date);
        service.markBusy(100L, 2L, date);

        assertThat(repository.freeDates(100L, 2L)).doesNotContain(date);
        assertThat(repository.freeDates(100L, 1L)).contains(date);
        assertThat(repository.sessionsAfter(100L, date.minusDays(1))).containsExactly(otherDate);
        assertThat(repository.sessionsAfter(200L, date.minusDays(1))).containsExactly(otherChat);
        assertThat(service.markFree(100L, 2L, date)).contains("Все свободны");
    }

    @Test
    void busyFromNonParticipantDoesNotRemoveSession() {
        InMemoryAvailabilityRepository repository = new InMemoryAvailabilityRepository();
        SchedulingService service = new SchedulingService(repository);
        GameSession session = new GameSession(100L, LocalDate.now().plusDays(2), "Game");
        repository.saveSessionIfAbsent(session);

        assertThat(service.markBusy(100L, 99L, session.date())).contains("Сначала присоединитесь");
        assertThat(repository.sessionsAfter(100L, session.date().minusDays(1))).containsExactly(session);
    }

    @Test
    void listsOnlyFutureSessionsOfCurrentChatInDateOrder() {
        InMemoryAvailabilityRepository repository = new InMemoryAvailabilityRepository();
        SchedulingService service = new SchedulingService(repository);
        LocalDate today = LocalDate.now();
        repository.saveSessionIfAbsent(new GameSession(100L, today.minusDays(1), "Past"));
        repository.saveSessionIfAbsent(new GameSession(100L, today, "Today"));
        repository.saveSessionIfAbsent(new GameSession(100L, today.plusDays(3), "Later"));
        repository.saveSessionIfAbsent(new GameSession(100L, today.plusDays(1), "Soon"));
        repository.saveSessionIfAbsent(new GameSession(200L, today.plusDays(2), "Other chat"));

        assertThat(service.sessions(100L)).isEqualTo("Запланированные игровые сессии:\n"
                + today.plusDays(1) + " — Soon\n" + today.plusDays(3) + " — Later");
    }

    @Test
    void reportsNoFutureSessions() {
        assertThat(new SchedulingService(new InMemoryAvailabilityRepository()).sessions(100L))
                .isEqualTo("Будущих игровых сессий пока нет.");
    }

    @Test
    void createsOneSessionWhenEveryParticipantIsFree() {
        SchedulingService service = new SchedulingService(new InMemoryAvailabilityRepository());
        LocalDate date = LocalDate.of(2026, 10, 12);

        service.join(100L, 1L, "Alice");
        service.join(100L, 2L, "Bob");

        assertThat(service.markFree(100L, 1L, date)).contains("отмечена как свободная");
        assertThat(service.markFree(100L, 2L, date)).contains("Все свободны");
        assertThat(service.schedule(100L)).contains("уже создано");
    }
}
