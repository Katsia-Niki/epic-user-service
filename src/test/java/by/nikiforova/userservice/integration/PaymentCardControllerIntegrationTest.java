package by.nikiforova.userservice.integration;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.dto.response.UserWithCardsResponseDto;
import by.nikiforova.userservice.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static by.nikiforova.userservice.constant.Constants.USER_NOT_FOUND_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentCardControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void shouldRunFullFlowFromControllerToDatabase() throws Exception {
        UserResponseDto user = createUser();

        MvcResult createCardResult = mockMvc.perform(post("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isCreated())
                .andReturn();

        PaymentCardResponseDto createdCard = objectMapper.readValue(
                createCardResult.getResponse().getContentAsString(),
                PaymentCardResponseDto.class);

        assertThat(createdCard.userId()).isEqualTo(user.id());
        assertThat(createdCard.active()).isTrue();
        assertThat(createdCard.number()).hasSize(16);
        assertThat(paymentCardRepository.count()).isEqualTo(1);

        MvcResult getUserResult = mockMvc.perform(get("/api/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andReturn();

        UserWithCardsResponseDto userWithCards = objectMapper.readValue(
                getUserResult.getResponse().getContentAsString(),
                UserWithCardsResponseDto.class);

        assertThat(userWithCards.cards()).hasSize(1);
        assertThat(userWithCards.cards().getFirst().id()).isEqualTo(createdCard.id());

        PaymentCardRequestDto updateRequest = new PaymentCardRequestDto(
                "Katsia Niki",
                LocalDate.now(ZoneId.of("Europe/Minsk")).plusYears(3)
        );

        MvcResult updateCardResult = mockMvc.perform(patch("/api/cards/{id}", createdCard.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();

        PaymentCardResponseDto updatedCard = objectMapper.readValue(
                updateCardResult.getResponse().getContentAsString(),
                PaymentCardResponseDto.class);

        assertThat(updatedCard.holder()).isEqualTo("Katsia Niki");
        assertThat(paymentCardRepository.findById(createdCard.id()).orElseThrow().getHolder())
                .isEqualTo("Katsia Niki");

        MvcResult deactivateCardResult = mockMvc.perform(patch("/api/cards/{id}/deactivate", createdCard.id()))
                .andExpect(status().isOk())
                .andReturn();

        PaymentCardResponseDto deactivatedCard = objectMapper.readValue(
                deactivateCardResult.getResponse().getContentAsString(),
                PaymentCardResponseDto.class);

        assertThat(deactivatedCard.active()).isFalse();
        assertThat(paymentCardRepository.findById(createdCard.id()).orElseThrow().getActive()).isFalse();

        MvcResult getCardsResult = mockMvc.perform(get("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isOk())
                .andReturn();

        List<PaymentCardResponseDto> userCards = objectMapper.readValue(
                getCardsResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, PaymentCardResponseDto.class));

        assertThat(userCards).hasSize(1);
        assertThat(userCards.getFirst().active()).isFalse();
    }

    @Test
    void shouldReturnNotFoundWhenCreatingCardForMissingUser() throws Exception {
        mockMvc.perform(post("/api/cards/users/{userId}", 745))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(USER_NOT_FOUND_MESSAGE.formatted(745)));

        assertThat(paymentCardRepository.count()).isZero();
    }

    @Test
    void shouldReturnNoContentWhenUserHasNoCards() throws Exception {
        UserResponseDto user = createUser();

        mockMvc.perform(get("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnConflictWhenCardLimitExceeded() throws Exception {
        UserResponseDto user = createUser();
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/cards/users/{userId}", user.id()))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Maximum cards reached for user: " + user.id()));
    }

    @Test
    void shouldGetAllCardsWithFilters() throws Exception {
        UserResponseDto user = createUser();

        mockMvc.perform(post("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/cards")
                        .param("name", "Katsia")
                        .param("surname", "Nikif"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(user.id()));
    }

    @Test
    void shouldGetCardById() throws Exception {
        UserResponseDto user = createUser();

        MvcResult createCardResult = mockMvc.perform(post("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isCreated())
                .andReturn();

        PaymentCardResponseDto createdCard = objectMapper.readValue(
                createCardResult.getResponse().getContentAsString(),
                PaymentCardResponseDto.class);

        MvcResult result = mockMvc.perform(get("/api/cards/{id}", createdCard.id()))
                .andExpect(status().isOk())
                .andReturn();

        PaymentCardResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PaymentCardResponseDto.class);

        assertThat(actual.id()).isEqualTo(createdCard.id());
        assertThat(actual.userId()).isEqualTo(user.id());
        assertThat(actual.number()).isEqualTo(createdCard.number());
        assertThat(actual.holder()).isEqualTo(createdCard.holder());
        assertThat(actual.expirationDate()).isEqualTo(createdCard.expirationDate());
        assertThat(actual.active()).isTrue();
        assertThat(actual.createdAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(createdCard.createdAt().truncatedTo(ChronoUnit.MILLIS));
        assertThat(actual.updatedAt().truncatedTo(ChronoUnit.MILLIS))
                .isEqualTo(createdCard.updatedAt().truncatedTo(ChronoUnit.MILLIS));
    }

    @Test
    void shouldReturnNotFoundWhenCardDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Card not found with id: 9999"));
    }

    private UserResponseDto createUser() throws Exception {
        UserRequestDto request = new UserRequestDto(
                "Katsiaryna",
                "Nikifarava",
                "katsiaryna.niki@gmail.com",
                LocalDate.of(1993, Month.APRIL, 14),
                "katsiaryna",
                "password123",
                Role.USER
        );

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), UserResponseDto.class);
    }

}
