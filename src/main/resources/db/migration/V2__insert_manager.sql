CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO credentials(email, password_hash, role)
VALUES (
           'manager@parkshark.com',
           crypt('${managerPassword}', gen_salt('bf')),
           'MANAGER'
       );