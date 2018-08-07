package com.ren.lib.processor;

import com.google.auto.service.AutoService;
import com.ren.lib.util.FieldTypeUtil;
import com.ren.lib.annotation.RequestEntity;
import com.ren.lib.annotation.RequestField;
import com.ren.lib.constant.Constant;
import com.ren.lib.entity.ClassEntity;
import com.ren.lib.entity.ClassFiledEntity;
import com.ren.lib.type.FieldType;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class ResponseBeanAdapterProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElementUtils;
    private Map<String, ClassEntity> mClassEntityMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElementUtils = processingEnvironment.getElementUtils();
        mClassEntityMap = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
       return process1(set, roundEnvironment);
    }

    private boolean process1(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment){
        Set<? extends Element> fieldEntitys = roundEnvironment.getElementsAnnotatedWith(RequestField.class);
        for (Element e : fieldEntitys) {
            //获取包名
            PackageElement packageOf = mElementUtils.getPackageOf(e);
            TypeElement enclosingElement = (TypeElement) e.getEnclosingElement();
            String packName = packageOf.getQualifiedName().toString(); //包名
            String simpleName = enclosingElement.getSimpleName().toString(); //完整路径类名
            String qualifiedName = enclosingElement.getQualifiedName().toString(); //不包含路径类名

            RequestEntity annotation = enclosingElement.getAnnotation(RequestEntity.class);
            if (annotation != null) {
                ClassEntity classEntity = mClassEntityMap.get(qualifiedName);
                if (classEntity == null) {
                    classEntity = new ClassEntity();
                    classEntity.packName = packName;
                    classEntity.simpleName = simpleName;
                    classEntity.qualifiedName = qualifiedName;
                    if (classEntity.annotionList == null) {
                        classEntity.annotionList = new ArrayList<>();
                    }
                    if (classEntity.filedEntityList == null) {
                        classEntity.filedEntityList = new ArrayList<>();
                    }
                    mClassEntityMap.put(qualifiedName, classEntity);
                }
                ClassFiledEntity filedEntity = parseElementField(e);
                ClassFiledEntity annotionsEntity = parseElementAnnotions(e, filedEntity);
                classEntity.filedEntityList.add(filedEntity);
                classEntity.annotionList.add(annotionsEntity);
            }
        }

        for (ClassEntity classEntity : mClassEntityMap.values()) {
            createRequestFile(classEntity);
        }

        return true;
    }

    private ClassFiledEntity parseElementField(Element e) {
        //屬性名称
        String fieldName = e.getSimpleName().toString();
        //属性类型
        String fieldType = e.asType().toString();
        ClassFiledEntity srcEnttiy = new ClassFiledEntity();
        srcEnttiy.name = fieldName;
        srcEnttiy.type = fieldType;
        return srcEnttiy;
    }

    private ClassFiledEntity parseElementAnnotions(Element e, ClassFiledEntity filedEntity) {
        RequestField annotation = e.getAnnotation(RequestField.class);
        //注解设定的名称
        String annotationName = annotation.name();
        //注解设定的属性
        FieldType annotationType = annotation.type();

        ClassFiledEntity entity = new ClassFiledEntity();
        if (annotationName == null || annotationName.equals("")) {
            entity.name = filedEntity.name;
        } else {
            entity.name = annotationName;
        }

        if (annotationType == FieldType.No || annotationType == null) {
            entity.type = filedEntity.type;
        } else {
            entity.type = FieldTypeUtil.parseFieldTypes(annotationType);
        }
        return entity;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(RequestField.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    /**
     * 生成文件
     */
    private void createRequestFile(ClassEntity classEntity) {
        try {
            JavaFileObject jfo = mFiler.createSourceFile(classEntity.qualifiedName + Constant.CLASS_SUFFIX_RESPONSE, new Element[]{});
            Writer writer = jfo.openWriter();
            writer.write(brewRequestCode(classEntity));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String brewRequestCode(ClassEntity classEntity) {
        StringBuilder builder = new StringBuilder();
        builder.append("package " + classEntity.packName + ";\n\n");
        builder.append("//自动生成的代码\n\n");
        builder.append("public class " + newRequestClassName(classEntity.simpleName) + " { \n\n");
        //生成属性
        for (int i = 0; i < classEntity.annotionList.size(); i++) {
            ClassFiledEntity entity = classEntity.annotionList.get(i);
            String name = entity.name;
            String type = entity.type;
            builder.append("  public " + type + " " + name + ";\n\n");
        }

        //生成构造函数
        builder.append("  public " + newRequestClassName(classEntity.simpleName) + "(" + classEntity.simpleName + " bean){\n\n");
        for (int i = 0; i < classEntity.annotionList.size(); i++) {
            ClassFiledEntity annotionEntity = classEntity.annotionList.get(i);
            ClassFiledEntity classFiledEntity = classEntity.filedEntityList.get(i);
            if (annotionEntity.type.equals(classFiledEntity.type)) {
                builder.append("    this." + annotionEntity.name + "=" + "bean." + classFiledEntity.name + ";\n\n");
            } else if (annotionEntity.type.equals("double") && classFiledEntity.type.equals("float")) {
                builder.append("    this." + annotionEntity.name + "=" + "bean." + classFiledEntity.name + ";\n\n");
            } else if (annotionEntity.type.equals("long") && classFiledEntity.type.equals("int")) {
                builder.append("    this." + annotionEntity.name + "=" + "bean." + classFiledEntity.name + ";\n\n");
            } else if ((annotionEntity.type.equals("double") || annotionEntity.type.equals("float")) && (classFiledEntity.type.equals("long") || classFiledEntity.type.equals("int"))) {
                builder.append("    this." + annotionEntity.name + "=" + "(double)bean." + classFiledEntity.name + ";\n\n");
            } else if (annotionEntity.type.equals("java.lang.String")) {
                builder.append("    this." + annotionEntity.name + "=" + "String.valueOf(bean." + classFiledEntity.name + ");\n\n");
            } else if (FieldTypeUtil.toFieldTypes(annotionEntity.type) != FieldType.No && classFiledEntity.type.equals("java.lang.String")) {
                builder.append("    this." + annotionEntity.name + "=" + FieldTypeUtil.toFieldTypes(annotionEntity.type) + ".valueOf(bean." + classFiledEntity.name + ");\n\n");
            } else {
                builder.append("    this." + annotionEntity.name + "=" + "bean." + classFiledEntity.name + ";\n\n");
            }

        }
        builder.append(" }\n\n");

        builder.append("}");
        return builder.toString();
    }

    private String newRequestClassName(String className) {
        return className + Constant.CLASS_SUFFIX_RESPONSE;
    }

    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }


}
