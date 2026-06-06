package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    Optional<LearningProgress> findByUserIdAndResourceId(Long userId, Long resourceId);
    List<LearningProgress> findByUserId(Long userId);
    void deleteByUserIdAndResourceId(Long userId, Long resourceId);
}
