package com.iboxshare.testble.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dd.CircularProgressButton;
import com.iboxshare.testble.R;
import com.iboxshare.testble.util.BLEUtils;
import com.iboxshare.testble.util.BluetoothLeClass;
import com.iboxshare.testble.util.Constant;
import com.iboxshare.testble.util.Utils;

/**
 * Created by KN on 16/9/13.
 */
public class UnlockActivity extends AppCompatActivity {
    private String TAG = "UnlockActivity";
    public Context context = this;
    public static CircularProgressButton CPB;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);
        CPB = (CircularProgressButton) findViewById(R.id.activity_unlock_btn);
        CPB.setIndeterminateProgressMode(true);
        CPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"点击按钮");
                CPB.setProgress(50);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothLeClass unlockBle = MainActivity.BLE;
                        unlockBle.setOnDataAvailableListener(MainActivity.onDataAvailableListener);
                        unlockBle.setOnServiceDiscoverListener(MainActivity.onServiceDiscoverListener);

                        BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR + "01" + "05" + BLEUtils.toHexString("123456789abcdefg") + Constant.END_CHAR),MainActivity.BLE);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR + Constant.SEVER_BLUETOOTH + "04" + "09" + BLEUtils.toHexString("root") + Constant.END_CHAR),MainActivity.BLE);

                        //当开锁成功时，onDataAvailableListener会将CircularProgressButton的progress设置为100
                        while (true){
                            if (CPB.getProgress() == 100) UnlockActivity.this.finish();

                        }
                    }
                }).start();
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
