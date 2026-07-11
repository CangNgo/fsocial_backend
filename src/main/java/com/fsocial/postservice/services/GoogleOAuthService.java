package com.fsocial.postservice.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

import java.time.LocalDate;

public interface GoogleOAuthService {
    GoogleIdToken.Payload verify(String idToken);
}
