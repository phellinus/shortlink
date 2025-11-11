package org.sangyu.shortlink.admin.toolkit;

import java.util.Random;

/**
 * 分组ID随机生成器
 */
public final class RandomGenerator {
    // 可用字符集（数字 + 大小写字母）
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 6;
    private static final Random RANDOM = new Random();

    /**
     * 生成默认长度（6位）的随机字符串
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机字符串
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }
}
