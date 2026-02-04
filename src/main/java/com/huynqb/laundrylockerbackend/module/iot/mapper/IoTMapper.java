package com.huynqb.laundrylockerbackend.module.iot.mapper;

import com.huynqb.laundrylockerbackend.module.iot.dto.response.PickupResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.UnlockBoxResponse;
import com.huynqb.laundrylockerbackend.module.iot.dto.response.VerifyPinResponse;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for IoT-specific DTOs. */
@Mapper(componentModel = "spring")
public interface IoTMapper {

  // ===== VerifyPinResponse mappings =====

  /** Create error verify response. */
  default VerifyPinResponse toVerifyError(Long boxId, String message) {
    return VerifyPinResponse.builder().valid(false).boxId(boxId).message(message).build();
  }

  /** Create success verify response. */
  @Mapping(target = "valid", constant = "true")
  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "boxId", source = "box.id")
  @Mapping(target = "boxNumber", source = "box.boxNumber")
  @Mapping(target = "lockerCode", source = "box.locker.code")
  @Mapping(target = "orderStatus", expression = "java(order.getStatus().name())")
  @Mapping(target = "message", constant = "PIN verified successfully")
  VerifyPinResponse toVerifySuccess(Order order, Box box);

  // ===== UnlockBoxResponse mappings =====

  /** Create error unlock response. */
  default UnlockBoxResponse toUnlockError(Long boxId, String message) {
    return UnlockBoxResponse.builder().success(false).boxId(boxId).message(message).build();
  }

  /** Create success unlock response. */
  @Mapping(target = "success", constant = "true")
  @Mapping(target = "boxId", source = "box.id")
  @Mapping(target = "boxNumber", source = "box.boxNumber")
  @Mapping(target = "lockerCode", source = "box.locker.code")
  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "unlockToken", source = "unlockToken")
  @Mapping(target = "unlockTimestamp", expression = "java(System.currentTimeMillis())")
  @Mapping(target = "message", constant = "Box unlocked successfully")
  UnlockBoxResponse toUnlockSuccess(Order order, Box box, String unlockToken);

  // ===== PickupResponse mappings =====

  /** Create error pickup response. */
  default PickupResponse toPickupError(Long orderId, String orderStatus, String message) {
    return PickupResponse.builder()
        .success(false)
        .orderId(orderId)
        .orderStatus(orderStatus)
        .message(message)
        .build();
  }

  /** Create success pickup response. */
  default PickupResponse toPickupSuccess(
      Long orderId, String status, LocalDateTime completedAt, String message) {
    return PickupResponse.builder()
        .success(true)
        .orderId(orderId)
        .orderStatus(status)
        .completedAt(completedAt)
        .message(message)
        .build();
  }
}
