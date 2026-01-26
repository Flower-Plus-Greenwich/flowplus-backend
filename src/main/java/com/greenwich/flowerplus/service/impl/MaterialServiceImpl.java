package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    //test only
    @Override
    public List<Material> getMaterials() {
        return materialRepository.findAll();
    }
}
