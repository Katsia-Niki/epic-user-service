package by.nikiforova.userservice.service;

import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.repository.PaymentCardRepository;
import by.nikiforova.userservice.repository.UserRepository;
import by.nikiforova.userservice.specification.PaymentCardSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardService {

    public static final int MAX_CARDS_PER_USER = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentCard createCard(Long userId) {

        log.info("Starting card creation for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (isMaximumCardsPerUserReached(userId)) {
            log.error("Maximum cards reached for user: {}", userId);
            throw new RuntimeException("Maximum cards reached for user: " + userId);
        }

        PaymentCard paymentCard = PaymentCard.builder()
                .user(user)
                .number(generateUniqueCardNumber())
                .holder(user.getName() + " " + user.getSurname())
                .expirationDate(LocalDate.now().plusYears(5))
                .active(true).build();

        return paymentCardRepository.save(paymentCard);
    }

    @Transactional(readOnly = true)
    public PaymentCard getCardById(Long id) {
        log.info("Fetching card by id: {}", id);

        return paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<PaymentCard> getAllCards(String name, String surname, Pageable pageable) {

        Specification<PaymentCard> spec = Specification
                .where(PaymentCardSpecification.hasUserName(name))
                .and(PaymentCardSpecification.hasUserSurname(surname));

        return paymentCardRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<PaymentCard> getAllCardsByUserId(Long userId) {
        log.info("Fetching cards by user id: {}", userId);

        return paymentCardRepository.findCardsByUserId(userId);
    }

    @Transactional
    public PaymentCard updateCard(Long id, String holder, LocalDate expirationDate) {
        log.info("Updating card with id: {}", id);

        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);

        return card;
    }

    @Transactional
    public PaymentCard activateCard(Long id) {
        log.info("Activating card with id: {}", id);

        PaymentCard card = getCardById(id);

        card.setActive(true);

        return card;
    }

    @Transactional
    public PaymentCard deactivateCard(Long id) {
        log.info("Deactivating card with id: {}", id);

        PaymentCard card = getCardById(id);

        card.setActive(false);

        return card;
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
