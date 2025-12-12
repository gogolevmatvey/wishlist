package com.example.wishlist.config;

public class AppConfig {

    // Настройки API
    public static final String API_BASE_URL = "http://192.168.56.1/";
    public static final int API_TIMEOUT = 30; // секунд

    // Настройки изображений
    public static final int MAX_IMAGE_SIZE_KB = 5120; // 5MB
    public static final int IMAGE_COMPRESSION_QUALITY = 80; // %
    public static final int IMAGE_PREVIEW_WIDTH = 400;
    public static final int IMAGE_PREVIEW_HEIGHT = 400;

    // Настройки приложения
    public static final String APP_SHARED_PREFS = "wishlist_prefs";
    public static final String PREF_FIRST_RUN = "first_run";
    public static final String PREF_USER_ID = "user_id";

    // Константы
    public static final String STATUS_WISH = "wish";
    public static final String STATUS_PURCHASED = "purchased";
    public static final String STATUS_CANCELLED = "cancelled";

    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_MEDIUM = "medium";
    public static final String PRIORITY_HIGH = "high";
}