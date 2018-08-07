package com.ren.lib;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ren.lib.annotation.ResponseField;
import com.ren.lib.constant.Constant;
import com.ren.lib.entity.ClassFiledEntity;
import com.ren.lib.entity._RequestBean;
import com.ren.lib.entity._ResponseBean;
import com.ren.lib.type.FieldType;
import com.ren.lib.util.FieldTypeUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ren on 2018/7/30.
 */

public class EntityKnife {

    public static <T extends _RequestBean> T bindRequest(KnifeRequest request, Class<T> clazz) {
        Constructor<T> constructor = null;
        try {
            constructor = clazz.getConstructor(request.getClass());
            T t = constructor.newInstance(request);
            return t;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String bindRequest(KnifeRequest request) {
        Class<?> aClass = null;
        try {
            aClass = request.getClass().getClassLoader().loadClass(request.getClass().getName() + Constant.CLASS_SUFFIX_REQUEST);
            Constructor<?> constructor = aClass.getConstructor(request.getClass());
            Object bean = constructor.newInstance(request);
            return JSON.toJSONString(bean);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static <T extends KnifeResponse> T parseResponse(String json, Class<T> clazz) {
        JSONObject jsonObject = JSON.parseObject(json);
        T t = null;
        try {
            t = clazz.newInstance();
            Field[] fields = t.getClass().getFields();
            for (Field field : fields) {
                ClassFiledEntity classFiledEntity = new ClassFiledEntity();
                ResponseField annotation = field.getAnnotation(ResponseField.class);
                field.setAccessible(true);
                if (annotation != null) {
                    if (annotation.name() == null || annotation.name().length() == 0) {
                        classFiledEntity.name = field.getName();
                    } else {
                        classFiledEntity.name = annotation.name();
                    }
                    classFiledEntity.type = field.getType().getName();
                }

                Object o = null;
                FieldType fieldType = FieldTypeUtil.toFieldTypes(classFiledEntity.type);
                if (jsonObject.containsKey(classFiledEntity.name)) {
                    switch (fieldType) {
                        case String:
                            o = jsonObject.containsKey(classFiledEntity.name) ? jsonObject.getString(classFiledEntity.name) : "";
                            break;
                        case Integer:
                            o = jsonObject.containsKey(classFiledEntity.name) ? jsonObject.getInteger(classFiledEntity.name) : 0;
                            break;
                        case Long:
                            o = jsonObject.containsKey(classFiledEntity.name) ? jsonObject.getLong(classFiledEntity.name) : 0;
                            break;
                        case Double:
                            o = jsonObject.containsKey(classFiledEntity.name) ? jsonObject.getDouble(classFiledEntity.name) : 0.0;
                            break;
                        case Float:
                            o = jsonObject.containsKey(classFiledEntity.name) ? jsonObject.getFloat(classFiledEntity.name) : 0.0;
                            break;
                        case Boolean:
                            o = jsonObject.containsKey(classFiledEntity.name) ? jsonObject.getBoolean(classFiledEntity.name) : false;
                            break;
                        case Array:
                            break;
                    }
                    field.set(t, o);
                }

            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return t;
    }

    public static <T extends KnifeResponse> T parseResponse(_ResponseBean responseBean, Class<T> clazz) {
        Field[] responseBeanFields = responseBean.getClass().getFields();
        Map<String, Object> responseBeanMap = new HashMap<>();
        for (Field field : responseBeanFields) {
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(responseBean);
                responseBeanMap.put(field.getName(), fieldValue);


            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        T t = null;
        try {
            t = clazz.newInstance();
            Field[] fields = t.getClass().getFields();
            for (Field field : fields) {
                ResponseField annotation = field.getAnnotation(ResponseField.class);
                field.setAccessible(true);
                String name = null;
                if (annotation != null) {
                    if (annotation.name() == null || annotation.name().length() == 0) {
                        name = field.getName();
                    } else {
                        name = annotation.name();
                    }

                    if (name != null) {
                        field.set(t, responseBeanMap.get(name));
                    }
                }


            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return t;
    }

}
