package com.fsocial.entity;

import com.fsocial.enums.ConversationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "conversations")
public class Conversation extends  AbstractEntity<String>{

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16, nullable = false)
    ConversationType type;

    @Column(name = "name", length = 100)
    String name;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    String avatarUrl;

    @OneToMany(mappedBy = "conversation")
    List<Message> messages  = new ArrayList<>();
}
