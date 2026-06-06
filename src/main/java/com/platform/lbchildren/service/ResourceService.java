package com.platform.lbchildren.service;

import com.platform.lbchildren.entity.EduResource;
import com.platform.lbchildren.entity.LearningProgress;
import com.platform.lbchildren.repository.EduResourceRepository;
import com.platform.lbchildren.repository.LearningProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private EduResourceRepository resourceRepository;

    @Autowired
    private LearningProgressRepository progressRepository;

    public List<EduResource> getResourcesByType(String type) {
        return resourceRepository.findByType(type.toUpperCase());
    }

    public List<EduResource> getAllResources() {
        return resourceRepository.findAll();
    }

    public List<String> getAllResourceTypes() {
        List<EduResource> allResources = resourceRepository.findAll();
        return allResources.stream()
                .map(EduResource::getType)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public LearningProgress updateProgress(Long userId, Long resourceId, Integer progressPercent) {
        Optional<LearningProgress> existingProgress = progressRepository.findByUserIdAndResourceId(userId, resourceId);
        
        LearningProgress progress;
        if (existingProgress.isPresent()) {
            progress = existingProgress.get();
            progress.setProgressPercent(progressPercent);
            progress.setLastLearnTime(LocalDateTime.now());
        } else {
            progress = new LearningProgress();
            progress.setUserId(userId);
            progress.setResourceId(resourceId);
            progress.setProgressPercent(progressPercent);
            progress.setLastLearnTime(LocalDateTime.now());
        }
        
        return progressRepository.save(progress);
    }

    public Optional<LearningProgress> getProgress(Long userId, Long resourceId) {
        return progressRepository.findByUserIdAndResourceId(userId, resourceId);
    }

    public List<LearningProgress> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    public void deleteProgress(Long userId, Long resourceId) {
        progressRepository.deleteByUserIdAndResourceId(userId, resourceId);
    }
}