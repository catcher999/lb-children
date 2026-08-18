package com.platform.lbchildren.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.dto.AddChildRequest;
import com.platform.lbchildren.dto.ChildProfileVO;
import com.platform.lbchildren.dto.ChildVO;
import com.platform.lbchildren.dto.LoginRequest;
import com.platform.lbchildren.dto.ProfileConsentRequest;
import com.platform.lbchildren.dto.RegisterRequest;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家长端接口：注册 / 登录 / 添加儿童 / 查看孩子
 */
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        parentService.register(request);
        return Result.ok("注册成功");
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = parentService.login(request.getUsername(), request.getPassword());
        return Result.ok(Map.of("token", token, "role", "PARENT"));
    }

    @PostMapping("/add-child")
    public Result<String> addChild(@AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody AddChildRequest request) {
        parentService.addChild(principal.getUserId(), request);
        return Result.ok("儿童添加成功");
    }

    @GetMapping("/my-children")
    public Result<List<ChildVO>> getMyChildren(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(parentService.getMyChildren(principal.getUserId()));
    }

    /** 设置是否授权查看孩子画像（阶段五） */
    @PostMapping("/child/{childId}/profile-consent")
    public Result<String> setProfileConsent(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long childId,
                                            @Valid @RequestBody ProfileConsentRequest request) {
        parentService.setProfileConsent(principal.getUserId(), childId, request.getConsent());
        return Result.ok(request.getConsent() ? "已授权查看孩子画像" : "已撤销孩子画像授权");
    }

    /** 查看孩子画像摘要（需家长授权，阶段五） */
    @GetMapping("/child/{childId}/profile")
    public Result<ChildProfileVO> getChildProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long childId) {
        return Result.ok(parentService.getChildProfile(principal.getUserId(), childId));
    }
}
