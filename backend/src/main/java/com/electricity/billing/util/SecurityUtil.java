package com.electricity.billing.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Small helper to read the currently authenticated user id from the security context. */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context.");
        }
        return authentication.getName();
    }
}
