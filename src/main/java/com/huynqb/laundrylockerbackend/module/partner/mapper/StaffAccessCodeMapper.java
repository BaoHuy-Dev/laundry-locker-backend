package com.huynqb.laundrylockerbackend.module.partner.mapper;

import com.huynqb.laundrylockerbackend.module.locker.model.Box;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffAccessCodeResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffCodeUnlockResponse;
import com.huynqb.laundrylockerbackend.module.partner.model.StaffAccessCode;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for StaffAccessCode entity. Follows DRY principle by centralizing all mapping
 * logic.
 */
@Mapper(componentModel = "spring")
public interface StaffAccessCodeMapper {

  @Mapping(target = "orderId", source = "order.id")
  @Mapping(target = "partnerId", source = "partner.id")
  @Mapping(target = "orderLockerCode", source = "order.locker.code")
  @Mapping(target = "orderLockerName", source = "order.locker.name")
  @Mapping(target = "orderBoxNumbers", source = "order", qualifiedByName = "orderToBoxNumbers")
  @Mapping(target = "customerName", source = "order.sender", qualifiedByName = "userToFullName")
  StaffAccessCodeResponse toResponse(StaffAccessCode accessCode);

  List<StaffAccessCodeResponse> toResponseList(List<StaffAccessCode> accessCodes);

  @Named("orderToBoxNumbers")
  default String orderToBoxNumbers(Order order) {
    if (order == null) {
      return "";
    }
    Set<Box> boxes = order.getSendBoxes();
    if (boxes != null && !boxes.isEmpty()) {
      return boxes.stream()
          .map(box -> String.valueOf(box.getBoxNumber()))
          .collect(Collectors.joining(", "));
    } else if (order.getSendBox() != null) {
      return String.valueOf(order.getSendBox().getBoxNumber());
    }
    return "";
  }

  @Named("userToFullName")
  default String userToFullName(User user) {
    if (user == null) {
      return null;
    }
    if (user.getFirstName() != null || user.getLastName() != null) {
      String fullName =
          (user.getFirstName() != null ? user.getFirstName() : "")
              + " "
              + (user.getLastName() != null ? user.getLastName() : "");
      return fullName.trim();
    }
    return user.getName();
  }

  /** Map Box to BoxInfo for unlock response. */
  @Mapping(target = "boxId", source = "id")
  @Mapping(target = "boxNumber", source = "boxNumber", qualifiedByName = "intToString")
  @Mapping(target = "size", source = "size", qualifiedByName = "enumToString")
  StaffCodeUnlockResponse.BoxInfo toBoxInfo(Box box);

  List<StaffCodeUnlockResponse.BoxInfo> toBoxInfoList(List<Box> boxes);

  @Named("intToString")
  default String intToString(Integer value) {
    return value != null ? String.valueOf(value) : null;
  }

  @Named("enumToString")
  default String enumToString(Enum<?> value) {
    return value != null ? value.name() : null;
  }
}
