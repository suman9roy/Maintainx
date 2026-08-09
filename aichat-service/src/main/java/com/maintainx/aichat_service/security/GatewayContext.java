package com.maintainx.aichat_service.security;

/**
 * Represents the authenticated user context forwarded by the API Gateway.
 *
 * The API Gateway validates the JWT and forwards trusted user information
 * using request headers.
 *
 * This service must never validate or parse JWT tokens directly.
 */
public record GatewayContext(

        String userId,

        String role,

        String apartmentId

) {
}