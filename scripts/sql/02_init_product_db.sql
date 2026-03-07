-- 创建商品服务数据库
CREATE DATABASE IF NOT EXISTS `seckill_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `seckill_product`;

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `description` TEXT COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `category_id` BIGINT COMMENT '分类ID',
  `image_url` VARCHAR(500) COMMENT '商品图片',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0-下架,1-上架',
  `is_seckill` TINYINT DEFAULT 0 COMMENT '是否秒杀商品:0-否,1-是',
  `seckill_price` DECIMAL(10,2) COMMENT '秒杀价格',
  `seckill_start_time` DATETIME COMMENT '秒杀开始时间',
  `seckill_end_time` DATETIME COMMENT '秒杀结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_category (`category_id`),
  INDEX idx_seckill (`is_seckill`, `seckill_start_time`, `seckill_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 商品分类表
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 插入测试数据
INSERT INTO `product_category` (`name`, `parent_id`, `sort`) VALUES
('电子产品', 0, 1),
('服装鞋帽', 0, 2),
('食品饮料', 0, 3);

INSERT INTO `product` (`name`, `description`, `price`, `category_id`, `status`, `is_seckill`, `seckill_price`, `seckill_start_time`, `seckill_end_time`) VALUES
('iPhone 15 Pro', '苹果最新旗舰手机', 7999.00, 1, 1, 1, 6999.00, '2026-03-08 10:00:00', '2026-03-08 12:00:00'),
('小米13', '小米旗舰手机', 3999.00, 1, 1, 0, NULL, NULL, NULL),
('Nike运动鞋', '舒适运动鞋', 599.00, 2, 1, 1, 399.00, '2026-03-08 14:00:00', '2026-03-08 16:00:00');
