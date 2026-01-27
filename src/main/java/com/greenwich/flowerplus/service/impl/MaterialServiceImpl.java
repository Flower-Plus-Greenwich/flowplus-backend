package com.greenwich.flowerplus.service.impl;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.MaterialRequest;
import com.greenwich.flowerplus.dto.request.MaterialSearchRequest;
import com.greenwich.flowerplus.dto.response.MaterialResponse;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.mapper.MaterialMapper;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    @Override
    public Page<MaterialResponse> searchMaterials(MaterialSearchRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by("createdAt").descending()
        );

        Specification<Material> spec = buildSpecification(request);
        return materialRepository.findAll(spec, pageable)
                .map(materialMapper::toResponse);
    }

    private Specification<Material> buildSpecification(MaterialSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getKeyword())) {
                String pattern = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (request.getType() != null) {
                predicates.add(cb.equal(root.get("type"), request.getType()));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<MaterialResponse> getAllMaterials() {
        return materialRepository.findAll(Sort.by("name").ascending())
                .stream()
                .map(materialMapper::toResponse)
                .toList();
    }

    @Override
    public MaterialResponse getMaterialById(Long id) {
        return materialRepository.findById(id)
                .map(materialMapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));
    }

    @Override
    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        Material material = materialMapper.toEntity(request);
        material = materialRepository.save(material);
        log.info("Created material with id: {}", material.getId());
        return materialMapper.toResponse(material);
    }

    @Override
    @Transactional
    public MaterialResponse updateMaterial(Long id, MaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_NOT_FOUND));

        materialMapper.updateEntity(material, request);
        material = materialRepository.save(material);
        log.info("Updated material with id: {}", material.getId());
        return materialMapper.toResponse(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        if (!materialRepository.existsById(id)) {
            throw new AppException(ErrorCode.MATERIAL_NOT_FOUND);
        }
        materialRepository.deleteById(id);
        log.info("Deleted material with id: {}", id);
    }
}

