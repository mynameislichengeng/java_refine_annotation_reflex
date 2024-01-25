package cn.tuizhi.annotation;

import cn.tuizhi.annotation.util.ValueEmptyUtil;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Date 2024/1/25 11:12
 * @Author by licheng01
 */
public class AnnotationFieldValidateDemo {

    public static void main(String[] args) {
        AppleReq appleReq = new AppleReq();
//        appleReq.setName("lc");
        Map<String, Boolean> configMap = new HashMap<>();
        configMap.put("name", false);
        configMap.put("school", true);
        FieldLoopHelper.loopHandle(appleReq, new FieldLoopHelper.LoopHandlerCallback() {
            @Override
            public boolean isHandler(Field field, Object reflectObj) {
                return field.getAnnotation(RequireFieldAnnotation.class) != null;
            }

            @Override
            public void handler(Field field, Object reflectObj) {
                RequireFieldAnnotation annotation = field.getAnnotation(RequireFieldAnnotation.class);
                RequireFieldEnum[] requireFieldEnums = annotation.configField();
                Object fieldValue = ReflectionUtils.getField(field, reflectObj);
                for (RequireFieldEnum requireFieldEnum : requireFieldEnums) {
                    boolean require = configMap.getOrDefault(requireFieldEnum.getFieldKey(), false);
                    if (!require) {
                        //不是必填，所以不需要处理
                        continue;
                    }
                    if (!ValueEmptyUtil.isEmpty(fieldValue)) {
                        //有值，符合要求
                        continue;
                    }
                    throw new RuntimeException(requireFieldEnum.getTips());
                }
            }
        });
    }

    public static class AppleReq {
        @RequireFieldAnnotation(configField = {RequireFieldEnum.NAME, RequireFieldEnum.SCHOOL})
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @Inherited
    public @interface RequireFieldAnnotation {
        RequireFieldEnum[] configField();
    }

    public enum RequireFieldEnum {
        NAME("name", "姓名不能为空"),
        SCHOOL("school", "学校不能为空");
        private final String fieldKey;

        private final String tips;

        RequireFieldEnum(String fieldKey, String tips) {
            this.fieldKey = fieldKey;
            this.tips = tips;
        }

        public String getFieldKey() {
            return fieldKey;
        }

        public String getTips() {
            return tips;
        }
    }
}
