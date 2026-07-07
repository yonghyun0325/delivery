package com.delivery.domain.user.repository;

import com.delivery.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByNickName(String nickName);

    Optional<User> findByUsername(String username);
}
