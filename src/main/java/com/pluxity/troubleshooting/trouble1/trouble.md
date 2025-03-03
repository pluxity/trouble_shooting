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
3. @EntityGraph로 연관관계를 설정한 후에 repository를 통해 엔티티를 다시 조회해도 프록시 객체가 반환됩니다.

## ✨ 도전 과제

위 코드를 수정하여 다음 조건을 만족하도록 만드세요:

1. 캐시에 저장되는 `Parent` 엔티티는 프록시 객체가 아닌 실제 초기화된 엔티티여야 합니다.
2. `Parent` 엔티티의 `children` 컬렉션이 정상적으로 로딩되어야 합니다.


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
    assertThat(parent).isNotInstanceOf(Parent.class);
    
    // children 컬렉션이 초기화되어 있어야 함
    assertThat(parent.getChildren()).isNotEmpty();
}
```

행운을 빕니다! 🍀 