package com.platform.lbchildren.service;

import com.platform.lbchildren.entity.EduResource;
import com.platform.lbchildren.repository.EduResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    @Autowired
    private EduResourceRepository resourceRepository;

    public List<EduResource> getResourcesByType(String type) {
        return resourceRepository.findByType(type.toUpperCase());
    }
}