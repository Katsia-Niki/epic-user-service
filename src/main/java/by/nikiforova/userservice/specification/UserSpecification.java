package by.nikiforova.userservice.specification;

import by.nikiforova.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {

    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasSurname(String surname) {
        return (root, query, cb) -> {
            if (surname == null || surname.isBlank()) {
                return null;
            }
            return cb.like(cb.lower(root.get("surname")), "%" + surname.toLowerCase() + "%");
        };
    }

    private UserSpecification() {
    }
}
