package by.nikiforova.userservice.repository;

import by.nikiforova.userservice.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    int countByUserId(Long userId);
    List<PaymentCard> findByUserId(Long userId);
    Page<PaymentCard> findByUserId(Long userId, Pageable pageable);
    boolean existsByNumber(String number);

    @Query(value = "SELECT * FROM payment_cards WHERE user_id = :userId", nativeQuery = true)
    List<PaymentCard> findCardsByUserId(@Param("userId") Long userId);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId AND pc.active = true")
    List<PaymentCard> findActiveCardsByUserId(@Param("userId") Long userId);
}
