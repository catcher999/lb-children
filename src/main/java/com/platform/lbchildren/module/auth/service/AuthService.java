package com.platform.lbchildren.module.auth.service;

import com.platform.lbchildren.domain.dto.ChildLoginRequest;
import com.platform.lbchildren.domain.dto.RegisterRequest;

/**
 * 认证服务：家长注册/登录、儿童登录
 */
public interface AuthService {

    void register(RegisterRequest request);

    String login(String username, String password);

    String childLogin(ChildLoginRequest request);
}
