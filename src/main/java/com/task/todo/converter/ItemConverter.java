package com.task.todo.converter;

import com.task.todo.entity.ItemEntity;
import com.task.todo.model.ItemDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ItemConverter {
    ItemEntity mapToEntity(ItemDto itemDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mappings({@Mapping(target = "id", ignore = true)})
    void mapToEntity(ItemDto itemDto, @MappingTarget ItemEntity itemEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({@Mapping(target = "lastModifiedDate", ignore = true)})
    void mapPartialToEntity(ItemDto itemDto, @MappingTarget ItemEntity itemEntity);

    @Mappings({
            @Mapping(target = "id", source = "id")
    })
    ItemDto mapToDto(ItemEntity itemEntity);

}