package com.iboxshare.testble.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.util.Util;
import com.iboxshare.testble.R;
import com.iboxshare.testble.util.Utils;

/**
 * Created by KN on 16/9/20.
 */
public class SetScreenPasswordActivity extends Activity {
    private Context context = this;
    private TextInputLayout passwordTIL;
    private Button commitBtn;

    //flags
    private int PASSWORD_IS_SET = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_screen_passowrd);
        bindView();
        init();
    }
    void bindView(){
        passwordTIL = (TextInputLayout) findViewById(R.id.activity_set_screen_password_TIL);
        commitBtn = (Button) findViewById(R.id.activity_set_screen_password_commit);
    }

    void init(){
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordTIL.getEditText() != null) {
                    String passowrd = passwordTIL.getEditText().getText().toString();
                    if (passowrd.isEmpty() || passowrd.length() != 6){
                        Utils.showGlobalToast(context,"请输入6位数字密码");
                    }else {
                        PASSWORD_IS_SET = 1;
                        MainActivity.setScreenPassword(Integer.valueOf(passowrd));
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(PASSWORD_IS_SET == 0){
            setResult(0);
            Utils.showGlobalToast(context,"取消设置密码");
        }
    }
}
