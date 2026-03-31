package tn.esprit.pi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Auth Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid Authorization header found");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("Extracted token: " + token.substring(0, Math.min(20, token.length())) + "...");
        String email;
        String role;

        try {
            email = jwtService.extractEmail(token);
            role = jwtService.extractRole(token);
            System.out.println("JWT Claims - Email: " + email + ", Role: " + role);
        } catch (Exception e) {
            System.out.println("JWT parsing failed: " + e.getMessage());
            log.warn("JWT parsing failed for {}: {}", request.getRequestURI(), e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // Build authentication directly from JWT claims — NO database query needed
        if (email != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorities = List.of(new SimpleGrantedAuthority(role));
            var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authenticated user {} with role {} for {}", email, role, request.getRequestURI());
        } else {
            System.out.println("Authentication NOT set - email: " + email + ", role: " + role
                    + ", existing auth: " + SecurityContextHolder.getContext().getAuthentication());
        }

        filterChain.doFilter(request, response);
    }
}
