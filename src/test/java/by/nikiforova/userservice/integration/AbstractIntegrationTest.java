package by.nikiforova.userservice.integration;

import by.nikiforova.userservice.client.AuthServiceClient;
import by.nikiforova.userservice.repository.PaymentCardRepository;
import by.nikiforova.userservice.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.cache.type=none"
})
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"));

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PaymentCardRepository paymentCardRepository;

    @MockitoBean
    protected AuthServiceClient authServiceClient;

    @BeforeEach
    void cleanDatabase() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
        authentication.setDetails(1L);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

}
