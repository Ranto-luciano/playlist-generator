package com.playlist.generator;

import com.playlist.generator.model.SongInfo;
import com.playlist.generator.service.PlaylistService;
import com.playlist.generator.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlaylistRunner {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private SongRepository songRepository;

    // Vous pouvez changer ce chemin dans application.properties
    @Value("${playlist.directory:./songs}")
    private String songsDirectory;

    @Value("${playlist.target.directory:./target_songs}")
    private String targetDirectory;

    @Value("${playlist.log.file:./playlist.log}")
    private String logFilePath;

    // S'execute au lancement puis toutes les 5 minutes (300000 millisecondes)
    @Scheduled(fixedDelayString = "${playlist.schedule.delay:300000}")
    public void run() {
        try {
            System.out.println("--- Demarrage du generateur de playlist ---");
            
            // Programme 1
            System.out.println("Execution du Programme 1 : Recuperation des chansons...");
            List<SongInfo> songs = playlistService.listSongsInDirectory(songsDirectory, targetDirectory);
            System.out.println(songs.size() + " nouvelle(s) chanson(s) trouvee(s).");

            // Programme 2
            System.out.println("Execution du Programme 2 : Extraction des metadonnees...");
            List<SongInfo> songsWithMetadata = playlistService.extractMetadata(songs);
            System.out.println("Metadonnees extraites.");

            // Programme 3
            System.out.println("Execution du Programme 3 : Deplacement des chansons et generation du journal...");
            playlistService.processAndMoveSongs(songsDirectory, targetDirectory, songsWithMetadata, logFilePath);
            System.out.println("Chansons deplacees et journal genere avec succes dans : " + logFilePath);

            // Sauvegarde dans la base de donnees
            System.out.println("Sauvegarde des chansons dans la base de donnees PostgreSQL...");
            for (SongInfo song : songsWithMetadata) {
                SongInfo existing = songRepository.findByAbsolutePath(song.getAbsolutePath());
                if (existing != null) {
                    song.setId(existing.getId()); // Update if exists
                }
                songRepository.save(song);
            }
            System.out.println("Sauvegarde terminee.");

            System.out.println("--- Fin de l'execution ---");
        } catch (Exception e) {
            System.err.println("Une erreur s'est produite lors de la generation de la playlist : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
