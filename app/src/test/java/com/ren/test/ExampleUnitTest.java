package com.ren.test;

import com.alibaba.fastjson.JSON;
import com.ren.test.controller.LoginController;
import com.ren.test.entity.LoginRequest;
import com.ren.test.entity.LoginResponse;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        login();

    }

    private void getString(){
        LoginResponse loginResponse = new LoginResponse();
//        loginResponse.age2="22";
//        loginResponse.test="true";
        System.out.println(JSON.toJSONString(loginResponse));
    }

    private void login() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        LoginRequest request = new LoginRequest();
        request.username="renaa";
        request.password="aa";
        LoginResponse login = LoginController.login(request, LoginResponse.class);
        System.out.println(JSON.toJSONString(login));
    }

}