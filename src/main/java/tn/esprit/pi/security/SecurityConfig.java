package tn.esprit.pi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.esprit.pi.security.jwt.JwtAuthFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;

    // ─── Authentication Provider ──────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider(); // Use empty constructor
        p.setUserDetailsService(userDetailsService); // Use setter for service
        p.setPasswordEncoder(passwordEncoder);       // Use setter for encoder
        return p;
    }

    // ─── Authentication Manager ───────────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─── Security Filter Chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/medical/**").hasAnyRole("PLAYER", "HEALTH_PROFESSIONAL", "COACH", "ADMIN")
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/upload-image").hasAnyRole("ADMIN", "VENUE_OWNER")
                        .requestMatchers(HttpMethod.POST, "/sponsors/upload-logo").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products").hasAnyRole("ADMIN", "VENUE_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/products/*").hasAnyRole("ADMIN", "VENUE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/products/*/reviews/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/products/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/*/reviews").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/sponsors/admin/**").hasRole("ADMIN")
                        .requestMatchers("/sponsors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sponsorships").permitAll()
                        .requestMatchers("/sponsorships/pending", "/sponsorships/active", "/sponsorships/team/**", "/sponsorships/event/**", "/sponsorships/venue/**", "/sponsorships/available-targets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sponsorships/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/sponsorships/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sponsorships/admin/**").hasRole("ADMIN")
                        .requestMatchers("/sponsorships/submit", "/sponsorships/my-sponsorships", "/sponsorships/*/cancel", "/sponsorships/*/renew", "/sponsorships/*/payment-proof", "/sponsorships/*/payment-proof-file").hasRole("SPONSOR")
                        .requestMatchers("/sponsors/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/orders/checkout").authenticated()
                        .requestMatchers("/orders/admin/**").hasRole("ADMIN")
                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/coach/**").hasRole("COACH")
                        .requestMatchers("/referee/**").hasRole("REFEREE")
                        .requestMatchers("/health/**").hasRole("HEALTH_PROFESSIONAL")
                        .requestMatchers("/venue/**").hasRole("VENUE_OWNER")
                        .requestMatchers("/wallet/**").authenticated()
                        .requestMatchers("/shops/**").permitAll()
                        .requestMatchers("/sponsorships/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─── CORS Configuration ───────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
