package com.bookshop.payment_service.payment.event;

import com.bookshop.payment_service.payment.domain.OrderStatus;
import com.bookshop.payment_service.payment.domain.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class OrderEvent {
    Long id;

    List<OrderItem> orderItems;
    Double amount;

    Boolean exchange;
    PaymentMethod paymentMethod;
    OrderStatus status;

    String userId;
    String email;
    String phone;
    String firstName;
    String lastName;

    String billingStreet;
    String billingCity;
    String billingState;
    String billingPostalCode;
    String billingCountry;

    String shippingStreet;
    String shippingCity;
    String shippingState;
    String shippingPostalCode;
    String shippingCountry;

    Instant createdDate;
    Instant lastModifiedDate;
    String createdBy;
    String lastModifiedBy;
}


