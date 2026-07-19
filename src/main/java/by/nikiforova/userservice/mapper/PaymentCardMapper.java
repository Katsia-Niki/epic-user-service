package by.nikiforova.userservice.mapper;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.dto.response.UserResponseDto;
import by.nikiforova.userservice.entity.PaymentCard;
import by.nikiforova.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    PaymentCard toEntity(PaymentCardRequestDto dto);
    PaymentCardResponseDto toResponseDto(PaymentCard card);
    void updateEntity(PaymentCardRequestDto dto, @MappingTarget PaymentCard card);
    Page<PaymentCardResponseDto> toDtoPage(Page<PaymentCard> userPage);
    List<PaymentCardResponseDto> toResponseDtoList(List<PaymentCard> cards);
}
