package com.huynqb.laundrylockerbackend.module.order.enums;

/** Order status enum following the order lifecycle. */
public enum OrderStatus {
  INITIALIZED, // Order created, waiting for customer to put items
  RESERVED, // Order reserved but not yet started
  WAITING, // Items placed, waiting for staff to collect
  COLLECTED, // Staff collected items from locker
  PROCESSING, // Items being processed at store
  READY, // Items ready to be returned
  RETURNED, // Items returned to locker
  COMPLETED, // Customer received items, order completed
  CANCELED // Order canceled
}
