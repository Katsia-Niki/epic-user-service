package by.nikiforova.userservice.mapper;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto dto);
    void updateEntity(UserRequestDto dto, @MappingTarget User user);
    UserResponseDto toResponseDto(User user);

    default Page<UserResponseDto> toDtoPage(Page<User> userPage) {
        return userPage.map(user -> toResponseDto(user));
    }
}
