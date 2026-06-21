package com.fsocial.postservice.dto.Account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OwnerDTO {
    String id;
    String lastName;
    String firstName;
    String avatar;
}
