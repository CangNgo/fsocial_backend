package com.fsocial.postservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "email_template")
@SuperBuilder
public class EmailTemplate extends AbstractEntity<String> {
    // id đã được kế thừa từ AbstractEntity

    @Column(name = "name", length = 128, nullable = false)
    String name;

    @Column(name = "content", columnDefinition = "text")
    String content;

    @Column(name = "is_active", nullable = false)
    boolean isActive;

    @Column(name = "is_default", nullable = false)
    boolean isDefault;
}
