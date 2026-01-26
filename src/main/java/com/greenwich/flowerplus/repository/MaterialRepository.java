package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
}
