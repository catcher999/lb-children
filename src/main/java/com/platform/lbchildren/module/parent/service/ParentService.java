package com.platform.lbchildren.module.parent.service;

import com.platform.lbchildren.domain.dto.AddChildRequest;
import com.platform.lbchildren.domain.dto.ChildProfileVO;
import com.platform.lbchildren.domain.dto.ChildVO;

import java.util.List;

/**
 * 家长端业务接口
 */
public interface ParentService {

    /** 家长添加儿童 */
    void addChild(Long parentId, AddChildRequest request);

    /** 查询家长的儿童列表 */
    List<ChildVO> getMyChildren(Long parentId);

    /** 设置是否授权查看孩子画像（阶段五） */
    void setProfileConsent(Long parentId, Long childId, boolean consent);

    /** 查看孩子画像摘要（需授权，阶段五） */
    ChildProfileVO getChildProfile(Long parentId, Long childId);
}
