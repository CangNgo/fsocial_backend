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
@Table(name = "term_of_service")
@SuperBuilder
public class TermOfServices extends AbstractEntity<String> {

    @Column(name = "name", nullable = false, columnDefinition = "text")
    String name;

    @Column(name = "status", nullable = false)
    @Builder.Default
    Boolean status = true;
}
