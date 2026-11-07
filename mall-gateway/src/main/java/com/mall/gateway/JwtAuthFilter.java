package com.mall.gateway;

import com.mall.common.core.Constants;
import com.mall.common.core.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_LOGIN = "/api/auth/login";
    private static final String AUTH_REGISTER = "/api/auth/register";

    @Value("${mall.jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Internal headers are only trusted when injected by this gateway.
        ServerHttpRequest cleanRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(Constants.HEADER_USER_ID);
                    headers.remove(Constants.HEADER_USERNAME);
                })
                .build();
        ServerWebExchange cleanExchange = exchange.mutate().request(cleanRequest).build();

        if (!path.startsWith("/api/") || isPublic(path, request.getMethod())) {
            return chain.filter(cleanExchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(Constants.BEARER_PREFIX)) {
            return unauthorized(exchange, "未登录，请先登录");
        }

        try {
            Claims claims = JwtUtil.parse(authorization.substring(Constants.BEARER_PREFIX.length()), secret);
            ServerHttpRequest authenticated = cleanRequest.mutate()
                    .header(Constants.HEADER_USER_ID, claims.getSubject())
                    .header(Constants.HEADER_USERNAME, String.valueOf(claims.get("username")))
                    .build();
            return chain.filter(cleanExchange.mutate().request(authenticated).build());
        } catch (Exception e) {
            return unauthorized(exchange, "登录已过期，请重新登录");
        }
    }

    private boolean isPublic(String path, HttpMethod method) {
        if (AUTH_LOGIN.equals(path) || AUTH_REGISTER.equals(path)) {
            return true;
        }
        // Admin console runs without authentication for now; secure before production.
        if (path.startsWith("/api/admin/")) {
            return true;
        }
        return HttpMethod.GET.equals(method)
                && (path.startsWith("/api/products") || path.startsWith("/api/categories"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
