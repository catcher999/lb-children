-- 只保留教育资源，其他初始数据改为 Java 代码初始化
INSERT INTO edu_resource (title, type, url, description, cover_url) VALUES
                                                             ('小学英语入门', 'VIDEO', 'https://example.com/english1.mp4', '适合6-10岁英语启蒙', 'https://example.com/covers/english1.jpg'),
                                                             ('数学思维训练', 'COURSE', 'https://example.com/math1', '小学数学重点知识讲解', 'https://example.com/covers/math1.jpg'),
                                                             ('安全知识小课堂', 'VIDEO', 'https://example.com/safety.mp4', '防溺水、交通安全教育', 'https://example.com/covers/safety.jpg');
