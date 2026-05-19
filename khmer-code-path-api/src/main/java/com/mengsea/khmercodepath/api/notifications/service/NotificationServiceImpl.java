package com.mengsea.khmercodepath.api.notifications.service;

import com.mengsea.khmercodepath.api.notifications.payload.NotificationListPayload;
import com.mengsea.khmercodepath.api.notifications.payload.NotificationPayload;
import com.mengsea.khmercodepath.api.notifications.payload.UnreadCountPayload;
import com.mengsea.khmercodepath.commons.constant.ExceptionCode;
import com.mengsea.khmercodepath.commons.constant.NotificationType;
import com.mengsea.khmercodepath.commons.domain.LmsClass;
import com.mengsea.khmercodepath.commons.domain.Notification;
import com.mengsea.khmercodepath.commons.domain.User;
import com.mengsea.khmercodepath.commons.exception.BusinessException;
import com.mengsea.khmercodepath.commons.repository.LmsClassRepository;
import com.mengsea.khmercodepath.commons.repository.NotificationRepository;
import com.mengsea.khmercodepath.commons.repository.UserRepository;
import com.mengsea.khmercodepath.commons.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final LmsClassRepository lmsClassRepository;

    @Override
    @Transactional(readOnly = true)
    public NotificationListPayload list(String filter, int limit, int offset) {
        User me = SecurityUtils.requireCurrentUser();
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        int safeOffset = Math.max(offset, 0);
        Pageable pageable = PageRequest.of(safeOffset / safeLimit, safeLimit);

        Page<Notification> page = switch (normalizeFilter(filter)) {
            case "unread" -> notificationRepository.findByUser_UuidAndDeletedFalseAndReadOrderByCreatedAtDesc(
                    me.getUuid(), false, pageable);
            case "read" -> notificationRepository.findByUser_UuidAndDeletedFalseAndReadOrderByCreatedAtDesc(
                    me.getUuid(), true, pageable);
            default -> notificationRepository.findByUser_UuidAndDeletedFalseOrderByCreatedAtDesc(
                    me.getUuid(), pageable);
        };

        long unread = notificationRepository.countByUser_UuidAndDeletedFalseAndReadFalse(me.getUuid());
        return NotificationListPayload.builder()
                .items(page.getContent().stream().map(this::toPayload).toList())
                .total(page.getTotalElements())
                .unreadCount(unread)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountPayload unreadCount() {
        User me = SecurityUtils.requireCurrentUser();
        long count = notificationRepository.countByUser_UuidAndDeletedFalseAndReadFalse(me.getUuid());
        return UnreadCountPayload.builder().count(count).build();
    }

    @Override
    @Transactional
    public NotificationPayload markRead(Long id) {
        User me = SecurityUtils.requireCurrentUser();
        Notification entity = notificationRepository.findByIdAndUser_UuidAndDeletedFalse(id, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.NOTIFICATION_NOT_FOUND));
        entity.setRead(true);
        notificationRepository.save(entity);
        return toPayload(entity);
    }

    @Override
    @Transactional
    public void markAllRead() {
        User me = SecurityUtils.requireCurrentUser();
        notificationRepository.markAllReadForUser(me.getUuid());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User me = SecurityUtils.requireCurrentUser();
        Notification entity = notificationRepository.findByIdAndUser_UuidAndDeletedFalse(id, me.getUuid())
                .orElseThrow(() -> new BusinessException(ExceptionCode.NOTIFICATION_NOT_FOUND));
        entity.setDeleted(true);
        notificationRepository.save(entity);
    }

    @Override
    @Transactional
    public NotificationPayload createForUser(
            String userUuid,
            NotificationType type,
            String title,
            String message,
            Long classId,
            String resourceType,
            Long resourceId
    ) {
        User user = userRepository.findByUuidAndDeletedFalse(userUuid)
                .orElseThrow(() -> new BusinessException(ExceptionCode.USER_NOT_FOUND));

        Notification entity = new Notification();
        entity.setUser(user);
        entity.setType(type);
        entity.setTitle(title.trim());
        entity.setMessage(message);
        entity.setRead(false);
        entity.setDeleted(false);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);

        if (classId != null) {
            LmsClass lmsClass = lmsClassRepository.findByIdAndDeletedFalse(classId).orElse(null);
            entity.setLmsClass(lmsClass);
        }

        notificationRepository.save(entity);
        return toPayload(entity);
    }

    NotificationPayload toPayload(Notification entity) {
        LmsClass lmsClass = entity.getLmsClass();
        return NotificationPayload.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .classId(lmsClass != null ? lmsClass.getId() : null)
                .className(lmsClass != null ? lmsClass.getName() : null)
                .resourceType(entity.getResourceType())
                .resourceId(entity.getResourceId())
                .read(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static String normalizeFilter(String filter) {
        if (filter == null) {
            return "all";
        }
        return switch (filter.trim().toLowerCase()) {
            case "unread", "read" -> filter.trim().toLowerCase();
            default -> "all";
        };
    }
}
