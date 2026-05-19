package com.platform.lbchildren.repository;

import com.platform.lbchildren.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
            Long sender1, Long receiver1, Long sender2, Long receiver2);
}