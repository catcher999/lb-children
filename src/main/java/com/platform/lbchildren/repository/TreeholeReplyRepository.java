package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.TreeholeReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TreeholeReplyRepository extends JpaRepository<TreeholeReply, Long> {
    List<TreeholeReply> findByPostIdOrderByCreatedAtAsc(Long postId);
}