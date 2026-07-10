package com.delivery.domain.ai.repository;

import com.delivery.domain.ai.entity.AiLogEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiLogRepository extends JpaRepository<AiLogEntity, UUID> {}
