package com.delivery.domain.menu.fixture;

import com.delivery.domain.user.entity.Role;
import com.delivery.domain.user.entity.User;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;

/**
 * Menu/AI 통합 테스트는 @Transactional 롤백 없이 공유 DB에서 여러 테스트 메서드가 함께 실행되므로, User.username/nickName의 유니크
 * 제약을 피하려면 호출마다 랜덤 값이 필요하다.
 */
@AllArgsConstructor
public enum TestUserFixture {
    CUSTOMER(Set.of(Role.CUSTOMER)),
    OWNER(Set.of(Role.OWNER)),
    MANAGER(Set.of(Role.MANAGER)),
    MASTER(Set.of(Role.MASTER));

    private final Set<Role> roles;

    public User createUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return User.create("u" + suffix, "encoded-password", "닉" + suffix, "01000000000", roles);
    }
}
