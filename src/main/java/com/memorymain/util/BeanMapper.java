package com.memorymain.util;

import org.dozer.DozerBeanMapper;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeanMapper {

    /**
     * 持有Dozer单例, 避免重复创建DozerMapper消耗资源.
     */
    private static final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    /**
     * 普通对象转换 比如: ADO -> AVO
     * 基于Dozer转换对象的类型.
     */
    public static <T> T map(Object source, Class<T> destinationClass) {
        return dozerBeanMapper.map(source, destinationClass);
    }

    /**
     * 基于Dozer转换Collection中对象的类型.
     */
    public static <T> List<T> mapList(Iterable<?> sourceList, Class<T> destinationClass) {
        List<T> destinationList = new ArrayList();
        for (Object sourceObject : sourceList) {
            T destinationObject = dozerBeanMapper.map(sourceObject, destinationClass);
            destinationList.add(destinationObject);
        }
        return destinationList;
    }

    /**
     * 基于Dozer将对象A的值拷贝到对象B中.
     * * 普通对象转换 比如: ADO -> AVO
     */
    public static void copy(Object source, Object destinationObject) {
        if(source!=null) {
            dozerBeanMapper.map(source, destinationObject);
        }
    }

    // List转换 比如: List<ADO> -> List<AVO>
    public static <T, S> List<T> mapList(Collection<S> source, Function<? super S, ? extends T> mapper) {
        return source.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * 比较两个实体属性值，返回一个map以有差异的属性名为key，value为一个Map分别存oldObject,newObject此属性名的值
     * @param oldObject 进行属性比较的对象1
     * @param newObject 进行属性比较的对象2
     * @return 属性差异比较结果map
     */
    public static Map<String, Map<String,Object>> compareFields(Object oldObject, Object newObject) {
        Map<String, Map<String, Object>> map = null;

        try{
            /**
             * 只有两个对象都是同一类型的才有可比性
             */
            if (oldObject.getClass() == newObject.getClass()) {
                map = new HashMap<String, Map<String,Object>>();
                Class clazz = oldObject.getClass();
                //获取object的所有属性
                PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz,Object.class).getPropertyDescriptors();
                for (PropertyDescriptor pd : pds) {
                    //遍历获取属性名
                    String name = pd.getName();
                    //获取属性的get方法
                    Method readMethod = pd.getReadMethod();
                    // 在oldObject上调用get方法等同于获得oldObject的属性值
                    Object oldValue = readMethod.invoke(oldObject);
                    // 在newObject上调用get方法等同于获得newObject的属性值
                    Object newValue = readMethod.invoke(newObject);

                    if(oldValue instanceof List){
                        continue;
                    }
                    if(newValue instanceof List){
                        continue;
                    }
                    if(oldValue instanceof Timestamp){
                        oldValue = new Date(((Timestamp) oldValue).getTime());
                    }
                    if(newValue instanceof Timestamp){
                        newValue = new Date(((Timestamp) newValue).getTime());
                    }
                    if(oldValue == null && newValue == null){
                        continue;
                    }else if(oldValue == null && newValue != null){
                        Map<String,Object> valueMap = new HashMap<String,Object>();
                        valueMap.put("oldValue",oldValue);
                        valueMap.put("newValue",newValue);
                        map.put(name, valueMap);
                        continue;
                    }
                    if (!oldValue.equals(newValue)) {// 比较这两个值是否相等,不等就可以放入map了
                        Map<String,Object> valueMap = new HashMap<String,Object>();
                        valueMap.put("oldValue",oldValue);
                        valueMap.put("newValue",newValue);
                        map.put(name, valueMap);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return map;
    }



}