package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.response.AuditorResponse;
import com.greenwich.flowerplus.entity.UserAccount;
import com.greenwich.flowerplus.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.mapstruct.Named;

/**
 * AuditorMapper - Maps user IDs to AuditorResponse with caching
 * 
 * This component handles the enrichment of auditor data (createdBy, updatedBy)
 * with actual user information from the database.
 * 
 * Uses batch loading and caching to avoid N+1 query issues.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditorMapper {

    private final UserAccountRepository userAccountRepository;

    /**
     * Maps a user ID string or username to AuditorResponse
     * 
     * @param identifier The user ID or username as string (stored in createdBy/updatedBy)
     * @return AuditorResponse with user details, or null if not found
     */
    @Named("mapAuditor")
    public AuditorResponse map(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            return null;
        }

        String trimmed = identifier.trim();

        // 0. Handle SYSTEM user
        if ("SYSTEM".equalsIgnoreCase(trimmed)) {
            return AuditorResponse.builder()
                    .id(0L)
                    .username("SYSTEM")
                    .email("system@flowerplus.com")
                    .build();
        }
        
        // 1. Try parsing as ID (Long)
        try {
            Long userId = Long.parseLong(trimmed);
            return userAccountRepository.findById(userId)
                    .map(this::toAuditorResponse)
                    .orElse(AuditorResponse.builder().id(userId).build());
        } catch (NumberFormatException e) {
            // 2. Not a number, try as username
            log.debug("Auditor identifier is not a number, trying as username: {}", trimmed);
            return userAccountRepository.findByUsername(trimmed)
                    .map(this::toAuditorResponse)
                    .orElse(AuditorResponse.builder().username(trimmed).build());
        }
    }

    /**
     * Batch load auditors to avoid N+1 queries
     * 
     * @param userIds Set of user ID strings
     * @return Map of userId -> AuditorResponse
     */
    public Map<String, AuditorResponse> batchLoadAuditors(Set<String> userIds) {
        Map<String, AuditorResponse> result = new HashMap<>();

        Set<Long> parsedIds = userIds.stream()
                .filter(StringUtils::hasText)
                .map(id -> {
                    try {
                        return Long.parseLong(id.trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());

        if (parsedIds.isEmpty()) {
            return result;
        }

        userAccountRepository.findAllById(parsedIds)
                .forEach(user -> result.put(
                        String.valueOf(user.getId()), 
                        toAuditorResponse(user)
                ));

        return result;
    }

    private AuditorResponse toAuditorResponse(UserAccount user) {
        return AuditorResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarUrl(null) // TODO: Add avatar URL from UserProfile if needed
                .build();
    }
}
