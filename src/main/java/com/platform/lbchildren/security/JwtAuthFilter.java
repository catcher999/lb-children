package com.platform.lbchildren.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = extractToken(header);
        if (token != null && jwtUtil.validateToken(token)) {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);
            Long userId = claims.get("userId", Long.class);
            UserPrincipal principal = new UserPrincipal(username, role, userId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal, null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从 Authorization 头提取 token，兼容：
     * - 大小写不敏感的 "Bearer" 前缀
     * - 前缀后多余空格（如 "Bearer  xxx"）
     * - token 被引号包裹（复制粘贴常见）
     * 无法提取时返回 null
     */
    private String extractToken(String header) {
        if (header == null || !header.regionMatches(true, 0, "Bearer", 0, "Bearer".length())) {
            return null;
        }
        String token = header.substring("Bearer".length()).trim();
        if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            token = token.substring(1, token.length() - 1).trim();
        }
        return token.isEmpty() ? null : token;
    }
}