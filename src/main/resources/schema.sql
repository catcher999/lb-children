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
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nickname    VARCHAR(50)  NOT NULL,
    age         INT,
    avatar      VARCHAR(255),
    parent_id   BIGINT       NOT NULL,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    CONSTRAINT fk_child_parent FOREIGN KEY (parent_id) REFERENCES parent (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '儿童表';

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
