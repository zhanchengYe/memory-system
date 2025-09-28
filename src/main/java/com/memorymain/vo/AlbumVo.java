package com.memorymain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class AlbumVo {
    private Long albumId;
    private String albumName;
    private String albumDesc;
    private String albumCover;
    private Long createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private Integer imageCount;
}
