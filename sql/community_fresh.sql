-- =====================================================================
-- 社区便民生鲜配送平台 数据库初始化脚本
-- MySQL 8.0+ / utf8mb4 / InnoDB
-- =====================================================================

-- 强制本连接字符集为 utf8mb4：官方镜像 init 入口以 latin1 连接导入，
-- 不加此句中文会被双重编码成乱码
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS community_fresh DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE community_fresh;

-- =====================================================================

-- 1. C端用户表（含团长角色）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(32)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `name`        VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
  `phone`       VARCHAR(11)  DEFAULT NULL COMMENT '手机号',
  `sex`         VARCHAR(2)   DEFAULT NULL COMMENT '性别 0女 1男',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `role`        TINYINT      NOT NULL DEFAULT 1 COMMENT '角色 1普通用户 2团长',
  `status`      INT          NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` DATETIME     DEFAULT NULL COMMENT '注册时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'C端用户表';

-- 2. 员工/管理员表
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(32)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `name`        VARCHAR(32)  NOT NULL COMMENT '姓名',
  `phone`       VARCHAR(11)  NOT NULL COMMENT '手机号',
  `sex`         VARCHAR(2)   DEFAULT NULL COMMENT '性别',
  `id_number`   VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
  `status`      INT          NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT NULL COMMENT '修改时间',
  `create_user` BIGINT       DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT       DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_username` (`username`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '员工/管理员表';

-- 3. 分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(32) NOT NULL COMMENT '分类名称',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序(升序)',
  `status`      INT         NOT NULL DEFAULT 1 COMMENT '状态 1启用 0禁用',
  `create_time` DATETIME    DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME    DEFAULT NULL COMMENT '修改时间',
  `create_user` BIGINT      DEFAULT NULL COMMENT '创建人',
  `update_user` BIGINT      DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- 4. 生鲜商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`            VARCHAR(64)  NOT NULL COMMENT '商品名称',
  `category_id`     BIGINT       NOT NULL COMMENT '分类id',
  `price`           DECIMAL(10,2) NOT NULL COMMENT '单价(每份/每斤/每kg)',
  `price_type`      TINYINT      NOT NULL DEFAULT 1 COMMENT '计价方式 1按份 2按重量',
  `unit`            VARCHAR(8)   NOT NULL DEFAULT '份' COMMENT '计价单位: 份/斤/kg',
  `fresh_type`      TINYINT      NOT NULL DEFAULT 0 COMMENT '保鲜类型 0常温 1冷藏 2冷冻',
  `shelf_life_days` INT          DEFAULT NULL COMMENT '保质期(天)',
  `stock`           INT          NOT NULL DEFAULT 0 COMMENT '总库存',
  `warn_stock`      INT          NOT NULL DEFAULT 0 COMMENT '预警库存',
  `origin_place`    VARCHAR(128) DEFAULT NULL COMMENT '产地',
  `trace_code`      VARCHAR(64)  DEFAULT NULL COMMENT '溯源码',
  `image`           VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `description`     VARCHAR(512) DEFAULT NULL COMMENT '商品描述',
  `is_presale`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否预售 0否 1是',
  `sale_count`      INT          NOT NULL DEFAULT 0 COMMENT '销量(展示用)',
  `status`          INT          NOT NULL DEFAULT 1 COMMENT '状态 1起售 0停售',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '修改时间',
  `create_user`     BIGINT       DEFAULT NULL COMMENT '创建人',
  `update_user`     BIGINT       DEFAULT NULL COMMENT '修改人',
  PRIMARY KEY (`id`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_status` (`status`),
  KEY `idx_product_warn` (`stock`, `warn_stock`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '生鲜商品表';

-- 5. 购物车表
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(64)  DEFAULT NULL COMMENT '商品名称(冗余)',
  `image`       VARCHAR(255) DEFAULT NULL COMMENT '商品图片(冗余)',
  `user_id`     BIGINT       NOT NULL COMMENT '用户id',
  `product_id`  BIGINT       NOT NULL COMMENT '商品id',
  `weight`      DECIMAL(10,3) DEFAULT NULL COMMENT '称重重量(按重量商品)',
  `number`      INT          NOT NULL DEFAULT 1 COMMENT '份数(按份商品)',
  `amount`      DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME     DEFAULT NULL COMMENT '加入时间',
  PRIMARY KEY (`id`),
  KEY `idx_cart_user` (`user_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '购物车表';

-- 6. 地址簿表
DROP TABLE IF EXISTS `address_book`;
CREATE TABLE `address_book` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT      NOT NULL COMMENT '用户id',
  `consignee`     VARCHAR(32) NOT NULL COMMENT '收货人',
  `sex`           VARCHAR(2)  DEFAULT NULL COMMENT '性别',
  `phone`         VARCHAR(11) NOT NULL COMMENT '手机号',
  `province_code` VARCHAR(12) DEFAULT NULL COMMENT '省级区划编号',
  `province_name` VARCHAR(32) DEFAULT NULL COMMENT '省级名称',
  `city_code`     VARCHAR(12) DEFAULT NULL COMMENT '市级区划编号',
  `city_name`     VARCHAR(32) DEFAULT NULL COMMENT '市级名称',
  `district_code` VARCHAR(12) DEFAULT NULL COMMENT '区级区划编号',
  `district_name` VARCHAR(32) DEFAULT NULL COMMENT '区级名称',
  `detail`        VARCHAR(200) NOT NULL COMMENT '详细地址',
  `label`         VARCHAR(20) DEFAULT NULL COMMENT '标签: 家/公司/学校',
  `is_default`    TINYINT     NOT NULL DEFAULT 0 COMMENT '是否默认 1是 0否',
  PRIMARY KEY (`id`),
  KEY `idx_address_user` (`user_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '地址簿表';

-- 7. 订单表（扩展履约方式/订单类型/自提字段）
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `number`            VARCHAR(50)  DEFAULT NULL COMMENT '订单号',
  `status`            TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1待付款 2待接单 3备货中/待配送 4配送中/待自提 5已完成 6已取消',
  `user_id`           BIGINT       NOT NULL COMMENT '下单用户id',
  `order_type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '订单类型 1普通 2预售 3团购',
  `delivery_type`     TINYINT      NOT NULL DEFAULT 1 COMMENT '履约方式 1配送到家 2自提',
  `address_book_id`   BIGINT       DEFAULT NULL COMMENT '地址id(配送单)',
  `pickup_point_id`   BIGINT       DEFAULT NULL COMMENT '自提点id(自提单)',
  `time_slot_id`      BIGINT       DEFAULT NULL COMMENT '自提时间段id',
  `pickup_code`       VARCHAR(8)   DEFAULT NULL COMMENT '自提码(6位数字)',
  `pickup_time`       DATETIME     DEFAULT NULL COMMENT '核销时间',
  `presale_id`        BIGINT       DEFAULT NULL COMMENT '预售活动id',
  `group_activity_id` BIGINT       DEFAULT NULL COMMENT '团购活动id',
  `order_time`        DATETIME     DEFAULT NULL COMMENT '下单时间',
  `checkout_time`     DATETIME     DEFAULT NULL COMMENT '支付时间',
  `pay_method`        TINYINT      DEFAULT NULL COMMENT '支付方式 1微信 2支付宝',
  `pay_status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '支付状态 0未支付 1已支付 2已退款',
  `amount`            DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `pack_amount`       DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '打包费',
  `remark`            VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `user_name`         VARCHAR(32)  DEFAULT NULL COMMENT '下单人昵称(冗余)',
  `phone`             VARCHAR(11)  DEFAULT NULL COMMENT '联系手机号',
  `address`           VARCHAR(255) DEFAULT NULL COMMENT '收货地址/自提点地址(冗余)',
  `consignee`         VARCHAR(32)  DEFAULT NULL COMMENT '收货人',
  `cancel_reason`     VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
  `rejection_reason`  VARCHAR(255) DEFAULT NULL COMMENT '拒单原因',
  `cancel_time`       DATETIME     DEFAULT NULL COMMENT '取消时间',
  `estimated_delivery_time` DATETIME DEFAULT NULL COMMENT '预计送达/可自提时间',
  `delivery_time`     DATETIME     DEFAULT NULL COMMENT '送达/完成时间',
  `create_time`       DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`       DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_orders_number` (`number`),
  KEY `idx_orders_user` (`user_id`),
  KEY `idx_orders_status` (`status`),
  KEY `idx_orders_pickup` (`pickup_point_id`, `status`),
  KEY `idx_orders_pickup_code` (`pickup_code`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

-- 8. 订单明细表
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`       VARCHAR(64)  DEFAULT NULL COMMENT '商品名称(冗余)',
  `image`      VARCHAR(255) DEFAULT NULL COMMENT '商品图片(冗余)',
  `order_id`   BIGINT       NOT NULL COMMENT '订单id',
  `product_id` BIGINT       NOT NULL COMMENT '商品id',
  `batch_id`   BIGINT       DEFAULT NULL COMMENT '扣减批次id',
  `weight`     DECIMAL(10,3) DEFAULT NULL COMMENT '称重重量(按重量商品)',
  `number`     INT          NOT NULL DEFAULT 1 COMMENT '份数',
  `amount`     DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_detail_order` (`order_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细表';

-- 9. 商品评价表
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`    BIGINT  NOT NULL COMMENT '订单id',
  `user_id`     BIGINT  NOT NULL COMMENT '用户id',
  `product_id`  BIGINT  NOT NULL COMMENT '商品id',
  `score`       TINYINT NOT NULL COMMENT '评分 1-5',
  `content`     VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `images`      VARCHAR(1000) DEFAULT NULL COMMENT '评价图片(JSON数组)',
  `create_time` DATETIME DEFAULT NULL COMMENT '评价时间',
  PRIMARY KEY (`id`),
  KEY `idx_eval_product` (`product_id`),
  KEY `idx_eval_order` (`order_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '商品评价表';

-- 10. 消息通知表（自提/成团通知模拟）
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT      NOT NULL COMMENT '接收用户id',
  `type`        TINYINT     NOT NULL COMMENT '类型 1自提通知 2成团通知 3流团退款通知 4系统通知',
  `title`       VARCHAR(100) NOT NULL COMMENT '标题',
  `content`     VARCHAR(500) DEFAULT NULL COMMENT '内容',
  `is_read`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否已读 1是 0否',
  `create_time` DATETIME    DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_notice_user` (`user_id`, `is_read`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '消息通知表';

-- =====================================================================
-- 二、生鲜特色新增表
-- =====================================================================

-- 11. 商品批次表（保质期/临期/损耗核心）
DROP TABLE IF EXISTS `product_batch`;
CREATE TABLE `product_batch` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`      BIGINT      NOT NULL COMMENT '商品id',
  `batch_no`        VARCHAR(32) NOT NULL COMMENT '批次号',
  `production_date` DATE        NOT NULL COMMENT '生产日期',
  `expiry_date`     DATE        NOT NULL COMMENT '到期日期',
  `stock`           INT         NOT NULL DEFAULT 0 COMMENT '批次剩余库存',
  `init_stock`      INT         NOT NULL DEFAULT 0 COMMENT '批次初始库存',
  `discount`        DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣(临期自动设置, 如0.70=7折)',
  `status`          TINYINT     NOT NULL DEFAULT 1 COMMENT '状态 1正常 2临期 3过期',
  `create_time`     DATETIME    DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME    DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_no` (`batch_no`),
  KEY `idx_batch_product` (`product_id`, `status`),
  KEY `idx_batch_expiry` (`expiry_date`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '商品批次表';

-- 12. 商品溯源表
DROP TABLE IF EXISTS `product_trace`;
CREATE TABLE `product_trace` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`     BIGINT       NOT NULL COMMENT '商品id',
  `origin_place`   VARCHAR(128) DEFAULT NULL COMMENT '产地',
  `farmer`         VARCHAR(64)  DEFAULT NULL COMMENT '种植户/养殖户',
  `plant_date`     DATE         DEFAULT NULL COMMENT '种植日期',
  `pick_date`      DATE         DEFAULT NULL COMMENT '采摘/出栏日期',
  `test_report`    VARCHAR(255) DEFAULT NULL COMMENT '检测报告(图片url)',
  `transport_info` VARCHAR(500) DEFAULT NULL COMMENT '运输/冷链信息',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trace_product` (`product_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '商品溯源表';

-- 13. 社区自提点表
DROP TABLE IF EXISTS `pickup_point`;
CREATE TABLE `pickup_point` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`           VARCHAR(64)  NOT NULL COMMENT '自提点名称',
  `leader_id`      BIGINT       DEFAULT NULL COMMENT '绑定团长(user.id)',
  `province_name`  VARCHAR(32)  DEFAULT NULL COMMENT '省',
  `city_name`      VARCHAR(32)  DEFAULT NULL COMMENT '市',
  `district_name`  VARCHAR(32)  DEFAULT NULL COMMENT '区',
  `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `lng`            DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
  `lat`            DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `phone`          VARCHAR(11)  DEFAULT NULL COMMENT '联系电话',
  `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间描述',
  `status`         INT          NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  `create_time`    DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_pickup_leader` (`leader_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '社区自提点表';

-- 14. 自提时间段表
DROP TABLE IF EXISTS `pickup_time_slot`;
CREATE TABLE `pickup_time_slot` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `point_id`    BIGINT   NOT NULL COMMENT '自提点id',
  `start_time`  TIME     NOT NULL COMMENT '开始时间',
  `end_time`    TIME     NOT NULL COMMENT '结束时间',
  `max_orders`  INT      NOT NULL DEFAULT 50 COMMENT '该时段最大订单数',
  `used_orders` INT      NOT NULL DEFAULT 0 COMMENT '已预约订单数',
  `status`      INT      NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  PRIMARY KEY (`id`),
  KEY `idx_slot_point` (`point_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '自提时间段表';

-- 15. 预售活动表
DROP TABLE IF EXISTS `presale_activity`;
CREATE TABLE `presale_activity` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`    BIGINT       NOT NULL COMMENT '预售商品id',
  `presale_price` DECIMAL(10,2) NOT NULL COMMENT '预售价',
  `stock_limit`   INT          NOT NULL COMMENT '预售限购库存',
  `sold_count`    INT          NOT NULL DEFAULT 0 COMMENT '已售数量',
  `start_time`    DATETIME     NOT NULL COMMENT '活动开始时间',
  `end_time`      DATETIME     NOT NULL COMMENT '下单截止时间',
  `delivery_date` DATE         NOT NULL COMMENT '统一发货日期',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0未开始 1进行中 2已结束',
  `create_time`   DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_presale_product` (`product_id`),
  KEY `idx_presale_status` (`status`, `end_time`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '预售活动表';

-- 16. 团购活动表
DROP TABLE IF EXISTS `group_buy_activity`;
CREATE TABLE `group_buy_activity` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id`      BIGINT       NOT NULL COMMENT '团购商品id',
  `leader_id`       BIGINT       NOT NULL COMMENT '发起团长(user.id)',
  `pickup_point_id` BIGINT       NOT NULL COMMENT '统一配送自提点',
  `group_price`     DECIMAL(10,2) NOT NULL COMMENT '团购价',
  `min_num`         INT          NOT NULL COMMENT '成团人数',
  `joined_num`      INT          NOT NULL DEFAULT 0 COMMENT '已参团人数',
  `start_time`      DATETIME     NOT NULL COMMENT '开始时间',
  `end_time`        DATETIME     NOT NULL COMMENT '截止时间',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0未开始 1进行中 2已成团 3已完成 4已流团',
  `create_time`     DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`     DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_leader` (`leader_id`),
  KEY `idx_group_status` (`status`, `end_time`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '团购活动表';

-- 17. 参团记录表
DROP TABLE IF EXISTS `group_buy_member`;
CREATE TABLE `group_buy_member` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_id` BIGINT   NOT NULL COMMENT '团购活动id',
  `user_id`     BIGINT   NOT NULL COMMENT '参团用户id',
  `order_id`    BIGINT   NOT NULL COMMENT '关联订单id',
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态 1已支付 2已退款',
  `create_time` DATETIME DEFAULT NULL COMMENT '参团时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_activity_user` (`activity_id`, `user_id`),
  KEY `idx_member_user` (`user_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '参团记录表';

-- 18. 社区帖子表
DROP TABLE IF EXISTS `community_post`;
CREATE TABLE `community_post` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT       NOT NULL COMMENT '发布用户id',
  `type`          TINYINT      NOT NULL DEFAULT 1 COMMENT '类型 1团购分享 2好物分享',
  `title`         VARCHAR(100) NOT NULL COMMENT '标题',
  `content`       TEXT         COMMENT '内容',
  `images`        VARCHAR(1000) DEFAULT NULL COMMENT '图片(JSON数组)',
  `like_count`    INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT          NOT NULL DEFAULT 0 COMMENT '评论数',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '审核状态 0待审核 1已通过 2已拒绝',
  `audit_remark`  VARCHAR(255) DEFAULT NULL COMMENT '审核意见',
  `audit_time`    DATETIME     DEFAULT NULL COMMENT '审核时间',
  `auditor_id`    BIGINT       DEFAULT NULL COMMENT '审核人(employee.id)',
  `create_time`   DATETIME     DEFAULT NULL COMMENT '发布时间',
  `update_time`   DATETIME     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_status` (`status`, `type`),
  KEY `idx_post_user` (`user_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '社区帖子表';

-- 19. 帖子评论表
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id`     BIGINT       NOT NULL COMMENT '帖子id',
  `user_id`     BIGINT       NOT NULL COMMENT '评论用户id',
  `parent_id`   BIGINT       DEFAULT NULL COMMENT '父评论id(楼中楼)',
  `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1正常 0删除',
  `create_time` DATETIME     DEFAULT NULL COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_comment_post` (`post_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '帖子评论表';

-- 20. 帖子点赞表
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `post_id`     BIGINT   NOT NULL COMMENT '帖子id',
  `user_id`     BIGINT   NOT NULL COMMENT '点赞用户id',
  `create_time` DATETIME DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like_post_user` (`post_id`, `user_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '帖子点赞表';

-- =====================================================================
-- 三、初始化数据
-- =====================================================================

-- 管理员账号 admin / 123456 (BCrypt)
INSERT INTO `employee` (`username`, `password`, `name`, `phone`, `sex`, `id_number`, `status`, `create_time`)
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '管理员', '13800000000', '1', '110101199001010000', 1, NOW());

-- 演示用户 user1 / 123456，团长 leader1 / 123456 (BCrypt)
INSERT INTO `user` (`username`, `password`, `name`, `phone`, `role`, `status`, `create_time`) VALUES
('user1',   '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '普通用户', '13900000001', 1, 1, NOW()),
('leader1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '团长一号', '13900000002', 2, 1, NOW());

-- 演示分类
INSERT INTO `category` (`name`, `sort`, `status`, `create_time`) VALUES
('新鲜蔬菜', 1, 1, NOW()),
('时令水果', 2, 1, NOW()),
('肉禽蛋品', 3, 1, NOW()),
('水产海鲜', 4, 1, NOW()),
('粮油副食', 5, 1, NOW());

-- 演示自提点 + 时间段
INSERT INTO `pickup_point` (`name`, `leader_id`, `province_name`, `city_name`, `district_name`, `detail_address`, `lng`, `lat`, `phone`, `business_hours`, `status`, `create_time`) VALUES
('幸福里社区自提点', 2, '浙江省', '杭州市', '西湖区', '幸福里小区北门便利店', 120.123456, 30.123456, '13900000002', '每天 08:00-20:00', 1, NOW());
INSERT INTO `pickup_time_slot` (`point_id`, `start_time`, `end_time`, `max_orders`) VALUES
(1, '09:00:00', '12:00:00', 50),
(1, '14:00:00', '18:00:00', 80);
