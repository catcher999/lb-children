package com.platform.lbchildren.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import com.platform.lbchildren.dto.AddChildRequest;
import com.platform.lbchildren.dto.ChildProfileVO;
import com.platform.lbchildren.dto.ChildVO;
import com.platform.lbchildren.dto.RegisterRequest;
import com.platform.lbchildren.entity.Child;
import com.platform.lbchildren.entity.Parent;
import com.platform.lbchildren.entity.UserProfile;
import com.platform.lbchildren.mapper.ChildMapper;
import com.platform.lbchildren.mapper.ParentMapper;
import com.platform.lbchildren.mapper.UserProfileMapper;
import com.platform.lbchildren.security.JwtUtil;
import com.platform.lbchildren.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 家长端业务实现
 */
@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentMapper parentMapper;
    private final ChildMapper childMapper;
    private final UserProfileMapper userProfileMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
        return jwtUtil.generateToken(parent.getUsername(), "PARENT", parent.getId());
    }

    @Override
    public void addChild(Long parentId, AddChildRequest request) {
        Parent parent = parentMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException(ResultCode.PARENT_NOT_FOUND);
        }
        Child child = new Child();
        child.setNickname(request.getNickname());
        child.setAge(request.getAge());
        child.setParentId(parent.getId());
        child.setCreatedAt(LocalDateTime.now());
        child.setUpdatedAt(LocalDateTime.now());
        childMapper.insert(child);
    }

    @Override
    public List<ChildVO> getMyChildren(Long parentId) {
        List<Child> children = childMapper.selectList(
                new LambdaQueryWrapper<Child>().eq(Child::getParentId, parentId));
        return children.stream().map(c -> {
            ChildVO vo = new ChildVO();
            vo.setId(c.getId());
            vo.setNickname(c.getNickname());
            vo.setAge(c.getAge());
            vo.setAvatar(c.getAvatar());
            vo.setProfileConsent(Boolean.TRUE.equals(c.getProfileConsent()));
            return vo;
        }).toList();
    }

    @Override
    public void setProfileConsent(Long parentId, Long childId, boolean consent) {
        Child child = requireOwnChild(parentId, childId);
        Child upd = new Child();
        upd.setId(child.getId());
        upd.setProfileConsent(consent);
        childMapper.updateById(upd);
    }

    @Override
    public ChildProfileVO getChildProfile(Long parentId, Long childId) {
        Child child = requireOwnChild(parentId, childId);
        if (!Boolean.TRUE.equals(child.getProfileConsent())) {
            throw new BusinessException(ResultCode.PROFILE_NOT_CONSENTED);
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, child.getId())
                .eq(UserProfile::getUserRole, "CHILD")
                .last("LIMIT 1"));
        ChildProfileVO vo = new ChildProfileVO();
        vo.setChildId(child.getId());
        vo.setNickname(child.getNickname());
        if (profile != null) {
            vo.setProfileSummary(profile.getProfileSummary());
            vo.setUpdatedAt(profile.getUpdatedAt());
        }
        return vo;
    }

    /** 校验儿童存在且归属当前家长，返回该儿童 */
    private Child requireOwnChild(Long parentId, Long childId) {
        Child child = childMapper.selectById(childId);
        if (child == null) {
            throw new BusinessException(ResultCode.CHILD_NOT_FOUND);
        }
        if (!child.getParentId().equals(parentId)) {
            throw new BusinessException(ResultCode.NOT_FAMILY);
        }
        return child;
    }
}
