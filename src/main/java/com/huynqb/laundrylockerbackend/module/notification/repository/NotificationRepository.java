package com.huynqb.laundrylockerbackend.module.notification.repository;

import com.huynqb.laundrylockerbackend.module.notification.enums.NotificationStatus;
import com.huynqb.laundrylockerbackend.module.notification.model.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for Notification entity. */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  /** Find all notifications for a user, ordered by creation date desc. */
  Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  /** Find all notifications for a user. */
  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

  /** Find unread notifications for a user. */
  List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(
      Long userId, NotificationStatus status);

  /** Count unread notifications for a user. */
  long countByUserIdAndStatus(Long userId, NotificationStatus status);

  /** Mark all notifications as read for a user. */
  @Modifying
  @Query(
      "UPDATE Notification n SET n.status = :status, n.readAt = CURRENT_TIMESTAMP WHERE n.user.id = :userId AND n.status = :currentStatus")
  int markAllAsRead(
      @Param("userId") Long userId,
      @Param("status") NotificationStatus status,
      @Param("currentStatus") NotificationStatus currentStatus);

  /** Delete all notifications for a user. */
  void deleteByUserId(Long userId);
}
