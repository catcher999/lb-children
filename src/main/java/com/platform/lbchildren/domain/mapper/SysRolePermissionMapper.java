package com.platform.lbchildren.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.lbchildren.domain.entity.SysRolePermission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RBAC 角色-权限关联 Mapper
 */
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 按用户（user_id + user_role）联表查询其全部权限编码集合。
     * 链路：sys_user_role -> sys_role -> sys_role_permission -> sys_permission
     */
    @Select("""
            SELECT p.code
            FROM sys_permission p
            JOIN sys_role_permission rp ON p.id = rp.permission_id
            JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId} AND ur.user_role = #{userRole}
            """)
    List<String> selectCodesByUser(@Param("userId") Long userId, @Param("userRole") String userRole);
}
