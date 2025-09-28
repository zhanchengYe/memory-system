package com.memorymain.util;

import java.util.List;
import java.util.stream.Collectors;

public final class PageUtils {
    public static <T> List<T> getMyDataByPage(List<T> data, int pageNo, int pageSize) {
        return data.stream()
                .skip((long) (pageNo - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }
}
