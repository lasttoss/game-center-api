package io.dy.gamecenter.api.configs.jwt;

import io.dy.gamecenter.api.services.AuthService;
import io.dy.gamecenter.api.services.UserDetailService;
import io.dy.gamecenter.api.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserDetailService userDetailService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String jwtToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer")) {
            jwtToken = authHeader;
        }

        if (jwtToken == null || jwtToken.isEmpty() || jwtToken.length() < 7) {//
            filterChain.doFilter(request, response);
        } else {
            jwtToken = jwtToken.substring(7);
            if (jwtToken.length() > 0) {
                String userId = jwtUtils.extractUsername(jwtToken);
                if (userId == null) {
                    filterChain.doFilter(request, response);
                    return;
                }
                UserDetails userDetail = userDetailService.loadUserByUsername(userId);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
            filterChain.doFilter(request, response);
        }
    }
}

