package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.IdentityProvider;
import com.greenwich.flowerplus.common.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * User account entity containing authentication data.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
@Entity
@Table(name = "users")
public class UserAccount extends BaseTsidSoftDeleteEntity {

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = CommonConfig.MAX_LENGTH_PASSWORD)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "provider", length = 20)
    private IdentityProvider provider = IdentityProvider.LOCAL;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    public void addRole(Role role) {
        if (this.userRoles == null) {
            this.userRoles = new HashSet<>();
        }
        UserRole userRole = new UserRole(this, role);
        this.userRoles.add(userRole);
    }

    public void removeRole(Role role) {
        this.userRoles.removeIf(ur -> ur.getRole().equals(role));
    }
}
