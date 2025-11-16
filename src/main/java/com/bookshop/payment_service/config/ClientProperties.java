package com.bookshop.payment_service.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "bookshop")
public record ClientProperties(

	@NotNull
	URI orderServiceUri

){}
