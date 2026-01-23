package com.huynqb.laundrylockerbackend.module.order.dto.request;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for creating a new order. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

  @NotNull(message = "Order type is required")
  private OrderType type;

  @NotNull(message = "Locker ID is required")
  private Long lockerId;

  /** Single box ID (backward compatibility). If boxIds is provided, this will be ignored. */
  private Long boxId;

  /**
   * Multiple box IDs for customers who need more than one box. If null/empty, system will
   * auto-assign based on boxId or find available.
   */
  private Set<Long> boxIds;

  private String customerNote;

  private String deliveryAddress;

  /**
   * Service IDs to apply to this order. Quantity/weight will be updated by staff after collection.
   */
  private List<Long> serviceIds;

  /**
   * Old format - still supported for backward compatibility.
   *
   * @deprecated Use serviceIds instead
   */
  @Deprecated private List<OrderItemRequest> items;
}
