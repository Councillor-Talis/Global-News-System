package com.gns.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gns.common.Result;
import com.gns.utils.JwtUtil;
import com.gns.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 没有 token，直接放行（公开接口不需要token）
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // 验证 token 格式
            if (!jwtUtil.isValid(token)) {
                writeUnauthorized(response);
                return;
            }

            // 验证 Redis 中是否存在（防止登出后token仍有效）
            Long userId = jwtUtil.getUserId(token);
            String redisToken = redisUtil.get("token:access:" + userId);
            if (redisToken == null || !redisToken.equals(token)) {
                writeUnauthorized(response);
                return;
            }

            // 解析用户信息，放入 Spring Security 上下文
            Claims claims = jwtUtil.parseToken(token);
            String role = claims.get("role", Integer.class) == 1 ? "ROLE_ADMIN" : "ROLE_USER";

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId, token, List.of(new SimpleGrantedAuthority(role))
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            writeUnauthorized(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.fail(401, "未登录或Token已过期"))
        );
    }
}