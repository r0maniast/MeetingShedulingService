package com.example.meetingshedulingservice.schedule;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** PostgreSQL implementation used when DATABASE_ENABLED=true. */
@Repository
@ConditionalOnProperty(prefix = "app.database", name = "enabled", havingValue = "true")
public class JdbcAvailabilityRepository implements AvailabilityRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAvailabilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void join(long chatId, Participant participant) {
        jdbcTemplate.update("insert into telegram_chats (telegram_chat_id) values (?) "
                + "on conflict (telegram_chat_id) do update set updated_at = now()", chatId);
        jdbcTemplate.update("insert into telegram_users (telegram_user_id, display_name) values (?, ?) "
                        + "on conflict (telegram_user_id) do update set display_name = excluded.display_name",
                participant.telegramUserId(), participant.displayName());
        jdbcTemplate.update("insert into chat_participants (telegram_chat_id, telegram_user_id) values (?, ?) "
                        + "on conflict do nothing",
                chatId, participant.telegramUserId());
    }

    @Override
    public void leave(long chatId, long telegramUserId) {
        jdbcTemplate.update("delete from chat_participants where telegram_chat_id = ? and telegram_user_id = ?",
                chatId, telegramUserId);
    }

    @Override
    public boolean isParticipant(long chatId, long telegramUserId) {
        Boolean result = jdbcTemplate.queryForObject(
                "select exists (select 1 from chat_participants where telegram_chat_id = ? and telegram_user_id = ?)",
                Boolean.class, chatId, telegramUserId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Collection<Participant> participants(long chatId) {
        return jdbcTemplate.query("select u.telegram_user_id, u.display_name "
                        + "from chat_participants cp join telegram_users u on u.telegram_user_id = cp.telegram_user_id "
                        + "where cp.telegram_chat_id = ? order by cp.joined_at",
                (resultSet, rowNum) -> new Participant(
                        resultSet.getLong("telegram_user_id"), resultSet.getString("display_name")), chatId);
    }

    @Override
    public void markFree(long chatId, long telegramUserId, LocalDate date) {
        jdbcTemplate.update("insert into participant_availability (telegram_chat_id, telegram_user_id, available_on) "
                        + "values (?, ?, ?) on conflict do nothing",
                chatId, telegramUserId, Date.valueOf(date));
    }

    @Override
    public void markBusy(long chatId, long telegramUserId, LocalDate date) {
        jdbcTemplate.update("delete from participant_availability "
                        + "where telegram_chat_id = ? and telegram_user_id = ? and available_on = ?",
                chatId, telegramUserId, Date.valueOf(date));
    }

    @Override
    public Set<LocalDate> freeDates(long chatId, long telegramUserId) {
        return Set.copyOf(jdbcTemplate.query("select available_on from participant_availability "
                        + "where telegram_chat_id = ? and telegram_user_id = ?",
                (resultSet, rowNum) -> resultSet.getObject("available_on", LocalDate.class), chatId, telegramUserId));
    }

    @Override
    public Optional<GameSession> saveSessionIfAbsent(GameSession session) {
        List<GameSession> sessions = jdbcTemplate.query("insert into game_sessions "
                        + "(telegram_chat_id, scheduled_for, title) values (?, ?, ?) "
                        + "on conflict (telegram_chat_id, scheduled_for) do nothing "
                        + "returning telegram_chat_id, scheduled_for, title",
                (resultSet, rowNum) -> new GameSession(
                        resultSet.getLong("telegram_chat_id"),
                        resultSet.getObject("scheduled_for", LocalDate.class),
                        resultSet.getString("title")),
                session.chatId(), Date.valueOf(session.date()), session.title());
        return sessions.stream().findFirst();
    }
}
