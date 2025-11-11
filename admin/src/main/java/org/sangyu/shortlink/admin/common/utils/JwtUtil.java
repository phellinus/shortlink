package org.sangyu.shortlink.admin.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {

    /**
     * JWT密钥（至少256位）
     */
    private static final String SECRET_KEY = "sangyu_shortlink_secret_key_2024_for_hs256_algorithm";
    
    /**
     * 获取签名密钥
     */
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 访问Token过期时间（30分钟）
     */
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 30 * 60 * 1000;

    /**
     * 刷新Token过期时间（7天）
     */
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    /**
     * Token类型
     */
    public enum TokenType {
        ACCESS,
        REFRESH
    }

    /**
     * 生成访问Token
     * @param username 用户名
     * @return JWT访问Token
     */
    public static String generateAccessToken(String username) {
        return createToken(new HashMap<>(), username, TokenType.ACCESS, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    /**
     * 生成刷新Token
     * @param username 用户名
     * @return JWT刷新Token
     */
    public static String generateRefreshToken(String username) {
        return createToken(new HashMap<>(), username, TokenType.REFRESH, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    /**
     * 创建JWT Token
     * @param claims 声明
     * @param subject 主题
     * @param tokenType token类型
     * @param expiration 过期时间
     * @return JWT Token
     */
    private static String createToken(Map<String, Object> claims, String subject, TokenType tokenType, long expiration) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        claims.put(TOKEN_TYPE_CLAIM, tokenType.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 从Token中获取声明
     * @param token JWT Token
     * @return 声明
     */
    private static Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @param username 用户名
     * @param expectedType 期望的token类型
     * @return 是否有效
     */
    public static boolean validateToken(String token, String username, TokenType expectedType) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenUsername = claims.getSubject();
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return tokenUsername.equals(username)
                    && expectedType.name().equals(tokenType)
                    && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     * @param claims JWT声明
     * @return 是否过期
     */
    private static boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 获取访问Token过期时间（毫秒）
     * @return 过期时间
     */
    public static long getAccessExpirationTime() {
        return ACCESS_TOKEN_EXPIRATION_TIME;
    }

    /**
     * 获取刷新Token过期时间（毫秒）
     * @return 过期时间
     */
    public static long getRefreshExpirationTime() {
        return REFRESH_TOKEN_EXPIRATION_TIME;
    }
}
