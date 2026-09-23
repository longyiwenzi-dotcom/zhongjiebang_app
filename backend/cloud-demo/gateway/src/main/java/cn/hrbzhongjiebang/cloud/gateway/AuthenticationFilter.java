package cn.hrbzhongjiebang.cloud.gateway;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/cloud/api/users/register", "/cloud/api/users/login", "/actuator/health");
    private final ReactiveStringRedisTemplate redis;

    public AuthenticationFilter(ReactiveStringRedisTemplate redis) { this.redis = redis; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (PUBLIC_PATHS.contains(path)) return chain.filter(stripIdentity(exchange));
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) return unauthorized(exchange);
        String token = authorization.substring(7).trim();
        if (token.length() < 32 || token.length() > 256) return unauthorized(exchange);
        return redis.opsForValue().get("zjb:session:" + sha256(token))
                .onErrorResume(error -> Mono.empty())
                .filter(userId -> userId.matches("[1-9][0-9]*"))
                .map(userId -> java.util.Optional.of(userId))
                .defaultIfEmpty(java.util.Optional.empty())
                .flatMap(userId -> userId.isPresent()
                        ? chain.filter(withIdentity(exchange, userId.get()))
                        : unauthorized(exchange));
    }

    private static ServerWebExchange stripIdentity(ServerWebExchange exchange) {
        return exchange.mutate().request(exchange.getRequest().mutate().headers(headers -> headers.remove("X-User-Id")).build()).build();
    }

    private static ServerWebExchange withIdentity(ServerWebExchange exchange, String userId) {
        return exchange.mutate().request(exchange.getRequest().mutate().headers(headers -> {
            headers.remove("X-User-Id");
            headers.set("X-User-Id", userId);
        }).build()).build();
    }

    private static Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] body = "{\"code\":\"UNAUTHORIZED\",\"message\":\"请先登录\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
    @Override public int getOrder() { return -100; }
}
