package com.example.ledgerpro.auth;

public class AuthPrincipal {

    private final Long userId;
    private final String username;
    private final String displayName;

    public AuthPrincipal(Long userId, String username, String displayName) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}
