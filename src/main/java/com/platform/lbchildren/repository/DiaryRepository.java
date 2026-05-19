package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByChildIdOrderByCreatedAtDesc(Long childId);
}