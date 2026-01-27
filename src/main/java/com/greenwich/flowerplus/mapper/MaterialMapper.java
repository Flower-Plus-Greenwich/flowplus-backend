package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.request.MaterialRequest;
import com.greenwich.flowerplus.dto.response.MaterialResponse;
import com.greenwich.flowerplus.entity.Material;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = {AuditorMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialMapper {

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapAuditor")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "mapAuditor")
    MaterialResponse toResponse(Material material);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "materialStock", ignore = true)
    Material toEntity(MaterialRequest request);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "materialStock", ignore = true)
    void updateEntity(@MappingTarget Material material, MaterialRequest request);
}
