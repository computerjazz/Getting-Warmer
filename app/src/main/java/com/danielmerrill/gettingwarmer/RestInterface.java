package com.danielmerrill.gettingwarmer;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;




public interface RestInterface {
    //chnage your IP here if you working on local
     //String url = "http://192.168.0.12:8888/register-login/v1";
    //For Hosting give the complete path before index.php
    String url = "http://ec2-54-193-32-2.us-west-1.compute.amazonaws.com/index.php";

    @FormUrlEncoded
    @POST("/login")
    void Login(@Field("username") String username,
               @Field("pass") String pass,
               Callback<LoginModel> cb);

    @FormUrlEncoded
    @POST("/signup")
    void SignUp(@Field("username") String username,
                @Field("pass") String pass,
                Callback<LoginModel> pm);

    @FormUrlEncoded
    @POST("/addfriend")
    void addFriend(@Field("username") String username,
                   @Field("friendUsername") String friendUsername,
                   Callback<LoginModel> pm);

    @FormUrlEncoded
    @POST("/requestfriend")
    void requestFriend(@Field("username") String username,
                   @Field("friendUsername") String friendUsername,
                   Callback<LoginModel> pm);

    @FormUrlEncoded
    @POST("/deleterelationship")
    void deleteRelationship(@Field("username") String username,
                       @Field("friendUsername") String friendUsername,
                       Callback<LoginModel> pm);

    @FormUrlEncoded
    @POST("/getfriends")
    void getFriends(@Field("username") String username,
                    Callback<LoginModel> cb);

    @FormUrlEncoded
    @POST("/getnew")
    void getFriendsWithNewLocation(@Field("username") String username,
                    Callback<LoginModel> cb);

    @FormUrlEncoded
    @POST("/getrequests")
    void getRequests(@Field("username") String username,
                    Callback<LoginModel> cb);

    @FormUrlEncoded
    @POST("/getlocation")
    void getLocation(@Field("username") String username,
                     @Field("friendUsername") String friendUsername,
                     Callback<LoginModel> pm);

    @FormUrlEncoded
    @POST("/setlocation")
    void setLocation(@Field("username") String username,
                     @Field("friendUsername") String friendUsername,
                     @Field("latitude_target") double latitude_target,
                     @Field("longitude_target") double longitude_target,
                     @Field("latitude_start") double latitude_start,
                     @Field("longitude_start") double longitude_start,
                     Callback<LoginModel> pm);

}
