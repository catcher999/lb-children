package com.platform.lbchildren.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.lbchildren.domain.entity.Child;
import com.platform.lbchildren.domain.entity.Parent;
import com.platform.lbchildren.domain.mapper.ChildMapper;
import com.platform.lbchildren.domain.mapper.ParentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 启动时初始化测试数据（家长 parent1 + 两个儿童）
 * 幂等：用户名已存在则跳过
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ParentMapper parentMapper;
    private final ChildMapper childMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (parentMapper.selectCount(
                new LambdaQueryWrapper<Parent>().eq(Parent::getUsername, "parent1")) > 0) {
            log.info("parent1 已存在，跳过初始化");
            return;
        }

        Parent parent = new Parent();
        parent.setUsername("parent1");
        parent.setPassword(passwordEncoder.encode("123456"));
        parent.setPhone("13800138000");
        parent.setCreatedAt(LocalDateTime.now());
        parent.setUpdatedAt(LocalDateTime.now());
        parentMapper.insert(parent);

        Child child1 = new Child();
        child1.setNickname("小明");
        child1.setAge(10);
        child1.setParentId(parent.getId());
        child1.setCreatedAt(LocalDateTime.now());
        child1.setUpdatedAt(LocalDateTime.now());
        childMapper.insert(child1);

        Child child2 = new Child();
        child2.setNickname("小红");
        child2.setAge(8);
        child2.setParentId(parent.getId());
        child2.setCreatedAt(LocalDateTime.now());
        child2.setUpdatedAt(LocalDateTime.now());
        childMapper.insert(child2);

        log.info("测试数据初始化完成：parent1 / 小明 / 小红");
    }
}