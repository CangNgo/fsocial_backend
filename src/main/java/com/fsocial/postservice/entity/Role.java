package com.fsocial.postservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Entity
@Table(name = "role")
public class Role extends AbstractEntity<String> {

    @Column(name = "name", length = 64, nullable = false, unique = true)
    String name;

    @Column(name = "description", columnDefinition = "text")
    String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_name")
    )
    @Builder.Default
    Set<Permission> permissions = new HashSet<>();
}
