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


    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    // Consistent with PhoneValidator logic
    private static final Pattern PHONE_PATTERN_1 = Pattern.compile("^\\d{10}$");
    private static final Pattern PHONE_PATTERN_2 = Pattern.compile("^\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}$");
    private static final Pattern PHONE_PATTERN_3 = Pattern.compile("^\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}$");
    private static final Pattern PHONE_PATTERN_4 = Pattern.compile("^\\(\\d{3}\\)-\\d{3}-\\d{4}$");

    public static boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhoneNumber(String phone) {
        if (!StringUtils.hasText(phone)) return false;
        return PHONE_PATTERN_1.matcher(phone).matches() ||
               PHONE_PATTERN_2.matcher(phone).matches() ||
               PHONE_PATTERN_3.matcher(phone).matches() ||
               PHONE_PATTERN_4.matcher(phone).matches();
    }
    
    public static boolean hasNumber(String text) {
        return StringUtils.hasText(text) && text.chars().anyMatch(Character::isDigit);
    }
    
    public static boolean hasSpecialChar(String text) {
        return StringUtils.hasText(text) && !text.matches("[a-zA-Z0-9 ]*");
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

    public static boolean isValidName(String name) {
        if (!StringUtils.hasText(name)) {
            return false;
        }
        
        // Check for bad words
        String lowerName = name.toLowerCase(Locale.ROOT);
        for (String badWord : BlockWords.getBadWords()) {
            if (lowerName.contains(badWord)) {
                return false;
            }
        }

        // Name should not contain numbers or special characters (simple check)
        // Allow ONLY letters and spaces/hyphens strictly? 
        // Or reuse hasNumber / hasSpecialChar?
        // Let's discourage numbers and symbols in names except maybe hyphens/apostrophes
        return !hasNumber(name) && !hasSpecialChar(name);
    }




}
