package com.huynqb.laundrylockerbackend.module.notification.mapper;

import com.huynqb.laundrylockerbackend.module.notification.dto.response.NotificationResponse;
import com.huynqb.laundrylockerbackend.module.notification.model.Notification;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/** MapStruct mapper for Notification entity. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

  NotificationResponse toResponse(Notification notification);

  List<NotificationResponse> toResponseList(List<Notification> notifications);
}
