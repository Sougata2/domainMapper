package com.domain.mapper.mapping;

import com.domain.mapper.references.MasterDto;
import com.domain.mapper.references.MasterEntity;

import java.util.Map;

public interface Mapping {
    Map<Class<? extends MasterEntity>, Class<? extends MasterDto>> getEntityDtoMap();

    Map<Class<? extends MasterDto>, Class<? extends MasterEntity>> getDtoEntityMap();
}
