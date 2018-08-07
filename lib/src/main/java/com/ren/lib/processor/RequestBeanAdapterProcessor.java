package com.ren.lib.processor;

import com.google.auto.service.AutoService;
import com.ren.lib.type.AnnotionType;
import com.ren.lib.util.FieldTypeUtil;
import com.ren.lib.annotation.RequestEntity;
import com.ren.lib.annotation.RequestField;
import com.ren.lib.annotation.ResponseEntity;
import com.ren.lib.annotation.ResponseField;
import com.ren.lib.constant.Constant;
import com.ren.lib.entity.ClassEntity;
import com.ren.lib.entity.ClassFiledEntity;
import com.ren.lib.type.FieldType;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
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
public class RequestBeanAdapterProcessor extends AbstractProcessor {

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
        processRequest(set, roundEnvironment);
//        processResponse(set, roundEnvironment);

        for (ClassEntity classEntity : mClassEntityMap.values()) {
            createFile(classEntity);
        }
        return true;
    }

    private boolean processResponse(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> fieldEntitys = roundEnvironment.getElementsAnnotatedWith(ResponseField.class);
        for (Element e : fieldEntitys) {
            String packName = getPackName(e);
            String simpleName = getSimpleClassName(e);
            String qualifiedName = getQualifiedName(e);

            ResponseEntity annotation = e.getEnclosingElement().getAnnotation(ResponseEntity.class);
            if (annotation != null) {
                buildClassEntity(e, packName, qualifiedName, simpleName, AnnotionType.RESPONSE);
            }
        }

        return true;
    }

    private boolean processRequest(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> fieldEntitys = roundEnvironment.getElementsAnnotatedWith(RequestField.class);
        for (Element e : fieldEntitys) {

            String packName = getPackName(e);
            String simpleName = getSimpleClassName(e);
            String qualifiedName = getQualifiedName(e);

            RequestEntity annotation = e.getEnclosingElement().getAnnotation(RequestEntity.class);
            if (annotation != null) {
                buildClassEntity(e, packName, qualifiedName, simpleName, AnnotionType.REQUEST);
            }
        }

        return true;
    }

    private String getPackName(Element e) {
        PackageElement packageOf = mElementUtils.getPackageOf(e);
        String packName = packageOf.getQualifiedName().toString(); //包名
        return packName;
    }

    private String getSimpleClassName(Element e) {
        TypeElement enclosingElement = (TypeElement) e.getEnclosingElement();
        String simpleName = enclosingElement.getSimpleName().toString(); //不包括包路径类名
        return simpleName;
    }

    private String getQualifiedName(Element e) {
        TypeElement enclosingElement = (TypeElement) e.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString(); //完整路径类名
        return qualifiedName;
    }

    private ClassEntity buildClassEntity(Element e, String packName, String qualifiedName, String simpleName, AnnotionType annotionType) {
        ClassEntity classEntity = mClassEntityMap.get(qualifiedName);
        if (classEntity == null) {
            classEntity = new ClassEntity();
            classEntity.annotionType = annotionType;
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
        ClassFiledEntity annotionsEntity = parseElementAnnotions(e, filedEntity,classEntity.annotionType);
        classEntity.filedEntityList.add(filedEntity);
        classEntity.annotionList.add(annotionsEntity);
        return classEntity;
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

    private ClassFiledEntity parseElementAnnotions(Element e, ClassFiledEntity filedEntity,AnnotionType annotionType) {

        String annotationName = null;
        FieldType fieldType=FieldType.No;

        if (annotionType==AnnotionType.REQUEST){
            RequestField annotation = e.getAnnotation(RequestField.class);
            //注解设定的名称
            annotationName = annotation.name();
            //注解设定的属性
            fieldType = annotation.type();
        }else if (annotionType==AnnotionType.RESPONSE) {
            ResponseField annotation = e.getAnnotation(ResponseField.class);
            //注解设定的名称
            annotationName = annotation.name();
            //注解设定的属性
            fieldType = annotation.type();
        }

        ClassFiledEntity entity = new ClassFiledEntity();
        if (annotationName == null || annotationName.equals("")) {
            entity.name = filedEntity.name;
        } else {
            entity.name = annotationName;
        }

        if (fieldType == FieldType.No || fieldType == null) {
            entity.type = filedEntity.type;
        } else {
            entity.type = FieldTypeUtil.parseFieldTypes(fieldType);
        }
        return entity;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(RequestField.class.getCanonicalName());
        set.add(ResponseField.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    /**
     * 生成请求实体文件
     */
    private void createFile(ClassEntity classEntity) {
        try {
            JavaFileObject jfo = mFiler.createSourceFile(buildClassName(classEntity.qualifiedName, classEntity.annotionType), new Element[]{});
            Writer writer = jfo.openWriter();
            writer.write(brewCode(classEntity));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String brewCode(ClassEntity classEntity) {
        switch (classEntity.annotionType) {
            case REQUEST:
                return brewRequestCode(classEntity);
            case RESPONSE:
                return brewResponseCode(classEntity);
        }
        return "";
    }

    private String brewResponseCode(ClassEntity classEntity) {
        StringBuilder builder = new StringBuilder();
        builder.append("package " + classEntity.packName + ";\n\n");
        builder.append("//自动生成的代码\n\n");
        builder.append("import com.ren.lib.entity._ResponseBean;\n\n");
        builder.append("public class " + buildClassName(classEntity.simpleName, classEntity.annotionType) + " extends _ResponseBean { \n\n");
        //生成属性
        for (int i = 0; i < classEntity.annotionList.size(); i++) {
            ClassFiledEntity entity = classEntity.annotionList.get(i);
            String name = entity.name;
            String type = entity.type;
            builder.append("  public " + type + " " + name + ";\n\n");
        }

        builder.append("}");
        return builder.toString();
    }

    private String brewRequestCode(ClassEntity classEntity) {
        StringBuilder builder = new StringBuilder();
        builder.append("package " + classEntity.packName + ";\n\n");
        builder.append("//自动生成的代码\n\n");
        builder.append("import com.ren.lib.entity._RequestBean;\n\n");
        builder.append("public class " + buildClassName(classEntity.simpleName, classEntity.annotionType) + " extends _RequestBean { \n\n");
        //生成属性
        for (int i = 0; i < classEntity.annotionList.size(); i++) {
            ClassFiledEntity entity = classEntity.annotionList.get(i);
            String name = entity.name;
            String type = entity.type;
            builder.append("  public " + type + " " + name + ";\n\n");
        }

        //生成构造函数
        builder.append("  public " + buildClassName(classEntity.simpleName, classEntity.annotionType) + "(" + classEntity.simpleName + " bean){\n\n");
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

    private String buildClassName(String className, AnnotionType annotionType) {
        switch (annotionType) {
            case REQUEST:
                return className + Constant.CLASS_SUFFIX_REQUEST;
            case RESPONSE:
                return className + Constant.CLASS_SUFFIX_RESPONSE;
        }
        return "";
    }


    private void note(String msg) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, msg);
    }


}
