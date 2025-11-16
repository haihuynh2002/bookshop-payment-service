package com.bookshop.payment_service.payment.domain;

import com.bookshop.payment_service.payment.event.DeliveryEvent;
import com.bookshop.payment_service.payment.event.DeliveryStatus;
import com.bookshop.payment_service.payment.event.OrderEvent;
import com.bookshop.payment_service.payment.web.PaymentResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PaymentService {

    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;
    PayPalService payPalService;
    StreamBridge streamBridge;

    public Mono<PaymentResponse> initiatePayment(Long orderId, String successUrl, String cancelUrl) {
        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new PaymentNotFoundException(orderId)))
                .flatMap(payment -> payPalService.createPayment(payment, successUrl, cancelUrl))
                .doOnNext(payment -> log.info("Created PayPal payment: {}", payment))
                .flatMap(paypalPayment -> updatePaymentWithPayPalInfo(orderId, paypalPayment))
                .map(this::createPaymentResponse);
    }

    public Mono<Payment> processPaymentCallback(String paymentId, String payerId, String token) {
        return paymentRepository.findByPaymentId(paymentId)
                .switchIfEmpty(Mono.error(new PaymentNotFoundException(paymentId)))
                .flatMap(payment -> executePayment(payment, payerId))
                .doOnNext(this::publishPaymentEvent);
    }

    public Flux<Payment> consumeOrderEvent(Flux<OrderEvent> flux) {
        return flux
                .flatMap(event -> switch (event.getStatus()) {
                    case OrderStatus.ACCEPTED -> buildPendingPayment(event);
                    case OrderStatus.CANCELLED -> cancelPayment(event.getId());
                    default -> Mono.empty();
                });
    }

    public Flux<Payment> consumeDeliveryEvent(Flux<DeliveryEvent> flux) {
        return flux
                .flatMap(event -> switch (event.getStatus()) {
                    case DeliveryStatus.SHIPPED -> completePayment(event.getOrderId());
                    case DeliveryStatus.CANCELLED -> cancelPayment(event.getOrderId());
                    default -> Mono.empty();
                });
    }

    public Mono<Payment> buildPendingPayment(OrderEvent event) {
        return Mono.fromCallable(() -> {
                    var payment = paymentMapper.toPayment(event);
                    payment.setStatus(PaymentStatus.PENDING);
                    return payment;
                })
                .flatMap(paymentRepository::save);
    }

    public Mono<Payment> cancelPayment(String token) {
        return paymentRepository.findByToken(token)
                .switchIfEmpty(Mono.error(new PaymentNotFoundException(token)))
                .flatMap(this::cancelPayment)
                .doOnNext(this::publishPaymentEvent);
    }

    public Mono<Payment> cancelPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.empty())
                .flatMap(payment -> {
                    if (payment.getPaymentMethod().equals(PaymentMethod.PAYPAL)
                            && payment.getStatus().equals(PaymentStatus.COMPLETED)) {
                        return processRefund(payment);
                    } else {
                        return cancelPayment(payment);
                    }
                });
    }

    public Mono<Payment> completePayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new PaymentNotFoundException(orderId)))
                .flatMap(this::completePayment);
    }

    public Flux<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private Mono<Payment> updatePaymentWithPayPalInfo(Long orderId, com.paypal.api.payments.Payment paypalPayment) {
        return paymentRepository.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new PaymentNotFoundException(orderId)))
                .map(payment -> {
                    payment.setPaymentId(paypalPayment.getId());
                    payment.setToken(extractTokenFromApprovalUrl(paypalPayment));
                    return payment;
                })
                .flatMap(paymentRepository::save);
    }

    private PaymentResponse createPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                getApprovalUrl(payment.getToken()),
                payment.getStatus()
        );
    }

    private Mono<Payment> executePayment(Payment payment, String payerId) {
        return payPalService.executePayment(payment.getPaymentId(), payerId)
                .flatMap(executedPayment -> {
                    return "approved".equalsIgnoreCase(executedPayment.getState())
                            ? completePayment(payment)
                            : cancelPayment(payment);
                });
    }

    private String getApprovalUrl(String token) {
        return "https://www.sandbox.paypal.com/checkoutnow?token=" + token;
    }

    private String extractTokenFromApprovalUrl(com.paypal.api.payments.Payment payment) {
        return payment.getLinks().stream()
                .filter(link -> "approval_url".equals(link.getRel()))
                .findFirst()
                .map(link -> {
                    String url = link.getHref();
                    int tokenIndex = url.indexOf("token=");
                    if (tokenIndex != -1) {
                        return url.substring(tokenIndex + 6);
                    }
                    return null;
                })
                .orElseThrow(() -> new PaymentProcessingException("No approval URL found"));
    }

    private void publishPaymentEvent(Payment payment) {
        var paymentEvent = paymentMapper.toPaymentEvent(payment);
        log.info("Publishing payment event: {}", paymentEvent);
        var result = streamBridge.send("payment-out-0", paymentEvent);
        log.info("Payment event published: {}", result);
    }

    private Mono<Payment> cancelPayment(Payment payment) {
        payment.setStatus(PaymentStatus.CANCELLED);
        return paymentRepository.save(payment);
    }

    private Mono<Payment> completePayment(Payment payment) {
        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
    }

    private Mono<Payment> processRefund(Payment payment) {
        return payPalService.refundPayment(payment.getPaymentId(), payment.getAmount(), "USD")
                .flatMap(detailedRefund -> {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    return paymentRepository.save(payment);
                });
    }
}