package com.greenwich.flowerplus.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserRole extends BaseCreatedEntity {

    // 1. Declare Composite Key
    @EmbeddedId
    private UserRoleId id;

    // 2. Mapping relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Map to 'userId' field of UserRoleId
    @JoinColumn(name = "user_id")
    private UserAccount user;

    // 3. Mapping relationship with Role
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId") // Map to 'roleId' field of UserRoleId
    @JoinColumn(name = "role_id")
    private Role role;

    // Utility constructor for quick creation
    public UserRole(UserAccount user, Role role) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleId(user.getId(), role.getId());
    }
}