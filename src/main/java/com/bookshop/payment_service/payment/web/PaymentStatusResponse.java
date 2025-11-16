package com.bookshop.payment_service.payment.web;

import com.bookshop.payment_service.payment.domain.PaymentStatus;

import java.math.BigDecimal;

public record PaymentStatusResponse(String paymentId, PaymentStatus status, BigDecimal amount) {
}
