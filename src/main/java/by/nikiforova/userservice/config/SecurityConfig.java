package by.nikiforova.userservice.config;

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

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/cards").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/activate").hasRole(ROLE_ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/deactivate").hasRole(ROLE_ADMIN)

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
