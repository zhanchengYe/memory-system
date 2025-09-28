package com.memorymain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.date.DateStringConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.memorymain.util.ExcelDictFormat;
import lombok.Data;

import java.util.Date;

@Data
public class SysUserExportVo {
    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户序号")
    private Long userId;

    /**
     * 用户账号
     */
    @ExcelProperty(value = "登录名称")
    private String username;

    /**
     * 用户邮箱
     */
    @ExcelProperty(value = "用户邮箱")
    private String email;

    /**
     * 手机号码
     */
    @ExcelProperty(value = "手机号码")
    private String phone;

    /**
     * 用户性别
     */
    @ExcelProperty(value = "用户性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=男,1=女")
    private String sex;

    @ExcelProperty(value = "生日日期",converter = DateStringConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;

    @ExcelProperty(value = "头像地址")
    private String avatar;

    /**
     * 帐号状态（0正常 1停用）
     */
    @ExcelProperty(value = "帐号状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=正常,1=禁用")
    private String status;
}
