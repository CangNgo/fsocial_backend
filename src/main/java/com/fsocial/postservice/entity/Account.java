package com.fsocial.postservice.entity;

import com.fsocial.postservice.enums.AuthProvider;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

@Document(collection = "accounts")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public class Account extends AbstractEntity<String> {

    @Indexed(unique = true)
    @Field("username")
    String username;

    @Field("password")
    String password;

    @Field("firstName")
    String firstName;

    @Field("lastName")
    String lastName;

    @Field("displayName")
    String displayName;

    @Field("dob")
    LocalDate dob;

    @Field("gender")
    int gender;

    @Field("avatar")
    String avatar;

    @Field("background")
    String background;

    @Field("bio")
    String bio;

    @Field("address")
    String address;

    boolean isKOL = false;

    @DBRef
    Role role;

    @DBRef
    Token token;

    String email;

    boolean status = true;

    @Field()
    private AuthProvider provider;

    @Field(name = "google_id")
    private String googleId;
}
