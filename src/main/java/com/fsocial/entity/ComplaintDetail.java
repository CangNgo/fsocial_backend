package com.fsocial.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "complaint_detail")
public class ComplaintDetail {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(length = 36)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    Complaint complaint;

    @Column(name = "user_id", length = 36, nullable = false)
    String userId;

    @Column(name = "term_of_service_id", length = 36)
    String termOfServiceId;

    @Column(name = "create_datetime", nullable = false)
    @Builder.Default
    LocalDateTime createDatetime = LocalDateTime.now();
}
