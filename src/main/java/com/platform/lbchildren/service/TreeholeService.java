package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.TreeholePostResponse;
import com.platform.lbchildren.dto.TreeholeReplyResponse;
import com.platform.lbchildren.entity.TreeholePost;
import com.platform.lbchildren.entity.TreeholeReply;
import com.platform.lbchildren.repository.TreeholePostRepository;
import com.platform.lbchildren.repository.TreeholeReplyRepository;
import com.platform.lbchildren.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TreeholeService {

    @Autowired
    private TreeholePostRepository postRepository;

    @Autowired
    private TreeholeReplyRepository replyRepository;

    // 创建帖子
    public TreeholePostResponse createPost(UserPrincipal user, String content, String imageUrl) {
        TreeholePost post = new TreeholePost();
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setAuthorUserId(user.getUserId());
        post.setAuthorRole(user.getRole());
        postRepository.save(post);
        return convertToPostResponse(post, false);
    }

    // 获取帖子列表（不含回复详情）
    public List<TreeholePostResponse> getAllPosts() {
        List<TreeholePost> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
                .map(p -> convertToPostResponse(p, false))
                .collect(Collectors.toList());
    }

    // 获取单个帖子（含回复列表）
    public TreeholePostResponse getPostDetail(Long postId) {
        TreeholePost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        return convertToPostResponse(post, true);
    }

    // 创建回复
    public TreeholeReplyResponse createReply(Long postId, UserPrincipal user, String content, String imageUrl) {
        TreeholePost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        TreeholeReply reply = new TreeholeReply();
        reply.setContent(content);
        reply.setImageUrl(imageUrl);
        reply.setAuthorUserId(user.getUserId());
        reply.setAuthorRole(user.getRole());
        reply.setPost(post);
        replyRepository.save(reply);
        return convertToReplyResponse(reply);
    }

    // 获取帖子的回复列表
    public List<TreeholeReplyResponse> getReplies(Long postId) {
        List<TreeholeReply> replies = replyRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return replies.stream()
                .map(this::convertToReplyResponse)
                .collect(Collectors.toList());
    }

    // 工具方法：转换 Post -> Response
    private TreeholePostResponse convertToPostResponse(TreeholePost post, boolean includeReplies) {
        TreeholePostResponse resp = new TreeholePostResponse();
        resp.setId(post.getId());
        resp.setContent(post.getContent());
        resp.setImageUrl(post.getImageUrl());
        resp.setCreatedAt(post.getCreatedAt());
        resp.setReplyCount(post.getReplies().size());
        if (includeReplies) {
            resp.setReplies(post.getReplies().stream()
                    .map(this::convertToReplyResponse)
                    .collect(Collectors.toList()));
        }
        return resp;
    }

    private TreeholeReplyResponse convertToReplyResponse(TreeholeReply reply) {
        TreeholeReplyResponse resp = new TreeholeReplyResponse();
        resp.setId(reply.getId());
        resp.setContent(reply.getContent());
        resp.setImageUrl(reply.getImageUrl());
        resp.setCreatedAt(reply.getCreatedAt());
        return resp;
    }
}
