package com.huynqb.laundrylockerbackend.module.loyalty.mapper;

import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.LoyaltyAccountResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.PointTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.StampCardResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.StampTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.model.LoyaltyAccount;
import com.huynqb.laundrylockerbackend.module.loyalty.model.PointTransaction;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampCard;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampTransaction;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
    componentModel = "spring",
    imports = {BigDecimal.class})
public interface LoyaltyMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userName", source = "account", qualifiedByName = "mapUserName")
  @Mapping(
      target = "pointsValueVnd",
      expression = "java(BigDecimal.valueOf(account.getPointsBalance()))")
  LoyaltyAccountResponse toAccountResponse(LoyaltyAccount account);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "orderId", source = "order.id")
  PointTransactionResponse toPointTransactionResponse(PointTransaction transaction);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "serviceId", source = "service.id")
  @Mapping(target = "serviceName", source = "service.name")
  @Mapping(target = "progressPercentage", source = "card", qualifiedByName = "calculateProgress")
  StampCardResponse toStampCardResponse(StampCard card);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "stampCardId", source = "stampCard.id")
  @Mapping(target = "orderId", source = "order.id")
  StampTransactionResponse toStampTransactionResponse(StampTransaction transaction);

  @Named("mapUserName")
  default String mapUserName(LoyaltyAccount account) {
    if (account.getUser() == null) return null;
    if (account.getUser().getFirstName() != null) {
      return account.getUser().getFirstName() + " " + account.getUser().getLastName();
    }
    return account.getUser().getName();
  }

  @Named("calculateProgress")
  default int calculateProgress(StampCard card) {
    if (card.getStampsRequired() > 0) {
      return (int) ((card.getCurrentStamps() * 100.0) / card.getStampsRequired());
    }
    return 0;
  }
}
