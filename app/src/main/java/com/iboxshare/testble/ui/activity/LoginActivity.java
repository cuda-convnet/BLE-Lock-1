package com.iboxshare.testble.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.iboxshare.testble.R;
import com.iboxshare.testble.model.UserInfo;
import com.iboxshare.testble.util.PostTool;

/**
 * Created by KN on 16/8/21.
 */
public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";
    private Context context = this;
    private LinearLayout parentLayout;
    private ActionProcessButton loginBtn;
    private TextInputLayout userNameTIL,passwordTIL;
    private UserInfo user;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //登录失败
                case 0:
                    loginBtn.setEnabled(true);
                    loginBtn.setProgress(-1);
                    break;

                //登陆成功
                case 1:
                    loginBtn.setProgress(100);
                    break;
            }
        }
    };
    //Flags
    private boolean NET_LOCKED = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        bindView();
        init();
    }


    void bindView(){
        parentLayout = (LinearLayout) findViewById(R.id.activity_login_parent_layout);
        loginBtn = (ActionProcessButton) findViewById(R.id.activity_login_commit_APB);
        userNameTIL = (TextInputLayout) findViewById(R.id.activity_login_username_TIL);
        passwordTIL = (TextInputLayout) findViewById(R.id.activity_login_password_TIL);
    }

    void init(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            String userName;
            String password;
            @Override
            public void onClick(View v) {
                userName = getTextFromTIL(userNameTIL).trim();
                password = getTextFromTIL(passwordTIL).trim();
                if (userName.isEmpty() || password.isEmpty()){
                    showSnackbar("用户名或密码不能为空");
                }else{
                    loginBtn.setEnabled(false);
                    loginBtn.setProgress(50);
                    //启动新进程进行网络访问
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            user = PostTool.login(userName,password);
                            if (user.getToken() != null){
                                //登录成功
                                handler.sendEmptyMessage(1);
                            }else{
                                //登录失败
                                handler.sendEmptyMessage(0);
                            }
                        }
                    }).start();
                }
            }
        });
    }


    private String getTextFromTIL(TextInputLayout v){
        return v.getEditText().getText().toString();
    }

    private void showSnackbar(String str){
        Snackbar.make(parentLayout,str,Snackbar.LENGTH_SHORT).show();
    }


}
