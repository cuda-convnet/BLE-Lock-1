package com.iboxshare.testble.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dd.processbutton.iml.ActionProcessButton;
import com.iboxshare.testble.R;
import com.iboxshare.testble.adapter.DevicesAdapter;
import com.iboxshare.testble.model.DeviceInfo;
import com.iboxshare.testble.util.BLEUtils;
import com.iboxshare.testble.util.BluetoothLeClass;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity";

    String[] PERMISSIONS = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};
    private Context context = this;
    private static ActionProcessButton actionProcessButton;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeClass BLE;
    private BluetoothLeScanner bluetoothLeScanner;
    private EasyRecyclerView easyRecyclerView;
    private List<DeviceInfo> deviceInfoList = new ArrayList<>();
    private HashMap<String,DeviceInfo> devicesHashMap = new HashMap<>();
    private DevicesAdapter deviceAdapter = new DevicesAdapter();
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                //setAdapter
                case 1:
                    easyRecyclerView.setAdapter(deviceAdapter);
                    break;
                //notifyDataChanged
                case 2:
                    deviceAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    //回调
    BluetoothAdapter.LeScanCallback leScanCallback;
    //Flags
    private static int DEVICES_NUM = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
        init();
        initCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        scanDevices(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        scanDevices(false);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestory");
        super.onDestroy();
    }


    /**
     * 绑定控件
     */
    void bindView(){
        actionProcessButton = (ActionProcessButton) findViewById(R.id.activity_main_APB_unlock);
        easyRecyclerView = (EasyRecyclerView) findViewById(R.id.activity_main_easyRecyclerView);
    }

    /**
     * 初始化数据
     */
    void init(){
        //为EasyRecyclerView设置LayoutManager
        easyRecyclerView.setLayoutManager(linearLayoutManager);
        //为DevicesAdapter设置数据
        deviceAdapter.setData(deviceInfoList,context);

        //针对Android M及以上设备申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(PERMISSIONS,1);
        }
        //启动蓝牙
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.enable();
        String bluetoothAddress = bluetoothAdapter.getAddress();
        Log.e(TAG,bluetoothAddress);

        BLE = new BluetoothLeClass(this);
        if (!BLE.initialize()){
            Log.e(TAG,"初始化失败");
        }else {

            String str = "root";
            Log.e(TAG, String.valueOf(BLEUtils.hexStringToBytes(BLEUtils.toHexString(str))));
        }

        //设置解锁按钮
        actionProcessButton.setOnClickListener(this);
    }

    /**
     * 扫描BLE设备
     */
    void scanDevices(boolean enable){
        if (enable){
            bluetoothAdapter.startLeScan(leScanCallback);
        }else{
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }


    /**
     * 初始化回调
     */
    void initCallback(){

            leScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.e(TAG,device.getName()+"   "+device.getAddress());
                    String name = device.getName();
                    String address = device.getAddress();
                    int signal = rssi;


                    DeviceInfo temp = new DeviceInfo(name,address,signal);
                    devicesHashMap.put(address,temp);

                    //通知相应mac地址的设备的信号强度变化
                    signalStrengthChanged(signal,address);
                    handler.sendEmptyMessage(2);

                    Log.e(TAG, String.valueOf(devicesHashMap.get(address).getSignal()));
                    Log.e(TAG, String.valueOf(devicesHashMap.size()));

                    //当被发现设备数目发生变化则更新DeviceInfoList
                    if (devicesHashMap.size() != DEVICES_NUM){
                        deviceInfoList.clear();
                        for (Map.Entry<String,DeviceInfo> entry : devicesHashMap.entrySet()){
                            deviceInfoList.add(entry.getValue());

                            //通知easyRecyclerView数据更改
                            if (easyRecyclerView.getAdapter() == null){
                                handler.sendEmptyMessage(1);
                            }else {
                                handler.sendEmptyMessage(2);
                            }
                        }
                        DEVICES_NUM = devicesHashMap.size();
                        Log.e(TAG, "LIST SIZE = "+deviceInfoList.size());
                    }

                }
            };


    }


    /**
     * 点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activity_main_APB_unlock:
                //设置解锁状态
                isUnlock();
                break;
        }
    }

    /**
     * 通知信号变化
     * @param signalStrength 信号强度
     * @param mac mac地址
     */
    void signalStrengthChanged(int signalStrength,String mac){
        if (devicesHashMap.containsKey(mac)){
            for (DeviceInfo device : deviceInfoList){
                if (device.getMac().equals(mac)){
                    device.setSignal(signalStrength);
                }
            }
        }
    }


    public static void isUnlock(){
        actionProcessButton.setText("正在解锁");
        actionProcessButton.setPressed(true);
        actionProcessButton.setEnabled(false);
        actionProcessButton.setProgress(50);
    }

}
