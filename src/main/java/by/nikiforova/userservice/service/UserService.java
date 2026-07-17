package by.nikiforova.userservice.service;

import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createUser(String name, String surname, String email, LocalDate birthDate) {
        log.info("Starting user creation: email={}", email);

        if (userRepository.existsByEmail(email)) {
            log.error("User with email already exists: {}", email);
            throw new RuntimeException("User with email already exists");
        }

        User user = User.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .birthDate(birthDate)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.info("Fetching user by id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }


}
