package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.TreeholePost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TreeholePostRepository extends JpaRepository<TreeholePost, Long> {
    List<TreeholePost> findAllByOrderByCreatedAtDesc();
}