package com.delivery.global.exception;

import com.delivery.domain.ai.exception.AiErrorCode;
import com.delivery.domain.cart.exception.CartErrorCode;
import com.delivery.domain.menu.exception.MenuErrorCode;
import com.delivery.domain.order.exception.OrderErrorCode;
import com.delivery.domain.payment.exception.PaymentErrorCode;
import com.delivery.domain.review.exception.ReviewErrorCode;
import com.delivery.domain.reviewreply.exception.ReviewReplyErrorCode;
import com.delivery.domain.store.exception.StoreErrorCode;
import com.delivery.domain.user.exception.AuthErrorCode;
import com.delivery.domain.user.exception.UserErrorCode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ErrorCodeRegistry {
    private final Map<String, ErrorCode> registry = new HashMap<>();

    public ErrorCodeRegistry() {
        register(GlobalErrorCode.values());
        register(UserErrorCode.values());
        register(AuthErrorCode.values());
        register(MenuErrorCode.values());
        register(CartErrorCode.values());
        register(AiErrorCode.values());
        register(StoreErrorCode.values());
        register(PaymentErrorCode.values());
        register(OrderErrorCode.values());
        register(ReviewErrorCode.values());
        register(ReviewReplyErrorCode.values());
    }

    private void register(ErrorCode[] codes) {
        for (ErrorCode code : codes) {
            registry.put(code.getName(), code);
        }
    }

    public ErrorCode getByName(String name) {
        return registry.getOrDefault(name, GlobalErrorCode.BAD_REQUEST);
    }
}
