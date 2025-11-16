package com.bookshop.payment_service.payment.domain;

import com.bookshop.payment_service.payment.event.OrderEvent;
import com.bookshop.payment_service.payment.event.PaymentEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PaymentMapper {
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "id", target = "orderId")
    @Mapping(target = "id", ignore = true)
    Payment toPayment(OrderEvent event);
    PaymentEvent toPaymentEvent(Payment payment);
}
