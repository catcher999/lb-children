package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.AddChildRequest;
import com.platform.lbchildren.dto.ChildVO;
import com.platform.lbchildren.dto.RegisterRequest;

import java.util.List;

/**
 * 家长端业务接口
 */
public interface ParentService {

    /** 家长注册 */
    void register(RegisterRequest request);

    /** 家长登录，返回 JWT token */
    String login(String username, String password);

    /** 家长添加儿童 */
    void addChild(Long parentId, AddChildRequest request);

    /** 查询家长的儿童列表 */
    List<ChildVO> getMyChildren(Long parentId);
}
