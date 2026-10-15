package com.example.meetingshedulingservice.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingServiceTest {

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
