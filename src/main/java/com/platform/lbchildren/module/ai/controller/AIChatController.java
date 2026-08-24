package com.platform.lbchildren.module.ai.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.domain.dto.AIChatRequest;
import com.platform.lbchildren.domain.dto.AIChatResponse;
import com.platform.lbchildren.domain.entity.AIChatHistory;
import com.platform.lbchildren.module.ai.service.AIChatService;
import com.platform.lbchildren.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ai:ask')")
    public Result<AIChatResponse> ask(@AuthenticationPrincipal UserPrincipal user,
                                      @Valid @RequestBody AIChatRequest request) {
        return Result.ok(aiChatService.ask(user, request.getQuestion()));
    }

    /** 查看自己的提问历史 */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ai:history')")
    public Result<List<AIChatHistory>> getHistory(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(aiChatService.getHistory(user.getUserId()));
    }
}
