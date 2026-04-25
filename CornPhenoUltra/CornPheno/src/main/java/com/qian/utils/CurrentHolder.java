package com.qian.utils;

public class CurrentHolder {

    private static final ThreadLocal<Long> CURRENT_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        CURRENT_LOCAL.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_LOCAL.get();
    }

    public static void remove() {
        CURRENT_LOCAL.remove();
    }
}