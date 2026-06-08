package com.playlist.generator.repository;

import com.playlist.generator.model.SongInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<SongInfo, Long> {
    SongInfo findByAbsolutePath(String absolutePath);
}
