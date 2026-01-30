package com.greenwich.flowerplus.service;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.common.exception.AppException;
import com.greenwich.flowerplus.dto.request.MaterialRequest;
import com.greenwich.flowerplus.dto.request.MaterialSearchRequest;
import com.greenwich.flowerplus.dto.response.MaterialResponse;
import com.greenwich.flowerplus.entity.Material;
import com.greenwich.flowerplus.mapper.MaterialMapper;
import com.greenwich.flowerplus.repository.MaterialRepository;
import com.greenwich.flowerplus.service.impl.MaterialServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.greenwich.flowerplus.common.enums.MaterialType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MaterialServiceImpl.
 * Tests business logic, validation, and exception handling.
 * Uses Mockito for mocking repositories and mappers.
 */
@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialMapper materialMapper;

    @InjectMocks
    private MaterialServiceImpl materialService;

    // ============================================================================
    // Helper method for asserting AppException with ErrorCode
    // ============================================================================
    
    private void assertAppExceptionWithErrorCode(Runnable action, ErrorCode expectedErrorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(expectedErrorCode.getCode());
                });
    }

    // ============================================================================
    // SEARCH MATERIALS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Search Materials Tests")
    class SearchMaterialsTests {

        @Test
        @DisplayName("Should search materials with keyword and verify repository call")
        @SuppressWarnings("unchecked")
        void searchMaterials_WithKeyword_CallsRepositoryCorrectly() {
            // Arrange
            MaterialSearchRequest request = new MaterialSearchRequest();
            request.setKeyword("Rose");
            request.setPage(1);
            request.setSize(10);

            Material material = Material.builder()
                    .name("Red Rose")
                    .costPrice(new BigDecimal("5000"))
                    .build();
            material.setId(1L);

            MaterialResponse expectedResponse = new MaterialResponse();
            Page<Material> materialPage = new PageImpl<>(List.of(material));

            when(materialRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(materialPage);
            when(materialMapper.toResponse(any(Material.class))).thenReturn(expectedResponse);

            // Act
            Page<MaterialResponse> result = materialService.searchMaterials(request);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(expectedResponse);

            // Verify repository was called with correct Pageable
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(materialRepository).findAll(any(Specification.class), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageNumber()).isZero(); // page 1 -> index 0
            assertThat(capturedPageable.getPageSize()).isEqualTo(10);
            assertThat(capturedPageable.getSort()).isEqualTo(Sort.by("createdAt").descending());
        }

        @Test
        @DisplayName("Should search materials with empty keyword")
        @SuppressWarnings("unchecked")
        void searchMaterials_EmptyKeyword_ReturnsAll() {
            // Arrange
            MaterialSearchRequest request = new MaterialSearchRequest();
            request.setKeyword(""); // Empty keyword
            request.setPage(1);
            request.setSize(20);

            Page<Material> emptyPage = new PageImpl<>(List.of());
            when(materialRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Act
            Page<MaterialResponse> result = materialService.searchMaterials(request);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(materialRepository).findAll(any(Specification.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search materials with type filter")
        @SuppressWarnings("unchecked")
        void searchMaterials_WithTypeFilter_AppliesFilter() {
            // Arrange
            MaterialSearchRequest request = new MaterialSearchRequest();
            request.setKeyword("Rose");
            request.setType(MaterialType.FLOWER);
            request.setPage(1);
            request.setSize(10);

            Page<Material> materialPage = new PageImpl<>(List.of());
            when(materialRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(materialPage);

            // Act
            materialService.searchMaterials(request);

            // Assert
            verify(materialRepository).findAll(any(Specification.class), any(Pageable.class));
        }
    }

    // ============================================================================
    // GET ALL MATERIALS TESTS
    // ============================================================================

    @Nested
    @DisplayName("Get All Materials Tests")
    class GetAllMaterialsTests {

        @Test
        @DisplayName("Should get all materials sorted by name ascending")
        void getAllMaterials_ReturnsSortedByName() {
            // Arrange
            Material mat1 = Material.builder().name("Apple").build();
            Material mat2 = Material.builder().name("Banana").build();
            MaterialResponse resp1 = new MaterialResponse();
            MaterialResponse resp2 = new MaterialResponse();

            when(materialRepository.findAll(Sort.by("name").ascending()))
                    .thenReturn(List.of(mat1, mat2));
            when(materialMapper.toResponse(mat1)).thenReturn(resp1);
            when(materialMapper.toResponse(mat2)).thenReturn(resp2);

            // Act
            List<MaterialResponse> result = materialService.getAllMaterials();

            // Assert
            assertThat(result).hasSize(2);
            verify(materialRepository).findAll(Sort.by("name").ascending());
        }
    }

    // ============================================================================
    // GET MATERIAL BY ID TESTS
    // ============================================================================

    @Nested
    @DisplayName("Get Material By ID Tests")
    class GetMaterialByIdTests {

        @Test
        @DisplayName("Should get material by ID successfully")
        void getMaterialById_Success() {
            // Arrange
            Long materialId = 1L;
            Material material = Material.builder()
                    .name("Red Rose")
                    .costPrice(new BigDecimal("5000"))
                    .build();
            material.setId(materialId);

            MaterialResponse expectedResponse = new MaterialResponse();

            when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
            when(materialMapper.toResponse(material)).thenReturn(expectedResponse);

            // Act
            MaterialResponse result = materialService.getMaterialById(materialId);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("Should throw exception when material not found by ID")
        void getMaterialById_NotFound_ThrowsException() {
            // Arrange
            Long materialId = 999L;
            when(materialRepository.findById(materialId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> materialService.getMaterialById(materialId),
                    ErrorCode.MATERIAL_NOT_FOUND);
        }
    }

    // ============================================================================
    // CREATE MATERIAL TESTS
    // ============================================================================

    @Nested
    @DisplayName("Create Material Tests")
    class CreateMaterialTests {

        @Test
        @DisplayName("Should create material successfully")
        void createMaterial_Success() {
            // Arrange
            MaterialRequest request = new MaterialRequest();
            Material material = Material.builder()
                    .name("White Lily")
                    .costPrice(new BigDecimal("8000"))
                    .build();
            MaterialResponse expectedResponse = new MaterialResponse();

            when(materialMapper.toEntity(request)).thenReturn(material);
            when(materialRepository.save(material)).thenReturn(material);
            when(materialMapper.toResponse(material)).thenReturn(expectedResponse);

            // Act
            MaterialResponse result = materialService.createMaterial(request);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(materialRepository).save(material);
            verify(materialMapper).toEntity(request);
            verify(materialMapper).toResponse(material);
        }

        @Test
        @DisplayName("Should map all fields correctly when creating material")
        void createMaterial_MapsFieldsCorrectly() {
            // Arrange
            MaterialRequest request = new MaterialRequest();
            Material material = Material.builder()
                    .name("Sunflower")
                    .costPrice(new BigDecimal("12000"))
                    .unit("stem")
                    .build();
            material.setId(1L);

            when(materialMapper.toEntity(request)).thenReturn(material);
            when(materialRepository.save(any(Material.class))).thenAnswer(i -> i.getArguments()[0]);
            when(materialMapper.toResponse(any(Material.class))).thenReturn(new MaterialResponse());

            // Act
            materialService.createMaterial(request);

            // Assert
            ArgumentCaptor<Material> materialCaptor = ArgumentCaptor.forClass(Material.class);
            verify(materialRepository).save(materialCaptor.capture());

            Material savedMaterial = materialCaptor.getValue();
            assertThat(savedMaterial.getName()).isEqualTo("Sunflower");
            assertThat(savedMaterial.getCostPrice()).isEqualByComparingTo(new BigDecimal("12000"));
            assertThat(savedMaterial.getUnit()).isEqualTo("stem");
        }
    }

    // ============================================================================
    // UPDATE MATERIAL TESTS
    // ============================================================================

    @Nested
    @DisplayName("Update Material Tests")
    class UpdateMaterialTests {

        @Test
        @DisplayName("Should update material successfully")
        void updateMaterial_Success() {
            // Arrange
            Long materialId = 1L;
            MaterialRequest request = new MaterialRequest();
            Material existingMaterial = Material.builder()
                    .name("Old Name")
                    .costPrice(new BigDecimal("5000"))
                    .build();
            existingMaterial.setId(materialId);

            MaterialResponse expectedResponse = new MaterialResponse();

            when(materialRepository.findById(materialId)).thenReturn(Optional.of(existingMaterial));
            when(materialRepository.save(existingMaterial)).thenReturn(existingMaterial);
            when(materialMapper.toResponse(existingMaterial)).thenReturn(expectedResponse);

            // Act
            MaterialResponse result = materialService.updateMaterial(materialId, request);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(materialMapper).updateEntity(existingMaterial, request);
            verify(materialRepository).save(existingMaterial);
        }

        @Test
        @DisplayName("Should throw MATERIAL_NOT_FOUND when updating non-existent material")
        void updateMaterial_NotFound_ThrowsException() {
            // Arrange
            Long materialId = 999L;
            when(materialRepository.findById(materialId)).thenReturn(Optional.empty());

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> materialService.updateMaterial(materialId, new MaterialRequest()),
                    ErrorCode.MATERIAL_NOT_FOUND);

            verify(materialRepository, never()).save(any());
        }
    }

    // ============================================================================
    // DELETE MATERIAL TESTS
    // ============================================================================

    @Nested
    @DisplayName("Delete Material Tests")
    class DeleteMaterialTests {

        @Test
        @DisplayName("Should delete material successfully")
        void deleteMaterial_Success() {
            // Arrange
            Long materialId = 1L;
            when(materialRepository.existsById(materialId)).thenReturn(true);

            // Act
            materialService.deleteMaterial(materialId);

            // Assert
            verify(materialRepository).deleteById(materialId);
        }

        @Test
        @DisplayName("Should throw MATERIAL_NOT_FOUND when deleting non-existent material")
        void deleteMaterial_NotFound_ThrowsException() {
            // Arrange
            Long materialId = 999L;
            when(materialRepository.existsById(materialId)).thenReturn(false);

            // Act & Assert
            assertAppExceptionWithErrorCode(
                    () -> materialService.deleteMaterial(materialId),
                    ErrorCode.MATERIAL_NOT_FOUND);

            verify(materialRepository, never()).deleteById(any());
        }
    }
}
