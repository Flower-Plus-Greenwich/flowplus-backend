package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.common.ApiResult;
import com.greenwich.flowerplus.dto.response.RoleResponse;
import com.greenwich.flowerplus.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Validated
@PreAuthorize("hasAnyRole('SHOP_STAFF', 'SHOP_OWNER')")
public class TestController {

    private final RoleService roleService;

    @GetMapping("/test")
    public ResponseEntity<ApiResult<List<RoleResponse>>> getRoles() {

        List<RoleResponse> roles = roleService.getRoles();

        return ResponseEntity.ok(ApiResult.success(roles));
    }


}
