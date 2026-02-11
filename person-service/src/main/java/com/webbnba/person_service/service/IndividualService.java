package com.webbnba.person_service.service;

import com.webbnba.person.dto.IndividualDto;
import com.webbnba.person.dto.IndividualPageDto;
import com.webbnba.person.dto.IndividualWriteDto;
import com.webbnba.person.dto.IndividualWriteResponseDto;
import com.webbnba.person_service.entity.Individual;
import com.webbnba.person_service.exception.PersonException;
import com.webbnba.person_service.mapper.IndividualMapper;
import com.webbnba.person_service.repository.IndividualRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
public class IndividualService {

    private final IndividualMapper individualMapper;
    private final IndividualRepository individualRepository;

    @Transactional
    public IndividualWriteResponseDto register(IndividualWriteDto writeDto) {
        Individual individual = individualMapper.to(writeDto);
        individualRepository.save(individual);
        log.info("IN - register: individual [{}] successfully registered", individual.getUser().getEmail());
        return new IndividualWriteResponseDto(individual.getId().toString());
    }

    public IndividualPageDto findByEmails(List<String> emails) {
        List<Individual> allByEmails = individualRepository.findAllByEmails(emails);
        List<IndividualDto> from = individualMapper.from(allByEmails);
        IndividualPageDto individualPageDto = new IndividualPageDto();
        individualPageDto.setItems(from);
        return individualPageDto;
    }

    public IndividualDto findById(UUID id) {
        Individual individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        log.info("IN - findById: individual with id = [{}] successfully found", id);
        return individualMapper.from(individual);
    }

    @Transactional
    public void softDelete(UUID id) {
        log.info("IN - softDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.softDelete(id);
    }

    @Transactional
    public void hardDelete(UUID id) {
        Individual individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        log.info("IN - hardDelete: individual with id = [{}] successfully deleted", id);
        individualRepository.delete(individual);
    }

    @Transactional
    public IndividualWriteResponseDto update(UUID id, IndividualWriteDto writeDto) {
        Individual individual = individualRepository.findById(id)
                .orElseThrow(() -> new PersonException("Individual not found by id=[%s]", id));
        individualMapper.update(individual, writeDto);
        individualRepository.save(individual);
        return new IndividualWriteResponseDto(individual.getId().toString());
    }
}
