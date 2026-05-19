package com.platform.lbchildren.service;

import com.platform.lbchildren.dto.AddChildRequest;
import com.platform.lbchildren.dto.RegisterRequest;
import com.platform.lbchildren.entity.Child;
import com.platform.lbchildren.entity.Parent;
import com.platform.lbchildren.repository.ChildRepository;
import com.platform.lbchildren.repository.ParentRepository;
import com.platform.lbchildren.security.JwtUtil;
import com.platform.lbchildren.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParentService {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(RegisterRequest request) {
        if (parentRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        Parent parent = new Parent();
        parent.setUsername(request.getUsername());
        parent.setPassword(passwordEncoder.encode(request.getPassword()));
        parent.setPhone(request.getPhone());
        parentRepository.save(parent);
        return "注册成功";
    }

    public String login(String username, String password) {
        Parent parent = parentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!passwordEncoder.matches(password, parent.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        return jwtUtil.generateToken(parent.getUsername(), "PARENT", parent.getId());
    }

    public String addChild(UserPrincipal principal, AddChildRequest request) {
        Parent parent = parentRepository.findById(principal.getUserId())
                .orElseThrow(() -> new RuntimeException("家长不存在"));
        Child child = new Child();
        child.setNickname(request.getNickname());
        child.setAge(request.getAge());
        child.setParent(parent);
        childRepository.save(child);
        return "儿童添加成功";
    }

    public List<Child> getMyChildren(Long parentId) {
        return childRepository.findByParentId(parentId);
    }
}