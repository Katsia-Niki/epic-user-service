package by.nikiforova.userservice.integration;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final UserRequestDto USER_REQUEST = new UserRequestDto(
            "Katsiaryna",
            "Nikifarava",
            "katsiaryna.niki@gmail.com",
            LocalDate.of(1993, Month.APRIL, 14)
    );

    @Test
    void shouldCreateUserAndSaveInDatabase() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(USER_REQUEST)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(USER_REQUEST.email()))
                .andExpect(jsonPath("$.active").value(true));

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.existsByEmail(USER_REQUEST.email())).isTrue();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(USER_REQUEST)));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(USER_REQUEST)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with email already exists"));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetUserByIdWithCardsFromDatabase() throws Exception {
        UserResponseDto createdUser = createUser();

        mockMvc.perform(get("/api/users/{id}", createdUser.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUser.id()))
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 7))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 7"));
    }

    @Test
    void shouldReturnPageOfUsers() throws Exception {
        createUser();
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenUserDataInvalid() throws Exception {
        UserRequestDto invalid = new UserRequestDto("", "Niki", "not-email", LocalDate.now());
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void shouldUpdateUserInDatabase() throws Exception {
        UserResponseDto createdUser = createUser();

        UserRequestDto updateRequest = new UserRequestDto(
                "Katsia",
                "Niki",
                USER_REQUEST.email(),
                USER_REQUEST.birthDate()
        );

        mockMvc.perform(patch("/api/users/{id}", createdUser.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Katsia"))
                .andExpect(jsonPath("$.surname").value("Niki"));

        assertThat(userRepository.findById(createdUser.id()))
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getName()).isEqualTo("Katsia");
                    assertThat(user.getSurname()).isEqualTo("Niki");
                });
    }

    @Test
    void shouldActivateAndDeactivateUserInDatabase() throws Exception {
        UserResponseDto createdUser = createUser();

        mockMvc.perform(patch("/api/users/{id}/deactivate", createdUser.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        assertThat(userRepository.findById(createdUser.id()).orElseThrow().getActive()).isFalse();

        mockMvc.perform(patch("/api/users/{id}/activate", createdUser.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        assertThat(userRepository.findById(createdUser.id()).orElseThrow().getActive()).isTrue();
    }

    @Test
    void shouldDeleteUserFromDatabase() throws Exception {
        UserResponseDto createdUser = createUser();

        mockMvc.perform(delete("/api/users/{id}", createdUser.id()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(createdUser.id())).isFalse();
    }

    private UserResponseDto createUser() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(USER_REQUEST)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), UserResponseDto.class);
    }

    @Test
    void shouldFilterUsersByNameAndSurname() throws Exception {
        createUser();

        mockMvc.perform(get("/api/users")
                        .param("name", "Katsia")
                        .param("surname", "Nikif"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value(USER_REQUEST.email()));
    }

    @Test
    void shouldReturnEmptyPageWhenUserFilterDoesNotMatch() throws Exception {
        createUser();

        mockMvc.perform(get("/api/users")
                        .param("name", "Nfjdkhkd")
                        .param("surname", "FJKHDKD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

}
