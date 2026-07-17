package com.fsocial.postservice.services;

import com.fsocial.postservice.dto.termOfService.TermOfServiceDTO;

import java.util.List;

public interface TermOfServicesService {
    TermOfServiceDTO addTermOfService(TermOfServiceDTO termOfServiceDTO);
    TermOfServiceDTO updateTermOfService(TermOfServiceDTO termOfService);
    String deleteTermOfService(String termOfServiceId);
    List<TermOfServiceDTO> getTermOfServices();
}
