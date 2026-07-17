package com.fsocial.postservice.services.impl;

import com.fsocial.postservice.dto.termOfService.TermOfServiceDTO;
import com.fsocial.postservice.entity.TermOfServices;
import com.fsocial.postservice.exception.AppException;
import com.fsocial.postservice.exception.StatusCode;
import com.fsocial.postservice.mapper.TermOfServiceMapper;
import com.fsocial.postservice.repository.TermRepository;
import com.fsocial.postservice.services.TermOfServicesService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor()
public class TermOfServicesServiceImpl implements TermOfServicesService {

    TermRepository termRepository;
    TermOfServiceMapper termOfServiceMapper;

    @Override
    public List<TermOfServiceDTO> getTermOfServices() {

        List<TermOfServices> res = termRepository.findAll();

        return termOfServiceMapper.toListDTO(res);
    }

    @Override
    public TermOfServiceDTO addTermOfService(TermOfServiceDTO termOfService) {
        return termOfServiceMapper.toDTO(termRepository.save(termOfServiceMapper.toEntity(termOfService)));
    }

    @Override
    public TermOfServiceDTO updateTermOfService(TermOfServiceDTO termOfService) {

        TermOfServices findById = termRepository.findById(termOfService.getId()).orElseThrow(() -> new AppException("Không tìm thấy chính sách", StatusCode.TERM_OF_SERVICE_NOT_FOUND));

        findById.setName(termOfService.getName());

        return termOfServiceMapper.toDTO(termRepository.save(findById));
    }

    @Override
    public String deleteTermOfService(String termOfServiceId) {
         termRepository.deleteById(termOfServiceId);
        return "Xóa chính sách thành công";
    }

}
