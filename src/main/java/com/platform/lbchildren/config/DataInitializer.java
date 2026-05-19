package com.platform.lbchildren.config;

import com.platform.lbchildren.entity.Child;
import com.platform.lbchildren.entity.Parent;
import com.platform.lbchildren.repository.ChildRepository;
import com.platform.lbchildren.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 如果 parent1 不存在则创建，密码动态加密
        if (!parentRepository.existsByUsername("parent1")) {
            Parent parent = new Parent();
            parent.setUsername("parent1");
            parent.setPassword(passwordEncoder.encode("123456"));  // 每次启动都重新加密
            parent.setPhone("13800138000");
            parentRepository.save(parent);  // 此时 parent 获得自增 ID

            // 同时创建孩子，并关联到刚保存的 parent
            Child child1 = new Child();
            child1.setNickname("小明");
            child1.setAge(10);
            child1.setParent(parent);
            childRepository.save(child1);

            Child child2 = new Child();
            child2.setNickname("小红");
            child2.setAge(8);
            child2.setParent(parent);
            childRepository.save(child2);
        }
        // 注：如果 H2 使用 create-drop，每次重启数据库清空，这里的条件判断将始终成立，会重新创建
    }
}