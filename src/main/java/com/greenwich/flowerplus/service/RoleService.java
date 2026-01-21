package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.RoleRequest;
import com.greenwich.flowerplus.dto.response.RoleResponse;
import com.greenwich.flowerplus.entity.Role;

import java.util.List;

public interface RoleService {

    void assignRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    void deleteRole(Long id);

    List<RoleResponse> getRoles();

    RoleResponse getRoleById(Long roleId);

    RoleResponse enrichRoleResponse(Role role);

}
