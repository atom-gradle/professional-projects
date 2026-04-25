package com.qian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qian.dto.request.*;
import com.qian.exception.AuthenticationFailureException;
import com.qian.exception.BusinessException;
import com.qian.exception.InvalidOperationException;
import com.qian.mapper.UserMapper;
import com.qian.dto.response.UserLoginInfoDTO;
import com.qian.pojo.User;
import com.qian.service.UserService;
import com.qian.utils.CurrentHolder;
import com.qian.utils.JwtUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @version 2.0.0
 * @author Qian
 * @since 2.0.0
 */

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String appSecret;

    private final RestTemplate restTemplate;

    private final UserMapper userMapper;

    // 定义为静态常量或注入Bean
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public UserServiceImpl(RestTemplate restTemplate, UserMapper userMapper) {
        this.restTemplate = restTemplate;
        this.userMapper = userMapper;
    }

    @Operation(summary = "微信登录")
    public UserLoginInfoDTO wxLogin(WxLoginDTO loginDTO) {
        // 1. 调用微信接口获取 openid
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, loginDTO.getCode()
        );

        // 修改：先获取String响应
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String responseBody = responseEntity.getBody();

        log.info("微信API原始响应: {}", responseBody);
        log.info("响应Content-Type: {}", responseEntity.getHeaders().getContentType());

        // 手动解析JSON
        Map<String, Object> response;
        try {
            response = OBJECT_MAPPER.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析微信响应失败: {}", responseBody, e);
            throw new BusinessException("微信服务返回异常格式: " + responseBody);
        }

        // 检查错误码
        if (response.containsKey("errcode")) {
            Integer errcode = (Integer) response.get("errcode");
            String errmsg = (String) response.get("errmsg");

            log.error("微信API返回错误: code={}, message={}", errcode, errmsg);

            // 根据错误码给出友好提示
            String errorMessage;
            switch (errcode) {
                case 40029:
                    errorMessage = "登录凭证code无效或已过期，请重新获取";
                    break;
                case 45011:
                    errorMessage = "API调用太频繁，请稍后再试";
                    break;
                case 40013:
                    errorMessage = "AppId无效，请联系管理员";
                    break;
                case 40125:
                    errorMessage = "AppSecret无效，请联系管理员";
                    break;
                default:
                    errorMessage = "微信服务错误(" + errcode + "): " + errmsg;
            }
            throw new AuthenticationFailureException(errorMessage);
        }

        String openid = (String) response.get("openid");
        String unionid = (String) response.get("unionid"); // 可能为 null

        if (openid == null) {
            throw new BusinessException("微信返回缺少 openid");
        }

        // 2. 根据 openid 查询用户
        User user = userMapper.selectByOpenid(openid);

        if (user == null) {
            // 3. 用户不存在，自动注册
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setAuditStatus(1); // 注册未审核
            user.setLoginStatus((byte) 1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            userMapper.insert(user);
            log.info("新用户注册成功: openid={}", openid);
        }

        int audit_status = user.getAuditStatus();
        long userId = user.getId();

        // 4. 生成 token
        String token = JwtUtils.generateToken(Map.of(
                "userId", user.getId(),
                "openid", openid
        ));

        // 5. 返回 JWT 令牌
        UserLoginInfoDTO userLoginInfoDTO = new UserLoginInfoDTO();
        userLoginInfoDTO.setUserId(userId);
        userLoginInfoDTO.setAuditStatus(audit_status);
        userLoginInfoDTO.setToken(token);

        return userLoginInfoDTO;
    }

    @Override
    public void register(RegisterRequest request) {
        Long userId = CurrentHolder.getCurrentId();
        if(userId == null) {
            throw new AuthenticationFailureException("找不到用户，请先登录");
        }

        User user = userMapper.selectById(userId);

        if(user == null) {
            throw new AuthenticationFailureException("用户未微信登录");
        }

        //更新audit_status为待审核
        user.setAuditStatus(1);

        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEnterprise(request.getEnterprise());
        user.setStaffId(request.getStaffId());

        updateById(user);
    }

    public UserInfoDTO getUserInfo() {
        // 获取当前用户ID
        Long currentUserId = CurrentHolder.getCurrentId();
        log.info("当前用户id为：{}", currentUserId);

        User user = getById(currentUserId);
        if(user == null) {
            throw new BusinessException("用户未注册");
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);

        return userInfoDTO;
    }

    public void updateUserInfo(UpdateInfoRequest request) {
        log.info("更新用户信息");

        Long currentUserId = CurrentHolder.getCurrentId();
        User user = userMapper.selectById(currentUserId);

        if(user == null) {
            throw new BusinessException("用户不存在");
        }

        String phone = request.getPhone();
        User existingUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if(existingUser != null && !existingUser.getId().equals(currentUserId)) {
            throw new InvalidOperationException("该手机号已被其他用户使用");
        }

        user.setPhone(request.getPhone());
        user.setUsername(request.getUsername());
        user.setEnterprise(request.getEnterprise());
        user.setStaffId(request.getStaffId());

        user.setAuditStatus(1);// 设置为待审核
        user.setUpdateTime(LocalDateTime.now());

        updateById(user);

    }

    public void logout() {

        // 1.获取当前用户id
        Long userId = CurrentHolder.getCurrentId();
        if(userId == null) {
            throw new BusinessException("用户不存在");
        }

        // 2.更新MySQL中当前用户状态
        User user = getById(userId);
        user.setLoginStatus((byte) 0);// 未登录
        updateById(user);

        // 3.从ThreadLocal中移除
        CurrentHolder.remove();
    }

}