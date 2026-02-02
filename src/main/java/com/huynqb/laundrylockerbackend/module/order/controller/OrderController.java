package com.huynqb.laundrylockerbackend.module.order.controller;

import com.huynqb.laundrylockerbackend.core.constant.TagConstants;
import com.huynqb.laundrylockerbackend.core.constant.UriParamConstants;
import com.huynqb.laundrylockerbackend.core.dto.ApiResponse;
import com.huynqb.laundrylockerbackend.core.dto.ResponseHelper;
import com.huynqb.laundrylockerbackend.core.security.jwt.JwtTokenProvider;
import com.huynqb.laundrylockerbackend.module.order.dto.request.CheckoutOrderRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.request.CreateOrderRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.request.UpdateOrderWeightRequest;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderResponse;
import com.huynqb.laundrylockerbackend.module.order.dto.response.OrderStatusResponse;
import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.service.OrderRatingService;
import com.huynqb.laundrylockerbackend.module.order.service.OrderService;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for order management. */
@Tag(name = TagConstants.ROOT_TAG_ORDERS)
@RequestMapping(UriParamConstants.ROOT_URI_ORDERS)
@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final OrderRatingService orderRatingService;
  private final JwtTokenProvider jwtTokenProvider;
  private final ResponseHelper responseHelper;

  /** Create a new order. */
  @Operation(summary = "Create Order", description = "Create a new laundry order")
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @Valid @RequestBody CreateOrderRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    OrderResponse response = orderService.createOrder(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(responseHelper.success(response, "ORDER_CREATED"));
  }

  /** Get all orders (paginated). */
  @Operation(
      summary = "Get All Orders",
      description = "Retrieve all orders with pagination (Admin/Staff only)")
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(Pageable pageable) {
    Page<OrderResponse> orders = orderService.getOrders(pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Get order by ID. */
  @Operation(summary = "Get Order By ID", description = "Retrieve order details by order ID")
  @GetMapping(UriParamConstants.BY_ORDER_ID)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long orderId) {
    OrderResponse response = orderService.getOrderById(orderId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RETRIEVED"));
  }

  /** Get order status - lightweight endpoint for status tracking. */
  @Operation(
      summary = "Get Order Status",
      description = "Retrieve order status with next action hints for user")
  @GetMapping(UriParamConstants.ORDER_STATUS)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
      @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    OrderStatusResponse response = orderService.getOrderStatus(orderId, userId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_STATUS_RETRIEVED"));
  }

  /** Get order by order code (e.g., ORD-20260202-ABC123). */
  @Operation(summary = "Get Order By Code", description = "Retrieve order details by order code")
  @GetMapping(UriParamConstants.BY_ORDER_CODE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(@PathVariable String orderCode) {
    OrderResponse response = orderService.getOrderByCode(orderCode);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RETRIEVED"));
  }

  /** Get order by PIN code. */
  @Operation(summary = "Get Order By PIN", description = "Retrieve order details by PIN code")
  @GetMapping(UriParamConstants.BY_PIN_CODE)
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderByPinCode(
      @PathVariable String pinCode) {
    OrderResponse response = orderService.getOrderByPinCode(pinCode);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RETRIEVED"));
  }

  /** Get my orders - Customer's own orders. */
  @Operation(
      summary = "Get My Orders",
      description = "Retrieve current user's orders with optional status filter")
  @GetMapping(UriParamConstants.MY_ORDERS)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
      @RequestParam(required = false) OrderStatus status,
      Pageable pageable,
      @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    Page<OrderResponse> orders = orderService.getMyOrders(userId, status, pageable);
    return ResponseEntity.ok(responseHelper.success(orders, "ORDERS_RETRIEVED"));
  }

  /** Complete order - Customer confirms pickup. */
  @Operation(
      summary = "Complete Order",
      description = "Customer confirms pickup, completing the order")
  @PutMapping(UriParamConstants.COMPLETE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderResponse>> completeOrder(
      @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    OrderResponse response = orderService.completeOrderByCustomer(orderId, userId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_COMPLETED"));
  }

  /** Checkout an order (payment). */
  @Operation(
      summary = "Checkout Order",
      description = "Process payment and complete order checkout")
  @PostMapping(UriParamConstants.CHECKOUT)
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<PaymentResponse>> checkoutOrder(
      @PathVariable Long orderId,
      @Valid @RequestBody CheckoutOrderRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    PaymentResponse response = orderService.checkoutOrder(orderId, request, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_CHECKOUT_SUCCESS"));
  }

  /** Collect order from locker (staff). */
  @Operation(summary = "Collect Order", description = "Staff collects order items from locker")
  @PutMapping(UriParamConstants.COLLECT)
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<OrderResponse>> collectOrder(
      @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    OrderResponse response = orderService.collectOrder(orderId, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_COLLECTED"));
  }

  /** Update order weight after collection (staff). */
  @Operation(
      summary = "Update Order Weight",
      description = "Staff updates order weight and items after weighing the laundry")
  @PutMapping(UriParamConstants.UPDATE_WEIGHT)
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<OrderResponse>> updateOrderWeight(
      @PathVariable Long orderId,
      @Valid @RequestBody UpdateOrderWeightRequest request,
      @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    OrderResponse response = orderService.updateOrderWeight(orderId, request, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_WEIGHT_UPDATED"));
  }

  /** Return processed order to locker. */
  @Operation(summary = "Return Order", description = "Return processed order items to locker")
  @PutMapping(UriParamConstants.RETURN)
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<OrderResponse>> returnOrder(
      @PathVariable Long orderId,
      @RequestParam Long boxId,
      @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    OrderResponse response = orderService.returnOrder(orderId, boxId, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RETURNED"));
  }

  /** Cancel an order. */
  @Operation(summary = "Cancel Order", description = "Cancel an existing order")
  @PutMapping(UriParamConstants.CANCEL)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
      @PathVariable Long orderId, @RequestParam(required = false) Integer reason) {
    OrderResponse response = orderService.cancelOrder(orderId, reason);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_CANCELED"));
  }

  /** Confirm order - Customer confirms items placed in locker. */
  @Operation(
      summary = "Confirm Order",
      description = "Customer confirms items have been placed in locker")
  @PutMapping(UriParamConstants.CONFIRM)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(@PathVariable Long orderId) {
    OrderResponse response = orderService.confirmOrder(orderId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_CONFIRMED"));
  }

  /** Start processing order. */
  @Operation(summary = "Process Order", description = "Start processing the order")
  @PutMapping(UriParamConstants.PROCESS)
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<OrderResponse>> processOrder(
      @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    OrderResponse response = orderService.processOrder(orderId, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_PROCESSING"));
  }

  /** Mark order as ready for return. */
  @Operation(summary = "Mark Order Ready", description = "Mark order as ready for customer pickup")
  @PutMapping(UriParamConstants.READY)
  @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
  public ResponseEntity<ApiResponse<OrderResponse>> markOrderReady(
      @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long staffId = extractUserId(authHeader);
    OrderResponse response = orderService.markOrderReady(orderId, staffId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_READY"));
  }

  /** Rate an order. */
  @Operation(summary = "Rate Order", description = "Rate a completed order")
  @PostMapping(UriParamConstants.ORDER_RATE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<
          ApiResponse<
              com.huynqb.laundrylockerbackend.module.order.dto.response.OrderRatingResponse>>
      rateOrder(
          @PathVariable Long orderId,
          @Valid @RequestBody
              com.huynqb.laundrylockerbackend.module.order.dto.request.OrderRatingRequest request,
          @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    var response = orderRatingService.createRating(orderId, request, userId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RATED"));
  }

  /** Get order rating. */
  @Operation(summary = "Get Order Rating", description = "Get rating for an order")
  @GetMapping(UriParamConstants.ORDER_RATING)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<
          ApiResponse<
              com.huynqb.laundrylockerbackend.module.order.dto.response.OrderRatingResponse>>
      getOrderRating(@PathVariable Long orderId) {
    var response = orderRatingService.getRating(orderId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_RATING_RETRIEVED"));
  }

  /** Get order timeline. */
  @Operation(summary = "Get Order Timeline", description = "Get timeline of order status changes")
  @GetMapping(UriParamConstants.ORDER_TIMELINE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<
          ApiResponse<
              com.huynqb.laundrylockerbackend.module.order.dto.response.OrderTimelineResponse>>
      getOrderTimeline(
          @PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
    Long userId = extractUserId(authHeader);
    var response = orderRatingService.getOrderTimeline(orderId, userId);
    return ResponseEntity.ok(responseHelper.success(response, "ORDER_TIMELINE_RETRIEVED"));
  }

  private Long extractUserId(String authHeader) {
    String token = authHeader.replace("Bearer ", "");
    return jwtTokenProvider.getUserIdFromToken(token);
  }
}
