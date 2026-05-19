package com.platform.lbchildren.controller;

import com.platform.lbchildren.entity.EduResource;
import com.platform.lbchildren.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            resources = resourceService.getResourcesByType("ALL");
        }
        return ResponseEntity.ok(resources);
    }
}
