package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.utils.TsidUtils;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@MappedSuperclass
@Getter
@Setter

public abstract class BaseTsidEntity extends BaseAuditEntity<Long> {

    @Id
    private Long id;

    // Optimistic Locking: Prevents "Lost Update" when multiple users edit the same record
    // JPA automatically checks this version on update and throws OptimisticLockException if stale
    @Version
    private Long version;


    // Override cái hook của cha
    @Override
    protected void onPrePersist() {
        if (this.id == null) {
            this.id = TsidUtils.nextId();
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    // --- EQUALS & HASHCODE (Chuẩn Hibernate) ---
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        BaseTsidEntity that = (BaseTsidEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}