package com.platform.lbchildren.module.resource.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.domain.entity.EduResource;
import com.platform.lbchildren.domain.entity.LearningProgress;
import com.platform.lbchildren.domain.mapper.EduResourceMapper;
import com.platform.lbchildren.domain.mapper.LearningProgressMapper;
import com.platform.lbchildren.module.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 教育资源业务实现
 */
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final EduResourceMapper resourceMapper;
    private final LearningProgressMapper progressMapper;

    @Override
    public List<EduResource> getResourcesByType(String type) {
        return resourceMapper.selectList(
                new LambdaQueryWrapper<EduResource>().eq(EduResource::getType, type.toUpperCase()));
    }

    @Override
    public List<EduResource> getAllResources() {
        return resourceMapper.selectList(null);
    }

    @Override
    public List<String> getAllResourceTypes() {
        List<EduResource> allResources = resourceMapper.selectList(null);
        return allResources.stream()
                .map(EduResource::getType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public LearningProgress updateProgress(Long userId, Long resourceId, Integer progressPercent) {
        LearningProgress progress = progressMapper.selectOne(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getResourceId, resourceId));
        if (progress != null) {
            progress.setProgressPercent(progressPercent);
            progress.setLastLearnTime(LocalDateTime.now());
            progressMapper.updateById(progress);
        } else {
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setResourceId(resourceId);
            progress.setProgressPercent(progressPercent);
            progress.setLastLearnTime(LocalDateTime.now());
            progress.setCreateTime(LocalDateTime.now());
            progress.setUpdateTime(LocalDateTime.now());
            progressMapper.insert(progress);
        }
        return progress;
    }

    @Override
    public Optional<LearningProgress> getProgress(Long userId, Long resourceId) {
        return Optional.ofNullable(progressMapper.selectOne(
                new LambdaQueryWrapper<LearningProgress>()
                        .eq(LearningProgress::getUserId, userId)
                        .eq(LearningProgress::getResourceId, resourceId)));
    }

    @Override
    public List<LearningProgress> getUserProgress(Long userId) {
        return progressMapper.selectList(
                new LambdaQueryWrapper<LearningProgress>().eq(LearningProgress::getUserId, userId));
    }

    @Override
    public void deleteProgress(Long userId, Long resourceId) {
        progressMapper.delete(new LambdaQueryWrapper<LearningProgress>()
                .eq(LearningProgress::getUserId, userId)
                .eq(LearningProgress::getResourceId, resourceId));
    }
}
