package com.mall.common.core;

public final class Constants {

    private Constants() {
    }

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-User-Name";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final int ORDER_STATUS_PENDING_PAYMENT = 0;
    public static final int ORDER_STATUS_PAID = 1;
    public static final int ORDER_STATUS_CANCELLED = 2;
    public static final int ORDER_STATUS_COMPLETED = 3;
}
