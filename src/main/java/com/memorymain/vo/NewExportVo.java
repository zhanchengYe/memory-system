package com.memorymain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.date.DateStringConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class NewExportVo {
    @ExcelProperty(value = "公告序号")
    private Long newId;
    @ExcelProperty(value = "标题")
    private String title;
    @ExcelProperty(value = "内容")
    private String content;
    @ExcelProperty(value = "图片地址")
    private String picture;
    @ExcelProperty(value = "创建日期",converter = DateStringConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
