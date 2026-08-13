package com.fsocial.entity;

import com.fsocial.util.UuidV7Generator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEntity<T extends Serializable> implements Serializable {

    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = UuidV7Generator.class)
    @Column()
    private String id;

    @CreatedBy
    @Column(name = "created_by" )
    private T createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 36)
    private T updatedBy;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
