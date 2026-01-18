package com.greenwich.flowerplus.infrastructure.security;

import com.greenwich.flowerplus.common.enums.ErrorCode;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserAccount user;
        if (isTsid(identifier)) {
            long userId = Long.parseLong(identifier);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by ID: " + userId));
        } else {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.INVALID_CREDENTIALS.name()));
        }

        // Map roles to authorities
        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .map(userRole -> new SimpleGrantedAuthority("ROLE_" + userRole.getRole().getName()))
                .collect(Collectors.toList());

        return SecurityUserDetails.build(user, authorities);
    }

    private boolean isTsid(String str) {
        return str != null && str.matches("\\d+");
    }

    public Optional<UserAccount> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return Optional.empty();

        Object principal = authentication.getPrincipal();

        if (principal instanceof SecurityUserDetails sud) {
            return Optional.of(sud.getUser());
        }
        return Optional.empty();
    }
}
