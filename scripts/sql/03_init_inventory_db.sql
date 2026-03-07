-- 创建库存服务数据库
CREATE DATABASE IF NOT EXISTS `seckill_inventory` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `seckill_inventory`;

-- 库存表
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
  `product_id` BIGINT NOT NULL UNIQUE COMMENT '商品ID',
  `total_stock` INT NOT NULL DEFAULT 0 COMMENT '总库存',
  `available_stock` INT NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_stock` INT NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_product (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- 库存变更日志表
DROP TABLE IF EXISTS `inventory_log`;
CREATE TABLE `inventory_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `order_id` BIGINT COMMENT '订单ID',
  `change_quantity` INT NOT NULL COMMENT '变更数量',
  `change_type` TINYINT NOT NULL COMMENT '变更类型:1-扣减,2-回滚,3-增加',
  `before_stock` INT NOT NULL COMMENT '变更前库存',
  `after_stock` INT NOT NULL COMMENT '变更后库存',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_product (`product_id`),
  INDEX idx_order (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更日志表';

-- 插入测试库存数据
INSERT INTO `inventory` (`product_id`, `total_stock`, `available_stock`, `locked_stock`) VALUES
(1, 1000, 1000, 0),
(2, 5000, 5000, 0),
(3, 500, 500, 0);
