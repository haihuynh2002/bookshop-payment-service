package com.bookshop.payment_service.payment.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PaymentRepository extends ReactiveCrudRepository<Payment,Long> {
    Mono<Payment> findByPaymentId(String paymentId);
    Mono<Payment> findByOrderId(Long orderId);
    Mono<Payment> findByToken(String token);
}
