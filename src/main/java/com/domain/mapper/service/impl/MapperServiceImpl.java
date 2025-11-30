package com.domain.mapper.service.impl;

import com.domain.mapper.mapping.Mapping;
import com.domain.mapper.references.Edge;
import com.domain.mapper.references.MasterDto;
import com.domain.mapper.references.MasterEntity;
import com.domain.mapper.references.ParentChild;
import com.domain.mapper.service.MapperService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MapperServiceImpl implements MapperService {
    private final Mapping map;
    private final EntityManager em;


    /**
     * both dependency will be imported from the target project
     * map & entityManager
     *
     */
    public MapperServiceImpl(Mapping map, EntityManager em) {
        this.map = map;
        this.em = em;
    }

    @Override
    public MasterEntity toEntity(MasterDto dto) {
        try {
            if (dto == null) return null;
            MasterEntity res = null;
            Queue<ParentChild<MasterEntity, MasterDto>> queue = new LinkedList<>();
            Set<Edge> visited = new HashSet<>();

            queue.add(new ParentChild<>(null, dto, "super"));
            visited.add(new Edge(null, dto, "super"));

            while (!queue.isEmpty()) {
                ParentChild<MasterEntity, MasterDto> u = queue.poll();

                MasterEntity parent = u.parent();
                MasterDto child = u.child();
                String relationName = u.relationName();
                Class<? extends MasterEntity> entityClass = map.getDtoEntityMap().get(child.getClass());
                if (entityClass == null) {
                    throw new IllegalStateException("No ENTITY Mapping found for %s".formatted(child.getClass()));
                }

                MasterEntity entity = entityClass.getDeclaredConstructor().newInstance();

                for (Field ef : entity.getClass().getDeclaredFields()) {
                    ef.setAccessible(true);
                    Field df = getDeclaredField(child.getClass(), ef.getName());
                    df.setAccessible(true);

                    Object value = df.get(child);

                    if (Collection.class.isAssignableFrom(ef.getType())) {
                        if (value != null) {
                            Collection<?> collection = (Collection<?>) value;
                            if (collection.isEmpty()) {
                                ef.set(entity, new HashSet<>());
                            } else {
                                for (Object o : collection) {
                                    Edge edge = new Edge(child, o, df.getName());
                                    if (!visited.contains(edge)) {
                                        visited.add(edge);
                                        queue.add(new ParentChild<>(entity, (MasterDto) o, ef.getName()));
                                    }
                                }
                            }
                        }
                    } else if (isComplex(ef)) {
                        if (value != null) {
                            Edge edge = new Edge(child, value, df.getName());
                            if (!visited.contains(edge)) {
                                visited.add(edge);
                                queue.add(new ParentChild<>(entity, (MasterDto) value, ef.getName()));
                            }
                        }
                    } else {
                        if (value != null) {
                            ef.set(entity, value);
                        }
                    }
                }

                if (parent == null) {
                    res = entity;
                } else {
                    for (Field pf : parent.getClass().getDeclaredFields()) {
                        pf.setAccessible(true);
                        if (Collection.class.isAssignableFrom(pf.getType())) {
                            Type genericType = pf.getGenericType();
                            if (genericType instanceof ParameterizedType) {
                                Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                                if (actualType == entity.getClass() && pf.getName().equals(relationName)) {
                                    Collection<MasterEntity> collection = (Collection<MasterEntity>) pf.get(parent);
                                    if (collection == null) {
                                        collection = new HashSet<>();
                                        collection.add(entity);
                                        pf.set(parent, collection);
                                    } else {
                                        collection.add(entity);
                                    }
                                }
                            }
                        } else if (isComplex(pf)) {
                            if (pf.getType() == entity.getClass() && pf.getName().equals(relationName)) {
                                pf.set(parent, entity);
                            }
                        }
                    }
                }
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MasterDto toDto(MasterEntity entity, int depth) {
        try {
            if (entity == null) return null;

            MasterDto res = null;

            Queue<ParentChild<MasterDto, MasterEntity>> queue = new LinkedList<>();
            Set<Edge> visited = new HashSet<>();

            entity = (entity instanceof HibernateProxy) ? (MasterEntity) Hibernate.unproxy(entity) : entity;
            queue.add(new ParentChild<>(null, entity, "super", 0));
            visited.add(new Edge(null, entity, "super"));

            while (!queue.isEmpty()) {
                ParentChild<MasterDto, MasterEntity> u = queue.poll();

                int level = u.level();
                MasterDto parent = u.parent();
                MasterEntity child = u.child();
                String relationName = u.relationName();
                Class<? extends MasterDto> dtoClass;

                if (child instanceof HibernateProxy) {
                    Class<? extends MasterEntity> actualClass = Hibernate.getClass(child);
                    dtoClass = map.getEntityDtoMap().get(actualClass);
                    if (dtoClass == null)
                        throw new IllegalStateException("No DTO Mapping found for %s".formatted(actualClass));
                } else {
                    dtoClass = map.getEntityDtoMap().get(child.getClass());
                    if (dtoClass == null)
                        throw new IllegalStateException("No DTO Mapping found for %s".formatted(child.getClass()));
                }

                MasterDto dto = dtoClass.getDeclaredConstructor().newInstance();

                for (Field df : dto.getClass().getDeclaredFields()) {
                    df.setAccessible(true);

                    Field ef;

                    MasterEntity actualChild = (child instanceof HibernateProxy) ? (MasterEntity) Hibernate.unproxy(child) : child;
                    ef = getDeclaredField(actualChild.getClass(), df.getName());
                    ef.setAccessible(true);

                    Object value = ef.get(actualChild);

                    // if the field is a collection
                    if (Collection.class.isAssignableFrom(df.getType())) {
                        Collection<?> collection = (Collection<?>) value;
                        if (value != null && level < depth) {
                            if (collection.isEmpty()) {
                                df.set(dto, new HashSet<>());
                            } else {
                                for (Object o : collection) {
                                    o = (o instanceof HibernateProxy) ? Hibernate.unproxy(o) : o;
                                    Edge edge = new Edge(actualChild, o, ef.getName());
                                    if (!visited.contains(edge)) {
                                        visited.add(edge);
                                        queue.add(new ParentChild<>(dto, (MasterEntity) o, df.getName(), level + 1));
                                    }
                                }
                            }
                        }
                    }
                    // if the field is an object
                    else if (isComplex(df)) {
                        if (value != null && level < depth) {
                            Edge edge = new Edge(actualChild, value, ef.getName());
                            if (!visited.contains(edge)) {
                                visited.add(edge);
                                queue.add(new ParentChild<>(dto, (MasterEntity) value, df.getName(), level + 1));
                            }
                        }
                    }
                    // if the field is primary field.
                    else {
                        if (value != null) {
                            df.set(dto, value);
                        }
                    }
                }

                // setting the child in the parent object
                if (parent == null) {
                    res = dto;
                } else {
                    for (Field pf : parent.getClass().getDeclaredFields()) {
                        pf.setAccessible(true);
                        if (Collection.class.isAssignableFrom(pf.getType())) {
                            Type genericType = pf.getGenericType();
                            if (genericType instanceof ParameterizedType) {
                                Type actualType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                                if (actualType == dto.getClass() && pf.getName().equals(relationName)) {
                                    Collection<MasterDto> collection = (Collection<MasterDto>) pf.get(parent);
                                    if (collection == null) {
                                        collection = new HashSet<>();
                                        collection.add(dto);
                                        pf.set(parent, collection);
                                    } else {
                                        collection.add(dto);
                                    }
                                }
                            }
                        } else if (isComplex(pf)) {
                            if (pf.getType() == dto.getClass() && pf.getName().equals(relationName)) {
                                pf.set(parent, dto);
                            }
                        }
                    }
                }
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MasterDto toDto(MasterEntity entity) {
        return toDto(entity, 1);
    }

    @Override
    public MasterEntity merge(MasterEntity og, MasterEntity nu) {
        try {
            for (Field ogf : og.getClass().getDeclaredFields()) {
                ogf.setAccessible(true);
                Field nuf = nu.getClass().getDeclaredField(ogf.getName());
                nuf.setAccessible(true);

                if (Collection.class.isAssignableFrom(ogf.getType())) {
                    Object ogValue = ogf.get(og);
                    Object nuValue = nuf.get(nu);

                    if (nuValue != null) {
                        Collection<MasterEntity> ogCollection = (Collection<MasterEntity>) ogValue;
                        Collection<MasterEntity> nuCollection = (Collection<MasterEntity>) nuValue;

                        Map<Long, MasterEntity> ogMap = ogCollection.stream().collect(Collectors.toMap(MasterEntity::getId, e -> e));
                        Map<Long, MasterEntity> nuMap = nuCollection.stream().collect(Collectors.toMap(MasterEntity::getId, e -> e));

                        Set<MasterEntity> insertSet = new HashSet<>();
                        for (MasterEntity o : nuCollection) {
                            if (o != null) {
                                if (!ogMap.containsKey(o.getId())) {
                                    try {
                                        MasterEntity managedEntity = em.getReference(o.getClass(), o.getId());
                                        if (managedEntity != null) {
                                            insertSet.add(managedEntity);
                                        }
                                    } catch (EntityNotFoundException e) {
                                        throw new EntityNotFoundException(e.getMessage());
                                    }
                                }
                            }
                        }
                        ogCollection.addAll(insertSet);

                        Set<MasterEntity> deleteSet = new HashSet<>();
                        for (MasterEntity o : ogCollection) {
                            if (o != null) {
                                if (!nuMap.containsKey(o.getId())) {
                                    deleteSet.add(o);
                                }
                            }
                        }
                        deleteSet.forEach(ogCollection::remove);
                    }
                } else if (isComplex(ogf)) {
                    MasterEntity relation = (MasterEntity) nuf.get(nu);
                    if (relation != null) {
                        if (relation.getId() == null) {
                            ogf.set(og, null);
                        } else if (relation.getId() != null) {
                            MasterEntity managedEntity = (MasterEntity) em.getReference(ogf.getType(), relation.getId());
                            if (managedEntity == null) throw new EntityNotFoundException();
                            ogf.set(og, managedEntity);
                        }
                    }
                } else {
                    Object nufValue = nuf.get(nu);
                    if (nufValue != null) {
                        ogf.set(og, nufValue);
                    }
                }
            }
            return og;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private boolean isComplex(Field f) {
        return !Integer.class.isAssignableFrom(f.getType()) && !Long.class.isAssignableFrom(f.getType()) &&
                !Double.class.isAssignableFrom(f.getType()) && !Boolean.class.isAssignableFrom(f.getType()) &&
                !Float.class.isAssignableFrom(f.getType()) && !String.class.isAssignableFrom(f.getType()) &&
                !LocalDateTime.class.isAssignableFrom(f.getType()) && !Timestamp.class.isAssignableFrom(f.getType()) &&
                !Date.class.isAssignableFrom(f.getType()) && !f.getType().isEnum();
    }
}
