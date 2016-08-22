package com.iboxshare.testble.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iboxshare.testble.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by KN on 16/8/21.
 */
public class PostTool {
    private static OkHttpClient client = new OkHttpClient();
    private static RequestBody requestBody;
    private static Request request;
    private static Response response;


    /**
     * 登录操作
     * @param userName  用户名
     * @param passWord  密码
     * @return 返回一个UserInfo对象
     */
    public static UserInfo login(String userName,String passWord){
        requestBody = new FormBody.Builder()
                .add("userName",userName)
                .add("passWord",passWord)
                .build();
        request = new Request.Builder().url("test").post(requestBody).build();
        UserInfo user = new UserInfo();
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonObject = new JSONObject(response.body().string());
                //获取结果码
                int resultCode = jsonObject.getInt("result");
                switch (resultCode){
                    //登录失败
                    case -1:
                    case 0:
                        return user;
                    //登录成功
                    case 1:
                        Gson gson = new Gson();
                        user = gson.fromJson(response.body().string(),new TypeToken<UserInfo>(){}.getType());
                        return user;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return user;
    }


    /**
     * 注册操作
     * @param userName  用户名
     * @param nickName  昵称
     * @param passWord  密码
     * @return 返回注册情况
     */
    public static String register(String userName,String nickName,String passWord){
        requestBody = new FormBody.Builder()
                .add("userName",userName)
                .add("nickName",nickName)
                .add("passWord",passWord)
                .build();
        request = new Request.Builder().url("test").post(requestBody).build();
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonObject = new JSONObject(response.body().string());
                int result = jsonObject.getInt("result");
                switch (result){
                    case -1:
                        return "服务器异常";

                    case 0:
                        return "用户名重复";

                    case 1:
                        return "注册成功";
                }
            }else {
                return "网络错误";
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "网络错误";
    }


    /**
     * 使用本地存放的token来实现自动登录或用于判断登录状态
     * @param token Token
     * @return 返回一个UserInfo对象
     */
    public static UserInfo tokenLogin(String token){
        requestBody = new FormBody.Builder()
                .add("token",token)
                .build();
        request = new Request.Builder().url("test").post(requestBody).build();
        UserInfo user = new UserInfo();
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonObject = new JSONObject(response.body().string());
                //获取结果码
                int resultCode = jsonObject.getInt("result");
                switch (resultCode){
                    //登录失败
                    case -1:
                    case 0:
                        return user;
                    //登录成功
                    case 1:
                        Gson gson = new Gson();
                        user = gson.fromJson(response.body().string(),new TypeToken<UserInfo>(){}.getType());
                        return user;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

}
