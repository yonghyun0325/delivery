package com.delivery.domain.user.service;

import com.delivery.domain.user.UserDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {
    private final AuthService authService;

    /**
     * 회원 탈퇴 리스너 회원 로그아웃 처리
     *
     * @param event
     */
    @Async
    @TransactionalEventListener
    public void handleUserDeleteEvent(UserDeletedEvent event) {


        throw new UnsupportedOperationException("개발 중 입니다.");
    }
}
