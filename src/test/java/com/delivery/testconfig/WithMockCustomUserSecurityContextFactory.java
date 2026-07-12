package com.delivery.testconfig;

import com.delivery.global.security.config.CustomUserDetails;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser mockCustomUser) {
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        CustomUserDetails principal =
                CustomUserDetails.builder()
                        .id(mockCustomUser.id())
                        .username(mockCustomUser.userName())
                        .nickName(mockCustomUser.nickName())
                        .phoneNumber(mockCustomUser.phoneNumber())
                        .authorities(
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + mockCustomUser.role())))
                        .build();

        final UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());

        securityContext.setAuthentication(authenticationToken);
        return securityContext;
    }
}
