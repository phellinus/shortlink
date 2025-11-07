package org.sangyu.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.sangyu.shortlink.admin.dao.entity.UserDO;
import org.sangyu.shortlink.admin.dto.req.UserRegisterReqDTO;
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
}
