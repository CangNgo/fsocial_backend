package com.fsocial.services;

import com.fsocial.dto.google.GoogleDTORequest;
import com.fsocial.dto.request.AccountLoginRequest;
import com.fsocial.dto.response.AuthenticationResponse;
import com.fsocial.dto.response.IntrospectResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    AuthenticationResponse login(AccountLoginRequest request, String userAgent, HttpServletRequest httpRequest);
    IntrospectResponse introspect(String token);
    AuthenticationResponse loginWithGoogle(GoogleDTORequest request, String userAgent, HttpServletRequest httpRequest);
}
