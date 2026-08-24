package com.platform.lbchildren.module.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.domain.dto.ChildLoginRequest;
import com.platform.lbchildren.domain.dto.RegisterRequest;
import com.platform.lbchildren.domain.entity.Child;
import com.platform.lbchildren.domain.entity.Parent;
import com.platform.lbchildren.domain.mapper.ChildMapper;
import com.platform.lbchildren.domain.mapper.ParentMapper;
import com.platform.lbchildren.module.auth.service.AuthService;
import com.platform.lbchildren.security.JwtUtil;
import com.platform.lbchildren.security.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现（阶段C：从 ParentServiceImpl/ChildServiceImpl 抽取）
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ParentMapper parentMapper;
    private final ChildMapper childMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RbacService rbacService;

    @Override
    public void register(RegisterRequest request) {
        Long count = parentMapper.selectCount(
                new LambdaQueryWrapper<Parent>().eq(Parent::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
        Parent parent = new Parent();
        parent.setUsername(request.getUsername());
        parent.setPassword(passwordEncoder.encode(request.getPassword()));
        parent.setPhone(request.getPhone());
        parent.setCreatedAt(LocalDateTime.now());
        parent.setUpdatedAt(LocalDateTime.now());
        parentMapper.insert(parent);
    }

    @Override
    public String login(String username, String password) {
        Parent parent = parentMapper.selectOne(
                new LambdaQueryWrapper<Parent>().eq(Parent::getUsername, username));
        if (parent == null || !passwordEncoder.matches(password, parent.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        // RBAC：登录成功即绑定家长角色（幂等）
        rbacService.ensureUserRole(parent.getId(), "PARENT");
        return jwtUtil.generateToken(parent.getUsername(), "PARENT", parent.getId());
    }

    @Override
    public String childLogin(ChildLoginRequest request) {
        Parent parent = parentMapper.selectOne(
                new LambdaQueryWrapper<Parent>().eq(Parent::getUsername, request.getParentUsername()));
        if (parent == null) {
            throw new BusinessException(ResultCode.PARENT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getParentPassword(), parent.getPassword())) {
            throw new BusinessException(ResultCode.LOGIN_FAIL);
        }
        Child child = childMapper.selectOne(new LambdaQueryWrapper<Child>()
                .eq(Child::getParentId, parent.getId())
                .eq(Child::getNickname, request.getChildNickname()));
        if (child == null) {
            throw new BusinessException(ResultCode.CHILD_NOT_FOUND);
        }
        // RBAC：登录成功即绑定儿童角色（幂等）
        rbacService.ensureUserRole(child.getId(), "CHILD");
        return jwtUtil.generateToken(child.getNickname(), "CHILD", child.getId());
    }
}
