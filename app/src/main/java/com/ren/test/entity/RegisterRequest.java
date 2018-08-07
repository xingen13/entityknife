package com.ren.test.entity;

import com.ren.lib.KnifeRequest;
import com.ren.lib.annotation.RequestField;

/**
 * Created by ren on 2018/7/27.
 */

public class RegisterRequest extends KnifeRequest {

    @RequestField(name = "username")
    public String username;

    @RequestField
    public String password;

}
