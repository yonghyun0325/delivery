package com.delivery.domain.user.repository;

import com.delivery.domain.user.entity.Address;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    long countByUserId(Long userId);

    boolean existsByUserIdAndIsDefault(Long userId, boolean isDefault);

    List<Address> findAllByUserId(Long userId);

    Address findByIdAndUserId(UUID addressId, Long userId);
}
