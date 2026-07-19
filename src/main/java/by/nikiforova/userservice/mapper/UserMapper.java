package by.nikiforova.userservice.mapper;

import by.nikiforova.userservice.dto.request.UserRequestDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto dto);
    void updateEntity(UserRequestDto dto, @MappingTarget User user);
    UserResponseDto toResponseDto(User user);
}
