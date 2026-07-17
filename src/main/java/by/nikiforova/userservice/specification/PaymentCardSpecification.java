package by.nikiforova.userservice.specification;

import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public final class PaymentCardSpecification {

    public static Specification<PaymentCard> hasUserName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            Join<PaymentCard, User> userJoin = root.join("user");
            return cb.like(
                    cb.lower(userJoin.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<PaymentCard> hasUserSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.isBlank()) {
                return null;
            }
            Join<PaymentCard, User> userJoin = root.join("user");
            return cb.like(
                    cb.lower(userJoin.get("surname")),
                    "%" + surname.toLowerCase() + "%"
            );
        };
    }

    private PaymentCardSpecification() {
    }
}
