package by.nikiforova.userservice.dto.request;

import by.nikiforova.userservice.entity.Role;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRequestDto (@NotBlank String name,
                              @NotBlank String surname,
                              @NotBlank @Email String email,
                              @NotNull @Past LocalDate birthDate) {
}
