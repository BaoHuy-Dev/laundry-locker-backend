package com.huynqb.laundrylockerbackend.module.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huynqb.laundrylockerbackend.core.exception.BusinessException;
import com.huynqb.laundrylockerbackend.module.admin.dto.request.PromotionRequest;
import com.huynqb.laundrylockerbackend.module.admin.dto.response.PromotionResponse;
import com.huynqb.laundrylockerbackend.module.admin.model.Promotion;
import com.huynqb.laundrylockerbackend.module.admin.model.PromotionUsage;
import com.huynqb.laundrylockerbackend.module.admin.repository.PromotionRepository;
import com.huynqb.laundrylockerbackend.module.admin.repository.PromotionUsageRepository;
import com.huynqb.laundrylockerbackend.module.user.model.User;
import com.huynqb.laundrylockerbackend.module.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for managing promotions. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;
  private final PromotionUsageRepository promotionUsageRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  /** Create a new promotion. */
  @Transactional
  public PromotionResponse createPromotion(PromotionRequest request, Long adminId) {
    log.info("Creating promotion with code: {} by admin: {}", request.getCode(), adminId);

    // Check code uniqueness
    if (promotionRepository.existsByCode(request.getCode())) {
      throw new BusinessException(
          "E_PROMO001", HttpStatus.CONFLICT, "Promotion code already exists");
    }

    // Validate dates
    if (request.getEndDate().isBefore(request.getStartDate())) {
      throw new BusinessException(
          "E_PROMO002", HttpStatus.BAD_REQUEST, "End date must be after start date");
    }

    User admin =
        userRepository
            .findById(adminId)
            .orElseThrow(
                () -> new BusinessException("E_PROMO003", HttpStatus.NOT_FOUND, "Admin not found"));

    Promotion promotion =
        Promotion.builder()
            .code(request.getCode().toUpperCase())
            .title(request.getTitle())
            .description(request.getDescription())
            .discountType(Promotion.DiscountType.valueOf(request.getDiscountType().name()))
            .discountValue(request.getDiscountValue())
            .maxDiscountAmount(request.getMaxDiscountAmount())
            .minOrderAmount(request.getMinOrderAmount())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .totalUsageLimit(request.getTotalUsageLimit())
            .perUserLimit(request.getPerUserLimit())
            .applicableServiceIds(toJson(request.getApplicableServiceIds()))
            .applicableStoreIds(toJson(request.getApplicableStoreIds()))
            .applicableTiers(toJson(request.getApplicableTiers()))
            .isActive(request.getIsActive())
            .priority(request.getPriority())
            .stackable(request.getStackable())
            .createdBy(admin)
            .build();

    promotion = promotionRepository.save(promotion);
    log.info("Created promotion with ID: {}", promotion.getId());

    return mapToResponse(promotion);
  }

  /** Update an existing promotion. */
  @Transactional
  public PromotionResponse updatePromotion(Long promotionId, PromotionRequest request) {
    log.info("Updating promotion: {}", promotionId);

    Promotion promotion = findPromotionById(promotionId);

    // Check code uniqueness if changed
    if (!promotion.getCode().equals(request.getCode().toUpperCase())
        && promotionRepository.existsByCode(request.getCode())) {
      throw new BusinessException(
          "E_PROMO001", HttpStatus.CONFLICT, "Promotion code already exists");
    }

    promotion.setCode(request.getCode().toUpperCase());
    promotion.setTitle(request.getTitle());
    promotion.setDescription(request.getDescription());
    promotion.setDiscountType(Promotion.DiscountType.valueOf(request.getDiscountType().name()));
    promotion.setDiscountValue(request.getDiscountValue());
    promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
    promotion.setMinOrderAmount(request.getMinOrderAmount());
    promotion.setStartDate(request.getStartDate());
    promotion.setEndDate(request.getEndDate());
    promotion.setTotalUsageLimit(request.getTotalUsageLimit());
    promotion.setPerUserLimit(request.getPerUserLimit());
    promotion.setApplicableServiceIds(toJson(request.getApplicableServiceIds()));
    promotion.setApplicableStoreIds(toJson(request.getApplicableStoreIds()));
    promotion.setApplicableTiers(toJson(request.getApplicableTiers()));
    promotion.setIsActive(request.getIsActive());
    promotion.setPriority(request.getPriority());
    promotion.setStackable(request.getStackable());

    promotion = promotionRepository.save(promotion);
    return mapToResponse(promotion);
  }

  /** Get promotion by ID. */
  public PromotionResponse getPromotion(Long promotionId) {
    return mapToResponse(findPromotionById(promotionId));
  }

  /** Get all promotions with pagination. */
  public Page<PromotionResponse> getPromotions(Pageable pageable) {
    return promotionRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToResponse);
  }

  /** Get promotions by status. */
  public Page<PromotionResponse> getPromotionsByStatus(String status, Pageable pageable) {
    return promotionRepository
        .findByStatus(status, LocalDateTime.now(), pageable)
        .map(this::mapToResponse);
  }

  /** Get active promotions. */
  public List<PromotionResponse> getActivePromotions() {
    return promotionRepository.findActivePromotions(LocalDateTime.now()).stream()
        .map(this::mapToResponse)
        .toList();
  }

  /**
   * Validate a promotion code. Supports both normal promo codes (promotions.code) and loyalty
   * reward codes (promotion_usage.reward_code).
   */
  @Transactional(readOnly = true)
  public PromotionResponse validatePromotionCode(String code) {
    Optional<Promotion> optionalPromotion = promotionRepository.findByCode(code.toUpperCase());

    if (optionalPromotion.isPresent()) {
      Promotion promotion = optionalPromotion.get();
      if (!promotion.isCurrentlyActive()) {
        throw new BusinessException(
            "E_PROMO005",
            HttpStatus.BAD_REQUEST,
            "Promotion is not active: " + promotion.getStatus());
      }
      return mapToResponse(promotion);
    }

    // Fall back to loyalty reward codes stored in promotion_usage
    Optional<PromotionUsage> optUsage =
        promotionUsageRepository.findByRewardCodeWithPromotion(code.toUpperCase());
    if (optUsage.isEmpty()) {
      throw new BusinessException("E_PROMO004", HttpStatus.NOT_FOUND, "Invalid promotion code");
    }

    PromotionUsage usage = optUsage.get();
    if (!usage.isValid()) {
      throw new BusinessException(
          "E_PROMO005",
          HttpStatus.BAD_REQUEST,
          "Voucher is not valid (status=" + usage.getStatus() + ")");
    }

    return mapToResponse(usage.getPromotion());
  }

  /** Delete a promotion. */
  @Transactional
  public void deletePromotion(Long promotionId) {
    log.info("Deleting promotion: {}", promotionId);
    Promotion promotion = findPromotionById(promotionId);

    if (promotion.getCurrentUsageCount() > 0) {
      // Soft delete - just deactivate
      promotion.setIsActive(false);
      promotionRepository.save(promotion);
      log.info("Soft deleted (deactivated) promotion: {}", promotionId);
    } else {
      promotionRepository.delete(promotion);
      log.info("Hard deleted promotion: {}", promotionId);
    }
  }

  /** Increment usage count after successful redemption. */
  @Transactional
  public void incrementUsageCount(Long promotionId) {
    promotionRepository.incrementUsageCount(promotionId);
  }

  /** Search promotions by keyword. */
  public Page<PromotionResponse> searchPromotions(String keyword, Pageable pageable) {
    return promotionRepository.searchByKeyword(keyword, pageable).map(this::mapToResponse);
  }

  // ===== Private Helper Methods =====

  private Promotion findPromotionById(Long promotionId) {
    return promotionRepository
        .findById(promotionId)
        .orElseThrow(
            () -> new BusinessException("E_PROMO006", HttpStatus.NOT_FOUND, "Promotion not found"));
  }

  private PromotionResponse mapToResponse(Promotion promotion) {
    Integer remainingUses = null;
    if (promotion.getTotalUsageLimit() != null) {
      remainingUses = promotion.getTotalUsageLimit() - promotion.getCurrentUsageCount();
    }

    return PromotionResponse.builder()
        .id(promotion.getId())
        .code(promotion.getCode())
        .title(promotion.getTitle())
        .description(promotion.getDescription())
        .discountType(promotion.getDiscountType().name())
        .discountValue(promotion.getDiscountValue())
        .maxDiscountAmount(promotion.getMaxDiscountAmount())
        .minOrderAmount(promotion.getMinOrderAmount())
        .startDate(promotion.getStartDate())
        .endDate(promotion.getEndDate())
        .totalUsageLimit(promotion.getTotalUsageLimit())
        .currentUsageCount(promotion.getCurrentUsageCount())
        .remainingUses(remainingUses)
        .perUserLimit(promotion.getPerUserLimit())
        .applicableServiceIds(
            fromJson(promotion.getApplicableServiceIds(), new TypeReference<List<Long>>() {}))
        .applicableStoreIds(
            fromJson(promotion.getApplicableStoreIds(), new TypeReference<List<Long>>() {}))
        .applicableTiers(
            fromJson(promotion.getApplicableTiers(), new TypeReference<List<String>>() {}))
        .isActive(promotion.getIsActive())
        .priority(promotion.getPriority())
        .stackable(promotion.getStackable())
        .status(promotion.getStatus())
        .createdAt(promotion.getCreatedAt())
        .updatedAt(promotion.getUpdatedAt())
        .createdBy(promotion.getCreatedBy() != null ? promotion.getCreatedBy().getId() : null)
        .build();
  }

  private String toJson(Object obj) {
    if (obj == null) return null;
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      log.error("Error serializing to JSON", e);
      return null;
    }
  }

  private <T> T fromJson(String json, TypeReference<T> typeRef) {
    if (json == null || json.isEmpty()) return null;
    try {
      return objectMapper.readValue(json, typeRef);
    } catch (JsonProcessingException e) {
      log.error("Error deserializing from JSON", e);
      return null;
    }
  }
}
