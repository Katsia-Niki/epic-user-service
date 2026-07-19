package by.nikiforova.userservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PaymentCardRequestDto (@NotBlank String holder,
                                     @NotNull @Future LocalDate expirationDate){
}
