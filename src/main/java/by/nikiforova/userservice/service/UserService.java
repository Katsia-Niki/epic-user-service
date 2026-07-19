package by.nikiforova.userservice.service;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.entity.User;
import by.nikiforova.userservice.exception.UserAlreadyExistsException;
import by.nikiforova.userservice.exception.EntityNotFoundException;
import by.nikiforova.userservice.mapper.UserMapper;
import by.nikiforova.userservice.repository.UserRepository;
import by.nikiforova.userservice.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {

        User user = userMapper.toEntity(dto);
        log.info("Starting user creation: email={}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            log.error("User with email already exists: {}", user.getEmail());
            throw new UserAlreadyExistsException("User with email already exists");
        }
        User savedUser = userRepository.save(user);
        savedUser.setActive(true);
        return userMapper.toResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user by id: {}", id);

         User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(String name, String surname, Pageable pageable) {

        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));

        Page<User> userPage = userRepository.findAll(spec, pageable);

        return userMapper.toDtoPage(userPage);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {

        log.info("Updating user with id: {}", id);

        User foundUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (!foundUser.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException("User with email already exists: " + dto.email());
        }
        userMapper.updateEntity(dto, foundUser);
        return userMapper.toResponseDto(foundUser);
    }

    @Transactional
    public UserResponseDto activateUser(Long id) {
        log.info("Activating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setActive(true);
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto deactivateUser(Long id) {
        log.info("Deactivating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setActive(false);
        return userMapper.toResponseDto(user);
    }

}
