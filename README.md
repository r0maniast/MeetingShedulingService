# Meeting Scheduling Telegram Bot

Spring Boot service for a group chat. Participants use `/join`, then mark dates with
`/free 2026-10-12`. When every enrolled participant is free on the same date, the
service creates a `Игровая сессия` and sends a confirmation to the chat. It stores
the state in Supabase when database mode is enabled.

## Run locally

Set the bot token without placing it in source control, then run the app:

```powershell
$env:TELEGRAM_BOT_TOKEN = "<token from BotFather>"
.\mvnw.cmd spring-boot:run
```

The webhook endpoint is `POST /api/telegram/updates`. Configure Telegram to send
updates to `https://<your-public-domain>/api/telegram/updates`; a tunnel is enough
for local testing.

## Deploy to Koyeb

The included `Dockerfile` builds the application with Java 17 and uses Koyeb's
`PORT` environment variable. Create a **Web Service** from this repository and
add `DATABASE_ENABLED`, `SUPABASE_DB_URL`, `SUPABASE_DB_USERNAME`,
`SUPABASE_DB_PASSWORD`, and `TELEGRAM_BOT_TOKEN` as environment variables.
After deployment, set Telegram's webhook to
`https://<koyeb-service-domain>/api/telegram/updates`.

## Supabase database

By default the service uses in-memory storage, which is useful for local command
testing but loses data on restart. To use the Supabase schema from `supabase/migrations`,
enable PostgreSQL storage with environment variables:

```powershell
$env:DATABASE_ENABLED = "true"
$env:SUPABASE_DB_URL = "jdbc:postgresql://db.<project-ref>.supabase.co:5432/postgres?sslmode=require"
$env:SUPABASE_DB_USERNAME = "postgres"
$env:SUPABASE_DB_PASSWORD = "<database password>"
```

Get the exact connection string and database password in Supabase: **Connect** →
**Direct connection**. Do not use the anon key or the service-role key here, and
do not commit the password. With `DATABASE_ENABLED=true`, the application selects
`JdbcAvailabilityRepository`; otherwise it uses in-memory storage.
