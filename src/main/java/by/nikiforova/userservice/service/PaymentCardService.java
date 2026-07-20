package by.nikiforova.userservice.service;

import by.nikiforova.userservice.config.CacheConfig;
import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.exception.CardLimitExceededException;
import by.nikiforova.userservice.exception.EntityNotFoundException;
import by.nikiforova.userservice.mapper.PaymentCardMapper;
import by.nikiforova.userservice.repository.PaymentCardRepository;
import by.nikiforova.userservice.repository.UserRepository;
import by.nikiforova.userservice.specification.PaymentCardSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardService {

    private static final int MAX_CARDS_PER_USER = 5;
    private static final String CARD_NOT_FOUND_MESSAGE = "Card not found with id: ";
    private final PaymentCardRepository paymentCardRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final PaymentCardMapper paymentCardMapper;

    @Transactional
    @CacheEvict(value = CacheConfig.USERS_WITH_CARDS_CACHE, key = "#userId")
    public PaymentCardResponseDto createCard(Long userId) {

        log.info("Starting card creation for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (isMaximumCardsPerUserReached(userId)) {
            log.error("Maximum cards reached for user: {}", userId);
            throw new CardLimitExceededException("Maximum cards reached for user: " + userId);
        }

        PaymentCard paymentCard = PaymentCard.builder()
                .user(user)
                .number(generateUniqueCardNumber())
                .holder(user.getName() + " " + user.getSurname())
                .expirationDate(LocalDate.now(ZoneId.of("Europe/Minsk")).plusYears(5))
                .active(true).build();

        PaymentCard savedCard = paymentCardRepository.save(paymentCard);

        return paymentCardMapper.toResponseDto(savedCard);
    }

    @Transactional(readOnly = true)
    public PaymentCardResponseDto getCardById(Long id) {
        log.info("Fetching card by id: {}", id);
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE + id));
        return paymentCardMapper.toResponseDto(paymentCard);
    }

    @Transactional(readOnly = true)
    public Page<PaymentCardResponseDto> getAllCards(String name, String surname, Pageable pageable) {

        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasUserName(name))
                .and(PaymentCardSpecification.hasUserSurname(surname));

        Page<PaymentCard> paymentCardPage = paymentCardRepository.findAll(spec, pageable);
        return paymentCardMapper.toDtoPage(paymentCardPage);
    }

    @Transactional(readOnly = true)
    public List<PaymentCardResponseDto> getAllCardsByUserId(Long userId) {
        log.info("Fetching cards by user id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);
        return paymentCardMapper.toResponseDtoList(cards);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USERS_WITH_CARDS_CACHE, key = "#result.userId")
    public PaymentCardResponseDto updateCard(Long id, PaymentCardRequestDto dto) {

        log.info("Updating card with id: {}", id);

        PaymentCard cardToUpdate = paymentCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        paymentCardMapper.updateEntity(dto, cardToUpdate);

        return paymentCardMapper.toResponseDto(cardToUpdate);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USERS_WITH_CARDS_CACHE, key = "#result.userId")
    public PaymentCardResponseDto activateCard(Long id) {
        log.info("Activating card with id: {}", id);

        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        card.setActive(true);

        return paymentCardMapper.toResponseDto(card);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.USERS_WITH_CARDS_CACHE, key = "#result.userId")
    public PaymentCardResponseDto deactivateCard(Long id) {
        log.info("Deactivating card with id: {}", id);

        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(CARD_NOT_FOUND_MESSAGE + id));

        card.setActive(false);

        return paymentCardMapper.toResponseDto(card);
    }

    private boolean isMaximumCardsPerUserReached(Long userId) {
        int totalCards = paymentCardRepository.countByUserId(userId);
        return totalCards >= MAX_CARDS_PER_USER;
    }

    private String generateUniqueCardNumber() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

}
