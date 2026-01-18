package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;


@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseAuditEntity<T extends Serializable> extends BaseEntity<T> {

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected void onPrePersist() {
        // Hook for subclasses, ko làm gì ở đây ai cần thì override
    }

    @PrePersist
    public void prePersistAudit() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

        this.onPrePersist();
    }

    @PreUpdate
    public void preUpdateAudit() {
        this.updatedAt = Instant.now();
    }

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy; // Lưu Username hoặc UserID

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

}