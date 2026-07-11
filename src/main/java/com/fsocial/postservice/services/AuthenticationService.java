package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.google.GoogleDTORequest;
import com.fsocial.postservice.dto.request.AccountLoginRequest;
import com.fsocial.postservice.dto.response.AuthenticationResponse;
import com.fsocial.postservice.dto.response.IntrospectResponse;
import com.fsocial.postservice.exception.AccountCheckedException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    AuthenticationResponse login(AccountLoginRequest request, String userAgent, HttpServletRequest httpRequest) throws AccountCheckedException;
    IntrospectResponse introspect(String token);
    AuthenticationResponse loginWithGoogle(GoogleDTORequest request, String userAgent, HttpServletRequest httpRequest);
}
