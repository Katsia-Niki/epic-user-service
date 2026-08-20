package by.nikiforova.userservice.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static by.nikiforova.userservice.constant.Constants.INTERNAL_KEY_HEADER;
import static by.nikiforova.userservice.constant.Constants.ROLE_INTERNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalApiKeyFilterTest {

    private static final String INTERNAL_KEY = "test-internal-key-test-internal-key";

    @Mock
    private FilterChain filterChain;

    private InternalApiKeyFilter filter;

    @BeforeEach
    void setUp() {

        filter = new InternalApiKeyFilter(INTERNAL_KEY);
    }

    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/users with valid key sets INTERNAL role")
    void doFilterWhenCreateUserWithValidKeyShouldSetInternalAuthentication() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/users");
        request.addHeader(INTERNAL_KEY_HEADER, INTERNAL_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals("gateway", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + ROLE_INTERNAL)));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} with valid key sets INTERNAL role")
    void doFilterWhenDeleteUserWithValidKeyShouldSetInternalAuthentication() throws Exception {
        MockHttpServletRequest request = request("DELETE", "/api/users/1");
        request.addHeader(INTERNAL_KEY_HEADER, INTERNAL_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + ROLE_INTERNAL)));

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST /api/users with wrong key does not authenticate")
    void doFilterWhenCreateUserWithWrongKeyShouldNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/users");
        request.addHeader(INTERNAL_KEY_HEADER, "wrong-wrong-wrong-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("POST /api/users without key does not authenticate")
    void doFilterWhenCreateUserWithoutKeyShouldNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = request("POST", "/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("GET /api/users/{id} with valid key does not authenticate")
    void doFilterWhenGetUserWithValidKeyShouldNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/users/1");
        request.addHeader(INTERNAL_KEY_HEADER, INTERNAL_KEY);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);
        return request;
    }
}
