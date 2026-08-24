package com.platform.lbchildren.module.auth.controller;

import com.platform.lbchildren.common.Result;
import com.platform.lbchildren.domain.dto.ChildLoginRequest;
import com.platform.lbchildren.domain.dto.LoginRequest;
import com.platform.lbchildren.domain.dto.RegisterRequest;
import com.platform.lbchildren.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证接口：家长注册/登录、儿童登录（阶段C从 ParentController/ChildController 抽出）
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/parent/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok("注册成功");
    }

    @PostMapping("/api/parent/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return Result.ok(Map.of("token", token, "role", "PARENT"));
    }

    @PostMapping("/api/child/login")
    public Result<Map<String, String>> childLogin(@Valid @RequestBody ChildLoginRequest request) {
        String token = authService.childLogin(request);
        return Result.ok(Map.of("token", token, "role", "CHILD"));
    }
}
