package com.platform.lbchildren.module.parent.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.domain.dto.AddChildRequest;
import com.platform.lbchildren.domain.dto.ChildProfileVO;
import com.platform.lbchildren.domain.dto.ChildVO;
import com.platform.lbchildren.domain.dto.ProfileConsentRequest;
import com.platform.lbchildren.module.parent.service.ParentService;
import com.platform.lbchildren.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家长端接口：注册 / 登录 / 添加儿童 / 查看孩子
 */
@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @PostMapping("/add-child")
    @PreAuthorize("hasAuthority('parent:child:add')")
    public Result<String> addChild(@AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody AddChildRequest request) {
        parentService.addChild(principal.getUserId(), request);
        return Result.ok("儿童添加成功");
    }

    @GetMapping("/my-children")
    @PreAuthorize("hasAuthority('parent:child:view')")
    public Result<List<ChildVO>> getMyChildren(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.ok(parentService.getMyChildren(principal.getUserId()));
    }

    /** 设置是否授权查看孩子画像（阶段五） */
    @PostMapping("/child/{childId}/profile-consent")
    @PreAuthorize("hasAuthority('parent:child:consent')")
    public Result<String> setProfileConsent(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long childId,
                                            @Valid @RequestBody ProfileConsentRequest request) {
        parentService.setProfileConsent(principal.getUserId(), childId, request.getConsent());
        return Result.ok(request.getConsent() ? "已授权查看孩子画像" : "已撤销孩子画像授权");
    }

    /** 查看孩子画像摘要（需家长授权，阶段五） */
    @GetMapping("/child/{childId}/profile")
    @PreAuthorize("hasAuthority('parent:child:profile')")
    public Result<ChildProfileVO> getChildProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long childId) {
        return Result.ok(parentService.getChildProfile(principal.getUserId(), childId));
    }
}
