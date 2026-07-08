package com.delivery.domain.user.repository;

import com.delivery.domain.user.entity.Address;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, UUID> {}
