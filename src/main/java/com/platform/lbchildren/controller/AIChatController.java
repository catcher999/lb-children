package com.platform.lbchildren.controller;

import com.platform.lbchildren.dto.AIChatRequest;
import com.platform.lbchildren.dto.AIChatResponse;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.AIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    @Autowired
    private AIChatService aiChatService;

    // 提问
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@AuthenticationPrincipal UserPrincipal user,
                                 @RequestBody AIChatRequest request) {
        try {
            AIChatResponse response = aiChatService.ask(user, request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 查看自己的提问历史
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal UserPrincipal user) {
        List<AIChatHistory> history = aiChatService.getHistory(user.getUserId());
        return ResponseEntity.ok(history);
    }
}