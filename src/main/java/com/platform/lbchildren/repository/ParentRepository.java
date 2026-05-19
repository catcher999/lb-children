package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByUsername(String username);
    Boolean existsByUsername(String username);
}