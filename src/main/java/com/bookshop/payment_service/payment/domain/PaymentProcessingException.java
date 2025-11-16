package com.bookshop.payment_service.payment.domain;

public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super("Payment processing failed: " + message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super("Payment processing failed: " + message, cause);
    }
}
