package com.huynqb.laundrylockerbackend.module.order.mapper;

import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderDetailResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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
  @Mapping(target = "sendBoxNumbers", source = "sendBoxes", qualifiedByName = "boxesToNumbers")
  @Mapping(
      target = "receiveBoxNumbers",
      source = "receiveBoxes",
      qualifiedByName = "boxesToNumbers")
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

  @Named("boxesToNumbers")
  default Set<Integer> boxesToNumbers(Set<Box> boxes) {
    if (boxes == null || boxes.isEmpty()) {
      return null;
    }
    return boxes.stream().map(Box::getBoxNumber).collect(Collectors.toSet());
  }
}
