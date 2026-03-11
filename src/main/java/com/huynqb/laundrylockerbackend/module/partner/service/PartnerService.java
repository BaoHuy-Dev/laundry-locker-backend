package com.huynqb.laundrylockerbackend.module.partner.service;

import com.huynqb.laundrylockerbackend.module.locker.dto.response.BoxResponse;
import com.huynqb.laundrylockerbackend.module.locker.dto.response.LockerResponse;
import com.huynqb.laundrylockerbackend.module.locker.service.LockerService;
import com.huynqb.laundrylockerbackend.module.order.dto.request.UpdateOrderWeightRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.mapper.OrderMapper;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.GenerateAccessCodeRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.PartnerRegistrationRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.request.PartnerUpdateRequest;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerDashboardResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerOrderStatisticsResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.PartnerRevenueResponse;
import com.huynqb.laundrylockerbackend.module.partner.dto.response.StaffAccessCodeResponse;
import com.huynqb.laundrylockerbackend.module.partner.enums.AccessCodeAction;
import com.huynqb.laundrylockerbackend.module.partner.enums.PartnerStatus;
import com.huynqb.laundrylockerbackend.module.partner.exception.PartnerException;
import com.huynqb.laundrylockerbackend.module.partner.helper.OrderBoxHelper;
import com.huynqb.laundrylockerbackend.module.partner.mapper.PartnerMapper;
import com.huynqb.laundrylockerbackend.module.partner.model.Partner;
import com.huynqb.laundrylockerbackend.module.partner.repository.PartnerRepository;
import com.huynqb.laundrylockerbackend.module.partner.validator.PartnerOrderValidator;
import com.huynqb.laundrylockerbackend.module.store.dto.response.StoreResponse;
import com.huynqb.laundrylockerbackend.module.store.mapper.StoreMapper;
import com.huynqb.laundrylockerbackend.module.store.model.Store;
import com.huynqb.laundrylockerbackend.module.store.repository.StoreRepository;
import com.huynqb.laundrylockerbackend.module.user.dto.response.UserResponse;
import com.huynqb.laundrylockerbackend.module.user.enums.RoleName;
import com.huynqb.laundrylockerbackend.module.user.mapper.UserMapper;
import com.huynqb.laundrylockerbackend.module.user.model.Role;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.RoleRepository;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for partner operations. Refactored following SOLID principles: - Single Responsibility:
 * Core partner business logic - Open/Closed: Uses validators and helpers for extension - Dependency
 * Inversion: Depends on abstractions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerService {

  private final PartnerRepository partnerRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final StoreRepository storeRepository;
  private final OrderRepository orderRepository;
  private final PartnerMapper partnerMapper;
  private final StoreMapper storeMapper;
  private final UserMapper userMapper;
  private final OrderMapper orderMapper;
  private final StaffAccessCodeService accessCodeService;
  private final LockerService lockerService;
  private final OrderBoxHelper orderBoxHelper;

  // ===== Partner Registration =====

  @Transactional
  public PartnerResponse registerPartner(Long userId, PartnerRegistrationRequest request) {
    User user = findUserById(userId);

    if (partnerRepository.existsByUserId(userId)) {
      throw PartnerException.alreadyExists();
    }

    Partner partner = partnerMapper.toEntity(request);
    partner.setUser(user);
    partner.setStatus(PartnerStatus.PENDING);
    if (partner.getContactEmail() == null) {
      partner.setContactEmail(user.getEmail());
    }
    partner = partnerRepository.save(partner);

    log.info("User {} registered as partner {}", userId, partner.getId());
    return partnerMapper.toResponse(partner);
  }

  @Transactional(readOnly = true)
  public PartnerResponse getPartnerByUserId(Long userId) {
    return partnerMapper.toResponse(findPartnerByUserId(userId));
  }

  @Transactional(readOnly = true)
  public PartnerResponse getPartnerById(Long partnerId) {
    return partnerMapper.toResponse(findPartnerById(partnerId));
  }

  @Transactional(readOnly = true)
  public Long getPartnerIdByUserId(Long userId) {
    return findPartnerByUserId(userId).getId();
  }

  // ===== Partner Dashboard =====

  @Transactional(readOnly = true)
  public PartnerDashboardResponse getPartnerDashboard(Long userId) {
    Partner partner = findPartnerByUserId(userId);
    validateApprovedStatus(partner);

    List<Long> storeIds = getStoreIds(partner);
    DashboardStats stats = calculateDashboardStats(storeIds, partner);

    return buildDashboardResponse(partner, stats);
  }

  // ===== Partner Stores =====

  @Transactional(readOnly = true)
  public List<StoreResponse> getPartnerStores(Long userId) {
    Partner partner = getApprovedPartner(userId);
    return partner.getStores().stream().map(storeMapper::toResponse).collect(Collectors.toList());
  }

  // ===== Partner Staff =====

  @Transactional(readOnly = true)
  public List<UserResponse> getPartnerStaff(Long userId) {
    Partner partner = getApprovedPartner(userId);
    return partner.getStaff().stream().map(userMapper::toResponse).collect(Collectors.toList());
  }

  @Transactional
  public UserResponse addStaffToPartner(Long userId, Long staffUserId) {
    Partner partner = getApprovedPartner(userId);
    User staffUser = findUserById(staffUserId);

    // Staff is managed internally by Partner, no separate STAFF role needed
    partner.getStaff().add(staffUser);
    partnerRepository.save(partner);

    log.info("Added staff {} to partner {}", staffUserId, partner.getId());
    return userMapper.toResponse(staffUser);
  }

  @Transactional
  public void removeStaffFromPartner(Long userId, Long staffUserId) {
    Partner partner = getApprovedPartner(userId);

    User staffUser =
        partner.getStaff().stream()
            .filter(s -> s.getId().equals(staffUserId))
            .findFirst()
            .orElseThrow(PartnerException::staffNotFound);

    partner.getStaff().remove(staffUser);
    partnerRepository.save(partner);

    log.info("Removed staff {} from partner {}", staffUserId, partner.getId());
  }

  // ===== Admin Operations =====

  @Transactional(readOnly = true)
  public Page<PartnerResponse> getAllPartners(PartnerStatus status, Pageable pageable) {
    Page<Partner> partners =
        status != null
            ? partnerRepository.findByStatus(status, pageable)
            : partnerRepository.findAll(pageable);
    return partners.map(partnerMapper::toResponse);
  }

  @Transactional
  public PartnerResponse approvePartner(Long partnerId, Long adminUserId) {
    Partner partner = findPartnerById(partnerId);
    validatePendingStatus(partner);

    assignPartnerRole(partner.getUser());
    partner.setStatus(PartnerStatus.APPROVED);
    partner.setApprovedAt(LocalDateTime.now());
    partner.setApprovedBy(adminUserId);
    partnerRepository.save(partner);

    log.info("Partner {} approved by admin {}", partnerId, adminUserId);
    return partnerMapper.toResponse(partner);
  }

  @Transactional
  public PartnerResponse rejectPartner(Long partnerId, String reason) {
    Partner partner = findPartnerById(partnerId);
    validatePendingStatus(partner);

    partner.setStatus(PartnerStatus.REJECTED);
    partner.setRejectionReason(reason);
    partnerRepository.save(partner);

    log.info("Partner {} rejected", partnerId);
    return partnerMapper.toResponse(partner);
  }

  @Transactional
  public PartnerResponse suspendPartner(Long partnerId) {
    Partner partner = findPartnerById(partnerId);
    partner.setStatus(PartnerStatus.SUSPENDED);
    partnerRepository.save(partner);

    log.info("Partner {} suspended", partnerId);
    return partnerMapper.toResponse(partner);
  }

  // ===== Order Management =====

  @Transactional(readOnly = true)
  public Page<OrderResponse> getPendingOrders(Long userId, Pageable pageable) {
    return getOrdersByStatus(userId, OrderStatus.WAITING, pageable);
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> getPartnerOrders(Long userId, String status, Pageable pageable) {
    Partner partner = getApprovedPartner(userId);
    List<Long> storeIds = getStoreIds(partner);

    if (storeIds.isEmpty()) {
      return Page.empty(pageable);
    }

    if (status != null && !status.isEmpty()) {
      try {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository
            .findByStoreIdsAndStatus(storeIds, orderStatus, pageable)
            .map(orderMapper::toResponse);
      } catch (IllegalArgumentException e) {
        log.warn("Invalid order status: {}", status);
      }
    }

    return orderRepository.findByStoreIds(storeIds, pageable).map(orderMapper::toResponse);
  }

  @Transactional
  public StaffAccessCodeResponse acceptOrderAndGenerateCode(
      Long userId, Long orderId, Integer expirationHours, String notes) {
    Partner partner = getApprovedPartner(userId);
    Order order = findOrderById(orderId);

    PartnerOrderValidator.validateOrderBelongsToPartner(order, partner);
    PartnerOrderValidator.validateForAcceptance(order);

    log.info("Partner {} accepted order {}", partner.getId(), orderId);
    return generateAccessCode(
        partner.getId(), orderId, AccessCodeAction.COLLECT, expirationHours, notes);
  }

  @Transactional
  public OrderResponse forceCollectOrder(Long userId, Long orderId) {
    Partner partner = getApprovedPartner(userId);
    Order order = findOrderById(orderId);

    PartnerOrderValidator.validateOrderBelongsToPartner(order, partner);
    PartnerOrderValidator.validateForAcceptance(order);

    orderBoxHelper.releaseSendBoxes(order);
    order.setStatus(OrderStatus.COLLECTED);
    order = orderRepository.save(order);

    log.info("Order {} force-collected by partner {}", orderId, partner.getId());
    return orderMapper.toResponse(order);
  }

  @Transactional
  public OrderResponse updateOrderToProcessing(Long userId, Long orderId) {
    Partner partner = getApprovedPartner(userId);
    Order order = findOrderById(orderId);

    PartnerOrderValidator.validateOrderBelongsToPartner(order, partner);
    PartnerOrderValidator.validateForProcessing(order);

    order.setStatus(OrderStatus.PROCESSING);
    order = orderRepository.save(order);

    log.info("Order {} updated to PROCESSING by partner {}", orderId, partner.getId());
    return orderMapper.toResponse(order);
  }

  @Transactional
  public StaffAccessCodeResponse markOrderReadyAndGenerateCode(
      Long userId, Long orderId, Integer expirationHours, String notes) {
    Partner partner = getApprovedPartner(userId);
    Order order = findOrderById(orderId);

    PartnerOrderValidator.validateOrderBelongsToPartner(order, partner);
    PartnerOrderValidator.validateForReady(order);

    order.setStatus(OrderStatus.READY);
    orderRepository.save(order);

    log.info("Order {} marked as READY by partner {}", orderId, partner.getId());
    return generateAccessCode(
        partner.getId(), orderId, AccessCodeAction.RETURN, expirationHours, notes);
  }

  // ===== Order Detail & Weight Update =====

  @Transactional(readOnly = true)
  public OrderResponse getOrderDetail(Long userId, Long orderId) {
    Partner partner = getApprovedPartner(userId);
    Order order = findOrderById(orderId);

    PartnerOrderValidator.validateOrderBelongsToPartner(order, partner);

    return orderMapper.toResponse(order);
  }

  @Transactional
  public OrderResponse updateOrderWeight(
      Long userId, Long orderId, UpdateOrderWeightRequest request) {
    Partner partner = getApprovedPartner(userId);
    Order order = findOrderById(orderId);

    PartnerOrderValidator.validateOrderBelongsToPartner(order, partner);

    // Validate status - can only update weight after COLLECTED
    if (order.getStatus() != OrderStatus.COLLECTED && order.getStatus() != OrderStatus.PROCESSING) {
      throw new RuntimeException(
          "Order weight can only be updated when status is COLLECTED or PROCESSING");
    }

    // Update weight info
    order.setActualWeight(request.getActualWeight());
    order.setWeightUnit(request.getWeightUnit());
    if (request.getStaffNote() != null) {
      order.setStaffNote(request.getStaffNote());
    }

    Order savedOrder = orderRepository.save(order);
    log.info(
        "Order {} weight updated to {} {} by partner {}",
        orderId,
        request.getActualWeight(),
        request.getWeightUnit(),
        partner.getId());

    return orderMapper.toResponse(savedOrder);
  }

  // ===== Partner Profile Update =====

  @Transactional
  public PartnerResponse updatePartner(Long userId, PartnerUpdateRequest request) {
    Partner partner = findPartnerByUserId(userId);
    partnerMapper.updateFromUpdateRequest(request, partner);
    partner = partnerRepository.save(partner);
    log.info("Partner {} profile updated", partner.getId());
    return partnerMapper.toResponse(partner);
  }

  // ===== Partner Lockers =====

  @Transactional(readOnly = true)
  public List<LockerResponse> getPartnerLockers(Long userId) {
    Partner partner = getApprovedPartner(userId);
    List<Long> storeIds = getStoreIds(partner);

    if (storeIds.isEmpty()) {
      return List.of();
    }

    return storeIds.stream()
        .flatMap(storeId -> lockerService.getLockersByStore(storeId).stream())
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BoxResponse> getPartnerLockerAvailableBoxes(Long userId, Long lockerId) {
    Partner partner = getApprovedPartner(userId);
    List<Long> storeIds = getStoreIds(partner);

    // Validate locker belongs to partner's stores
    LockerResponse locker = lockerService.getLockerById(lockerId);
    if (!storeIds.contains(locker.getStoreId())) {
      throw PartnerException.storeNotOwned();
    }

    return lockerService.getAvailableBoxes(lockerId);
  }

  // ===== Partner Revenue & Statistics =====

  @Transactional(readOnly = true)
  public PartnerRevenueResponse getPartnerRevenue(
      Long userId, LocalDateTime fromDate, LocalDateTime toDate) {
    Partner partner = getApprovedPartner(userId);
    List<Long> storeIds = getStoreIds(partner);

    if (storeIds.isEmpty()) {
      return buildEmptyRevenueResponse(partner, fromDate, toDate);
    }

    BigDecimal grossRevenue =
        Objects.requireNonNullElse(
            orderRepository.sumRevenueByStoreIdsAndDateRange(storeIds, fromDate, toDate),
            BigDecimal.ZERO);

    long totalOrders = orderRepository.countByStoreIdsAndDateRange(storeIds, fromDate, toDate);
    long completedOrders =
        orderRepository.countByStoreIdsAndStatusAndDateRange(
            storeIds, OrderStatus.COMPLETED, fromDate, toDate);
    long canceledOrders =
        orderRepository.countByStoreIdsAndStatusAndDateRange(
            storeIds, OrderStatus.CANCELED, fromDate, toDate);

    BigDecimal partnerRevenue = calculatePartnerRevenue(grossRevenue, partner);
    BigDecimal platformFee = grossRevenue.subtract(partnerRevenue);

    return PartnerRevenueResponse.builder()
        .partnerId(partner.getId())
        .businessName(partner.getBusinessName())
        .fromDate(fromDate)
        .toDate(toDate)
        .grossRevenue(grossRevenue)
        .partnerRevenue(partnerRevenue)
        .platformFee(platformFee)
        .revenueSharePercent(partner.getRevenueSharePercent())
        .totalOrders(totalOrders)
        .completedOrders(completedOrders)
        .canceledOrders(canceledOrders)
        .build();
  }

  @Transactional(readOnly = true)
  public PartnerOrderStatisticsResponse getPartnerOrderStatistics(Long userId) {
    Partner partner = getApprovedPartner(userId);
    List<Long> storeIds = getStoreIds(partner);

    if (storeIds.isEmpty()) {
      return buildEmptyStatisticsResponse(partner);
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
    LocalDateTime startOfWeek = now.minusDays(7);
    LocalDateTime startOfMonth = now.minusDays(30);

    // Count orders by status
    OrderStatusCounts statusCounts = countOrdersByStatus(storeIds);

    // Count orders by time period
    long todayOrders = orderRepository.countByStoreIdsAndDateRange(storeIds, startOfToday, now);
    long weekOrders = orderRepository.countByStoreIdsAndDateRange(storeIds, startOfWeek, now);
    long monthOrders = orderRepository.countByStoreIdsAndDateRange(storeIds, startOfMonth, now);

    // Revenue metrics
    BigDecimal totalRevenue = getRevenueOrZero(orderRepository.sumRevenueByStoreIds(storeIds));
    BigDecimal todayRevenue =
        getRevenueOrZero(
            orderRepository.sumRevenueByStoreIdsAndDateRange(storeIds, startOfToday, now));
    BigDecimal weekRevenue =
        getRevenueOrZero(
            orderRepository.sumRevenueByStoreIdsAndDateRange(storeIds, startOfWeek, now));
    BigDecimal monthRevenue =
        getRevenueOrZero(
            orderRepository.sumRevenueByStoreIdsAndDateRange(storeIds, startOfMonth, now));

    BigDecimal averageOrderValue =
        statusCounts.completed > 0
            ? totalRevenue.divide(
                BigDecimal.valueOf(statusCounts.completed), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

    return PartnerOrderStatisticsResponse.builder()
        .partnerId(partner.getId())
        .totalOrders(statusCounts.total)
        .todayOrders(todayOrders)
        .weekOrders(weekOrders)
        .monthOrders(monthOrders)
        .waitingOrders(statusCounts.waiting)
        .collectedOrders(statusCounts.collected)
        .processingOrders(statusCounts.processing)
        .readyOrders(statusCounts.ready)
        .returnedOrders(statusCounts.returned)
        .completedOrders(statusCounts.completed)
        .canceledOrders(statusCounts.canceled)
        .totalRevenue(totalRevenue)
        .todayRevenue(todayRevenue)
        .weekRevenue(weekRevenue)
        .monthRevenue(monthRevenue)
        .averageOrderValue(averageOrderValue)
        .build();
  }

  private PartnerRevenueResponse buildEmptyRevenueResponse(
      Partner partner, LocalDateTime fromDate, LocalDateTime toDate) {
    return PartnerRevenueResponse.builder()
        .partnerId(partner.getId())
        .businessName(partner.getBusinessName())
        .fromDate(fromDate)
        .toDate(toDate)
        .grossRevenue(BigDecimal.ZERO)
        .partnerRevenue(BigDecimal.ZERO)
        .platformFee(BigDecimal.ZERO)
        .revenueSharePercent(partner.getRevenueSharePercent())
        .totalOrders(0)
        .completedOrders(0)
        .canceledOrders(0)
        .build();
  }

  private PartnerOrderStatisticsResponse buildEmptyStatisticsResponse(Partner partner) {
    return PartnerOrderStatisticsResponse.builder()
        .partnerId(partner.getId())
        .totalOrders(0)
        .todayOrders(0)
        .weekOrders(0)
        .monthOrders(0)
        .waitingOrders(0)
        .collectedOrders(0)
        .processingOrders(0)
        .readyOrders(0)
        .returnedOrders(0)
        .completedOrders(0)
        .canceledOrders(0)
        .totalRevenue(BigDecimal.ZERO)
        .todayRevenue(BigDecimal.ZERO)
        .weekRevenue(BigDecimal.ZERO)
        .monthRevenue(BigDecimal.ZERO)
        .averageOrderValue(BigDecimal.ZERO)
        .build();
  }

  private User findUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new PartnerException("E_PARTNER008", "User not found"));
  }

  private Partner findPartnerById(Long partnerId) {
    return partnerRepository.findById(partnerId).orElseThrow(PartnerException::notFound);
  }

  private Partner findPartnerByUserId(Long userId) {
    return partnerRepository.findByUserId(userId).orElseThrow(PartnerException::notFound);
  }

  private Order findOrderById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
  }

  private Partner getApprovedPartner(Long userId) {
    Partner partner = findPartnerByUserId(userId);

    if (partner.getStatus() == PartnerStatus.SUSPENDED) {
      throw PartnerException.suspended();
    }
    if (partner.getStatus() != PartnerStatus.APPROVED) {
      throw PartnerException.notApproved();
    }

    return partner;
  }

  private List<Long> getStoreIds(Partner partner) {
    return partner.getStores().stream().map(Store::getId).collect(Collectors.toList());
  }

  private BigDecimal getRevenueOrZero(BigDecimal revenue) {
    return Objects.requireNonNullElse(revenue, BigDecimal.ZERO);
  }

  private BigDecimal calculatePartnerRevenue(BigDecimal grossRevenue, Partner partner) {
    return grossRevenue
        .multiply(partner.getRevenueSharePercent())
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  private record OrderStatusCounts(
      long total,
      long waiting,
      long collected,
      long processing,
      long ready,
      long returned,
      long completed,
      long canceled) {}

  private OrderStatusCounts countOrdersByStatus(List<Long> storeIds) {
    return new OrderStatusCounts(
        orderRepository.countByStoreIds(storeIds),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.WAITING),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.COLLECTED),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.PROCESSING),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.READY),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.RETURNED),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.COMPLETED),
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.CANCELED));
  }

  private Page<OrderResponse> getOrdersByStatus(
      Long userId, OrderStatus status, Pageable pageable) {
    Partner partner = getApprovedPartner(userId);
    List<Long> storeIds = getStoreIds(partner);

    if (storeIds.isEmpty()) {
      return Page.empty(pageable);
    }

    return orderRepository
        .findByStoreIdsAndStatus(storeIds, status, pageable)
        .map(orderMapper::toResponse);
  }

  private StaffAccessCodeResponse generateAccessCode(
      Long partnerId,
      Long orderId,
      AccessCodeAction action,
      Integer expirationHours,
      String notes) {
    GenerateAccessCodeRequest request =
        GenerateAccessCodeRequest.builder()
            .orderId(orderId)
            .action(action)
            .expirationHours(expirationHours)
            .notes(notes)
            .build();
    return accessCodeService.generateAccessCode(partnerId, request);
  }

  private void validateApprovedStatus(Partner partner) {
    if (partner.getStatus() != PartnerStatus.APPROVED) {
      throw PartnerException.notApproved();
    }
  }

  private void validatePendingStatus(Partner partner) {
    if (partner.getStatus() != PartnerStatus.PENDING) {
      throw PartnerException.cannotModify();
    }
  }

  private void assignPartnerRole(User user) {
    Role partnerRole =
        roleRepository
            .findByName(RoleName.PARTNER)
            .orElseThrow(() -> new RuntimeException("PARTNER role not found"));

    if (!user.getRoles().contains(partnerRole)) {
      user.getRoles().add(partnerRole);
      userRepository.save(user);
    }
  }

  // ===== Dashboard Stats Helper =====

  private record DashboardStats(
      long totalOrders,
      long completedOrders,
      long pendingOrders,
      long canceledOrders,
      BigDecimal totalRevenue,
      BigDecimal partnerRevenue,
      BigDecimal platformFee,
      int activeStores) {}

  private DashboardStats calculateDashboardStats(List<Long> storeIds, Partner partner) {
    if (storeIds.isEmpty()) {
      return new DashboardStats(0, 0, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }

    long totalOrders = orderRepository.countByStoreIds(storeIds);
    long completedOrders =
        orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.COMPLETED);
    long pendingOrders = orderRepository.countPendingByStoreIds(storeIds);
    long canceledOrders = orderRepository.countByStoreIdsAndStatus(storeIds, OrderStatus.CANCELED);

    BigDecimal totalRevenue = orderRepository.sumRevenueByStoreIds(storeIds);
    if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

    BigDecimal partnerRevenue =
        totalRevenue
            .multiply(partner.getRevenueSharePercent())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    BigDecimal platformFee = totalRevenue.subtract(partnerRevenue);

    int activeStores =
        (int)
            partner.getStores().stream().filter(s -> Boolean.TRUE.equals(s.getIsActive())).count();

    return new DashboardStats(
        totalOrders,
        completedOrders,
        pendingOrders,
        canceledOrders,
        totalRevenue,
        partnerRevenue,
        platformFee,
        activeStores);
  }

  private PartnerDashboardResponse buildDashboardResponse(Partner partner, DashboardStats stats) {
    return PartnerDashboardResponse.builder()
        .partnerId(partner.getId())
        .businessName(partner.getBusinessName())
        .totalStores(partner.getStores().size())
        .activeStores(stats.activeStores())
        .totalStaff(partner.getStaff().size())
        .totalOrders(stats.totalOrders())
        .pendingOrders(stats.pendingOrders())
        .completedOrders(stats.completedOrders())
        .canceledOrders(stats.canceledOrders())
        .totalRevenue(stats.totalRevenue())
        .partnerRevenue(stats.partnerRevenue())
        .platformFee(stats.platformFee())
        .build();
  }
}
