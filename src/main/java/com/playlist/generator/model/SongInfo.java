package com.playlist.generator.model;

import jakarta.persistence.*;
import java.util.Map;

@Entity
@Table(name = "songs")
public class SongInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String absolutePath;

    @Column(nullable = false)
    private String songName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "song_metadata", joinColumns = @JoinColumn(name = "song_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata;

    // Constructeur sans argument nécessaire pour JPA
    public SongInfo() {
    }

    public SongInfo(String absolutePath, String songName) {
        this.absolutePath = absolutePath;
        this.songName = songName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "id=" + id +
                ", songName='" + songName + '\'' +
                ", absolutePath='" + absolutePath + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
