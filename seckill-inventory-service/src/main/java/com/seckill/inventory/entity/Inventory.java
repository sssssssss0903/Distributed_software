package com.seckill.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Integer totalStock;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
