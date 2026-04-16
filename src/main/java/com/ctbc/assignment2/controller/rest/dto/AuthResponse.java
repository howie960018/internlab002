package com.ctbc.assignment2.controller.rest.dto;

public class AuthResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresInMs;

    public AuthResponse(String accessToken, String tokenType, long expiresInMs) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresInMs = expiresInMs;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }
}
