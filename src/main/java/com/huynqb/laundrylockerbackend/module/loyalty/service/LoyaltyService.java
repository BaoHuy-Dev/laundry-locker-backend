package com.huynqb.laundrylockerbackend.module.loyalty.service;

import com.huynqb.laundrylockerbackend.module.loyalty.dto.request.AdjustPointsRequest;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.request.RedeemPointsRequest;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.request.RedeemStampRequest;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.LoyaltyAccountResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.LoyaltySummaryResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.PointTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.StampCardResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.dto.response.StampTransactionResponse;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.PointTransactionType;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampTransactionType;
import com.huynqb.laundrylockerbackend.module.loyalty.enums.StampType;
import com.huynqb.laundrylockerbackend.module.loyalty.exception.LoyaltyException;
import com.huynqb.laundrylockerbackend.module.loyalty.mapper.LoyaltyMapper;
import com.huynqb.laundrylockerbackend.module.loyalty.model.LoyaltyAccount;
import com.huynqb.laundrylockerbackend.module.loyalty.model.PointTransaction;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampCard;
import com.huynqb.laundrylockerbackend.module.loyalty.model.StampTransaction;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.LoyaltyAccountRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.PointTransactionRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.StampCardRepository;
import com.huynqb.laundrylockerbackend.module.loyalty.repository.StampTransactionRepository;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loyalty Service for managing points and stamps.
 *
 * <p>Points System: - Earn: 10,000 VND spent = 1 point - Redeem: 1 point = 1 VND discount (so
 * 10,000 points = 10,000 VND discount)
 *
 * <p>Stamp System: - Use 6 times = get 1 free
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyService {

  private final LoyaltyAccountRepository loyaltyAccountRepository;
  private final PointTransactionRepository pointTransactionRepository;
  private final StampCardRepository stampCardRepository;
  private final StampTransactionRepository stampTransactionRepository;
  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final LoyaltyMapper loyaltyMapper;

  // Points configuration
  private static final BigDecimal VND_PER_POINT_EARN =
      new BigDecimal("10000"); // 10,000 VND = 1 point
  private static final BigDecimal VND_PER_POINT_REDEEM = BigDecimal.ONE; // 1 point = 1 VND

  // Stamps configuration
  private static final int STAMPS_REQUIRED_FOR_REWARD = 6;

  // ===== Loyalty Account =====

  /** Get or create loyalty account for user. */
  @Transactional
  public LoyaltyAccount getOrCreateAccount(Long userId) {
    return loyaltyAccountRepository.findByUserId(userId).orElseGet(() -> createAccount(userId));
  }

  private LoyaltyAccount createAccount(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY001", "User not found"));

    LoyaltyAccount account =
        LoyaltyAccount.builder()
            .user(user)
            .pointsBalance(0L)
            .totalPointsEarned(0L)
            .totalPointsRedeemed(0L)
            .totalAmountSpent(BigDecimal.ZERO)
            .build();

    return loyaltyAccountRepository.save(account);
  }

  /** Get loyalty account response. */
  @Transactional(readOnly = true)
  public LoyaltyAccountResponse getAccountResponse(Long userId) {
    LoyaltyAccount account = getOrCreateAccount(userId);
    return loyaltyMapper.toAccountResponse(account);
  }

  /** Get loyalty summary (points + all stamp cards). */
  @Transactional(readOnly = true)
  public LoyaltySummaryResponse getLoyaltySummary(Long userId) {
    LoyaltyAccount account = getOrCreateAccount(userId);
    List<StampCard> stampCards = stampCardRepository.findByUserId(userId);

    int totalFreeRewards = stampCards.stream().mapToInt(StampCard::getFreeRewardsAvailable).sum();

    BigDecimal totalRedeemableValue =
        BigDecimal.valueOf(account.getPointsBalance()).multiply(VND_PER_POINT_REDEEM);

    return loyaltyMapper.toSummaryResponse(
        account, stampCards, totalRedeemableValue, totalFreeRewards);
  }

  // ===== Points Operations =====

  /** Earn points from completed order. Called when payment is successful. */
  @Transactional
  public PointTransactionResponse earnPointsFromOrder(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY002", "Order not found"));

    Long userId = order.getSender().getId();
    LoyaltyAccount account =
        loyaltyAccountRepository
            .findByUserIdForUpdate(userId)
            .orElseGet(() -> createAccount(userId));

    // Calculate points: 10,000 VND = 1 point
    BigDecimal orderAmount = order.getTotalPrice();
    long pointsEarned = orderAmount.divide(VND_PER_POINT_EARN, 0, RoundingMode.DOWN).longValue();

    if (pointsEarned <= 0) {
      log.info("Order {} amount {} is too small to earn points", orderId, orderAmount);
      return null;
    }

    // Update account
    account.addPoints(pointsEarned);
    account.setTotalAmountSpent(account.getTotalAmountSpent().add(orderAmount));
    loyaltyAccountRepository.save(account);

    // Create transaction
    PointTransaction transaction =
        PointTransaction.builder()
            .user(order.getSender())
            .order(order)
            .type(PointTransactionType.EARN)
            .points(pointsEarned)
            .relatedAmount(orderAmount)
            .balanceAfter(account.getPointsBalance())
            .description("Earned from order #" + orderId)
            .build();
    pointTransactionRepository.save(transaction);

    log.info("User {} earned {} points from order {}", userId, pointsEarned, orderId);
    return loyaltyMapper.toPointTransactionResponse(transaction);
  }

  /** Redeem points for discount on order. */
  @Transactional
  public PointTransactionResponse redeemPoints(Long userId, RedeemPointsRequest request) {
    LoyaltyAccount account =
        loyaltyAccountRepository
            .findByUserIdForUpdate(userId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY003", "Loyalty account not found"));

    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY002", "Order not found"));

    // Validate ownership
    if (!order.getSender().getId().equals(userId)) {
      throw new LoyaltyException("E_LOYALTY004", "You don't own this order");
    }

    Long pointsToRedeem = request.getPointsToRedeem();

    // Check balance
    if (!account.hasEnoughPoints(pointsToRedeem)) {
      throw new LoyaltyException("E_LOYALTY005", "Insufficient points balance");
    }

    // Calculate discount value (1 point = 1 VND)
    BigDecimal discountValue = BigDecimal.valueOf(pointsToRedeem).multiply(VND_PER_POINT_REDEEM);

    // Check if discount exceeds order total
    if (discountValue.compareTo(order.getTotalPrice()) > 0) {
      throw new LoyaltyException("E_LOYALTY006", "Discount exceeds order total");
    }

    // Redeem points
    account.redeemPoints(pointsToRedeem);
    loyaltyAccountRepository.save(account);

    // Apply discount to order
    order.setDiscount(order.getDiscount().add(discountValue));
    order.setTotalPrice(order.getTotalPrice().subtract(discountValue));
    orderRepository.save(order);

    // Create transaction
    PointTransaction transaction =
        PointTransaction.builder()
            .user(account.getUser())
            .order(order)
            .type(PointTransactionType.REDEEM)
            .points(-pointsToRedeem)
            .relatedAmount(discountValue)
            .balanceAfter(account.getPointsBalance())
            .description("Redeemed for order #" + order.getId())
            .build();
    pointTransactionRepository.save(transaction);

    log.info(
        "User {} redeemed {} points for {} VND discount on order {}",
        userId,
        pointsToRedeem,
        discountValue,
        order.getId());

    return loyaltyMapper.toPointTransactionResponse(transaction);
  }

  /** Admin adjust points (add/subtract). */
  @Transactional
  public PointTransactionResponse adjustPoints(AdjustPointsRequest request) {
    LoyaltyAccount account =
        loyaltyAccountRepository
            .findByUserIdForUpdate(request.getUserId())
            .orElseGet(() -> createAccount(request.getUserId()));

    Long points = request.getPoints();

    if (points > 0) {
      account.addPoints(points);
    } else {
      if (!account.hasEnoughPoints(-points)) {
        throw new LoyaltyException("E_LOYALTY005", "Cannot reduce more points than balance");
      }
      account.redeemPoints(-points);
    }
    loyaltyAccountRepository.save(account);

    PointTransaction transaction =
        PointTransaction.builder()
            .user(account.getUser())
            .type(PointTransactionType.ADJUST)
            .points(points)
            .balanceAfter(account.getPointsBalance())
            .description(request.getReason() != null ? request.getReason() : "Admin adjustment")
            .build();
    pointTransactionRepository.save(transaction);

    log.info("Admin adjusted {} points for user {}", points, request.getUserId());
    return loyaltyMapper.toPointTransactionResponse(transaction);
  }

  /** Get point transaction history. */
  @Transactional(readOnly = true)
  public Page<PointTransactionResponse> getPointHistory(Long userId, Pageable pageable) {
    return pointTransactionRepository
        .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(loyaltyMapper::toPointTransactionResponse);
  }

  /** Get point transaction history (alias for controller). */
  @Transactional(readOnly = true)
  public Page<PointTransactionResponse> getPointTransactionHistory(Long userId, Pageable pageable) {
    return getPointHistory(userId, pageable);
  }

  /** Get all stamp cards for user (for controller). */
  @Transactional(readOnly = true)
  public List<StampCardResponse> getUserStampCards(Long userId) {
    return getStampCards(userId);
  }

  /** Get specific stamp card response with validation. */
  @Transactional(readOnly = true)
  public StampCardResponse getStampCardResponse(Long userId, Long stampCardId) {
    StampCard card =
        stampCardRepository
            .findById(stampCardId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY007", "Stamp card not found"));

    if (!card.getUser().getId().equals(userId)) {
      throw new LoyaltyException("E_LOYALTY008", "This is not your stamp card");
    }

    return loyaltyMapper.toStampCardResponse(card);
  }

  /** Redeem stamp reward using RedeemStampRequest. */
  @Transactional
  public StampTransactionResponse redeemStampReward(Long userId, RedeemStampRequest request) {
    StampCard card =
        stampCardRepository
            .findById(request.getStampCardId())
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY007", "Stamp card not found"));

    if (!card.getUser().getId().equals(userId)) {
      throw new LoyaltyException("E_LOYALTY008", "This is not your stamp card");
    }

    if (!card.hasRewardAvailable()) {
      throw new LoyaltyException("E_LOYALTY009", "No rewards available");
    }

    Order order =
        orderRepository
            .findById(request.getOrderId())
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY002", "Order not found"));

    if (!order.getSender().getId().equals(userId)) {
      throw new LoyaltyException("E_LOYALTY004", "You don't own this order");
    }

    // Calculate discount based on stamp type
    BigDecimal discountValue = calculateStampDiscount(card, order);

    // Redeem reward
    card.redeemReward();
    stampCardRepository.save(card);

    // Apply discount
    order.setDiscount(order.getDiscount().add(discountValue));
    order.setTotalPrice(order.getTotalPrice().subtract(discountValue));
    orderRepository.save(order);

    // Create transaction
    StampTransaction transaction =
        StampTransaction.builder()
            .user(card.getUser())
            .stampCard(card)
            .order(order)
            .type(StampTransactionType.REDEEM)
            .stamps(0)
            .stampsAfter(card.getCurrentStamps())
            .rewardsAfter(card.getFreeRewardsAvailable())
            .discountApplied(discountValue)
            .description("Redeemed reward for order #" + order.getId())
            .build();
    stampTransactionRepository.save(transaction);

    log.info(
        "User {} redeemed stamp reward for {} VND on order {}",
        userId,
        discountValue,
        order.getId());

    return loyaltyMapper.toStampTransactionResponse(transaction);
  }

  /** Get loyalty statistics for admin dashboard. */
  @Transactional(readOnly = true)
  public Map<String, Object> getLoyaltyStatistics() {
    Map<String, Object> stats = new HashMap<>();

    // Total accounts
    long totalAccounts = loyaltyAccountRepository.count();
    stats.put("totalAccounts", totalAccounts);

    // Total points in circulation
    Long totalPointsBalance = loyaltyAccountRepository.sumAllPointsBalance();
    stats.put("totalPointsInCirculation", totalPointsBalance != null ? totalPointsBalance : 0);

    // Total points earned all time
    Long totalPointsEarned = loyaltyAccountRepository.sumAllPointsEarned();
    stats.put("totalPointsEarned", totalPointsEarned != null ? totalPointsEarned : 0);

    // Total points redeemed
    Long totalPointsRedeemed = loyaltyAccountRepository.sumAllPointsRedeemed();
    stats.put("totalPointsRedeemed", totalPointsRedeemed != null ? totalPointsRedeemed : 0);

    // Total stamp cards
    long totalStampCards = stampCardRepository.count();
    stats.put("totalStampCards", totalStampCards);

    // Total rewards redeemed
    Integer totalRewardsRedeemed = stampCardRepository.sumAllRewardsRedeemed();
    stats.put("totalRewardsRedeemed", totalRewardsRedeemed != null ? totalRewardsRedeemed : 0);

    return stats;
  }

  // ===== Stamp Operations =====

  /** Get or create stamp card for user. */
  @Transactional
  public StampCard getOrCreateStampCard(
      Long userId, StampType type, String boxSize, Long serviceId) {
    if (type == StampType.BOX) {
      return stampCardRepository
          .findByUserIdAndStampTypeAndBoxSize(userId, type, boxSize)
          .orElseGet(() -> createStampCard(userId, type, boxSize, null));
    } else {
      return stampCardRepository
          .findByUserIdAndStampTypeAndServiceId(userId, type, serviceId)
          .orElseGet(() -> createStampCard(userId, type, null, serviceId));
    }
  }

  private StampCard createStampCard(Long userId, StampType type, String boxSize, Long serviceId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY001", "User not found"));

    StampCard.StampCardBuilder builder =
        StampCard.builder().user(user).stampType(type).stampsRequired(STAMPS_REQUIRED_FOR_REWARD);

    if (type == StampType.BOX) {
      builder.boxSize(boxSize);
    } else if (serviceId != null) {
      // Service will be set later when we have service repository
      // For now, just store the reference
    }

    return stampCardRepository.save(builder.build());
  }

  /** Earn stamps from order completion. Called when payment is successful. */
  @Transactional
  public List<StampCardResponse> earnStampsFromOrder(Long orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY002", "Order not found"));

    Long userId = order.getSender().getId();
    User user = order.getSender();

    // Earn box stamps based on boxes used
    // Get box sizes from order
    java.util.Set<String> boxSizes = new java.util.HashSet<>();

    if (order.getSendBoxes() != null) {
      order
          .getSendBoxes()
          .forEach(
              box -> {
                if (box.getSize() != null) {
                  boxSizes.add(box.getSize().name());
                }
              });
    }
    if (order.getSendBox() != null && order.getSendBox().getSize() != null) {
      boxSizes.add(order.getSendBox().getSize().name());
    }

    for (String boxSize : boxSizes) {
      StampCard card =
          stampCardRepository
              .findByUserIdAndStampTypeAndBoxSizeForUpdate(userId, StampType.BOX, boxSize)
              .orElseGet(() -> createStampCard(userId, StampType.BOX, boxSize, null));

      boolean earnedReward = card.addStamp();
      stampCardRepository.save(card);

      // Create transaction
      StampTransaction transaction =
          StampTransaction.builder()
              .user(user)
              .stampCard(card)
              .order(order)
              .type(StampTransactionType.EARN)
              .stamps(1)
              .stampsAfter(card.getCurrentStamps())
              .rewardsAfter(card.getFreeRewardsAvailable())
              .description("Earned from order #" + orderId + " (Box " + boxSize + ")")
              .build();
      stampTransactionRepository.save(transaction);

      if (earnedReward) {
        log.info("User {} earned a FREE REWARD for box size {}!", userId, boxSize);
      }
    }

    // Return updated stamp cards
    return stampCardRepository.findByUserId(userId).stream()
        .map(loyaltyMapper::toStampCardResponse)
        .collect(Collectors.toList());
  }

  /** Redeem stamp reward for free box/service. */
  @Transactional
  public StampCardResponse redeemStampReward(Long userId, Long stampCardId, Long orderId) {
    StampCard card =
        stampCardRepository
            .findById(stampCardId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY007", "Stamp card not found"));

    if (!card.getUser().getId().equals(userId)) {
      throw new LoyaltyException("E_LOYALTY008", "This is not your stamp card");
    }

    if (!card.hasRewardAvailable()) {
      throw new LoyaltyException("E_LOYALTY009", "No rewards available");
    }

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new LoyaltyException("E_LOYALTY002", "Order not found"));

    // Calculate discount based on stamp type
    BigDecimal discountValue = calculateStampDiscount(card, order);

    // Redeem reward
    card.redeemReward();
    stampCardRepository.save(card);

    // Apply discount
    order.setDiscount(order.getDiscount().add(discountValue));
    order.setTotalPrice(order.getTotalPrice().subtract(discountValue));
    orderRepository.save(order);

    // Create transaction
    StampTransaction transaction =
        StampTransaction.builder()
            .user(card.getUser())
            .stampCard(card)
            .order(order)
            .type(StampTransactionType.REDEEM)
            .stamps(0)
            .stampsAfter(card.getCurrentStamps())
            .rewardsAfter(card.getFreeRewardsAvailable())
            .discountApplied(discountValue)
            .description("Redeemed reward for order #" + orderId)
            .build();
    stampTransactionRepository.save(transaction);

    log.info(
        "User {} redeemed stamp reward for {} VND on order {}", userId, discountValue, orderId);

    return loyaltyMapper.toStampCardResponse(card);
  }

  private BigDecimal calculateStampDiscount(StampCard card, Order order) {
    // For BOX type: discount = storage price for one box of that size
    // For SERVICE type: discount = service price
    if (card.getStampType() == StampType.BOX) {
      // Return storage price as discount
      return order.getStoragePrice();
    } else {
      // Return service price (simplified)
      return order.getTotalPrice().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }
  }

  /** Get all stamp cards for user. */
  @Transactional(readOnly = true)
  public List<StampCardResponse> getStampCards(Long userId) {
    return stampCardRepository.findByUserId(userId).stream()
        .map(loyaltyMapper::toStampCardResponse)
        .collect(Collectors.toList());
  }

  /** Get stamp cards with available rewards. */
  @Transactional(readOnly = true)
  public List<StampCardResponse> getAvailableRewards(Long userId) {
    return stampCardRepository.findCardsWithAvailableRewards(userId).stream()
        .map(loyaltyMapper::toStampCardResponse)
        .collect(Collectors.toList());
  }

  /** Calculate potential points for an amount. */
  public long calculatePointsForAmount(BigDecimal amount) {
    return amount.divide(VND_PER_POINT_EARN, 0, RoundingMode.DOWN).longValue();
  }

  /** Calculate VND value for points. */
  public BigDecimal calculateVndForPoints(long points) {
    return BigDecimal.valueOf(points).multiply(VND_PER_POINT_REDEEM);
  }
}
