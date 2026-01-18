package com.greenwich.flowerplus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@MappedSuperclass
@Getter @Setter
public abstract class BaseTsidSoftDeleteEntity extends BaseTsidEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }


}