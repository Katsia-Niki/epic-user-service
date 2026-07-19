package by.nikiforova.userservice.controller;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Slf4j
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponseDto>> getAllCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable) {
        log.info("Getting all cards");
        return ResponseEntity.ok(paymentCardService.getAllCards(name, surname, pageable));
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<PaymentCardResponseDto> createCard(@PathVariable Long userId) {
        log.info("Creating card for user {}", userId);

        PaymentCardResponseDto dto = paymentCardService.createCard(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getCardsByUserId(@PathVariable Long userId) {
        log.info("Getting cards for user {}", userId);

        List<PaymentCardResponseDto> dtoList = paymentCardService.getAllCardsByUserId(userId);

        if (dtoList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getCardById(@PathVariable Long id) {
        log.info("Getting card with id {}", id);
        return ResponseEntity.ok(paymentCardService.getCardById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> updateCardById(
            @PathVariable Long id,
            @Valid @RequestBody PaymentCardRequestDto dto) {
        log.info("Updating card with id {}", id);
        return ResponseEntity.ok(paymentCardService.updateCard(id, dto));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardResponseDto> activatePaymentCard(@PathVariable Long id) {
        log.info("Activating card with id {}", id);
        return ResponseEntity.ok(paymentCardService.activateCard(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PaymentCardResponseDto> deactivatePaymentCard(@PathVariable Long id) {
        log.info("Deactivating card with id {}", id);
        return ResponseEntity.ok(paymentCardService.deactivateCard(id));
    }

}
