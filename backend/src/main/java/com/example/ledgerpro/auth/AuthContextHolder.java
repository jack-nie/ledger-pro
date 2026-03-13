package com.example.ledgerpro.auth;

public final class AuthContextHolder {

    private static final ThreadLocal<AuthPrincipal> HOLDER = new ThreadLocal<AuthPrincipal>();

    private AuthContextHolder() {
    }

    public static void set(AuthPrincipal principal) {
        HOLDER.set(principal);
    }

    public static AuthPrincipal get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
