ALTER TABLE assinaturas ADD COLUMN id_pet INT NULL;

UPDATE assinaturas a
JOIN (SELECT id_usuario, MIN(id_pet) AS id_pet FROM pet GROUP BY id_usuario) p ON p.id_usuario = a.id_usuario
SET a.id_pet = p.id_pet
WHERE a.id_pet IS NULL;

DELETE FROM assinaturas WHERE id_pet IS NULL;

ALTER TABLE assinaturas
    MODIFY id_pet INT NOT NULL,
    ADD CONSTRAINT uq_assinaturas_pet UNIQUE (id_pet),
    ADD CONSTRAINT fk_assinaturas_pet FOREIGN KEY (id_pet) REFERENCES pet (id_pet) ON DELETE CASCADE;
