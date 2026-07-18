package by.nikiforova.userservice.mapper;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    PaymentCardResponseDto toResponseDto(PaymentCard card);
    void updateEntity(PaymentCardRequestDto dto, @MappingTarget PaymentCard card);
}
