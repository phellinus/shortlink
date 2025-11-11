package org.sangyu.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sangyu.shortlink.admin.dao.entity.UserDO;
import org.sangyu.shortlink.admin.dto.req.UserLoginReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.sangyu.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.sangyu.shortlink.admin.dto.resp.UserRespDTO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 判断用户名是否存在
     * @param username 用户名
     * @return 用户名存在返回false，用户名不存在返回true
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 用户修改信息
     * @param requestParam 用户修改信息请求参数
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录请求参数
     * @return 用户登录响应参数 token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     * @param username 用户名
     * @param token token
     * @return 登录返回true，未登录返回false
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     * @param username 用户名
     * @param token token
     */
    void logout(String username, String token);

    /**
     * 刷新token
     * @param requestParam 刷新token请求参数
     * @return 新的token信息
     */
    UserLoginRespDTO refreshToken(UserRefreshTokenReqDTO requestParam);
}
