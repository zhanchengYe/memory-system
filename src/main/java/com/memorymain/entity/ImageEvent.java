package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("image_event")
public class ImageEvent {
    @TableId(type = IdType.AUTO)
    private Long eiId;
    private Long imageId;
    private Long evId;
}
