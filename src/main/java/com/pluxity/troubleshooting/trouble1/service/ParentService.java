package com.pluxity.troubleshooting.trouble1.service;

import com.pluxity.troubleshooting.trouble1.entity.Child;
import com.pluxity.troubleshooting.trouble1.entity.Parent;
import com.pluxity.troubleshooting.trouble1.repository.ChildRepository;
import com.pluxity.troubleshooting.trouble1.repository.ParentRepository;
import jakarta.persistence.EntityManager;
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
    private final EntityManager em;

    @Transactional
    public Parent updateParentCache(Long childId, String newName) {
        log.info("자식 엔티티의 이름을 변경하고 부모 엔티티의 캐시를 업데이트합니다");

        // 1. Child 엔티티 조회 - 이 시점에서는 parent 필드가 초기화되지 않은 상태
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found"));
        child.updateName(newName);

        // 2. Parent 프록시 객체 획득
        // @ManyToOne(fetch = FetchType.LAZY) 설정으로 인해 프록시 객체가 반환됨
        Parent parent = child.getParent();

        // 3. 영속성 컨텍스트 초기화
        // - 이 작업이 없으면 이후 repository 조회 시 DB 조회 없이 1차 캐시의 프록시 객체가 반환됨
        // - em.flush(): 변경 감지된 내용을 DB에 반영
        // - em.clear(): 1차 캐시 완전 초기화
        clearContext();

        // 4. 캐시 업데이트
        // - @Transactional 범위가 CacheService까지 이어지므로, 
        // - CacheService에서 REQUIRES_NEW를 사용하지 않으면 여기서 초기화된 1차 캐시가 그대로 전달됨
        cacheService.updateParentCache(parent.getId());
        return parent;
    }

    /**
     * 영속성 컨텍스트를 초기화하여 1차 캐시를 비움
     * 이후 조회 시 무조건 DB에서 새로 조회하도록 함
     */
    private void clearContext() {
        em.flush(); // 변경 감지된 내용을 DB에 동기화
        em.clear(); // 영속성 컨텍스트의 모든 엔티티 제거
    }
} 