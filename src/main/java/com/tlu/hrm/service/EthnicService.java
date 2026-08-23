package com.tlu.hrm.service;

import com.tlu.hrm.dto.request.EthnicDto;
import com.tlu.hrm.dto.search.SearchDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface EthnicService {
    Page<EthnicDto> pagingEthnics(SearchDto searchDto);
    List<EthnicDto> getAllEthnics();
    EthnicDto getById(UUID id);
    EthnicDto saveEthnic(EthnicDto dto);
    void deleteEthnic(UUID id);
    boolean isValidCode(EthnicDto dto);
}
