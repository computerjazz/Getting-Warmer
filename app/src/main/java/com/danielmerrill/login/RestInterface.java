package com.danielmerrill.login;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by danielmerrill on 6/16/16.
 */


public interface RestInterface {
    //chnage your IP here if you working on local
     String url = "http://192.168.0.3:8888/register-login/v1";
    //For Hosting give the complete path before index.php
    //String url = "http://go2code.com/demo/android/register-login/v1";

    @FormUrlEncoded
    @POST("/login")
    void Login(@Field("email") String email,
               @Field("pass") String pass, Callback<LoginModel> cb);

    @FormUrlEncoded
    @POST("/signup")
    void SignUp(@Field("name") String name, @Field("email") String email,
                @Field("pass") String pass, Callback<LoginModel> pm);

}
