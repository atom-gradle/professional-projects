package com.qian.interceptor;

import com.qian.mapper.UserMapper;
import com.qian.pojo.User;
import com.qian.utils.CurrentHolder;
import com.qian.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    // 以下排除的路径（不需要令牌验证、不需要通过审核(不需要audit_status=2)）
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            // Swagger相关路径
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-ui/index.html",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/api-docs/**",

            // 其他需要排除的路径
            "/api/public/**",
            "/error",
            "/favicon.ico",

            // 微信登录
            "/api/v1/user/wx-login",

            // 算法调用
            "/api/v1/analysis/*/submit",

            // 统计
            "/api/v1/admin/statistics"

    );

    // 排除的路径（需要令牌验证，但不需要通过审核(不需要audit_status=2)）
    private static final List<String> EXCLUDE_PATHS2 = Arrays.asList(
            // 认证相关
            "/api/v1/user/register",
            "/api/v1/user/info",
            "/api/v1/user/logout"
    );

    public TokenInterceptor(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        log.info("请求路径为:{}", requestUri);

        for (String excludePath : EXCLUDE_PATHS) {
            if (matchPath(excludePath, requestUri)) {
                log.info("路径 {} 匹配排除模式 {}, 放行", requestUri, excludePath);
                return true;
            }
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.info("收到 OPTIONS 预检请求，直接放行");
            return true;
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader);

        // 判断令牌是否存在
        if (!StringUtils.hasLength(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.info("获取到Jwt令牌为空或格式不正确, 返回错误结果");

            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            // 返回JSON格式的错误信息
            String errorJson = "{\"code\":401,\"message\":\"请先登录\"}";
            response.getWriter().write(errorJson);
            return false;
        }

        // 提取token（去掉"Bearer "前缀）
        String jwt = authHeader.substring(7).trim();
        log.info("提取的token: {}", jwt);

        // 解析token
        try {
            Claims claims = JwtUtils.parseJWT(jwt);
            Integer userIdInt = (Integer)claims.get("userId");
            Long userId = userIdInt != null ? userIdInt.longValue() : null;

            // 1.检查userId
            if (userId == null) {
                log.error("token中未找到userId");
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"无效的token\"}");
                return false;
            }

            // 2.放行需要令牌，但不需要autid_status == 2的路径
            log.info("放行需要令牌，但不需要autid_status == 2的路径");
            if(EXCLUDE_PATHS2.contains(requestUri)) {
                User user_ = userMapper.selectById(userId);
                if(user_ == null) {
                    log.info("用户不存在，不放行");
                    return false;
                }
                CurrentHolder.setCurrentId(userId);
                log.info("认证相关接口无需audit_status == 2，放行, userId: {}, 放行", userId);
                return true;
            }

            // 3.进一步检查audit_status，未审核通过则拒绝
            log.info("进一步检查audit_status == 2，才放行，userId为：{}", userId);
            User user = userMapper.selectById(userId);
            if(user == null || user.getAuditStatus() != 2) {
                log.info("用户不存在或 用户状态 != 审核通过，不予放行！");
                return false;
            }

            // 4.保存到ThreadLocal
            CurrentHolder.setCurrentId(userId);
            log.info("token解析成功, userId: {}, 放行", userId);
            return true;

        } catch (Exception e) {
            log.error("解析令牌失败: {}", e.getMessage());
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\"}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentHolder.remove();
        log.info("请求结束，已清空当前线程绑定的用户ID");
    }

    private boolean matchPath(String pattern, String path) {
        // 处理 /** 模式
        if (pattern.endsWith("/**")) {
            String basePath = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(basePath);
        }

        // 处理 /*/ 模式
        if (pattern.contains("/*/")) {
            String regex = pattern.replace("*", "[^/]*").replace("/", "\\/");
            return path.matches(regex);
        }

        // 精确匹配
        return pattern.equals(path);
    }

}
