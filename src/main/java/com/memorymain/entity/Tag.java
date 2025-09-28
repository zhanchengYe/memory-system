package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long tagId;
    @NotBlank(message = "标签名字不能为空")
    private String name;
    private Long createBy;
}
