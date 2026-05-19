package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByChildIdOrderByCreatedAtDesc(Long childId);
}