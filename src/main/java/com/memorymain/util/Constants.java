package com.memorymain.util;

/**
 * 用户常量信息
 *
 * @author ruoyi
 */
public class Constants
{
    /**
     * 管理员ID
     */
    public static final Long ADMIN_ID = 1L;
    /**
     * 平台内系统用户的唯一标志
     */
    public static final String SYS_USER = "SYS_USER";

    /** 正常状态 */
    public static final String NORMAL = "0";

    /** 异常状态 */
    public static final String EXCEPTION = "1";

    /**
     * 用户名长度限制
     */
    public static final int USERNAME_MIN_LENGTH = 2;
    public static final int USERNAME_MAX_LENGTH = 20;

    /**
     * 密码长度限制
     */
    public static final int PASSWORD_MIN_LENGTH = 5;
    public static final int PASSWORD_MAX_LENGTH = 20;

    /**
     * 用户权限标识
      */
    public final static String ADMIN = "admin";
    public final static String COMMON = "common";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";
    /**
     * 验证码有效期（分钟）
     */
    public static final Integer CAPTCHA_EXPIRATION = 2;
    public static final String LOGIN_USER_KEY = "loginUser";
    public static final String USER_KEY = "userId";
    /**
     * 令牌
     */
    public static final String TOKEN = "token";
}
