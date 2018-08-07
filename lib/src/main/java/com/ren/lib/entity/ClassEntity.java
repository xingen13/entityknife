package com.ren.lib.entity;

import com.ren.lib.type.AnnotionType;

import java.util.List;

/**
 * 描述类的结构信息
 * Created by ren on 2018/7/31.
 */

public class ClassEntity {

    /**
     * 类注解的类型
     */
    public AnnotionType annotionType;

    /**
     * 包名
     */
    public String packName;

    /**
     * 完整路径类名
     */
    public String qualifiedName;

    /**
     * 不包含包路径的类名称
     */
    public String simpleName;

    /**
     * 属性的注解
     */
    public List<ClassFiledEntity> annotionList;

    /**
     * 属性
     */
    public List<ClassFiledEntity> filedEntityList;

}
