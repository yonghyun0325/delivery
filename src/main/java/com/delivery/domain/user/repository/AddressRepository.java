package com.delivery.domain.user.repository;

import com.delivery.domain.user.entity.Address;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    long countByUserIdAndDeletedAtIsNull(Long userId);

    boolean existsByUserIdAndIsDefaultAndDeletedAtIsNull(Long userId, boolean isDefault);

    List<Address> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Address> findByIdAndUserIdAndDeletedAtIsNull(UUID addressId, Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrueAndDeletedAtIsNull(Long userId);
}
