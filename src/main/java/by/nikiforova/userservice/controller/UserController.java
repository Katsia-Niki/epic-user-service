package by.nikiforova.userservice.controller;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.dto.response.UserWithCardsResponseDto;
import by.nikiforova.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(name, surname, pageable));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto dto) {
        UserResponseDto createdUser = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserWithCardsResponseDto> getUserById(
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto dto
    ) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDto> activateUser (@PathVariable Long id) {
        return ResponseEntity.ok(userService.activateUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDto> deactivateUser (@PathVariable Long id) {
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

}
