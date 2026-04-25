package com.qian.controller;

import com.qian.common.Result;
import com.qian.dto.request.*;
import com.qian.exception.AuthenticationFailureException;
import com.qian.dto.response.UserLoginInfoDTO;
import com.qian.service.UserService;

import com.qian.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

/**
 * @version 1.0.0
 * @author Qian
 * @since 2.0.0
 */

@Tag(name="用户认证相关接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/wx-login")
    public Result<UserLoginInfoDTO> wxLogin(@RequestBody WxLoginDTO loginDTO) {
        String wxCode = loginDTO.getCode();
        if (StringUtils.isBlank(wxCode)) {
            throw new AuthenticationFailureException("缺少微信登录凭证 code");
        }

        log.info("收到微信登录请求，wxCode: {}", wxCode);

        UserLoginInfoDTO userLoginInfoDTO = userService.wxLogin(loginDTO);
        return Result.success(userLoginInfoDTO);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest request) {
        log.info("用户注册");

        userService.register(request);
        return Result.success();
    }

    @GetMapping("/logout")
    public Result<?> logout() {
        log.info("用户退出登录");

        userService.logout();

        return Result.success();
    }
}

