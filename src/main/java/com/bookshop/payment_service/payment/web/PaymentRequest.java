package com.bookshop.payment_service.payment.web;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(

	@NotBlank(message = "The book ISBN must be defined.")
	Long orderId

){}
