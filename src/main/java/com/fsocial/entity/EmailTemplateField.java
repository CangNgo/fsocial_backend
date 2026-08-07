package com.fsocial.entity;

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
@Table(name = "email_template_field")
@SuperBuilder
public class EmailTemplateField extends AbstractEntity<String> {

    @Column(name = "name", length = 128, nullable = false)
    String name;

    @Column(name = "description", columnDefinition = "text")
    String description;
}
