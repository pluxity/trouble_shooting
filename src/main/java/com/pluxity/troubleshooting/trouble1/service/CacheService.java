package com.pluxity.troubleshooting.trouble1.service;

import com.pluxity.troubleshooting.trouble1.entity.Parent;
import com.pluxity.troubleshooting.trouble1.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final ParentRepository parentRepository;
    
    // 실제 캐시 저장소: 동시성을 고려하여 ConcurrentHashMap 사용
    private final Map<Long, Parent> parentCache = new ConcurrentHashMap<>();

    /**
     * Parent 엔티티를 캐시에 저장
     * 
     * @Transactional(readOnly = true):
     * - 읽기 전용 트랜잭션으로 설정하여 불필요한 변경 감지 비용 절약
     * 
     * @Transactional(propagation = Propagation.REQUIRES_NEW):
     * - 호출한 곳의 트랜잭션과 완전히 분리된 새로운 트랜잭션 시작
     * - 새로운 영속성 컨텍스트가 생성되어 이전의 1차 캐시 영향을 받지 않음
     * - 이 설정이 없다면 상위 트랜잭션의 1차 캐시를 그대로 사용하게 됨
     */
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public void updateParentCache(Long parentId) {
        // 새로운 트랜잭션에서 조회하므로 항상 DB에서 fetch한 데이터를 가져옴
        Parent parent = parentRepository.findById(parentId).orElseThrow();

        // 캐시 업데이트
        parentCache.put(parent.getId(), parent);
        log.info("Cache updated. Current cache state for parent {}:", parent);
    }
    
    /**
     * 캐시에서 Parent 엔티티 조회
     * 캐시 miss인 경우 null 반환
     */
    public Parent getFromCache(Long parentId) {
        return parentCache.get(parentId);
    }
    
}