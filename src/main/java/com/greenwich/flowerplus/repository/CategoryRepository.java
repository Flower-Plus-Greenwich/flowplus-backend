package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Category> findByIsActiveTrueAndDeletedAtIsNullOrderByNameAsc();

    List<Category> findByDeletedAtIsNullOrderByNameAsc();

    List<Category> findByParentIsNullAndDeletedAtIsNullOrderByNameAsc();

    boolean existsByParentId(Long parentId);

    Optional<Category> findByIdAndDeletedAtIsNull(Long id);
}

