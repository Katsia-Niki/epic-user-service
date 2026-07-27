package by.nikiforova.userservice.mapper;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.dto.response.UserWithCardsResponseDto;
import by.nikiforova.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {

    User toEntity(UserRequestDto dto);
    void updateEntity(UserRequestDto dto, @MappingTarget User user);
    UserResponseDto toResponseDto(User user);
    @Mapping(source = "paymentCards", target = "cards")
    UserWithCardsResponseDto toWithCardsResponseDto(User user);

    default Page<UserResponseDto> toDtoPage(Page<User> userPage) {
        return userPage.map(this::toResponseDto);
    }
}
