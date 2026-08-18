package com.platform.lbchildren.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.entity.EduResource;
import com.platform.lbchildren.entity.LearningProgress;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 教育资源接口
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /** 资源列表（公开访问） */
    @GetMapping
    public Result<List<EduResource>> getResources(@RequestParam(required = false) String type) {
        if (type != null) {
            return Result.ok(resourceService.getResourcesByType(type));
        }
        return Result.ok(resourceService.getAllResources());
    }

    @GetMapping("/types")
    public Result<List<String>> getResourceTypes() {
        return Result.ok(resourceService.getAllResourceTypes());
    }

    /** 以下进度接口需要登录，userId 从 token 取，防止越权 */
    @PostMapping("/{resourceId}/progress")
    public Result<LearningProgress> updateProgress(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, Object> request) {
        Integer progressPercent = Integer.valueOf(request.get("progressPercent").toString());
        return Result.ok(resourceService.updateProgress(user.getUserId(), resourceId, progressPercent));
    }

    @GetMapping("/{resourceId}/progress")
    public Result<?> getProgress(@PathVariable Long resourceId,
                                 @AuthenticationPrincipal UserPrincipal user) {
        Optional<LearningProgress> progress = resourceService.getProgress(user.getUserId(), resourceId);
        return progress.<Result<?>>map(Result::ok)
                .orElseGet(() -> Result.ok(Map.of("message", "No progress found")));
    }

    @GetMapping("/progress")
    public Result<List<LearningProgress>> getUserProgress(@AuthenticationPrincipal UserPrincipal user) {
        return Result.ok(resourceService.getUserProgress(user.getUserId()));
    }

    @DeleteMapping("/{resourceId}/progress")
    public Result<String> deleteProgress(@PathVariable Long resourceId,
                                         @AuthenticationPrincipal UserPrincipal user) {
        resourceService.deleteProgress(user.getUserId(), resourceId);
        return Result.ok("进度删除成功");
    }
}