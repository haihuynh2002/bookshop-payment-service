package com.bookshop.payment_service.payment.web;

import com.bookshop.payment_service.payment.domain.Payment;
import com.bookshop.payment_service.payment.domain.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
	PaymentService paymentService;

    @PostMapping("/initiate/{orderId}")
    public Mono<PaymentResponse> initiatePayment(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "${paypal.success-url}") String successUrl,
            @RequestParam(defaultValue = "${paypal.cancel-url}") String cancelUrl) {
        return paymentService.initiatePayment(orderId, successUrl, cancelUrl);
    }

    @GetMapping("/success")
    public Mono<ResponseEntity> paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam("token") String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/order"));
        return paymentService.processPaymentCallback(paymentId, payerId, token)
                .thenReturn(new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY));
    }

    @GetMapping("/cancel")
    public Mono<ResponseEntity> cancelPayment(
            @RequestParam(value = "token", required = false) String token
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/order"));
        return paymentService.cancelPayment(token)
                .thenReturn(new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY));
    }

    @GetMapping
	public Flux<Payment> getAllPayments() {
		return paymentService.getAllPayments();
	}
}
