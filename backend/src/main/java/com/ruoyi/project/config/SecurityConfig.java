package com.ruoyi.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean("securityCorsSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean("acgSecurityFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/anime/bangumi/**", "/api/anime/bgm/**", "/api/follow/status/bangumi/**").permitAll()
                .requestMatchers("/api/anime/list", "/api/anime/page", "/api/anime/library/**", "/api/anime/featured/**", "/api/anime/calendar", "/api/anime/{id}", "/api/anime/sync/**").permitAll()
                .requestMatchers("/api/admin/user/**").permitAll()
                .requestMatchers("/api/comment/page").permitAll()
                .requestMatchers("/api/article/**", "/api/comment/**", "/api/follow/list").permitAll()
                .requestMatchers("/api/user/*/profile", "/api/user/*/articles", "/api/user/*/comments", "/api/user/*/follows", "/api/user/*/likes", "/api/user/search", "/api/user/follow/status").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/item/**", "/api/gacha/**", "/api/transaction/**", "/api/delivery/**", "/api/asset/**", "/api/market/**", "/api/address/**", "/api/points/**", "/api/message/**", "/api/fragment/**", "/api/redeem/**", "/api/recharge/**", "/api/synthesize/**", "/api/redeem-product/**", "/api/admin/redeem-product/**", "/api/admin/redeem/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthenticationTokenFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
