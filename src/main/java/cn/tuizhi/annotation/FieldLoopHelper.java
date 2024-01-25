package cn.tuizhi.annotation;

import cn.tuizhi.annotation.util.ValueEmptyUtil;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @Description 字段遍历协助
 * @Date 2024/1/15 10:34
 * @Author by licheng01
 */
public class FieldLoopHelper {

    private static Logger logger = LoggerFactory.getLogger(FieldLoopHelper.class);
    /**
     * 递归最大层级
     */
    private static final int MAX_LEVEL = 10;
    /**
     * 递归开始层级
     */
    private static final int BEGIN_LEVEL = 1;

    /**
     * 项目代码对应包名开头
     */
    private static final String PATH = "cn.mucang";


    public static void loopHandle(Object responseObj, LoopHandlerCallback callback) {
        loopHandle(responseObj, callback, BEGIN_LEVEL);
    }

    private static void loopHandle(Object responseObj, LoopHandlerCallback callback, int level) {
        if (responseObj != null) {
            if (responseObj instanceof Collection) {
                //集合
                handlerCollectionField((Collection) responseObj, callback, level);
            } else if (!isPrimitive(responseObj.getClass())) {
                //非基本对象
                handlerObjectField(responseObj, callback, level);
            }
        }
    }

    private static boolean isPrimitive(Class<?> cls) {
        if (ClassUtils.isPrimitiveOrWrapper(cls)) {
            return true;
        }
        return cls == String.class || cls == BigDecimal.class || cls == BigInteger.class || cls == LocalDate.class || cls == LocalDateTime.class || cls == LocalTime.class || Date.class.isAssignableFrom(cls);
    }

    private static void handlerCollectionField(Collection<Object> responseCollection, LoopHandlerCallback callback, int level) {
        if (level > MAX_LEVEL) {
            return;
        }
        for (Object item : responseCollection) {
            handlerObjectField(item, callback, level);
        }
    }

    private static void handlerMapField(Map<?, ?> value, LoopHandlerCallback callback, int level) {
        if (level > MAX_LEVEL) {
            return;
        }
        value.forEach((k, v) -> loopHandle(v, callback, level));
    }

    /**
     * 隐藏对象中的字段
     */
    private static void handlerObjectField(Object responseObj, LoopHandlerCallback callback, int level) {
        //如果递归层级太多不处理
        if (level > MAX_LEVEL) {
            return;
        }
        Field[] fields = FieldUtils.getAllFields(responseObj.getClass());
        for (Field declaredField : fields) {
            try {
                ReflectionUtils.makeAccessible(declaredField);
            } catch (Throwable e) {
                continue;
            }
            //是否需要做校验
            if (callback.isHandler(declaredField, responseObj)) {
                //是否需要遍历
                callback.handler(declaredField, responseObj);
            }
            if (isPrimitive(declaredField.getType())) {
                //如果是基础类型，就不需要遍历
                continue;
            }

            //继续往后
            Object fieldValue = ReflectionUtils.getField(declaredField, responseObj);
            if (ValueEmptyUtil.isEmpty(fieldValue)) {
                //如果没有值，则不遍历了
                continue;
            }
            //列表
            if (fieldValue instanceof Collection) {
                handlerCollectionField((Collection<Object>) fieldValue, callback, level + 1);
                continue;
            }
            //map结构
            if (fieldValue instanceof Map) {
                handlerMapField((Map<?, ?>) fieldValue, callback, level + 1);
                continue;
            }
            //复合对象
            //如果实际使用中，为了稳一些，可以使用这个
//                if (StringUtils.startsWith(declaredField.getType().getName(), PATH)) {
//                    handlerObjectField(value, operateCallback, level + 1);
//                    continue;
//                }
            handlerObjectField(fieldValue, callback, level + 1);
        }
    }

    public interface LoopHandlerCallback {

        /**
         * 是否需要做校验
         *
         * @param field      字段
         * @param reflectObj 反射对象
         * @return 如果ture需要做校验, false则不需要做
         */
        boolean isHandler(Field field, Object reflectObj);

        /**
         * 校验逻辑
         *
         * @param field      字段
         * @param reflectObj 反射对象
         */
        void handler(Field field, Object reflectObj);
    }
}
