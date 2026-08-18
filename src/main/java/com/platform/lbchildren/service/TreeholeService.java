package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.TreeholePostResponse;
import com.platform.lbchildren.dto.TreeholeReplyResponse;
import com.platform.lbchildren.security.UserPrincipal;

import java.util.List;

/**
 * 树洞倾诉业务接口
 */
public interface TreeholeService {

    /** 发布帖子 */
    TreeholePostResponse createPost(UserPrincipal user, String content, String imageUrl);

    /** 获取帖子列表（不含回复详情） */
    List<TreeholePostResponse> getAllPosts();

    /** 获取帖子详情（含回复列表） */
    TreeholePostResponse getPostDetail(Long postId);

    /** 回复帖子 */
    TreeholeReplyResponse createReply(Long postId, UserPrincipal user, String content, String imageUrl);

    /** 获取帖子的回复列表 */
    List<TreeholeReplyResponse> getReplies(Long postId);
}
