package org.sangyu.shortlink.admin.common.biz.user.interceptor;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.sangyu.shortlink.admin.common.biz.user.UserContext;
import org.sangyu.shortlink.admin.common.biz.user.UserInfoDTO;
import org.sangyu.shortlink.admin.common.convention.exception.ClientException;
import org.sangyu.shortlink.admin.common.utils.JwtUtil;
import org.sangyu.shortlink.admin.dao.entity.UserDO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.sangyu.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

@Slf4j
public class UserTransmitInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final StringRedisTemplate stringRedisTemplate;

    public UserTransmitInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        resolveUserFromJwt(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeUser();
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
                throw new ClientException("token解析失败");
            }
            if (!JwtUtil.validateToken(token, username, JwtUtil.TokenType.ACCESS)) {
                throw new ClientException("登录已失效，请重新登录");
            }

            UserInfoDTO userInfoDTO = buildUserInfo(username, token);
            UserContext.setUser(userInfoDTO);

        } catch (ClientException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to parse JWT token", ex);
            throw new ClientException("用户登录状态异常，请重新登录");
        }
    }

    private UserInfoDTO buildUserInfo(String username, String token) {
        String cacheKey = USER_LOGIN_KEY + username;
        String cachedUser = stringRedisTemplate.opsForValue().get(cacheKey);

        if (!StringUtils.hasText(cachedUser)) {
            throw new ClientException("用户未登录或会话已失效");
        }

        UserDO userDO = JSON.parseObject(cachedUser, UserDO.class);
        if (userDO == null) {
            throw new ClientException("用户信息解析失败");
        }

        return UserInfoDTO.builder()
                .userId(userDO.getId() == null ? null : String.valueOf(userDO.getId()))
                .username(userDO.getUsername())
                .realName(userDO.getRealName())
                .phone(userDO.getPhone())
                .mail(userDO.getMail())
                .token(token)
                .build();
    }
}