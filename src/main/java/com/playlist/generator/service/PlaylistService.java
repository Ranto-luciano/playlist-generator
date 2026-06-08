package com.playlist.generator.service;

import com.playlist.generator.model.SongInfo;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Programme 1 : liste toutes les chansons dans un dossier donne, en ignorant celles deja dans la cible
    public List<SongInfo> listSongsInDirectory(String directoryPath, String targetDirectory) {
        List<SongInfo> songs = new ArrayList<>();
        File folder = new File(directoryPath);
        findSongs(folder, songs, targetDirectory);
        return songs;
    }

    private void findSongs(File folder, List<SongInfo> songs, String targetDirectory) {
        if (folder != null && folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        findSongs(file, songs, targetDirectory); // Cherche dans les sous-dossiers
                    } else if (file.isFile()) {
                        String fileName = file.getName().toLowerCase();
                        if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".flac")) {
                            File targetFile = new File(targetDirectory, file.getName());
                            if (targetFile.exists()) {
                                // Doublon detecte : on l'ignore (on ne le traite plus)
                                // Optionnellement, on pourrait faire: file.delete(); pour nettoyer la source
                                System.out.println("Doublon ignore (existe deja dans la cible) : " + file.getName());
                            } else {
                                SongInfo songInfo = new SongInfo(file.getAbsolutePath(), file.getName());
                                songs.add(songInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    // Programme 2 : extrait les metadata de chaque chanson
    public List<SongInfo> extractMetadata(List<SongInfo> songs) {
        for (SongInfo song : songs) {
            Map<String, String> metadata = new HashMap<>();
            try {
                File file = new File(song.getAbsolutePath());
                AudioFile audioFile = AudioFileIO.read(file);
                Tag tag = audioFile.getTag();

                if (tag != null) {
                    // Recuperer toutes les metadonnees textuelles disponibles
                    for (FieldKey key : FieldKey.values()) {
                        try {
                            String value = tag.getFirst(key);
                            if (value != null && !value.trim().isEmpty()) {
                                metadata.put(key.name(), value);
                            }
                        } catch (Exception e) {
                            // Ignorer si la cle n'est pas supportee par ce type de fichier
                        }
                    }
                }
                
                // Recuperer les informations techniques (duree, bitrate, etc.)
                org.jaudiotagger.audio.AudioHeader header = audioFile.getAudioHeader();
                if (header != null) {
                    metadata.put("Duration (seconds)", String.valueOf(header.getTrackLength()));
                    metadata.put("BitRate", header.getBitRate());
                    metadata.put("SampleRate", header.getSampleRate());
                    metadata.put("Format", header.getFormat());
                }
            } catch (Exception e) {
                metadata.put("Error", "Impossible de lire les metadata: " + e.getMessage());
            }
            song.setMetadata(metadata);
        }
        return songs;
    }

    // Programme 3 : prend en argument le chemin absolu initial, chemin absolu cible, les chansons et metadonnees, et genère le log
    public void processAndMoveSongs(String initialDirectory, String targetDirectory, List<SongInfo> songsWithMetadata, String logFilePath) {
        File targetDirFile = new File(targetDirectory);
        if (!targetDirFile.exists()) {
            targetDirFile.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            logInfo(writer, "DEBUT DE LA GENERATION DE LA PLAYLIST ET DU DEPLACEMENT");
            logInfo(writer, "Chemin absolu initial : " + new File(initialDirectory).getAbsolutePath());
            logInfo(writer, "Chemin absolu cible : " + targetDirFile.getAbsolutePath());
            logInfo(writer, "Nombre total de chansons a traiter : " + songsWithMetadata.size());

            for (SongInfo song : songsWithMetadata) {
                logInfo(writer, "Traitement de la chanson : " + song.getSongName());
                logInfo(writer, " - Chemin absolu initial : " + song.getAbsolutePath());
                
                // Deplacement du fichier (copie vers cible puis effacement de l'original)
                File sourceFile = new File(song.getAbsolutePath());
                File targetFile = new File(targetDirFile, song.getSongName());
                
                try {
                    java.nio.file.Files.move(sourceFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    logInfo(writer, " - Fichier deplace avec succès vers : " + targetFile.getAbsolutePath());
                    
                    // Mise a jour du chemin dans l'objet pour la sauvegarde en base de donnees
                    song.setAbsolutePath(targetFile.getAbsolutePath());
                } catch (IOException e) {
                    logInfo(writer, " - ERREUR lors du deplacement du fichier : " + e.getMessage());
                }

                logInfo(writer, " - Metadonnees extraites : ");
                if (song.getMetadata() != null && !song.getMetadata().isEmpty()) {
                    for (Map.Entry<String, String> entry : song.getMetadata().entrySet()) {
                        String valeur = entry.getValue();
                        if (valeur == null || valeur.isEmpty()) {
                            valeur = "Inconnu";
                        }
                        logInfo(writer, "    * " + entry.getKey() + " : " + valeur);
                    }
                } else {
                    logInfo(writer, "    * Aucune metadonnee trouvee.");
                }
            }

            logInfo(writer, "FIN DE LA GENERATION DE LA PLAYLIST\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logInfo(PrintWriter writer, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] INFO: %s", timestamp, message);
        writer.println(logEntry);
        System.out.println(logEntry);
    }
}
