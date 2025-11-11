package org.sangyu.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.sangyu.shortlink.admin.common.convention.exception.ClientException;
import org.sangyu.shortlink.admin.common.convention.result.Result;
import org.sangyu.shortlink.admin.common.convention.result.Results;
import org.sangyu.shortlink.admin.dto.req.UserLoginReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserRefreshTokenReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.sangyu.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.sangyu.shortlink.admin.dto.resp.UserActualRespDTO;
import org.sangyu.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.sangyu.shortlink.admin.dto.resp.UserRespDTO;
import org.sangyu.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据手机号返回用户信息(脱敏版)
     * @param username //用户名
     * @return // result
     */
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        return Results.success(userService.getUserByUsername(username));
    }
    /**
     * 根据手机号返回用户信息(无脱敏)
     * @param username //用户名
     * @return //result
     */
    @GetMapping("/api/short-link/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getUserActualByUsername(@PathVariable("username") String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username), UserActualRespDTO.class));
    }
    /**
     * 判断用户名是否存在
     * @param username //用户名
     * @return result
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username){
        return Results.success(userService.hasUsername(username));
    }

    /**
     *
     * @param requestParam //用户注册请求参数
     * @return result
     */
    @PostMapping("/api/short-link/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.register(requestParam);
        return Results.success();
    }
    /**
     * 修改用户信息
     * @param requestParam //用户修改请求参数
     * @return result
     */
    @PutMapping("/api/short-link/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParam) {
        userService.update(requestParam);
        return Results.success();
    }

    /**
     * 用户登录
     * @param requestParam //用户登录请求参数
     * @return result
     */
    @PostMapping("/api/short-link/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        UserLoginRespDTO result = userService.login(requestParam);
        return Results.success(result);
    }

    /**
     * 刷新token
     * @param requestParam 刷新请求参数
     * @return result
     */
    @PostMapping("/api/short-link/v1/user/refresh-token")
    public Result<UserLoginRespDTO> refreshToken(@RequestBody UserRefreshTokenReqDTO requestParam) {
        return Results.success(userService.refreshToken(requestParam));
    }

    @GetMapping("/api/short-link/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username,
                                      @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Results.success(userService.checkLogin(username, resolveToken(authorization)));
    }

    @DeleteMapping("/api/short-link/v1/user/logout")
    public Result<Void> logout(@RequestParam("username") String username,
                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        userService.logout(username, resolveToken(authorization));
        return Results.success();
    }

    private String resolveToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ClientException("请求头中缺少或未按Bearer格式提供token");
        }
        return authorization.substring("Bearer ".length());
    }
}
