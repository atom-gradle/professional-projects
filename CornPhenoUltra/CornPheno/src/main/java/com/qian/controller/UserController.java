package com.qian.controller;

import com.qian.common.Result;
import com.qian.dto.request.*;
import com.qian.service.UserService;

import com.qian.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @version 3.0.0
 * @author Qian
 * @since 2.0.0
 */

@Tag(name="用户管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public Result<UserInfoDTO> getInfo() {
        log.info("查询当前用户信息");

        return Result.success(userService.getUserInfo());
    }

    @PutMapping("/info")
    public Result<?> updateInfo(@RequestBody UpdateInfoRequest request) {
        log.info("更新用户信息: {}", request);

        userService.updateUserInfo(request);

        return Result.success();
    }

}
