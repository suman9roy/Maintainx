package com.maintainx.api_gateway.config;

import com.maintainx.api_gateway.util.CorsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the API gateway.
 *
 * Why this is a Java bean and NOT in application.yml globalcors:
 *
 * Spring Cloud Gateway's YAML-based globalcors is applied as a
 * regular gateway filter — it only adds CORS headers to responses
 * that flow through the full filter chain. When our
 * JwtAuthenticationFilter.reject() fires for a 401/403, it calls
 * exchange.getResponse().setComplete() directly, bypassing the rest
 * of the chain. The browser receives the rejection with no CORS headers
 * and reports a CORS error instead of a 401/403 — making frontend
 * debugging nearly impossible.
 *
 * CorsWebFilter is a WebFilter (lower level than gateway filters)
 * and wraps the ENTIRE request, including short-circuited rejections.
 * Setting its order to -2 ensures it runs BEFORE our JWT filter (-1),
 * so CORS headers are guaranteed on every response regardless of
 * what the JWT filter does.
 *
 * Preflight (OPTIONS) requests are handled here — they are completed
 * immediately with 200 + CORS headers so the browser's preflight
 * check succeeds before the JWT filter even runs.
 */
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;


    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // Only the explicitly listed origins — never "*" when
        // allowCredentials is true (browser blocks this combination)
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());

        // All HTTP methods your frontend will use
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()   // required for preflight
        ));

        // Allow all request headers — specifically needed for
        // Authorization: Bearer <token> and Content-Type: application/json
        config.addAllowedHeader("*");

        // Required for the frontend to read response headers (e.g. for
        // custom headers added by services in the future)
        config.addExposedHeader("*");

        // Required for Authorization header to be sent with requests
        // from the browser. Cannot be combined with allowedOrigins: "*"
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour — reduces OPTIONS round trips
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // Order -2 → runs before JwtAuthenticationFilter (order -1)
        // This guarantees CORS headers on ALL responses including 401/403
        CorsWebFilter filter = new CorsWebFilter(source);
        return filter;
    }
}