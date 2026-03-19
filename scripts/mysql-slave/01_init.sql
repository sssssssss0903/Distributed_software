-- 从库初始化：创建数据库和表结构（与主库相同，复制会同步数据）
-- 若主从复制已配置，从库会从主库同步，此脚本仅作首次建库用
CREATE DATABASE IF NOT EXISTS `seckill_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `seckill_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `seckill_inventory` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `seckill_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
