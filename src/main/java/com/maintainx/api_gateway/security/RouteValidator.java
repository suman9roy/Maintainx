package com.maintainx.api_gateway.security;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;


@Component
public class RouteValidator {

    /**
     * Completely public — no JWT required at all.
     */
    public static final List<String> openEndpoints = List.of(
            "/auth/register",
            "/auth/login"
    );

    /**
     * Any valid JWT is enough — both ADMIN and RESIDENT can access.
     * Fine-grained ownership checks happen inside the service layer.
     *
     * Examples:
     *   GET /residents/{id}              → service checks userId matches
     *   GET /complaints/resident/{email} → service checks userId matches
     *   GET /notices/**                  → all residents can read
     *   GET /expenses/**                 → all residents can read
     *   GET /maintenance/{flatNumber}    → resident sees own bills
     */
    public static final List<String> authenticatedEndpoints = List.of(
            "/residents/",              // GET /{id} only — POST/DELETE blocked below
            "/complaints/resident/",    // GET own complaints
            "/complaints",              // POST raise complaint
            "/notices",                 // GET all notices
            "/notices/type/",           // GET by type
            "/expenses",                // GET all expenses (residents can view)
            "/expenses/category/",      // GET by category
            "/expenses/fund-summary",   // GET fund summary
            "/maintenance/",            // GET /maintenance/{flatNumber} or /bill/{id}
            "/payments"                 // POST create-order / verify
    );

    // ── Predicates used by JwtAuthenticationFilter ────────────────────────────

    /**
     * True when the route needs JWT validation (i.e. not in openEndpoints).
     */
    public Predicate<String> isSecured =
            path -> openEndpoints.stream().noneMatch(path::startsWith);
}
