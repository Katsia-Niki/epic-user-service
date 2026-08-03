package by.nikiforova.userservice.service;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.exception.CardLimitExceededException;
import by.nikiforova.userservice.exception.EntityNotFoundException;
import by.nikiforova.userservice.mapper.PaymentCardMapper;
import by.nikiforova.userservice.repository.PaymentCardRepository;
import by.nikiforova.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static by.nikiforova.userservice.constant.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    private static final String USER_NAME = "Katsiaryna";
    private static final String USER_SURNAME = "Nikifarava";
    private static final String EMAIL = "katsiaryna.niki@gmail.com";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1993, Month.APRIL, 14);
    private static final String CARD_NUMBER = "1234567891234567";
    private static final LocalDate EXPIRATION_DATE = LocalDate.of(2030, Month.APRIL, 25);

    @Mock
    private PaymentCardRepository paymentCardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private User user;
    private PaymentCard paymentCard;
    private PaymentCardRequestDto paymentCardRequestDto;
    private PaymentCardResponseDto paymentCardResponseDto;

    @BeforeEach
    void setUp() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
        authentication.setDetails(1L);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        user = User.builder()
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .birthDate(BIRTH_DATE)
                .email(EMAIL)
                .build();
        user.setId(1L);

        paymentCard = PaymentCard.builder()
                .user(user)
                .number(CARD_NUMBER)
                .holder(user.getName() + " " + user.getSurname())
                .expirationDate(EXPIRATION_DATE)
                .active(true)
                .build();
        paymentCard.setId(1L);

        paymentCardRequestDto = new PaymentCardRequestDto(user.getName() + " " + user.getSurname(),
                EXPIRATION_DATE);

        paymentCardResponseDto = new PaymentCardResponseDto(1L, 1L, CARD_NUMBER,
                user.getName() + " " + user.getSurname(), EXPIRATION_DATE,
                true, null, null);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("create card - success")
    void createPaymentCardWhenUserExistsShouldCreateAndReturnDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(0);
        when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(paymentCard);
        when(paymentCardMapper.toResponseDto(any(PaymentCard.class))).thenReturn(paymentCardResponseDto);

        PaymentCardResponseDto result = paymentCardService.createCard(1L);

        assertEquals(paymentCardResponseDto, result);
        verify(userRepository).findById(1L);
        verify(paymentCardRepository).countByUserId(1L);
        verify(paymentCardRepository).save(any(PaymentCard.class));
        verify(paymentCardMapper).toResponseDto(any(PaymentCard.class));
    }

    @Test
    @DisplayName("create card - EntityNotFoundException")
    void createCardWhenUserNotFoundShouldThrowException() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentCardService.createCard(2L)
        );

        assertEquals(USER_NOT_FOUND_MESSAGE.formatted(2), exception.getMessage());
        verify(userRepository).findById(2L);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("create card - CardLimitExceededException")
    void createCardWhenMaximumCardsReachedShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(MAX_CARDS_PER_USER);

        CardLimitExceededException exception = assertThrows(
                CardLimitExceededException.class,
                () -> paymentCardService.createCard(1L)
        );
        assertEquals("Maximum cards reached for user: 1", exception.getMessage());
        verify(paymentCardRepository).countByUserId(1L);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    @DisplayName("get card by id - success")
    void getCardByIdWhenCardExistsShouldReturnDto() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toResponseDto(paymentCard)).thenReturn(paymentCardResponseDto);

        PaymentCardResponseDto result = paymentCardService.getCardById(1L);

        assertEquals(paymentCardResponseDto, result);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toResponseDto(paymentCard);
    }

    @Test
    @DisplayName("get card by id - EntityNotFoundException")
    void getCardByIdWhenCardNotFoundShouldThrowException() {
        when(paymentCardRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentCardService.getCardById(2L)
        );

        assertEquals(CARD_NOT_FOUND_MESSAGE.formatted(2), exception.getMessage());

        verify(paymentCardRepository).findById(2L);
        verify(paymentCardMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("get all cards - success")
    void getAllCardsShouldReturnPageOfDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(paymentCard));
        Page<PaymentCardResponseDto> expectedPage = new PageImpl<>(List.of(paymentCardResponseDto));

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(paymentCardMapper.toDtoPage(cardPage)).thenReturn(expectedPage);

        Page<PaymentCardResponseDto> result = paymentCardService.getAllCards(USER_NAME, USER_SURNAME, pageable);

        assertEquals(expectedPage, result);

        verify(paymentCardRepository).findAll(any(Specification.class), eq(pageable));
        verify(paymentCardMapper).toDtoPage(cardPage);
    }

    @Test
    @DisplayName("get all cards by user id - success")
    void getAllCardsByUserIdWhenUserExistsShouldReturnList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(paymentCardRepository.findByUserId(1L)).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toResponseDtoList(List.of(paymentCard)))
                .thenReturn(List.of(paymentCardResponseDto));

        List<PaymentCardResponseDto> result = paymentCardService.getAllCardsByUserId(1L);

        assertEquals(List.of(paymentCardResponseDto), result);

        verify(userRepository).existsById(1L);
        verify(paymentCardRepository).findByUserId(1L);
        verify(paymentCardMapper).toResponseDtoList(List.of(paymentCard));
    }

    @Test
    @DisplayName("get all cards by user id - EntityNotFoundException")
    void getAllCardsByUserIdWhenUserNotFoundShouldThrowException() {
        when(userRepository.existsById(2L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentCardService.getAllCardsByUserId(2L)
        );

        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository).existsById(2L);
        verify(paymentCardRepository, never()).findByUserId(any());
    }

    @Test
    @DisplayName("update card - success")
    void updateCardWhenCardExistsShouldUpdateAndReturnDto() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toResponseDto(paymentCard)).thenReturn(paymentCardResponseDto);

        PaymentCardResponseDto result = paymentCardService.updateCard(1L, paymentCardRequestDto);

        assertEquals(paymentCardResponseDto, result);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).updateEntity(paymentCardRequestDto, paymentCard);
        verify(paymentCardMapper).toResponseDto(paymentCard);
    }

    @Test
    @DisplayName("update card - EntityNotFoundException")
    void updateCardWhenCardNotFoundShouldThrowException() {
        when(paymentCardRepository.findById(2L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentCardService.updateCard(2L, paymentCardRequestDto)
        );

        assertEquals(CARD_NOT_FOUND_MESSAGE.formatted(2), exception.getMessage());

        verify(paymentCardRepository).findById(2L);
        verify(paymentCardMapper, never()).updateEntity(any(), any());
        verify(paymentCardMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("activate card - success")
    void activateCardWhenCardExistsShouldActivate() {
        paymentCard.setActive(false);
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toResponseDto(paymentCard)).thenReturn(paymentCardResponseDto);

        PaymentCardResponseDto result = paymentCardService.activateCard(1L);

        assertEquals(true, paymentCard.getActive());
        assertEquals(paymentCardResponseDto, result);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toResponseDto(paymentCard);
    }

    @Test
    @DisplayName("deactivate card - success")
    void deactivateCardWhenCardExistsShouldDeactivate() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toResponseDto(paymentCard)).thenReturn(paymentCardResponseDto);

        PaymentCardResponseDto result = paymentCardService.deactivateCard(1L);

        assertEquals(false, paymentCard.getActive());
        assertEquals(paymentCardResponseDto, result);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toResponseDto(paymentCard);
    }

}
