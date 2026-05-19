package com.platform.lbchildren.controller;

import com.platform.lbchildren.dto.TreeholePostResponse;
import com.platform.lbchildren.dto.TreeholeReplyResponse;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.TreeholeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/treehole")
public class TreeholeController {

    @Autowired
    private TreeholeService treeholeService;

    // 发布帖子
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@AuthenticationPrincipal UserPrincipal user,
                                        @RequestBody Map<String, String> body) {
        try {
            String content = body.get("content");
            String imageUrl = body.getOrDefault("imageUrl", null);
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "内容不能为空"));
            }
            TreeholePostResponse post = treeholeService.createPost(user, content, imageUrl);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 获取帖子列表（不含回复详情）
    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts() {
        try {
            List<TreeholePostResponse> posts = treeholeService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 获取帖子详情（含回复列表）
    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {
        try {
            TreeholePostResponse post = treeholeService.getPostDetail(postId);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 回复帖子
    @PostMapping("/posts/{postId}/replies")
    public ResponseEntity<?> createReply(@PathVariable Long postId,
                                         @AuthenticationPrincipal UserPrincipal user,
                                         @RequestBody Map<String, String> body) {
        try {
            String content = body.get("content");
            String imageUrl = body.getOrDefault("imageUrl", null);
            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "内容不能为空"));
            }
            TreeholeReplyResponse reply = treeholeService.createReply(postId, user, content, imageUrl);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 获取帖子的回复列表（独立接口，也可不提供，因为详情已包含）
    @GetMapping("/posts/{postId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long postId) {
        try {
            List<TreeholeReplyResponse> replies = treeholeService.getReplies(postId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
