package org.sangyu.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.sangyu.shortlink.admin.common.convention.exception.ClientException;
import org.sangyu.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.sangyu.shortlink.admin.dao.entity.UserDO;
import org.sangyu.shortlink.admin.dao.mapper.UserMapper;
import org.sangyu.shortlink.admin.dto.req.UserLoginReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.sangyu.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.sangyu.shortlink.admin.dto.resp.UserRespDTO;
import org.sangyu.shortlink.admin.common.utils.JwtUtil;
import org.sangyu.shortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.sangyu.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.sangyu.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static org.sangyu.shortlink.admin.common.constant.RedisCacheConstant.USER_REFRESH_TOKEN_KEY;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        //如果用户名存在
        if (!hasUsername(requestParam.getUsername())) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIT);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY+requestParam.getUsername());
        try {
            if (lock.tryLock()) {
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if(insert<1){
                    throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return;
            }
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIT);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO 验证当前的用户是否为登录用户
        LambdaUpdateWrapper<UserDO> wrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class),wrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO result = baseMapper.selectOne(wrapper);
        if(result == null) {
            throw new ClientException("用户不存在");
        }
        Boolean hasLogin = stringRedisTemplate.hasKey(USER_LOGIN_KEY + requestParam.getUsername());
        if (hasLogin) {
            throw new ClientException("用户已登录");
        }
        // 生成JWT Token
        String accessToken = JwtUtil.generateAccessToken(requestParam.getUsername());
        String refreshToken = JwtUtil.generateRefreshToken(requestParam.getUsername());
        // 将用户信息存储到Redis，Key为用户名，Value为JSON字符串
        stringRedisTemplate.opsForValue().set(
                USER_LOGIN_KEY + requestParam.getUsername(),
                JSON.toJSONString(result),
                JwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );
        stringRedisTemplate.opsForValue().set(
                USER_REFRESH_TOKEN_KEY + requestParam.getUsername(),
                refreshToken,
                JwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );

        return new UserLoginRespDTO(accessToken, refreshToken, JwtUtil.getAccessExpirationTime());
    }

    /**
     * 检查用户是否登录
     * @param username 用户名
     * @param token token
     * @return boolean
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        // 验证JWT访问Token是否有效
        if (!JwtUtil.validateToken(token, username, JwtUtil.TokenType.ACCESS)) {
            return false;
        }
        // 检查Redis中是否存在用户登录信息
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(USER_LOGIN_KEY + username));
    }

    /**
     * 用户退出登录
     * @param username 用户名
     * @param token token
     */
    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(Arrays.asList(
                    USER_LOGIN_KEY + username,
                    USER_REFRESH_TOKEN_KEY + username
            ));
            return;
        }
        throw new ClientException("用户token不存在或者用户未登录");
    }

    /**
     * 刷新token
     * @param requestParam 刷新请求
     * @return 新的token
     */
    @Override
    public UserLoginRespDTO refreshToken(UserRefreshTokenReqDTO requestParam) {
        String refreshToken = requestParam.getRefreshToken();
        if (refreshToken == null) {
            throw new ClientException("刷新token不能为空");
        }
        String username;
        try {
            username = JwtUtil.getUsernameFromToken(refreshToken);
        } catch (Exception e) {
            throw new ClientException("刷新token解析失败");
        }
        Boolean hasLogin = stringRedisTemplate.hasKey(USER_LOGIN_KEY + username);
        if (!Boolean.TRUE.equals(hasLogin)) {
            throw new ClientException("用户未登录或会话已失效");
        }
        if (!JwtUtil.validateToken(refreshToken, username, JwtUtil.TokenType.REFRESH)) {
            throw new ClientException("刷新token无效");
        }
        String cachedRefreshToken = stringRedisTemplate.opsForValue().get(USER_REFRESH_TOKEN_KEY + username);
        if (cachedRefreshToken == null || !cachedRefreshToken.equals(refreshToken)) {
            throw new ClientException("刷新token已失效");
        }
        String newAccessToken = JwtUtil.generateAccessToken(username);
        String newRefreshToken = JwtUtil.generateRefreshToken(username);
        stringRedisTemplate.opsForValue().set(
                USER_REFRESH_TOKEN_KEY + username,
                newRefreshToken,
                JwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );
        stringRedisTemplate.expire(
                USER_LOGIN_KEY + username,
                JwtUtil.getRefreshExpirationTime(),
                TimeUnit.MILLISECONDS
        );
        return new UserLoginRespDTO(newAccessToken, newRefreshToken, JwtUtil.getAccessExpirationTime());
    }
}
