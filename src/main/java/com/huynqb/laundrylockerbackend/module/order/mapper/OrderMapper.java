package com.huynqb.laundrylockerbackend.module.order.mapper;

import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderDetailResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for Order entities to DTOs. Follows DRY principle by centralizing mapping. */
@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "senderId", source = "sender.id")
  @Mapping(target = "senderName", source = "sender.name")
  @Mapping(target = "senderPhone", source = "sender.phoneNumber")
  @Mapping(target = "receiverId", source = "receiver.id")
  @Mapping(target = "receiverName", source = "receiver.name")
  @Mapping(target = "lockerId", source = "locker.id")
  @Mapping(target = "lockerName", source = "locker.name")
  @Mapping(target = "lockerCode", source = "locker.code")
  @Mapping(target = "sendBoxNumber", source = "sendBox.boxNumber")
  @Mapping(target = "receiveBoxNumber", source = "receiveBox.boxNumber")
  @Mapping(target = "staffId", source = "staff.id")
  @Mapping(target = "staffName", source = "staff.name")
  OrderResponse toResponse(Order order);

  List<OrderResponse> toResponseList(List<Order> orders);

  @Mapping(target = "serviceId", source = "service.id")
  @Mapping(target = "serviceName", source = "service.name")
  @Mapping(target = "serviceImage", source = "service.image")
  @Mapping(target = "unit", source = "service.unit")
  OrderDetailResponse toDetailResponse(OrderDetail detail);

  List<OrderDetailResponse> toDetailResponseList(List<OrderDetail> details);
}
