package com.pluxity.troubleshooting.trouble1.repository;

import com.pluxity.troubleshooting.trouble1.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<Child, Long> {
} 