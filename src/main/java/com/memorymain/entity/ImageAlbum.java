package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("image_album")
public class ImageAlbum {
    @TableId(type = IdType.AUTO)
    private Long iaId;
    private Long imageId;
    private Long albumId;
}
