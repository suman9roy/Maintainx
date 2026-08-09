package com.maintainx.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Logs every request the gateway handles: what came in, which route it
 * matched, where it was actually forwarded (after Eureka/load-balancer
 * resolution — this is the piece that's otherwise invisible), and what
 * came back.
 *
 * Ordered.LOWEST_PRECEDENCE means this runs LAST on the way in — i.e.
 * after routing has already been decided — so exchange attributes like
 * GATEWAY_ROUTE_ATTR and GATEWAY_REQUEST_URL_ATTR are already populated
 * by the time we read them. On the way out (the .then(...) below), it's
 * the first thing to see the response as it unwinds back up the chain.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String method = exchange.getRequest().getMethod().name();
        String path   = exchange.getRequest().getURI().getPath();
        long start    = System.currentTimeMillis();

        log.info("--> {} {}", method, path);

        return chain.filter(exchange)
                .doOnSuccess(v -> logOutcome(exchange, method, path, start, null))
                .doOnError(ex -> logOutcome(exchange, method, path, start, ex));
    }

    private void logOutcome(ServerWebExchange exchange, String method, String path, long start, Throwable ex) {

        long tookMs = System.currentTimeMillis() - start;

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI resolvedUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        HttpStatusCode status = exchange.getResponse().getStatusCode();

        String routeId = route != null ? route.getId() : "NO_ROUTE_MATCHED";
        String destination = resolvedUri != null ? resolvedUri.toString() : "UNRESOLVED";

        if (ex != null) {
            log.error("<-- {} {} FAILED route={} destination={} tookMs={}: {}",
                    method, path, routeId, destination, tookMs, ex.getMessage());
        } else {
            log.info("<-- {} {} status={} route={} destination={} tookMs={}",
                    method, path, status, routeId, destination, tookMs);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}