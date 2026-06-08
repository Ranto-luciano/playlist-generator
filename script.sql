-- Création de la base de données (A exécuter séparément en tant qu'administrateur postgres si elle n'existe pas)
-- CREATE DATABASE playlist_db;
-- \c playlist_db;

-- Table principale pour stocker les chansons
CREATE TABLE songs (
    id BIGSERIAL PRIMARY KEY,
    absolute_path VARCHAR(255) NOT NULL UNIQUE,
    song_name VARCHAR(255) NOT NULL
);

-- Table pour stocker les métadonnées (relation One-To-Many / ElementCollection)
CREATE TABLE song_metadata (
    song_id BIGINT NOT NULL,
    metadata_value VARCHAR(255),
    metadata_key VARCHAR(255) NOT NULL,
    PRIMARY KEY (song_id, metadata_key),
    CONSTRAINT fk_song_metadata_song_id FOREIGN KEY (song_id) REFERENCES songs(id) ON DELETE CASCADE
);
