package com.platform.lbchildren.security;

import com.platform.lbchildren.domain.mapper.SysRolePermissionMapper;
import com.platform.lbchildren.domain.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * RBAC 权限服务：登录时绑定角色、请求时加载权限集合。
 * <p>
 * 用户定位采用 (userId, userRole) 复合键——parent 与 child 的 id 可能重复，
 * userRole 指明数据源类型（PARENT/CHILD）。
 */
@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    /**
     * 登录成功后幂等地为用户绑定角色（user_id + user_role 已存在则忽略）。
     */
    public void ensureUserRole(Long userId, String userRole) {
        userRoleMapper.ensureUserRole(userId, userRole, userRole);
    }

    /**
     * 加载用户当前拥有的全部权限编码集合（每次请求实时查库，权限变更即时生效）。
     */
    public Set<String> loadPermissionCodes(Long userId, String userRole) {
        return Set.copyOf(rolePermissionMapper.selectCodesByUser(userId, userRole));
    }
}
