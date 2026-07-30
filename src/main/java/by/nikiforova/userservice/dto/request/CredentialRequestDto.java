package by.nikiforova.userservice.dto.request;

import by.nikiforova.userservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CredentialRequestDto (@NotNull Long userId,
                                    @NotBlank String login,
                                    @NotBlank @Size(min = 8) String password,
                                    @NotNull Role role) {
}
