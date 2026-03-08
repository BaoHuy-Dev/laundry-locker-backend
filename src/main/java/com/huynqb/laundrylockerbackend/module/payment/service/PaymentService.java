package com.huynqb.laundrylockerbackend.module.payment.service;

import com.huynqb.laundrylockerbackend.module.order.enums.OrderStatus;
import com.huynqb.laundrylockerbackend.module.order.exception.OrderException;
import com.huynqb.laundrylockerbackend.module.order.model.Order;
import com.huynqb.laundrylockerbackend.module.order.repository.OrderRepository;
import com.huynqb.laundrylockerbackend.module.payment.dto.request.CreatePaymentRequest;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentResponse;
import com.huynqb.laundrylockerbackend.module.payment.dto.response.PaymentUrlResponse;
import com.huynqb.laundrylockerbackend.module.payment.enums.PaymentStatus;
import com.huynqb.laundrylockerbackend.module.payment.exception.PaymentException;
import com.huynqb.laundrylockerbackend.module.payment.mapper.PaymentMapper;
import com.huynqb.laundrylockerbackend.module.payment.model.Payment;
import com.huynqb.laundrylockerbackend.module.payment.repository.PaymentRepository;
import com.huynqb.laundrylockerbackend.module.payment.util.PaymentUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment service facade. Follows OCP by delegating to specific payment provider services. Follows
 * DIP by depending on abstractions (strategy pattern for payment providers).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final VNPayService vnPayService;
  private final MoMoService momoService;
  private final PaymentMapper paymentMapper;

  // Valid order statuses for online payment
  private static final List<OrderStatus> PAYABLE_STATUSES =
      List.of(
          OrderStatus.INITIALIZED, OrderStatus.WAITING, OrderStatus.RETURNED, OrderStatus.READY);

  /**
   * Create online payment and get payment URL.
   *
   * @param request Payment request
   * @param ipAddress Client IP address
   * @return Payment URL response
   */
  @Transactional
  public PaymentUrlResponse createPayment(CreatePaymentRequest request, String ipAddress) {
    log.info(
        "Creating payment for order: {}, method: {}",
        request.getOrderId(),
        request.getPaymentMethod());

    // Find and validate order
    Order order = findOrderById(request.getOrderId());
    validateOrderForPayment(order);
    checkExistingPayment(order);

    // Calculate remaining amount
    BigDecimal totalPaid = calculateTotalPaid(order.getId());
    BigDecimal remainingAmount = order.getTotalPrice().subtract(totalPaid);
    if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new PaymentException("E_PAYMENT003", "Order already fully paid");
    }

    // Create payment record
    Payment payment =
        Payment.builder()
            .order(order)
            .customer(order.getSender())
            .amount(remainingAmount)
            .method(request.getPaymentMethod())
            .status(PaymentStatus.PENDING)
            .referenceId(UUID.randomUUID().toString())
            .content("Thanh toan don hang #" + order.getId())
            .build();
    payment = paymentRepository.save(payment);

    // Create payment URL based on method
    PaymentUrlResponse urlResponse =
        switch (request.getPaymentMethod()) {
          case VNPAY ->
              vnPayService.createPaymentUrl(
                  order, ipAddress, request.getBankCode(), request.getLanguage());
          case MOMO -> momoService.createPaymentUrl(order);
          default ->
              throw new PaymentException(
                  "E_PAYMENT005", "Unsupported payment method: " + request.getPaymentMethod());
        };

    // Update payment ID in response
    urlResponse.setPaymentId(payment.getId());

    log.info("Payment created: {} for order: {}", payment.getId(), order.getId());
    return urlResponse;
  }

  /**
   * Process VNPay IPN callback.
   *
   * @param params Callback parameters
   * @return IPN response
   */
  @Transactional
  public Map<String, String> processVNPayIpn(Map<String, String> params) {
    log.info("Processing VNPay IPN callback");

    // Verify and process
    Map<String, String> ipnResponse = vnPayService.processIpnCallback(params);

    // If checksum valid, update payment status
    if ("00".equals(ipnResponse.get("RspCode"))) {
      String txnRef = vnPayService.getTxnRef(params);
      Long orderId = PaymentUtils.extractOrderIdFromTxnRef(txnRef);

      if (orderId != null) {
        boolean success = vnPayService.isPaymentSuccess(params);
        updatePaymentFromVNPayCallback(orderId, params, success);
      }
    }

    return ipnResponse;
  }

  /**
   * Process VNPay return URL.
   *
   * @param params Return URL parameters
   * @return Payment response
   */
  @Transactional(readOnly = true)
  public PaymentResponse processVNPayReturn(Map<String, String> params) {
    log.info("Processing VNPay return URL");

    String txnRef = vnPayService.getTxnRef(params);
    Long orderId = PaymentUtils.extractOrderIdFromTxnRef(txnRef);

    if (orderId == null) {
      throw new PaymentException("E_PAYMENT001", "Invalid transaction reference");
    }

    Payment payment =
        paymentRepository
            .findFirstByOrderIdAndStatus(orderId, PaymentStatus.COMPLETED)
            .orElse(null);

    if (payment == null) {
      payment =
          paymentRepository
              .findFirstByOrderIdAndStatus(orderId, PaymentStatus.PENDING)
              .orElse(null);
    }

    if (payment == null) {
      throw new PaymentException("E_PAYMENT001", "Payment not found");
    }

    return paymentMapper.toResponse(payment);
  }

  /**
   * Process MoMo callback.
   *
   * @param params Callback parameters
   */
  @Transactional
  public void processMoMoCallback(Map<String, Object> params) {
    log.info("Processing MoMo callback");

    // Verify signature
    if (!momoService.verifySignature(params)) {
      log.error("Invalid MoMo signature");
      throw new PaymentException("E_PAYMENT002", "Invalid signature");
    }

    Long orderId = momoService.extractOrderId(params);
    if (orderId == null) {
      log.error("Cannot extract order ID from MoMo callback");
      return;
    }

    boolean success = momoService.isPaymentSuccess(params);
    String transactionId = momoService.getTransactionId(params);

    updatePaymentFromMoMoCallback(orderId, transactionId, success);
  }

  /**
   * Get payment by ID.
   *
   * @param paymentId Payment ID
   * @return Payment response
   */
  @Transactional(readOnly = true)
  public PaymentResponse getPaymentById(Long paymentId) {
    Payment payment = findPaymentById(paymentId);
    return paymentMapper.toResponse(payment);
  }

  /**
   * Get payments by order ID.
   *
   * @param orderId Order ID
   * @return List of payment responses
   */
  @Transactional(readOnly = true)
  public List<PaymentResponse> getPaymentsByOrder(Long orderId) {
    return paymentRepository.findByOrderId(orderId).stream()
        .map(paymentMapper::toResponse)
        .collect(Collectors.toList());
  }

  // ===== Private Helper Methods =====

  private Order findOrderById(Long orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderException("E_ORDER001"));
  }

  private Payment findPaymentById(Long paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new PaymentException("E_PAYMENT001"));
  }

  private void validateOrderForPayment(Order order) {
    if (!PAYABLE_STATUSES.contains(order.getStatus())) {
      throw new PaymentException("E_PAYMENT006", "Order is not ready for payment");
    }
  }

  private BigDecimal calculateTotalPaid(Long orderId) {
    return paymentRepository.findByOrderId(orderId).stream()
        .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
        .map(Payment::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void checkExistingPayment(Order order) {
    BigDecimal totalPaid = calculateTotalPaid(order.getId());
    if (totalPaid.compareTo(order.getTotalPrice()) >= 0) {
      throw new PaymentException("E_PAYMENT003", "Order already fully paid");
    }
  }

  private void updatePaymentFromVNPayCallback(
      Long orderId, Map<String, String> params, boolean success) {
    Payment payment =
        paymentRepository.findFirstByOrderIdAndStatus(orderId, PaymentStatus.PENDING).orElse(null);

    if (payment == null) {
      log.warn("Pending payment not found for order: {}", orderId);
      return;
    }

    if (success) {
      payment.setStatus(PaymentStatus.COMPLETED);
      payment.setReferenceTransactionId(vnPayService.getTransactionNo(params));
      completeOrderPayment(payment.getOrder());
    } else {
      payment.setStatus(PaymentStatus.FAILED);
      payment.setDescription(vnPayService.getResponseMessage(params.get("vnp_ResponseCode")));
    }

    paymentRepository.save(payment);
    log.info("Payment {} updated to status: {}", payment.getId(), payment.getStatus());
  }

  private void updatePaymentFromMoMoCallback(Long orderId, String transactionId, boolean success) {
    Payment payment =
        paymentRepository.findFirstByOrderIdAndStatus(orderId, PaymentStatus.PENDING).orElse(null);

    if (payment == null) {
      log.warn("Pending payment not found for order: {}", orderId);
      return;
    }

    if (success) {
      payment.setStatus(PaymentStatus.COMPLETED);
      payment.setReferenceTransactionId(transactionId);
      completeOrderPayment(payment.getOrder());
    } else {
      payment.setStatus(PaymentStatus.FAILED);
    }

    paymentRepository.save(payment);
    log.info("Payment {} updated to status: {}", payment.getId(), payment.getStatus());
  }

  private void completeOrderPayment(Order order) {
    // We no longer force order.setStatus(OrderStatus.COMPLETED) here.
    // Payment completion just marks the payment as COMPLETED.
    // The actual order flow (e.g., pickupStorageOrder, completeOrder) will handle status
    // transitions.
    log.info("Order {} payment completed. Current status: {}", order.getId(), order.getStatus());
  }
}
