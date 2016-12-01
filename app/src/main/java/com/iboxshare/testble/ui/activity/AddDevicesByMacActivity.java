package com.iboxshare.testble.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.dd.CircularProgressButton;
import com.iboxshare.testble.R;
import com.iboxshare.testble.model.DeviceInfo;
import com.iboxshare.testble.model.UserInfo;
import com.iboxshare.testble.util.PostTool;
import com.iboxshare.testble.util.Utils;

/**
 * Created by KN on 16/11/28.
 */

public class AddDevicesByMacActivity extends AppCompatActivity{
    private String TAG = "AddDevicesActivityByMac";
    private Context context = this;
    UserInfo user;
    Handler handler;
    CircularProgressButton CPB;
    EditText macET,lockNameET;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_devices_by_mac);
        user = (UserInfo) getIntent().getSerializableExtra("user");
        bindView();
        init();
    }

    void bindView(){
        CPB = (CircularProgressButton) findViewById(R.id.activity_add_devices_by_mac_btn);
        macET = (EditText) findViewById(R.id.activity_add_devices_by_mac_mac_ET);
        lockNameET = (EditText) findViewById(R.id.activity_add_devices_by_mac_lock_name_ET);
    }

    void init(){
        //设置handler
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 1:
                        Log.e(TAG,"添加成功");
                        CPB.setProgress(100);
                        Utils.showGlobalToast(context,"设备注册成功!");
                        //MainActivity.deviceList.add(new DeviceInfo(lockNameET.getText().toString(),macET.getText().toString(),-999));
                        //MainActivity.adapter.notifyDataSetChanged();
                        finish();
                        break;
                    case 0:
                        Log.e(TAG,"添加失败");
                        CPB.setProgress(-1);
                        break;
                }
            }
        };


        CPB.setIndeterminateProgressMode(true);
        CPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CPB.setProgress(0);
                CPB.setProgress(50);
                String mac = macET.getText().toString();
                final String lockName = lockNameET.getText().toString();
                final String admin = user.getUser_name();

                if (mac.isEmpty()||lockName.isEmpty()){
                    CPB.setErrorText("请输入正确的设备信息");
                    CPB.setProgress(-1);
                    return;
                }else {
                    //正则表达式判断是否为正确的mac地址
                    String regex = "([A-Fa-f0-9]{2}[:]){5}[A-Fa-f0-9]{2}";

                    if (mac.matches(regex)){
                        mac = mac.toUpperCase();
                        final String finalMac = mac;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (PostTool.addDevicesByMac(finalMac,lockName,admin)){
                                    handler.sendEmptyMessage(1);
                                }else {
                                    handler.sendEmptyMessage(0);
                                }
                            }
                        }).start();
                    }else {
                        CPB.setErrorText("MAC地址格式错误");
                        CPB.setProgress(-1);
                    }
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        MainActivity.BLE.disconnect();
        MainActivity.BLE.close();
    }
}
