package com.example.meetingshedulingservice.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingServiceTest {

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
