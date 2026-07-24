package by.nikiforova.userservice.service;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.dto.response.UserWithCardsResponseDto;
import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.exception.EntityNotFoundException;
import by.nikiforova.userservice.exception.UserAlreadyExistsException;
import by.nikiforova.userservice.mapper.UserMapper;
import by.nikiforova.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Katsiaryna")
                .surname("Nikifarava")
                .birthDate(LocalDate.of(1993, Month.APRIL, 14))
                .email("katsiaryna.niki@gmail.com")
                .build();
        user.setId(1L);
        userRequestDto = new UserRequestDto("Katsiaryna", "Nikifarava",
                "katsiaryna.niki@gmail.com", LocalDate.of(1993, Month.APRIL, 14));
        userResponseDto = new UserResponseDto(1L, "Katsiaryna", "Nikifarava",
                "katsiaryna.niki@gmail.com", LocalDate.of(1993, Month.APRIL, 14),
                true, null, null);
    }

    @Test
    @DisplayName("create user - success")
    void createUserWhenEmailIsUniqueShouldSaveAndReturnDto() {
        when(userMapper.toEntity(userRequestDto)).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertEquals(userResponseDto, result);
        assertTrue(user.getActive());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).save(user);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    @DisplayName("create user - UserAlreadyExistsException")
    void createUserWhenEmailAlreadyExistsShouldThrowException() {
        when(userMapper.toEntity(userRequestDto)).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(userRequestDto));

        assertEquals("User with email already exists", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    @DisplayName("get user by id - success")
    void getUserByIdWhenUserExistsShouldReturnUserWithCardsDto() {
        UserWithCardsResponseDto expectedDto = new UserWithCardsResponseDto(
                1L,
                "Katsiaryna",
                "Nikifarava",
                "katsiaryna.niki@gmail.com",
                LocalDate.of(1993, Month.APRIL, 14),
                true,
                List.of(),
                null,
                null
        );
        when(userRepository.findWithPaymentCardsById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toWithCardsResponseDto(user)).thenReturn(expectedDto);

        UserWithCardsResponseDto result = userService.getUserById(1L);

        assertEquals(expectedDto, result);
        verify(userRepository).findWithPaymentCardsById(1L);
        verify(userMapper).toWithCardsResponseDto(user);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("get user by id - EntityNotFoundException")
    void getUserByIdWhenUserDoesNotExistShouldThrowException() {
        when(userRepository.findWithPaymentCardsById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserById(2L)
        );

        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository).findWithPaymentCardsById(2L);
        verify(userMapper, never()).toWithCardsResponseDto(any());
    }

    @Test
    @DisplayName("get all users - success")
    void getAllUsersShouldReturnPageOfDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user));
        Page<UserResponseDto> expectedPage = new PageImpl<>(List.of(userResponseDto));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDtoPage(userPage)).thenReturn(expectedPage);

        Page<UserResponseDto> result = userService.getAllUsers("Katsiaryna", "Nikifarava",
                pageable);

        assertEquals(expectedPage, result);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
        verify(userMapper).toDtoPage(userPage);
    }

    @Test
    @DisplayName("update user - success")
    void updateUserWhenUserExistsShouldUpdateAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(1L, userRequestDto);

        assertEquals(userResponseDto, result);


        verify(userRepository).findById(1L);
        verify(userMapper).updateEntity(userRequestDto, user);
        verify(userMapper).toResponseDto(user);
        verify(userRepository, never()).existsByEmail(any());
    }


    @Test
    @DisplayName("update user - EntityNotFoundException")
    void updateUserWhenUserNotFoundShouldThrowEntityNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(2L, userRequestDto)
        );

        assertEquals("User not found with id: 2", exception.getMessage());


        verify(userRepository).findById(2L);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("update user - UserAlreadyExistsException")
    void updateUserWhenEmailAlreadyExistsShouldThrowException() {
        UserRequestDto dtoWithNewEmail = new UserRequestDto(
                "Katsiaryna",
                "Nikifarava",
                "katsia-niki@gmail.com",
                LocalDate.of(1993, Month.APRIL, 14)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("katsia-niki@gmail.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.updateUser(1L, dtoWithNewEmail)
        );
        assertEquals("User with email already exists: katsia-niki@gmail.com", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("katsia-niki@gmail.com");
        verify(userMapper, never()).updateEntity(any(), any());
    }

    @Test
    @DisplayName("delete user - success")
    void deleteUserWhenUserExistsShouldDelete () {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);


        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("delete user - EntityNotFoundException")
    void deleteUserWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.deleteUser(2L)
        );

        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository).findById(2L);
        verify(userRepository, never()).delete(user);
    }

    @Test
    @DisplayName("activate user - success")
    void activateUserWhenUserExistsShouldActivate() {
        user.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.activateUser(1L);

        assertEquals(true, user.getActive());
        assertEquals(userResponseDto, result);

        verify(userRepository).findById(1L);
        verify(userMapper).toResponseDto(user);
    }

    @Test
    @DisplayName("deactivate user - success")
    void deactivateUserWhenUserExistsShouldDeactivate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.deactivateUser(1L);

        assertEquals(false, user.getActive());
        assertEquals(userResponseDto, result);

        verify(userRepository).findById(1L);
        verify(userMapper).toResponseDto(user);
    }

}
