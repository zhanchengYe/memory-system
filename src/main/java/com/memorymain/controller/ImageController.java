package com.memorymain.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.entity.*;
import com.memorymain.mapper.*;
import com.memorymain.util.*;
import com.memorymain.vo.Chart;
import com.memorymain.vo.ImageAlbumVo;
import com.memorymain.vo.ImageVo;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/image")
public class ImageController {
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private ImageTypeMapper imageTypeMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private ImageTagMapper imageTagMapper;
    @Resource
    private ImageAlbumMapper imageAlbumMapper;
    @Resource
    private AlbumMapper albumMapper;
    @Resource
    private ImageEventMapper imageEventMapper;
    @Resource
    private EventMapper eventMapper;

    // 根据编号获取详细信息
    @GetMapping(value = {"/", "/{imageId}"})
    public R<?> getInfo(@PathVariable(value = "imageId", required = false) Long imageId) {
        Image image = imageMapper.selectById(imageId);
        if(StringUtils.isNull(image))
            return R.fail("照片不存在");
        return R.ok(getImageVo(image));
    }

    private ImageVo getImageVo(Image image) {
        ImageVo vo = new ImageVo();
        BeanMapper.copy(image,vo);
        ImageAlbum imageAlbum = imageAlbumMapper.selectOne(new LambdaQueryWrapper<ImageAlbum>().eq(ImageAlbum::getImageId, image.getImageId())
                .last("limit 1"));
        if(StringUtils.isNotNull(imageAlbum)){
            Album album = albumMapper.selectById(imageAlbum.getAlbumId());
            if(StringUtils.isNotNull(album)){
                vo.setAlbumId(album.getAlbumId());
                vo.setAlbumName(album.getAlbumName());
            }
        }
        ImageType imageType = imageTypeMapper.selectOne(new LambdaQueryWrapper<ImageType>()
                .eq(ImageType::getImageId, image.getImageId()).last("limit 1"));
        if(StringUtils.isNotNull(imageType)){
            Type type = typeMapper.selectById(imageType.getTypeId());
            if(StringUtils.isNotNull(type)){
                vo.setTypeId(type.getTypeId());
                vo.setTypeName(type.getName());
            }
        }
        List<ImageTag> imageTagList = imageTagMapper.selectList(new LambdaQueryWrapper<ImageTag>()
                .eq(ImageTag::getImageId, image.getImageId()));
        List<String> tagNameList = new ArrayList<>();
        if(StringUtils.isNotEmpty(imageTagList)){
            List<Long> tagIds = imageTagList.stream().map(ImageTag::getTagId).collect(Collectors.toList());
            List<Tag> tagList = tagMapper.selectList(new LambdaQueryWrapper<Tag>().in(Tag::getTagId, tagIds));
            tagNameList = tagList.stream().map(Tag::getName).collect(Collectors.toList());
        }
        vo.setTagNameList(tagNameList);
        return vo;
    }

