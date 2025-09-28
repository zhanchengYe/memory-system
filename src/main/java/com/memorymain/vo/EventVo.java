package com.memorymain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class EventVo {
    private Long evId;
    private String name;
    private String background;
    private List<ImageVo> imageVoList;
    private List<Long> imageIds;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private Long createBy;
    private String createByName;
    private Boolean isSelf;
}
