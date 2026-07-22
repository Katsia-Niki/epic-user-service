package by.nikiforova.userservice.integration;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;

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
                .andExpect(jsonPath("$.userId").value(user.id()))
                .andExpect(jsonPath("$.number").isString())
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        PaymentCardResponseDto createdCard = objectMapper.readValue(
                createCardResult.getResponse().getContentAsString(),
                PaymentCardResponseDto.class);

        assertThat(createdCard.number()).hasSize(16);
        assertThat(paymentCardRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/api/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].id").value(createdCard.id()));

        PaymentCardRequestDto updateRequest = new PaymentCardRequestDto(
                "Katsia Niki",
                LocalDate.now(ZoneId.of("Europe/Minsk")).plusYears(3)
        );

        mockMvc.perform(patch("/api/cards/{id}", createdCard.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("Katsia Niki"));

        assertThat(paymentCardRepository.findById(createdCard.id()).orElseThrow().getHolder())
                .isEqualTo("Katsia Niki");

        mockMvc.perform(patch("/api/cards/{id}/deactivate", createdCard.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        assertThat(paymentCardRepository.findById(createdCard.id()).orElseThrow().getActive()).isFalse();

        mockMvc.perform(get("/api/cards/users/{userId}", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].active").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenCreatingCardForMissingUser() throws Exception {
        mockMvc.perform(post("/api/cards/users/{userId}", 745))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

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

        mockMvc.perform(get("/api/cards/{id}", createdCard.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdCard.id()))
                .andExpect(jsonPath("$.userId").value(user.id()))
                .andExpect(jsonPath("$.active").value(true));
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
                LocalDate.of(1993, Month.APRIL, 14)
        );

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), UserResponseDto.class);
    }

}
