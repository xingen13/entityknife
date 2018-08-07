package com.ren.test.entity;

import com.ren.lib.KnifeResponse;
import com.ren.lib.annotation.ResponseField;
import com.ren.lib.type.FieldType;

/**
 * Created by ren on 2018/8/1.
 */

public class LoginResponse extends KnifeResponse{

    @ResponseField(name = "age")
    public int age2;

    @ResponseField(name = "test2")
    public boolean test;

}
