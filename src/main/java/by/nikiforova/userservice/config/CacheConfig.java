package by.nikiforova.userservice.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConfig {

    public static final String USERS_WITH_CARDS_CACHE = "users-with-cards";

}
