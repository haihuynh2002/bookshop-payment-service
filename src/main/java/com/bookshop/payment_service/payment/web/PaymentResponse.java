package com.bookshop.payment_service.payment.web;

import com.bookshop.payment_service.payment.domain.PaymentStatus;

public record PaymentResponse(String paymentId, String approvalUrl, PaymentStatus status) {
}
