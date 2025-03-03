package com.pluxity.troubleshooting.trouble1.repository;

import com.pluxity.troubleshooting.trouble1.entity.Parent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    @EntityGraph(attributePaths = {"children"})
    Optional<Parent> findWithChildrenById(Long id);
} 