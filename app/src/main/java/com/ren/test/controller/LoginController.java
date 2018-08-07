package com.ren.test.controller;

import com.ren.lib.EntityKnife;
import com.ren.lib.KnifeResponse;
import com.ren.lib.entity._ResponseBean;
import com.ren.test.entity.LoginRequest;
import com.ren.test.entity.LoginRequest_RequestBean;
import com.ren.test.entity.LoginResponse;

/**
 * Created by ren on 2018/7/27.
 */

public class LoginController {

    public static LoginResponse login(LoginRequest request, Class<? extends KnifeResponse> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String jsonStr="{\"age\":\"22\",\"test2\":\"true\"}";
        KnifeResponse knifeResponse = EntityKnife.parseResponse(jsonStr, clazz);
        return (LoginResponse) knifeResponse;
    }

}
