package com.iboxshare.testble.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.internal.ParcelableSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.util.Util;
import com.iboxshare.testble.R;
import com.iboxshare.testble.adapter.DevicesAdapter;
import com.iboxshare.testble.model.DeviceInfo;
import com.iboxshare.testble.model.UserInfo;
import com.iboxshare.testble.myInterface.RecyclerViewOnItemClickListener;
import com.iboxshare.testble.util.BLEUtils;
import com.iboxshare.testble.util.BluetoothLeClass;
import com.iboxshare.testble.util.Constant;
import com.iboxshare.testble.util.Utils;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KN on 16/9/7.
 */
public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private Context context = this;
    private UserInfo user;
    private DevicesAdapter adapter = new DevicesAdapter();
    private List<String> macList = new ArrayList<>();
    private List<DeviceInfo> deviceList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
    private EasyRecyclerView easyRecyclerView;


    //蓝牙相关
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    public static BluetoothLeClass BLE;

    //回调相关
    public static BluetoothAdapter.LeScanCallback leScanCallback;
    public static BluetoothLeClass.OnDataAvailableListener onDataAvailableListener;
    public static BluetoothLeClass.OnServiceDiscoverListener onServiceDiscoverListener;

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = (UserInfo) getIntent().getExtras().get("user");
        bindView();
        init();
        initCallback();

    }


    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
        scanDevices(true);
    }


    /**
     * onStop
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"onStop");
        scanDevices(false);
    }


    /**
     * onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        BLE.disconnect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case 0:
                //没有设置密码
                Log.e(TAG,"没有设置触屏密码");
                break;
            case 1:
                //设置了触屏密码
                int password = data.getIntExtra("password",0);
                if (password == 0){
                    Utils.showGlobalToast(context,"未知错误");
                }else {
                    BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR
                            + Constant.SEVER_BLUETOOTH
                            + "01"
                            + "0b"
                            + BLEUtils.toHexString("123456")
                            + Constant.END_CHAR),MainActivity.BLE);
                }
                break;
        }
    }



    /**
     * 绑定组件
     */
    void bindView() {
        easyRecyclerView = (EasyRecyclerView) findViewById(R.id.activity_main_easyRecyclerView);
    }


    /**
     * 初始化
     */
    void init() {
        //获取用户信息中的mac地址列表
        if (!(user.getMac() == null)) {
//            不能直接这样赋值
//            macList = user.getMac();
            macList.addAll(user.getMac());
            Log.e("DevicesNum", String.valueOf(macList.size()));
            for (String mac :
                    macList) {
                if (!(mac.isEmpty())) {
                    DeviceInfo device = new DeviceInfo();
                    device.setName("设备不在附近");
                    device.setSignal(-999);
                    device.setMac(mac);
                    deviceList.add(device);
                }

            }
            Log.e("device数量", String.valueOf(deviceList.size()));
            adapter.setData(context, deviceList);
            easyRecyclerView.setLayoutManager(linearLayoutManager);
            easyRecyclerView.setAdapter(adapter);
        }

//        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        bluetoothAdapter = bluetoothManager.getAdapter();
//        if (bluetoothAdapter != null){
//            bluetoothAdapter.enable();
//        }else {
//            Log.e(TAG,"初始化蓝牙适配器失败");
//        }

        BLE = new BluetoothLeClass(context);

        //初始化蓝牙
        if (BLE.initialize()){
            Log.e(TAG,"蓝牙初始化成功");
            bluetoothAdapter = BLE.getBluetoothAdapter();
        }else{
            Log.e(TAG,"蓝牙初始化失败");
        }


        //给Adapter每个Item设置监听事件
        adapter.setOnItemClickListener(new RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                //获取到position
                int position = (int) view.findViewById(R.id.item_device_details_mac).getTag();
                if (BLE.connect(deviceList.get(position).getMac())){
                    scanDevices(false);
                    Log.e(TAG,"连接成功!");
                    BLE.setOnServiceDiscoverListener(onServiceDiscoverListener);
                    BLE.setOnDataAvailableListener(onDataAvailableListener);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR + "01" + "05" + BLEUtils.toHexString("123456789abcdefg") + Constant.END_CHAR),MainActivity.BLE);

                    Intent intent = new Intent(context,UnlockActivity.class);
                    startActivity(intent);

                }else {
                    Log.e(TAG,"连接失败");
                }
            }
        });


        //扫描设备
        scanDevices(true);



    }


    /**
     * 初始化回调
     */
    void initCallback(){
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (macList.contains(device.getAddress())){
                    Log.e("此设备在附近",device.getAddress() + "===>" + "信号强度为:" + rssi);
                    updateDeviceInfo(device.getAddress(),device.getName(),rssi);
                }

            }
        };



        onServiceDiscoverListener = new BluetoothLeClass.OnServiceDiscoverListener() {
            @Override
            public void onServiceDiscover(BluetoothGatt gatt) {
                BLEUtils.displayGattServices(BLE.getSupportedGattServices(),BLE);
            }
        };


        onDataAvailableListener = new BluetoothLeClass.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.e("读取终端数据回调: ", gatt.getDevice().getAddress()
                        + "===>"
                        + characteristic.getUuid().toString()
                        + BLEUtils.bytesToHexString(characteristic.getValue()));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                String str = BLEUtils.bytesToHexString(characteristic.getValue());
                Log.e("终端写入数据回调", str);
                switch (str){

                    //请录入指纹
                    case "aa020b050d":
                        Utils.showGlobalToast(context,"请设置指纹");
                        break;

                    //添加指纹成功
                    case "aa020c050d":
                        Utils.showGlobalToast(context,"添加指纹成功");
                        break;

                    //设置触屏密码
                    case "aa0200050d":
                        Utils.showGlobalToast(context,"请设置触屏密码");
                        Intent intent = new Intent(context,SetScreenPasswordActivity.class);
                        startActivityForResult(intent,1);
                        break;
                    //设置触屏密码成功
                    case "aa0202050d":
                        Utils.showGlobalToast(context,"触屏密码设置成功");
                        Log.e(TAG,"触屏密码设置成功");
                        break;

                    //请进行开锁操作
                    case "aa020f050d":
                        Utils.showGlobalToast(context,"请进行开锁操作");
                        break;
                    case "aa0210050d":
                        Utils.showGlobalToast(context,"开锁成功!");
                        break;
                }
            }
        };
    }

    /**
     * 扫描BLE设备
     */
    void scanDevices(boolean enable){

        if (bluetoothAdapter != null){
            if (enable){
                bluetoothAdapter.startLeScan(leScanCallback);

            }else {
                bluetoothAdapter.stopLeScan(leScanCallback);
            }
        }
    }


    /**
     * 更新列表中设备的信息
     * @param mac   mac地址
     * @param name  设备名称
     * @param rssi  信号强度
     */
    void updateDeviceInfo(String mac,String name,int rssi){
        for (DeviceInfo device:
             deviceList) {
            if (device.getMac().equals(mac)){
                device.setName(name);
                device.setSignal(rssi);
            }
            //通知设备信息更新
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * 向设备发送设置触屏密码指令
     * @param passowrd  触屏密码
     */
    public static void setScreenPassword(int passowrd){
        Log.e("设置的触屏密码是: ", String.valueOf(passowrd));
        BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR
                + Constant.SEVER_BLUETOOTH
                + "01"
                + "0b"
                + BLEUtils.toHexString("123456")
                + Constant.END_CHAR),MainActivity.BLE);
    }
}
