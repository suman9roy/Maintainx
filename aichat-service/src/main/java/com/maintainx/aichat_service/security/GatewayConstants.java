package com.maintainx.aichat_service.security;



/**
 * Standard HTTP headers forwarded by the API Gateway after
 * successful JWT authentication.
 */
public final class GatewayConstants {

    private GatewayConstants() {
        // Prevent instantiation
    }

    public static final String USER_ID = "X-User-Id";

    public static final String ROLE = "X-User-Role";

    public static final String APARTMENT_ID = "X-Apartment-Id";
}