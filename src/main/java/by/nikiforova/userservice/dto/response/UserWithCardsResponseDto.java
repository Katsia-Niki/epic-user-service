package by.nikiforova.userservice.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UserWithCardsResponseDto (Long id,
                                       String name,
                                       String surname,
                                       String email,
                                       LocalDate birthDate,
                                       Boolean active,
                                       List<PaymentCardResponseDto> cards,
                                       LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
}
