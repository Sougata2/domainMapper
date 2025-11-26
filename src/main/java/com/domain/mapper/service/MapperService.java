package com.domain.mapper.service;

import com.domain.mapper.references.MasterDto;
import com.domain.mapper.references.MasterEntity;

public interface MapperService {
    MasterEntity toEntity(MasterDto dto);
    MasterDto toDto(MasterEntity entity);
    MasterEntity merge(MasterEntity og, MasterDto nu);
}
