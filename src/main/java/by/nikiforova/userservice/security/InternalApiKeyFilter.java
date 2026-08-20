package by.nikiforova.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static by.nikiforova.userservice.constant.Constants.INTERNAL_KEY_HEADER;
import static by.nikiforova.userservice.constant.Constants.ROLE_INTERNAL;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String internalKey;

    public InternalApiKeyFilter(@Value("${app.internal-key}") String internalKey) {
        this.internalKey = internalKey;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isGatewayCall(request) && internalKey.equals(request.getHeader(INTERNAL_KEY_HEADER))) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "gateway",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + ROLE_INTERNAL))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isGatewayCall(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean create = HttpMethod.POST.matches(method) && "/api/users".equals(path);
        boolean delete = HttpMethod.DELETE.matches(method) && path.matches("/api/users/\\d+");

        return create || delete;
    }
}
