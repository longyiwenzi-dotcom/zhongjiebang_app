package cn.hrbzhongjiebang.cloud.gateway;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationFilterTest {
    @Test
    void rejectsProtectedRequestWithoutToken() {
        AuthenticationFilter filter = new AuthenticationFilter(mock(ReactiveStringRedisTemplate.class));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/cloud/api/houses/1").build());
        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void stripsSpoofedIdentityFromPublicRequest() {
        AuthenticationFilter filter = new AuthenticationFilter(mock(ReactiveStringRedisTemplate.class));
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/cloud/api/users/login").header("X-User-Id", "999").build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        GatewayFilterChain chain = next -> { forwarded.set(next); return Mono.empty(); };
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertNull(forwarded.get().getRequest().getHeaders().getFirst("X-User-Id"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void replacesSpoofedIdentityWithAuthenticatedUser() {
        ReactiveStringRedisTemplate redis = mock(ReactiveStringRedisTemplate.class);
        ReactiveValueOperations<String, String> values = mock(ReactiveValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(Mono.just("42"));
        AuthenticationFilter filter = new AuthenticationFilter(redis);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/cloud/api/houses/1")
                .header("Authorization", "Bearer 12345678901234567890123456789012")
                .header("X-User-Id", "999").build());
        AtomicReference<ServerWebExchange> forwarded = new AtomicReference<>();
        StepVerifier.create(filter.filter(exchange, next -> { forwarded.set(next); next.getResponse().setStatusCode(HttpStatus.OK); return next.getResponse().setComplete(); })).verifyComplete();
        assertEquals("42", forwarded.get().getRequest().getHeaders().getFirst("X-User-Id"));
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        assertEquals("", exchange.getResponse().getBodyAsString().block());
    }

    @SuppressWarnings("unchecked")
    @Test void missingOrUnavailableSessionNeverReachesService() {
        for (Mono<String> result : java.util.List.of(Mono.<String>empty(), Mono.<String>error(new RuntimeException("Redis unavailable")))) {
            ReactiveStringRedisTemplate redis=mock(ReactiveStringRedisTemplate.class);
            ReactiveValueOperations<String,String> values=mock(ReactiveValueOperations.class);
            when(redis.opsForValue()).thenReturn(values);when(values.get(anyString())).thenReturn(result);
            var exchange=MockServerWebExchange.from(MockServerHttpRequest.get("/cloud/api/houses/1").header("Authorization","Bearer 12345678901234567890123456789012"));
            var called=new java.util.concurrent.atomic.AtomicBoolean();
            StepVerifier.create(new AuthenticationFilter(redis).filter(exchange,next->{called.set(true);return Mono.empty();})).verifyComplete();
            assertEquals(false,called.get());assertEquals(HttpStatus.UNAUTHORIZED,exchange.getResponse().getStatusCode());
        }
    }
}
