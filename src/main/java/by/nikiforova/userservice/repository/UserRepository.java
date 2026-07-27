package by.nikiforova.userservice.repository;

import by.nikiforova.userservice.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = "paymentCards")
    Optional<User> findWithPaymentCardsById(Long id);
}
