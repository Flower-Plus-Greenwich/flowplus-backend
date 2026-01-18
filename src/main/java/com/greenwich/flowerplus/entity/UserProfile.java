package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.constant.CommonConfig;
import com.greenwich.flowerplus.common.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * User profile entity containing personal information.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseSoftDeleteEntity<Long> {

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Override
    public Long getId() {
        return userId;
    }

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "full_name", nullable = false, length = CommonConfig.MAX_LENGTH_DISPLAY_NAME)
    private String fullName;

    @Column(name = "phone", length = CommonConfig.MAX_PHONE_NUMBER)
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
}
