CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
    bot_names TEXT[] := ARRAY[
    		'megaman', 'G_CASH_MONEY', 'Champ', 'champion', '77yeetyeet', 'southclamp', 'shadow_king', 'protoman', 'noodleman', 'gutsman',
    		'cutman', 'akinono', 'marshmellow_fly', 'smellz', 'j_123', 'NE_6', 'num1', 'stenop', 'arrga', 'TB12',
    		'musashi', 'bobby_fisher', 'david', 'clamz', 'CArry', 'John', 'p__p', 'george', 'jerry', 'number10',
    		'xyz', 'pope', 'ALEX', 'marcus_arelius', 'David2', 'ccc', 'Lnedros', 'Caesar', 'gary', 'theon',
    		'James3rd', 'oneill', 'casino_vinoo', 'MattMac', 'the', 'optimus', 'tornado', 'crash', 'sonic', 'chillzee' 
    ];
	bot_name TEXT;
BEGIN
    FOREACH bot_name IN ARRAY bot_names LOOP
        INSERT INTO account (account_id, created, verified, email, password, username, banned) VALUES (uuid_generate_v4(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CONCAT(bot_name, '@bot.com'), '12345678', bot_name, false);
        INSERT INTO bot (bot_id, bot_account_id, in_use, last_modified) VALUES (uuid_generate_v4(), (SELECT account_id FROM account WHERE username = bot_name), false, CURRENT_TIMESTAMP);
        INSERT INTO rating (rating_id, account_id, rating) VALUES (uuid_generate_v4(), (SELECT account_id FROM account WHERE username = bot_name), 800);
    END LOOP;
END $$;
