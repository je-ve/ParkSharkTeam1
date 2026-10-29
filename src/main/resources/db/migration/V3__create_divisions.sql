CREATE TABLE divisions
(
    id UUID DEFAULT GEN_RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    original_name VARCHAR(50) NOT NULL,
    director VARCHAR(100) NOT NULL,
    parent_division_id UUID
)