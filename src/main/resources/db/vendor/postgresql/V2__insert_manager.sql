CREATE EXTENSION IF NOT EXISTS pgcrypto SCHEMA main_parkshark;

INSERT INTO credentials(email, password_hash, role)
VALUES (
           'manager@parkshark.com',
           main_parkshark.crypt('${managerPassword}', main_parkshark.gen_salt('bf')),
           'MANAGER'
       );
