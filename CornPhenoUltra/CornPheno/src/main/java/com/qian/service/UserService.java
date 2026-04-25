package com.qian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qian.dto.request.*;
import com.qian.pojo.User;
import com.qian.dto.response.UserLoginInfoDTO;

public interface UserService extends IService<User> {

    UserLoginInfoDTO wxLogin(WxLoginDTO loginDTO);
    void register(RegisterRequest request);
    UserInfoDTO getUserInfo();
    void updateUserInfo(UpdateInfoRequest request);
    void logout();
}
