# JPA 프록시 객체와 캐시 문제 해결하기

## 🎯 문제 상황

현재 시스템에서는 자식 엔티티의 이름을 변경하고, 이와 연관된 부모 엔티티를 캐시에 저장하는 기능이 있습니다.
하지만 캐시에 저장되는 부모 엔티티가 프록시 객체로 저장되어 문제가 발생하고 있습니다.

### 코드 구조

```java
@Entity
public class Parent {
    @OneToMany(mappedBy = "parent")
    private Set<Child> children = new HashSet<>();
    // ...
}

@Entity
public class Child {
    @ManyToOne(fetch = FetchType.LAZY)
    private Parent parent;
    // ...
}

@Service
public class ParentService {
    public Parent updateParentCache(Long childId, String newName) {
        Child child = childRepository.findById(childId).orElseThrow();
        child.updateName(newName);
        
        Parent parent = child.getParent(); // 프록시 객체
        cacheService.updateParentCache(parent.getId());
        return parent;
    }
}
```

## 🤔 해결해야 할 문제

1. 현재 캐시에는 프록시 객체가 저장되고 있습니다.
2. 프록시 객체의 `children` 컬렉션이 초기화되지 않은 상태입니다.
3. JPA 1차 캐시로 인해 같은 트랜잭션 내에서 엔티티를 다시 조회해도 프록시 객체가 반환됩니다.

## ✨ 도전 과제

위 코드를 수정하여 다음 조건을 만족하도록 만드세요:

1. 캐시에 저장되는 `Parent` 엔티티는 프록시 객체가 아닌 실제 초기화된 엔티티여야 합니다.
2. `Parent` 엔티티의 `children` 컬렉션이 정상적으로 로딩되어야 합니다.
3. JPA 1차 캐시의 영향을 받지 않아야 합니다.

## 💡 힌트

1. `@EntityGraph`를 사용하면 연관 엔티티를 함께 조회할 수 있습니다.
2. JPA 영속성 컨텍스트를 적절히 제어하면 프록시 객체 문제를 해결할 수 있습니다.
3. 엔티티를 직접 조회하는 방식을 고려해보세요.

## 🎓 학습 포인트

- JPA 프록시 객체의 동작 방식
- 영속성 컨텍스트와 1차 캐시의 관계
- 지연 로딩(Lazy Loading)과 즉시 로딩(Eager Loading)
- 엔티티 그래프(@EntityGraph)의 활용

## ✅ 확인사항

수정된 코드는 다음 테스트를 통과해야 합니다:
```java
@Test
@Transactional
void demonstrateFirstLevelCacheProblem() {
    Parent parent = parentService.updateParentCache(childId, "새이름");
    
    // 프록시 객체가 아닌 실제 엔티티여야 함
    assertThat(parent).isNotInstanceOf(HibernateProxy.class);
    
    // children 컬렉션이 초기화되어 있어야 함
    assertThat(parent.getChildren()).isNotEmpty();
}
```

## 🚀 해결 방법

JPA의 1차 캐시로 인해 프록시 객체가 캐시에 저장되는 문제는 다음 두 가지 방법으로 해결할 수 있습니다:

### 1. EntityManager를 사용한 영속성 컨텍스트 초기화

```java
@Service
@RequiredArgsConstructor
public class ParentService {
    private final EntityManager em;
    
    @Transactional
    public Parent updateParentCache(Long childId, String newName) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found"));
        child.updateName(newName);
        
        Parent parent = child.getParent();
        
        // 영속성 컨텍스트 초기화
        em.flush(); // 변경사항을 DB에 반영
        em.clear(); // 1차 캐시 초기화
        
        // 새로운 영속성 컨텍스트에서 조회
        parent = parentRepository.findWithChildrenById(parent.getId())
                .orElseThrow();
                
        cacheService.updateParentCache(parent.getId());
        return parent;
    }
}
```

### 2. 새로운 트랜잭션 시작

```java
@Service
public class ParentService {
    @Transactional
    public Parent updateParentCache(Long childId, String newName) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found"));
        child.updateName(newName);
        
        Parent parent = child.getParent();
        Long parentId = parent.getId();
        
        return loadParentInNewTransaction(parentId);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Parent loadParentInNewTransaction(Long parentId) {
        Parent parent = parentRepository.findWithChildrenById(parentId)
            .orElseThrow(() -> new RuntimeException("Parent not found"));
            
        cacheService.updateParentCache(parent.getId());
        return parent;
    }
}
```

## 💡 각 해결 방법의 특징

### EntityManager 사용 방식
- 명시적으로 영속성 컨텍스트를 제어
- 단일 트랜잭션 내에서 처리
- 더 가벼운 방식
- 트랜잭션 전파 없음

### REQUIRES_NEW 트랜잭션 방식
- 새로운 트랜잭션을 생성
- 새로운 영속성 컨텍스트 생성
- 부모 트랜잭션과 독립적
- 트랜잭션 격리 보장

두 방법 모두 1차 캐시를 초기화하여 프록시 객체 대신 실제 엔티티를 조회할 수 있게 합니다.
상황에 따라 적절한 방법을 선택하여 사용하면 됩니다.

행운을 빕니다! 🍀