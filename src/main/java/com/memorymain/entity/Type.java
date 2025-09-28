package com.memorymain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@TableName("type")
public class Type {
    @TableId(type = IdType.AUTO)
    private Long typeId;
    @NotBlank(message = "分类名字不能为空")
    private String name;
    private Long createBy;
}
