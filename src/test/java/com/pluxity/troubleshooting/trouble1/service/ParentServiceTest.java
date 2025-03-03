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

    @Test
    @Transactional
    @DisplayName("모두 프록시 객체")
    void demonstrateFirstLevelCacheProblem() {
        String newName = "새로운자식이름";
        // when
        Parent parent = parentService.updateParentCache(1L, newName);

        // then
        // 1. Parent가 프록시 객체인지 확인
        assertThat(parent).isInstanceOf(HibernateProxy.class);

        // 2. 캐시에 저장된 데이터 확인
        Parent cachedData = cacheService.getFromCache(parent.getId());
        assertThat(cachedData).isInstanceOf(HibernateProxy.class);

        // 3. children 컬렉션이 비어있는지 확인
        Parent foundParent = parentRepository.findWithChildrenById(parent.getId()).orElseThrow();
        assertThat(foundParent).isInstanceOf(HibernateProxy.class);
    }
}