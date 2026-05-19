package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.EduResource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EduResourceRepository extends JpaRepository<EduResource, Long> {
    List<EduResource> findByType(String type);
}