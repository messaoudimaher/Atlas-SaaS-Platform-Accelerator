package com.atlas.notification.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
