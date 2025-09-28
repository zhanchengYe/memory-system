package com.memorymain.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.entity.Album;
import com.memorymain.entity.Image;
import com.memorymain.entity.ImageAlbum;
import com.memorymain.mapper.*;
import com.memorymain.util.*;
import com.memorymain.vo.AlbumVo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/album")
public class AlbumController {
    @Resource
    private AlbumMapper albumMapper;
    @Resource
    private ImageAlbumMapper imageAlbumMapper;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private TagMapper tagMapper;

    // 根据编号获取详细信息
    @GetMapping(value = {"/", "/{albumId}"})
    public R<?> getInfo(@PathVariable(value = "albumId", required = false) Long albumId) {
        return R.ok(albumMapper.selectById(albumId));
    }

    // 获取分页列表
    @GetMapping("/list")
    public R<?> list(Album album, int pageNum, int pageSize) {
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        album.setCreateBy(LoginHelper.getUserId());
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<Album> wrapper = new LambdaQueryWrapper<Album>()
                .like(StringUtils.isNotBlank(album.getAlbumName()), Album::getAlbumName, album.getAlbumName())
                .like(StringUtils.isNotBlank(album.getAlbumDesc()), Album::getAlbumDesc, album.getAlbumDesc())
                .eq(Album::getCreateBy,album.getCreateBy());
        List<Album> albumList = albumMapper.selectList(wrapper);
        HashMap<String, Object> rspData = new HashMap<>();
        List<AlbumVo> albumVos = BeanMapper.mapList(albumList, item -> {
            AlbumVo vo = new AlbumVo();
            BeanMapper.copy(item,vo);
            vo.setImageCount(0);
            List<ImageAlbum> imageAlbumList = imageAlbumMapper.selectList(new LambdaQueryWrapper<ImageAlbum>().eq(ImageAlbum::getAlbumId, item.getAlbumId()));
            if(StringUtils.isNotEmpty(imageAlbumList)){
                List<Long> collect = imageAlbumList.stream().map(ImageAlbum::getImageId).collect(Collectors.toList());
                List<Image> imageList = imageMapper.selectList(new LambdaQueryWrapper<Image>().in(Image::getImageId, collect));
                if(StringUtils.isNotEmpty(imageList)){
                    vo.setImageCount((int) imageList.stream().filter(a -> a.getIsDelete().equals(Constants.NORMAL)).count());
                }
            }
            return vo;
        });
        rspData.put("rows",albumVos);
        rspData.put("total",new PageInfo(albumList).getTotal());
        return R.ok(rspData);
    }

    // 添加信息
    @PostMapping
    public R<?> save(@Validated @RequestBody Album album) {
        album.setCreateBy(LoginHelper.getUserId());
        album.setCreateTime(new Date());
        albumMapper.insert(album);
        return R.ok();
    }

    // 更新信息
    @PutMapping
    public R<?> update(@RequestBody Album album) {
        albumMapper.update(album,new UpdateWrapper<Album>().lambda()
                .eq(Album::getAlbumId,album.getAlbumId())
                .set(StringUtils.isEmpty(album.getAlbumDesc()),Album::getAlbumDesc,null));
        return R.ok();
    }

    // 删除相册
    @DeleteMapping("/{albumIds}")
    @Transactional
    public R<?> remove(@PathVariable Long[] albumIds) {
        albumMapper.delete(new QueryWrapper<Album>().lambda()
                .in(Album::getAlbumId,albumIds));
        imageAlbumMapper.delete(new QueryWrapper<ImageAlbum>().lambda()
                .in(ImageAlbum::getAlbumId,albumIds));
        return R.ok();
    }
}
