package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.response.RoleResponse;
import com.greenwich.flowerplus.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    @Mapping(target = "createdBy", source = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", source = "updatedBy", ignore = true)
    RoleResponse roleToRoleResponse(Role role);

}
