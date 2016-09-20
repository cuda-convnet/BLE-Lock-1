package com.iboxshare.testble.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.iboxshare.testble.R;
import com.iboxshare.testble.model.UserInfo;
import com.iboxshare.testble.util.PostTool;
import com.iboxshare.testble.util.Utils;

import java.util.List;

/**
 * Created by KN on 16/8/23.
 */
public class SplashActivity extends AppCompatActivity{
    private Context context = this;
    private UserInfo user;
    String[] PERMISSIONS = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent();
            switch (msg.what){
                case 0:
                    intent.setClass(context,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case 1:
                    Log.e("登录成功","UserName === " + user.getUser_name());
                    intent.setClass(context,MainActivity.class);
                    Bundle data = new Bundle();
                    data.putSerializable("user",user);
                    intent.putExtras(data);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //针对Android M及以上设备申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(PERMISSIONS,1);
        }

        //利用Token进行自动登录
        final String token = (String) Utils.getUserProfiles(context,Utils.USER_PROFILES_TOKEN);
        if (token == null){
            //无Token需要手动登录
            Intent intent = new Intent(context,LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    user = PostTool.tokenLogin(token);
                    if (user.getToken() == null){
                        //自动登录失败
                        handler.sendEmptyMessage(0);
                    } else {
                        //自动登录成功
                        handler.sendEmptyMessage(1);
                    }
                }
            }).start();
        }
    }

    /**
     * Android 6.0以上设备申请权限返回结果
     * @param requestCode   请求码
     * @param permissions   请求的权限
     * @param grantResults  授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
