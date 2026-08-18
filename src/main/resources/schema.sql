-- =========================================================
-- lb_children 建表脚本（MySQL 8.x）
-- 使用 IF NOT EXISTS，可重复执行
-- =========================================================

-- 家长表
CREATE TABLE IF NOT EXISTS parent (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密',
    phone       VARCHAR(20),
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '家长用户表';

-- 儿童表
CREATE TABLE IF NOT EXISTS child (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    nickname         VARCHAR(50)  NOT NULL,
    age              INT,
    avatar           VARCHAR(255),
    parent_id        BIGINT       NOT NULL,
    profile_consent  TINYINT(1)   DEFAULT 0 COMMENT '家长是否授权查看孩子画像',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    CONSTRAINT fk_child_parent FOREIGN KEY (parent_id) REFERENCES parent (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '儿童表';

-- 阶段五：为已存在的 child 表补齐 profile_consent 字段（幂等，列已存在则跳过）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'child' AND COLUMN_NAME = 'profile_consent');
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE child ADD COLUMN profile_consent TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''家长是否授权查看孩子画像''',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 成长相册表
CREATE TABLE IF NOT EXISTS album (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    image_url   VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    child_id    BIGINT       NOT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    CONSTRAINT fk_album_child FOREIGN KEY (child_id) REFERENCES child (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '成长相册表';

-- 儿童日记表
CREATE TABLE IF NOT EXISTS diary (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    content     TEXT         NOT NULL,
    image_url   VARCHAR(255),
    is_anonymous TINYINT(1)  DEFAULT 0 COMMENT '是否匿名',
    child_id    BIGINT       NOT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_child_id (child_id),
    CONSTRAINT fk_diary_child FOREIGN KEY (child_id) REFERENCES child (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '儿童日记表';

-- AI 聊天历史表
CREATE TABLE IF NOT EXISTS ai_chat_history (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    user_role   VARCHAR(20)  NOT NULL COMMENT 'PARENT 或 CHILD',
    question    TEXT         NOT NULL,
    answer      TEXT         NOT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT 'AI 聊天历史表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS message (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    sender_type VARCHAR(20)  NOT NULL COMMENT 'PARENT 或 CHILD',
    sender_id   BIGINT       NOT NULL,
    receiver_id BIGINT       NOT NULL,
    content     TEXT,
    media_url   VARCHAR(255),
    message_type VARCHAR(20) NOT NULL COMMENT 'CHAT / VOICE / PHOTO',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sender_receiver (sender_id, receiver_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '实时聊天消息表';

-- 教育资源表
CREATE TABLE IF NOT EXISTS edu_resource (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    type        VARCHAR(20)  NOT NULL COMMENT 'VIDEO / COURSE / ARTICLE',
    url         VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    cover_url   VARCHAR(255),
    PRIMARY KEY (id),
    KEY idx_type (type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '教育资源表';

-- 学习进度表
CREATE TABLE IF NOT EXISTS learning_progress (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    resource_id      BIGINT      NOT NULL,
    progress_percent INT         NOT NULL DEFAULT 0,
    last_learn_time  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    create_time      DATETIME    DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_resource (user_id, resource_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '学习进度表';

-- 树洞帖子表
CREATE TABLE IF NOT EXISTS treehole_post (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    content        TEXT         NOT NULL,
    image_url      VARCHAR(255),
    author_user_id BIGINT       NOT NULL COMMENT '内部记录发布者，不对外暴露',
    author_role    VARCHAR(20)  NOT NULL COMMENT 'PARENT 或 CHILD',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created_at (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '树洞帖子表';

-- 树洞回复表
CREATE TABLE IF NOT EXISTS treehole_reply (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    content        TEXT         NOT NULL,
    image_url      VARCHAR(255),
    author_user_id BIGINT       NOT NULL,
    author_role    VARCHAR(20)  NOT NULL,
    post_id        BIGINT       NOT NULL,
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_post_id (post_id),
    CONSTRAINT fk_reply_post FOREIGN KEY (post_id) REFERENCES treehole_post (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '树洞回复表';

-- 短期记忆表（阶段二：AI 记忆，评分衰减 + 综合得分检索 + 引用强化）
CREATE TABLE IF NOT EXISTS user_memory (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL COMMENT '记忆主体 ID',
    user_role     VARCHAR(20)  NOT NULL COMMENT 'PARENT 或 CHILD',
    category      VARCHAR(20)  NOT NULL COMMENT 'DIARY/CHAT/ALBUM/TREEHOLE/MARKED',
    content       TEXT         NOT NULL COMMENT '记忆条目（已清洗）',
    emotion       VARCHAR(20)  DEFAULT 'NONE' COMMENT '情感标签 HAPPY/SAD/ANGRY/ANXIOUS/NONE',
    importance    DOUBLE       DEFAULT 0.5 COMMENT '初始重要性',
    last_accessed DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '最近被引用时间',
    level         VARCHAR(10)  DEFAULT 'L2' COMMENT '升级路径 L1/L2/L3',
    status        VARCHAR(20)  DEFAULT 'active' COMMENT 'active/archived',
    source_id     BIGINT       COMMENT '溯源原记录 id',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id, user_role, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '短期记忆表';

-- 长期画像表（阶段三：LLM 把短期记忆压缩成人设画像，L3 核心记忆）
CREATE TABLE IF NOT EXISTS user_profile (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    user_role       VARCHAR(20)  NOT NULL COMMENT 'PARENT 或 CHILD',
    profile_summary TEXT         NOT NULL COMMENT 'LLM 压缩出的长期画像',
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id, user_role)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '长期画像表';

-- 权威文献知识库（阶段四 RAG 通道：只放权威心理/教育/安全指南，绝不存用户个人数据）
CREATE TABLE IF NOT EXISTS literature (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    title      VARCHAR(200) NOT NULL COMMENT '文献/指南标题',
    source     VARCHAR(100) NOT NULL COMMENT '发布机构',
    source_url VARCHAR(500) COMMENT '来源链接',
    category   VARCHAR(50)  NOT NULL COMMENT 'PSYCHOLOGY/EDUCATION/SAFETY/CRISIS/USE_DIGITAL',
    audience   VARCHAR(20)  NOT NULL COMMENT '适用对象 CHILD/PARENT/BOTH',
    summary    TEXT         NOT NULL COMMENT '整理后的要点正文（供注入）',
    keywords   VARCHAR(300) COMMENT '检索关键词（空格分隔）',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '权威文献知识库（RAG 通道）';
