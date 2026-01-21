package com.huynqb.laundrylockerbackend.module.order.dto.request;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderType;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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

  private Long boxId; // Optional, system can auto-assign

  private String customerNote;

  private String deliveryAddress;

  private List<OrderItemRequest> items;
}
