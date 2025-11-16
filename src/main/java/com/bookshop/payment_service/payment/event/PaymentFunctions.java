package com.bookshop.payment_service.payment.event;

import com.bookshop.payment_service.payment.domain.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Configuration
@Slf4j
public class PaymentFunctions {

	@Bean
	public Consumer<Flux<OrderEvent>> handleOrderEvent(PaymentService paymentService) {
		return flux -> paymentService.consumeOrderEvent(flux)
				.doOnNext(payment -> log.info("The payment with id {} is created", payment.getId()))
				.subscribe();
	}

    @Bean
    public Consumer<Flux<DeliveryEvent>> handleDeliveryEvent(PaymentService paymentService) {
        return flux -> paymentService.consumeDeliveryEvent(flux)
                .doOnNext(payment -> log.info("The payment with id {} is created", payment.getId()))
                .subscribe();
    }


}
