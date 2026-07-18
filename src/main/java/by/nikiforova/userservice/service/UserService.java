package by.nikiforova.userservice.service;

import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.repository.UserRepository;
import by.nikiforova.userservice.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public Page<User> getAllUsers(String name, String surname, Pageable pageable) {

        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        return userRepository.findAll(spec, pageable);
    }

    @Transactional
    public User updateUser(Long id, String name, String surname, String email, LocalDate birthDate) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email already exists: " + email);
        }

        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setBirthDate(birthDate);

        return user;
    }

    @Transactional
    public User activateUser(Long id) {
        log.info("Activating user with id: {}", id);
        User user = getUserById(id);
        user.setActive(true);
        return user;
    }

    @Transactional
    public User deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        User user = getUserById(id);
        user.setActive(false);
        return user;
    }

}
