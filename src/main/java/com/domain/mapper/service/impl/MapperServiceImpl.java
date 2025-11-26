package com.domain.mapper.service.impl;

import com.domain.mapper.mapping.Mapping;
import com.domain.mapper.references.MasterDto;
import com.domain.mapper.service.MapperService;
import com.domain.mapper.references.MasterEntity;
import jakarta.persistence.EntityManager;

public class MapperServiceImpl implements MapperService {
    private final Mapping map;
    private final EntityManager em;


    /**
     * both dependency will be imported from the target project
     * map & entityManager
     * */
    public MapperServiceImpl(Mapping map, EntityManager em) {
        this.map = map;
        this.em = em;
    }

    @Override
    public MasterEntity toEntity(MasterDto dto) {
        return null;
    }

    @Override
    public MasterDto toDto(MasterEntity entity) {
        return null;
    }

    @Override
    public MasterEntity merge(MasterEntity og, MasterDto nu) {
        return null;
    }
}
