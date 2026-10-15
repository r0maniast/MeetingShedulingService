package com.example.meetingshedulingservice.bot;

import com.example.meetingshedulingservice.schedule.SchedulingService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class BotUpdateService {
    private final SchedulingService schedulingService;

    public BotUpdateService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    public Optional<String> handle(TelegramUpdate update) {
        if (update == null || update.message() == null || update.message().chat() == null
                || update.message().from() == null || update.message().text() == null) {
            return Optional.empty();
        }
        TelegramMessage message = update.message();
        String[] parts = message.text().trim().split("\\s+", 2);
        String command = parts[0].replaceFirst("@\\w+$", "").toLowerCase();
        long chatId = message.chat().id();
        long userId = message.from().id();
        String displayName = displayName(message.from());
        return switch (command) {
            case "/start", "/help" -> Optional.of(help());
            case "/join" -> Optional.of(schedulingService.join(chatId, userId, displayName));
            case "/leave" -> Optional.of(schedulingService.leave(chatId, userId));
            case "/free" -> Optional.of(withDate(parts, date -> schedulingService.markFree(chatId, userId, date)));
            case "/busy" -> Optional.of(withDate(parts, date -> schedulingService.markBusy(chatId, userId, date)));
            case "/schedule" -> Optional.of(schedulingService.schedule(chatId));
            default -> Optional.empty();
        };
    }

    private String withDate(String[] parts, java.util.function.Function<LocalDate, String> action) {
        if (parts.length < 2) {
            return "Укажите дату в формате ГГГГ-ММ-ДД, например: /free 2026-10-12";
        }
        try {
            return action.apply(LocalDate.parse(parts[1]));
        } catch (DateTimeParseException exception) {
            return "Не удалось прочитать дату. Используйте формат ГГГГ-ММ-ДД.";
        }
    }

    private String displayName(TelegramUser user) {
        return user.username() == null || user.username().isBlank() ? user.first_name() : "@" + user.username();
    }

    private String help() {
        return "Команды:\n/join — участвовать в планировании\n/leave — выйти из планирования\n/free ГГГГ-ММ-ДД — свободен\n/busy ГГГГ-ММ-ДД — занят\n/schedule — проверить общую дату";
    }
}
