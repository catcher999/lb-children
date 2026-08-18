package com.platform.lbchildren.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.dto.TreeholePostRequest;
import com.platform.lbchildren.dto.TreeholePostResponse;
import com.platform.lbchildren.dto.TreeholeReplyRequest;
import com.platform.lbchildren.dto.TreeholeReplyResponse;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.TreeholeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 树洞倾诉接口
 */
@RestController
@RequestMapping("/api/treehole")
@RequiredArgsConstructor
public class TreeholeController {

    private final TreeholeService treeholeService;

    /** 发布帖子 */
    @PostMapping("/posts")
    public Result<TreeholePostResponse> createPost(@AuthenticationPrincipal UserPrincipal user,
                                                   @Valid @RequestBody TreeholePostRequest request) {
        return Result.ok(treeholeService.createPost(user, request.getContent(), request.getImageUrl()));
    }

    /** 获取帖子列表（不含回复详情） */
    @GetMapping("/posts")
    public Result<List<TreeholePostResponse>> getAllPosts() {
        return Result.ok(treeholeService.getAllPosts());
    }

    /** 获取帖子详情（含回复列表） */
    @GetMapping("/posts/{postId}")
    public Result<TreeholePostResponse> getPostDetail(@PathVariable Long postId) {
        return Result.ok(treeholeService.getPostDetail(postId));
    }

    /** 回复帖子 */
    @PostMapping("/posts/{postId}/replies")
    public Result<TreeholeReplyResponse> createReply(@PathVariable Long postId,
                                                     @AuthenticationPrincipal UserPrincipal user,
                                                     @Valid @RequestBody TreeholeReplyRequest request) {
        return Result.ok(treeholeService.createReply(postId, user, request.getContent(), request.getImageUrl()));
    }

    /** 获取帖子的回复列表 */
    @GetMapping("/posts/{postId}/replies")
    public Result<List<TreeholeReplyResponse>> getReplies(@PathVariable Long postId) {
        return Result.ok(treeholeService.getReplies(postId));
    }
}
