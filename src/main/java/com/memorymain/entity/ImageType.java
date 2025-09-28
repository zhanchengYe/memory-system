package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("image_type")
public class ImageType {
    @TableId(type = IdType.AUTO)
    private Long itId;
    private Long imageId;
    private Long typeId;
}
