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

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        // Social features
                        .requestMatchers("/posts/**").permitAll()
                        .requestMatchers("/comments/**").permitAll()
                        .requestMatchers("/likes/**").permitAll()
                        .requestMatchers("/comment-reactions/**").permitAll()
                        // Virtual team / prediction
                        .requestMatchers("/virtual-teams/**").permitAll()
                        .requestMatchers("/player-profiles/**").permitAll()
                        .requestMatchers("/predictions/**").permitAll()
                        .requestMatchers("/owned-players/**").permitAll()
                        // Medical
                        .requestMatchers("/medical/**").hasAnyRole("PLAYER", "HEALTH_PROFESSIONAL", "COACH", "ADMIN")
                        // Teams
                        .requestMatchers("/api/teams/**").authenticated()
                        // Venues
                        .requestMatchers("/reservations/venues").authenticated()
                        .requestMatchers("/reservations/venue/**").authenticated()
                        .requestMatchers("/reservations/admin/**").hasRole("ADMIN")
                        .requestMatchers("/reservations/owner/**").hasRole("VENUE_OWNER")
                        .requestMatchers("/reservations/**").authenticated()
                        .requestMatchers("/venue/all").authenticated()
                        .requestMatchers("/venue/**").hasRole("VENUE_OWNER")
                        // Carpooling / Cars
                        .requestMatchers("/cars/**").authenticated()
                        .requestMatchers("/carpoolings/**").authenticated()
                        // Shop / Products
                        .requestMatchers("/products/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/upload-image").hasAnyRole("ADMIN", "VENUE_OWNER")
                        .requestMatchers(HttpMethod.POST, "/products").hasAnyRole("ADMIN", "VENUE_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/products/*").hasAnyRole("ADMIN", "VENUE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/products/*/reviews/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/products/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/*/reviews").authenticated()
                        .requestMatchers("/shops/**").permitAll()
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/orders/checkout").authenticated()
                        .requestMatchers("/orders/admin/**").hasRole("ADMIN")
                        .requestMatchers("/orders/**").authenticated()
                        // Player merch
                        .requestMatchers(HttpMethod.GET, "/player-merch/approved", "/player-merch/{id}").permitAll()
                        .requestMatchers("/player-merch/submit", "/player-merch/my-submissions").hasRole("PLAYER")
                        .requestMatchers(HttpMethod.PUT, "/player-merch/{id}").hasRole("PLAYER")
                        .requestMatchers(HttpMethod.DELETE, "/player-merch/{id}").hasRole("PLAYER")
                        .requestMatchers("/player-merch/admin/**").hasRole("ADMIN")
                        // Sponsors
                        .requestMatchers(HttpMethod.DELETE, "/sponsors/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/sponsors/upload-logo").permitAll()
                        .requestMatchers("/sponsors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sponsorships").permitAll()
                        .requestMatchers("/sponsorships/pending", "/sponsorships/active", "/sponsorships/team/**", "/sponsorships/event/**", "/sponsorships/venue/**", "/sponsorships/available-targets").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sponsorships/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/sponsorships/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sponsorships/admin/**").hasRole("ADMIN")
                        .requestMatchers("/sponsorships/submit", "/sponsorships/my-sponsorships", "/sponsorships/*/cancel", "/sponsorships/*/renew", "/sponsorships/*/payment-proof", "/sponsorships/*/payment-proof-file").hasRole("SPONSOR")
                        .requestMatchers("/sponsors/admin/**").hasRole("ADMIN")
                        .requestMatchers("/sponsorships/**").authenticated()
                        // Wallet / User
                        .requestMatchers("/wallet/**").authenticated()
                        .requestMatchers("/user/me").authenticated()
                        // Roles
                        .requestMatchers("/coach/**").hasRole("COACH")
                        .requestMatchers("/referee/**").hasRole("REFEREE")
                        .requestMatchers("/health/**").hasRole("HEALTH_PROFESSIONAL")
                        // Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
