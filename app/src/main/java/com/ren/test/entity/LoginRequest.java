package com.ren.test.entity;

import com.ren.lib.KnifeRequest;
import com.ren.lib.annotation.RequestField;
import com.ren.lib.type.FieldType;

import java.util.List;

/**
 * Created by ren on 2018/7/27.
 */

public class LoginRequest extends KnifeRequest {

    @RequestField(name = "u",type = FieldType.Boolean)
    public String username;

    @RequestField(type = FieldType.String)
    public String password;

    @RequestField(type = FieldType.Long)
    public int age1;

    @RequestField(type = FieldType.String)
    public short age2;

    @RequestField
    public double age3;

    @RequestField
    public float age4;

    @RequestField(type = FieldType.String)
    public boolean age5;

    @RequestField(type = FieldType.Double)
    public long age6;

    @RequestField(type = FieldType.Array)
    public List list;
}
