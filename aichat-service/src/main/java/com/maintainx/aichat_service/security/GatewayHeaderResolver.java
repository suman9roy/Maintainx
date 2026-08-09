package com.maintainx.aichat_service.security;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GatewayHeaderResolver {

    public GatewayContext resolve(HttpServletRequest request) {

        return new GatewayContext(
                request.getHeader(GatewayConstants.USER_ID),
                request.getHeader(GatewayConstants.ROLE),
                request.getHeader(GatewayConstants.APARTMENT_ID)
        );
    }

}