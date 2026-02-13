package com.webbnba.api.mapper;

import com.webbnba.individual.dto.IndividualDto;
import com.webbnba.individual.dto.IndividualWriteDto;
import com.webbnba.individual.dto.IndividualWriteResponseDto;
import org.mapstruct.Mapper;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, injectionStrategy = CONSTRUCTOR)
public interface PersonMapper {

    com.webbnba.person.dto.IndividualWriteDto from(IndividualWriteDto dto);

    com.webbnba.person.dto.IndividualDto from(IndividualDto dto);

    IndividualDto from(com.webbnba.person.dto.IndividualDto dto);

    IndividualWriteResponseDto from(com.webbnba.person.dto.IndividualWriteResponseDto dto);
}
