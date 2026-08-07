package com.fsocial.repository;

import com.fsocial.entity.NotificationTemplate;
import com.fsocial.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, String> {

    Optional<NotificationTemplate> findByType(NotificationType type);
}
