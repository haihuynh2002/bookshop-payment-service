package com.bookshop.payment_service.payment.domain;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(Long orderId) {
        super("Payment not found for order id: " + orderId);
    }

    public PaymentNotFoundException(String paymentId) {
        super("Payment not found with id: " + paymentId);
    }
}
