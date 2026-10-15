# Supabase schema

`migrations/20261012160000_create_scheduling_schema.sql` is the initial database
schema for the Telegram scheduling bot. It mirrors the current repository:

- `telegram_users` stores Telegram users globally;
- `telegram_chats` stores each group chat;
- `chat_participants` records who joined planning in a chat;
- `participant_availability` contains dates explicitly marked free;
- `game_sessions` contains at most one generated session per chat and date.

The migration enables Row Level Security without public policies. The Spring
backend should connect through a server-side PostgreSQL role; do not put a
Supabase service-role key in the Telegram bot or another client application.

## Applying the migration

After authenticating the Supabase CLI and linking this directory to your
Supabase project, run:

```powershell
supabase db push
```

For a one-off setup, open the SQL Editor in the Supabase dashboard, paste the
migration contents, and execute it. The CLI path is preferable because the
schema remains versioned in this repository.
