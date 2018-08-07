package com.ren.lib.annotation;

import com.ren.lib.type.FieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 属性适配器
 * Created by ren on 2018/7/27.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RequestField {

    /**
     * 属性名字
     */
    String name() default "";

    /**
     * 属性类型
     */
    FieldType type() default FieldType.No;

}
