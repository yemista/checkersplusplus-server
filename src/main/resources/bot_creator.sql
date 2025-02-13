CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
DECLARE
    bot_names TEXT[] := ARRAY[
    		'megaman', 'G_CASH_MONEY', 'Champ', 'champion', '77yeetyeet', 'southclamp', 'shadow_king', 'protoman', 'noodleman', 'gutsman',
    		'cutman', 'akinono', 'marshmellow_fly', 'smellz', 'j_123', 'NE_6', 'num1', 'stenop', 'arrga', 'TB12',
    		'musashi', 'bobby_fisher', 'david', 'clamz', 'CArry', 'John', 'p__p', 'george', 'jerry', 'number10',
    		'xyz', 'pope', 'ALEX', 'marcus_arelius', 'David2', 'ccc', 'Lnedros', 'Caesar', 'gary', 'theon',
    		'James3rd', 'oneill', 'casino_vinoo', 'MattMac', 'the', 'optimus', 'tornado', 'crash', 'sonic', 'chillzee',
    		'roxy_relof', '46217', 'true_gummy', 'combo_panda', 'xxxxxxEEEEE', 'ryan', 'gus', 'mason', 'Marrio', 'bro' 
    ];
	bot_name TEXT;
	counter INTEGER := 0;
	bot_level INTEGER := 1;
	half_length INTEGER := array_length(bot_names, 1) / 2;
	three_quarters_length INTEGER := array_length(bot_names, 1) * 3 / 4;
BEGIN
    FOREACH bot_name IN ARRAY bot_names LOOP
    	counter := counter + 1;
    	
    	IF counter > half_length THEN
            bot_level := 2;
        END IF;
        
        IF counter > three_quarters_length THEN
            bot_level := 3;
        END IF;
        
        INSERT INTO account (account_id, created, verified, email, password, username, banned, bot, tutorial) VALUES (uuid_generate_v4(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CONCAT(bot_name, '@bot.com'), '12345678', bot_name, false, true, false);
        INSERT INTO bot (bot_id, bot_account_id, in_use, last_modified, level) VALUES (uuid_generate_v4(), (SELECT account_id FROM account WHERE username = bot_name), false, CURRENT_TIMESTAMP, bot_level);
        INSERT INTO rating (rating_id, account_id, rating) VALUES (uuid_generate_v4(), (SELECT account_id FROM account WHERE username = bot_name), 800);
    END LOOP;
END $$;
