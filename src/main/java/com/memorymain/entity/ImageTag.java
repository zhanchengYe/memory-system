package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
@Data
@TableName("image_tag")
public class ImageTag{
    @TableId(type = IdType.AUTO)
    private Long imtId;
    private Long imageId;
    private Long tagId;
}
