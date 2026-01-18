package com.greenwich.flowerplus.infrastructure.security;

import com.greenwich.flowerplus.common.enums.UserStatus;
import com.greenwich.flowerplus.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Slf4j
public class SecurityUserDetails implements UserDetails {

    private Collection<? extends GrantedAuthority> authorities;
    private final UserAccount user;


    public static SecurityUserDetails build(UserAccount user, Collection<? extends GrantedAuthority> authorityList) {
        return SecurityUserDetails.builder()
                .authorities(authorityList)
                .user(user)
                .build();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public String getUsername() {
        // QUAN TRỌNG: dùng Username (hoặc TSID) làm định danh hệ thống
        // Không dùng email ở đây nữa để tránh rủi ro khi user đổi email
//        return user.getUsername();

        // Hoặc nếu bạn đã dùng TSID thì:
        return String.valueOf(user.getId());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("Returning authorities from SecurityUserDetails: {}", authorities);
        return this.authorities == null ? List.of() : this.authorities;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != UserStatus.UNVERIFIED && user.getStatus() != UserStatus.INCOMPLETE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.BANNED && user.getStatus() != UserStatus.LOCKED;
    }
}
