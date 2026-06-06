package com.interview.common.util;

/**
 * 用户上下文（ThreadLocal）
 * 存储当前请求的用户信息，线程隔离
 * 在JwtInterceptor中设置，在Controller/Service中获取
 * 请求结束后在afterCompletion中清除，防止内存泄漏
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    /**
     * 请求结束时必须调用，防止内存泄漏
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
