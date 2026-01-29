package com.greenwich.flowerplus.repository;

import com.greenwich.flowerplus.entity.ArrangementStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArrangementStyleRepository extends JpaRepository<ArrangementStyle, Long> {
}