    // 获取分页列表
    @GetMapping("/list")
    @Transactional
    public R<?> list(ImageVo image, int pageNum, int pageSize) {
        ArrayList<ImageVo> voList = new ArrayList<>();
        List<Long> imageTypeIds = new ArrayList<>();
        List<Long> imageTagIds = new ArrayList<>();
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        Long userId = LoginHelper.getUserId();
        image.setCreateBy(userId);
        List<Long> imageIds = new ArrayList<>();
        Boolean isReturnEmpty = true;
        Boolean isSearch = false;
        if(StringUtils.isNotNull(image.getAlbumId())){
            isSearch = true;
            List<ImageAlbum> imageAlbumList = imageAlbumMapper.selectList(new LambdaQueryWrapper<ImageAlbum>()
                    .eq(ImageAlbum::getAlbumId, image.getAlbumId()));
            if(StringUtils.isNotEmpty(imageAlbumList)){
                imageIds = imageAlbumList.stream().map(ImageAlbum::getImageId).collect(Collectors.toList());
                isReturnEmpty = false;
            }
        }
        if(StringUtils.isNotNull(image.getTypeName())){
            isSearch = true;
            List<Type> types = typeMapper.selectList(new LambdaQueryWrapper<Type>().like(Type::getName, image.getTypeName()));
            if(StringUtils.isNotEmpty(types)){
                List<Long> typeIds = types.stream().map(Type::getTypeId).collect(Collectors.toList());
                List<ImageType> imageTypes = imageTypeMapper.selectList(new LambdaQueryWrapper<ImageType>()
                        .in(ImageType::getTypeId, typeIds));
                if(StringUtils.isNotEmpty(imageTypes)){
                    imageTypeIds = imageTypes.stream().map(ImageType::getImageId).collect(Collectors.toList());
                    isReturnEmpty = false;
                }
            }

        }
        if(StringUtils.isNotNull(image.getTagName())){
            isSearch = true;
            List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>().like(Tag::getName, image.getTagName()));
            if(StringUtils.isNotEmpty(tags)){
                List<Long> tagIds = tags.stream().map(Tag::getTagId).collect(Collectors.toList());
                List<ImageTag> imageTags = imageTagMapper.selectList(new LambdaQueryWrapper<ImageTag>()
                        .in(ImageTag::getTagId, tagIds));
                if(StringUtils.isNotEmpty(imageTags)){
                    imageTagIds = imageTags.stream().map(ImageTag::getImageId).collect(Collectors.toList());
                    isReturnEmpty = false;
                }
            }
        }
        if(StringUtils.isNotNull(image.getIsDelete())){
            if(Constants.EXCEPTION.equals(image.getIsDelete())){
                imageMapper.delete(new LambdaQueryWrapper<Image>()
                        .isNotNull(Image::getExpiredTime)
                        .lt(Image::getExpiredTime,new Date()));
            }
        }
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<Image> wrapper = new LambdaQueryWrapper<Image>()
                .like(StringUtils.isNotBlank(image.getImageDesc()), Image::getImageDesc, image.getImageDesc())
                .eq(Image::getCreateBy,image.getCreateBy())
                .in(StringUtils.isNotNull(image.getAlbumId())&&StringUtils.isNotEmpty(imageIds),Image::getImageId,imageIds)
                .in(StringUtils.isNotNull(image.getTypeName())&&StringUtils.isNotEmpty(imageTypeIds),Image::getImageId,imageTypeIds)
                .in(StringUtils.isNotNull(image.getTagName())&&StringUtils.isNotEmpty(imageTagIds),Image::getImageId,imageTagIds)
                .eq(StringUtils.isNotNull(image.getIsDelete()),Image::getIsDelete,image.getIsDelete())
                .between(StringUtils.isNotNull(image.getBeginTime()) && StringUtils.isNotNull(image.getEndTime())
                        ,Image::getCreateTime,image.getBeginTime(),image.getEndTime())
                .orderByDesc(Image::getCreateTime);
        List<Image> imageList = imageMapper.selectList(wrapper);
        if(!(isSearch && isReturnEmpty))
            imageList.forEach(item -> voList.add(getImageVo(item)));
        HashMap<String, Object> rspData = new HashMap<>();
        rspData.put("rows",voList);
        rspData.put("total",isSearch && isReturnEmpty? 0 : new PageInfo(imageList).getTotal());
        return R.ok(rspData);
    }

    // 添加信息
    @PostMapping
    @Transactional
    public R<?> save(@Validated @RequestBody ImageVo image) {
        Image newImage = new Image();
        BeanMapper.copy(image,newImage);
        newImage.setImageId(LoginHelper.getUserId());
        newImage.setCreateTime(new Date());
        newImage.setCreateBy(LoginHelper.getUserId());
        imageMapper.insert(newImage);
        if(StringUtils.isNotNull(image.getAlbumId())){
            Album album = albumMapper.selectById(image.getAlbumId());
            if(StringUtils.isNull(album))
                return R.fail("相册不存在");
            ImageAlbum imageAlbum = new ImageAlbum();
            imageAlbum.setAlbumId(image.getAlbumId());
            imageAlbum.setImageId(newImage.getImageId());
            imageAlbumMapper.insert(imageAlbum);
            album.setAlbumCover(image.getImageUrl());
            albumMapper.updateById(album);
//            if(StringUtils.isNull(album.getAlbumCover())){
//                List<ImageAlbum> imageAlbums = imageAlbumMapper.selectList(new LambdaQueryWrapper<ImageAlbum>()
//                        .eq(ImageAlbum::getAlbumId, image.getAlbumId()));
//                List<Long> collect = imageAlbums.stream().map(ImageAlbum::getImageId).collect(Collectors.toList());
//                Image one = imageMapper.selectOne(new LambdaQueryWrapper<Image>()
//                        .in(Image::getImageId, collect)
//                        .orderByDesc(Image::getCreateTime));
//                album.setAlbumCover(one.getImageUrl());
//                albumMapper.updateById(album);
//            }
        }
        if(StringUtils.isNotNull(image.getTypeId())){
            ImageType imageType = new ImageType();
            imageType.setImageId(newImage.getImageId());
            imageType.setTypeId(image.getTypeId());
            imageTypeMapper.insert(imageType);
        }
        if(StringUtils.isNotEmpty(image.getTagNameList())){
            image.getTagNameList().forEach(item -> {
                Tag tag = new Tag();
                tag.setName(item);
                tagMapper.insert(tag);
                ImageTag imageTag = new ImageTag();
                imageTag.setTagId(tag.getTagId());
                imageTag.setImageId(newImage.getImageId());
                imageTagMapper.insert(imageTag);
            });
        }
        return R.ok();
    }

    // 设置封面
    @PostMapping("/setCover")
    public R<?> setCover(@RequestBody ImageVo image) {
        Image img = imageMapper.selectById(image.getImageId());
        if(StringUtils.isNull(img))
            return R.fail("图片不存在");
        Album album = albumMapper.selectById(image.getAlbumId());
        if(StringUtils.isNull(album))
            return R.fail("相册不存在");
        album.setAlbumCover(img.getImageUrl());
        albumMapper.updateById(album);
        return R.ok();
    }

    // 更新信息
    @PutMapping
    @Transactional
    public R<?> update(@RequestBody ImageVo image) {
        Set<Long> set = new HashSet<>();
        Image image1 = imageMapper.selectById(image.getImageId());
        if(StringUtils.isNotNull(image1)){
            List<ImageAlbum> imageAlbums = imageAlbumMapper.selectList(new LambdaQueryWrapper<ImageAlbum>().eq(ImageAlbum::getImageId,image1.getImageId()));
            if(StringUtils.isNotEmpty(imageAlbums)){
                set = imageAlbums.stream().map(ImageAlbum::getAlbumId).collect(Collectors.toSet());
            }
        }
        Image newImage = new Image();
        BeanMapper.copy(image,newImage);
        imageMapper.update(newImage,new UpdateWrapper<Image>().lambda()
                .eq(Image::getImageId,newImage.getImageId())
                .set(StringUtils.isEmpty(newImage.getImageDesc()),Image::getImageDesc,null));
        imageAlbumMapper.delete(new LambdaQueryWrapper<ImageAlbum>()
                .eq(ImageAlbum::getImageId,image.getImageId()));
        if(StringUtils.isNotNull(image.getAlbumId())){
            set.add(image.getAlbumId());
            ImageAlbum imageAlbum = new ImageAlbum();
            imageAlbum.setAlbumId(image.getAlbumId());
            imageAlbum.setImageId(newImage.getImageId());
            imageAlbumMapper.insert(imageAlbum);
        }
        imageTypeMapper.delete(new LambdaQueryWrapper<ImageType>()
                .eq(ImageType::getImageId,image.getImageId()));
        if(StringUtils.isNotNull(image.getTypeId())){
            ImageType imageType = new ImageType();
            imageType.setImageId(newImage.getImageId());
            imageType.setTypeId(image.getTypeId());
            imageTypeMapper.insert(imageType);
        }
        List<ImageTag> imageTags = imageTagMapper.selectList(new LambdaQueryWrapper<ImageTag>()
                .eq(ImageTag::getImageId, image.getImageId()));
        if(StringUtils.isNotEmpty(imageTags)){
            List<Long> tagIds = imageTags.stream().map(ImageTag::getTagId).collect(Collectors.toList());
            tagMapper.delete(new LambdaQueryWrapper<Tag>().in(Tag::getTagId,tagIds));
            imageTagMapper.delete(new LambdaQueryWrapper<ImageTag>()
                    .in(ImageTag::getImageId,tagIds));
        }
        image.getTagNameList().forEach(item -> {
            Tag tag = new Tag();
            tag.setName(item);
            tagMapper.insert(tag);
            ImageTag imageTag = new ImageTag();
            imageTag.setTagId(tag.getTagId());
            imageTag.setImageId(newImage.getImageId());
            imageTagMapper.insert(imageTag);
        });
        updateCover(set);
        return R.ok();
    }

    // 移入回收站
    @PutMapping("/{imageIds}")
    @Transactional
    public R<?> falseRemove(@PathVariable Long[] imageIds) {
        HashSet<Long> set = new HashSet<>();
        for (Long imageId : imageIds) {
            Image image = imageMapper.selectById(imageId);
            if(StringUtils.isNull(image))
                return R.fail("图片不存在");
            Integer count = imageEventMapper.selectCount(new LambdaQueryWrapper<ImageEvent>().eq(ImageEvent::getImageId, image.getImageId()));
            if(count>0)
                return R.fail("请将该图片从事件移除再删除图片");
            image.setIsDelete(Constants.EXCEPTION);
            // 获取当前日期
            LocalDate currentDate = LocalDate.now();
            // 加上一个月
            LocalDate newDate = currentDate.plusMonths(1);
            // 将LocalDate转换为ZonedDateTime，默认时间设置为午夜0点
            ZonedDateTime zonedDateTime = newDate.atStartOfDay(ZoneId.systemDefault());
            // 将ZonedDateTime转换为Date
            Date date = Date.from(zonedDateTime.toInstant());
            image.setExpiredTime(date);
            imageMapper.updateById(image);
            List<ImageAlbum> imageAlbumList = imageAlbumMapper.selectList(new LambdaQueryWrapper<ImageAlbum>()
                    .eq(ImageAlbum::getImageId, imageId));
            if(StringUtils.isNotEmpty(imageAlbumList)){
                Set<Long> collect = imageAlbumList.stream().map(ImageAlbum::getAlbumId).collect(Collectors.toSet());
                set.addAll(collect);
            }
            imageAlbumMapper.delete(new LambdaQueryWrapper<ImageAlbum>()
                    .eq(ImageAlbum::getImageId,imageId));
            imageTypeMapper.delete(new LambdaQueryWrapper<ImageType>()
                    .eq(ImageType::getImageId,imageId));
            List<ImageTag> imageTags = imageTagMapper.selectList(new LambdaQueryWrapper<ImageTag>()
                    .eq(ImageTag::getImageId, imageId));
            if(StringUtils.isNotEmpty(imageTags)){
                List<Long> tagIds = imageTags.stream().map(ImageTag::getTagId).collect(Collectors.toList());
                tagMapper.delete(new LambdaQueryWrapper<Tag>().in(Tag::getTagId,tagIds));
                imageTagMapper.delete(new LambdaQueryWrapper<ImageTag>()
                        .in(ImageTag::getImageId,tagIds));
            }
        }
        updateCover(set);
        return R.ok();
    }

    private void updateCover(Set<Long> set) {
        set.forEach(item -> {
            Album album = albumMapper.selectById(item);
            if(StringUtils.isNotNull(album)){
                List<ImageAlbum> imageAlbumList = imageAlbumMapper.selectList(new LambdaQueryWrapper<ImageAlbum>()
                        .eq(ImageAlbum::getAlbumId, album.getAlbumId()));
                if(StringUtils.isNotEmpty(imageAlbumList)){
                    List<Long> collect = imageAlbumList.stream().map(ImageAlbum::getImageId).collect(Collectors.toList());
                    Image one = imageMapper.selectOne(new LambdaQueryWrapper<Image>().in(Image::getImageId, collect)
                            .orderByDesc(Image::getCreateTime).last("limit 1"));
                    album.setAlbumCover(one.getImageUrl());
                    albumMapper.updateById(album);
                }
            }
        });
    }

    // 从回收站恢复
    @PutMapping("/reCover/{imageIds}")
    public R<?> reCover(@PathVariable Long[] imageIds){
        for (Long imageId : imageIds) {
            Image image = imageMapper.selectById(imageId);
            if(StringUtils.isNull(image))
                return R.fail("图片不存在");
            image.setIsDelete(Constants.NORMAL);
            imageMapper.update(image,new UpdateWrapper<Image>().lambda().eq(Image::getImageId,image.getImageId())
                    .set(Image::getExpiredTime,null));
        }
        return R.ok();
    }

    // 删除信息
    @DeleteMapping("/{imageIds}")
    @Transactional
    public R<?> remove(@PathVariable Long[] imageIds) {
        imageMapper.delete(new QueryWrapper<Image>().lambda()
                .in(Image::getImageId,imageIds));
        return R.ok();
    }

    // 图片加入事件
    @PostMapping("/importImgEvent")
    public R<?> importImgEvent(@RequestBody ImageVo image){
        Event event = eventMapper.selectById(image.getEvId());
        if(StringUtils.isNull(event))
            return R.fail("事件不存在");
        ImageEvent imageEvent = new ImageEvent();
        imageEvent.setImageId(image.getImageId());
        imageEvent.setEvId(imageEvent.getEvId());
        imageEventMapper.insert(imageEvent);
        return R.ok();
    }

    // 图片移除事件
    @PutMapping("/removeImgEvent")
    public R<?> removeImgEvent(@RequestBody ImageVo image){
        Event event = eventMapper.selectById(image.getEvId());
        if(StringUtils.isNull(event))
            return R.fail("事件不存在");
        imageEventMapper.delete(new LambdaQueryWrapper<ImageEvent>()
                .eq(ImageEvent::getEvId,image.getEvId())
                .eq(ImageEvent::getImageId,image.getImageId()));
        return R.ok();
    }

    // 图片加入相册
    @PutMapping("/moveToAlbum")
    @Transactional
    public R<?> moveToAlbum(@RequestBody ImageAlbumVo vo){
        if(StringUtils.isNull(vo.getAlbumId()))
            vo.getImageIds().forEach(item -> imageAlbumMapper.delete(new LambdaQueryWrapper<ImageAlbum>().eq(ImageAlbum::getImageId,item)));
        else {
            Album album = albumMapper.selectById(vo.getAlbumId());
            if(StringUtils.isNull(album))
                return R.fail("相册不存在");
            vo.getImageIds().forEach(item -> {
                imageAlbumMapper.delete(new LambdaQueryWrapper<ImageAlbum>().eq(ImageAlbum::getImageId,item));
                ImageAlbum imageAlbum = new ImageAlbum();
                imageAlbum.setAlbumId(vo.getAlbumId());
                imageAlbum.setImageId(item);
                imageAlbumMapper.insert(imageAlbum);
            });
        }

        return R.ok();
    }

    // 获取饼状图
    @GetMapping("/getPieChart/{type}")
    public R<?> getPieChart(@PathVariable String type){
        List<Chart> list = new ArrayList<>();
        switch (type){
            // 相册
            case "1":
                List<Album> albums = albumMapper.selectList(new LambdaQueryWrapper<Album>().eq(Album::getCreateBy, LoginHelper.getUserId()));
                albums.forEach(item -> {
                    Chart chart = new Chart();
                    chart.setName(item.getAlbumName());
                    chart.setValue(imageAlbumMapper.selectCount(new LambdaQueryWrapper<ImageAlbum>().eq(ImageAlbum::getAlbumId,item.getAlbumId())));
                    list.add(chart);
                });
                break;
            // 分类
            case "2":
                List<Type> types = typeMapper.selectList(new LambdaQueryWrapper<Type>().eq(Type::getCreateBy, LoginHelper.getUserId()));
                types.forEach(item -> {
                    Chart chart = new Chart();
                    chart.setName(item.getName());
                    chart.setValue(imageTypeMapper.selectCount(new LambdaQueryWrapper<ImageType>().eq(ImageType::getTypeId,item.getTypeId())));
                    list.add(chart);
                });
                break;
        }
        return R.ok(list);
    }
}
