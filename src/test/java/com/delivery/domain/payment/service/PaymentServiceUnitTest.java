package com.delivery.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.delivery.domain.payment.dto.response.PaymentPageResponse;
import com.delivery.domain.payment.dto.response.PaymentResponse;
import com.delivery.domain.payment.entity.Payment;
import com.delivery.domain.payment.entity.PaymentMethod;
import com.delivery.domain.payment.entity.PaymentStatus;
import com.delivery.domain.payment.repository.PaymentRepository;
import com.delivery.domain.store.entity.Store;
import com.delivery.domain.store.repository.StoreRepository;
import com.delivery.domain.user.enums.Role;
import com.delivery.global.exception.BusinessException;
import com.delivery.global.security.config.CustomUserDetails;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class PaymentServiceUnitTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private StoreRepository storeRepository;

    @InjectMocks private PaymentService paymentService;

    @Nested
    @DisplayName("결제 단건 조회")
    class GetPayment {

        @Test
        @DisplayName("고객은 본인 결제만 조회할 수 있다")
        void getPayment_success_when_customer_owns_payment() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 1L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            PaymentResponse response = paymentService.getPayment(paymentId, userDetails);

            assertThat(response.paymentId()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("고객이 다른 사람 결제를 조회하면 예외가 발생한다")
        void getPayment_fail_when_customer_does_not_own_payment() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.getPayment(paymentId, userDetails))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("OWNER는 본인 가게 결제를 조회할 수 있다")
        void getPayment_success_when_owner_owns_store() {
            UUID paymentId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails =
                    createUserDetails(10L, Set.of(Role.OWNER, Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(paymentRepository.findStoreIdByPaymentId(paymentId))
                    .thenReturn(Optional.of(storeId));
            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.of(createStore(storeId, 10L)));

            PaymentResponse response = paymentService.getPayment(paymentId, userDetails);

            assertThat(response.paymentId()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("OWNER는 다른 가게 결제를 조회할 수 없다")
        void getPayment_fail_when_owner_does_not_own_store() {
            UUID paymentId = UUID.randomUUID();
            UUID storeId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails =
                    createUserDetails(10L, Set.of(Role.OWNER, Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(paymentRepository.findStoreIdByPaymentId(paymentId))
                    .thenReturn(Optional.of(storeId));
            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.of(createStore(storeId, 20L)));

            assertThatThrownBy(() -> paymentService.getPayment(paymentId, userDetails))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("MANAGER는 다른 사람 결제도 조회할 수 있다")
        void getPayment_success_when_manager_reads_other_payment() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(99L, Set.of(Role.MANAGER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            PaymentResponse response = paymentService.getPayment(paymentId, userDetails);

            assertThat(response.paymentId()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("MASTER는 다른 사람 결제도 조회할 수 있다")
        void getPayment_success_when_master_reads_other_payment() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(100L, Set.of(Role.MASTER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            PaymentResponse response = paymentService.getPayment(paymentId, userDetails);

            assertThat(response.paymentId()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외가 발생한다")
        void getPayment_fail_when_payment_does_not_exist() {
            UUID paymentId = UUID.randomUUID();
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPayment(paymentId, userDetails))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("내 결제 목록 조회")
    class GetMyPayments {

        @Test
        @DisplayName("페이지 요청 값이 잘못되면 예외가 발생한다")
        void getMyPayments_fail_when_page_request_is_invalid() {
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            assertThatThrownBy(() -> paymentService.getMyPayments(userDetails, -1, 10, null))
                    .isInstanceOf(BusinessException.class);

            verify(paymentRepository, never()).findByUserId(any(), any(Pageable.class));
        }

        @Test
        @DisplayName("상태 조건이 있으면 상태 기반 목록을 조회한다")
        void getMyPayments_success_when_status_filter_exists() {
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));
            Payment payment = createPayment(UUID.randomUUID(), 1L, PaymentStatus.PAID);

            when(paymentRepository.findByUserIdAndPaymentStatus(
                            eq(1L), eq(PaymentStatus.PAID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(payment)));

            PaymentPageResponse response =
                    paymentService.getMyPayments(userDetails, 0, 10, PaymentStatus.PAID);

            assertThat(response.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("가게 결제 목록 조회")
    class GetStorePayments {

        @Test
        @DisplayName("OWNER는 본인 가게 결제 목록을 조회할 수 있다")
        void getStorePayments_success_when_owner_owns_store() {
            UUID storeId = UUID.randomUUID();
            Payment payment = createPayment(UUID.randomUUID(), 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails =
                    createUserDetails(10L, Set.of(Role.OWNER, Role.CUSTOMER));

            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.of(createStore(storeId, 10L)));
            when(paymentRepository.findByStoreIdAndPaymentStatus(
                            eq(storeId), eq(PaymentStatus.PAID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(payment)));

            PaymentPageResponse response =
                    paymentService.getStorePayments(
                            storeId, userDetails, 0, 10, PaymentStatus.PAID);

            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("OWNER는 다른 가게 결제 목록을 조회할 수 없다")
        void getStorePayments_fail_when_owner_does_not_own_store() {
            UUID storeId = UUID.randomUUID();
            CustomUserDetails userDetails =
                    createUserDetails(10L, Set.of(Role.OWNER, Role.CUSTOMER));

            when(storeRepository.findByStoreIdAndDeletedAtIsNull(storeId))
                    .thenReturn(Optional.of(createStore(storeId, 20L)));

            assertThatThrownBy(
                            () ->
                                    paymentService.getStorePayments(
                                            storeId, userDetails, 0, 10, PaymentStatus.PAID))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("MANAGER는 모든 가게 결제 목록을 조회할 수 있다")
        void getStorePayments_success_when_manager_reads_any_store() {
            UUID storeId = UUID.randomUUID();
            Payment payment = createPayment(UUID.randomUUID(), 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(99L, Set.of(Role.MANAGER));

            when(paymentRepository.findByStoreIdAndPaymentStatus(
                            eq(storeId), eq(PaymentStatus.PAID), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(payment)));

            PaymentPageResponse response =
                    paymentService.getStorePayments(
                            storeId, userDetails, 0, 10, PaymentStatus.PAID);

            assertThat(response.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class CancelPayment {

        @Test
        @DisplayName("이미 취소된 결제는 다시 취소할 수 없다")
        void cancelPayment_fail_when_payment_already_canceled() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 1L, PaymentStatus.CANCELED);
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, "고객 요청", userDetails))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("고객은 본인 결제만 취소할 수 있다")
        void cancelPayment_fail_when_customer_does_not_own_payment() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 2L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, "고객 요청", userDetails))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("본인 결제는 취소할 수 있다")
        void cancelPayment_success() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 1L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(1L, Set.of(Role.CUSTOMER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            PaymentResponse response =
                    paymentService.cancelPayment(paymentId, "고객 요청", userDetails);

            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        }

        @Test
        @DisplayName("MANAGER는 다른 사람 결제도 취소할 수 있다")
        void cancelPayment_success_when_manager_cancels_other_payment() {
            UUID paymentId = UUID.randomUUID();
            Payment payment = createPayment(paymentId, 1L, PaymentStatus.PAID);
            CustomUserDetails userDetails = createUserDetails(99L, Set.of(Role.MANAGER));

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            PaymentResponse response =
                    paymentService.cancelPayment(paymentId, "관리자 취소", userDetails);

            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.CANCELED);
        }
    }

    private Payment createPayment(UUID paymentId, Long userId, PaymentStatus paymentStatus) {
        return Payment.builder()
                .paymentId(paymentId)
                .orderId(UUID.randomUUID())
                .userId(userId)
                .paymentMethod(PaymentMethod.CARD)
                .paidAt(LocalDateTime.now())
                .paymentStatus(paymentStatus)
                .paymentAmount(15000)
                .build();
    }

    private Store createStore(UUID storeId, Long ownerUserId) {
        return Store.builder()
                .storeId(storeId)
                .userId(ownerUserId)
                .categoryId(UUID.randomUUID())
                .regionId(UUID.randomUUID())
                .name("store")
                .address("address")
                .phone("010-1234-5678")
                .description("desc")
                .minOrderAmount(10000)
                .build();
    }

    private CustomUserDetails createUserDetails(Long id, Set<Role> roles) {
        return CustomUserDetails.builder()
                .id(id)
                .username("tester")
                .password("password")
                .nickName("tester")
                .phoneNumber("01012345678")
                .authorities(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                                .toList())
                .build();
    }
}
