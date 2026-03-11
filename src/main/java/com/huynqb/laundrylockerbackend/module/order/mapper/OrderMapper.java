package com.huynqb.laundrylockerbackend.module.order.mapper;

import com.huynqb.laundrylockerbackend.module.laundry.enums.ServiceCategory;
import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderDetailResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.enums.PricingType;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.model.OrderDetail;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** MapStruct mapper for Order entities to DTOs. Follows DRY principle by centralizing mapping. */
@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "orderCode", source = "orderCode")
  @Mapping(target = "senderId", source = "sender.id")
  @Mapping(target = "senderName", source = "sender.name")
  @Mapping(target = "senderPhone", source = "sender.phoneNumber")
  @Mapping(target = "receiverId", source = "receiver.id")
  @Mapping(target = "receiverName", source = "order", qualifiedByName = "mapReceiverName")
  @Mapping(target = "receiverPhone", source = "order", qualifiedByName = "mapReceiverPhone")
  @Mapping(target = "lockerId", source = "locker.id")
  @Mapping(target = "lockerName", source = "locker.name")
  @Mapping(target = "lockerCode", source = "locker.code")
  @Mapping(target = "sendBoxNumber", source = "sendBox.boxNumber")
  @Mapping(target = "receiveBoxNumber", source = "receiveBox.boxNumber")
  @Mapping(target = "sendBoxId", source = "sendBox.id")
  @Mapping(target = "receiveBoxId", source = "receiveBox.id")
  @Mapping(target = "sendBoxNumbers", source = "sendBoxes", qualifiedByName = "boxesToNumbers")
  @Mapping(
      target = "receiveBoxNumbers",
      source = "receiveBoxes",
      qualifiedByName = "boxesToNumbers")
  @Mapping(target = "staffId", source = "staff.id")
  @Mapping(target = "staffName", source = "staff.name")
  @Mapping(target = "serviceCategory", source = "serviceCategory")
  @Mapping(target = "pricingType", source = "order", qualifiedByName = "mapPricingType")
  @Mapping(target = "returnedAt", source = "returnedAt")
  @Mapping(target = "pickupDeadline", source = "pickupDeadline")
  @Mapping(target = "isOvertime", source = "order", qualifiedByName = "isOvertime")
  @Mapping(target = "overtimeHours", source = "order", qualifiedByName = "calculateOvertimeHours")
  @Mapping(target = "isPaid", source = "order", qualifiedByName = "isPaid")
  @Mapping(target = "paymentRequired", source = "order", qualifiedByName = "isPaymentRequired")
  @Mapping(target = "nextAction", source = "order", qualifiedByName = "getNextAction")
  @Mapping(target = "nextActionMessage", source = "order", qualifiedByName = "getNextActionMessage")
  // ===== PROMOTION MAPPINGS =====
  @Mapping(target = "promotionCode", source = "promotionCode")
  @Mapping(
      target = "appliedPromotionCodes",
      source = "order",
      qualifiedByName = "mapAppliedPromotionCodes")
  @Mapping(target = "originalPrice", source = "originalPrice")
  @Mapping(target = "promotionDiscount", source = "discount")
  // ===== ORDER DETAILS MAPPING =====
  @Mapping(target = "orderDetails", source = "orderDetails")
  @Mapping(target = "intendedReceiveAt", source = "intendedReceiveAt")
  OrderResponse toResponse(Order order);

  List<OrderResponse> toResponseList(List<Order> orders);

  @Mapping(target = "serviceId", source = "service.id")
  @Mapping(target = "serviceName", source = "service.name")
  @Mapping(target = "serviceImage", source = "service.image")
  @Mapping(target = "unit", source = "service.unit")
  @Mapping(target = "price", source = "price")
  OrderDetailResponse toDetailResponse(OrderDetail detail);

  List<OrderDetailResponse> toDetailResponseList(List<OrderDetail> details);

  @Named("boxesToNumbers")
  default Set<Integer> boxesToNumbers(Set<Box> boxes) {
    if (boxes == null || boxes.isEmpty()) {
      return null;
    }
    return boxes.stream().map(Box::getBoxNumber).collect(Collectors.toSet());
  }

  @Named("mapAppliedPromotionCodes")
  default List<String> mapAppliedPromotionCodes(Order order) {
    if (order.getAppliedPromotionCodes() == null || order.getAppliedPromotionCodes().isBlank()) {
      return null;
    }
    return Arrays.asList(order.getAppliedPromotionCodes().split(","));
  }

  @Named("mapReceiverName")
  default String mapReceiverName(Order order) {
    // Ưu tiên lấy từ field receiverName, nếu không có thì lấy từ receiver
    if (order.getReceiverName() != null) {
      return order.getReceiverName();
    }
    if (order.getReceiver() != null) {
      return order.getReceiver().getName();
    }
    return null;
  }

  @Named("mapReceiverPhone")
  default String mapReceiverPhone(Order order) {
    // Ưu tiên lấy từ field receiverPhone, nếu không có thì lấy từ receiver
    if (order.getReceiverPhone() != null) {
      return order.getReceiverPhone();
    }
    if (order.getReceiver() != null) {
      return order.getReceiver().getPhoneNumber();
    }
    return null;
  }

  @Named("mapPricingType")
  default PricingType mapPricingType(Order order) {
    if (order.getServiceCategory() == null) {
      return null;
    }
    return order.getServiceCategory() == ServiceCategory.STORAGE
        ? PricingType.FIXED
        : PricingType.PER_WEIGHT;
  }

  @Named("isOvertime")
  default Boolean isOvertime(Order order) {
    if (order.getPickupDeadline() == null || order.getStatus() == OrderStatus.COMPLETED) {
      return false;
    }
    return LocalDateTime.now().isAfter(order.getPickupDeadline());
  }

  @Named("calculateOvertimeHours")
  default Integer calculateOvertimeHours(Order order) {
    if (order.getPickupDeadline() == null) {
      return 0;
    }
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(order.getPickupDeadline())) {
      return 0;
    }
    return (int) ChronoUnit.HOURS.between(order.getPickupDeadline(), now);
  }

  @Named("isPaid")
  default Boolean isPaid(Order order) {
    // STORAGE: Thanh toán trước khi gửi
    // LAUNDRY: Thanh toán khi nhận
    if (order.getServiceCategory() == ServiceCategory.STORAGE) {
      return order.getStatus() != OrderStatus.INITIALIZED;
    }
    return order.getStatus() == OrderStatus.COMPLETED;
  }

  @Named("isPaymentRequired")
  default Boolean isPaymentRequired(Order order) {
    // STORAGE: Yêu cầu thanh toán sau khi tạo (INITIALIZED)
    if (order.getServiceCategory() == ServiceCategory.STORAGE) {
      return order.getStatus() == OrderStatus.INITIALIZED;
    }
    // LAUNDRY: Yêu cầu thanh toán khi RETURNED
    return order.getStatus() == OrderStatus.RETURNED;
  }

  @Named("getNextAction")
  default String getNextAction(Order order) {
    return switch (order.getStatus()) {
      case INITIALIZED ->
          order.getServiceCategory() == ServiceCategory.STORAGE ? "PAY_AND_DROP" : "DROP_ITEMS";
      case WAITING -> "WAIT_FOR_STAFF";
      case COLLECTED -> "PROCESSING";
      case PROCESSING -> "WAIT_FOR_READY";
      case READY -> "WAIT_FOR_RETURN";
      case RETURNED ->
          order.getServiceCategory() == ServiceCategory.STORAGE ? "PICKUP" : "PAY_AND_PICKUP";
      case COMPLETED -> "DONE";
      case CANCELED -> "CANCELED";
      default -> "UNKNOWN";
    };
  }

  @Named("getNextActionMessage")
  default String getNextActionMessage(Order order) {
    return switch (order.getStatus()) {
      case INITIALIZED ->
          order.getServiceCategory() == ServiceCategory.STORAGE
              ? "Vui lòng thanh toán và đặt đồ vào locker"
              : "Vui lòng đặt đồ vào locker";
      case WAITING -> "Đang chờ nhân viên lấy đồ";
      case COLLECTED -> "Đồ đã được lấy, đang chuẩn bị xử lý";
      case PROCESSING -> "Đồ đang được giặt";
      case READY -> "Đồ đã giặt xong, đang chờ trả về locker";
      case RETURNED ->
          order.getServiceCategory() == ServiceCategory.STORAGE
              ? "Đồ đã được trả, vui lòng đến lấy"
              : "Đồ đã được trả, vui lòng thanh toán và lấy đồ";
      case COMPLETED -> "Đơn hàng hoàn tất";
      case CANCELED -> "Đơn hàng đã bị hủy";
      default -> "";
    };
  }
}
