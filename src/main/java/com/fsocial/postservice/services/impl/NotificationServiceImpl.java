package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.ActorSnapshotDTO;
import com.fsocial.postservice.dto.ApiResponse;
import com.fsocial.postservice.dto.notification.NoticeRequest;
import com.fsocial.postservice.dto.notification.NotificationCursorResponse;
import com.fsocial.postservice.dto.notification.NotificationDTO;
import com.fsocial.postservice.dto.notification.NotificationResponse;
import com.fsocial.postservice.entity.Account;
import com.fsocial.postservice.entity.Notification;
import com.fsocial.postservice.enums.NotificationConst;
import com.fsocial.postservice.enums.NotificationType;
import com.fsocial.postservice.repository.AccountRepository;
import com.fsocial.postservice.repository.NotificationRepository;
import com.fsocial.postservice.services.NotificaitonService;
import com.fsocial.postservice.util.DisplayNameUtils;
import com.fsocial.postservice.util.DateUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificaitonService {

    RestTemplate restTemplate;
    NotificationRepository notificationRepository;
    AccountRepository accountRepository;

    @NonFinal
    @Value("${app.services.notification}")
    String notificationServiceUrl;

    @Override
    public NotificationResponse createNotification(NoticeRequest notificationRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NoticeRequest> entity = new HttpEntity<>(notificationRequest, headers);

        ResponseEntity<ApiResponse<NotificationResponse>> response = restTemplate.exchange(
                notificationServiceUrl + "/notification/notice",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        ApiResponse<NotificationResponse> body = response.getBody();
        return body != null ? body.getData() : null;
    }

    @Override
    public void notifcationCreateConsumer(NotificationDTO dto) {
        NotificationConst notiConst = NotificationConst.from(dto.type());
        Object bodyArg = (dto.type() == NotificationType.LOGIN)
                ? DateUtils.getNow()
                : displayNameOf(dto.senderId());

        Notification entity = Notification.builder()
                .title(notiConst.getTitle())
                .body(String.format(notiConst.getBody(), bodyArg))
                .recipientId(dto.recipientId())
                .senderId(dto.senderId())
                .type(dto.type())
                .build();
        notificationRepository.save(entity);
    }

    private String displayNameOf(String senderId) {
        if (senderId == null) return "";
        return accountRepository.findById(senderId).map(DisplayNameUtils::build).orElse("");
    }

    static final int PAGE_SIZE = 10;

    @Override
    public NotificationCursorResponse getNotifications(String userId, String cursor) {

        var pageable = PageRequest.of(0, PAGE_SIZE);
        List<NotificationResponse> found = (cursor == null || cursor.isBlank())
                ? notificationRepository.findByRecipientIdOrderByIdDesc(userId, pageable)
                : notificationRepository.findByRecipientIdAndIdLessThanOrderByIdDesc(
                        userId, cursor, pageable);

        boolean hasMore = found.size() > PAGE_SIZE;
        List<NotificationResponse> items = hasMore ? found.subList(0, PAGE_SIZE) : found;
        String nextCursor = hasMore ? items.getLast().getId() : null;

        enrichActors(items);

        return new NotificationCursorResponse(items, nextCursor, hasMore);
    }

    /** Lookup Account theo senderId/aggregatedSenderIds, gán vào actor/aggregatedActors */
    private void enrichActors(List<NotificationResponse> items) {
        if (items.isEmpty()) return;

        List<String> senderIds = items.stream()
                .flatMap(n -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.ofNullable(n.getSenderId()),
                        n.getAggregatedSenderIds() == null ? java.util.stream.Stream.empty()
                                : n.getAggregatedSenderIds().stream()))
                .distinct()
                .toList();

        Map<String, Account> accountMap = accountRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        for (NotificationResponse n : items) {
            n.setActor(toActorSnapshot(accountMap.get(n.getSenderId())));
            if (n.getAggregatedSenderIds() != null) {
                n.setAggregatedActors(n.getAggregatedSenderIds().stream()
                        .map(accountMap::get)
                        .map(this::toActorSnapshot)
                        .filter(java.util.Objects::nonNull)
                        .toList());
            }
        }
    }

    private ActorSnapshotDTO toActorSnapshot(Account account) {
        if (account == null) return null;
        return ActorSnapshotDTO.builder()
                .userId(account.getId())
                .displayName(DisplayNameUtils.build(account))
                .avatar(account.getAvatar())
                .build();
    }

    @Override
    public long getCountNotificationByRecipientId(String recipient) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipient);
    }

    @Override
    public NotificationResponse readNotification(String notificationId) {

        if(notificationId.isEmpty()){
//            throw new Notfound
        }

        return null;
    }
}
