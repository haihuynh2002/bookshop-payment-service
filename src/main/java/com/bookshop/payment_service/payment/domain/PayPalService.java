package com.bookshop.payment_service.payment.domain;

import com.bookshop.payment_service.paypal.PayPalConfig;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    private final PayPalConfig payPalConfig;
    private final APIContext apiContext;

    public PayPalService(PayPalConfig payPalConfig) {
        this.payPalConfig = payPalConfig;
        this.apiContext = new APIContext(
                payPalConfig.clientId(),
                payPalConfig.clientSecret(),
                payPalConfig.mode()
        );
    }

    public Mono<com.paypal.api.payments.Payment> createPayment(Payment payment, String returnUrl, String cancelUrl) {
        return Mono.fromCallable(() -> {
            Amount amount = new Amount();
            amount.setCurrency("USD");
            amount.setTotal(String.format("%.2f", payment.getAmount()));

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setDescription("Payment for order #" + payment.getOrderId());

            List<Transaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            Payer payer = new Payer();
            payer.setPaymentMethod("paypal");

            com.paypal.api.payments.Payment payPalPayment = new com.paypal.api.payments.Payment();
            payPalPayment.setIntent("sale");
            payPalPayment.setPayer(payer);
            payPalPayment.setTransactions(transactions);

            RedirectUrls redirectUrls = new RedirectUrls();
            redirectUrls.setCancelUrl(cancelUrl);
            redirectUrls.setReturnUrl(returnUrl);
            payPalPayment.setRedirectUrls(redirectUrls);

            return payPalPayment.create(apiContext);
        }).onErrorMap(ex -> new PaymentProcessingException("Failed to create PayPal payment", ex));
    }

    public Mono<com.paypal.api.payments.Payment> executePayment(String paymentId, String payerId) {
        return Mono.fromCallable(() -> {
            com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
            payment.setId(paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            return payment.execute(apiContext, paymentExecution);
        }).onErrorMap(ex -> new PaymentProcessingException("Failed to execute PayPal payment", ex));
    }

    public Mono<com.paypal.api.payments.Payment> getPaymentDetails(String paymentId) {
        return Mono.fromCallable(() -> {
            return com.paypal.api.payments.Payment.get(apiContext, paymentId);
        }).onErrorMap(ex -> new PaymentProcessingException("Failed to get payment details", ex));
    }

    public Mono<DetailedRefund> refundPayment(String paymentId, BigDecimal amount, String currency) {
        return Mono.fromCallable(() -> {
            // Get the payment details first
            com.paypal.api.payments.Payment payment = com.paypal.api.payments.Payment.get(apiContext, paymentId);

            // Find the sale ID (transaction ID) from the payment
            String saleId = payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId();

            // Create refund request
            RefundRequest refundRequest = new RefundRequest();

            // Set amount if partial refund
            if (amount != null) {
                Amount refundAmount = new Amount();
                refundAmount.setCurrency(currency != null ? currency : "USD");
                refundAmount.setTotal(String.format("%.2f", amount));
                refundRequest.setAmount(refundAmount);
            }

            // Execute refund
            Sale sale = new Sale();
            sale.setId(saleId);

            return sale.refund(apiContext, refundRequest);
        }).onErrorMap(ex -> new PaymentProcessingException("Failed to process PayPal refund", ex));

    }

    public Mono<Refund> getRefundDetails(String refundId) {
        return Mono.fromCallable(() -> {
            return Refund.get(apiContext, refundId);
        }).onErrorMap(ex -> new PaymentProcessingException("Failed to get refund details", ex));
    }
}
