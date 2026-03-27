package com.java_springboot_3rd.user_core;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Vừa bước qua cửa là phát thẻ tên ngay lập tức
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", "Trace-" + traceId);

        try { // MỞ KHỐI TRY

            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    String username = jwtUtil.extractUsername(token);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        String role = jwtUtil.extractRole(token);
                        if (role != null && !role.startsWith("ROLE_")) {
                            role = "ROLE_" + role;
                        }

                        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.info("🎫 Khách hàng {} (Chức vụ: {}) hợp lệ! Cho qua!", username, authToken.getAuthorities());
                    }
                } catch (Exception e) {
                    log.error("❌ Token không hợp lệ hoặc đã hết hạn!");
                }
            }

            // 2. Cho phép mang theo thẻ tên đi vào Controller và Service
            filterChain.doFilter(request, response);

        } finally {
            // ĐÓNG KHỐI TRY KHỔNG LỒ VÀ THU HỒI THẺ TÊN TẠI ĐÂY
            // Dù trong Controller có sập hay thành công thì khách đi ra vẫn sẽ bị thu lại thẻ
            MDC.remove("traceId");
        }
    }
}