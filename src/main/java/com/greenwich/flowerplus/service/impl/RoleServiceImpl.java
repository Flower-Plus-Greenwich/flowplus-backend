package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.RoleRequest;
import com.greenwich.flowerplus.dto.response.RoleResponse;
import com.greenwich.flowerplus.entity.Role;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.entity.UserProfile;
import com.greenwich.flowerplus.repository.RoleRepository;
import com.greenwich.flowerplus.repository.UserAccountRepository;
import com.greenwich.flowerplus.repository.UserProfileRepository;
import com.greenwich.flowerplus.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(Long userId, Long roleId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.addRole(role);
        userAccountRepository.save(user);
        log.info("Assigned role {} to user {}", role.getName(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long userId, Long roleId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.removeRole(role);
        userAccountRepository.save(user);
        log.info("Removed role {} from user {}", role.getName(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.findByName(request.name()).isPresent()) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT);
        }
        Role role = Role.builder()
                .name(request.name())
                .description(request.description())
                .build();
        role = roleRepository.save(role);
        return enrichRoleResponse(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Optional<Role> existingRole = roleRepository.findByName(request.name());
        if (existingRole.isPresent() && !existingRole.get().getId().equals(id)) {
            throw new AppException(ErrorCode.RESOURCE_CONFLICT);
        }

        role.setName(request.name());
        role.setDescription(request.description());
        role = roleRepository.save(role);
        return enrichRoleResponse(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleRepository.deleteById(id);
    }

    @Override
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream()
                .map(this::enrichRoleResponse)
                .toList();
    }

    @Override
    public RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return enrichRoleResponse(role);
    }

    @Override
    public RoleResponse enrichRoleResponse(Role role) {
        String createdByName = resolveUserName(role.getCreatedBy());
        String updatedByName = resolveUserName(role.getUpdatedBy());

        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                createdByName != null ? createdByName : role.getCreatedBy(),
                updatedByName != null ? updatedByName : role.getUpdatedBy()
        );
    }

    private String resolveUserName(String identifier) {
        if (identifier == null) return null;

        // 1. Try to treat identifier as User ID First (Based on SecurityUserDetails.getUsername returning ID)
        try {
            Long userId = Long.valueOf(identifier);
            Optional<UserProfile> profileOpt = userProfileRepository.findById(userId);
            if (profileOpt.isPresent()) {
                return profileOpt.get().getFullName();
            }
            // Fallback to username if profile missing
            Optional<UserAccount> accountOpt = userAccountRepository.findById(userId);
            if (accountOpt.isPresent()) {
                return accountOpt.get().getUsername();
            }
        } catch (NumberFormatException e) {
            // Not a numeric ID, continue to fallback
        }

        // 2. Fallback to find by username (legacy or fallback)
        Optional<UserAccount> userOpt = userAccountRepository.findByUsername(identifier);
        if (userOpt.isPresent()) {
            Optional<UserProfile> profileOpt = userProfileRepository.findById(userOpt.get().getId());
            if (profileOpt.isPresent()) {
                return profileOpt.get().getFullName();
            }
            return userOpt.get().getUsername();
        }

        return identifier;
    }
}
