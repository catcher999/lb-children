package com.platform.lbchildren.module.ai.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.domain.dto.AIChatRequest;
import com.platform.lbchildren.domain.dto.AIChatResponse;
import com.platform.lbchildren.domain.dto.AiSessionVO;
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

    /** 提问（B1：带 sessionId 续写，不带则自动新建会话） */
    @PostMapping("/ask")
    @PreAuthorize("hasAuthority('ai:ask')")
    public Result<AIChatResponse> ask(@AuthenticationPrincipal UserPrincipal user,
                                      @Valid @RequestBody AIChatRequest request) {
        return Result.ok(aiChatService.ask(user, request.getQuestion(), request.getSessionId()));
    }

    /** 查看自己的提问历史（历史遗留记录兜底座：session_id 为 NULL） */
    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ai:history')")
    public Result<List<AIChatHistory>> getHistory(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(aiChatService.getHistory(user.getUserId()));
    }

    /** 会话列表（B1） */
    @GetMapping("/sessions")
    @PreAuthorize("hasAuthority('ai:history')")
    public Result<List<AiSessionVO>> listSessions(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(aiChatService.listSessions(user));
    }

    /** 某会话内记录（B1，服务端已做属主校验） */
    @GetMapping("/sessions/{id}/messages")
    @PreAuthorize("hasAuthority('ai:history')")
    public Result<List<AIChatHistory>> getSessionMessages(@AuthenticationPrincipal UserPrincipal user,
                                                          @PathVariable("id") Long sessionId) {
        return Result.ok(aiChatService.getSessionMessages(user, sessionId));
    }
}