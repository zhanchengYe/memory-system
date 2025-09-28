package com.memorymain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ImageAlbumVo {
    private Long albumId;
    private List<Long> imageIds;
}
