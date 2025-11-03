package org.sangyu.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.sangyu.shortlink.admin.common.convention.result.Result;
import org.sangyu.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.sangyu.shortlink.admin.dto.resp.UserRespDTO;
import org.sangyu.shortlink.admin.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO result = userService.getUserByUsername(username);
        if (result == null) {
            return new Result<UserRespDTO>().setCode(UserErrorCodeEnum.USER_NULL.code()).setMessage(UserErrorCodeEnum.USER_NULL.message());
        }else{
            return new Result<UserRespDTO>().setCode("0").setData(result);
        }
    }
}
