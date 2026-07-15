// package com.delivery.domain.user.repository;
//
// import com.delivery.common.util.CryptoConverter;
// import com.delivery.common.util.SsnEncryptor;
// import com.delivery.config.AbstractIntegrationTest;
// import com.delivery.domain.user.fixture.UserFixture;
// import com.delivery.global.config.CustomAuditorAware;
// import com.delivery.global.config.EncryptConfig;
// import com.delivery.global.config.JpaAuditingConfig;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Import;
// import org.springframework.data.domain.AuditorAware;
//
// import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
// @DataJpaTest
// @Import({
//        JpaAuditingConfig.class,
//        UserRepositoryTest.TestAuditorConfig.class,
//        EncryptConfig.class,
//        SsnEncryptor.class,
//        CryptoConverter.class
// })
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// class UserRepositoryTest extends AbstractIntegrationTest {
//    @TestConfiguration
//    static class TestAuditorConfig {
//        @Bean(name = "customAuditorAware")
//        public AuditorAware<String> customAuditorAware() {
//            return new CustomAuditorAware();
//        }
//    }
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Nested
//    @DisplayName("아이디 존재 여부 확인")
//    class existsByUsername {
//        @Test
//        @DisplayName("해당 아이디가 존재하면 true를 반환한다..")
//        void existsByUsername_exist() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            boolean result = userRepository.existsByUsername("test123");
//
//            // then
//            assertThat(result).isTrue();
//        }
//
//        @Test
//        @DisplayName("해당 아이디가 없으면 false를 반환한다.")
//        void existsByUsername_not_found() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            boolean result = userRepository.existsByUsername("123");
//
//            // then
//            assertThat(result).isFalse();
//        }
//    }
//
//    @Nested
//    @DisplayName("닉네임 존재 여부 확인")
//    class existsByNickName {
//        @Test
//        @DisplayName("해당 닉네임이 존재하면 true를 반환한다.")
//        void existsByNickName_exist() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            boolean result = userRepository.existsByNickName("test123");
//
//            // then
//            assertThat(result).isTrue();
//        }
//
//        @Test
//        @DisplayName("해당 닉네임이 없으면 false를 반환한다.")
//        void existsByNickName_not_found() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            boolean result = userRepository.existsByNickName("123");
//
//            // then
//            assertThat(result).isFalse();
//        }
//    }
//
//    @Nested
//    @DisplayName("특정 아이디를 가진 사용자를 찾는다.")
//    class findByUsername {
//        @Test
//        @DisplayName("특정 아이디를 가진 사용자를 조회한다.")
//        void findByUsername_success() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            var result = userRepository.findByUsername("test123");
//
//            // then
//            assertThat(result).isNotEmpty();
//        }
//
//        @Test
//        @DisplayName("특정 아이디를 가진 사용자를 찾을 수 없는 경우 조회되지 않는다..")
//        void findByUsername_fail() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            var result = userRepository.findByUsername("213124");
//
//            // then
//            assertThat(result).isEmpty();
//        }
//    }
//
//    @Nested
//    @DisplayName("특정 아이디를 가진 삭제되지 않은 사용자를 찾는다.")
//    class findByIdAndDeletedAtIsNull {
//        @Test
//        @DisplayName("삭제되지 않은 특정 아이디를 가진 사용자를 조회한다.")
//        void findByIdAndDeletedAtIsNull_find() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            var result = userRepository.findByUsername("test123");
//
//            // then
//            assertThat(result).isNotEmpty();
//        }
//
//        @Test
//        @DisplayName("삭제되지 않은 특정 아이디를 가진 사용자가 없으면 조회되지 않는다..")
//        void findByIdAndDeletedAtIsNull_not_found() {
//            // given
//            userRepository.save(UserFixture.ROLE_CUSTOMER.createUserNoId());
//
//            // when
//            var result = userRepository.findByUsername("1232142144");
//
//            // then
//            assertThat(result).isNotEmpty();
//        }
//    }
//
//
//
////    @Test
////    void findWithRolesByUsernameAndDeletedAtIsNull() {
////    }
////
////    @Test
////    void findWithRolesByUserUuidAndDeletedAtIsNull() {
////    }
////
////    @Test
////    void findWithRolesByIdAndDeletedAtIsNull() {
////    }
////
////    @Test
////    void findWithRolesById() {
////    }
////
////    @Test
////    void findAllBy() {
////    }
////
////    @Test
////    void findByUsernameAndDeletedAtIsNull() {
////    }
// }
