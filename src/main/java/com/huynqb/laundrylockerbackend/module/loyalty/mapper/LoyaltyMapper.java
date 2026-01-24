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
import org.springframework.stereotype.Component;

@Component
public class LoyaltyMapper {

  public LoyaltyAccountResponse toAccountResponse(LoyaltyAccount account) {
    if (account == null) return null;

    String userName = null;
    if (account.getUser() != null) {
      userName =
          account.getUser().getFirstName() != null
              ? account.getUser().getFirstName() + " " + account.getUser().getLastName()
              : account.getUser().getName();
    }

    return LoyaltyAccountResponse.builder()
        .id(account.getId())
        .userId(account.getUser() != null ? account.getUser().getId() : null)
        .userName(userName)
        .pointsBalance(account.getPointsBalance())
        .pointsValueVnd(BigDecimal.valueOf(account.getPointsBalance())) // 1 point = 1 VND
        .totalPointsEarned(account.getTotalPointsEarned())
        .totalPointsRedeemed(account.getTotalPointsRedeemed())
        .totalAmountSpent(account.getTotalAmountSpent())
        .createdAt(account.getCreatedAt())
        .updatedAt(account.getUpdatedAt())
        .build();
  }

  public PointTransactionResponse toPointTransactionResponse(PointTransaction transaction) {
    if (transaction == null) return null;

    return PointTransactionResponse.builder()
        .id(transaction.getId())
        .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
        .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
        .type(transaction.getType())
        .points(transaction.getPoints())
        .relatedAmount(transaction.getRelatedAmount())
        .balanceAfter(transaction.getBalanceAfter())
        .description(transaction.getDescription())
        .referenceId(transaction.getReferenceId())
        .createdAt(transaction.getCreatedAt())
        .build();
  }

  public StampCardResponse toStampCardResponse(StampCard card) {
    if (card == null) return null;

    int progressPercentage = 0;
    if (card.getStampsRequired() > 0) {
      progressPercentage = (int) ((card.getCurrentStamps() * 100.0) / card.getStampsRequired());
    }

    String serviceName = null;
    if (card.getService() != null) {
      serviceName = card.getService().getName();
    }

    return StampCardResponse.builder()
        .id(card.getId())
        .userId(card.getUser() != null ? card.getUser().getId() : null)
        .stampType(card.getStampType())
        .serviceId(card.getService() != null ? card.getService().getId() : null)
        .serviceName(serviceName)
        .boxSize(card.getBoxSize())
        .stampsRequired(card.getStampsRequired())
        .currentStamps(card.getCurrentStamps())
        .freeRewardsAvailable(card.getFreeRewardsAvailable())
        .totalStampsEarned(card.getTotalStampsEarned())
        .totalRewardsRedeemed(card.getTotalRewardsRedeemed())
        .progressPercentage(progressPercentage)
        .createdAt(card.getCreatedAt())
        .updatedAt(card.getUpdatedAt())
        .build();
  }

  public StampTransactionResponse toStampTransactionResponse(StampTransaction transaction) {
    if (transaction == null) return null;

    return StampTransactionResponse.builder()
        .id(transaction.getId())
        .userId(transaction.getUser() != null ? transaction.getUser().getId() : null)
        .stampCardId(transaction.getStampCard() != null ? transaction.getStampCard().getId() : null)
        .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
        .type(transaction.getType())
        .stamps(transaction.getStamps())
        .stampsAfter(transaction.getStampsAfter())
        .rewardsAfter(transaction.getRewardsAfter())
        .discountApplied(transaction.getDiscountApplied())
        .description(transaction.getDescription())
        .createdAt(transaction.getCreatedAt())
        .build();
  }
}
