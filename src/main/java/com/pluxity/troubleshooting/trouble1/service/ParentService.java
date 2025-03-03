package com.pluxity.troubleshooting.trouble1.service;

import com.pluxity.troubleshooting.trouble1.entity.Child;
import com.pluxity.troubleshooting.trouble1.entity.Parent;
import com.pluxity.troubleshooting.trouble1.repository.ChildRepository;
import com.pluxity.troubleshooting.trouble1.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentService {
    private final ChildRepository childRepository;
    private final CacheService cacheService;

    @Transactional
    public Parent updateParentCache(Long childId, String newName) {
        log.info("자식 엔티티의 이름을 변경하고 부모 엔티티의 캐시를 업데이트합니다");

        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found"));
        child.updateName(newName);

        Parent parent = child.getParent();
        cacheService.updateParentCache(parent.getId());
        return parent;
    }
} 