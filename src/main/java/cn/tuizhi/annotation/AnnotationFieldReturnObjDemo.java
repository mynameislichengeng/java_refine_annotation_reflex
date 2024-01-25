package cn.tuizhi.annotation;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Date 2024/1/25 11:12
 * @Author by licheng01
 */
public class AnnotationFieldReturnObjDemo {

    public static void main(String[] args) {
        AppleReq appleReq = new AppleReq();
        appleReq.setName("lc");
        OneItem oneItem = new OneItem();
        oneItem.setItem("item");
        appleReq.setHome(Arrays.asList(oneItem));
        Set<String> configSet = new HashSet<>();
        FieldLoopHelper.loopHandle(appleReq, new FieldLoopHelper.LoopHandlerCallback() {
            @Override
            public boolean isHandler(Field field, Object reflectObj) {
                return field.getAnnotation(ReturnFieldAnnotation.class) != null;
            }

            @Override
            public void handler(Field field, Object reflectObj) {
                Object fieldValue = ReflectionUtils.getField(field, reflectObj);
                if (fieldValue == null) {
                    return;
                }
                ReturnFieldAnnotation annotation = field.getAnnotation(ReturnFieldAnnotation.class);
                ReturnFieldEnum returnFieldEnum = annotation.configField();
                if (configSet.contains(returnFieldEnum.getFieldKey())) {
                    //如果包含，那么就有字段数据权限，不需要处理
                    return;
                }
                //如果不包含，那么就没有字段数据权限，需要将对应的值，置空
                ReflectionUtils.setField(field, reflectObj, null);
            }
        });
        System.out.println(appleReq);
    }

    public static class AppleReq {
        private String name;

        private List<OneItem> home;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<OneItem> getHome() {
            return home;
        }

        public void setHome(List<OneItem> home) {
            this.home = home;
        }
    }

    public static class OneItem {
        @ReturnFieldAnnotation(configField = ReturnFieldEnum.ITEM)
        private String item;

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @Inherited
    public @interface ReturnFieldAnnotation {
        ReturnFieldEnum configField();
    }

    public enum ReturnFieldEnum {
        NAME("name"),
        ITEM("item"),
        ;
        private final String fieldKey;

        ReturnFieldEnum(String fieldKey) {
            this.fieldKey = fieldKey;
        }

        public String getFieldKey() {
            return fieldKey;
        }

    }
}
