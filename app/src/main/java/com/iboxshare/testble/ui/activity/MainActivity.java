package com.iboxshare.testble.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dd.processbutton.iml.ActionProcessButton;
import com.iboxshare.testble.R;
import com.iboxshare.testble.adapter.DevicesAdapter;
import com.iboxshare.testble.model.DeviceInfo;
import com.iboxshare.testble.myInterface.RecyclerViewOnItemClickListener;
import com.iboxshare.testble.util.BLEUtils;
import com.iboxshare.testble.util.BluetoothLeClass;
import com.iboxshare.testble.util.Constant;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity";

    String[] PERMISSIONS = {"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"};
    private Context context = this;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private static BluetoothLeClass BLE;
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
    ScanCallback scanCallback;
    BluetoothLeClass.OnDataAvailableListener dataAvailableListener;
    BluetoothLeClass.OnServiceDiscoverListener serviceDiscoverListener;



    //Flags
    private static int DEVICES_NUM = 0;


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

    /**
     * OnCreate
     * @param savedInstanceState    savedInstanceState
     */
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
        Log.e("本机mac地址",bluetoothAdapter.getAddress().toString());
        scanDevices(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        scanDevices(false);
        BLE.disconnect();

        //清零后在onResume时才会刷新UI
        DEVICES_NUM = 0;
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

        //为每个Item设置监听事件
        deviceAdapter.setOnItemClickListener(new RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                //获取到Item对应的位置
                int position = (int) view.findViewById(R.id.item_device_details_mac).getTag();
                Log.e(TAG, String.valueOf(position));
                Log.e(TAG,deviceInfoList.get(position).getMac());
                boolean connectResult = BLE.connect(deviceInfoList.get(position).getMac());
                if (connectResult){
                    Log.e(TAG,"蓝牙连接成功");
                    String str = BLEUtils.toHexString("root" + "123456");
                    writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR + "0f" + Constant.LOGIN + Constant.SEVER_BLUETOOTH + str + Constant.END_CHAR));
                }

            }
        });

        //针对Android M及以上设备申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(PERMISSIONS,1);
        }
        //启动蓝牙
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.enable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
        String bluetoothAddress = bluetoothAdapter.getAddress();
        Log.e(TAG,bluetoothAddress);

        BLE = new BluetoothLeClass(this);
        if (!BLE.initialize()){
            Log.e(TAG,"初始化失败");
        }else {
            //BLE设备初始化成功后操作
            String str = "root";
            Log.e(TAG, Arrays.toString(BLEUtils.hexStringToBytes(BLEUtils.toHexString(str))));

            //为BLE设置OnDataAvailableListener
            BLE.setOnDataAvailableListener(dataAvailableListener);

            //为BLE设置OnServiceDiscoverListener
            BLE.setOnServiceDiscoverListener(serviceDiscoverListener);

        }

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

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                }
            };
        }

*/

        //扫描回调
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


        //发现BLE设备Services时的回调
        serviceDiscoverListener = new BluetoothLeClass.OnServiceDiscoverListener() {
            @Override
            public void onServiceDiscover(BluetoothGatt gatt) {
                displayGattServices(BLE.getSupportedGattServices());
            }
        };

        //数据交互回调
        dataAvailableListener = new BluetoothLeClass.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.e("读取终端数据: ",gatt.getDevice().getAddress()
                        + "===>"
                        + characteristic.getUuid().toString()
                        + BLEUtils.bytesToHexString(characteristic.getValue()));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                String str = BLEUtils.bytesToHexString(characteristic.getValue());
                Log.e("终端写入数据",str);

            }
        };

    }

    /**
     * 点击事件
     * @param v 传入View
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
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


    /**
     * 写入bytes数据
     * @param bytes bytes数据
     */
    public static void writeChar(byte[] bytes) {
        Log.i("writeChar", "Message = " + bytes);
        if (gattCharacteristic_char != null) {
            gattCharacteristic_char.setValue(bytes);
            BLE.writeCharacteristic(gattCharacteristic_char);
        }
    }

    /**
     * 列出所有服务
     * @param gattServices  GattServices
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        BluetoothGattCharacteristic Characteristic_cur = null;

        for (BluetoothGattService gattService : gattServices) {
            // -----Service的字段信息----//
            int type = gattService.getType();
            Log.e(TAG, "-->service type:" + BLEUtils.getServiceType(type));
            Log.e(TAG, "-->includedServices size:"
                    + gattService.getIncludedServices().size());
            Log.e(TAG, "-->service uuid:" + gattService.getUuid());

            // -----Characteristics的字段信息----//
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.e(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG,
                        "---->char permission:"
                                + BLEUtils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                Log.e(TAG,
                        "---->char property:"
                                + BLEUtils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG, "---->char value:" + new String(data));
                }

                if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR6)) {
                    // 把char1 保存起来以方便后面读写数据时使用
                    gattCharacteristic_char = gattCharacteristic;
                    Characteristic_cur = gattCharacteristic;
                    BLE.setCharacteristicNotification(gattCharacteristic, true);
                    Log.i(TAG, "+++++++++UUID_CHAR");
                }

                if (gattCharacteristic.getUuid().toString()
                        .equals(UUID_HERATRATE)) {
                    // 把heartrate 保存起来以方便后面读写数据时使用
                    gattCharacteristic_heartrate = gattCharacteristic;
                    Characteristic_cur = gattCharacteristic;
                    BLE.setCharacteristicNotification(gattCharacteristic, true);
                    Log.i(TAG, "+++++++++UUID_HERATRATE");
                }

                if (gattCharacteristic.getUuid().toString()
                        .equals(UUID_KEY_DATA)) {
                    // 把heartrate 保存起来以方便后面读写数据时使用
                    gattCharacteristic_keydata = gattCharacteristic;
                    Characteristic_cur = gattCharacteristic;
                    BLE.setCharacteristicNotification(gattCharacteristic, true);
                    Log.i(TAG, "+++++++++UUID_KEY_DATA");
                }

                if (gattCharacteristic.getUuid().toString()
                        .equals(UUID_TEMPERATURE)) {
                    // 把heartrate 保存起来以方便后面读写数据时使用
                    gattCharacteristic_temperature = gattCharacteristic;
                    Characteristic_cur = gattCharacteristic;
                    BLE.setCharacteristicNotification(gattCharacteristic, true);
                    Log.i(TAG, "+++++++++UUID_TEMPERATURE");
                }

                // -----Descriptors的字段信息----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic
                        .getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG,
                            "-------->desc permission:"
                                    + BLEUtils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:" + new String(desData));
                    }
                }
            }
        }

    }



    //初始化变量
    public static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
    public static String UUID_HERATRATE = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String UUID_TEMPERATURE = "00002a1c-0000-1000-8000-00805f9b34fb";

    static BluetoothGattCharacteristic gattCharacteristic_char = null;
    static BluetoothGattCharacteristic gattCharacteristic_heartrate = null;
    static BluetoothGattCharacteristic gattCharacteristic_keydata = null;
    static BluetoothGattCharacteristic gattCharacteristic_temperature = null;
}
