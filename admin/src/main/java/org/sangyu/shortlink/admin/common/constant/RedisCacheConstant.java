package org.sangyu.shortlink.admin.common.constant;

/**
 * 短链接后台管理 Redis 缓存常量类
 */
public class RedisCacheConstant {
    public static final String LOCK_USER_REGISTER_KEY = "short-link:lock_user-register:";
    
    /**
     * 用户登录缓存Key前缀
     */
    public static final String USER_LOGIN_KEY = "short-link:user:login:";
}
