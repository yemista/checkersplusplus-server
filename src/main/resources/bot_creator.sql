CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
    bot_names TEXT[] := ARRAY['String1', 'String2', 'String3'];
	bot_name TEXT;
BEGIN
    FOREACH bot_name IN ARRAY bot_names LOOP
        INSERT INTO account (account_id, created, verified, email, password, username) VALUES (uuid_generate_v4(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CONCAT(bot_name, '@bot.com'), '12345678', bot_name);
        INSERT INTO bot (bot_id, bot_account_id, in_use) VALUES (uuid_generate_v4(), (SELECT account_id FROM account WHERE username = bot_name), false);
        INSERT INTO rating (rating_id, account_id, rating) VALUES (uuid_generate_v4(), (SELECT account_id FROM account WHERE username = bot_name), 800);
    END LOOP;
END $$;
