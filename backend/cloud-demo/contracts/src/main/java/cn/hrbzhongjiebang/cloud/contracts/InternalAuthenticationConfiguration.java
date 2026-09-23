package cn.hrbzhongjiebang.cloud.contracts;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
@Configuration
public class InternalAuthenticationConfiguration {
 @Bean public OncePerRequestFilter internalAuthentication(@Value("${app.internal-token:}") String expected) {
  if(expected.length()<32) throw new IllegalStateException("INTERNAL_SERVICE_TOKEN must have at least 32 characters");
  return new OncePerRequestFilter() {
   @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
    if(req.getRequestURI().equals("/actuator/health")){chain.doFilter(req,res);return;}
    String actual=req.getHeader("X-Internal-Token");
    if(actual==null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),actual.getBytes(StandardCharsets.UTF_8))){res.sendError(401);return;}
    chain.doFilter(req,res);
   }
  };
 }
}
