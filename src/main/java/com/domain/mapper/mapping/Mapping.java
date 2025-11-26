package com.domain.mapper.mapping;

import com.domain.mapper.references.MasterDto;
import com.domain.mapper.references.MasterEntity;

import java.util.Map;

public interface Mapping {
    Map<? extends MasterEntity, ? extends MasterDto> getEntityDtoMap();
    Map<? extends MasterDto, ? extends MasterEntity> getDtoEntityMap();
}
