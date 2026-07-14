package com.delivery.domain.user.repository;

import com.delivery.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    boolean existsByUsername(String username);

    boolean existsByNickName(String nickName);

    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByUsernameAndDeletedAtIsNull(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByUserUuidAndDeletedAtIsNull(UUID userUuid);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByIdAndDeletedAtIsNull(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesById(Long id);

    @EntityGraph(attributePaths = "roles")
    List<User> findAllBy();

    Optional<User> findByUsernameAndDeletedAtIsNull(String id);

}
