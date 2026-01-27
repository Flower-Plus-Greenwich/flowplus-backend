package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.dto.request.MaterialRequest;
import com.greenwich.flowerplus.dto.request.MaterialSearchRequest;
import com.greenwich.flowerplus.dto.response.MaterialResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MaterialService {
    Page<MaterialResponse> searchMaterials(MaterialSearchRequest request);

    List<MaterialResponse> getAllMaterials();

    MaterialResponse getMaterialById(Long id);

    MaterialResponse createMaterial(MaterialRequest request);

    MaterialResponse updateMaterial(Long id, MaterialRequest request);

    void deleteMaterial(Long id);
}

