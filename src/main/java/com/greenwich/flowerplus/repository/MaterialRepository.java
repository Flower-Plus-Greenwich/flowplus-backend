package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.common.enums.MaterialType;
import com.greenwich.flowerplus.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT m FROM Material m WHERE " +
           "(:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type IS NULL OR m.type = :type)")
    Page<Material> search(@Param("keyword") String keyword, 
                          @Param("type") MaterialType type, 
                          Pageable pageable);
}

