package by.nikiforova.userservice.mapper;

import by.nikiforova.userservice.dto.request.PaymentCardRequestDto;
import by.nikiforova.userservice.dto.response.PaymentCardResponseDto;
import by.nikiforova.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponseDto toResponseDto(PaymentCard card);
    void updateEntity(PaymentCardRequestDto dto, @MappingTarget PaymentCard card);

    default Page<PaymentCardResponseDto> toDtoPage(Page<PaymentCard> cardPage) {
        return cardPage.map(this::toResponseDto);
    }

    List<PaymentCardResponseDto> toResponseDtoList(List<PaymentCard> cards);
}
