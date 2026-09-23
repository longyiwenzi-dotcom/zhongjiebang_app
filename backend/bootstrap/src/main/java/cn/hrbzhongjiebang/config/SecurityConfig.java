package cn.hrbzhongjiebang.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter tokens) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/swagger-ui/**", "/zhongjiebang-demo/swagger-ui/**", "/zhongjiebang-demo/docs", "/zhongjiebang-demo/v3/api-docs/**").permitAll()
                        .requestMatchers("/zhongjiebang-demo/", "/zhongjiebang-demo/index.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/zhongjiebang-demo/api/v1/auth/register", "/zhongjiebang-demo/api/v1/auth/login/password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/zhongjiebang-demo/api/v1/houses").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"请先登录\"}");
                }))
                .addFilterBefore(tokens, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
