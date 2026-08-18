package com.platform.lbchildren.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.dto.AIChatRequest;
import com.platform.lbchildren.dto.AIChatResponse;
import com.platform.lbchildren.entity.AIChatHistory;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.AIChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 聊天接口
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    /** 提问 */
    @PostMapping("/ask")
    public Result<AIChatResponse> ask(@AuthenticationPrincipal UserPrincipal user,
                                      @Valid @RequestBody AIChatRequest request) {
        return Result.ok(aiChatService.ask(user, request.getQuestion()));
    }

    /** 查看自己的提问历史 */
    @GetMapping("/history")
    public Result<List<AIChatHistory>> getHistory(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(aiChatService.getHistory(user.getUserId()));
    }
}
