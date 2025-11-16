package com.bookshop.payment_service.payment.domain;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table("payment")
@ToString
public class Payment {

    @Id
    @NotNull(message = "ID cannot be null")
    @Positive(message = "ID must be a positive number")
    Long id;

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be a positive number")
    Long orderId;

    @NotBlank(message = "Payment ID cannot be blank")
    @Size(max = 100, message = "Payment ID must be less than 100 characters")
    String paymentId;

    @Size(max = 100, message = "Token must be less than 100 characters")
    String token;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    BigDecimal amount;

    @NotNull(message = "Payment status cannot be null")
    PaymentStatus status;

    @NotBlank(message = "Customer ID cannot be blank")
    @Size(max = 50, message = "Customer ID must be less than 50 characters")
    String userId;

    @NotBlank(message = "Customer email cannot be blank")
    @Email(message = "Customer email must be a valid email address")
    String email;

    @NotBlank(message = "Customer fist name cannot be blank")
    @Size(max = 50, message = "Customer first name must be less than 50 characters")
    String firstName;

    @NotBlank(message = "Customer last name cannot be blank")
    @Size(max = 50, message = "Customer last name must be less than 50 characters")
    String lastName;


    @NotBlank(message = "Payment method cannot be blank")
    @Pattern(regexp = "^(CASH|PAYPAL)$", message = "Payment method must be CASH or PAYPAL")
    PaymentMethod paymentMethod;

    String billingStreet;
    String billingCity;
    String billingState;
    String billingPostalCode;
    String billingCountry;

    @CreatedDate
    Instant createdDate;

    @LastModifiedDate
    Instant lastModifiedDate;

    @Version
    Long version;
}