package com.greenwich.flowerplus.service.validator;


import com.greenwich.flowerplus.common.constant.BlockWords;
import com.greenwich.flowerplus.common.constant.CommonConfig;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

public class AccountAuthenticationValidator {

    private AccountAuthenticationValidator() {
    }

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$");


    public static boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidDisplayName(String displayName) {
        if (!StringUtils.hasText(displayName)) {
            return false;
        }
        String lowerName = displayName.toLowerCase(Locale.ROOT);
        for (String badWord : BlockWords.getBadWords()) {
            if (lowerName.contains(badWord)) {
                return false;
            }
        }
        return displayName.length() <= CommonConfig.MAX_LENGTH_DISPLAY_NAME;
    }




}
