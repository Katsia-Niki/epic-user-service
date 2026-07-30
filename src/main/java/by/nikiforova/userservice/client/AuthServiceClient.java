package by.nikiforova.userservice.client;

import by.nikiforova.userservice.dto.request.CredentialRequestDto;
import by.nikiforova.userservice.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthServiceClient {

    private final RestClient restClient = RestClient.builder().build();

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public void createCredentials(Long userId, String login, String password, Role role) {

        CredentialRequestDto request = new CredentialRequestDto(userId, login, password, role);

        restClient.post()
                .uri(authServiceUrl + "/api/credentials")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();

    }
}
