package com.fsocial.mapper;

import com.fsocial.dto.termOfService.TermOfServiceDTO;
import com.fsocial.entity.TermOfServices;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TermOfServiceMapper {
    TermOfServiceDTO toDTO(TermOfServices termOfServices);
    TermOfServices toEntity(TermOfServiceDTO termOfServiceDTO);
    List<TermOfServiceDTO> toListDTO(List<TermOfServices> termOfServices);
}
