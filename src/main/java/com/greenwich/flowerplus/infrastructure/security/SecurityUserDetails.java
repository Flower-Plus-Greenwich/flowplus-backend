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
        // IMPORTANT: Use Username (or TSID) as the system identifier
        // Do not use email here to avoid risks when the user changes their email
//        return user.getUsername();

        // If using TSID:
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
