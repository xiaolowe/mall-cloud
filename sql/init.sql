-- Mall Cloud local/dev schema. Each microservice owns its own database.
-- Safe to re-run: tables use IF NOT EXISTS and seed rows use INSERT IGNORE.

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS mall_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mall_product DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mall_inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mall_order DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS mall_payment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ---------------------------------------------------------------- user
USE mall_user;

CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(64)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    nickname   VARCHAR(64)  DEFAULT NULL,
    phone      VARCHAR(20)  DEFAULT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS user_address (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    receiver_name  VARCHAR(64)  NOT NULL,
    receiver_phone VARCHAR(20)  NOT NULL,
    detail         VARCHAR(255) NOT NULL,
    is_default     TINYINT      NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Built-in demo account: demo / 123456
INSERT IGNORE INTO users (username, password, nickname, phone) VALUES
    ('demo', '$2a$10$hmiIwQQO45snxl89bY001.i8Z1SYT94C2cF253yJS.5wcMUkvmnR6', '演示用户', '13800000000');

-- ---------------------------------------------------------------- product
USE mall_product;

CREATE TABLE IF NOT EXISTS category (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    sort INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    category_id BIGINT        NOT NULL,
    name        VARCHAR(128)  NOT NULL,
    subtitle    VARCHAR(255)  DEFAULT NULL,
    cover_url   VARCHAR(512)  DEFAULT NULL,
    description TEXT,
    price       DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status      TINYINT       NOT NULL DEFAULT 1,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category_id (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS product_sku (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    product_id BIGINT         NOT NULL,
    title      VARCHAR(128)   NOT NULL,
    price      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    spec_json  VARCHAR(255)   DEFAULT NULL,
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_product_id (product_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO category (id, name, sort) VALUES
    (1, '手机数码', 1),
    (2, '电脑办公', 2),
    (3, '家用电器', 3);

INSERT IGNORE INTO product (id, category_id, name, subtitle, cover_url, description, price, status) VALUES
    (1, 1, '星辰 X1 5G 手机', '6.7 英寸 OLED · 5000mAh · 256GB', 'https://picsum.photos/seed/phone/640/420', '旗舰影像系统，支持 120Hz 高刷与 66W 快充。', 3999.00, 1),
    (2, 2, '极光 Air 轻薄本', '14 英寸 2.8K · 32GB · 1TB', 'https://picsum.photos/seed/laptop/640/420', '全金属机身，1.2kg 轻量设计，适合移动办公。', 6999.00, 1),
    (3, 1, '静音降噪耳机 Pro', '主动降噪 · 40 小时续航', 'https://picsum.photos/seed/headphone/640/420', '双设备连接，通透模式，佩戴舒适。', 899.00, 1),
    (4, 1, '智能手表 S2', '血氧监测 · 独立通话', 'https://picsum.photos/seed/watch/640/420', '全天候健康监测，支持 100+ 运动模式。', 1299.00, 1),
    (5, 3, '家用净水器 800G', '大通量 · 低废水比', 'https://picsum.photos/seed/purifier/640/420', '五级过滤，即滤即饮，智能滤芯提醒。', 2199.00, 1),
    (6, 2, '4K 显示器 27 英寸', 'IPS · 98% DCI-P3', 'https://picsum.photos/seed/monitor/640/420', '专业色彩表现，Type-C 90W 反向供电。', 1899.00, 1);

INSERT IGNORE INTO product_sku (id, product_id, title, price, spec_json) VALUES
    (1, 1, '星辰 X1 8GB+256GB 曜石黑', 3999.00, '{"color":"曜石黑","storage":"256GB"}'),
    (2, 1, '星辰 X1 12GB+512GB 冰川蓝', 4599.00, '{"color":"冰川蓝","storage":"512GB"}'),
    (3, 2, '极光 Air 32GB+1TB 银色', 6999.00, '{"color":"银色","storage":"1TB"}'),
    (4, 2, '极光 Air 16GB+512GB 深空灰', 5999.00, '{"color":"深空灰","storage":"512GB"}'),
    (5, 3, '静音降噪耳机 Pro 黑色', 899.00, '{"color":"黑色"}'),
    (6, 3, '静音降噪耳机 Pro 白色', 899.00, '{"color":"白色"}'),
    (7, 4, '智能手表 S2 46mm 曜黑', 1299.00, '{"size":"46mm","color":"曜黑"}'),
    (8, 4, '智能手表 S2 42mm 玫瑰金', 1199.00, '{"size":"42mm","color":"玫瑰金"}'),
    (9, 5, '家用净水器 800G 标准版', 2199.00, '{"version":"标准版"}'),
    (10, 6, '4K 显示器 27 英寸', 1899.00, '{"size":"27英寸","resolution":"4K"}');

-- ---------------------------------------------------------------- inventory
USE mall_inventory;

CREATE TABLE IF NOT EXISTS stock (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    sku_id     BIGINT   NOT NULL,
    total      INT      NOT NULL DEFAULT 0,
    available  INT      NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_id (sku_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS stock_flow (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    order_no   VARCHAR(32) NOT NULL,
    sku_id     BIGINT      NOT NULL,
    quantity   INT         NOT NULL,
    type       VARCHAR(16) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_sku_type (order_no, sku_id, type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO stock (sku_id, total, available) VALUES
    (1, 100, 100), (2, 100, 100), (3, 100, 100), (4, 100, 100), (5, 100, 100),
    (6, 100, 100), (7, 100, 100), (8, 100, 100), (9, 100, 100), (10, 100, 100);

-- ---------------------------------------------------------------- order
USE mall_order;

CREATE TABLE IF NOT EXISTS cart_item (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL,
    sku_id     BIGINT   NOT NULL,
    quantity   INT      NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sku (user_id, sku_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    order_no         VARCHAR(32)    NOT NULL,
    user_id          BIGINT         NOT NULL,
    total_amount     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status           TINYINT        NOT NULL DEFAULT 0,
    receiver_name    VARCHAR(64)    NOT NULL,
    receiver_phone   VARCHAR(20)    NOT NULL,
    receiver_address VARCHAR(255)   NOT NULL,
    cancel_reason    VARCHAR(128)   DEFAULT NULL,
    created_at       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at          DATETIME       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_created (user_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    order_id     BIGINT         NOT NULL,
    sku_id       BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(128)   DEFAULT NULL,
    sku_title    VARCHAR(128)   DEFAULT NULL,
    cover_url    VARCHAR(512)   DEFAULT NULL,
    price        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    quantity     INT            NOT NULL,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------- payment
USE mall_payment;

CREATE TABLE IF NOT EXISTS payment (
    id         BIGINT         NOT NULL AUTO_INCREMENT,
    payment_no VARCHAR(32)    NOT NULL,
    order_no   VARCHAR(32)    NOT NULL,
    user_id    BIGINT         NOT NULL,
    amount     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    channel    VARCHAR(32)    NOT NULL DEFAULT 'MOCK',
    status     TINYINT        NOT NULL DEFAULT 0,
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at    DATETIME       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_no (order_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
