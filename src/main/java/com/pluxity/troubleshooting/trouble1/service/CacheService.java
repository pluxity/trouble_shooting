package com.pluxity.troubleshooting.trouble1.service;

import com.pluxity.troubleshooting.trouble1.entity.Parent;
import com.pluxity.troubleshooting.trouble1.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final ParentRepository parentRepository;
    
    // Parent 캐시: Key는 Parent ID, Value는 Parent
    private final Map<Long, Parent> parentCache = new ConcurrentHashMap<>();

    public void updateParentCache(Long parentId) {
        // parentRepository에서 entityGraph를 통해 모두 조회한다.
        Parent parent = parentRepository.findById(parentId).orElseThrow();

        parentCache.put(parent.getId(), parent);
        log.info("Cache updated. Current cache state for parent {}:", parent);
    }
    
    public Parent getFromCache(Long parentId) {
        return parentCache.get(parentId);
    }
    
}