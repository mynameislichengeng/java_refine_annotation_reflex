package cn.tuizhi.annotation.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * @Description
 * @Date 2024/1/25 11:51
 * @Author by licheng01
 */
public class ValueEmptyUtil {

    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).size() == 0;
        }
        if (value instanceof Map) {
            return ((Map) value).size() == 0;
        }
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        return false;
    }
}
