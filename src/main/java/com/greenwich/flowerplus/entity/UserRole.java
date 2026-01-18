package com.greenwich.flowerplus.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserRole extends BaseCreatedEntity {

    // 1. Khai báo Composite Key
    @EmbeddedId
    private UserRoleId id;

    // 2. Mapping quan hệ với User
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Map vào field 'userId' của UserRoleId
    @JoinColumn(name = "user_id")
    private UserAccount user;

    // 3. Mapping quan hệ với Role
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId") // Map vào field 'roleId' của UserRoleId
    @JoinColumn(name = "role_id")
    private Role role;

    // Constructor tiện ích để tạo nhanh
    public UserRole(UserAccount user, Role role) {
        this.user = user;
        this.role = role;
        this.id = new UserRoleId(user.getId(), role.getId());
    }
}