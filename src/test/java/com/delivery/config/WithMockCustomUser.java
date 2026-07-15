package com.delivery.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

import org.springframework.security.test.context.support.WithSecurityContext;

/** 커스텀 유저 인증 객체 @WithMockCustomUser */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    long id() default 1L;

    String userName() default "testUser123";

    String userUuid() default "8a38c201-9f79-450f-a38f-51ef19c0de2c";

    String phoneNumber() default "01012345678";

    String role() default "CUSTOMER";
}
