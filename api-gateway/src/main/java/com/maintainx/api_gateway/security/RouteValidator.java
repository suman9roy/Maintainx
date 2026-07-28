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
            "/auth/login",
            "/apartments/public"        // NEW — lets a prospective resident pick an apartment before they have any account
    );

    public static final List<String> authenticatedEndpoints = List.of(
            "/residents/",
            "/complaints/resident/",
            "/complaints",
            "/notices",
            "/notices/type/",
            "/expenses",
            "/expenses/category/",
            "/expenses/fund-summary",
            "/maintenance/",
            "/payments"
    );

    public Predicate<String> isSecured =
            path -> openEndpoints.stream().noneMatch(path::startsWith);
}
