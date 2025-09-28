package com.memorymain.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.memorymain.entity.*;
import com.memorymain.mapper.*;
import com.memorymain.util.BeanMapper;
import com.memorymain.util.LoginHelper;
import com.memorymain.util.R;
import com.memorymain.util.StringUtils;
import com.memorymain.vo.EventVo;
import com.memorymain.vo.ImageVo;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/event")
public class EventController {
    @Resource
    private EventMapper eventMapper;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private ImageEventMapper imageEventMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private ImageTypeMapper imageTypeMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private ImageTagMapper imageTagMapper;
    @Resource
    private UserMapper userMapper;

    // 根据新闻编号获取详细信息
    @GetMapping(value = {"/", "/{evId}"})
    public R<?> getInfo(@PathVariable(value = "evId", required = false) Long evId) {
        List<ImageVo> voList = new ArrayList<>();
        EventVo vo = new EventVo();
        Event event = eventMapper.selectById(evId);
        if(StringUtils.isNull(event))
            return R.fail("事件不存在");
        BeanMapper.copy(event,vo);
        List<ImageEvent> imageEventList = imageEventMapper.selectList(new LambdaQueryWrapper<ImageEvent>().eq(ImageEvent::getEvId, evId));
        if(StringUtils.isNotEmpty(imageEventList)){
            Set<Long> set = imageEventList.stream().map(ImageEvent::getImageId).collect(Collectors.toSet());
            List<Image> imageList = imageMapper.selectList(new LambdaQueryWrapper<Image>().in(Image::getImageId, set));
            if(StringUtils.isNotEmpty(imageList)){
                imageList.forEach(item -> voList.add(getImageVo(item)));
            }
        }
        vo.setImageVoList(voList);
        return R.ok(vo);
    }

    // 获取分页列表
    @GetMapping("/list")
    public R<?> list(Event event, int pageNum, int pageSize) {
        pageNum = ObjectUtil.defaultIfNull(pageNum, 1);
        pageSize = ObjectUtil.defaultIfNull(pageSize, 10);
        PageHelper.startPage(pageNum,pageSize);
        LambdaQueryWrapper<Event> wrapper = new LambdaQueryWrapper<Event>()
                .like(StringUtils.isNotBlank(event.getName()), Event::getName, event.getName());
        List<Event> eventList = eventMapper.selectList(wrapper);
        List<EventVo> eventVoList = BeanMapper.mapList(eventList, item -> {
            EventVo eventVo = new EventVo();
            BeanMapper.copy(item, eventVo);
            List<ImageEvent> imageEvents = imageEventMapper.selectList(new LambdaQueryWrapper<ImageEvent>()
                    .eq(ImageEvent::getEvId, item.getEvId()));
            List<ImageVo> imageVoList = new ArrayList<>();
            if (StringUtils.isNotEmpty(imageEvents)) {
                List<Long> collect = imageEvents.stream().map(ImageEvent::getImageId).collect(Collectors.toList());
                List<Image> imageList = imageMapper.selectList(new LambdaQueryWrapper<Image>().in(Image::getImageId, collect));
                imageVoList = BeanMapper.mapList(imageList, this::getImageVo);
            }
            eventVo.setImageVoList(imageVoList);
            User user = userMapper.selectById(item.getCreateBy());
            if(StringUtils.isNotNull(user)){
                eventVo.setCreateByName(StringUtils.isNotNull(user.getUsername())?user.getUsername():"");
            }
            eventVo.setIsSelf(Objects.equals(LoginHelper.getUserId(), item.getCreateBy()));
            return eventVo;
        });
        HashMap<String, Object> rspData = new HashMap<>();
        rspData.put("rows",eventVoList);
        rspData.put("total",new PageInfo(eventList).getTotal());
        return R.ok(rspData);
    }

    private ImageVo getImageVo(Image image) {
        ImageVo imageVo = new ImageVo();
        BeanMapper.copy(image, imageVo);
        // 补充标签分类信息
        ImageType imageType = imageTypeMapper.selectOne(new LambdaQueryWrapper<ImageType>()
                .eq(ImageType::getImageId, imageVo.getImageId()).last("limit 1"));
        if(StringUtils.isNotNull(imageType)){
            Type type = typeMapper.selectOne(new LambdaQueryWrapper<Type>()
                    .eq(Type::getTypeId, imageType.getTypeId()).last("limit 1"));
            if(StringUtils.isNotNull(type)){
                imageVo.setTypeId(type.getTypeId());
                imageVo.setTypeName(type.getName());
            }
        }
        List<ImageTag> imageTags = imageTagMapper.selectList(new LambdaQueryWrapper<ImageTag>()
                .eq(ImageTag::getImageId, imageVo.getImageId()));
        if(StringUtils.isNotEmpty(imageTags)){
            List<Long> tagIds = imageTags.stream().map(ImageTag::getTagId).collect(Collectors.toList());
            List<Tag> tagList = tagMapper.selectList(new LambdaQueryWrapper<Tag>().in(Tag::getTagId, tagIds));
            List<String> list = tagList.stream().map(Tag::getName).collect(Collectors.toList());
            imageVo.setTagNameList(list);
        }
        return imageVo;
    }

    // 添加信息
    @PostMapping
    @Transactional
    public R<?> save( @RequestBody EventVo eventVo) {
        Event event = new Event();
        BeanMapper.copy(eventVo,event);
        event.setCreateBy(LoginHelper.getUserId());
        event.setCreateTime(new Date());
        eventMapper.insert(event);
        eventVo.getImageIds().forEach(item -> {
            ImageEvent imageEvent = new ImageEvent();
            imageEvent.setImageId(item);
            imageEvent.setEvId(event.getEvId());
            imageEventMapper.insert(imageEvent);
        });
        return R.ok();
    }

    // 更新信息
    @PutMapping
    @Transactional
    public R<?> update( @RequestBody EventVo eventVo) {
        Event event = new Event();
        BeanMapper.copy(eventVo,event);
        eventMapper.updateById(event);
        imageEventMapper.delete(new LambdaQueryWrapper<ImageEvent>()
                .eq(ImageEvent::getEvId,event.getEvId()));
        eventVo.getImageIds().forEach(item -> {
            ImageEvent imageEvent = new ImageEvent();
            imageEvent.setImageId(item);
            imageEvent.setEvId(event.getEvId());
            imageEventMapper.insert(imageEvent);
        });
        return R.ok();
    }

    // 删除信息
    @DeleteMapping("/{evIds}")
    public R<?> remove(@PathVariable Long[] evIds) {
        eventMapper.delete(new QueryWrapper<Event>().lambda()
                .in(Event::getEvId,evIds));
        imageEventMapper.delete(new LambdaQueryWrapper<ImageEvent>().in(ImageEvent::getEvId,evIds));
        return R.ok();
    }
}
