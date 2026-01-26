package com.greenwich.flowerplus.controller;

import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/materials")
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public List<Material> getMaterials() {
        return materialService.getMaterials();
    }

}
