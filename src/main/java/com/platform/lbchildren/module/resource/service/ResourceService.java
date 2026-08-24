package com.platform.lbchildren.module.resource.service;

import com.platform.lbchildren.domain.entity.EduResource;
import com.platform.lbchildren.domain.entity.LearningProgress;

import java.util.List;
import java.util.Optional;

/**
 * 教育资源业务接口
 */
public interface ResourceService {

    /** 按类型查询教育资源 */
    List<EduResource> getResourcesByType(String type);

    /** 查询全部教育资源 */
    List<EduResource> getAllResources();

    /** 查询所有资源类型 */
    List<String> getAllResourceTypes();

    /** 更新学习进度 */
    LearningProgress updateProgress(Long userId, Long resourceId, Integer progressPercent);

    /** 查询某资源的学习进度 */
    Optional<LearningProgress> getProgress(Long userId, Long resourceId);

    /** 查询某用户的所有学习进度 */
    List<LearningProgress> getUserProgress(Long userId);

    /** 删除某学习进度 */
    void deleteProgress(Long userId, Long resourceId);
}
