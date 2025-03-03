package com.pluxity.troubleshooting.trouble1.service;


import com.pluxity.troubleshooting.trouble1.entity.Parent;
import com.pluxity.troubleshooting.trouble1.repository.ParentRepository;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ParentServiceTest {

    @Autowired
    private ParentService parentService;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private CacheService cacheService;

    /**
     * 테스트 메서드의 @Transactional 주의사항
     * 
     * 1. 테스트에서의 @Transactional도 서비스 계층과 동일하게 영속성 컨텍스트를 공유함
     * 2. 테스트 시작 시 트랜잭션이 시작되고, 테스트 종료 시 롤백됨
     * 3. 테스트 내에서 생성된 엔티티들도 영속성 컨텍스트에 관리되므로:
     *    - 엔티티 초기화나 1차 캐시 초기화를 하지 않으면
     *    - 이후 조회 시 DB 대신 영속성 컨텍스트의 캐시된 객체가 반환될 수 있음
     *    - 특히 프록시 객체 테스트 시 주의가 필요함
     */
    @Test
    @Transactional
    @DisplayName("모두 프록시 객체")
    void demonstrateFirstLevelCacheProblem() {
        String newName = "새로운자식이름";
        
        // when
        Parent parent = parentService.updateParentCache(1L, newName);

        // then
        // 1. Parent 프록시 객체 확인
        // - Child 엔티티의 parent 필드가 지연 로딩으로 설정되어 있어 프록시 객체임
        // - child.getParent()를 통해 얻은 객체이므로 실제 초기화되지 않은 프록시 상태
        assertThat(parent).isInstanceOf(HibernateProxy.class);

        // 2. 캐시에 저장된 데이터 확인
        // - CacheService에서 REQUIRES_NEW로 새로운 트랜잭션에서 조회했으므로
        // - 실제 초기화된 Parent 엔티티가 저장되어 있어야 함
        Parent cachedData = cacheService.getFromCache(parent.getId());
        assertThat(cachedData).isInstanceOf(Parent.class);

        // 3. EntityGraph를 통한 조회 결과 확인
        // - findWithChildrenById는 @EntityGraph를 사용하여 즉시 로딩으로 조회
        // - 이전에는 1차 캐시의 프록시 객체가 반환되었으나
        // - 현재는 CacheService의 새 트랜잭션으로 인해 실제 Parent 엔티티가 반환됨
        Parent foundParent = parentRepository.findWithChildrenById(parent.getId()).orElseThrow();
        assertThat(foundParent).isInstanceOf(Parent.class);
    }
}