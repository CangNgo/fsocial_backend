package com.fsocial.services;

import com.fsocial.dto.termOfService.TermOfServiceDTO;

import java.util.List;

public interface TermOfServicesService {
    TermOfServiceDTO addTermOfService(TermOfServiceDTO termOfServiceDTO);
    TermOfServiceDTO updateTermOfService(TermOfServiceDTO termOfService);
    String deleteTermOfService(String termOfServiceId);
    List<TermOfServiceDTO> getTermOfServices();
}
