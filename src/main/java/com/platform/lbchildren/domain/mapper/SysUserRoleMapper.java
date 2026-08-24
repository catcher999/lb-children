package com.platform.lbchildren.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.lbchildren.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * RBAC 用户-角色关联 Mapper
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 幂等地为用户绑定角色：user_id + user_role 已存在则忽略。
     * role_id 通过子查询按角色编码解析。
     */
    @Insert("""
            INSERT IGNORE INTO sys_user_role (user_id, user_role, role_id)
            SELECT #{userId}, #{userRole}, id FROM sys_role WHERE code = #{roleCode}
            """)
    int ensureUserRole(@Param("userId") Long userId,
                       @Param("userRole") String userRole,
                       @Param("roleCode") String roleCode);
}
