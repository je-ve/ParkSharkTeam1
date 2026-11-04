ALTER TABLE division
ADD COLUMN parent_id UUID REFERENCES division (id);