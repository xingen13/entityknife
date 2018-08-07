package com.ren.lib.util;

import com.ren.lib.type.FieldType;

/**
 * Created by ren on 2018/7/31.
 */

public class FieldTypeUtil {

    public static String parseFieldTypes(FieldType fieldType) {
        switch (fieldType) {
            case String:
                return "java.lang.String";
            case Long:
                return "long";
            case Integer:
                return "int";
            case Double:
                return "double";
            case Float:
                return "float";
            case Boolean:
                return "boolean";
            case Array:
                return "java.util.List";
            default:
                return "";
        }
    }


    public static FieldType toFieldTypes(String string) {

        switch (string) {
            case "java.lang.String":
                return FieldType.String;
            case "long":
                return FieldType.Long;
            case "int":
                return FieldType.Integer;
            case "double":
                return FieldType.Double;
            case "float":
                return FieldType.Float;
            case "boolean":
                return FieldType.Boolean;
            case "java.util.List":
                return FieldType.Array;
            default:
                return FieldType.No;
        }


    }

}
