package org.sangyu.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.sangyu.shortlink.admin.common.utils.JwtUtil;
import org.sangyu.shortlink.admin.dao.entity.UserDO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static org.sangyu.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户信息传输过滤器
 */
@Slf4j
public class UserTransmitFilter implements Filter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate stringRedisTemplate;

    public UserTransmitFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        try {
            resolveUserFromJwt(httpServletRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    private void resolveUserFromJwt(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            return;
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            String username = JwtUtil.getUsernameFromToken(token);
            if (!StringUtils.hasText(username)) {
                return;
            }
            if (!JwtUtil.validateToken(token, username, JwtUtil.TokenType.ACCESS)) {
                log.debug("Invalid JWT token detected, skip user context population for URI {}", request.getRequestURI());
                return;
            }
            UserInfoDTO userInfoDTO = buildUserInfo(username, token);
            UserContext.setUser(userInfoDTO);
        } catch (Exception ex) {
            log.warn("Failed to parse JWT token from request URI {}", request.getRequestURI(), ex);
        }
    }

    private UserInfoDTO buildUserInfo(String username, String token) {
        if (stringRedisTemplate == null) {
            return minimalUser(username, token);
        }
        String cacheKey = USER_LOGIN_KEY + username;
        try {
            String cachedUser = stringRedisTemplate.opsForValue().get(cacheKey);
            if (!StringUtils.hasText(cachedUser)) {
                return minimalUser(username, token);
            }
            UserDO userDO = JSON.parseObject(cachedUser, UserDO.class);
            return UserInfoDTO.builder()
                    .userId(userDO.getId() == null ? null : String.valueOf(userDO.getId()))
                    .username(userDO.getUsername())
                    .realName(userDO.getRealName())
                    .phone(userDO.getPhone())
                    .mail(userDO.getMail())
                    .token(token)
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to deserialize user cache for username {}", username, ex);
            return minimalUser(username, token);
        }
    }

    private static UserInfoDTO minimalUser(String username, String token) {
        return UserInfoDTO.builder()
                .username(username)
                .token(token)
                .build();
    }
}
