package com.platform.lbchildren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.dto.TreeholePostResponse;
import com.platform.lbchildren.dto.TreeholeReplyResponse;
import com.platform.lbchildren.entity.TreeholePost;
import com.platform.lbchildren.entity.TreeholeReply;
import com.platform.lbchildren.mapper.TreeholePostMapper;
import com.platform.lbchildren.mapper.TreeholeReplyMapper;
import com.platform.lbchildren.security.UserPrincipal;
import com.platform.lbchildren.service.TreeholeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 树洞倾诉业务实现
 */
@Service
@RequiredArgsConstructor
public class TreeholeServiceImpl implements TreeholeService {

    private final TreeholePostMapper postMapper;
    private final TreeholeReplyMapper replyMapper;

    @Override
    public TreeholePostResponse createPost(UserPrincipal user, String content, String imageUrl) {
        TreeholePost post = new TreeholePost();
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setAuthorUserId(user.getUserId());
        post.setAuthorRole(user.getRole());
        post.setCreatedAt(LocalDateTime.now());
        postMapper.insert(post);
        return convertToPostResponse(post, false);
    }

    @Override
    public List<TreeholePostResponse> getAllPosts() {
        List<TreeholePost> posts = postMapper.selectList(
                new LambdaQueryWrapper<TreeholePost>().orderByDesc(TreeholePost::getCreatedAt));
        return posts.stream()
                .map(p -> convertToPostResponse(p, false))
                .collect(Collectors.toList());
    }

    @Override
    public TreeholePostResponse getPostDetail(Long postId) {
        TreeholePost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return convertToPostResponse(post, true);
    }

    @Override
    public TreeholeReplyResponse createReply(Long postId, UserPrincipal user, String content, String imageUrl) {
        TreeholePost post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        TreeholeReply reply = new TreeholeReply();
        reply.setContent(content);
        reply.setImageUrl(imageUrl);
        reply.setAuthorUserId(user.getUserId());
        reply.setAuthorRole(user.getRole());
        reply.setPostId(post.getId());
        reply.setCreatedAt(LocalDateTime.now());
        replyMapper.insert(reply);
        return convertToReplyResponse(reply);
    }

    @Override
    public List<TreeholeReplyResponse> getReplies(Long postId) {
        List<TreeholeReply> replies = replyMapper.selectList(
                new LambdaQueryWrapper<TreeholeReply>()
                        .eq(TreeholeReply::getPostId, postId)
                        .orderByAsc(TreeholeReply::getCreatedAt));
        return replies.stream()
                .map(this::convertToReplyResponse)
                .collect(Collectors.toList());
    }

    private TreeholePostResponse convertToPostResponse(TreeholePost post, boolean includeReplies) {
        TreeholePostResponse resp = new TreeholePostResponse();
        resp.setId(post.getId());
        resp.setContent(post.getContent());
        resp.setImageUrl(post.getImageUrl());
        resp.setCreatedAt(post.getCreatedAt());
        // 统计回复数
        Long replyCount = replyMapper.selectCount(
                new LambdaQueryWrapper<TreeholeReply>().eq(TreeholeReply::getPostId, post.getId()));
        resp.setReplyCount(replyCount.intValue());
        if (includeReplies) {
            List<TreeholeReply> replies = replyMapper.selectList(
                    new LambdaQueryWrapper<TreeholeReply>()
                            .eq(TreeholeReply::getPostId, post.getId())
                            .orderByAsc(TreeholeReply::getCreatedAt));
            resp.setReplies(replies.stream().map(this::convertToReplyResponse).collect(Collectors.toList()));
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