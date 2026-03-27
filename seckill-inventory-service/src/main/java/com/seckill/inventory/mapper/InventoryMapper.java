package com.seckill.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Update("UPDATE inventory SET available_stock = available_stock - #{quantity}, version = version + 1 " +
            "WHERE product_id = #{productId} AND available_stock >= #{quantity}")
    int deduct(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
