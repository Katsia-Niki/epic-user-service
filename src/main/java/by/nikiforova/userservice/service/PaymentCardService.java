package by.nikiforova.userservice.service;

import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.repository.PaymentCardRepository;
import by.nikiforova.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;

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
