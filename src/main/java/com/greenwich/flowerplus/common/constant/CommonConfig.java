package com.greenwich.flowerplus.common.constant;

public class CommonConfig {



    private CommonConfig() {
    }
    // ========= USER ===============================
    public static final String DEFAULT_AVATAR_URL = "";
    public static final String DEFAULT_BACKGROUND_URL = "";
    public static final String DEFAULT_ROLE = "USER";

    public static final int MAX_LENGTH_USERNAME = 30;
    public static final int MAX_LENGTH_BIO = 160;
    public static final int MAX_LENGTH_EMAIL = 255;
    public static final int MIN_LENGTH_PASSWORD = 8;
    public static final int MAX_LENGTH_PASSWORD = 255;
    public static final int MAX_LENGTH_CONTENT_TITLE = 300;
    public static final int MAX_LENGTH_CONTENT_DESCRIPTION = 1000;
    public static final int MAX_LENGTH_DISPLAY_NAME = 100;
    public static final long TIME_EXPIRE_TOKEN_BLACK_LIST_MINUTES = 10;
    public static final int MAX_PHONE_NUMBER = 20;
    public static final int MAX_LENGTH_ADDRESS = 50;
    public static final int NAME_LENGTH = 100;
    // ========= PRODUCT ===============================
    public static final int PRODUCT_NAME_LENGTH = 200;
    public static final int SLUG_LENGTH = 250;
    public static final int SKU_LENGTH = 50;
    public static final int SHORT_DESC_LENGTH = 500;
}
