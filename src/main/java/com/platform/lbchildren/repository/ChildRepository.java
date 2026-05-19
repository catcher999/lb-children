package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByParentId(Long parentId);
    Child findByParentIdAndNickname(Long parentId, String nickname);
}