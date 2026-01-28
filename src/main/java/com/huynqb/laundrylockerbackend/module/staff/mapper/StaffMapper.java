package com.huynqb.laundrylockerbackend.module.staff.mapper;

import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.staff.dto.response.StaffOrderSummaryResponse;
import com.huynqb.laundrylockerbackend.module.staff.dto.response.StaffUnlockBoxResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for Staff-specific DTOs. */
@Mapper(componentModel = "spring")
public interface StaffMapper {

  /** Create StaffOrderSummaryResponse from counts and recent orders. */
  default StaffOrderSummaryResponse toOrderSummaryResponse(
      long waitingCount,
      long processingCount,
      long collectedCount,
      long readyCount,
      List<OrderResponse> recentOrders) {
    return StaffOrderSummaryResponse.builder()
        .waitingCount(waitingCount)
        .processingCount(processingCount)
        .collectedCount(collectedCount)
        .readyCount(readyCount)
        .recentOrders(recentOrders)
        .build();
  }

  /** Create successful unlock response from box. */
  @Mapping(target = "success", constant = "true")
  @Mapping(target = "boxId", source = "box.id")
  @Mapping(target = "boxNumber", source = "box.boxNumber")
  @Mapping(target = "lockerCode", source = "box.locker.code")
  @Mapping(target = "lockerName", source = "box.locker.name")
  @Mapping(target = "orderId", source = "orderId")
  @Mapping(target = "unlockToken", source = "unlockToken")
  @Mapping(target = "unlockTimestamp", expression = "java(System.currentTimeMillis())")
  @Mapping(target = "message", constant = "Box unlocked successfully")
  StaffUnlockBoxResponse toSuccessUnlockResponse(Box box, Long orderId, String unlockToken);

  /** Create error unlock response. */
  default StaffUnlockBoxResponse toErrorUnlockResponse(Long boxId, String message) {
    return StaffUnlockBoxResponse.builder().success(false).boxId(boxId).message(message).build();
  }
}
