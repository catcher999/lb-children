package com.platform.lbchildren.controller;

import com.platform.lbchildren.entity.EduResource;
import com.platform.lbchildren.entity.LearningProgress;
import com.platform.lbchildren.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping
    public ResponseEntity<?> getResources(@RequestParam(required = false) String type) {
        List<EduResource> resources;
        if (type != null) {
            resources = resourceService.getResourcesByType(type);
        } else {
            resources = resourceService.getAllResources();
        }
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/types")
    public ResponseEntity<?> getResourceTypes() {
        List<String> types = resourceService.getAllResourceTypes();
        return ResponseEntity.ok(Map.of("types", types));
    }

    @PostMapping("/{resourceId}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long resourceId,
            @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Integer progressPercent = Integer.valueOf(request.get("progressPercent").toString());
        
        LearningProgress progress = resourceService.updateProgress(userId, resourceId, progressPercent);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{resourceId}/progress")
    public ResponseEntity<?> getProgress(
            @PathVariable Long resourceId,
            @RequestParam Long userId) {
        Optional<LearningProgress> progress = resourceService.getProgress(userId, resourceId);
        if (progress.isPresent()) {
            return ResponseEntity.ok(progress.get());
        } else {
            return ResponseEntity.ok(Map.of("message", "No progress found"));
        }
    }

    @GetMapping("/progress")
    public ResponseEntity<?> getUserProgress(@RequestParam Long userId) {
        List<LearningProgress> progressList = resourceService.getUserProgress(userId);
        return ResponseEntity.ok(progressList);
    }

    @DeleteMapping("/{resourceId}/progress")
    public ResponseEntity<?> deleteProgress(
            @PathVariable Long resourceId,
            @RequestParam Long userId) {
        resourceService.deleteProgress(userId, resourceId);
        return ResponseEntity.ok(Map.of("message", "Progress deleted successfully"));
    }
}
