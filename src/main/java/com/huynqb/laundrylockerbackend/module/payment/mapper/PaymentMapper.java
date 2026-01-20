package com.huynqb.laundrylockerbackend.module.payment.mapper;

import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for Payment entities to DTOs. */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "customerId", source = "customer.id")
  @Mapping(target = "customerName", source = "customer.name")
  PaymentResponse toResponse(Payment payment);

  List<PaymentResponse> toResponseList(List<Payment> payments);
}
