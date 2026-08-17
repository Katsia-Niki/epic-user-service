package by.nikiforova.userservice.constant;

public final class Constants {

    private Constants() {
    }

    public static final String USERS_WITH_CARDS_CACHE = "users-with-cards";
    public static final String TIMEZONE = "Europe/Minsk";
    public static final int MAX_CARDS_PER_USER = 5;
    public static final String CARD_NOT_FOUND_MESSAGE = "Card not found with id: %s";
    public static final String USER_NOT_FOUND_MESSAGE = "User not found with id: %s";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INTERNAL = "INTERNAL";
    public static final String INTERNAL_KEY_HEADER = "X-Internal-Key";

}
