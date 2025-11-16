package com.bookshop.payment_service.paypal;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paypal")
public record PayPalConfig (
        String clientId,
        String clientSecret,
        String mode,
        String cancelUrl,
        String successUrl
) {
}