package by.nikiforova.userservice.config;

import by.nikiforova.userservice.security.InternalApiKeyFilter;
import by.nikiforova.userservice.security.JsonAccessDeniedHandler;
import by.nikiforova.userservice.security.JsonAuthenticationEntryPoint;
import by.nikiforova.userservice.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static by.nikiforova.userservice.constant.Constants.ROLE_ADMIN;
import static by.nikiforova.userservice.constant.Constants.ROLE_INTERNAL;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole(ROLE_INTERNAL)
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasAnyRole(ROLE_ADMIN, ROLE_INTERNAL)

                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/users/by-ids").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cards").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/activate").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/deactivate").hasRole(ROLE_ADMIN)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalApiKeyFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
